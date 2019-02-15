package io.scalecube.organization.repository.couchbase;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/** Default JSON implementation of <code>TranslationService</code>. */
class JacksonTranslationService implements TranslationService {

  private final ObjectMapper objectMapper = new ObjectMapper();

  JacksonTranslationService() {
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Override
  public <T> String encode(final T source) {
    Writer writer = new StringWriter();

    try {
      objectMapper.writeValue(writer, source);
      writer.close();
    } catch (IOException ex) {
      throw new RuntimeException("Could not encode JSON", ex);
    }

    return writer.toString();
  }

  @Override
  public <T> T decode(String source, Class<T> target) {
    try {
      return objectMapper.readValue(source, target);
    } catch (IOException ex) {
      throw new RuntimeException("Could not decode JSON", ex);
    }
  }
}
