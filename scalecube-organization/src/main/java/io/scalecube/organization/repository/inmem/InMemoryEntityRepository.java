package io.scalecube.organization.repository.inmem;

import io.scalecube.organization.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * Abstract base in-memory <Code>Repository</Code> implementation.
 *
 * @param <T> This repository entity type.
 * @param <I> This repository entity I type.
 */
public abstract class InMemoryEntityRepository<T, I>
    implements Repository<T, I> {

  private final HashMap<I, T> entities = new HashMap<>();

  @Override
  public boolean existByProperty(String propertyName, Object propertyValue) {
    return false;
  }

  @Override
  public Optional<T> findById(I id) {
    return entities.containsKey(id)
        ? Optional.of(entities.get(id))
        : Optional.empty();
  }

  @Override
  public boolean existsById(I id) {
    return entities.containsKey(id);
  }

  @Override
  public T save(I id, T entity) {
    entities.put(id, entity);
    return entity;
  }

  @Override
  public void deleteById(I id) {
    entities.remove(id);
  }

  @Override
  public Iterable<T> findAll() {
    return new ArrayList<>(entities.values());
  }

}
