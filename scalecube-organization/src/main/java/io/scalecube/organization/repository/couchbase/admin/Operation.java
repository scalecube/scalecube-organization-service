package io.scalecube.organization.repository.couchbase.admin;

/**
 * Represents a Couchbase admin operation.
 * @param <R> return type of the admin operation
 */
public abstract class Operation<R> {

  /**
   * Executes this admin operation and return a value.
   * @param context context for executing the operation
   * @return a result of the operation execution
   */
  public abstract R execute(AdminOperationContext context);
}
