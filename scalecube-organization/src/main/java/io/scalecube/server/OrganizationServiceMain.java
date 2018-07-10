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

public class OrganizationServiceDriver {

  /**
   * AccountBootstrap main.
   * 
   * @param args application params.
   */
  public static void main(String[] args) {

    PackageInfo info = new PackageInfo();

    //final Microservices seed;
    ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
    //System.out.println(context.getBean("organizations"));
    //UserRepository r =  (UserRepository)(context.getBean("users"));
    //User u = r.save(new User("1", "bla", true, "", "", "", "", "", null));
    //r.deleteById(u.id());
    Microservices.Builder serviceBuilder = Microservices
            .builder()
            .services(OrganizationServiceImpl
                    .builder()
                    .organizationRepository((OrganizationRepository) context.getBean("organizations"))
                    .userRepository((UserRepository) context.getBean("users"))
                    .build());

    if (PackageInfo.seedAddress() != null) {
      serviceBuilder = serviceBuilder.seeds(PackageInfo.seedAddress());
    }
//
    //seed =
            serviceBuilder.startAwait();
//
//    Logo.builder().tagVersion(info.version())
//            .port(seed.cluster().address().port() + "")
//            .ip(seed.cluster().address().host())
//            .group(info.groupId())
//            .artifact(info.artifactId())
//            .javaVersion(PackageInfo.java())
//            .osType(PackageInfo.os())
//            .pid(PackageInfo.pid())
//            .website()
//            .draw();

  }

}
