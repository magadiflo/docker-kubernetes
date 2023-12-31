# Microservicio dk-ms-users

---

## Dependencias iniciales

Inicialmente, nuestro microservicio `dk-ms-users` tendrá las siguientes dependencias:

````xml
<!--Spring Boot 3.1.4-->
<!--Spring Cloud 2022.0.4-->
<!--Java 17-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

**NOTA**
> Si abrimos el `pom.xml` del microservicio `dk-ms-users` con un editor, no veremos todas las dependencias como se
> muestra en la parte superior, sino más bien las dependencias que solo manejará ese microservicio, es decir que
> solo serán propios de ese microservicio `(connector de mysql)`; las otras dependencias son comunes
> a los otros proyectos, y para evitar estar agregando una y otra vez, lo que hacemos es organizarlos en los módulos
> padres. Es decir, al final nuestro microservicio `dk-ms-users` sí las usa, ya que lo está heredando y no solo él lo
> usará sino otros microservicios que lo requiereran.

## Configurando el contexto de persistencia JPA/Hibernate

Configuramos el `application.yml` dándole un nombre a este microservicio y estableciéndo un puerto:

````yaml
server:
  port: 8001

spring:
  application:
    name: dk-ms-users
````

Creamos el modelo de entidad `User`:

````java

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;

    /* Getters, Setters an toString() methods */
}
````

## Implementando el componente Repository de acceso a datos

Creamos la interfaz de repositorio para nuestra entidad `User`:

````java
public interface IUserRepository extends CrudRepository<User, Long> {

}
````

## Implementando el componente Service

Crearemos primero la interfaz `IUserService` y luego su implementación:

````java
public interface IUserService {
    List<User> findAllUsers();

    Optional<User> findUserById(Long id);

    User saveUser(User user);

    Optional<User> updateUser(Long id, User userWithChangeData);

    Optional<Boolean> deleteUserById(Long id);
}
````

Ahora, crearemos la clase que implementará la interfaz `IUserService`:

````java

@Service
public class UserServiceImpl implements IUserService {
    private final IUserRepository userRepository;

    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return (List<User>) this.userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> updateUser(Long id, User userWithChangeData) {
        return this.userRepository.findById(id)
                .map(userDB -> {
                    userDB.setName(userWithChangeData.getName());
                    userDB.setEmail(userWithChangeData.getEmail());
                    userDB.setPassword(userWithChangeData.getPassword());
                    return userDB;
                })
                .map(this.userRepository::save);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteUserById(Long id) {
        return this.userRepository.findById(id)
                .map(userDB -> {
                    this.userRepository.deleteById(userDB.getId());
                    return true;
                });
    }
}
````

## Implementando el controlador RestController y métodos handler GET

En esta sección crearemos el RestController para `User` donde implementaremos los dos métodos handler del tipo `GET`:

````java

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(this.userService.findAllUsers());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return this.userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
````

## RestController y métodos handler POST y PUT

Creamos los métodos handler `POST` y `PUT` para crear y actualizar un user respectivamente:

````java

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    /* other code */

    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        User userDB = this.userService.saveUser(user);
        URI location = ServletUriComponentsBuilder //Extrae información del HttpServletRequest
                .fromCurrentRequest() // Obtiene la URI actual del servlet
                .path("/{id}") // Añadimos el segmento de la URI correspondiente al Id del User
                .buildAndExpand(userDB.getId()) // Reemplazamos el {id} con el Id del usuario recién creado
                .toUri(); // Convertimos lo realizado en una URI
        // Ejm. uriLocation => http://localhost:8001/api/v1/users/5, donde 5 es el id que generó la BD.
        return ResponseEntity.created(location).body(userDB);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return this.userService.updateUser(id, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
````

En el código anterior observemos el `ServletUriComponentsBuilder` **(extrae información del** `HttpServletRequest`)
al que le concatenamos varios métodos para finalmente construir una uri dinámica cuyo resultado tendrá esta forma:

````
http://localhost:8001/api/v1/users/5 <-- donde el 5, es un valor representativo, aquí irá el id generado en la bd
````

## RestController y métodos handler DELETE

Implementamos nuestro método handler `DELETE` para eliminar un user:

````java

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    /* other code*/

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return this.userService.deleteUserById(id)
                .map(wasDeleted -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
````

## Configurando application.yml conexión MySQL

A las configuraciones iniciales que teníamos en el `application.yml` le agregamos ahora las configuraciones de la
conexión a nuestra base de datos:

````yaml
# Other configuration

spring:
  # Other configuration

  ## DataSource MySQL
  datasource:
    url: jdbc:mysql://localhost:3306/db_dk_ms_users
    username: root
    password: magadiflo
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    generate-ddl: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: debug
````

**DONDE**

- `spring.jpa.database-platform`, permite configurar el dialecto de la BD a usar en JPA. Cada motor de BD tiene su
  propio dialecto, es decir palabras reservadas que son propias de la BD a usar.
- `spring.jpa.generate-ddl`, esta configuración es específica de Spring Boot y controla si se debe generar
  automáticamente el **DDL (Data Definition Language)** para la base de datos a partir de las entidades JPA de tu
  aplicación. Cuando se establece en true, Spring Boot intentará generar el DDL necesario para crear o actualizar la
  base de datos de acuerdo con tus entidades.
- `logging.level.org.hibernate.SQL`, nos permite **ver las consultas SQL** generadas por Hibernate en la consola de
  registro.

## Probando la conexión a MySQL

Luego de ejecutar la aplicación veremos en el log de IntelliJ IDEA las instrucciones ejecutadas para la creación de
nuestra tabla `users`. Ahora, si abrimos la base de datos veremos dicha tabla:

![users-table](./assets/users-table.png)

## Probando API Restful de Users

Luego de haber construido el microservicio dk-ms-users llega el momento de probar todos sus endpoints.

- Crear un user

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"martin\", \"email\":\"martin@gmail.com\", \"password\": \"12345\"}" http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 201
< Location: http://localhost:8001/api/v1/users/1
< Content-Type: application/json
<
{
  "id": 1,
  "name": "martin",
  "email": "martin@gmail.com",
  "password": "12345"
}
````

- Listar users

````bash
$ curl -v http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 1,
    "name": "martin",
    "email": "martin@gmail.com",
    "password": "12345"
  }
]
````

- Obtener un user

````bash
$ curl -v http://localhost:8001/api/v1/users/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "martin",
  "email": "martin@gmail.com",
  "password": "12345"
}
````

- Actualizar un user

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"name\": \"Martin\", \"email\":\"martin.system@gmail.com\", \"password\": \"abcde\"}" http://localhost:8001/api/v1/users/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Martin",
  "email": "martin.system@gmail.com",
  "password": "abcde"
}
````

- Eliminar un user

````bash
$  curl -v -X DELETE http://localhost:8001/api/v1/users/1 | jq

>
< HTTP/1.1 204
````

## Validando los datos del JSON

Validaremos los campos de nuestra entidad `User` utilizando las anotaciones proporcionadas por la dependencia
`spring-boot-starter-validation`. Nuestra entidad `User` quedaría de esta manera:

````java

@Entity
@Table(name = "users")
public class User {
    /* other property */
    @NotBlank
    private String name;
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
    @NotBlank
    private String password;
    /* other cod */
}
````

Ahora, en nuestro controlador `UserController` agregaremos la anotación `@Valid` antes del parámetro `user`, que es el
parámetro que queremos validar:

````java

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    /* other code*/
    @PostMapping
    public ResponseEntity<User> saveUser(@Valid @RequestBody User user) {
        /* code */
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        /* code */
    }
    /* other code*/
}
````

En el controlador anterior vemos la parte de la validación mediante la anotación `@Valid` dentro de los parámetros del
método. **Esta anotación se encargará de validar el objeto que llega, validando los argumentos**. La validación es del
estándar [JSR380](https://beanvalidation.org/2.0-jsr380/). Cuando la validación falla se lanzará
un `MethodArgumentNotValidException` de Spring.
[Fuente: Refactorizando](https://refactorizando.com/validadores-spring-boot/)

Ahora, necesitamos capturar de alguna manera los errores cuando se produzca la excepción
`MethodArgumentNotValidException`, para eso nos apoyaremos del `@RestControllerAdvice` de Spring que no solo se
encargará de manejar la excepción anterior, sino todas aquellas que le definamos.

**NOTA**
> El tutor del curso no usa la anotación `@RestControllerAdvice` sino más bien maneja la excepción dentro del mismo
> método del controlador usando no solo el `@Valid`, sino también la interfaz `BindingResult`, algo así:
>
> `...saveUser(@Valid @RequestBody User user, BindingResult result){ if(result.hasErrors()){}}`
>
> En mi caso uso la anotación `@RestControllerAdvice` para tener una clase dedicada al manejo de errores.

Antes de construir la clase con la anotación `@RestControllerAdvice` necesitamos crear un record que tendrá los datos
que siempre mandaremos al cliente cuando ocurra una excepción, de esta manera uniformizamos los mensajes de error.

````java

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExceptionHttpResponse(LocalDateTime timestamp, int statusCode, HttpStatus httpStatus, String message,
                                    Map<String, String> errors) {
}
````

Observar que en el código anterior estamos usando la anotación `@JsonInclude(JsonInclude.Include.NON_NULL)`, esta
anotación nos permite **ignorar los campos nulos al serializar** la clase java. Esto significa que si un atributo
de nuestro record `ExceptionHttpResponse` tiene un valor nulo, no se incluirá en la respuesta JSON.

Para nuestro caso, veremos en el código siguiente que el campo `errors` para otro tipo de excepciones que no sea el de
validar los campos, será nulo, por lo que con esta anotación estaremos ignorando dicho campo.

Ahora sí, creamos nuestra clase global encargada de manejar las excepciones producidas en nuestra aplicación:

````java

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionHttpResponse> handleValidationErrors(MethodArgumentNotValidException exception) {
        LOG.error("MethodArgumentNotValidException: Error al validar los campos [{}]", exception.getStatusCode());

        Map<String, String> fieldErrors = exception.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, this::messageFieldError));

        return this.httpResponse(HttpStatus.BAD_REQUEST, "Error al validar los campos", fieldErrors);
    }

    private ResponseEntity<ExceptionHttpResponse> exceptionHttpResponse(HttpStatus httpStatus, String message) {
        return this.httpResponse(httpStatus, message, null);
    }

    private ResponseEntity<ExceptionHttpResponse> httpResponse(HttpStatus httpStatus, String message, Map<String, String> errors) {
        ExceptionHttpResponse exceptionBody = new ExceptionHttpResponse(LocalDateTime.now(),
                httpStatus.value(), httpStatus, message, errors);
        return ResponseEntity.status(httpStatus).body(exceptionBody);
    }

    private String messageFieldError(FieldError fieldError) {
        return String.format("Ocurrió un error, el campo %s %s", fieldError.getField(), fieldError.getDefaultMessage());
    }
}
````

## Probando validaciones

