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

![courses-table](./assets/courses-table.png)

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
