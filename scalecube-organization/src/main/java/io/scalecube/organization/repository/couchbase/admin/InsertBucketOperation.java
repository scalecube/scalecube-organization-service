package io.scalecube.organization.repository.couchbase.admin;

import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.DefaultBucketSettings;

final class InsertBucketOperation extends Operation<BucketSettings> {

  @Override
  public BucketSettings execute(AdminOperationContext context) {
    return context
        .cluster()
        .clusterManager()
        .insertBucket(
            new DefaultBucketSettings.Builder()
                .type(context.settings().bucketType())
                .name(context.name())
                .quota(context.settings().bucketQuota())
                .replicas(context.settings().bucketReplicas())
                .indexReplicas(context.settings().bucketIndexReplicas())
                .enableFlush(context.settings().bucketEnableFlush())
                .build());
  }
}
