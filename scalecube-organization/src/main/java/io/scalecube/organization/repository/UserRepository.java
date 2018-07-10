package io.scalecube.organization.repository;

import io.scalecube.account.api.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,String> {
}
