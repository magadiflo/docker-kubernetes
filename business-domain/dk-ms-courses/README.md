# Microservicio dk-ms-courses

---

## Dependencias iniciales

Inicialmente, nuestro microservicio `dk-ms-courses` tendrá las siguientes dependencias:

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
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
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
> Si abrimos el `pom.xml` del microservicio `dk-ms-courses` con un editor, no veremos todas las dependencias como se
> muestra en la parte superior, sino más bien las dependencias que solo manejará ese microservicio, es decir
> solo serán propios de ese microservicio `(conector de postgresql)`; las otras dependencias son comunes
> a los otros proyectos, y para evitar estar agregando una y otra vez, lo que hacemos es organizarlos en los módulos
> padres. Es decir, al final nuestro microservicio `dk-ms-courses` sí las usa, ya que lo está heredando y no solo él lo
> usará sino otros microservicios que lo requiereran.

Como hemos agregado el microservicio `dk-ms-courses` dentro del módulo `business-domain`, tenemos que hacer lo mismo
dentro del `pom.xml` del `business-domain`, por lo que ahora ya no tendríamos solo al `dk-ms-users` sino también al
`dk-ms-courses`:

````xml

<modules>
    <module>dk-ms-users</module>
    <module>dk-ms-courses</module>
</modules>
````

## Configurando el contexto de persistencia JPA/Hibernate

Configuramos el `application.yml` dándole un nombre a este microservicio y estableciéndole un puerto:

````yaml
server:
  port: 8002

spring:
  application:
    name: dk-ms-courses
````

## Añadiendo la clase Entity y el CrudRepository

Creamos la entidad `Course`:

````java

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    /* Getters, Setters and toString() methods */
}
````

Creamos el repositorio de la entidad course:

````java
public interface ICourseRepository extends CrudRepository<Course, Long> {
}
````

## Agregando el componente Service

Empezamos creando la interfaz que usará nuestro servicio de courses:

````java
public interface ICourseService {
    List<Course> findAllCourses();

    Optional<Course> findCourseById(Long id);

    Course saveCourse(Course course);

    Optional<Course> updateCourse(Long id, Course courseWithChangeData);

    Optional<Boolean> deleteCourseById(Long id);

}
````

Ahora toca implementar el servicio del course:

````java

@Service
public class CourseServiceImpl implements ICourseService {
    private final ICourseRepository courseRepository;

