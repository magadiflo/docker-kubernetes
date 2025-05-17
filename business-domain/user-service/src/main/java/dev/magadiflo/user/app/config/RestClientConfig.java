package dev.magadiflo.user.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${custom.course-service.base-url}")
    private String courseServiceBaseUrl;

    @Bean
    public RestClient courseServiceRestClient() {
        return RestClient.builder()
                .baseUrl(this.courseServiceBaseUrl)
                .build();
    }
}
