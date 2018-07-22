package io.scalecube.server;

import io.scalecube.account.api.User;
import org.springframework.context.ApplicationContext;

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

  static <T> T  getBean(ApplicationContext context, String beanName) {
    return (T)context.getBean(beanName);
  }

}
