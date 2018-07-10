package io.scalecube.organization.repository.exception;

public class OrganizationNotFoundException extends Exception {
    public OrganizationNotFoundException(String id) {
        super(id);
    }
}
