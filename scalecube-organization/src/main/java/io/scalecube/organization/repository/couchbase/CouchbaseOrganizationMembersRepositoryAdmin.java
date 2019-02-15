package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Cluster;
import io.scalecube.organization.Organization;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.couchbase.admin.AdminOperationContext;
import io.scalecube.organization.repository.couchbase.admin.AdminOperationsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CouchbaseOrganizationMembersRepositoryAdmin
    implements OrganizationMembersRepositoryAdmin {

  private static final Logger logger =
      LoggerFactory.getLogger(CouchbaseOrganizationMembersRepositoryAdmin.class);
  private final CouchbaseSettings settings;
  private final Cluster cluster;

  CouchbaseOrganizationMembersRepositoryAdmin(CouchbaseSettings settings, Cluster cluster) {
    this.settings = settings;
    this.cluster = cluster;
  }

  @Override
  public void createRepository(Organization organization) {
    logger.debug("createRepository: enter: organization: {}", organization);
    String bucketName = getOrgMembersBucketName(organization);
    AdminOperationsFactory.insertBucket().execute(operationContext(bucketName));

    try {
      createPrimaryIndex(bucketName);
      insertUser(operationContext(bucketName, organization));
    } catch (Throwable throwable) {
      logger.error("createRepository: organization: {}, error: {}", organization, throwable);
      // rollback
      cluster.clusterManager().removeBucket(bucketName);
      throw throwable;
    }
    logger.debug("createRepository: exit: organization: {}", organization);
  }

  /**
   * To enable select queries on the members bucket, this method creates a primary index on the
   * bucket.
   *
   * @param bucketName the bucket name to create the index
   */
  private void createPrimaryIndex(String bucketName) {
    logger.debug("createPrimaryIndex: enter: name: {}", bucketName);
    AdminOperationsFactory.createPrimaryIndex().execute(operationContext(bucketName));
    logger.debug("createPrimaryIndex: exit: name: {}", bucketName);
  }

  /**
   * Couchbase Server 5.0 introduced role-based access control (RBAC). By creating a user with same
   * name as the org-members-bucket, we limit the access to this bucket only with the need for a
   * password when opening the bucket.
   *
   * @param operationContext the bucket name and organization object
   */
  private void insertUser(AdminOperationContext operationContext) {
    logger.debug("insetUser: enter: name: {}", operationContext);
    AdminOperationsFactory.insertUser().execute(operationContext);
    logger.debug("insetUser: exit: name: {}", operationContext);
  }

  @Override
  public void deleteRepository(Organization organization) {
    logger.debug("deleteRepository: enter: organization: {}", organization);
    String bucketName = getOrgMembersBucketName(organization);
    AdminOperationsFactory.deleteRepository().execute(operationContext(bucketName));
    logger.debug("deleteRepository: exit: organization: {}", organization);
  }

  private AdminOperationContext operationContext(String bucketName) {
    return operationContext(bucketName, null);
  }

  private AdminOperationContext operationContext(String bucketName, Organization organization) {
    return AdminOperationContext.builder()
        .settings(settings)
        .cluster(cluster)
        .name(bucketName)
        .organization(organization)
        .build();
  }

  private String getOrgMembersBucketName(Organization organization) {
    return String.format(settings.bucketNamePattern(), organization.id());
  }
}
