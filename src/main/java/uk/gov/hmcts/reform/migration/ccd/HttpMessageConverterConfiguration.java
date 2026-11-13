package uk.gov.hmcts.reform.migration.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpMessageConverterConfiguration {

    private final ObjectMapper objectMapper;

    public HttpMessageConverterConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void customizeObjectMapper() {
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }
}
