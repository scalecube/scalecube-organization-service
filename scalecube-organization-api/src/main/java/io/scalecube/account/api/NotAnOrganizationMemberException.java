package io.scalecube.account.api;

/**
 * Represents an business exception which occurs when a user is not a member of an organization.
 */
public class NotAnOrganizationMemberException extends Throwable {
  public NotAnOrganizationMemberException(String message) {
    super(message);
  }
}
