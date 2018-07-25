package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.User;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;

public class OrganizationServiceMain {

  /**
   * AccountBootstrap main.
   * 
   * @param args application params.
   */
  public static void main(String[] args) {
    //ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
    //final Microservices seed = Microservices.builder().build().startAwait();
    //AppConfiguration configuration = getBean(context, "config");
    //OrganizationRepository organizationRepository =
    //        getBean(context, "organizationRepository");

    //UserRepository userRepository = getBean(context, "userRepository");

    User testUser = new User("1", "user1@gmail.com", true, "name 1",
            "http://picture.jpg", "EN", "fname", "lname", null);


    OrganizationService service = OrganizationServiceImpl
            .builder()
            .organizationRepository(CouchbaseRepositoryFactory.organizations())
            .userRepository(CouchbaseRepositoryFactory.users())
            .organizationMembershipRepository(CouchbaseRepositoryFactory.organizationMembers())
            .organizationMembershipRepositoryAdmin(CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin())
            .build();



    //userRepository.save(testUser);
//    userRepository.findById(testUser.id());

    //organizationRepository.save(new Organization.Builder().id(String.valueOf(System.currentTimeMillis())).name("myorg").build());

//    OrganizationRepository organizationRepository =
//            getBean(context, "organizationRepository");
//
//    UserRepository userRepository = getBean(context, "userRepository");
//    OrganizationService service = OrganizationServiceImpl
//            .builder()
//            .organizationRepository(new CouchbaseOrganizationRepository(organizationRepository))
//            .userRepository(new CouchbaseUserRepository(userRepository))
//            .build();
//    Microservices
//            .builder()
//            .services(service)
//            //.seeds(seed.cluster().address())
//            .build()
//            .startAwait();
//    try {
//      System.in.read();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }

//  static <T> T  getBean(ApplicationContext context, String beanName) {
//    return (T)context.getBean(beanName);
//  }

}