Registramos un usuario con datos no válidos:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \" \", \"email\":\"test\", \"password\": \"12345\"}" http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 400
< Content-Type: application/json
<
{
  "timestamp": "2023-10-20T16:59:35.659272",
  "statusCode": 400,
  "httpStatus": "BAD_REQUEST",
  "message": "Error al validar los campos",
  "errors": {
    "name": "Ocurrió un error, el campo name must not be blank",
    "email": "Ocurrió un error, el campo email must be a well-formed email address"
  }
}
````

## Validando si existe el email del usuario en la Base de Datos

Para validar si ya existe un email de un usuario registrado en la base de datos necesitamos crear una **excepción
personalizada** que lanzaremos cuando verifiquemos que se intenta registrar un email ya existente:

````java
public class EmailExistException extends RuntimeException {
    public EmailExistException(String message) {
        super(message);
    }
}
````

Ahora, como ya hemos creado nuestro manejador de excepciones globales, crearemos el método que capturará la
excepción `EmailExistException`:

````java

@RestControllerAdvice
public class GlobalExceptionHandler {
    /* other code*/
    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<ExceptionHttpResponse> emailExistException(EmailExistException exception) {
        return this.exceptionHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
    /* other code*/
}
````

Necesitamos un método en el `IUserRepository` que nos verifique si el email que le pasamos por parámetro existe en la
base de datos:

````java
public interface IUserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);
}
````

Finalmente, en nuestra implementación del servicio `UserServiceImpl` validamos la existencia del email en los métodos
`saveUser()` y `updateUser()`:

````java

@Service
public class UserServiceImpl implements IUserService {
    /* other code */

    @Override
    @Transactional
    public User saveUser(User user) {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new EmailExistException("Ya existe un usuario con ese email");
        }
        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> updateUser(Long id, User userWithChangeData) {
        return this.userRepository.findById(id)
                .map(userDB -> {

                    if (!userWithChangeData.getEmail().equalsIgnoreCase(userDB.getEmail()) &&
                        this.userRepository.existsByEmail(userWithChangeData.getEmail())) {

                        throw new EmailExistException("Ya existe un usuario con ese email");
                    }

                    userDB.setName(userWithChangeData.getName());
                    userDB.setEmail(userWithChangeData.getEmail());
                    userDB.setPassword(userWithChangeData.getPassword());
                    return userDB;
                })
                .map(this.userRepository::save);
    }

    /* other code */
}
````

## Probando validación de email

Ejecutamos la aplicación y registramos un email existente a un nuevo usuario:

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"name\": \"Martin\", \"email\":\"nophy@gmail.com\", \"password\": \"abcde\"}" http://localhost:8001/api/v1/users/2 | jq

>
< HTTP/1.1 400
< Content-Type: application/json
<
{
  "timestamp": "2023-10-20T19:36:34.3487394",
  "statusCode": 400,
  "httpStatus": "BAD_REQUEST",
  "message": "Ya existe un usuario con ese email"
}
````

Lo mismo ocurre si tratamos de actualizar un email por otro ya existente en la base de datos:

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"name\": \"Martin\", \"email\":\"nophy@gmail.com\", \"password\": \"abcde\"}" http://localhost:8001/api/v1/users/2 | jq

>
< HTTP/1.1 400
< Content-Type: application/json
<
{
  "timestamp": "2023-10-20T19:38:08.4345786",
  "statusCode": 400,
  "httpStatus": "BAD_REQUEST",
  "message": "Ya existe un usuario con ese email"
}
````

## dk-ms-users obtener alumnos por ids

Crearemos un endpoint en nuestro microservicio `dk-ms-users` que nos retornará un grupo de usuarios a partir de los ids
proporcionados.

````java
public interface IUserService {
    /* other methods */
    List<User> findAllById(Iterable<Long> ids);
    /* other methods */
}
````

Implementamos el método anterior en la clase de servicio:

````java

@Service
public class UserServiceImpl implements IUserService {
    /* other methods */
    @Override
    @Transactional(readOnly = true)
    public List<User> findAllById(Iterable<Long> ids) {
        return (List<User>) this.userRepository.findAllById(ids);
    }
    /* other methods */
}
````

Finalmente, en nuestro controlador definimos el endpoint. Notar que el endpoint es del tipo `GET` y está esperando
recibir un `RequestParam` con atributo `userIds`:

````java

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {
    /* other methods */
    @GetMapping(path = "/group")
    public ResponseEntity<List<User>> findAllById(@RequestParam List<Long> userIds) {
        return ResponseEntity.ok(this.userService.findAllById(userIds));
    }
    /* other methods */
}
````

Ejecutamos la aplicación y probamos el endpoint. Supongamos que queremos obtener los usuarios que tienen el id 2 y 3:

````bash
$ curl -v http://localhost:8001/api/v1/users/group?userIds=2,3 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 2,
    "name": "Martin",
    "email": "martin@gmail.com",
    "password": "12345"
  },
  {
    "id": 3,
    "name": "nophy",
    "email": "nophy@gmail.com",
    "password": "12345"
  }
]
````

## Agregando cliente http para eliminar alumno de dk-ms-courses

Como vamos a trabajar con el cliente `HTTP Feign Client` necesitamos agregar la anotación en la clase principal:

````java

@EnableFeignClients
@SpringBootApplication
public class DkMsUsersApplication {

    public static void main(String[] args) {
        SpringApplication.run(DkMsUsersApplication.class, args);
    }

}
````

Ahora creamos la interfaz que consumirá la API de nuestro microservicio `dk-ms-courses`:

````java

@FeignClient(name = "dk-ms-courses", url = "localhost:8002", path = "/api/v1/courses")
public interface ICourseFeignClient {
    @DeleteMapping(path = "/unassigning-user-by-userid/{userId}")
    void unassigningUserByUserId(@PathVariable Long userId);
}
````

Como vamos a trabajar con `Http Feign Client` necesitamos manejar los errores que pueda producir. Para eso, crearemos
un manejador de excepción del tipo `FeignException` en nuestro controlador `@RestControllerAdvice`:

````java

@RestControllerAdvice
public class GlobalExceptionHandler {
    /* other methods */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ExceptionHttpResponse> feignException(FeignException exception) {
        String message = "Error en la comunicación entre microservicios: " + exception.getMessage();
        return this.exceptionHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
    /* other methods */
}
````

Finalmente, en la clase de implementación del servicio mandamos a al endpoint implementado:

````java

@Service
public class UserServiceImpl implements IUserService {
    /* other code */
    private final ICourseFeignClient courseFeignClient;
    /* other methods */

    @Override
    @Transactional
    public Optional<Boolean> deleteUserById(Long id) {
        return this.userRepository.findById(id)
                .map(userDB -> {
                    this.userRepository.deleteById(userDB.getId());
                    this.courseFeignClient.unassigningUserByUserId(id);
                    return true;
                });
    }
}
````

Verificamos los usuarios asignados a los cursos:

````bash
$ curl -v http://localhost:8002/api/v1/courses | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 1,
    "name": "Kubernetes",
    "courseUsers": [
      {
        "id": 1,
        "userId": 2
      },
      {
        "id": 2,
        "userId": 5
      }
    ],
    "users": []
  },
  {...}
]
````

En el microservicio `dk-ms-users` eliminamos el usuario con ` id = 5`. Ahora, como ese usuario está asignado al curso
de `Kubernetes`, por debajo el microservicio `dk-ms-users` debe ir al microservicio `dk-ms-courses` y eliminar dicho
usuario:

````bash
$ curl -v -X DELETE http://localhost:8001/api/v1/users/5 | jq

>
< HTTP/1.1 204
< Date: Tue, 24 Oct 2023 18:00:22 GMT
<
````

Si volvemos a listar los cursos:

````bash
$ curl -v http://localhost:8002/api/v1/courses | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 1,
    "name": "Kubernetes",
    "courseUsers": [
      {
        "id": 1,
        "userId": 2
      }
    ],
    "users": []
  },
  {...}
````

---

---

# Sección 6: Docker: Introducción

---

## Creando archivo Dockerfile usando imagen OpenJDK

En la raíz del microservicio `dk-ms-users` creamos el archivo `Dockerfile` y agregamos las siguientes instrucciones:

