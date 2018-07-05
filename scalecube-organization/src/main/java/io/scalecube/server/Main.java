package io.scalecube.server;

//import io.scalecube.account.RedisAccountService;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.services.Microservices;

public class Main {

  /**
   * AccountBootstrap main.
   * 
   * @param args application params.
   */
  public static void main(String[] args) {

    PackageInfo info = new PackageInfo();

    final Microservices seed;

    if (PackageInfo.seedAddress() != null) {
      seed = Microservices.builder()
          //.services(RedisAccountService.builder().redisson(info.redisClient()).build())
              .services(OrganizationServiceImpl.builder().build())
              .seeds(PackageInfo.seedAddress())
              .startAwait();
    } else {
      seed = Microservices.builder()
          //.services(RedisAccountService.builder().redisson(info.redisClient()).build())
              .services(OrganizationServiceImpl.builder().build())
              .startAwait();
    }

    Logo.builder().tagVersion(info.version())
            .port(seed.cluster().address().port() + "")
            .ip(seed.cluster().address().host())
            .group(info.groupId())
            .artifact(info.artifactId())
            .javaVersion(PackageInfo.java())
            .osType(PackageInfo.os())
            .pid(PackageInfo.pid())
            .website()
            .draw();

  }

}
