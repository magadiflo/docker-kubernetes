package dev.magadiflo.user.app.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class CourseServiceClient {

    private static final String COURSE_URI = "/api/v1/courses";
    private final RestClient restClient;

    public CourseServiceClient(@Qualifier("courseRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void unassignUserFromAssociatedCourse(Long userId) {
        log.info("Llamando al servicio course-service para des-asignar de algún curso al usuario con id {}", userId);
        this.restClient
                .delete()
                .uri(COURSE_URI.concat("/users/{userId}"), userId)
                .retrieve()
                .toBodilessEntity();
        log.info("Fin de la llamada al servicio course-service para des-asignar de algún curso al usuario con id {}", userId);
    }

}