````dockerfile
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY ./target/*.jar ./app.jar
EXPOSE 8001
CMD ["java", "-jar", "app.jar"]
````

**DONDE**

- `FROM openjdk:17-jdk-alpine`, usaremos como imagen base para nuestra versión 17 de java la imagen de `openjdk` cuyo
  tag es `17-jdk-alpine`.
- `WORKDIR /app`, crearemos un directorio de trabajo donde colocaremos nuestra aplicación y desde donde trabajaremos, no
  es obligatorio, pero teniendo un directorio de trabajo nos aseguramos de saber exactamente dónde está nuestra
  aplicación y dónde se está ejecutando para que cuando se acceda al contenedor sepamos exactamente dónde buscar.
- `COPY ./target/*.jar ./app.jar`, copiamos el archivo `*.jar` que está en la ruta de nuestra máquina
  local `(./target/*.jar)` hacia la ruta en la **imagen/contenedor** `(./app.jar)`.
    - Podría haber colocado en vez de `*.jar` de nuestra ruta de la máquina local, el nombre completo que generamos al
      compilar el proyecto: `dk-ms-users-0.0.1-SNAPSHOT.jar`, pero como siempre habrá un único archivo que termine con
      extensión `.jar` es que coloco el comodín `*.jar`, de esa manera evito escribir todo el nombre.
    - En el directorio de destino, el archivo `*.jar` que estamos copiando lo vamos a renombrar a `app.jar`.
    - La copia se realizará hacia el `WORKDIR /app` que creamos al inicio, es decir, del `./app.jar` el `. == /app`, lo
      que significa que la copia final quedaría `/app/app.jar`.
- `EXPOSE 8001`, es a modo de documentación `(opcional)`. La instrucción EXPOSE informa a Docker que el contenedor
  escucha en los puertos de red especificados en tiempo de
  ejecución. `La instrucción EXPOSE no publica realmente el puerto`. **Funciona como un tipo de documentación entre la
  persona que construye la imagen y la persona que ejecuta el contenedor**, sobre qué puertos están destinados a ser
  publicados.
- `CMD ["java", "-jar", "app.jar"]`, se ejecuta por defecto en la raíz del `WORKDIR`, o sea en nuestro caso en
  el `/app`. **Es una instrucción para cuando se construyan los contenedores, no para las imágenes.** El propósito
  principal de un CMD es proporcionar valores por defecto para un contenedor en ejecución.

## Construyendo nuestra primera imagen con Dockerfile y corriendo contenedor

Antes de ejecutar nuestro contenedor realizaremos un cambio en el código fuente de nuestra aplicación de Spring Boot,
ya que, **si corremos un contenedor, nuestra aplicación de Spring Boot se va a levantar dentro de él y al momento de
iniciarse tratará de conectarse a la base de datos de MySQL**, pues en el archivo `application.yml` está especificado
la url de conexión como `localhost`, y como nuestra aplicación de Spring Boot está dentro del contenedor, ahora ese
`localhost` sería la parte interna del contenedor. Entonces, lo que se quiere es que nuestra aplicación de Spring
Boot que está dentro del contenedor se comunique con MySQL que está en el lado externo, es decir, en nuestra pc local.

Entonces, para solucionar el problema anterior, utilizaremos un **nombre de dominio especial de docker:**
`host.docker.internal`, lo que hace es que la aplicación que está dentro del contenedor se pueda comunicar con una
aplicación que está fuera, en nuestro caso con MySQL que está en nuestra **máquina host local**.

````yaml
# Other properties
spring:
  # Other properties
  datasource:
    url: jdbc:mysql://host.docker.internal:3306/db_dk_ms_users
# Other properties
````

Listo, ahora sí, **como hicimos un cambio en el código fuente es necesario volver a generar el .jar, y también volver a
generar la imagen.**

Ubicados en la raíz del microservicio `dk-ms-users`, ejecutamos:

````bash
$ mvnw clean package -DskipTests
````

**Importante**
> Como cambiamos la dirección de la base de datos a `host.docker.internal`, al momento de generar el .jar va a fallar
> porque no reconocerá esa dirección. Recordemos que esa dirección solo funciona dentro del contenedor y nosotros
> estamos generando el .jar en nuestra pc local.
>
> Para evitar ese fallo, o para ser más exactos, para saltarnos el test, agregaremos la bandera `-DskipTests`.

Una vez generado el `jar`, ejecutamos el comando para crear nuestra imagen de docker desde la misma raíz del
microservicio `dk-ms-users`:

````bash
$ docker build .
````

**DONDE**

- `.` le indica a Docker que busque el `Dockerfile` y los recursos relacionados en el `directorio actual` en el que nos
  encontramos, en nuestro caso en la raíz de nuestro microservicio `dk-ms-users` donde actualmente estamos posicionados
  mediante la línea de comandos.

Terminado la construcción de la imagen, podemos listarlo:

````bash
$ docker image ls
REPOSITORY   TAG       IMAGE ID       CREATED         SIZE
<none>       <none>    675a27a7e90b   9 minutes ago   387MB
````

Ahora, a partir de la imagen anterior creamos nuestro contenedor:

````bash
$ docker container run -p 8001:8001 675a27a7e90b
````

**DONDE**

- `-p 8001:8001`, especificamos el puerto `externo:interno`. El puerto externo, es desde donde se puede acceder
  externamente al contenedor, mientras que, el puerto interno es el que usa nuestra aplicación al interior del
  contenedor. En nuestro caso, definimos el mismo valor para ambos puertos.

Listamos los contenedores para ver el que acabamos de levantar:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE          COMMAND               CREATED          STATUS          PORTS                    NAMES
ca7eb1d41446   675a27a7e90b   "java -jar app.jar"   33 minutes ago   Up 33 minutes   0.0.0.0:8001->8001/tcp   recursing_murdock
````

Finalmente, si accedemos a algún endpoint de la aplicación dockerizada veremos que funciona correctamente:

````bash
$ curl -v http://localhost:8001/api/v1/users/3 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 3,
  "name": "nophy",
  "email": "nophy@gmail.com",
  "password": "12345"
}
````

## Comunicación entre aplicación en contenedor y aplicación local (pc host)

En esta sección estableceremos comunicación entre nuestro `dk-ms-users` que ya está dockerizado con
nuestro `dk-ms-courses` que aún no está dockerizado.

Entonces, como nuestro microservicio `dk-ms-courses` se va a comunicar desde el interior del contenedor hacia afuera,
necesitamos modificar la propiedad `url` del `@FeignClient` para que también use el `host.docker.internal` en reemplazo
de `localhost`:

````java

@FeignClient(name = "dk-ms-courses", url = "host.docker.internal:8002", path = "/api/v1/courses")
public interface ICourseFeignClient {
    /* code */
}
````

Ahora, en nuestro microservicio `dk-ms-courses` también tenemos un `@FeignClient` cuya url apunta a un `localhost:8001`,
en este caso **no habría que modificar nada**, ya que el puerto externo que tendrá el contenedro será de `8001` y para
poder acceder desde nuestra pc local al contenedor usamos el `localhost`:

````java

@FeignClient(name = "dk-ms-users", url = "localhost:8001", path = "/api/v1/users")
public interface IUserFeignClient {
    /* code */
}
````

Listo, ahora volvemos a realizar todo el proceso que vimos en la sección anterior ya que hemos modificado el código
fuente del `dk-ms-users`:

````bash
$ mvnw clean package -DskipTests

$ docker build -t dk-ms-users .

$ docker image ls
REPOSITORY    TAG       IMAGE ID       CREATED             SIZE
dk-ms-users   latest    2aae057fd9d4   4 minutes ago       387MB

$ docker container run -p 8001:8001 dk-ms-users

$ docker container ls -a
CONTAINER ID   IMAGE          COMMAND               CREATED              STATUS                        PORTS                    NAMES
9e3dffc8ad30   dk-ms-users    "java -jar app.jar"   About a minute ago   Up About a minute             0.0.0.0:8001->8001/tcp   charming_dubinsky
````

**DONDE**

- `-t dk-ms-users`, con este tag le damos un nombre a la imagen que vamos a crear.

Finalmente, una vez que ya tenemos nuestro contenedor del microservicio `dk-ms-users` corriendo, levantamos nuestro
microservicio `dk-ms-courses` que esté en nuestra pc local, lo podemos hacer usando el IDE IntelliJ IDEA. Ahora que
ambos están levantados ejecutamos el siguiente comando para ver si hay comunicación entre ambos:

````bash
$ curl -v http://localhost:8002/api/v1/courses/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Kubernetes",
  "courseUsers": [
    {
      "id": 1,
      "userId": 2
    }
  ],
  "users": [
    {
      "id": 2,
      "name": "Martin",
      "email": "martin@gmail.com",
      "password": "12345"
    }
  ]
}
````

## Optimizando Dockerfile

Hasta ahora lo que estamos haciendo para generar la imagen de nuestro proyecto de spring boot es:

1. Generar manualmente el archivo `.jar`.
2. Utilizar ese empaquetado para generar la imagen.

**Pero, ¡podemos automatizar ese proceso!**, para eso tenemos que realizar modificaciones en nuestro `Dockerfile`.
Ahora, como estamos trabajando con un proyecto `maven multiple-module`, tenemos los `pom.xml` organizados en módulos y
como ahora queremos usar un comando que se encargue de **generar el .jar** y luego **crear la imagen** necesitamos
definir una ubicación desde dónde ejecutaremos el comando.

La ubicación desde dónde ejecutaremos el comando será la raíz de todo nuestro proyecto **maven multiple-module**, es
decir `/docker-kubernetes`, ya que desde allí podemos llegar a todos los subdirectorios `(sub-módulos)`. Listo, teniendo
en cuenta ese detalle, reescribimos el `Dockerfile`:

````dockerfile
FROM openjdk:17-jdk-alpine
WORKDIR /app/business-domain/dk-ms-users

COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/dk-ms-users ./

RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8001
CMD ["java", "-jar", "./target/dk-ms-users-0.0.1-SNAPSHOT.jar"]
````

Como observamos, nuestro `Dockerfile` ha variado en algunos casos a cómo lo teníamos inicialmente. A continuación
explicaré los cambios realizados:

- `WORKDIR /app/business-domain/dk-ms-users`, como queremos construir la imagen de nuestro microservicio `dk-ms-users`
  necesitamos tener, en la imagen que vamos a crear, la misma estructura o niveles de directorios que tenemos en
  nuestro proyecto local hasta el microservicio que queremos construir. La finalidad es, poder mantener la misma
  estructura del proyecto `maven multiple-module`, eso nos permitirá copiar en cada nivel su correspondiente `pom.xml`.
  La única diferencia que observaremos con la estructura de directorios a crear es que en el proyecto de la máquina
  local, el proyecto raíz se llama `docker-kubernetes` mientras que el nombre del directorio raíz dentro de la imagen lo
  llamaré `/app`. Lo importante es tener los mismos niveles de directorios.
- Los `COPY` están haciendo lo que se mencionó en el punto anterior, copiar el `pom.xml` en el respectivo directorio de
  destino. **¡Importante!** el origen de las copias que se hace, es a partir de la raíz del proyecto.
- En el tercer `COPY` si hay diferencia, lo que se está haciendo es copiar todo el contenido del
  directorio `./business-domain/dk-ms-users`, eso incluye obviamente también el `pom.xml`, en el `WORKDIR` que definimos
  al inicio, es decir, todo el contenido se copiará en `/app/business-domain/dk-ms-users`.
- `RUN sed -i -e 's/\r$//' ./mvnw`, este comando lo colocamos solo porque nos surgió el siguiente error cuando tratamos
  de generar la imagen:

  ````bash
  $ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
  [+] Building 4.5s (11/11) FINISHED
   > [6/6] RUN ./mvnw clean package -DskipTests:
  0.558 /bin/sh: ./mvnw: not found
  ------
  Dockerfile:10
  --------------------
    10 | >>> RUN ./mvnw clean package -DskipTests
    11 |
    12 |     EXPOSE 8001
  --------------------
  ERROR: failed to solve: process "/bin/sh -c ./mvnw clean package -DskipTests" did not complete successfully: exit code: 127
  ````

  > Según `ChatGPT`, la instrucción `RUN sed -i -e 's/\r$//' ./mvnw`, es un comando de Linux que utiliza sed, el editor
  > de flujo, para eliminar los caracteres de retorno de carro (\r) al final de cada línea en el archivo mvnw. Los
  > caracteres de retorno de carro son caracteres de control que se utilizan en sistemas operativos Windows y en algunos
  > otros sistemas para indicar el final de una línea de texto en un archivo. En sistemas Unix y Linux, se utiliza el
  > carácter de nueva línea (\n) para este propósito.
  >
  > - `sed`: Es el comando para invocar el editor de flujo en Linux.
  > - `-i`: Es una opción que le indica a sed que realice cambios en el archivo en su lugar, es decir, modificará el
      archivo mvnw directamente.
  > - `-e`: Indica que se proporcionará una expresión de script a sed.
  > - `'s/\r$//'`: Es la expresión de script que busca y reemplaza el retorno de carro (\r) al final de cada línea del
      > archivo por una cadena vacía, es decir, lo elimina.
  > - `./mvnw`: Es el archivo en el que se realizará la modificación. En este caso, se asume que el archivo mvnw está en
      el directorio actual (./).
  >
  > Este comando es útil cuando los archivos se han creado o editado en un sistema Windows o en un entorno que
  utiliza retornos de carro, y se deben utilizar en un entorno Linux o Unix donde se espera el carácter de nueva
  línea para indicar el final de una línea. Al eliminar los caracteres de retorno de carro, se asegura que el
  archivo sea compatible con el sistema en el que se está utilizando.

  **CONCLUSIÓN**
  Como estoy en windows, el fichero `mvnw` no se ejecuta bien en el linux de docker, ya que ahora, como reinstalé
  Docker, utilicé en la instalación la opción `WSL 2 (Windows Subsystem for Linux)`.


