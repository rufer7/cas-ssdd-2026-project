package ch.ssdd.eventhub.adapters.inbound.rest.config;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SanitizedStringConverter implements Converter<String, SanitizedString> {
    private static final PolicyFactory POLICY = Sanitizers.BLOCKS.and(Sanitizers.FORMATTING).and(Sanitizers.LINKS);

    @Override
    public SanitizedString convert(String source) {
        if (source == null) {
            return null;
        }
        return new SanitizedString(POLICY.sanitize(source));
    }
}
