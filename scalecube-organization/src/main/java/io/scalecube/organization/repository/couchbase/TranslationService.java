package io.scalecube.organization.repository.couchbase;

/** Represents a translation service from object to string and vice versa. */
public interface TranslationService {

  /**
   * Encodes a JSON String into the target format.
   *
   * @param source the source contents to encode.
   * @return the encoded document representation.
   */
  <T> String encode(T source);

  /**
   * Decodes the string into the target
   *
   * @param source the source formatted document.
   * @param target the target of the populated data.
   * @return a properly populated object to work with.
   */
  <T> T decode(String source, Class<T> target);
}