- `RUN ./mvnw clean package -DskipTests`, RUN es para cuando se construye las imágenes, mientras que CMD o ENTRYPOINT es
  para cuando se levanta contenedores. En ese sentido, cuando se construya la imagen y llegue a esa instrucción lo que
  hará será ejecutar el archivo `./mvnw` que está ubicado en `/app/business-domain/dk-ms-users`, es decir, de esto
  `./mvnw`, el `./` corresponden al `WORKDIR` definido al inicio.
- `CMD ["java", "-jar", "./target/dk-ms-users-0.0.1-SNAPSHOT.jar"]`, como se mencionó en el punto anterior, este comando
  es usado cuando se levanta el contenedor.

  > Del punto anterior, es muy importante colocar el nombre de archivo `jar` generado en la compilación.
  > Por defecto el nombre del jar para ese microservicio es `dk-ms-users-0.0.1-SNAPSHOT.jar`. Ahora, notar que
  > estamos iniciando con `./target/...`, eso significa que cuando se compiló el `jar` dentro del `WORKDIR`, se generó
  > el directorio `/target` y dentro de él el `jar`.

Ahora, llega el momento de ejecutar el comando del que tanto hablaba al inicio. Como mencioné, debemos posicionarnos en
la raíz del proyecto `/docker-kubernetes`, ya que desde ese punto, se empezará a realizar los `COPY` definidos en
el `Dockerfile` y todo lo demás.

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
````

**DONDE**

- `.`, el primer punto (.) sigue **representando el contexto actual donde Docker buscará otros recursos necesarios para
  la construcción de la imagen.** Es decir, es el directorio actual donde estamos posicionados.
- `-f .\business-domain\dk-ms-users\Dockerfile`, indica la ruta y el nombre del archivo Dockerfile a utilizar.

Entonces, en este caso, estás siendo explícito al decirle a Docker que utilice el `Dockerfile` ubicado en el
subdirectorio `.\business-domain\dk-ms-users`. Esto difiere del comando `docker build .` que **busca automáticamente un
Dockerfile en el directorio actual sin la necesidad de especificar su nombre y ubicación.**

Listamos las imágenes para ver el que acabamos de crear, ¡Wow! nos damos con la sorpresa de que un tamaño muy grande
502MB. En el siguiente capítulo veremos cómo optimizar aún más:

````bash
$ docker image ls
REPOSITORY    TAG       IMAGE ID       CREATED          SIZE
dk-ms-users   latest    99a21f6ec731   59 seconds ago   502MB
````

Procedemos a ejecutar un contenedor a partir de la imagen anterior:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE          COMMAND                  CREATED          STATUS          PORTS                    NAMES
b501a8e94b43   dk-ms-users    "java -jar ./target/…"   50 seconds ago   Up 49 seconds   0.0.0.0:8001->8001/tcp   modest_lumiere
````

Verificamos funcionamiento de nuestra aplicación contenerizada:

````bash
$ curl -v http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 2,
    "name": "Martin",
    "email": "martin@gmail.com",
    "password": "12345"
  },
  {...}
]
````

## Optimizando un Dockerfile parte 2 - Añadiendo nuevas capas

Si bien es cierto, las modificaciones que hicimos al `Dockerfile` en la sección anterior nos evita estar generando
manualmente el `jar` y luego a partir de eso crear la imagen, pero aún hay un pequeño problema, **¿cuál?**:

Cuando realicemos una modificación al código fuente, tenemos que volver a ejecutar el comando para generar nuevamente
la imagen, hasta ahí todo correcto, el problema ocurre cuando la instrucción llega en los puntos siguientes:

````dockerfile
# Other instructions
COPY ./business-domain/dk-ms-users ./
# Other instruction
RUN ./mvnw clean package -DskipTests
# Other instructions
````

Como hemos modificado el código fuente, y ese código fuente está en el directorio `./business-domain/dk-ms-users`,
`docker` sabe que algún archivo dentro de esa dirección ha cambiado, pero no sabe cuál, entonces el `COPY`
vuelve a copiar todo el contenido de ese directorio hacia el `WORKDIR` que definimos al inicio y a partir de ahí hacia
abajo, empieza a realizar todo **como si fuera la primera vez**, es por eso que, cuando llega al `RUN`, esa instrucción
hace que **nuevamente se empiecen a descargar todas las dependencias**, ya que dentro de los archivos copiados está
el `pom.xml` del propio proyecto. Eso no debería ocurrir, porque **"solo modificamos el código fuente"**, por lo que
las dependencias ya lo teníamos descargadas. **¿Cómo solucionarlo?**

Para eso, debemos realizar las siguientes modificaciones en el `Dockerfile` que consistirán básicamente en:

1. Descargar todas las dependencias
2. Copiar el código fuente a la imagen
3. Generar el `.jar` a partir de los dos pasos anteriores

````dockerfile
FROM openjdk:17-jdk-alpine
WORKDIR /app/business-domain/dk-ms-users

COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/dk-ms-users/pom.xml ./
COPY ./business-domain/dk-ms-users/mvnw ./
COPY ./business-domain/dk-ms-users/.mvn ./.mvn

RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline

COPY ./business-domain/dk-ms-users/src ./src
RUN ./mvnw clean package -DskipTests

EXPOSE 8001
CMD ["java", "-jar", "./target/dk-ms-users-0.0.1-SNAPSHOT.jar"]
````

**DONDE**

- Los tres primeros `COPY` copian el `pom.xml` del código fuente local hacia su correspondiente nivel en la estructura
  de directorios definida en el `WORKDIR`.
- El cuarto `COPY`, copia el archivo `mvnw` dentro del `WORKDIR` de la imagen `(/app/business-domain/dk-ms-users)`.
- El quinto `COPY`, copia el contenido del directorio `.mvn` dentro de un directorio con el mismo nombre `.mvn` que se
  creará dentro del `WORKDIR` de la imagen.
- `RUN sed -i -e 's/\r$//' ./mvnw`, este comando lo vimos en la sección anterior, es utilizado antes de ejecutar el
  archivo `.mvnw`, para que haga la conversión de dicho archivo y no haya errores cuando se ejecute con el RUN.
- `RUN ./mvnw dependency:go-offline`, con este comando iniciamos la descarga de las dependencias de maven.
- `COPY ./business-domain/dk-ms-users/src ./src`, copiamos solo el código fuente que está ubicado en el
  directorio `.../src`. Lo copiamos dentro de un directorio `/src` pero que estará dentro del `WORKDIR`.
- `RUN ./mvnw clean package -DskipTests`, iniciamos la creación del `jar`, pero esta vez, ya no volverá a descargar las
  dependencias, ya las tenemos descargadas en las capas anteriores.

Listo, con esas modificaciones realizadas a nuestro `Dockerfile`, cada vez que cambiemos algo en el código fuente,
las dependencias ya no volverán a descargarse, porque lo que modificamos fue el código fuente y no las dependencias, por
lo tanto, **la velocidad de creación de la imagen será más rápido.**

**NOTA 1**
> En el quinto `COPY` estamos copiando prácticamente el directorio `.mvn` y su contenido a la imagen de docker, pero,
> **¿qué es ese directorio?**
>
> El directorio `.mvn` en una aplicación de Spring Boot es un directorio especial que se utiliza para alojar archivos
> relacionados con la construcción y configuración del proyecto. En particular, el directorio `.mvn` **se utiliza para
> personalizar la construcción del proyecto utilizando el mecanismo de "wrapper" de Maven.**
>
> El `Maven Wrapper (o simplemente "wrapper")` **es una forma de garantizar que un proyecto se construya con una versión
> específica de Maven**, independientemente de la versión de Maven instalada en el sistema del desarrollador. Esto puede
> ser útil para garantizar que todos los miembros del equipo utilicen la misma versión de Maven y para simplificar la
> configuración del entorno de construcción.
>
> Dentro del directorio `.mvn`, normalmente encontrarás dos archivos clave:
>
> `wrapper/ (subdirectorio):` Este subdirectorio contiene los archivos necesarios para el Maven Wrapper. Los archivos
> más importantes son:
> - `maven-wrapper.properties` especifica la versión de Maven que se utilizará y cómo se descargará si no está presente.
> - `maven-wrapper.jar` es una biblioteca que permite ejecutar Maven sin tenerlo instalado localmente.

**NOTA 2**
> `RUN ./mvnw dependency:go-offline`:<br>
> - `dependencia:go-offline`, objetivo (goal) que resuelve todas las dependencias del proyecto, incluyendo plugins e
    informes y sus dependencias. Después de ejecutar este objetivo, podemos trabajar con seguridad en modo offline.
> - El objetivo `dependency:go-offline` descarga todas las dependencias del proyecto y las almacena en el repositorio
    local de Maven en la imagen de Docker.
> - Esto es útil para garantizar que todas las dependencias estén disponibles sin necesidad de una conexión a Internet
    durante la construcción de la imagen de Docker.
> - Esta instrucción no construye el proyecto ni empaqueta la aplicación Spring Boot.
>
> **Conclusión:** utilizamos esa instrucción, ya que solo deseamos descargar las dependencias y preparar el entorno de
> Maven para una construcción posterior.

Probemos los cambios realizados. Ejecutemos por primera vez la imagen y veamos cuánto tiempo se demora en crearla:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
[+] Building 260.2s (17/17) FINISHED
````

Bien, se demoró `260.2s = 4' 20"` aproximadamente. Ahora veamos la imagen que nos generó:

````bash
$ docker image ls
REPOSITORY    TAG       IMAGE ID       CREATED              SIZE
dk-ms-users   latest    27654cbed002   About a minute ago   579MB
````

**Realizamos un cambio en el código fuente**, y volvemos a **generar la imagen por segunda vez:**

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
[+] Building 19.8s (17/17) FINISHED
````

Bien, esta vez se demoró `19.8"` aproximadamente, eso significa que nuestra configuración está funcionando. Ahora,
generamos un contenedor a partir de la imagen anterior para comprobar que todo está funcionando como antes:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE         COMMAND                  CREATED          STATUS         PORTS                    NAMES
b3ff48ab28e6   dk-ms-users   "java -jar ./target/…"   15 seconds ago   Up 13 seconds  0.0.0.0:8001->8001/tcp   hungry_shockley
````

Comprobamos que nuestra aplicación alojada dentro del contenedor, se está ejecutando correctamente:

````bash
$ curl -v http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 2,
    "name": "Martin",
    "email": "martin@gmail.com",
    "password": "12345"
  },
  {...}
]
````

## Optimizando Dockerfile con compilaciones de varias etapas o multi-stage builds

