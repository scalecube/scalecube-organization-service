package io.scalecube.organization.repository.inmem;

import io.scalecube.organization.repository.Repository;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class InMemoryEntityRepository<T, ID>
        implements Repository<T, ID> {
    private final HashMap<ID, T> entities
            = new HashMap<>();

    @Override
    public boolean existByProperty(String propertyName, Object propertyValue) {
        return false;
    }

    @Override
    public Optional<T> findById(ID id) {
        return entities.containsKey(id)
                ? Optional.of(entities.get(id))
                : Optional.empty();
    }

    @Override
    public boolean existsById(ID id) {
        return entities.containsKey(id);
    }

    @Override
    public T save(ID id, T t) {
        entities.put(id, t);
        return t;
    }

    @Override
    public void deleteById(ID id) {
        entities.remove(id);
    }

    @Override
    public Iterable<T> findAll() {
        return entities.values().stream().collect(Collectors.toList());
    }

}
