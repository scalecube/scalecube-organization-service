package io.scalecube.account.api;

public class GetMembershipRequest {

  private Token token;

  GetMembershipRequest() {}

  public GetMembershipRequest(Token token) {
    this.token = token;
  }

  public Token token() {
    return this.token;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" [token=%s]", token);
  }
}
