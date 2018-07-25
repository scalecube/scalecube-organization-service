package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.core.*;
import com.couchbase.client.core.config.ConfigurationException;
import com.couchbase.client.core.endpoint.SSLException;
import com.couchbase.client.core.endpoint.kv.AuthenticationException;
import com.couchbase.client.core.env.EnvironmentException;
import com.couchbase.client.core.state.NotConnectedException;
import com.couchbase.client.java.error.*;
import io.scalecube.organization.repository.exception.*;

import java.util.concurrent.TimeoutException;

class CouchbaseExceptionTranslator {
    DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        if (ex instanceof InvalidPasswordException
                || ex instanceof NotConnectedException
                || ex instanceof ConfigurationException
                || ex instanceof EnvironmentException
                || ex instanceof InvalidPasswordException
                || ex instanceof SSLException
                || ex instanceof ServiceNotAvailableException
                || ex instanceof BucketClosedException
                || ex instanceof BucketDoesNotExistException
                || ex instanceof AuthenticationException) {
            return new DataAccessResourceFailureException(ex.getMessage(), ex);
        }

        if (ex instanceof DocumentAlreadyExistsException) {
            return new DuplicateKeyException(ex.getMessage(), ex);
        }

        if (ex instanceof DocumentDoesNotExistException) {
            return new DataRetrievalFailureException(ex.getMessage(), ex);
        }

        if (ex instanceof CASMismatchException
                || ex instanceof DocumentConcurrentlyModifiedException
                || ex instanceof ReplicaNotConfiguredException
                || ex instanceof DurabilityException) {
            return new DataIntegrityViolationException(ex.getMessage(), ex);
        }

        if (ex instanceof RequestCancelledException
                || ex instanceof BackpressureException) {
            return new OperationCancellationException(ex.getMessage(), ex);
        }

        if (ex instanceof ViewDoesNotExistException
                || ex instanceof RequestTooBigException
                || ex instanceof DesignDocumentException) {
            return new InvalidDataAccessResourceUsageException(ex.getMessage(), ex);
        }

        if (ex instanceof TemporaryLockFailureException
                || ex instanceof TemporaryFailureException) {
            return new TransientDataAccessResourceException(ex.getMessage(), ex);
        }

        if ((ex != null && ex.getCause() instanceof TimeoutException)) {
            return new QueryTimeoutException(ex.getMessage(), ex);
        }

        if (ex instanceof TranscodingException) {
            //note: the more specific CouchbaseQueryExecutionException should be thrown by the template
            //when dealing with TranscodingException in the query/n1ql methods.
            return new DataRetrievalFailureException(ex.getMessage(), ex);
        }

        // Unable to translate exception, therefore just throw the original!
        throw ex;
    }
}
