package io.scalecube.organization.repository.couchbase;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static java.util.Objects.requireNonNull;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.exception.DataRetrievalFailureException;
import io.scalecube.organization.repository.exception.OperationInterruptedException;
import io.scalecube.organization.repository.exception.QueryTimeoutException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import rx.Observable;
import rx.functions.Func1;

/**
 * Abstract base couchbase <Code>Repository</Code> implementation.
 *
 * @param <T> This repository entity type.
 * @param <I> This repository entity Id type which extends <codes>java.lang.String</codes>.
 */
abstract class CouchbaseEntityRepository<T, I extends String> implements Repository<T, I> {

  private static final String BUCKET_PASSWORD = ".bucket.password";
  private static final String BUCKET = ".bucket";
  private final CouchbaseExceptionTranslator exceptionTranslator =
      new CouchbaseExceptionTranslator();
  private final TranslationService translationService = new JacksonTranslationService();
  protected final CouchbaseSettings settings;
  protected final Cluster cluster;
  private final Class<T> type;
  private String bucketName;

  CouchbaseEntityRepository(
      CouchbaseSettings settings, Cluster cluster, String alias, Class<T> type) {
    this.settings = requireNonNull(settings);
    this.cluster = requireNonNull(cluster);
    this.type = requireNonNull(type);
    if (alias != null) {
      this.bucketName = settings.getProperty(alias + BUCKET);
    }
  }

  String getBucketName() {
    return bucketName;
  }

  @Override
  public boolean existByProperty(String propertyName, Object propertyValue) {
    return false;
  }

  @Override
  public Optional<T> findById(I id) {
    return findById(client(), id);
  }

  protected Optional<T> findById(Bucket client, I id) {
    requireNonNull(id);
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
  public boolean existsById(I id) {
    return existsById(client(), id);
  }

  protected boolean existsById(Bucket client, I id) {
    requireNonNull(id);
    return execute(() -> client.exists(id), client);
  }

  @Override
  public T save(I id, T entity) {
    return save(client(), id, entity);
  }

  protected T save(Bucket client, I id, T entity) {
    requireNonNull(id);
    requireNonNull(entity);

    execute(
        () -> client.upsert(RawJsonDocument.create(id, translationService.encode(entity))), client);
    return entity;
  }

  @Override
  public void deleteById(I id) {
    requireNonNull(id);
    deleteById(client(), id);
  }

  protected void deleteById(Bucket client, I id) {
    execute(() -> client.remove(id), client);
  }

  @Override
  public Iterable<T> findAll() {
    return findAll(client());
  }

  protected Iterable<T> findAll(Bucket client) {
    requireNonNull(client);
    final SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(client.name())));

    try {
      return executeAsync(client.async().query(query))
          .flatMap(
              result ->
                  result
                      .rows()
                      .mergeWith(
                          result
                              .errors()
                              .flatMap(
                                  error ->
                                      Observable.error(
                                          new DataRetrievalFailureException(
                                              "N1QL error: " + error.toString()))))
                      .flatMap(
                          row ->
                              Observable.just(
                                  translationService.decode(
                                      row.value().get(client.name()).toString(), type)))
                      .toList())
          .toBlocking()
          .single();
    } finally {
      client.close();
    }
  }

  protected Bucket client() {
    return cluster.openBucket(bucketName, settings.getCouchbasePassword());
  }

  private <R> Observable<R> executeAsync(Observable<R> asyncAction) {
    return asyncAction.onErrorResumeNext(
        (Func1<Throwable, Observable<R>>)
            e -> {
              if (e instanceof RuntimeException) {
                return Observable.error(
                    exceptionTranslator.translateExceptionIfPossible((RuntimeException) e));
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

  protected <R> R execute(BucketCallback<R> action, Bucket client) {
    requireNonNull(client);
    requireNonNull(action);

    try {
      return action.doInBucket();
    } catch (RuntimeException ex) {
      throw exceptionTranslator.translateExceptionIfPossible(ex);
    } catch (TimeoutException ex) {
      throw new QueryTimeoutException(ex.getMessage(), ex);
    } catch (InterruptedException | ExecutionException ex) {
      throw new OperationInterruptedException(ex.getMessage(), ex);
    } finally {
      client.close();
    }
  }
}
