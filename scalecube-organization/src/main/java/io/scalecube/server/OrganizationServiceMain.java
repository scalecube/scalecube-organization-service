package io.scalecube.server;

import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.User;
import io.scalecube.config.AppConfiguration;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.couchbase.CouchbaseOrganizationRepository;
import io.scalecube.organization.repository.couchbase.CouchbaseUserRepository;
import io.scalecube.organization.repository.couchbase.OrganizationRepository;
import io.scalecube.organization.repository.couchbase.UserRepository;
import io.scalecube.services.Microservices;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class OrganizationServiceMain {

  /**
   * AccountBootstrap main.
   * 
   * @param args application params.
   */
  public static void main(String[] args) {
    ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
    //final Microservices seed = Microservices.builder().build().startAwait();
    OrganizationRepository organizationRepository =
            getBean(context, "organizationRepository");
    UserRepository userRepository = getBean(context, "userRepository");
    OrganizationService service = OrganizationServiceImpl
            .builder()
            .organizationRepository(new CouchbaseOrganizationRepository(organizationRepository))
            .userRepository(new CouchbaseUserRepository(userRepository))
            .build();
//    Microservices
//            .builder()
//            .services(service)
//            //.seeds(seed.cluster().address())
//            .build()
//            .startAwait();
    try {
      System.in.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static <T> T  getBean(ApplicationContext context, String beanName) {
    return (T)context.getBean(beanName);
  }

}
