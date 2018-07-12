package io.scalecube.organization.repository.couchbase;

import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class CouchbaseEntityRepository<T, ID> implements Repository<T, ID> {
    private CrudRepository<T, ID> repository;


    CouchbaseEntityRepository(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }


    @Override
    public T save(ID id, T t) {
        return repository.save(t);
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public Iterable<T> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return StreamSupport.stream(repository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }}
