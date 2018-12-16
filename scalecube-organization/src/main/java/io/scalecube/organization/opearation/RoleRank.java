package io.scalecube.organization.opearation;

import io.scalecube.account.api.Role;

import java.util.HashMap;
import java.util.Map;

final class RoleRank {
  private static final Map<Role, RoleRank> roleToRank = new HashMap<>();
  private static final Map<Role, Integer> ranks = new HashMap<Role, Integer>() {{
      put(Role.Owner, 300);
      put(Role.Admin, 200);
      put(Role.Member, 100);
    }
  };

  private final Role role;

  static RoleRank from(Role role) {
    return roleToRank.computeIfAbsent(role, RoleRank::new);
  }

  private RoleRank(Role role) {
    this.role = role;
  }

  protected boolean isHigherRank(Role target) {
    return ranks.get(target).compareTo(ranks.get(role)) > 0;
  }
}
