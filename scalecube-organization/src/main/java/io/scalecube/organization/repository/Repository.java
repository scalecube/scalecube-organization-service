package io.scalecube.organization.repository;

import java.util.Optional;

public interface Repository<T, ID> {
    boolean existByProperty(String propertyName, Object propertyValue);
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    T save(ID id, T t);
    void deleteById(ID id);
    Iterable<T> findAll();
}
