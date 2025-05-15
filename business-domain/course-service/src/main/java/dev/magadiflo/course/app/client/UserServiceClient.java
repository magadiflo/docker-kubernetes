package dev.magadiflo.course.app.client;

import dev.magadiflo.course.app.dto.UserRequest;
import dev.magadiflo.course.app.dto.UserResponse;
import dev.magadiflo.course.app.exception.CommunicationException;
import dev.magadiflo.course.app.exception.ErrorResponse;
import dev.magadiflo.course.app.exception.RemoteUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserResponse getUserFromUserService(Long userId) {
        log.info("Consultando al servicio user-service por el usuario con id {}", userId);
        UserResponse userResponse = this.restClient
                .get()
                .uri("/{userId}", userId)
                .exchange((clientRequest, clientResponse) -> {
                    HttpStatusCode statusCode = clientResponse.getStatusCode();
                    if (statusCode == HttpStatus.OK) {
                        return clientResponse.bodyTo(UserResponse.class);
                    }

                    if (statusCode == HttpStatus.NOT_FOUND) {
                        throw new RemoteUserNotFoundException(userId);
                    }

                    ErrorResponse errorResponse = clientResponse.bodyTo(ErrorResponse.class);
                    String message = Optional.ofNullable(errorResponse)
                            .map(ErrorResponse::error)
                            .orElseGet(() -> "Error desconocido al consultar el user-service");

                    log.info("Mensaje de error desde el user-service: {}", message);
                    throw new CommunicationException(message);
                });
        log.info("El servicio user-service encontró al usuario buscado: {}", userResponse);
        return userResponse;
    }

    public UserResponse createUserInUserService(UserRequest userRequest) {
        log.info("Registrando usuario en el user-service: {}", userRequest);
        UserResponse userResponse = this.restClient
                .post()
                .body(userRequest)
                .retrieve()
                .body(UserResponse.class);
        log.info("Usuario registrado con éxito en el user-service: {}", userResponse);
        return userResponse;
    }

    public List<UserResponse> getUsersByIdsFromUserService(List<Long> userIds) {
        log.info("Consultando al servicio user-service por los usuarios con id: {}", userIds);
        List<UserResponse> userResponseList = this.restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/by-ids")
                        .queryParam("userIds", userIds)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        log.info("El servicio user-service encontró a los usuarios: {}", userResponseList);
        return userResponseList;
    }
}
