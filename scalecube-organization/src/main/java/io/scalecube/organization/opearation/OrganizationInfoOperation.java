package io.scalecube.organization.opearation;

import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.InvalidInputException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import io.scalecube.tokens.TokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OrganizationInfoOperation<I, O> extends ServiceOperation<I, O> {
  private static final String VALID_ORG_NAME_CHARS_REGEX = "^[.%a-zA-Z0-9_-]*$";
  private static final Logger logger = LoggerFactory.getLogger(OrganizationInfoOperation.class);

  protected OrganizationInfoOperation(TokenVerifier tokenVerifier,
      OrganizationsDataAccess repository) {
    super(tokenVerifier, repository);
  }

  protected void validate(OrganizationInfo info, OperationServiceContext context) {
    logger.debug("OrganizationInfoOperation: validate: enter: info: {}", info);
    requireNonNullOrEmpty(info.id(),
        "organizationId is a required argument");
    requireNonNullOrEmpty(info.email(), "Organization email cannot be empty");
    requireNonNullOrEmpty(info.name(), "Organization name cannot be empty");
    requireNonNullOrEmpty(info.ownerId(), "Organization owner id cannot be empty");
    boolean invalidOrgNameValid = !info.name().matches(VALID_ORG_NAME_CHARS_REGEX);
    logger.debug("OrganizationInfoOperation: validate: enter: info: {}", info);

    logger.debug("OrganizationInfoOperation: validate: name: {}", info.name());
    if (invalidOrgNameValid) {
      throw new InvalidInputException(
          "Organization name can only contain characters in range A-Z, a-z, 0-9 as well as "
              + "underscore, period, dash & percent.");
    }

    logger.debug("OrganizationInfoOperation: validate: name not in use: {}", info.name());
    if (context.repository().existByName(info.name())) {
      throw new NameAlreadyInUseException(
          String.format("Organization name: '%s' already in use.",
              info.name()));
    }

    logger.debug("OrganizationInfoOperation: validate: exit: info: {}", info);
  }
}
