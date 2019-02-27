package io.scalecube.account.api;

public enum Role {
  Owner(300),
  Admin(200),
  Member(100);

  private final int rank;

  Role(int rank) {
    this.rank = rank;
  }

  public boolean isHigherThan(Role target) {
    return rank > target.rank;
  }
}
