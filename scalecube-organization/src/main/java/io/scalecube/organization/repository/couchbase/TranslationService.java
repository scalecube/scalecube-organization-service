package io.scalecube.organization.repository.couchbase;

import org.springframework.data.couchbase.core.mapping.CouchbaseDocument;
import org.springframework.data.couchbase.core.mapping.CouchbaseStorable;

public interface TranslationService {
    /**
     * Encodes a JSON String into the target format.
     *
     * @param source the source contents to encode.
     * @return the encoded document representation.
     */
    <T> String  encode(T source);

    /**
     * Decodes the target format into a {@link CouchbaseDocument}
     *
     * @param source the source formatted document.
     * @param target the target of the populated data.
     * @return a properly populated document to work with.
     */
    <T> T decode(String source, T target);
}
