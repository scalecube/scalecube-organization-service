package io.scalecube.organization.repository.couchbase;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.couchbase.core.mapping.CouchbaseDocument;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

public class JacksonTranslationService implements TranslationService {
    /**
     * JSON factory for Jackson.
     */
    private JsonFactory factory = new JsonFactory();

    @Override
    public <T> String encode(final T source) {
        Writer writer = new StringWriter();

        try {
            JsonGenerator generator = factory.createGenerator(writer);
            generator.writeStartObject();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            generator.writeFieldName("_class");
            objectMapper.writeValue(generator, source);
            //generator.setCodec(objectMapper);
            //generator.writeObject(source);
            generator.writeEndObject();
            generator.close();
            writer.close();
        }
        catch (IOException ex) {
            throw new RuntimeException("Could not encode JSON", ex);
        }

        return writer.toString();
    }

    @Override
    public <T> T decode(String source, Class<T> target) {
        try {
            JsonParser parser = factory.createParser(source);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(source, target);
        } catch (IOException ex) {
            throw new RuntimeException("Could not decode JSON", ex);
        }
        //return null;
    }
}
