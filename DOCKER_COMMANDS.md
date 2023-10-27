# Comandos Docker

---

## Contenedores

- **Crear un contenedor (default attach)**

Si luego de que está ejecutándose el contenedor, presionamos `Ctrl + C`, detendremos el contenedor.

````bash
$ docker container run -p 8005:8001 dk-ms-users

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

2023-10-27T17:24:34.052Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Starting DkMsUsersApplication v0.0.1-SNAPSHOT using Java 17-ea with PID 1 (/app/app.jar started by root in /app)
......
````

**DONDE:**  
`docker container run`, permite corre un nuevo contenedor.  
`-p 8005:8001`, asigna un puerto `externo:interno` al contenedor.  
`dk-ms-users`, nombre de la imagen a partir del cual se crea el contenedor.  
`¿Qué es el modo attach?`, la posesión de la terminal luego de ejecutar el comando.

- **Crear un contenedor en modo dettach (-d)**

````bash
$ docker container run -d -p 8003:8001 dk-ms-users
````

- **Volver a attachar un contenedor que está en ejecución, por su nombre o ID**

Si luego de que attachamos el contenedor, presionamos `Ctrl + C`, el contenedor se detendrá.

````bash
$ docker container attach 6bb53e81e181
2023-10-27T17:37:19.395Z  INFO 1 --- [nio-8001-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2023-10-27T17:37:19.397Z  INFO 1 --- [nio-8001-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2023-10-27T17:37:19.402Z  INFO 1 --- [nio-8001-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 4 ms
2023-10-27T17:37:19.730Z DEBUG 1 --- [nio-8001-exec-1] org.hibernate.SQL                        :
    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password
    from
        users u1_0
````

- **Listar todos los contenedores (-a): Up y Exited**

````bash
$ docker container ls -a
CONTAINER ID   IMAGE         COMMAND               CREATED          STATUS                       PORTS                    NAMES
1493e4efbe4a   dk-ms-users   "java -jar app.jar"   4 minutes ago    Up 4 minutes                 0.0.0.0:8001->8001/tcp   gifted_hypatia
b3e0fd8e029d   dk-ms-users   "java -jar app.jar"   12 minutes ago   Exited (143) 4 minutes ago                            competent_ramanujan
````

- **Detener un contenedor por su nombre o ID**

````bash
$ docker container stop 1493e4efbe4a
````

- **Reiniciar un contenedor por su nombre o ID (default dettach)**

````bash
$ docker container start 1493e4efbe4a
````

- **Reiniciar un contenedor por su nombre o ID en modo attach (-a)**

Si luego de ejecutarse el contenedor presionamos `Ctrl + C`, detendremos el contenedor.

````bash
$ docker container start ca57c372b489 -a

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

2023-10-27T17:43:34.336Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Starting DkMsUsersApplication v0.0.1-SNAPSHOT using Java 17-ea with PID 1 (/app/app.jar started by root in /app)
2023-10-27T17:43:34.344Z  INFO 1 --- [
````

- **Solo mostrar el log del contenedor por su nombre o ID**

El comando solo muestra el log, **no toma posesión del terminal:**

````bash
$ docker container logs 56cc2ac14229

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

2023-10-27T17:24:34.052Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Starting DkMsUsersApplication v0.0.1-SNAPSHOT using Java 17-ea with PID 1 (/app/app.jar started by root in /app)
2023-10-27T17:24:34.059Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : No active profile set, falling back to 1 default profile: "default"
...
2023-10-27T18:16:53.393Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8001 (http) with context path ''
2023-10-27T18:16:53.442Z  INFO 1 --- [           main] c.m.d.b.d.u.app.DkMsUsersApplication     : Started DkMsUsersApplication in 14.002 seconds (process running for 15.416
````

- **Mostrar y seguir la salida del log del contenedor por su nombre o ID (-f)**

Además de mostrar el log del contenedor, el comando siguiente le da seguimiento. Si ya no queremos darle seguimiento,
simplemente presionamos `Ctrl + C`, **eso hace que retomemos el control de la línea de comando y el contenedor seguirá
ejecutándose por debajo, no lo detiene.**

````bash
$ docker container logs 56cc2ac14229 -f

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)

2023-10-27T17:24:34.052Z  INFO 1 --- [
...
2023-10-27T18:26:24.637Z  INFO 1 --- [nio-8001-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 3 ms
2023-10-27T18:26:25.014Z DEBUG 1 --- [nio-8001-exec-1] org.hibernate.SQL                        :
    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password
    from
        users u1_0
````
