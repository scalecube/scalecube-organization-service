package io.scalecube.account.api;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;
import reactor.core.publisher.Mono;

@Service("organizations")
public interface OrganizationService {

  @ServiceMethod
  Mono<CreateOrganizationResponse> createOrganization(CreateOrganizationRequest request);

  @ServiceMethod ("getMyOrganizations")
  Mono<GetMembershipResponse> getUserOrganizationsMembership(GetMembershipRequest request);

  @ServiceMethod
  Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request);

  @ServiceMethod
  Mono<DeleteOrganizationResponse> deleteOrganization(DeleteOrganizationRequest request);

  @ServiceMethod
  Mono<UpdateOrganizationResponse> updateOrganization(UpdateOrganizationRequest request);

  @ServiceMethod
  Mono<GetOrganizationMembersResponse> getOrganizationMembers(
      GetOrganizationMembersRequest request);

  @ServiceMethod
  Mono<InviteOrganizationMemberResponse> inviteMember(InviteOrganizationMemberRequest request);

  @ServiceMethod
  Mono<KickoutOrganizationMemberResponse> kickoutMember(KickoutOrganizationMemberRequest request);

  @ServiceMethod
  Mono<LeaveOrganizationResponse> leaveOrganization(LeaveOrganizationRequest request);

  @ServiceMethod ("addApiKey")
  Mono<GetOrganizationResponse> addOrganizationApiKey(AddOrganizationApiKeyRequest request);

  @ServiceMethod ("deleteApiKey")
  Mono<GetOrganizationResponse> deleteOrganizationApiKey(DeleteOrganizationApiKeyRequest request);

  @ServiceMethod ("updateMemberRole")
  Mono<UpdateOrganizationMemberRoleResponse> updateOrganizationMemberRole(
      UpdateOrganizationMemberRoleRequest request);

  @ServiceMethod
  Mono<GetPublicKeyResponse> getPublicKey(GetPublicKeyRequest request);
}
