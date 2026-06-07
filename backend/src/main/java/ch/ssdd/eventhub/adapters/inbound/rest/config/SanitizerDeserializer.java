package ch.ssdd.eventhub.adapters.inbound.rest.config;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class SanitizerDeserializer extends ValueDeserializer<String> {
    private static final PolicyFactory POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        return POLICY.sanitize(value);
    }
}
