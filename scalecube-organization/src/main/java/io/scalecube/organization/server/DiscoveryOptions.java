package io.scalecube.organization.server;

import io.scalecube.net.Address;
import java.util.List;
import java.util.Optional;

public final class DiscoveryOptions {

  private List<String> seeds;
  private Integer servicePort;
  private Integer discoveryPort;
  private String memberHost;
  private Integer memberPort;

  public int servicePort() {
    return servicePort != null ? servicePort : 0;
  }

  public Integer discoveryPort() {
    return discoveryPort != null ? discoveryPort : 0;
  }

  /**
   * Returns seeds as an {@link Address}'s array.
   *
   * @return {@link Address}'s array
   */
  public Address[] seeds() {
    return Optional.ofNullable(seeds)
        .map(seeds -> seeds.stream().map(Address::from).toArray(Address[]::new))
        .orElse(new Address[0]);
  }

  public String memberHost() {
    return memberHost;
  }

  public Integer memberPort() {
    return memberPort;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DiscoveryOptions{");
    sb.append("seeds=").append(seeds);
    sb.append(", servicePort=").append(servicePort);
    sb.append(", discoveryPort=").append(discoveryPort);
    sb.append(", memberHost=").append(memberHost);
    sb.append(", memberPort=").append(memberPort);
    sb.append('}');
    return sb.toString();
  }
}
