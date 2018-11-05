package io.scalecube.organization;

import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.ServiceOperationException;
import io.scalecube.account.api.Token;
import io.scalecube.organization.opearation.OperationServiceContext;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.security.Profile;
import io.scalecube.tokens.TokenVerifier;
import java.util.Objects;

public abstract class ServiceOperation<Request, Response> {
 private final TokenVerifier tokenVerifier;
 private final OrganizationsDataAccess repository;

 protected ServiceOperation(TokenVerifier tokenVerifier, OrganizationsDataAccess repository) {
  this.tokenVerifier = tokenVerifier;
  this.repository = repository;
 }

 public Response execute(Request request) throws ServiceOperationException {
  try {
   Objects.requireNonNull(request);
   validate(request);
   Token token = getToken(request);
   Profile profile = verifyToken(token);
   return process(request, new OperationServiceContext(profile, repository));
  } catch (Throwable throwable) {
   throw new ServiceOperationException(request.toString(), throwable);
  }
 }

 protected abstract Response process(Request request, OperationServiceContext context);

 protected abstract void validate(Request request);

 protected Profile verifyToken(Token token) throws Throwable {
  Objects.requireNonNull(token.token());

  Profile owner = tokenVerifier.verify(token);
  if (owner == null) {
   throw new InvalidAuthenticationToken();
  }
  return owner;
 }


 protected abstract Token getToken(Request request);

 protected static void requireNonNullOrEmpty(Object  object, String message) {
  Objects.requireNonNull(object, message);

  if (object.toString().length() == 0) {
   throw new IllegalArgumentException(message);
  }
 }
}