    public CourseServiceImpl(ICourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAllCourses() {
        return (List<Course>) this.courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findCourseById(Long id) {
        return this.courseRepository.findById(id);
    }

    @Override
    @Transactional
    public Course saveCourse(Course course) {
        return this.courseRepository.save(course);
    }

    @Override
    @Transactional
    public Optional<Course> updateCourse(Long id, Course courseWithChangeData) {
        return this.courseRepository.findById(id)
                .map(courseDB -> {
                    courseDB.setName(courseWithChangeData.getName());
                    return courseDB;
                })
                .map(this.courseRepository::save);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteCourseById(Long id) {
        return this.courseRepository.findById(id)
                .map(courseDB -> {
                    this.courseRepository.deleteById(courseDB.getId());
                    return true;
                });
    }
}
````

## Escribiendo el controlador RestController para cursos

Implementamos los endpoints de nuestro microservicio courses:

````java

@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {
    private final ICourseService courseService;

    public CourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(this.courseService.findAllCourses());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return this.courseService.findCourseById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> saveCourse(@RequestBody Course course) {
        Course courseDB = this.courseService.saveCourse(course);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(courseDB.getId())
                .toUri();
        return ResponseEntity.created(location).body(courseDB);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return this.courseService.updateCourse(id, course)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        return this.courseService.deleteCourseById(id)
                .map(wasDeleted -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
````

## Configurando el datasource y conexión con PostgreSQL

En esta sección configuraremos la conexión a la base de datos de PostgreSQL similar a cómo configuramos el DataSource
del microservicio `dk-ms-users`:

````yaml
# Other property

spring:
  # Other property

  datasource:
    url: jdbc:postgresql://localhost:5432/db_dk_ms_courses
    username: postgres
    password: magadiflo
    driver-class-name: org.postgresql.Driver

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    generate-ddl: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: debug
````

Como tenemos la configuración `spring.jpa.generate-ddl=true`, al ejecutar la aplicación por primera vez, hibernate
creará la tabla `courses` en la BD a partir de la entidad `Course`:

![1.courses-table](./assets/1.courses-table.png)

## Probando API Restful de dk-ms-courses

Llegó el momento de probar los endpoints desarrollados en nuestro microservicio `dk-ms-courses`:

- Guardar un course:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"Docker\"}" http://localhost:8002/api/v1/courses | jq

>
< HTTP/1.1 201
< Location: http://localhost:8002/api/v1/courses/1
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Docker"
}
````

- Listar courses:

````bash
$ curl -v http://localhost:8002/api/v1/courses | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 1,
    "name": "Docker"
  }
]
````

- Ver un course:

````bash
$ curl -v http://localhost:8002/api/v1/courses/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Docker"
}
````

- Actualizar un course:

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"name\": \"Master en Docker\"}" http://localhost:8002/api/v1/courses/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Master en Docker"
}

````

- Eliminar un course:

````bash
$ curl -v -X DELETE http://localhost:8002/api/v1/courses/1 | jq

>
< HTTP/1.1 204
< Date: Fri, 20 Oct 2023 04:23:42 GMT
````

## Validando los datos del JSON

Validaremos los campos de nuestra entidad `Course` utilizando las anotaciones proporcionadas por la dependencia
`spring-boot-starter-validation`. Nuestra entidad `Course` quedaría de esta manera:

````java

@Entity
@Table(name = "courses")
public class Course {
    /* other property */
    @NotBlank
    private String name;
    /* other cod */
}
````

Ahora, en nuestro controlador `CourseController` agregaremos la anotación `@Valid` antes del parámetro `course`, que es
el parámetro que queremos validar:

````java

@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {
    /* other code*/
    @PostMapping
    public ResponseEntity<Course> saveCourse(@Valid @RequestBody Course course) {
        /* code */
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody Course course) {
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
> `...saveCourse(@Valid @RequestBody Course course, BindingResult result){ if(result.hasErrors()){}}`
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

Registramos un curso con dato inválido:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"  \"}" http://localhost:8002/api/v1/courses | jq

< HTTP/1.1 400
< Content-Type: application/json
<
{
  "timestamp": "2023-10-20T17:03:28.4812147",
  "statusCode": 400,
  "httpStatus": "BAD_REQUEST",
  "message": "Error al validar los campos",
  "errors": {
    "name": "Ocurrió un error, el campo name must not be blank"
  }
}
````

---

# Sección 5: Cliente HTTP Feign: Comunicación entre microservicios

---

## Creando JPA Entity CourseUser

Hasta este punto tenemos creados nuestros dos microservicios `dk-ms-courses` y `dk-ms-users`, cada uno manejando su
propia base de datos, aunque solo tenemos una tabla en cada microservicio.

![2.courses-users-table](./assets/2.courses-users-table.png)

Ahora, dejemos a un lado solo por este momento el tema de microservicios y enfoquémonos en la regla de negocio que
trabajaremos en este proyecto:

> Un **usuario** o alumno podrá estar en un único **curso** y en un **curso** podrán estar muchos **usuarios** o
> alumnos. Imaginemos que **cursos** son por ejemplo cursos de deporte donde tú como alumno **puedes elegir
> estar solo en uno de ellos.**
>
> Lo que se quiere lograr es una relación de **One-To-Many**, podríamos haber tomado cualquier otro ejemplo como
> Categoría y Productos y haber realizado todo el proyecto en base a esas entidades, pero bueno, el tutor eligió
> cursos y usuarios para trabajar en todo este proyecto.

Por lo tanto, teniendo nuestra regla de negocio definida, nuestro diagrama ER de Base de Datos quedaría de esta manera:

![3.one-to-many_courses_users](./assets/3.one-to-many_courses_users.png)

Ahora, la pregunta es **¿cómo llevamos esa relación a los microservicios, si cada microservicio tiene su propia tabla y
su propia base de datos independiente?**

Lo que podemos hacer es crear una tabla, en una de las bases de datos, que tenga la función de ser un "espejo" de la
tabla de la otra base de datos y donde solo almacene los identificadores, ya que la información completa la tiene la
otra base de datos.

Y ahora, la pregunta es **¿en qué base de datos creamos la nueva tabla que hará de "espejo" de la otra tabla?**.

Analizando la pregunta anterior, llegamos a la conclusión de que la nueva tabla, a la que llamaremos por cierto
`course_users`, debería estar en el microservicio de `dk-ms-courses` ya que de por sí, un curso necesariamente requiere
usuarios que estén registrados en él para que tenga sentido su razón de existencia, por lo tanto, llevaremos ese
control en dicho microservicio.

![4.one-to-many-microservicios](./assets/4.one-to-many-microservicios.png)

Para finalizar la idea anterior, la tabla `course_users` sería como si colocáramos la tabla `users` dentro del
microservicio `dk-ms-courses` en su reemplazo, pero aquí únicamente contendrá la `id` de la tabla `users` a través del
atributo `user_id`, es decir, el `user_id` sería como la `id` de la tabla `users`. Ahora, con respecto al atributo
`course_id`, como estamos en el microservicio `dk-ms-courses` aquí sí se convierte en un `FK` explícito que apunta a
la tabla `courses`. Finalmente, con respecto al `id` de la tabla `course_users`, solo nos sirve como clave primaria de
la tabla, para nada más. Aquí los dos atributos importantes son `course_id` y el `user_id`.

Listo, una vez habiendo explicado el funcionamiento de la tabla `course_users`, llega el momento de crear la entidad
correspondiente y establecer la relación.

A continuación creamos la entidad `CourseUser` correspondiente a la tabla `course_users` donde debemos observar varios
aspectos importantes:

1. Definimos la propiedad `userId` correspondiente al campo `user_id` que representa conceptualmente la `Primary Key`
   de la tabla `users` en la tabla `course_users`, es decir, es como si `course_users` fuera la tabla `users`. ¡Ojo!
   estoy diciendo que **representa conceptualmente**, es decir, estamos diciendo a qué hace referencia ese atributo.
   Además, estamos diciendo que dicha propiedad es única para evitar que un usuario pueda estar en varios cursos.
2. Sobreescribimos el método `equals()` para decirle a hibernate que cuando se compare una entidad del tipo
   `CourseUser` lo haga a través de la propiedad `userId`.

````java

@Entity
@Table(name = "course_users")
public class CourseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", unique = true)
    private Long userId;

    /* Getter and setter */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseUser that = (CourseUser) o;
        return Objects.equals(userId, that.userId);
    }

    /* toString() method */
}
````

Ahora, en la entidad `Course` establecemos la `relación unidireccional @OneToMany` con la entidad `CourseUser`.
Observemos que además hemos creado dos métodos adicionales `addCourseUser()` y `removeCourseUser()`, precisamente para
eso fue que sobreescribiemos el método `equals()` de la entidad `CourseUser`, para que cuando usemos el
método `removeCourseUser()` elimine la entidad estableciendo la comparación por la propiedad `userId` de la
entidad `CourseUser`:

````java

@Entity
@Table(name = "courses")
public class Course {
    /* id and name properties */

    @JoinColumn(name = "course_id")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseUser> courseUsers = new ArrayList<>();

    /* Getters and Setters from id and name */

    public List<CourseUser> getCourseUsers() {
        return courseUsers;
    }

    public void setCourseUsers(List<CourseUser> courseUsers) {
        this.courseUsers = courseUsers;
    }

