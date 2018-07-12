package io.scalecube.organization.repository;

import io.scalecube.organization.repository.exception.EntityNotFoundException;

import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findById(ID id) throws EntityNotFoundException;
    boolean existsById(ID id);
    T save(ID id, T t);
    void deleteById(ID id);
    Iterable<T> findAll();
    Iterable<T> findAllById(Iterable<ID> ids);
}
