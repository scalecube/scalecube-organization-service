package io.scalecube.organization.repository;

import java.util.Optional;

/**
 * Represents a repository of entities of type T with entity Id type.
 *
 * @param <T> Entity type.
 * @param <Id> Entity id type.
 */
public interface Repository<T, Id> {

  /**
   * Determines if a entity with property corresponding to the <code>propertyName</code> argument
   * and property value matching the <code>propertyValue</code> exists in this repository.
   *
   * @param propertyName The entity's property name search criteria.
   * @param propertyValue The entity's property value search criteria.
   * @return True if an entity corresponding to the <code>propertyName</code> with value that
   * matches the <code>propertyValue</code> exists in this repository; False otherwise.
   */
  boolean existByProperty(String propertyName, Object propertyValue);

  /**
   * Attempts to return an entity corresponding to the <code>id</code> argument in this repository.
   *
   * @param id The entity id to return.
   * @return An optional of an entity.
   */
  Optional<T> findById(Id id);

  /**
   * Determines if an entity corresponding to the <code>id</code> argument exists in this
   * repository.
   *
   * @param id Search entity id criteria.
   * @return True if an entity corresponding to the <code>id</code> exists in this repository; False
   * otherwise.
   */
  boolean existsById(Id id);

  /**
   * Saves the <code>t</code> entity to this repository using the <code>id</code> argument.
   *
   * @param id The entity id.
   * @param t The entity to sae.
   * @return The saved entity.
   */
  T save(Id id, T t);

  /**
   * Removes an entity with Id corresponding to the <code>id<code/> argument from this repository.
   *
   * @param id The id of the entity to be delted.
   */
  void deleteById(Id id);

  /**
   * Returns an <code>Iterable<T><code/> of all entities in this repository.
   *
   * @return an <code>Iterable<T><code/> of all entities in this repository.
   */
  Iterable<T> findAll();
}
