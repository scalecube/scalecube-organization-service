package io.scalecube.organization;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.InvalidAuthenticationToken;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.organization.repository.exception.AccessPermissionException;
import io.scalecube.organization.repository.exception.EntityNotFoundException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

public class OrganizationServiceTest extends Base {


  @Test
  public void createOrganizationWithNameAlreadyInUseShouldFail() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest(organisation.name(), token))
        , NameAlreadyInUseException.class);
    assertNotNull(duration);
  }
  
/**
 *   #MPA-7229 (#1)
 *   <p>Scenario: Successful creation of the Organization</p>
 *  <p>Given the user "A" have got a valid "token" issued by relevant authority</p>
 *  <p>When user "A" requested to create the organization with specified non-existent "name" and some "email"</p>
 *  <p>Then new organization should be created and stored in DB with relevant "organizationId" assigned for potential members</p>
 *  <p>And the user "A" should become the "owner" among the potential members of the relevant organization</p>
 */
  
  @Test
  public void createOrganization() {
    String id = createRandomOrganization();
    Duration duration = StepVerifier
        .create(service.getOrganization(new GetOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.id(), is(id)))
        .verifyComplete();

    deleteOrganization(id);

    assertNotNull(duration);
  }

  @Test
  public void createOrganizationWithEmptyNameShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("",
                token)), IllegalArgumentException.class);
    assertNotNull(duration);
  }
  
  /**
    #MPA-7229 (#1.3) - SHOULD WE REMOVE SUCH VALIDATION AND ENABLE TO ADD ANY CHARS?
    Scenario: Fail to create the Organization with the name which contain else symbols apart of allowed chars
    Given the user "A" have got a valid "token" issued by relevant authority
    When the user "A" requested to create the organization with specified "name" which contains "+" and some "email"
    Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent"
   */
  
  @ParameterizedTest
  @MethodSource("invalidOrgNames")
  public void createOrganizationWithIlligalNameShouldFailWithIllegalArgumentException(String invalidString) {
    StepVerifier.create(
        service.createOrganization(
            new CreateOrganizationRequest(invalidString,
                token)))
    .expectErrorMessage("Organization name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent.")
    .verify();
  }
  
  public static Stream<Arguments> invalidOrgNames() {
    return IntStream.concat(
        IntStream.concat(IntStream.range('!', '%'),
            IntStream.range('&', ')')
            ), IntStream.range('[', '_'))
        .mapToObj(i -> new StringBuilder("org").append((char)i).toString()
    ).map(Arguments::of);
  }
  
  
  /**
  #MPA-7229 (#1.3) - SHOULD WE REMOVE SUCH VALIDATION AND ENABLE TO ADD ANY CHARS?
  Scenario: Fail to create the Organization with the name which contain else symbols apart of allowed chars
  Given the user "A" have got a valid "token" issued by relevant authority
  When the user "A" requested to create the organization with specified "name" which contains "+" and some "email"
  Then user "A" should get an error message: "name can only contain characters in range A-Z, a-z, 0-9 as well as underscore, period, dash & percent"
 */

  @ParameterizedTest
  @MethodSource("validOrgNames")
  public void createOrganizationWithValidNameShouldNotFailWithIllegalArgumentException(String invalidString) {
    StepVerifier.create(
        service.createOrganization(
            new CreateOrganizationRequest(invalidString,
                token)))
    .assertNext(Objects::nonNull)
    .verifyComplete();
  }

  public static Stream<Arguments> validOrgNames() {
    return IntStream.of('_', '%', '.', '-', '%')
        .mapToObj(i -> new StringBuilder("org").append((char) i).toString())
        .map(Arguments::of);
  }
  
  @Test
  public void createOrganizationWithNullNameShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest(null,
                token)), NullPointerException.class);
    assertNotNull(duration);
  }



