package io.scalecube.organization.repository.couchbase;

import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.exception.EntityNotFoundException;

import java.util.Optional;

public abstract class CouchbaseEntityRepository<T, ID> implements Repository<T, ID> {
    private TranslationService translationService;

    @Override
    public Optional<T> findById(ID id) throws EntityNotFoundException {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ID id) {
        return false;
    }


    @Override
    public T save(ID id, T t) {
        return t;
    }

    @Override
    public void deleteById(ID id) {
    }

    @Override
    public Iterable<T> findAll() {
        return null;
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return null;
    }}
