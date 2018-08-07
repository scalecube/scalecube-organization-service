package io.scalecube.organization.repository.inmem;

import io.scalecube.organization.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * Abstract base in-memory <Code>Repository</Code> implementation.
 * @param <T> This repository entity type.
 * @param <Id> This repository entity Id type.
 */
public abstract class InMemoryEntityRepository<T, Id>
        implements Repository<T, Id> {
    private final HashMap<Id, T> entities = new HashMap<>();

    @Override
    public boolean existByProperty(String propertyName, Object propertyValue) {
        return false;
    }

    @Override
    public Optional<T> findById(Id id) {
        return entities.containsKey(id)
                ? Optional.of(entities.get(id))
                : Optional.empty();
    }

    @Override
    public boolean existsById(Id id) {
        return entities.containsKey(id);
    }

    @Override
    public T save(Id id, T t) {
        entities.put(id, t);
        return t;
    }

    @Override
    public void deleteById(Id id) {
        entities.remove(id);
    }

    @Override
    public Iterable<T> findAll() {
        return new ArrayList<>(entities.values());
    }

}
