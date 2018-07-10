package io.scalecube.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCouchbaseRepositories
public class Config extends AbstractCouchbaseConfiguration {

    @Override
    protected List<String> getBootstrapHosts() {
        return Arrays.asList("localhost");
    }

    @Override
    protected String getBucketName() {
        return "organizations";
    }

    @Override
    protected String getBucketPassword() {
        return null;
    }
}