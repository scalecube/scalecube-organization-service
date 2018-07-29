package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.exception.DataRetrievalFailureException;
import io.scalecube.organization.repository.exception.OperationInterruptedException;
import io.scalecube.organization.repository.exception.QueryTimeoutException;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.google.common.base.Preconditions.checkNotNull;

abstract class CouchbaseEntityRepository<T, ID extends String> implements Repository<T, ID> {
    private static final String BUCKET_PASSWORD = ".bucket.password";
    private static final String BUCKET = ".bucket";
    private CouchbaseCluster cluster;
    private TranslationService translationService = new JacksonTranslationService();
    private String bucketName;
    private String bucketPassword;
    private final CouchbaseSettings settings;
    private final Class<T> type;
    private final CouchbaseExceptionTranslator exceptionTranslator = new CouchbaseExceptionTranslator();
    private static final Object lock = new Object();

    CouchbaseEntityRepository(String alias, Class<T> type) {
        checkNotNull(type);

        this.type = type;
        this.settings = new CouchbaseSettings.Builder().build();

        if (alias != null) {
            this.bucketName = getBucketName(alias);
            this.bucketPassword = getBucketPassword(alias);
        }
    }

    private String getBucketName(String alias) {
        return settings.getProperty(alias + BUCKET);
    }

    String getBucketName() {
        return bucketName;
    }

    private String getBucketPassword(String alias) {
        return settings.getProperty(alias + BUCKET_PASSWORD);
    }

    @Override
    public boolean existByProperty(String propertyName, Object propertyValue) {
        return false;
    }

    @Override
    public Optional<T> findById(ID id) {
        return findById(client(), id);
    }

    Optional<T> findById(Bucket client, ID id) {
        checkNotNull(id);
        return toEntity(execute(() -> client.get(id), client));
    }

    private Optional<T> toEntity(JsonDocument document) {
        T entity = null;

        if (document != null) {
            entity = translationService.decode(document.content().toString(), type);
        }

        return Optional.ofNullable(entity);
    }

    @Override
    public boolean existsById(ID id) {
        return existsById(client(), id);
    }

    boolean existsById(Bucket client,  ID id) {
        checkNotNull(id);
        return execute(() -> client.exists(id), client);
    }

    @Override
    public T save(ID id, T t) {
        return save(client(), id, t);
    }

    T save(Bucket client, ID id, T t) {
        checkNotNull(id);
        checkNotNull(t);

        execute(() -> client.upsert(RawJsonDocument.create(id, translationService.encode(t))), client);
        return t;
    }

    @Override
    public void deleteById(ID id) {
        checkNotNull(id);
        deleteById(client(), id);
    }

    void deleteById(Bucket client, ID id) {
        execute(() -> client.remove(id), client);
    }

    @Override
    public Iterable<T> findAll() {
        return findAll(client());
    }

    Iterable<T> findAll(Bucket client) {
        checkNotNull(client);
        final SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(client.name())));

        try {
            return executeAsync(client.async().query(query))
                    .flatMap(result -> result.rows()
                            .mergeWith(
                                    result
                                            .errors()
                                            .flatMap(
                                                    error -> Observable.error(new DataRetrievalFailureException(
                                                            "N1QL error: " + error.toString())))
                            )
                            .flatMap(row ->
                                    Observable.just(translationService.decode(
                                            row.value().get(client.name()).toString(), type)))
                            .toList()
                    )
                    .toBlocking()
                    .single();
        } finally {
            client.close();
        }
    }

    Bucket client() {
        return cluster().openBucket(bucketName, bucketPassword);
    }

    private <R> Observable<R> executeAsync(Observable<R> asyncAction) {
        return asyncAction
                .onErrorResumeNext((Func1<Throwable, Observable<R>>) e -> {
                    if (e instanceof RuntimeException) {
                        return Observable.error(exceptionTranslator.translateExceptionIfPossible((RuntimeException) e));
                    } else if (e instanceof TimeoutException) {
                        return Observable.error(new QueryTimeoutException(e.getMessage(), e));
                    } else if (e instanceof InterruptedException) {
                        return Observable.error(new OperationInterruptedException(e.getMessage(), e));
                    } else if (e instanceof ExecutionException) {
                        return Observable.error(new OperationInterruptedException(e.getMessage(), e));
                    } else {
                        return Observable.error(e);
                    }
                });
    }

    CouchbaseCluster cluster() {
        if (cluster == null) {
            synchronized (lock) {
                if (cluster == null) {
                    List<String> nodes = settings.getCouchbaseClusterNodes();

                    cluster = nodes.isEmpty()
                            ? CouchbaseCluster.create()
                            : CouchbaseCluster.create(nodes);
                }
            }
        }
        return cluster;
    }


    <R> R execute(BucketCallback<R> action, Bucket client) {
        checkNotNull(client);
        checkNotNull(action);

        try {
            return action.doInBucket();
        }
        catch (RuntimeException e) {
            throw exceptionTranslator.translateExceptionIfPossible(e);
        }
        catch (TimeoutException e) {
            throw new QueryTimeoutException(e.getMessage(), e);
        }
        catch (InterruptedException | ExecutionException e) {
            throw new OperationInterruptedException(e.getMessage(), e);
        } finally {
            client.close();
        }
    }
}
