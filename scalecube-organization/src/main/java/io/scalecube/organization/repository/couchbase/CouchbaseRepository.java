package io.scalecube.organization.repository.couchbase;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static java.util.Objects.requireNonNull;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.organization.domain.Entity;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.exception.DataRetrievalFailureException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

/**
 * Abstract base couchbase <Code>Repository</Code> implementation.
 *
 * @param <T> This repository entity type.
 */
abstract class CouchbaseRepository<T extends Entity> implements Repository<T, String> {

  private static final String ID_CANNOT_BE_NULL = "id cannot be null";

  private static final String SELECT_COUNT_BY_DOCUMENT_FIELD_QUERY =
      "select count(id) as count from %s where %s = '%s'";

  private final TranslationService translationService = new JacksonTranslationService();

  private final AsyncBucket bucket;
  private final Class<T> type;

  CouchbaseRepository(AsyncBucket bucket, Class<T> type) {
    this.bucket = requireNonNull(bucket, "bucket cannot be null");
    this.type = requireNonNull(type, "entity type cannot be null");
  }

  @Override
  public Mono<Boolean> existByProperty(String propertyName, Object propertyValue) {
    return Mono.fromRunnable(() -> requireNonNull(propertyName, "property name cannot be null"))
        .then(
            Mono.fromCallable(
                () ->
                    N1qlQuery.simple(
                        String.format(
                            SELECT_COUNT_BY_DOCUMENT_FIELD_QUERY,
                            bucket.name(),
                            propertyName,
                            propertyValue))))
        .flatMap(
            query ->
                Mono.from(
                    RxReactiveStreams.toPublisher(
                        bucket.query(query).flatMap(AsyncN1qlQueryResult::rows))))
        .map(row -> row.value().getInt("count") > 0);
  }

  @Override
  public Mono<T> findById(String id) {
    return Mono.fromRunnable(() -> requireNonNull(id, ID_CANNOT_BE_NULL))
        .then(Mono.defer(() -> Mono.from(RxReactiveStreams.toPublisher(bucket.get(id)))))
        .map(this::toEntity)
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  private T toEntity(JsonDocument document) {
    T entity = translationService.decode(document.content().toString(), type);
    entity.version(document.cas());
    return entity;
  }

  @Override
  public Mono<Boolean> existsById(String id) {
    return Mono.fromRunnable(() -> requireNonNull(id, ID_CANNOT_BE_NULL))
        .then(Mono.defer(() -> Mono.from(RxReactiveStreams.toPublisher(bucket.exists(id)))))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  @Override
  public Mono<T> save(String id, T entity) {
    return Mono.fromRunnable(
        () -> {
          requireNonNull(id, ID_CANNOT_BE_NULL);
          requireNonNull(entity, type.getSimpleName() + " cannot be null");
        })
        .then(
            Mono.fromCallable(
                () ->
                    RawJsonDocument.create(
                        id, translationService.encode(entity), entity.version())))
        .flatMap(
            document -> {
              if (entity.version() == 0) {
                return Mono.from(RxReactiveStreams.toPublisher(bucket.insert(document)));
              } else {
                return Mono.from(RxReactiveStreams.toPublisher(bucket.replace(document)));
              }
            })
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .then(Mono.just(entity));
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return Mono.fromRunnable(() -> requireNonNull(id, ID_CANNOT_BE_NULL))
        .then(Mono.defer(() -> Mono.from(RxReactiveStreams.toPublisher(bucket.remove(id)))))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .then();
  }

  @Override
  public Flux<T> findAll() {
    final SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(bucket.name())));

    return Flux.from(
            RxReactiveStreams.toPublisher(
                bucket
                    .query(query)
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
                                                row.value().get(bucket.name()).toString(),
                                                type))))))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }
}