En la sección anterior, vimos que la creación de la imagen fue más rápido, pero aun el tamaño de la imagen generada es
muy grande, esto ocurre porque la imagen generada contiene el código fuente y otros archivos que solo lo requerimos para
poder realizar la compilación, etc. Entonces, para reducir el peso de la imagen, necesitamos generar una imagen que
contenga solo el `jar` de nuestra aplicación, es decir el resultado final. Para eso podemos usar la construcción
`Multi-stage`.

### [Multi-stage builds](https://docs.docker.com/build/building/multi-stage/)

Las compilaciones multietapa son útiles para cualquiera que haya luchado por optimizar los archivos Docker sin que dejen
de ser fáciles de leer y mantener.

### Use multi-stage builds

Con las compilaciones multietapa, **utilizas múltiples instrucciones FROM en tu Dockerfile**. Cada instrucción FROM
puede utilizar una base diferente, y cada una de ellas inicia una nueva etapa de la compilación. **Puede copiar
artefactos de forma selectiva de una etapa a otra, dejando atrás todo lo que no desee en la imagen final.**

A continuación se muestra la modificación realizada al `Dockerfile` que ahora es `Multi-stage`:

````dockerfile
FROM openjdk:17-jdk-alpine AS builder
WORKDIR /app/business-domain/dk-ms-users
COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/dk-ms-users/pom.xml ./
COPY ./business-domain/dk-ms-users/mvnw ./
COPY ./business-domain/dk-ms-users/.mvn ./.mvn
RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline
COPY ./business-domain/dk-ms-users/src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/business-domain/dk-ms-users/target/*.jar ./app.jar
EXPOSE 8001
CMD ["java", "-jar", "app.jar"]
````

**DONDE**

- `FROM openjdk:17-jdk-alpine AS builder`, puedes nombrar tus etapas, añadiendo un `AS <NAME>` a la instrucción `FROM`.
  En mi caso, nombré a esta primera etapa como `builder`.
- `FROM openjdk:17-jdk-alpine`, esta segunda etapa no le puse un nombre por defecto.
- Cada `FROM` inicia una nueva etapa, por lo que la configuración anterior tiene 2 etapas.
- `COPY --from=builder /app/business-domain/dk-ms-users/target/*.jar ./app.jar`, en la segunda etapa, esta instrucción
  copia de la etapa `builder`, de su directorio `/app/business-domain/dk-ms-users/target/` el archivo generado que
  termina en `*.jar`, lo copia hacia el `WORKDIR /app` de esta segunda etapa.
- `CMD ["java", "-jar", "app.jar"]`, se ejecuta cuando se crean contenedores y se ejecuta en la raíz del `WORKDIR /app`.

Dejamos limpio docker y ejecutamos el comando para la construcción de la imagen. Vemos que al construir la imagen por
primera vez con la nueva configuración, el tiempo tomado fue de `260.2s == 4' 20"`:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
[+] Building 260.2s (18/18) FINISHED
````

Ahora, modificamos algo en el código fuente y volvemos a crear la imagen. En esta segunda vez que se construyó la imagen
el tiempo tomado para la construcción fue de `21.8s`:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
[+] Building 21.8s (19/19) FINISHED
````

Ahora, revisemos cuál es el tamaño de la imagen generada `387MB`:

````bash
$ docker image ls
REPOSITORY    TAG       IMAGE ID       CREATED          SIZE
dk-ms-users   latest    d66046b6ba18   54 seconds ago   387MB
````

Creamos el contenedor a partir de la imagen anterior:

````bash
$  docker container ls -a
CONTAINER ID   IMAGE         COMMAND               CREATED          STATUS          PORTS                    NAMES
82eb5fa89ca9   dk-ms-users   "java -jar app.jar"   29 seconds ago   Up 28 seconds   0.0.0.0:8001->8001/tcp   flamboyant_maxwell
````

Finalmente, verificamos que nuestra aplicación de Spring Boot dentro del contenedor está funcionando correctamente:

````bash
$ curl -v http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 200
< Content-Type: application/json

<
[
  {
    "id": 2,
    "name": "Martin",
    "email": "martin@gmail.com",
    "password": "12345"
  },
  {...}
]
````

## [Logging to a File](https://reflectoring.io/springboot-logging/)

Podemos escribir nuestros registros en una ruta de archivo estableciendo solo una de las propiedades `logging.file.name`
o `logging.file.path` en nuestro `application.properties`. Por defecto, para la salida de archivos, el nivel de
registro se establece en `info`.

En nuestro caso, utilizaremos la siguiente configuración en nuestro `application.yml` para guardar el log en un
archivo personalizado llamado `dk-ms-users.log` en la ruta `/app/logs/`:

````yaml
## More properties
logging:
  # Another property
  file:
    name: /app/logs/dk-ms-users.log
````

Ahora, otra forma de obtener el log es usando la configuración del `path`, en ese caso, la salida al archivo por defecto
se llamará `spring.log` y se ubicará en el path definido, por ejemplo `/app/logs/`:

````yaml
logging:
  file:
    path: /app/logs
````

Si se establecen ambas propiedades, solo tiene efecto `logging.file.name`.

Ahora, al momento de construir la imagen, debemos crear manualmente el directorio `/app/logs` donde el
`dk-ms-users.log` estará almacenado. Para eso debemos agregar la siguiente instrucción a nuestro `Dockerfile`:

````dockerfile
# First Stage
# Second Stage
FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir ./logs
# Other code
CMD ["java", "-jar", "app.jar"]
````

Como observamos, en la segunda etapa de la construcción de la imagen estamos creando el directorio `/logs` con la
instrucción: `RUN mkdir ./logs`. Recordar que el `./` corresponden al `WORKDIR`, por lo que el directorio creado
finalmente será `/app/logs`.

---

# Sección 9: Docker Networks: Comunicación entre contenedores

---

## Dockerizando microservicio cursos y configurando la red o network

La mayor parte del trabajo realizado en esta sección es en el microservicio `dk-ms-courses`, pero aquí en
el `dk-ms-users` también requerimos hacer ciertos cambios para establecer la comunicación con el otro microservicio.

Los cambios realizados en este microservicio `dk-ms-users` serán en la interfaz del cliente feign `ICourseFeignClient`:

````java

@FeignClient(name = "dk-ms-courses", url = "dk-ms-courses:8002", path = "/api/v1/courses")
public interface ICourseFeignClient {
    /* code */
}
````

Recordemos que antes del cambio realizado, el valor del url era `url = "host.docker.internal:8002"`, donde estábamos
usando un dominio especial de docker llamado `host.docker.internal`, que nos permitía comunicarnos **desde adentro del
contenedor hacia afuera, hacia nuestra máquina local**, donde precisamente estába corriendo en nuestro IDE de
IntelliJ IDEA la aplicación del microservicio `dk-ms-courses`. Es decir, nuestra aplicación `dk-ms-users` dockerizada
estába comunicándose con la aplicación `dk-ms-courses` no deckerizada.

Ahora, llega el momento de dockerizar la aplicación de `dk-ms-courses` y nuestra aplicación dockerizada `dk-ms-users`
debe apuntar a esa aplicación que ahora estará dockerizada, es por esa razón que cambiamos el `host.docker.internal` por
el nombre del contenedor al que apuntará, es decir, cuando creemos un contenedor del microservicio de cursos, debemos
asignarle un nombre con `--name` llamado `dk-ms-courses` y es ese nombre que hace referencia en la `url`.

**En resumen:**

- `name = "dk-ms-courses"`, hace referencia al nombre del microservicio que consumiremos. El nombre está definido en el
  microservicio a consumir, en su archivo application.yml
- `url = "dk-ms-courses:8002"`, hace referencia al nombre del contenedor que le daremos cuando se cree con la bandera
  `--name`. El puerto seguirá siendo el mismo.

Como hemos realizado modificaciones a este microservicio, debemos volver a generar su imagen para tener los cambios:

````bash
$ docker build -t dk-ms-users:v2 . -f .\business-domain\dk-ms-users\Dockerfile

$ docker image ls
REPOSITORY      TAG       IMAGE ID       CREATED         SIZE
dk-ms-courses   latest    b579ec873861   3 minutes ago   385MB
dk-ms-courses   v2        b579ec873861   3 minutes ago   385MB
dk-ms-users     latest    583a7919c097   7 minutes ago   387MB
dk-ms-users     v2        583a7919c097   7 minutes ago   387MB
````

## Dockerizando MySQL

Actualmente, estoy conectando nuestros contenedores del microservicio `dk-ms-users` hacía MySQL que está instalado en mi
máquina local. Pero ahora, vamos a contenerizar `MySQL` para usarlo como un contenedor dentro de nuestra plataforma de
`Docker`. Para eso, necesitamos bajar la `imagen` de MySQL, así que en nuestra terminal ejecutamos el siguiente comando.
Por cierto, bajaré la versión `(tag) 8`:

````bash
$  docker pull mysql:8
````

