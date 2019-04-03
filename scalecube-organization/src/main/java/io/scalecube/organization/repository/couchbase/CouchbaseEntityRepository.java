package io.scalecube.organization.repository.couchbase;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static java.util.Objects.requireNonNull;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.exception.DataRetrievalFailureException;
import io.scalecube.organization.repository.exception.OperationInterruptedException;
import io.scalecube.organization.repository.exception.QueryTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import rx.Observable;

/**
 * Abstract base couchbase <Code>Repository</Code> implementation.
 *
 * @param <T> This repository entity type.
 * @param <I> This repository entity Id type which extends <codes>java.lang.String</codes>.
 */
abstract class CouchbaseEntityRepository<T, I extends String> implements Repository<T, I> {

  private static final String SELECT_COUNT_BY_DOCUMENT_FIELD =
      "select count(id) as count from %s where %s = '%s'";

  private final CouchbaseExceptionTranslator exceptionTranslator =
      new CouchbaseExceptionTranslator();
  private final TranslationService translationService = new JacksonTranslationService();

  private final Bucket bucket;
  private final Class<T> type;

  CouchbaseEntityRepository(Bucket bucket, Class<T> type) {
    this.bucket = requireNonNull(bucket);
    this.type = requireNonNull(type);
  }

  @Override
  public boolean existByProperty(String propertyName, Object propertyValue) {
    N1qlQuery query =
        N1qlQuery.simple(
            String.format(
                SELECT_COUNT_BY_DOCUMENT_FIELD, bucket.name(), propertyName, propertyValue));
    N1qlQueryResult queryResult = bucket.query(query);
    List<N1qlQueryRow> rows = queryResult.allRows();
    return !rows.isEmpty() && rows.get(0).value().getInt("count") > 0;
  }

  @Override
  public Optional<T> findById(I id) {
    requireNonNull(id);
    return toEntity(execute(() -> bucket.get(id), bucket));
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
    requireNonNull(id);
    return execute(() -> bucket.exists(id), bucket);
  }

  @Override
  public T save(I id, T entity) {
    requireNonNull(id);
    requireNonNull(entity);

    execute(
        () -> bucket.upsert(RawJsonDocument.create(id, translationService.encode(entity))), bucket);
    return entity;
  }

  @Override
  public void deleteById(I id) {
    requireNonNull(id);
    execute(() -> bucket.remove(id), bucket);
  }

  @Override
  public Iterable<T> findAll() {
    requireNonNull(bucket);
    final SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(bucket.name())));

    return executeAsync(bucket.async().query(query))
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
                                    row.value().get(bucket.name()).toString(), type)))
                    .toList())
        .toBlocking()
        .single();
  }

  private <R> Observable<R> executeAsync(Observable<R> asyncAction) {
    return asyncAction.onErrorResumeNext(
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

  protected <R> R execute(BucketCallback<R> action, Bucket bucket) {
    requireNonNull(bucket);
    requireNonNull(action);

    try {
      return action.doInBucket();
    } catch (RuntimeException ex) {
      throw exceptionTranslator.translateExceptionIfPossible(ex);
    } catch (TimeoutException ex) {
      throw new QueryTimeoutException(ex.getMessage(), ex);
    } catch (InterruptedException | ExecutionException ex) {
      throw new OperationInterruptedException(ex.getMessage(), ex);
    }
  }
}