/**
 *   <p>#MPA-7229 (#1.1)</p>
 *   <p>Scenario: Fail to create the Organization if the token is invalid (expired)</p>
 *   <p>Given a user have got the invalid either expired "token"</p>
 *   <p>When this user requested to create the organization with some "name" and "email"</p>
 *   <p>Then this user should get an error message: "Token verification failed"</p>
 */
  @Test
  public void createOrganizationShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile).createOrganization(
                new CreateOrganizationRequest("myTestOrg5", token))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganizationNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.createOrganization(
                new CreateOrganizationRequest("myTestOrg5", null))
        , NullPointerException.class);
    assertNotNull(duration);
  }


  @Test
  public void createOrganizationNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.createOrganization(
                new CreateOrganizationRequest("myTestOrg5", new Token(null)))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void createOrganizationEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        service.createOrganization(
            new CreateOrganizationRequest("myTestOrg5", new Token("")))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }
  

  @Test
  public void deleteOrganizationInvalidTokenShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(
        createService(invalidProfile)
        .deleteOrganization(
        new DeleteOrganizationRequest(token, organisationId)), InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithIdNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, "orgIdNotExists")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithEmptyIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithNullIdShouldFailWithNullPointerException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(token, null)),
          NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(null, organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganizationWithEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        createService(testProfile)
            .deleteOrganization(
                new DeleteOrganizationRequest(new Token(""), organisationId)),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void deleteOrganization() {
    String id = createRandomOrganization();
    Duration duration = StepVerifier
        .create(service.deleteOrganization(new DeleteOrganizationRequest(token, id)))
        .expectSubscription()
        .assertNext((r) -> assertThat(r.deleted(), is(true)))
        .expectComplete()
        .verify();
    deleteOrganization(id);
    assertNotNull(duration);
  }
  
 @Test
  public void getOrganizationMembers() {
    addMemberToOrganization(organisationId, service, testProfile4);
    addMemberToOrganization(organisationId, service, testProfile5);

    Duration duration = StepVerifier.create(service.getOrganizationMembers(
        new GetOrganizationMembersRequest(organisationId, token)))
        .expectSubscription()
        .assertNext((r) -> {
          Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());
          assertThat(r.members().length, is(3));
          long membersCount = members.get()
              .filter((m) -> Objects.equals(m.role(), Role.Member.toString())).count();
          assertThat(membersCount, is(2L));
          List<String> ids = members.get().map(OrganizationMember::id).collect(Collectors.toList());
          assertThat(ids, hasItem(testProfile4.getUserId()));
          assertThat(ids, hasItem(testProfile5.getUserId()));
        }).verifyComplete();
    assertNotNull(duration);
  }








  @Test
  public void getOrganizationMembersEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest("", token))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullOrgIdShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(null, token))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            new Token("")))
        , IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            null))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId,
            new Token(null)))
        , NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile)
            .getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token))
        , InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void getOrganizationMembersShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(
            "orgNotExists", token))
        , EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMember() {
    addMemberToOrganization(organisationId, service, testProfile2);
    Duration duration = StepVerifier.create(
        service.getOrganizationMembers(new GetOrganizationMembersRequest(organisationId, token)))
        .expectSubscription()
        .assertNext((r) -> {
          Supplier<Stream<OrganizationMember>> members = () -> Arrays.stream(r.members());

          assertThat(members.get().map(OrganizationMember::id).collect(Collectors.toList()),
              hasItem(testProfile2.getUserId()));
          assertThat(members.get().filter(i -> Objects.equals(i.id(), testProfile2.getUserId()))
              .findFirst().orElseThrow(IllegalStateException::new), is(notNullValue()));

        })
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullRoleShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), null)),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyRoleShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), "")),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberInvalidRoleShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), "bla")),
      IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberHigherRoleShouldFailWithAccessPermissionException() {
    addMemberToOrganization(organisationId, service, testProfile2, Role.Admin);

    Duration duration = expectError(createService(testProfile2).inviteMember(
      new InviteOrganizationMemberRequest(token, organisationId,
        testProfile5.getUserId(), Role.Owner.toString())),
      AccessPermissionException.class);
    assertNotNull(duration);
  }



  @Test
  public void inviteMemberEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "",
          testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullOrgIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, null,
          testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyUserIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          "", Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullUserIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          null, Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberEmptyTokenShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(""), organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(null, organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberNullInnerTokenShouldFailWithNullPointerException() {
    Duration duration = expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(new Token(null), organisationId,
            testProfile5.getUserId(), Role.Member.toString())),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void inviteMemberOrgNotExistsShouldFailWithEntityNotFoundException() {
    expectError(service.inviteMember(
        new InviteOrganizationMemberRequest(token, "orgNotExists",
            testProfile5.getUserId(), Role.Member.toString())),
        EntityNotFoundException.class);
    assertThat(true, is(true));
  }

  @Test
  public void inviteMemberShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).inviteMember(
        new InviteOrganizationMemberRequest(token, organisationId,
          testProfile5.getUserId(), Role.Member.toString())),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganization() {
    addMemberToOrganization(organisationId, service, testProfile5);
    Duration duration = StepVerifier
        .create(createService(testProfile5).leaveOrganization(
            new LeaveOrganizationRequest(token, organisationId)))
        .expectSubscription()
        .assertNext(x -> StepVerifier
            .create(service.getOrganizationMembers(
                new GetOrganizationMembersRequest(organisationId, token)))
            .expectSubscription()
            .assertNext(r -> assertThat(Arrays.asList(r.members()),
                not(hasItem(
                    new OrganizationMember(testProfile5.getUserId(), Role.Member.toString())))))
            .verifyComplete())
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithEmptyOrgIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "")),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullOrgIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, null)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullTokenIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(null, organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithEmptyTokenIdShouldFailWithIllegalArgumentException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(new Token(""), organisationId)),
        IllegalArgumentException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationWithNullInnerTokenIdShouldFailWithNullPointerException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(new Token(null), organisationId)),
        NullPointerException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationOrgNotExistsShouldFailWithEntityNotFoundException() {
    Duration duration = expectError(service.leaveOrganization(
        new LeaveOrganizationRequest(token, "orgNotExists")),
        EntityNotFoundException.class);
    assertNotNull(duration);
  }

  @Test
  public void leaveOrganizationInvalidUserShouldFailWithInvalidAuthenticationToken() {
    Duration duration = expectError(createService(invalidProfile).leaveOrganization(
        new LeaveOrganizationRequest(token, "orgNotExists")),
        InvalidAuthenticationToken.class);
    assertNotNull(duration);
  }
}