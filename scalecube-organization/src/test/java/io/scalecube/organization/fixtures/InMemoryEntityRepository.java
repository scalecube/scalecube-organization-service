package io.scalecube.organization.fixtures;

import io.scalecube.organization.repository.Repository;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstract base in-memory <Code>Repository</Code> implementation.
 *
 * @param <T> This repository entity type.
 * @param <I> This repository entity I type.
 */
public abstract class InMemoryEntityRepository<T, I> implements Repository<T, I> {

  private final Map<I, T> entities = new HashMap<>();

  @Override
  public Mono<Boolean> existByProperty(String propertyName, Object propertyValue) {
    if (entities.isEmpty()) {
      return Mono.just(false);
    }
    try {
      Field field = entities.values().iterator().next().getClass().getDeclaredField(propertyName);
      field.setAccessible(true);
      return Mono.just(
          entities.values().stream()
              .anyMatch(
                  i -> {
                    try {
                      return Objects.equals(field.get(i), propertyValue);
                    } catch (IllegalAccessException e) {
                      e.printStackTrace();
                      return false;
                    }
                  }));
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    return Mono.just(false);
  }

  @Override
  public Mono<T> findById(I id) {
    return entities.containsKey(id) ? Mono.just(entities.get(id)) : Mono.empty();
  }

  @Override
  public Mono<Boolean> existsById(I id) {
    return Mono.just(entities.containsKey(id));
  }

  @Override
  public Mono<T> save(I id, T entity) {
    return Mono.fromCallable(() -> entities.put(id, entity)).then(Mono.just(entity));
  }

  @Override
  public Mono<Void> deleteById(I id) {
    return Mono.fromRunnable(() -> entities.remove(id)).then();
  }

  @Override
  public Flux<T> findAll() {
    return Flux.fromIterable(entities.values());
  }
}
