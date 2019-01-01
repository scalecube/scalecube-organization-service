package io.scalecube.organization;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class DeleteOrganizationTest extends Base {

  @Test
  public void deleteOrganizationApiKeyUserNotOwnerShouldFailWithAccessPermissionException() {
    Duration duration = expectError(createService(testProfile2).deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "api_key")),
        AccessPermissionException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, "", "api_key")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, "bla", "api_key")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyNullOrgShouldFailWithNullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, null, "api_key")),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyNullApiKeyNameShouldFailWithNullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyNameShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyInvalidUserShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(token, organisationId, "api_key")),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(null, organisationId, "api_key")),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(new Token(null), organisationId,
            "api_key")),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationApiKeyEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.deleteOrganizationApiKey(
        new DeleteOrganizationApiKeyRequest(new Token(""), organisationId,
            "api_key")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }}
