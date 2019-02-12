package io.scalecube.organization.repository.couchbase.admin;

import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.query.N1qlQueryResult;

/**
 * Factory class for constructing admin operation classes.
 */
public abstract class AdminOperationsFactory {
  public static Operation<BucketSettings> insertBucket() {
    return new InsertBucketOperation();
  }

  public static Operation<N1qlQueryResult> createPrimaryIndex() {
    return new CreatePrimaryIndexOperation();
  }

  public static Operation<Boolean> insertUser() {
    return new InsertUserOperation();
  }

  public static Operation<Boolean> deleteRepository() {
    return new DeleteRepositoryOperation();
  }

}