    public void addCourseUser(CourseUser courseUser) {
        this.courseUsers.add(courseUser);
    }

    public void removeCourseUser(CourseUser courseUser) {
        this.courseUsers.remove(courseUser);
    }

    /* toString() method */
}
````

## Creando la clase POJO User

Recordemos que en la base de datos del microservicio `dk-ms-courses` únicamente tenemos dos tablas relacionadas:
`courses` y `course_users`. Ahora, cuando recuperemos información de la tabla `course_users` podremos recuperar la
información de la entidad `Course` ya que está en el mismo microservicio, mientras que por el lado de los usuarios,
únicamente nos retornará sus `identificadores`. Entonces, es en ese momento donde requerimos hacer una llamada con
nuestro `Http Feign Client` para solicitarle al microservicio `dk-ms-users` nos retorne la información de todos los
usuarios a partir de su `identificador`, por lo que ahora necesitamos tener en el microservicio `dk-ms-courses` un
objeto que tenga la estructura de la entidad `User`.

Es por eso que crearemos un POJO o un DTO `User`, en mi caso será utilizando un `record` de java. Digamos que esta
clase será una clase de modelo, no un Entity, sino una clase de modelo que representa la estructura de la entidad
`User` del microservicio `dk-ms-users`. Por ejemplo, cuando se solicite todos los usuarios que están registrados en
un curso, esta clase de pojo `User` nos va a servir para representar dicha información en el objeto JSON.

````java
public record User(Long id, String name, String email, String password) {
}
````

Ahora, cuando mostremos información de un curso, necesitamos mostrar información de los usuarios que están registrados
en dicho curso, en ese sentido, necesitamos agregar un atributo de lista de usuarios en la entidad `Course`, pero
debemos anotarlo con `@Transient` para decirle a hibernate que ese atributo no deberá ser mapeado a ningún campo de la
base de datos, sino más bien, es solo un campo que no es parte del contexto de persistencia de JPA/Hibernate. Solo lo
usaremos para poblar los datos de los usuarios.

````java

@Entity
@Table(name = "courses")
public class Course {
    /* other properties */

    @Transient
    private List<User> users = new ArrayList<>();

    /* other methods */

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    /* other method */
}
````

Para dejar más claro el código anterior, recordemos que la entidad `Course` tiene una relación de `@OneToMany` con
la entidad `CourseUser`, entonces cuando recuperemos un curso, se recuperarán también los registros asociados al curso
que están registrados en la tabla `course_users`, para ser más exactos, se recuperarán los `identificadores` de los
usuarios que están registrados en la tabla `course_users` y que pertenecen al curso recuperado. Pero **¿de qué
nos sirve tener los ids de los usuarios?**, pues bien, a partir de esos `ids` recuperados, se hará una llamada al
microservicio `dk-ms-users` para recuperar la información completa de los usuarios, una vez recuperados, necesitamos
de alguna manera asociarlo al curso, eh ahí la razón del porqué creamos el atributo
`@Transient private List<User> users`. De esta forma, cuando enviemos información de un curso al cliente, no solo
enviemos los identificadores de los usuarios asignados a ese curso, sino más bien la información completa.

## Revisando tablas de la Base de Datos y agregando métodos de comunicación HTTP

Si ejecutamos nuestra aplicación veremos la creación de la tabla `course_users` y su relación con la tabla `courses`:

![5.courses_course_users](./assets/5.courses_course_users.png)

Ahora, necesitamos agregar métodos para interactuar con el microservicio `dk-ms-users`, eso lo haremos en
la capa de servicio:

````java
public interface ICourseService {
    /* other methods*/

    Optional<User> assignExistingUserToACourse(User user, Long courseId);

    Optional<User> createUserAndAssignToCourse(User user, Long courseId);

    Optional<User> unassigningAnExistingUserFromACourse(User user, Long courseId);
}
````

Por el momento solo dejaremos definido los métodos, más adelante lo implementaremos, ya que requerimos previamente
configurar el `HTTP Feign Client` con algunos métodos para comunicarnos con el microservicio `dk-ms-users`:

````java

@Service
public class CourseServiceImpl implements ICourseService {
    /* other methods */

    @Override
    public Optional<User> assignExistingUserToACourse(User user, Long courseId) {
        return Optional.empty();
    }

    @Override
    public Optional<User> createUserAndAssignToCourse(User user, Long courseId) {
        return Optional.empty();
    }

    @Override
    public Optional<User> unassigningAnExistingUserFromACourse(User user, Long courseId) {
        return Optional.empty();
    }
}
````

## Escribiendo el Cliente HTTP con Spring Cloud Feign

Como ya tenemos la dependencia de `spring-cloud-starter-openfeign` en nuestro proyecto, podemos usarlo para crear
nuestro cliente rest del tipo Feign. Esto es una alternativa al uso de `RestTemplate` que nos permite realizar llamadas
http para consumir servicios rest.

Lo primero que haremos será agregar la anotación `@EnableFeignClients` en la clase principal del proyecto. Esta
anotación **busca interfaces que declaren ser clientes feign (mediante la anotación @FeignClient).** Además, con
esta anotación **habilitamos en la aplicación el contexto de feign para poder implementar nuestras api rest de forma
declarativa**.

````java

@EnableFeignClients
@SpringBootApplication
public class DkMsCoursesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DkMsCoursesApplication.class, args);
    }

}
````

Ahora, necesitamos crear una interface que hará las peticiones al microservicio de usuarios, esta interfaz estará
anotada con `@FeignClient`. Esta anotación es para interfaces que declara que debe crearse un cliente REST con esa
interfaz (Por ejemplo, **para hacer una inyección en otro componente**). Si SC LoadBalancer está disponible, se
utilizará para equilibrar la carga de las solicitudes del backend, y el equilibrador de carga puede configurarse
utilizando el mismo nombre (es decir, valor) que el cliente feign.

