package io.scalecube.server;

//import io.scalecube.account.RedisAccountService;
import io.scalecube.account.api.User;
import io.scalecube.config.AppConfiguration;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.OrganizationRepository;
import io.scalecube.organization.repository.UserRepository;
import io.scalecube.services.Microservices;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
    Microservices
            .builder()
            .services(OrganizationServiceImpl
                    .builder()
                    .organizationRepository(organizationRepository)
                    .userRepository(userRepository)
                    .build())
            //.seeds(seed.cluster().address())
            .build()
            .startAwait();
  }

  static <T> T  getBean(ApplicationContext context, String beanName) {
    return (T)context.getBean(beanName);
  }

}
