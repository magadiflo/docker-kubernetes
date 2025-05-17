package dev.magadiflo.user.app.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class CourseServiceClient {

    private final RestClient restClient;

    public void unassignUserFromAssociatedCourse(Long userId) {
        log.info("Llamando al servicio course-service para des-asignar de algún curso al usuario con id {}", userId);
        this.restClient
                .delete()
                .uri("/users/{userId}", userId)
                .retrieve()
                .toBodilessEntity();
        log.info("Fin de la llamada al servicio course-service para des-asignar de algún curso al usuario con id {}", userId);
    }

}
