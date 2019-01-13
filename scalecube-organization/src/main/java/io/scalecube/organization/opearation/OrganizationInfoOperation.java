package io.scalecube.organization.opearation;

import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.InvalidInputException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import io.scalecube.tokens.TokenVerifier;

public abstract class OrganizationInfoOperation<I, O> extends ServiceOperation<I, O> {
  private static final String VALID_ORG_NAME_CHARS_REGEX = "^[.%a-zA-Z0-9_-]*$";

  protected OrganizationInfoOperation(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  protected void validate(OrganizationInfo info, OperationServiceContext context) {
    requireNonNullOrEmpty(info.id(),
        "organizationId is a required argument");
    requireNonNullOrEmpty(info.email(), "Organization email cannot be empty");
    requireNonNullOrEmpty(info.name(), "Organization name cannot be empty");
    boolean invalidOrgNameValid = !info.name().matches(VALID_ORG_NAME_CHARS_REGEX);

    if (invalidOrgNameValid) {
      throw new InvalidInputException(
          "Organization name can only contain characters in range A-Z, a-z, 0-9 as well as "
              + "underscore, period, dash & percent.");
    }

    if (context.repository().existByName(info.name())) {
      throw new NameAlreadyInUseException(
          String.format("Organization name: '%s' already in use.",
              info.name()));
    }
  }
}
