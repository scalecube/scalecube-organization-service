package io.scalecube.organization.repository;


import io.scalecube.account.api.Organization;
import org.springframework.data.repository.CrudRepository;

public interface OrganizationsRepository extends CrudRepository<Organization,String>  {
}
