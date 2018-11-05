package io.scalecube.organization.opearation;

import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationNotFound;
import io.scalecube.account.api.ServiceOperationException;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.security.Profile;
import io.scalecube.tokens.TokenVerifier;

import java.util.Objects;


/**
 * Represents a service operation.
 * @param <I> Operation execution input
 * @param <O> Operation execution output
 */
public abstract class ServiceOperation<I, O> {

  private final TokenVerifier tokenVerifier;
  private final OrganizationsDataAccess repository;

  protected ServiceOperation(TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
    this.tokenVerifier = tokenVerifier;
    this.repository = repository;
  }

  /**
   * Executes the request argument.
   * @param request the request to execute
   * @return response as a result of the request execution
   * @throws ServiceOperationException in case of an error during request execution
   */
  public O execute(I request) throws ServiceOperationException {
    Objects.requireNonNull(repository, "repository");
    try {
      validate(request);
      Token token = getToken(request);
      Profile profile = verifyToken(token);
      return process(request, new OperationServiceContext(profile, repository));
    } catch (Throwable throwable) {
      throw new ServiceOperationException(request.toString(), throwable);
    }
  }

  protected void validate(I request) {
    Objects.requireNonNull(request, "request is a required argument");
  }

  protected abstract Token getToken(I request);

  private Profile verifyToken(Token token) throws Throwable {
    Objects.requireNonNull(tokenVerifier, "tokenVerifier");
    Objects.requireNonNull(token, "token");
    requireNonNullOrEmpty(token.token(), "token");

    Profile profile = tokenVerifier.verify(token);
    
    if (profile == null) {
      throw new InvalidAuthenticationToken();
    }
    return profile;
  }

  protected abstract O process(I request, OperationServiceContext context) throws Throwable;

  protected Organization getOrganization(String id)
      throws EntityNotFoundException, OrganizationNotFound {
    Objects.requireNonNull(repository, "repository");
    Organization organization = repository.getOrganization(id);

    if (organization == null) {
      throw new OrganizationNotFound(id);
    }

    return organization;
  }

  protected GetOrganizationResponse getOrganizationResponse(Organization organization) {
    return new GetOrganizationResponse(OrganizationInfo.builder()
        .id(organization.id())
        .name(organization.name())
        .apiKeys(organization.apiKeys())
        .email(organization.email())
        .ownerId(organization.ownerId()));
  }

  protected static void requireNonNullOrEmpty(Object object, String message) {
    Objects.requireNonNull(object, message);

    if (object.toString().length() == 0) {
      throw new IllegalArgumentException(message);
    }
  }
}