Como se mencionó anteriormente, de forma automática la interfaz anotada con `@FeignClient` se convierte en un componente
de Spring para poder ser inyectado en otro componente. Es como cuando usamos el `CrudRepository<>`, es decir, por debajo
se implementa la funcionalidad.

A continuación se muestra nuestra interfaz `IUserFeignClient`:

````java

@FeignClient(name = "dk-ms-users", url = "localhost:8001", path = "/api/v1/users")
public interface IUserFeignClient {
    @GetMapping(path = "/{id}")
    User getUser(@PathVariable Long id);

    @PostMapping
    User saveUser(@RequestBody User user);
}
````

**DONDE**

- `name`, corresponde al nombre del microservicio que vamos a consumir. En este caso, el nombre lo definimos en
  la propiedad  `spring.application.name` del `application.yml` del microservicio `dk-ms-users`.
- `url`, una URL absoluta o un nombre de host resoluble (el protocolo es opcional).
- `path`, prefijo de ruta que deben utilizar todas las asignaciones a nivel de método.

En el `IUserFeignClient` hemos definido dos métodos que corresponden a los endpoints que consumiremos del microservicio
de usuarios. Si vamos a ese microservicio y vemos esos dos endpoints veremos lo siguiente:

````java
/**
 * En el microservicio dk-ms-users
 */
@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

    @GetMapping(path = "/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        /* code */
    }

    @PostMapping
    public ResponseEntity<User> saveUser(@Valid @RequestBody User user) {
        /* code */
    }
}
````

**¿Qué podemos concluir?** el método definido en la interfaz `IUserFeignClient` es similar al que está definido en
el controlador que consumiremos. En realidad lo que nos interesa es la firma del endpoint, lo que recibe y lo que
retorna, el nombre del método que definamos en la interfaz da lo mismo. Ahora, otro punto a observar es que en el
endpoint del microservicio de usuarios está retornando un `ResponseEntity<User>` y nosotros hemos colocado en la
interfaz solo `User`, eso está bien, por debajo cuando se construya la implementación, spring lo resolverá y nos
retornará el `User`. Por último en el método `saveUser(@Valid...)` del microservicio de usuarios está la anotación
`@Valid` que permite validar los campos cuando se envíe a ese endpoint un objeto de usuario, pero en nuestra interfaz
`IUserFeignClient` no lo definimos, eso es porque en esta interfaz lo que hacemos es **consumir** el endpoint, mas no
validar los datos.

Ahora que tenemos definido nuestro cliente feign, lo inyectamos en la clase de servicio para su posterior uso:

````java

@Service
public class CourseServiceImpl implements ICourseService {

    private final ICourseRepository courseRepository;
    private final IUserFeignClient userFeignClient;

    public CourseServiceImpl(ICourseRepository courseRepository, IUserFeignClient userFeignClient) {
        this.courseRepository = courseRepository;
        this.userFeignClient = userFeignClient;
    }
    /* other code */
}
````

## Añadiendo e implementando métodos de comunicación HTTP en el Service

Implementamos los métodos que quedaron pendientes en el servicio `CourseServiceImpl`. Recordar que los dejamos
pendientes porque previamente requerimos definir el cliente feign, ya que estos métodos requieren hacer llamadas al
microservicio de usuarios para su funcionamiento.

````java

@Service
public class CourseServiceImpl implements ICourseService {

    /* other properties and methods */

    @Override
    @Transactional
    public Optional<User> assignExistingUserToACourse(User user, Long courseId) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> {
                    User userMsDB = this.userFeignClient.getUser(user.id()); //<-- Puede ocurrir un FeignException
                    this.assignUserToCourse(userMsDB, courseDB);
                    return userMsDB;
                });
    }

    @Override
    @Transactional
    public Optional<User> createUserAndAssignToCourse(User user, Long courseId) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> {
                    User userMsDB = this.userFeignClient.saveUser(user); //<-- Puede ocurrir un FeignException
                    this.assignUserToCourse(userMsDB, courseDB);
                    return userMsDB;
                });
    }

    @Override
    @Transactional
    public Optional<User> unassigningAnExistingUserFromACourse(User user, Long courseId) {
        return this.courseRepository.findById(courseId)
                .map(courseDB -> {
                    User userMsDB = this.userFeignClient.getUser(user.id()); //<-- Puede ocurrir un FeignException
                    CourseUser courseUser = new CourseUser();
                    courseUser.setUserId(userMsDB.id());
                    courseDB.removeCourseUser(courseUser);// Aquí comparará por el userId que definimos en el método equals
                    this.courseRepository.save(courseDB);
                    return userMsDB;
                });
    }

    private void assignUserToCourse(User userMsDB, Course courseDB) {
        CourseUser courseUser = new CourseUser();
        courseUser.setUserId(userMsDB.id());
        courseDB.addCourseUser(courseUser);
        this.courseRepository.save(courseDB);
    }
}
````

Notar que en la comunicación que realizamos con `FeignClient` podemos obtener errores, ya que nos comunicamos con otro
microservicio y por ejemplo, se puede ir la red, el servidor del otro microservicio puede caerse, existe latencia,
el usuario que se busca no existe, etc. por lo que de alguna manera debemos manejar el error que se produzca para
enviarle al cliente. En la siguiente sección manejaremos esa posible excepción que pueda ocurrir.

## Añadiendo métodos de comunicación en el controlador rest

Como se comentó en la sección anterior, **necesitamos manejar las excepciones producidas por nuestro cliente Feign**,
eso lo haremos en nuestro `@RestControllerAdvice`:

````java

