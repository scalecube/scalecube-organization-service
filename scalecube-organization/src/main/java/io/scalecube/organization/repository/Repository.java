package io.scalecube.organization.repository;

import java.util.Optional;

/**
 * Represents a repository of entities of type T with entity I type.
 *
 * @param <T> Entity type.
 * @param <I> Entity id type.
 */
public interface Repository<T, I> {

  /**
   * Determines if a entity with property corresponding to the <code>propertyName</code> argument
   * and property value matching the <code>propertyValue</code> exists in this repository.
   *
   * @param propertyName The entity property name search criteria.
   * @param propertyValue The entity property value search criteria.
   * @return True if an entity with matching criteria exists in this repository; False otherwise.
   */
  boolean existByProperty(String propertyName, Object propertyValue);

  /**
   * Attempts to return an entity corresponding to the <code>I</code> argument in this repository.
   *
   * @param id The entity I to return.
   * @return An optional of an entity.
   */
  Optional<T> findById(I id);

  /**
   * Determines if an entity corresponding to the <code>I</code> argument exists in this
   * repository.
   *
   * @param id Search entity I criteria.
   * @return True if an entity exists in this repository; False otherwise.
   */
  boolean existsById(I id);

  /**
   * Saves the <code>entity</code> entity to this repository using the <code>I</code> argument.
   *
   * @param id The entity I.
   * @param entity The entity to save.
   * @return The saved entity.
   */
  T save(I id, T entity);

  /**
   * Removes an entity with I corresponding to the <code>I</code> argument from this repository.
   *
   * @param id The I of the entity to be deleted.
   */
  void deleteById(I id);

  /**
   * Returns an <code>Iterable</code> of all entities in this repository.
   *
   * @return an <code>Iterable</code>.
   */
  Iterable<T> findAll();
}
