# Comandos Docker

---

## Contenedores

---

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
$ docker container start -a ca57c372b489

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
$ docker container logs -f 56cc2ac14229

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

- **Eliminar un contenedor que está detenido**

````bash
$ docker container rm b6d46323c2d0
````

- **Forzar la eliminación un contenedor que está siendo ejecutado**

````bash
$ docker container rm -f ca57c372b489
````

- **Elimina todos los contenedores que están detenidos (Exited), los que están levantados (Up) no los toca**

````bash
$ docker container prune
WARNING! This will remove all stopped containers.
Are you sure you want to continue? [y/N] y
Deleted Containers:
56cc2ac14229caeee5f41bd46d20d5087db15321ddd567984bfdfae9d01ea89d
6bb53e81e181d4660d9a23eeca752668d007d95d4b62e809149ecc2f7bb4bb66

Total reclaimed space: 0B
````

- **Elimina automáticamente el contenedor cuando se detiene (--rm)**

Para eliminar automáticamente un contenedor cuando hagamos un `stop` o cuando ejecutemos algún comando que haga que el
contenedor se detenga, debemos crear dicho contenedor agregando la bandera `--rm`:

````bash
$ docker container run -d -p 8001:8001 --rm dk-ms-users
13580ad5259133bd21eacc9e2f4c7768abdc8990adc89c70b681dd9469f7ed68
````

**DONDE**  
`-d`, indica que el contenedor se creará en modo `dettached`, es decir, desacoplado de la línea de comando.  
`--rm`, nos permite eliminar automáticamente el contenedor cuando se detenga.

En el comando anterior hemos creado un contenedor al que le agregamos la bandera `--rm`. Ahora, listemos los
contenedores para ver el que acabamos de crear.

````bash
$ docker container ls -a
CONTAINER ID   IMAGE         COMMAND               CREATED          STATUS          PORTS                    NAMES
13580ad52591   dk-ms-users   "java -jar app.jar"   11 seconds ago   Up 10 seconds   0.0.0.0:8001->8001/tcp   romantic_greider
````

Ahora, detengamos el contenedor ejecutando el siguiente comando:

````bash
$ docker container stop 13580ad52591
13580ad52591
````

Finalmente, si volvemos a listar los contenedores, veremos que el contenedor que detuvimos
**se eliminó automáticamente:**

````bash
$ docker container ls -a
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
````

---

# Imágenes

---

- **Listar las imágenes**

````bash
$ docker image ls
REPOSITORY    TAG       IMAGE ID       CREATED       SIZE
dk-ms-users   latest    db7d7d6737b1   6 hours ago   387MB
dk-ms-users   test      db7d7d6737b1   6 hours ago   387MB
````

- **Eliminar una imagen con tag por defecto**

La instrucción siguiente eliminó la imagen `dk-ms-users`. Como no especificamos la etiqueta, por defecto elimina el
`(TAG) latest` que es el tag por defecto. Si la imagen tiene contenedores ejecutándose mostrará mensajes de error y no
podrá eliminarse.

````bash
$ docker image rm dk-ms-users
Untagged: dk-ms-users:latest
Deleted: sha256:db7d7d6737b1cd72856f5d250b0b4da95e9a7d98e00b145c1aacecc8755b1bf0
````

- **Eliminar una imagen especificando su tag**

La instrucción siguiente eliminó la imagen `dk-ms-users` con `TAG test`. Si la imagen tiene contenedores ejecutándose
mostrará mensajes de error y no podrá eliminarse.

````bash
$ docker image rm dk-ms-users:test
Untagged: dk-ms-users:test
````

- **Eliminar imágenes no utilizadas `<none>:<none>`**

````bash
$ docker image ls
REPOSITORY    TAG           IMAGE ID       CREATED         SIZE
<none>        <none>        3ad3b56a2b31   3 minutes ago   387MB
dk-ms-users   development   db7d7d6737b1   7 hours ago     387MB
dk-ms-users   latest        db7d7d6737b1   7 hours ago     387MB

$ docker image prune
WARNING! This will remove all dangling images.
Are you sure you want to continue? [y/N] y
Deleted Images:
deleted: sha256:3ad3b56a2b315ce667740a41fa92a65d1902b30269a2484b22b64a1ddb31b98e

Total reclaimed space: 0B

$ docker image ls
REPOSITORY    TAG           IMAGE ID       CREATED       SIZE
dk-ms-users   development   db7d7d6737b1   7 hours ago   387MB
dk-ms-users   latest        db7d7d6737b1   7 hours ago   387MB
````
