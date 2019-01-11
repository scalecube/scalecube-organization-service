package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.organization.repository.couchbase.CouchbaseSettings;
import io.scalecube.services.Microservices;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service runner main entry point. */
public class OrganizationServiceRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceRunner.class);

  /**
   * Bootstrap main.
   *
   * @param args application params.
   */
  public static void main(String[] args) throws Exception {
    start();
    Thread.currentThread().join();
  }

  private static void start() {
    DiscoveryOptions discoveryOptions =
        ConfigRegistryConfiguration.configRegistry()
            .objectProperty("io.scalecube.organization", DiscoveryOptions.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));

    LOGGER.info("Starting organization service on {}", discoveryOptions);

    Microservices.builder()
        .discovery(
            options ->
                options
                    .seeds(discoveryOptions.seeds())
                    .port(discoveryOptions.discoveryPort())
                    .memberHost(discoveryOptions.memberHost())
                    .memberPort(discoveryOptions.memberPort()))
        .transport(options -> options.port(discoveryOptions.servicePort()))
        .services(createOrganizationService())
        .startAwait();
  }

  private static OrganizationService createOrganizationService() {
    CouchbaseSettings settings =
        ConfigRegistryConfiguration.configRegistry()
            .objectProperty(couchbaseSettingsBindingMap(), CouchbaseSettings.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load couchbase settings"));

    CouchbaseRepositoryFactory factory = new CouchbaseRepositoryFactory(settings);

    return new OrganizationServiceImpl.Builder()
        .organizationRepository(factory.organizations())
        .organizationMembershipRepository(factory.organizationMembers())
        .organizationMembershipRepositoryAdmin(factory.organizationMembersRepositoryAdmin())
        .build();
  }

  private static Map<String, String> couchbaseSettingsBindingMap() {
    Map<String, String> bindingMap = new HashMap<>();

    bindingMap.put("hosts", "couchbase.hosts");
    bindingMap.put("username", "couchbase.username");
    bindingMap.put("password", "couchbase.password");
    bindingMap.put("userRoles", "organizations.members.userRoles");
    bindingMap.put("bucketNamePattern", "organizations.members.bucketNamePattern");
    bindingMap.put("bucketType", "organizations.members.bucketType");
    bindingMap.put("bucketQuota", "organizations.members.bucketQuota");
    bindingMap.put("bucketReplicas", "organizations.members.bucketReplicas");
    bindingMap.put("bucketIndexReplicas", "organizations.members.bucketIndexReplicas");
    bindingMap.put("bucketEnableFlush", "organizations.members.bucketEnableFlush");
    bindingMap.put("organizationsBucketName", "organizations.bucket");

    return bindingMap;
  }
}
