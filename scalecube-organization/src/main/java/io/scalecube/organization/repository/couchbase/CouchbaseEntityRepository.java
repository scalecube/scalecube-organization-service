package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.ClusterInfo;
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

abstract class CouchbaseEntityRepository<T, ID extends String> implements Repository<T, ID> {
    protected CouchbaseCluster cluster;
    private Bucket client;
    private TranslationService translationService = new JacksonTranslationService();
    String bucketName;
    String bucketPassword;
    private final CouchbaseSettings settings;
    private final Class<T> type;
    private final CouchbaseExceptionTranslator exceptionTranslator = new CouchbaseExceptionTranslator();

    CouchbaseEntityRepository(String alias, Class<T> type) {
        this.settings = new CouchbaseSettings.Builder().build();
        this.bucketName = getBucketName(alias);
        this.bucketPassword = getBucketPassword(alias);
        this.type = type;
    }

    Bucket client() {
        return cluster.openBucket(bucketName, bucketPassword);
    }

    private String getBucketName(String alias) {
        return settings.getProperty(alias + ".bucket");
    }

    private String getBucketPassword(String alias) {
        return settings.getProperty(alias + ".bucket.password");
    }

    @Override
    public Optional<T> findById(ID id) {
        JsonDocument document = execute(() -> client().get(id));
        T entity = null;

        if (document != null) {
            entity = translationService.decode(document.content().toString(), type);
        }

        return Optional.ofNullable(entity);
    }

    @Override
    public boolean existsById(ID id) {
        return execute(() -> client().exists(id));
    }


    @Override
    public T save(ID id, T t) {
        execute(()->client().upsert(RawJsonDocument.create(id, translationService.encode(t))));
        return t;
    }



    @Override
    public void deleteById(ID id) {
        execute(() -> client().remove(id));
    }

    @Override
    public Iterable<T> findAll() {
        final SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(bucketName)));
        return executeAsync(client().async().query(query))
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
                                        row.value().get(bucketName).toString(), type)))
                        .toList()
                )
                .toBlocking()
                .single();
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
                });//.doOnCompleted(() -> cluster.disconnect());
    }

    CouchbaseCluster cluster() {
        if (cluster == null) {
            List<String> nodes = settings.getCouchbaseClusterNodes();

            this.cluster = nodes.isEmpty()
                    ? CouchbaseCluster.create()
                    : CouchbaseCluster.create(nodes);
        }
        return cluster;
    }


    private <R> R execute(BucketCallback<R> action) {
        cluster = cluster();

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
            //cluster.disconnect();
        }
    }
}
