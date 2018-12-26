package io.scalecube.organization.repository.couchbase.admin;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import io.scalecube.organization.repository.exception.CreatePrimaryIndexException;

final class CreatePrimaryIndexOperation extends Operation<N1qlQueryResult> {
  private static final String CREATE_PRIMARY_INDEX =
      "CREATE PRIMARY INDEX `%s-primary-idx` ON `%s`";

  @Override
  public N1qlQueryResult execute(AdminOperationContext context) {
    N1qlQuery index = N1qlQuery.simple(String.format(CREATE_PRIMARY_INDEX,
        context.name(), context.name()));
    N1qlQueryResult queryResult = context.cluster().openBucket(context.name()).query(index);

    if (!queryResult.finalSuccess()) {
      StringBuilder buffer = new StringBuilder();
      for (JsonObject error : queryResult.errors()) {
        buffer.append(error);
      }
      throw new CreatePrimaryIndexException(buffer.toString());
    }

    return queryResult;
  }
}
