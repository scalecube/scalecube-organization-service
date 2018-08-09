package io.scalecube.config;

//@Configuration
//@EnableCouchbaseRepositories("io.scalecube.organization.repository")
public class AppConfiguration {//extends AbstractCouchbaseConfiguration {
    /*
    private static final String CB_USER = "cb_user";
    private static final String CB_PASSWORD = "qazwsx";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;


    @Bean("organizationRepository")
    public OrganizationRepository organizationRepository() {
        return organizationRepository;
    }

    @Bean("config")
    public AppConfiguration appConfiguration() {
        return this;
    }

    @Bean("userRepository")
    public UserRepository userRepository() {
        return userRepository;
    }


    @Override
    protected List<String> getBootstrapHosts() {
        return Arrays.asList("127.0.0.1");
    }

    @Override
    protected String getBucketName() {
        return "organizations";
    }

    @Override
    protected String getBucketPassword() {
        return null;
    }


    @Bean
    public Bucket usersBucket() throws Exception {
        return couchbaseCluster().openBucket("users");
    }


    @Bean
    public CouchbaseTemplate usersTemplate() throws Exception {
        CouchbaseTemplate template = new CouchbaseTemplate(
                couchbaseClusterInfo(),
                usersBucket(),
                mappingCouchbaseConverter(),
                translationService());
        template.setDefaultConsistency(getDefaultConsistency());
        return template;
    }

    @Override
    public ClusterInfo couchbaseClusterInfo() throws Exception {
        return this.couchbaseCluster().clusterManager().info();
    }

    @Override
    @Bean(destroyMethod = "disconnect", name = BeanNames.COUCHBASE_CLUSTER)
    public Cluster couchbaseCluster() throws Exception {
        return CouchbaseCluster
                .create(this.couchbaseEnvironment(), this.getBootstrapHosts())
                .authenticate(CB_USER, CB_PASSWORD);
    }

    @Override
    public void configureRepositoryOperationsMapping(
            RepositoryOperationsMapping baseMapping) {
        try {
            baseMapping.mapEntity(Profile.class, usersTemplate());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Bean(destroyMethod = "close", name = {BeanNames.COUCHBASE_BUCKET, "organizations"})
    public Bucket couchbaseClient() throws Exception {
        return couchbaseCluster().openBucket("organizations");
    }
    */

}