Si listamos las imágenes, veremos que entre ellas está la imagen bajada de MySQL. Esta imagen por cierto, la bajamos
de la plataforma [Docker Hub](https://hub.docker.com/)

````bash 
$ docker image ls
REPOSITORY      TAG       IMAGE ID       CREATED       SIZE
dk-ms-courses   latest    b579ec873861   6 hours ago   385MB
dk-ms-courses   v2        b579ec873861   6 hours ago   385MB
dk-ms-users     latest    583a7919c097   6 hours ago   387MB
dk-ms-users     v2        583a7919c097   6 hours ago   387MB
mysql           8         a3b6608898d6   6 days ago    596MB
````

A partir de la imagen de MySQL descargada en nuestra plataforma de docker, crearemos un contenedor:

````bash
$ docker container run -d -p 3307:3306 --name mysql-8 --network spring-net -e MYSQL_ROOT_PASSWORD=magadiflo -e MYSQL_DATABASE=db_dk_ms_users mysql:8
abe9d3014495c0707a96420d3c1eee73e9921c9ba72c9bb3857abd0189524cde
````

**DONDE**

- `-p 3307:3306`, el puerto externo estamos colocando en `3307`, ya que actualmente tenemos MySQL en nuestra pc local
  que está corriendo en el puerto `3306`. El puerto interno lo dejamos tal cual `3306`, ya que eso trabaja al interno
  del contenedor, mientras que el externo hace referencia a nuestra máquina local.
- `--name mysql-8`, le damos un nombre al contenedor.
- `--network spring-net`, lo agregamos a la red donde están los otros dos microservicios.
- `-e (--env)`, nos permite establecer variables de entorno. Cada variable de entorno a definir, debe estar precedido
  por la bandera `-e` o `--env`.

Listando los contenedores:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE              COMMAND                  CREATED             STATUS             PORTS                               NAMES
abe9d3014495   mysql:8            "docker-entrypoint.s…"   About an hour ago   Up About an hour   33060/tcp, 0.0.0.0:3307->3306/tcp   mysql-8
040dd8b44572   dk-ms-courses:v2   "java -jar app.jar"      3 hours ago         Up 3 hours         0.0.0.0:8002->8002/tcp              dk-ms-courses
c12fa66e43f3   dk-ms-users:v2     "java -jar app.jar"      3 hours ago         Up 3 hours         0.0.0.0:8001->8001/tcp              dk-ms-users
````

Podemos verificar si podemos conectarnos desde DBeaver instalada en nuestra pc local hacia MySql que ahora mismo está
ejecutándose en el puerto externo `3307` del contenedor `mysql-8`. El resultado debe ser una conexión exitosa.

**IMPORTANTE**
> Si al conectarnos con DBeaver al contenedor de MySQL nos sale el siguiente error
> `MySQL : Public Key Retrieval is not allowed` lo que debemos hacer es una configuración en el DBeaver.
> Vamos a `Ajustes de conexión/Driver properties/allowPublicKeyRetrieval = true`.
>
> [StackOverflow](https://stackoverflow.com/questions/50379839/connection-java-mysql-public-key-retrieval-is-not-allowed)

## Comunicación entre contenedores con BBDD Dockerizadas (MySQL)

En esta sección debemos modificar el `application.yml` del `dk-ms-users` para poder comunicarnos con la base de
datos de `MySQL` que ahora la tenemos contenerizada.

````yaml
# Other properties
datasource:
  url: jdbc:mysql://mysql-8:3306/db_dk_ms_users
# Other properties
````

El cambio realizado en la propiedad anterior fue reemplazar el `host.docker.internal` por el nombre que le dimos al
contenedor de MySQL con la bandera `--name mysql-8`, de esta forma, el contenedor de nuestro microservicio de usuarios
podrá comunicarse con el contenedor de la base de datos de MySQL, siempre y cuando ambos estén en la misma red. En
nuestro caso, haremos que nuestros contenedores estén en la misma red `spring-net`.

Habiendo realizado la modificación en el código fuente del microservicio de `dk-ms-users` volvemos a generar la imagen y
a partir de ella generamos el contenedor:

````bash
$ docker container run -d -p 8001:8001 --rm --name dk-ms-users --network spring-net dk-ms-users:v2
152fff6b17b711b06b39e909a8140b8962bab869bc14e659ec8b2e43a16f7d71
````

Listamos los contenedores:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                COMMAND                  CREATED              STATUS              PORTS                               NAMES
152fff6b17b7   dk-ms-users:v2       "java -jar app.jar"      About a minute ago   Up About a minute   0.0.0.0:8001->8001/tcp              dk-ms-users
b28f9c622dc4   postgres:14-alpine   "docker-entrypoint.s…"   30 minutes ago       Up 30 minutes       0.0.0.0:5433->5432/tcp              postgres-14
c8f8710d2c2b   mysql:8              "docker-entrypoint.s…"   31 minutes ago       Up 31 minutes       33060/tcp, 0.0.0.0:3307->3306/tcp   mysql-8
````

Luego de tener nuestros 4 contenedores levantados, verificamos que estén en la misma red, para eso podemos usar el
siguiente comando:

````bash
$ docker network inspect spring-net
[
    {
        "Name": "spring-net",
        ...
        "ConfigOnly": false,
        "Containers": {
            "152fff6b17b711b06b39e909a8140b8962bab869bc14e659ec8b2e43a16f7d71": {
                "Name": "dk-ms-users",
                "EndpointID": "e0c40f333d210d9fd18b9536ebfa7c76983146b0e836bd199e4f6da1e4d33961",
                "MacAddress": "02:42:ac:12:00:04",
                "IPv4Address": "172.18.0.4/16",
                "IPv6Address": ""
            },
            "4e76998d231467c76a9d2e9b56468f83a642569d38fec8aeffd6de24960cde7e": {
                "Name": "dk-ms-courses",
                "EndpointID": "1ed6f9e628ffda0b3d64e1c39580c9ab73f7e4c56469af57009f411a434cb2ee",
                "MacAddress": "02:42:ac:12:00:05",
                "IPv4Address": "172.18.0.5/16",
                "IPv6Address": ""
            },
            "b28f9c622dc44fd088e9df62c869dcce48ee0468673ae204482e65a589b5cb31": {
                "Name": "postgres-14",
                "EndpointID": "2d68c0d0b54cb0771eba4861bf5a3f15f98a17f29377d77f07e92323f4b5f519",
                "MacAddress": "02:42:ac:12:00:03",
                "IPv4Address": "172.18.0.3/16",
                "IPv6Address": ""
            },
            "c8f8710d2c2bee70c13c52d2871bd511bdf5a61b7e1e6609c752a0d58612e3b3": {
                "Name": "mysql-8",
                "EndpointID": "c83d7f7a029ee5a73f7e514bb87e20a389deaf7ff1dd8abf2660bf18eb54d872",
                "MacAddress": "02:42:ac:12:00:02",
                "IPv4Address": "172.18.0.2/16",
                "IPv6Address": ""
            }
        },
        ...
    }
]
````

## Revisando microservicios dockerizados

Ahora que tenemos nuestras aplicaciones dockerizadas así como las bases de datos, llega el momento de realizar las
peticiones para comprobar si funcionan correctamente.

Guardamos un usuario utilizando nuestro microservicio `dk-ms-users` y la base de datos de `MySQL`, ambos dockerizados:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"martin\", \"email\":\"martin@gmail.com\", \"password\": \"12345\"}" http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 201
< Location: http://localhost:8001/api/v1/users/1
< Content-Type: application/json
<
{
  "id": 1,
  "name": "martin",
  "email": "martin@gmail.com",
  "password": "12345"
}
````

Agregamos nuevos usuarios y los listamos:

````bash
$ curl -v http://localhost:8001/api/v1/users | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 1,
    "name": "martin",
    "email": "martin@gmail.com",
    "password": "12345"
  },
  {
    "id": 2,
    "name": "Alison",
    "email": "alison@gmail.com",
    "password": "12345"
  },
  {
    "id": 3,
    "name": "Tinkler",
    "email": "tinkler@gmail.com",
    "password": "12345"
  }
]
````

## Trabajando con variables de ambiente (ENV)

Veremos un primer acercamiento al uso de las variables de ambiente en este microservicio. El ejemplo será, cambiar
dinámicamente el puerto en la que correrá la aplicación al interior del contendor, de tal forma, cuando creemos un
nuevo contenedor, podremos asignárle dinámicamente un puerto distinto.

> ¡Ojo! solo es para probar el funcionamiento de las variables de entorno, al final dejaremos el puerto interno con el
> mismo valor que hemos venido trabajando hasta ahora.

### Definiendo variable de ambiente en el Dockerfile

Lo primero que haremos será modificar el `application.yml` para utilizar la variable de ambiente `CONTAINER_PORT`:

````yml
server:
  port: ${CONTAINER_PORT:8001}
# properties
````

**DONDE**

- `CONTAINER_PORT`, variable de ambiente.
- `8001`, valor por defecto. Es decir, si la variable CONTAINER_PORT no viene definida, se tomará por defecto el 8001.

Luego, en el `Dockerfile`, podemos definir la variable de ambiente utilizando la instrucción `ENV`:

````dockerfile
# Instrucciones del pimer stage

