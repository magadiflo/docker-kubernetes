package dev.magadiflo.course.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${custom.user-service.base-url}")
    private String userServiceBaseUrl;

    @Bean
    public RestClient userServiceRestClient() {
        return RestClient.builder()
                .baseUrl(this.userServiceBaseUrl)
                .build();
    }
}