@RestControllerAdvice
public class GlobalExceptionHandler {
    /* other code */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ExceptionHttpResponse> feignException(FeignException exception) {
        String message = "Error en la comunicación entre microservicios: " + exception.getMessage();
        return this.exceptionHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
    /* other code */
}
````

Ahora sí, con total tranquilidad nos vamos a implementar nuestro controlador:

````java

@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {
    /* other code */
    @PutMapping(path = "/assign-user-to-course/{courseId}")
    public ResponseEntity<User> assignExistingUserToACourse(@RequestBody User user, @PathVariable Long courseId) {
        return this.courseService.assignExistingUserToACourse(user, courseId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/create-user-and-assign-to-course/{courseId}")
    public ResponseEntity<User> createUserAndAssignToCourse(@RequestBody User user, @PathVariable Long courseId) {
        return this.courseService.createUserAndAssignToCourse(user, courseId)
                .map(userDB -> {
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(userDB.id())
                            .toUri();
                    return ResponseEntity.created(location).body(userDB);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/unassigning-user-from-a-course/{courseId}")
    public ResponseEntity<User> unassigningAnExistingUserFromACourse(@RequestBody User user, @PathVariable Long courseId) {
        return this.courseService.unassigningAnExistingUserFromACourse(user, courseId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    /* other code */
}
````

## Probando comunicaciones HTTP entre microservicios

Llegó el momento de probar el funcionamiento de los nuevos endpoints en el microservicio de cursos:

- Asignando un usuario existente a un curso:

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"id\": 2, \"name\": \"Martin\", \"email\": \"martin@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/assign-user-to-course/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 2,
  "name": "Martin",
  "email": "martin@gmail.com",
  "password": "12345"
}
````

- Creando un usuario y luego asignándolo a un curso:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"Alicia\", \"email\": \"alicia@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1 | jq

>
< HTTP/1.1 201
< Location: http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1/5
< Content-Type: application/json
<
{
  "id": 5,
  "name": "Alicia",
  "email": "alicia@gmail.com",
  "password": "12345"
}
````

- Des-asignando un usuario de un curso:

````bash
$ curl -v -X DELETE -H "Content-Type: application/json" -d "{\"id\": 6, \"name\": \"Alison\", \"email\": \"alison@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/unassigning-user-from-a-course/2 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 6,
  "name": "Alison",
  "email": "alison@gmail.com",
  "password": "12345"
}
````

- **Listando los cursos:** A continuación debemos notar que el curso con `id = 1` tiene dos usuarios asignados, pero
  únicamente vemos el identificador, es decir no vemos el detalle completo del usuario, eso está bien, ya que lo que en
  realidad se está mostrando es la entidad `CourseUser` que como vimos, este únicamente tiene el identificador del
  usuario como atributo. Ahora, si queremos ver toda la información del usuario, eso se hará más adelante y se poblará
  en el atributo `users`, que como vemos, ahora está vacío. Otro punto a añadir es que esos detalles del usuario, los
  mostraremos únicamente cuando veamos un curso determinado y no cuando traigamos todos los cursos como en el resultado
  de abajo, eso lo hacemos por temas de rendimiento.

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
  {
    "id": 2,
    "name": "Angular",
    "courseUsers": [],
    "users": []
  },
  {
    "id": 3,
    "name": "Docker",
    "courseUsers": [],
    "users": []
  }
]
````

- Probando manejo de error cuando ocurre una excepción del `Feign Client`:

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"id\": 10, \"name\": \"Hacker\", \"email\": \"hacker@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/assign-user-to-course/1 | jq

>
< HTTP/1.1 500
< Content-Type: application/json
<
{
  "timestamp": "2023-10-23T23:53:19.3384155",
  "statusCode": 500,
  "httpStatus": "INTERNAL_SERVER_ERROR",
  "message": "Error en la comunicación entre microservicios: [404] during [GET] to [http://localhost:8001/api/v1/users/10] [IUserFeignClient#getUser(Long)]: []"
}
````

## dk-ms-courses detalle del curso con los alumnos asignados

Como en el microservicio `dk-ms-users` implementamos un nuevo endpoint para obtener el detalle completo de los usuarios,
ahora, en este microservicio llega el momento de usarlo. Para eso necesitamos consumir dicho endpoint usando nuestro
cliente feign:

````java

@FeignClient(name = "dk-ms-users", url = "localhost:8001", path = "/api/v1/users")
public interface IUserFeignClient {
    /* other method */

    // Usamos Iterable dentro del parámetro del método en reemplazo de List, ya que como estamos usando Feign para 
    // consumir el endpoint, aparentemente si usamos List, nos traería problemas.
    @GetMapping(path = "/group")
    List<User> findAllById(@RequestParam Iterable<Long> userIds);

    /* other method */
}
````

En la interfaz `ICourseService` agregamos el nuevo método para buscar el curso por su id y que nos retorne con los
detalles completos de sus usuarios:

````java
public interface ICourseService {
    /* other methods */
    Optional<Course> findCourseByIdWithFullUsersDetails(Long id);
    /* other methods */
}
````

Ahora, implementamos el método anterior en la clase de servicio:

````java

@Service
public class CourseServiceImpl implements ICourseService {
    /* other methods */
    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findCourseByIdWithFullUsersDetails(Long id) {
        return this.courseRepository.findById(id)
                .map(courseDB -> {
                    if (!courseDB.getCourseUsers().isEmpty()) {
                        List<Long> userIds = courseDB.getCourseUsers().stream().map(CourseUser::getUserId).toList();
                        List<User> users = this.userFeignClient.findAllById(userIds);
                        courseDB.setUsers(users);
                    }
                    return courseDB;
                });
    }
    /* other methods */
}
````

En el controlador cambiamos el método `findCourseById()` por el que acabamos de implementar:

````java

@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {
    /* other methods */
    @GetMapping(path = "/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        return this.courseService.findCourseByIdWithFullUsersDetails(id) //<-- Utilizando nuestro nuevo método
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    /* other methods */
}
````

Verificamos el resultado y vemos que ahora la propiedad `users` que en secciones iniciales siempre nos devolvía
un arreglo vacío, ahora ya viene poblado con el detalle completo de cada usuario.

Otra cosa a notar es que la propiedad `courseUsers` ya no debería mostrarse, eso podría solucionarse si trabajamos con
`DTOs`.

````bash
$ curl -v http://localhost:8002/api/v1/courses/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
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
  "users": [
    {
      "id": 2,
      "name": "Martin",
      "email": "martin@gmail.com",
      "password": "12345"
    },
    {
      "id": 5,
      "name": "Alicia",
      "email": "alicia@gmail.com",
      "password": "12345"
    }
  ]
}
````

## Des-asignar un usuario del curso al ser eliminado en el dk-ms-users

Cuando eliminemos un usuario en desde el microservicio `dk-ms-users` internamente se hará una petición al
`dk-ms-courses` para eliminar el usuario de la tabla `course_users` si está asignado.

Creamos un método personalizado para eliminar el usuario asignado de la entidad `CourseUser`:

````java
public interface ICourseRepository extends CrudRepository<Course, Long> {

    @Modifying
    @Query("""
            DELETE FROM CourseUser AS cu
            WHERE cu.userId = :userId
            """)
    void deleteCurseUserById(@Param("userId") Long userId);
}
````

En la interfaz `ICourseService` definimos el método donde llamaremos al método anterior:

````java
public interface ICourseService {
    /* other methods */
    Optional<Boolean> deleteCurseUserById(Long userId);
    /* other methods */
}
````

Ahora implementamos el servicio:

````java

@Service
public class CourseServiceImpl implements ICourseService {
    /* other methods */
    @Override
    @Transactional
    public Optional<Boolean> deleteCurseUserById(Long userId) {
        this.courseRepository.deleteCurseUserById(userId);
        return Optional.of(true);
    }
    /* other methods */
}
````

Finalmente, en la clase de controlador definimos el nuevo endpoint que solo recibirá el id del usuario que
des-asignaremos de la tabla `course_users`:

````java

@RestController
@RequestMapping(path = "/api/v1/courses")
public class CourseController {
    /* other methods */
    @DeleteMapping(path = "/unassigning-user-by-userid/{userId}")
    public ResponseEntity<Void> unassigningUserByUserId(@PathVariable Long userId) {
        return this.courseService.deleteCurseUserById(userId)
                .map(wasDeleted -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
````

---

# Sección 9: Docker Networks: Comunicación entre contenedores

---

## Dockerizando microservicio cursos y configurando la red o network

Hasta ahora este microservicio de cursos lo hemos estado trabajando sin dockerizar, así que ahora llega el momento de
hacerlo. El primer cambio que haremos será modificar el `application.yml`:

````yaml
# other properties

spring:
  # other properties

  datasource:
    url: jdbc:postgresql://host.docker.internal:5432/db_dk_ms_courses
    # other properties

logging:
  # other properties 
  file:
    name: /app/logs/dk-ms-courses.log
````

Como nuestro microservicio de cursos estará dockerizada, necesitamos que el contenedor apunte hacia nuestra máquina
local, que es donde tenemos instalada `PostgreSQL`. Para eso cambiamos el `localhost` por `host.docker.internal`.
También agregamos la ruta del login donde guardar los registros, pero eso es solo por que esté igual que el otro
microservicio.

Otra modificación que tenemos que hacer es en el archivo `IUserFeignClient`:

````java

@FeignClient(name = "dk-ms-users", url = "dk-ms-users:8001", path = "/api/v1/users")
public interface IUserFeignClient {
    // code
}
````

**DONDE**

- `name = "dk-ms-users"`, corresponde al nombre del microservicio que vamos a consumir.
- `url = "dk-ms-users:8001"`, corresponde al nombre que le daremos al contenedor cuando creemos uno con la
  bandera `--name`. El puerto seguirá siendo el mismo.

Finalmente, copiaremos el `Dockerfile` del microservicio de usuarios y le realizaremos algunos cambios para nuestro
microservicio de cursos:

````Dockerfile
FROM openjdk:17-jdk-alpine AS builder
WORKDIR /app/business-domain/dk-ms-courses
COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/dk-ms-courses/pom.xml ./
COPY ./business-domain/dk-ms-courses/mvnw ./
COPY ./business-domain/dk-ms-courses/.mvn ./.mvn
RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline
COPY ./business-domain/dk-ms-courses/src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/business-domain/dk-ms-courses/target/*.jar ./app.jar
EXPOSE 8002
CMD ["java", "-jar", "app.jar"]
````

Ahora que ya tenemos lo necesario, crearemos la imagen del microservicio de cursos:

````bash
$ docker build -t dk-ms-courses:v2 . -f .\business-domain\dk-ms-courses\Dockerfile
[+] Building 1.8s (20/20) FINISHED

$ docker image ls
REPOSITORY      TAG       IMAGE ID       CREATED         SIZE
dk-ms-courses   latest    b579ec873861   3 minutes ago   385MB
dk-ms-courses   v2        b579ec873861   3 minutes ago   385MB
dk-ms-users     latest    583a7919c097   7 minutes ago   387MB
dk-ms-users     v2        583a7919c097   7 minutes ago   387MB
````

## Dockerizando PostgreSQL

Actualmente, estoy conectando nuestros contenedores del microservicio `dk-ms-courses` hacía PostgreSQL que está
instalado en mi máquina local. Pero ahora, vamos a contenerizar `PostgreSQL` para usarlo como un contenedor dentro de
nuestra plataforma de `Docker`.

Cuando contenerizamos la base de datos de MySQL, lo primero que hicimos fue descargar la imagen con el
comando `docker pull`, pero en esta ocasión, con `PostgreSQL` crearemos directamente el contenedor. Docker al ver que
no lo tenemos descargado, nos mostrará el mensaje `Unable to find image 'postgres:14-alpine' locally` y lo empezará
a descargar por nosotros, posteriormente creará nuestro contenedor.

````bash
$ docker container run -d -p 5433:5432 --name postgres-14 --network spring-net -e POSTGRES_PASSWORD=magadiflo -e POSTGRES_DB=db_dk_ms_courses postgres:14-alpine
Unable to find image 'postgres:14-alpine' locally
14-alpine: Pulling from library/postgres
96526aa774ef: Pull complete
...
1c5e4ba76017: Pull complete
Digest: sha256:874f566dd512d79cf74f59754833e869ae76ece96716d153b0fa3e64aec88d92
Status: Downloaded newer image for postgres:14-alpine
a28d341a25c5986ae23781ec711c7e814c6989c541c868694b85d714ed1a5a8c
````

**DONDE**

- `-p 5433:5432`, el puerto externo estamos colocando en `5433`, ya que actualmente tenemos PostgreSQL en nuestra pc
  local que está corriendo en el puerto `5432`. El puerto interno lo dejamos tal cual `5432`, ya que eso trabaja al
  interno del contenedor, mientras que el externo hace referencia a nuestra máquina local.
- `--name postgres-14`, le damos un nombre al contenedor.
- `--network spring-net`, lo agregamos a la red donde están los otros dos microservicios.
- `-e (--env)`, nos permite establecer variables de entorno. Cada variable de entorno a definir, debe estar precedido
  por la bandera `-e` o `--env`. Notar que, como no estamos especificando un usuario, la imagen de PostgreSQL usará por
  defecto el usuario `postgres`.

Listando los contenedores:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                COMMAND                  CREATED         STATUS                       PORTS                               NAMES
a28d341a25c5   postgres:14-alpine   "docker-entrypoint.s…"   8 minutes ago   Up 8 minutes                 0.0.0.0:5433->5432/tcp              postgres-14
abe9d3014495   mysql:8              "docker-entrypoint.s…"   15 hours ago    Exited (255) 9 minutes ago   33060/tcp, 0.0.0.0:3307->3306/tcp   mysql-8
````

Podemos verificar si podemos conectarnos desde DBeaver instalada en nuestra pc local hacia PostgreSQL que ahora mismo
está ejecutándose en el puerto externo `5433` del contenedor `postgres-14`. El resultado debe ser una conexión exitosa.

## Comunicación entre contenedores con BBDD Dockerizadas (PostgreSQL)

En esta sección debemos modificar el `application.yml` del `dk-ms-courses` para poder comunicarnos con la base de
datos de `PostgreSQL` que ahora la tenemos contenerizada.

````yaml
# Other properties
datasource:
  url: jdbc:postgresql://postgres-14:5432/db_dk_ms_courses
# Other properties
````

El cambio realizado en la propiedad anterior fue reemplazar el `host.docker.internal` por el nombre que le dimos al
contenedor de PostgreSQL con la bandera `--name postgres-14`, de esta forma, el contenedor de nuestro microservicio de
cursos
podrá comunicarse con el contenedor de la base de datos de PostgreSQL, siempre y cuando ambos estén en la misma red. En
nuestro caso, haremos que nuestros contenedores estén en la misma red `spring-net`.

Habiendo realizado la modificación en el código fuente del microservicio de `dk-ms-courses` volvemos a generar la imagen
y
a partir de ella generamos el contenedor:

````bash
$ docker container run -d -p 8002:8002 --rm --name dk-ms-courses --network spring-net dk-ms-courses:v2
4e76998d231467c76a9d2e9b56468f83a642569d38fec8aeffd6de24960cde7e
````

Listamos los contenedores:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                COMMAND                  CREATED          STATUS          PORTS                               NAMES
4e76998d2314   dk-ms-courses:v2     "java -jar app.jar"      21 seconds ago   Up 19 seconds   0.0.0.0:8002->8002/tcp              dk-ms-courses
152fff6b17b7   dk-ms-users:v2       "java -jar app.jar"      7 minutes ago    Up 7 minutes    0.0.0.0:8001->8001/tcp              dk-ms-users
b28f9c622dc4   postgres:14-alpine   "docker-entrypoint.s…"   36 minutes ago   Up 35 minutes   0.0.0.0:5433->5432/tcp              postgres-14
c8f8710d2c2b   mysql:8              "docker-entrypoint.s…"   37 minutes ago   Up 36 minutes   33060/tcp, 0.0.0.0:3307->3306/tcp   mysql-8
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

Guardamos un curso utilizando nuestro microservicio `dk-ms-courses` y la base de datos de `PostreSQL`, ambos
dockerizados:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"Docker\"}" http://localhost:8002/api/v1/courses | jq

>
< HTTP/1.1 201
< Location: http://localhost:8002/api/v1/courses/1
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Docker",
  "courseUsers": [],
  "users": []
}
````

Creamos un nuevo usuario y lo asignamos al curso con id = 1:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"Alicia\", \"email\": \"alicia@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1 | jq

>
< HTTP/1.1 201
< Location: http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1/4
< Content-Type: application/json
<
{
  "id": 4,
  "name": "Alicia",
  "email": "alicia@gmail.com",
  "password": "12345"
}
````

Asignando un usuario existente al curso con id = 1:

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"id\": 1, \"name\": \"martin\", \"email\": \"martin@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/assign-user-to-course/1 | jq

} [77 bytes data]
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

Vemos el detalle del curso con id = 1, nos traerá la información completa de los usuarios que están en dicho curso:

````bash
$ curl -v http://localhost:8002/api/v1/courses/1 | jq

< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Docker",
  "courseUsers": [
    {
      "id": 1,
      "userId": 4
    },
    {
      "id": 2,
      "userId": 1
    }
  ],
  "users": [
    {
      "id": 1,
      "name": "martin",
      "email": "martin@gmail.com",
      "password": "12345"
    },
    {
      "id": 4,
      "name": "Alicia",
      "email": "alicia@gmail.com",
      "password": "12345"
    }
  ]
}
````

## Variables de ambiente (ENV) para los parámetros de PostgreSQL

Configuramos nuevas variables de ambiente en el `application.yml`:

````yaml
server:
  port: ${CONTAINER_PORT:8002}

spring:
  application:
    name: dk-ms-courses

  datasource:
    url: jdbc:postgresql://${DATA_BASE_HOST}:${DATA_BASE_PORT}/${DATA_BASE_NAME}
    username: ${DATA_BASE_USERNAME}
    password: ${DATA_BASE_PASSWORD}
# Other properties
````

Las anteriores variables de ambiente las tenemos que definir en el archivo `.env`, ya que las manejaremos a través de
ese archivo:

````dotenv
# Host and Container
HOST_PORT=8002
CONTAINER_PORT=8002

# Data Base
DATA_BASE_HOST=postgres-14
DATA_BASE_PORT=5432
DATA_BASE_NAME=db_dk_ms_courses
DATA_BASE_USERNAME=postgres
DATA_BASE_PASSWORD=magadiflo
````

Volvemos a construir la imagen:

````bash
$ docker build -t dk-ms-courses . -f .\business-domain\dk-ms-courses\Dockerfile
````

Ahora arrancaremos un contenedor pero especificando el archivo `.env`, ya que en ese archivo definimos las variables de
ambiente:

````bash
$ docker container run -d -p 8002:8002 --env-file .\business-domain\dk-ms-courses\.env --rm --name dk-ms-courses --network spring-net dk-ms-courses
858d409803d167b37635c32805ee78f19ac38afffc6ef1d6b63bfad1ffb5314d
````

Verificamos que esté funcionando correctamente haciendo una llamada al api del `dk-ms-courses`:

````bash
$ curl -v http://localhost:8002/api/v1/courses | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 1,
    "name": "Docker",
    "courseUsers": [
      {
        "id": 1,
        "userId": 4
      },
      {
        "id": 2,
        "userId": 1
      }
    ],
    "users": []
  },
  {
    "id": 2,
    "name": "Kubernetes",
    "courseUsers": [],
    "users": []
  },
  {
    "id": 3,
    "name": "Angular",
    "courseUsers": [],
    "users": []
  }
]
````

Verificamos que la comunicación entre ambos contenedores sigue funcionando:

````bash
$ curl -v http://localhost:8002/api/v1/courses/1 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
{
  "id": 1,
  "name": "Docker",
  "courseUsers": [
    {
      "id": 1,
      "userId": 4
    },
    {
      "id": 2,
      "userId": 1
    }
  ],
  "users": [
    {
      "id": 1,
      "name": "martin",
      "email": "martin@gmail.com",
      "password": "12345"
    },
    {
      "id": 4,
      "name": "Nophy",
      "email": "nophy@gmail.com",
      "password": "12345"
    }
  ]
}
````

## Revisando variables de ambiente DB con el comando inspect

En la sección anterior luego de haber creado las variables de entorno comprobamos que todo estaba funcionando
correctamente. En esta sección inspeccionaremos el contenedor para ver que allí podemos encontrar las variables
definidas.

````bash
$ docker container inspect dk-ms-courses
[
  {
    ...
    "Config": {
        ...
        "Env": [
            "HOST_PORT=8002",
            "CONTAINER_PORT=8002",
            "DATA_BASE_HOST=postgres-14",
            "DATA_BASE_PORT=5432",
            "DATA_BASE_NAME=db_dk_ms_courses",
            "DATA_BASE_USERNAME=postgres",
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
        "Image": "dk-ms-courses",
        "Volumes": null,
        "WorkingDir": "/app",
        ...
    },
    ...
  }
]
````

## Variables de ambiente (ENV) para los hostnames de los contenedores

En esta sección vamos a colocar en variables de ambiente el host y el puerto del microservicio `dk-ms-users` con el
que nos vamos a comunicar usando el `Feign Client`, de esa forma evitamos tenerlo hardcodeado. Entonces, en el archivo
`.env` agregamos las nuevas variables de entorno:

````dotenv
# others variables

# Communication with microservice dk-ms-users
CLIENT_USERS_HOST=dk-ms-users
CLIENT_USERS_PORT=8001
````

A continuación en el `application.yml` creamos **nuestras propiedades personalizadas** que harán uso de las
variables de entorno definidas en el `.env`:

````yaml
# Other properties

# Custom property
microservices:
  communication:
    dk-ms-users:
      url: ${CLIENT_USERS_HOST}:${CLIENT_USERS_PORT}
````

Finalmente, en la interfaz `IUserFeignClient` usamos la propiedad personalizada que definimos en el `application.yml`.
Una de las características de la anotación `@FeignClient` es que dentro de la `url` podemos usar el `spEL` para poder
acceder a la configuración del `application.yml`:

````java

@FeignClient(name = "dk-ms-users", url = "${microservices.communication.dk-ms-users.url}", path = "/api/v1/users")
public interface IUserFeignClient {
    /* code */
}
````

Una vez finalizado todos los cambios, es necesario volver a construir la imagen:

````bash
$ docker build -t dk-ms-courses . -f .\business-domain\dk-ms-courses\Dockerfile
````

Ahora, levantamos un contenedor:

````bash
$ docker container run -d -p 8002:8002 --env-file .\business-domain\dk-ms-courses\.env --rm --name dk-ms-courses --network spring-net dk-ms-courses
a533ce5ba73a03a0b8fd0487c76abfd07095cd6d0fd777e9afeae603941afd9d
````

Finalmente, teniendo en cuenta que en el `dk-ms-users` también hicimos los mismos cambios, es momento de probar la
comunicación entre ambos microservicios.

La comprobación consistirá, en que desde este microservicio crearemos al usuario `Liz` y lo asignaremos a un curso:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"Liz\", \"email\": \"liz@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1 | jq

>
< HTTP/1.1 201
< Location: http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1/6
< Content-Type: application/json
<
{
  "id": 6,
  "name": "Liz",
  "email": "liz@gmail.com",
  "password": "12345"
}
````