# Instrucciones del segundo stage
FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/business-domain/dk-ms-users/target/*.jar ./app.jar

ENV CONTAINER_PORT=8000

EXPOSE 8001
CMD ["java", "-jar", "app.jar"]
````

**DONDE**  
`ENV`, esta instrucción establece la variable de entorno `CONTAINER_PORT` al valor `8000`. Este valor estará en el
entorno para todas las instrucciones posteriores en la etapa de construcción y puede ser reemplazado en línea.

**NOTA**
> La instrucción `EXPOSE` tiene hardcodeado el puerto a valor `8001`. Esta instrucción es solo para documentar, pero
> también requerimos que sea dinámico. Eso lo haremos más adelante, ya que recordemos, la instrucción `EXPOSE` hace
> referencia al puerto `externo` y lo que nosotros estamos cambiado, a modo de ejemplo, es el puerto `interno`, por eso
> fue que definí el nombre de la variable de entorno `CONTAINER_PORT` para hacer referencia al puerto interno del
> contenedor. Cuando lleguemos al punto de crear la variable de entorno para la instrucción `EXPOSE` se le colocará de
> nombre `HOST_PORT`, para hacer referencia al puerto que desde el host local nos podremos conectar.

### Probando variable de ambiente definida en el Dockerfile

Como hemos realizado modificaciones al `Dockerfile` es necesario volver a ejecutar `docker build` para construir
nuevamente la imagen:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile

$ docker image ls
REPOSITORY      TAG         IMAGE ID       CREATED          SIZE
dk-ms-users     latest      424435057168   15 seconds ago   387MB
...
````

Ya tenemos la imagen, ahora llega el momento de crear un contenedor:

````bash
$ docker container run -d -p 8001:8000 --rm --name dk-ms-users --network spring-net dk-ms-users
b47571cd9d9eb16f0dc449a45585d77d22b44eb5a9a198af3fea119d4737ea95
````

**DONDE**

- `-p 8001:8000`, el puerto externo sigue siendo `8001`, recordemos que usamos ese valor para poder acceder desde
  nuestra máquina local al contendor. Por otro lado, el cambio que hemos realizado fue en el puerto interno `8000`.
  Esto significa que al interior del contenedor ese puerto estará disponible para la aplicación que se esté ejecutando
  dentro de él.

**IMPORTANTE**
> Es muy importante volver a recalcar que el puerto interno que se asignó al crear el contenedor anterior fue el `8000`.
> Ahora, si el puerto interno que definimos fue el `8000`, por coherencia, el puerto que se le debe asignar a la
> aplicación de Spring Boot debe ser el `8000` y ese valor es lo que precisamente asignamos a la variable de entorno
> `CONTAINER_PORT` dentro del `Dockerfile` y a su vez estamos utilizando esa variable `CONTAINER_PORT` dentro de la
> configuración del `application.yml`.

El contenedor creado fue el siguiente:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                COMMAND                  CREATED          STATUS          PORTS                               NAMES
b47571cd9d9e   dk-ms-users          "java -jar app.jar"      11 minutes ago   Up 11 minutes   8001/tcp, 0.0.0.0:8001->8000/tcp    dk-ms-users
8bdd9a35f2cb   postgres:14-alpine   "docker-entrypoint.s…"   19 hours ago     Up 3 hours      0.0.0.0:5433->5432/tcp              postgres-14
dd97a1adcb06   mysql:8              "docker-entrypoint.s…"   19 hours ago     Up 3 hours      33060/tcp, 0.0.0.0:3307->3306/tcp   mysql-8
````

Comprobemos que nuestra aplicación de Spring Boot obtuvo el valor del puerto definido en la variable de
entorno `CONTAINER_PORT=8000`:

````bash
$ docker container logs dk-ms-users

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

...
2023-11-01T17:32:19.998Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8000 (http)
2023-11-01T17:32:20.031Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2023-11-01T17:32:20.032Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.13]
...
2023-11-01T17:32:28.792Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8000 (http) with context path ''
2023-11-01T17:32:28.829Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Started DkMsUsersApplication in 15.058 seconds (process running for 16.356)
````

Ahora, comprobamos que la aplicación sigue funcionando con el puerto externo de siempre:

````bash
$ curl -v http://localhost:8001/api/v1/users/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "martin",
  "email": "martin@gmail.com",
  "password": "12345"
}
````

### Sobreescribiendo variable de ambiente (ENV) a través de la línea de comando

Al momento de correr un nuevo contenedor, podemos asignar en la misma línea de comando la variable de entorno que
definimos en el `Dockerfile`, de esa forma estaremos sobreescribiendo dicha variable.

**NOTA**
> La variable de ambiente que definamos a través de la línea de comandos, no necesariamente tiene que estar definida
> en el `Dockerfile`, es decir, a través de la línea de comandos podemos definir variables de ambiente, y si existen
> en el `Dockerfile`, obviamente se sobreescribirán, en caso de que no existan, simplemente estarán disponibles en el
> entorno de ejecución de dicho contenedor, por lo que, podrán ser accedidos por ejemplo, desde el `application.yml`.

Recordemos que la variable `CONTAINER_PORT` del `Dockerfile` tiene el valor de `8000`. Ahora, usando la línea de comando
para correr un nuevo contenedor, sobreescribiremos el valor de dicha variable de entorno:

````bash
$ docker container run -d -p 8001:8090 -e CONTAINER_PORT=8090 --rm --name dk-ms-users --network spring-net dk-ms-users
c64be83477480c02e1538f469ba18b37ae21b9f544dfc0bda67a6465bbfe21e5
````

**DONDE**

- `-p 8001:8090`, el valor que le definimos al puerto interno de este nuevo contenedor es `8090`.
- `-e` o `--env`, nos permite definir una variable de ambiente.
- `CONTAINER_PORT=8090`, variable de ambiente definida en la línea de comandos. Esta variable sobreescribe a la variable
  que definimos en el `Dockerfile`, si es que en ese archivo existe dicha variable. Caso contrario, simplemente estamos
  creando la variable para que sea usada por quien la defina al interior del contenedor. En nuestro caso,
  el `application.yml` en la configuración `server.port`.

Listamos los contenedores:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                COMMAND                  CREATED         STATUS         PORTS                               NAMES
c64be8347748   dk-ms-users          "java -jar app.jar"      9 minutes ago   Up 9 minutes   8001/tcp, 0.0.0.0:8001->8090/tcp    dk-ms-users
8bdd9a35f2cb   postgres:14-alpine   "docker-entrypoint.s…"   19 hours ago    Up 4 hours     0.0.0.0:5433->5432/tcp              postgres-14
dd97a1adcb06   mysql:8              "docker-entrypoint.s…"   19 hours ago    Up 4 hours     33060/tcp, 0.0.0.0:3307->3306/tcp   mysql-8
````

Ahora, comprobemos que nuestra aplicación de Spring Boot obtuvo el valor del puerto definido en la variable de
entorno `CONTAINER_PORT=8090` de la línea de comandos:

````bash
$ docker container logs dk-ms-users

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

...
2023-11-01T17:53:38.684Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8090 (http)
...
2023-11-01T17:53:47.593Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8090 (http) with context path ''
2023-11-01T17:53:47.630Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Started DkMsUsersApplication in 15.585 seconds (process running for 16.935)
````

Ahora, comprobamos que la aplicación sigue funcionando con el puerto externo de siempre:

````bash
$ curl -v http://localhost:8001/api/v1/users/2 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 2,
  "name": "Alison",
  "email": "alicon@gmail.com",
  "password": "12345"
}
````

### Definiendo variable de ambiente en archivo de configuración .env

Supongamos que tenemos muchas variables de ambiente y queremos utilizar la línea de comandos y en ella definir dichas
variables. Resultaría muy improductivo hacerlo como en el apartado anterior, en vez de eso, podríamos usar
un archivo `.env` donde definiríamos todas las variables de entorno a usar y simplemente en la línea de comando llamar
a ese archivo.

Creamos el archivo `.env` en la raíz del microservicio `dk-ms-users` y definimos nuestras variables de entorno. Para
nuestro ejemplo, solo definimos una variable:

````bash
CONTAINER_PORT=8888
````

Ahora, al momento de correr un nuevo contenedor debemos llamar a este archivo con la instrucción `--env-file`:

````bash
$ docker container run -d -p 8001:8888 --env-file .\business-domain\dk-ms-users\.env --rm --name dk-ms-users --network spring-net dk-ms-users
01b88306d4d1628faaae062fac9e716858067e2a8732e4f8e77aa4b0f10f5a34
````

**DONDE**

- `--env-file`, instrucción que nos permite leer un archivo de variables de entorno.
- `.\business-domain\dk-ms-users\.env`, ruta donde está ubicada el archivo `.env`.
- `-p 8001:8888`, definimos para este ejemplo el valor del puerto interno a `8888`. Recordar que ese valor también
  deberá ser definido en el `CONTAINER_PORT` del archivo `.env`.

Verificamos que el contenedor esté en la lista de contenedores:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                COMMAND                  CREATED         STATUS         PORTS                               NAMES
01b88306d4d1   dk-ms-users          "java -jar app.jar"      2 minutes ago   Up 2 minutes   8001/tcp, 0.0.0.0:8001->8888/tcp    dk-ms-users
8bdd9a35f2cb   postgres:14-alpine   "docker-entrypoint.s…"   19 hours ago    Up 4 hours     0.0.0.0:5433->5432/tcp              postgres-14
dd97a1adcb06   mysql:8              "docker-entrypoint.s…"   19 hours ago    Up 4 hours     33060/tcp, 0.0.0.0:3307->3306/tcp   mysql-8
````

Ahora, comprobemos que nuestra aplicación de Spring Boot obtuvo el valor del puerto definido en la variable de
entorno `CONTAINER_PORT=8888` del archivo `.env`:

````bash
$ docker container logs dk-ms-users

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

...
2023-11-01T18:24:19.109Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8888 (http)
...
2023-11-01T18:24:27.795Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8888 (http) with context path ''
2023-11-01T18:24:27.838Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Started DkMsUsersApplication in 14.715 seconds (process running for 16.029)
````

Ahora, comprobamos que la aplicación sigue funcionando con el puerto externo de siempre:

````bash
$ curl -v http://localhost:8001/api/v1/users/3 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 3,
  "name": "Tinkler",
  "email": "tinkler@gmail.com",
  "password": "12345"
}
````

## Trabajando con argumentos en el Dockerfile (ARG)

Antes de mostrar los cambios realizados para trabajar con argumentos (ARG), considero importante revisar la siguiente
teoría:

### [Entendiendo cómo interactúan ARG y FROM](https://docs.docker.com/engine/reference/builder/#understand-how-arg-and-from-interact)

Las instrucciones `FROM` soportan variables que son declaradas por cualquier instrucción `ARG` que ocurra antes del
primer `FROM`. Es decir, podemos declarar al inicio de todas las etapas variables del tipo `ARG` y la instrucción
`FROM` de cada etapa las podrá usar sin problema. **Veamos el siguiente ejemplo (no es parte del proyecto):**

````dockerfile
ARG  CODE_VERSION=latest

FROM base:${CODE_VERSION}
CMD  /code/run-app

FROM extras:${CODE_VERSION}
CMD  /code/run-extras
````

Ahora, qué pasa si queremos usar el `ARG` definido al inicio del `Dockerfile` dentro de las etapas, es decir, a
continuación del `FROM` de cada etapa. Bueno, un `ARG` declarado antes de un `FROM` está fuera de una etapa de
construcción, por lo que no se puede utilizar en ninguna instrucción después de un `FROM`. **Para utilizar el valor
predeterminado de un ARG declarado antes del primer FROM**, `utilice una instrucción ARG sin valor` dentro de una
etapa de construcción:

````dockerfile
ARG VERSION=latest

FROM busybox:${VERSION}
# Aquí se está volviendo a definir el ARG VERSION pero sin valor para poder usar
# dentro de esta etapa la variable declarada al inicio del archivo
ARG VERSION
RUN echo ${VERSION} > image_version
````

### Utilizando variables ARG

A continuación se muestra el `Dockerfile` completo del `dk-ms-users` donde hacemos uso de variables `ARG`.

````dockerfile
ARG MICROSERVICE_NAME=dk-ms-users

FROM openjdk:17-jdk-alpine AS builder
ARG MICROSERVICE_NAME
WORKDIR /app/business-domain/${MICROSERVICE_NAME}
COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/${MICROSERVICE_NAME}/pom.xml ./
COPY ./business-domain/${MICROSERVICE_NAME}/mvnw ./
COPY ./business-domain/${MICROSERVICE_NAME}/.mvn ./.mvn
RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline
COPY ./business-domain/${MICROSERVICE_NAME}/src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine
ARG MICROSERVICE_NAME
ARG HOST_PORT=8001
WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/business-domain/${MICROSERVICE_NAME}/target/*.jar ./app.jar
ENV CONTAINER_PORT=8001
EXPOSE ${HOST_PORT}
CMD ["java", "-jar", "app.jar"]
````

**DONDE**

- `ARG MICROSERVICE_NAME=dk-ms-users`, en esta instrucción definimos la variable del tipo `ARG` llamada
  `MICROSERVICE_NAME` y le estamos asignando un valor por defecto `dk-ms-users`. Nótese que este `ARG` está siendo
  definida al inicio del archivo, antes de empezar el primer `FROM`. Esto se hace intencionalmente con la finalidad de
  poder acceder a ese `ARG` desde las otras etapas de construcción.
- Observemos que en las dos etapas del dockerfile, a continuación de cada `FROM` estamos volviendo a definir el
  `ARG MICROSERVICE_NAME`. Esto lo hacemos para poder utilizar el valor del `ARG` definido en la primera línea del
  archivo. Si usamos directamente el `ARG` dentro de las etapas, es decir lo usamos así `${MICROSERVICE_NAME}`, sin
  haber vuelto a definirla, probablemente nos va a marcar algún error o simplemente el `ARG` estará vacío.
- En la segunda etapa de construcción, definí otra variable con su valor `ARG HOST_PORT=8001` y es en esa misma etapa
  que estamos haciendo uso de ella en la instrucción `EXPOSE ${HOST_PORT}`. Esta variable hace referencia al puerto
  externo del contenedor, de tal forma que desde la máquina local podamos acceder al contenedor a través de ese puerto.
  Pero, recordemos que el `EXPOSE` solo funciona como una documentación, no crea el puerto en sí.

Ahora, construiremos la imagen y ejecutaremos un contenedor:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile

$ docker container run -d -p 8001:8001 --rm --name dk-ms-users --network spring-net dk-ms-users
2b7669edced87df22c3aa109aa42549895e90d28ddedd6d0fd954b596df43f61
````

Verificamos el log del contendor:

````bash
$ docker container logs dk-ms-users

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

...
2023-11-02T16:53:06.949Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8001 (http)
...
2023-11-02T16:53:14.149Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8001 (http) with context path ''
2023-11-02T16:53:14.185Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Started DkMsUsersApplication in 13.018 seconds (process running for 14.264)
````

Accedemos desde la pc local hacia el contenedor y comprobamos que todo sigue funcionando correctamente:

````bash
$ curl -v http://localhost:8001/api/v1/users/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "martin",
  "email": "martin@gmail.com",
  "password": "12345"
}
````

### Sobreescribiendo variables ARG a través de la línea de comando

Al momento de construir una imagen podemos asignar en la misma línea de comando la variable ARG que hayamos definido
en el `Dockerfile`, de esa forma estaremos sobreescribiendo dicha variable. Esto es posible gracias a la instrucción
`--build-arg <varname>=<value>`.

Realicemos una pequeña modificación al `Dockerfile`, **solo para este ejemplo**. Vamos a comprobar que efectivamente
está tomando la variable ARG que definamos en la línea de comando y la vamos a utilizar tanto en el `ENV CONTAINER_PORT`
COMO EN EL `EXPOSE ${HOST_PORT}`, de esta manera, el valor que coloquemos al `HOST_PORT` por la línea de comando
será el valor con la que la aplicación de Spring Boot se ejecute al interior del contenedor, y además será el valor
que se exponga, como parte de la documentación.

````dockerfile
# Segunta etapa del Dockerfile
ARG HOST_PORT=8001
## Otras instrucciones
#ENV CONTAINER_PORT=8001 # Lo comentamos solo para el ejemplo
ENV CONTAINER_PORT=${HOST_PORT}
EXPOSE ${HOST_PORT}
# Instrucción CMD
````

Ahora, construyamos la imagen definiendo la instrucción `--build-arg` y luego ejecutemos un contenedor:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile --build-arg HOST_PORT=9292

$ docker container run -d -p 9292:9292 --rm --name dk-ms-users --network spring-net dk-ms-users
e0f1aeee37a7f3fe6743f61fe9f4fb5ac54d7766f94d06e095d32166e03ff956
````

Verificamos el log del contenedor y vemos que el puerto donde está ejecutándose la aplicación de Spring Boot es
el puerto `9292`, esto se está aplicando por esta instrucción `ENV CONTAINER_PORT=${HOST_PORT}`:

````bash
$ docker container logs dk-ms-users

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

...
2023-11-02T17:24:11.454Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 9292 (http)
...
2023-11-02T17:24:20.749Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 9292 (http) with context path ''
2023-11-02T17:24:20.796Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Started DkMsUsersApplication in 15.653 seconds (process running for 17.038)
````

Accedemos desde la pc local hacia el contenedor, esta vez usando el puerto definido en el `HOST_PORT=9292` y
comprobamos que sigue funcionando correctamente:

````bash
$  curl -v http://localhost:9292/api/v1/users/2 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 2,
  "name": "Alison",
  "email": "alicon@gmail.com",
  "password": "12345"
}
````

## Variables de ambiente (ENV) para los parámetros de MySQL

Configuramos nuevas variables de ambiente en el `application.yml`:

````yaml
server:
  port: ${CONTAINER_PORT:8001}

spring:
  application:
    name: dk-ms-users

  datasource:
    url: jdbc:mysql://${DATA_BASE_HOST}:${DATA_BASE_PORT}/${DATA_BASE_NAME}
    username: ${DATA_BASE_USERNAME}
    password: ${DATA_BASE_PASSWORD}
# Other properties
````

Las anteriores variables de ambiente las tenemos que definir en el archivo `.env` ya que será a través de ese archivo
que las manejaremos:

````dotenv
# Host and Container
HOST_PORT=8001
CONTAINER_PORT=8001

# Data Base
DATA_BASE_HOST=mysql-8
DATA_BASE_PORT=3306
DATA_BASE_NAME=db_dk_ms_users
DATA_BASE_USERNAME=root
DATA_BASE_PASSWORD=magadiflo
````

Volvemos a construir la imagen:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
````

Ahora arrancaremos un contenedor pero especificando el archivo `.env`, ya que en ese archivo definimos las variables de
ambiente:

````bash
$ docker container run -d -p 8001:8001 --env-file .\business-domain\dk-ms-users\.env --rm --name dk-ms-users --network spring-net dk-ms-users
0c1b8f2a0ddfa0715aedcfc9cf1cbe9aea45b7a4afdf60cebb205916f9d8ce63
````

Verificamos que esté funcionando correctamente haciendo una llamada al api del `dk-ms-users`:

````bash
$ curl -v http://localhost:8001/api/v1/users/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "martin",
  "email": "martin@gmail.com",
  "password": "12345"
}
````

## Revisando variables de ambiente DB con el comando inspect

En la sección anterior luego de haber creado las variables de entorno comprobamos que todo estaba funcionando
correctamente. En esta sección inspeccionaremos el contenedor para ver que allí podemos encontrar las variables
definidas.

````bash
$ docker container inspect dk-ms-users
[
  {
    ...
    "Config": {
        ...
        "Env": [
            "HOST_PORT=8001",
            "CONTAINER_PORT=8001",
            "DATA_BASE_HOST=mysql-8",
            "DATA_BASE_PORT=3306",
            "DATA_BASE_NAME=db_dk_ms_users",
            "DATA_BASE_USERNAME=root",
            "DATA_BASE_PASSWORD=magadiflo",
            "PATH=/opt/openjdk-17/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "JAVA_HOME=/opt/openjdk-17",
            "JAVA_VERSION=17-ea+14"
        ],
        "Cmd": [
            "java",
            "-jar",
            "app.jar"
        ],
        "Image": "dk-ms-users",
        "Volumes": null,
        "WorkingDir": "/app",
        ...
    },
    ...
  }
]
````

## Variables de ambiente (ENV) para los hostnames de los contenedores

En esta sección vamos a colocar en variables de ambiente el host y el puerto del microservicio `dk-ms-courses` con el
que nos vamos a comunicar usando el `Feign Client`, de esa forma evitamos tenerlo hardcodeado. Entonces, en el archivo
`.env` agregamos las nuevas variables de entorno:

````dotenv
# others variables

# Communication with microservice dk-ms-courses
CLIENT_COURSES_HOST=dk-ms-courses
CLIENT_COURSES_PORT=8002
````

A continuación en el `application.yml` creamos **nuestras propiedades personalizadas** que harán uso de las
variables de entorno definidas en el `.env`:

````yaml
# Other properties

# Custom property
microservices:
  communication:
    dk-ms-courses:
      url: ${CLIENT_COURSES_HOST}:${CLIENT_COURSES_PORT}
````

Finalmente, en la interfaz `ICourseFeignClient` usamos la propiedad personalizada que definimos en el `application.yml`.
Una de las características de la anotación `@FeignClient` es que dentro de la `url` podemos usar el `spEL` para poder
acceder a la configuración del `application.yml`:

````java

@FeignClient(name = "dk-ms-courses", url = "${microservices.communication.dk-ms-courses.url}", path = "/api/v1/courses")
public interface ICourseFeignClient {
    /* code */
}
````

