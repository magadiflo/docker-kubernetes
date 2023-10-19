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
> muestra en la parte superior, sino más bien las dependencias que solo manejará ese microservicio, es decir
> solo serán propios de ese microservicio `(openfeign y el connector de mysql)`; las otras dependencias son comunes
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