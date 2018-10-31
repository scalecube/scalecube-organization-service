package io.scalecube.organization.repository.couchbase.admin;

import com.couchbase.client.java.cluster.AuthDomain;

final class DeleteRepositoryOperation extends Operation<Boolean> {

  @Override
  public Boolean execute(AdminOperationContext context) {
    context.cluster().clusterManager().removeUser(AuthDomain.LOCAL, context.name());
    context.cluster().clusterManager().removeBucket(context.name());
    return true;
  }
}