Una vez finalizado todos los cambios, es necesario volver a construir la imagen:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
````

Ahora, levantamos un contenedor:

````bash
$ docker container run -d -p 8001:8001 --env-file .\business-domain\dk-ms-users\.env --rm --name dk-ms-users --network spring-net dk-ms-users
57a78403ce768ec4335b330eabef1df803902c89f8e3ae3e3569bbe97c89fca8
````

Finalmente, teniendo en cuenta que en el `dk-ms-courses` también hicimos los mismos cambios, es momento de probar la
comunicación entre ambos microservicios.

La comprobación consistirá, en que desde el `dk-ms-courses` crearemos al usuario `Liz` y lo asignaremos a un curso.
Luego desde nuestro microservicio `dk-ms-users` podremos ver a ese usuario:

````bash
$ curl -v http://localhost:8001/api/v1/users/6 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 6,
  "name": "Liz",
  "email": "liz@gmail.com",
  "password": "12345"
}
````

---

# Sección 17: Kubernetes: Spring Cloud Kubernetes

---

## Configurando nuestros microservicios con Spring Cloud Kubernetes

Para empezar a trabajar con `Spring Cloud Kubernetes` necesitamos agregar las dependencias en este microservicio, pero
como estamos trabajando con `módulos de maven` agregaremos estas dependencias en el módulo padre de este microservicio:

`pom.xml (business-domain)`

````xml

<dependencies>
    <!--Other dependencies-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-kubernetes-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-kubernetes-client-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-kubernetes-client-loadbalancer</artifactId>
    </dependency>
</dependencies>
````

Ahora agregamos la anotación `@EnableDiscoveryClient` en el archivo principal de la aplicación. Esta anotación permite
habilitar una implementación de `DiscoveryClient`:

````java

@EnableDiscoveryClient //<-- Anotación agregada
@EnableFeignClients
@SpringBootApplication
public class DkMsUsersApplication {
    /* code */
}
````

Empezamos a realizar cambios significativos en el código fuente, para ser exactos en la interfaz `ICourseFeignClient`:

````java

@FeignClient(name = "dk-ms-courses", path = "/api/v1/courses")
public interface ICourseFeignClient {
    /* code */
}
````

Lo que quitamos del código anterior fue: `url = "${microservices.communication.dk-ms-courses.url}"`, es decir ahora
ya no necesitamos de esta url, porque ahora usaremos el `nombre del microservicio` como dominio, en reemplazo de
dicha `url`. Recordemos que el nombre del microservicio la definimos en el `application.yml`. También es importante
observar que el `name = "dk-ms-courses"` del `@FefignClient` debe coincidir con el nombre del microservicio con el que
nos vamos a comunicar, además el nombre del servicio de kubernetes también debe ser el mismo.

`@FeignClient` tiene características de load balancer, entonces tal solo colocando la dependencia
`spring-cloud-starter-kubernetes-client-loadbalancer` en el `pom.xml`, automáticamente `@FeignClient` realizará balanceo
de carga.

Como quitamos de la interfaz `ICourseFeignClient` el atributo `url` que tenía definido una propiedad del
`application.yml`, eso quiere decir que dicha propiedad (microservices.communication.dk-ms-courses.url) ya no lo 
usaremos, así que procedemos a eliminarlo del `application.yml`.

Lo que sí debemos agregar es una configuración adicional en el `application.yml`:

````yaml
# Other configurations
spring:
  # Other properties
  cloud:
    kubernetes:
      secrets:
        enable-api: true
      discovery:
        all-namespaces: true
# Other configurations
````

Luego de haber realizado varias modificaciones al código fuente, debemos volver a construir la imagen:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile
[+] Building 147.7s (20/20) FINISHED
````

Renombramos la imagen y luego procedemos a subirlo a `Docker Hub`:

````bash
$ docker tag dk-ms-users magadiflo/dk-ms-users
$ docker push magadiflo/dk-ms-user
````
