package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public interface BucketCallback<T> {
    T doInBucket() throws TimeoutException, ExecutionException, InterruptedException;

}
