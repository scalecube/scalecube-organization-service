package io.scalecube.organization.repository.couchbase;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface BucketCallback<T> {
    T doInBucket() throws TimeoutException, ExecutionException, InterruptedException;

}
