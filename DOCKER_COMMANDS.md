# Comandos Docker

---

## Contenedores

---

### Crear un contenedor (default attach)

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

### Crear un contenedor en modo dettach (-d)

````bash
$ docker container run -d -p 8003:8001 dk-ms-users
````

### Attachar un contenedor que está en ejecución

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

**DONDE**  
`6bb53e81e181`, corresponde al ID del contenedor. También se puede utilizar el nombre del contenedor en lugar el ID.

### Listar todos los contenedores (-a)

````bash
$ docker container ls -a
CONTAINER ID   IMAGE         COMMAND               CREATED          STATUS                       PORTS                    NAMES
1493e4efbe4a   dk-ms-users   "java -jar app.jar"   4 minutes ago    Up 4 minutes                 0.0.0.0:8001->8001/tcp   gifted_hypatia
b3e0fd8e029d   dk-ms-users   "java -jar app.jar"   12 minutes ago   Exited (143) 4 minutes ago                            competent_ramanujan
````

### Detener un contenedor

````bash
$ docker container stop 1493e4efbe4a
````

### Reiniciar un contenedor (default dettach)

````bash
$ docker container start 1493e4efbe4a
````

### Reiniciar un contenedor en modo attach (-a)

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

### Mostrar solo el log del contenedor

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

### Mostrar y seguir la salida del log del contenedor (-f)

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

### Eliminar un contenedor que está detenido

````bash
$ docker container rm b6d46323c2d0
````

### Forzar la eliminación un contenedor que está siendo ejecutado

````bash
$ docker container rm -f ca57c372b489
````

### Eliminar todos los contenedores que están detenidos (Exited), los que están levantados (Up) no los toca

````bash
$ docker container prune
WARNING! This will remove all stopped containers.
Are you sure you want to continue? [y/N] y
Deleted Containers:
56cc2ac14229caeee5f41bd46d20d5087db15321ddd567984bfdfae9d01ea89d
6bb53e81e181d4660d9a23eeca752668d007d95d4b62e809149ecc2f7bb4bb66

Total reclaimed space: 0B
````

### Eliminar automáticamente el contenedor cuando se detiene (--rm)

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

### Ingresar en modo interactivo en contenedores en ejecución (exec -it)

En algún momento **podemos requerir ingresar dentro de algún contenedor que se está ejecutando**, para eso podemos usar
el siguiente comando:

````bash
$ docker exec -it b4a898eb2f19 /bin/sh
/app # ls
app.jar
/app # cd ..
/ # ls
app    bin    dev    etc    home   lib    media  mnt    opt    proc   root   run    sbin   srv    sys    tmp    usr    var
/ # exit
````

**DONDE**  
`exec`, ejecuta un comando en un **contenedor Docker en ejecución.**  
`-it`, interactive terminal, nos permiten interactuar directamente con el shell del contenedor.  
`/bin/sh`, es el comando que se ejecutará en el contenedor. En este caso, se está ejecutando un shell interactivo (por
lo general, sh, que es el shell Bourne). Esto te permite acceder al entorno del contenedor y ejecutar comandos dentro de
él.

Como se observa en el resultado anterior, pudimos ingresar dentro del contenedor en ejecución y lo primero que hice fue
listar el contenido del directorio actual `ls` y ¡Oh, sorpresa!, está el `app.jar` que compilamos al construir la
imagen.

### Ingresar en modo interactivo al iniciar la creación de un nuevo contenedor (-it)

En este caso vamos a crear un nuevo contenedor, pero vamos a ingresar directamente a él para inspeccionar su contenido y
cuando salgamos de él con el comando `exit`, el contenedor se borrará automáticamente:

````bash
$ docker container run -p 8001:8001 --rm -it dk-ms-users /bin/sh
/app # ls
app.jar
/app # exit

$ docker container list -a
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
````

Como observamos en la salida anterior, vemos que **creamos un contenedor nuevo** pero le agregamos los comandos `--rm`
para que cuando hagamos `exit` en la terminal dentro del contenedor, éste se elimine automáticamente. También usamos
el comando `-it` para utilizar el `terminal interactivo` del contenedor y finalmente le agregamos la instrucción
`/bin/sh`.

### Copiando archivos hacia/desde el contenedor en ejecución

Podemos copiar archivos que están en nuestra pc local hacia dentro de un **contenedor que está en ejecución** y también
en el otro sentido, es decir, copiar archivos que están dentro del contenedor en ejecución hacia nuestra pc local.

Veamos cómo podemos **copiar un archivo de nuestra pc local hacia dentro del contenedor**. Primero crearemos un
contenedor como en la sección anterior:

````bash
$ docker container run -p 8001:8001 --rm -it dk-ms-users /bin/sh
/app # ls
app.jar
````

Como observamos, tenemos en el `WORKDIR /app` nuestro empaquetado `jar`. A continuación abriremos otra terminal para ver
los detalles del contenedor creado:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE         COMMAND     CREATED              STATUS              PORTS                    NAMES
b03d590b8ce9   dk-ms-users   "/bin/sh"   About a minute ago   Up About a minute   0.0.0.0:8001->8001/tcp   keen_curran
````

Ahora, nos posicionaremos con la terminal en el directorio donde está el archivo que queremos copiar. En nuestro caso
será un archivo `Login.java`:

````bash
M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\INTELLIJ_IDEA\01.udemy\02.udemy_andres_guzman\03.docker_y_kubernetes_2023
$ ls
antiguo/  docker-kubernetes/  Login.java
````

Listo, ahora usamos el comando `cp` para copiar dicho archivo:

````bash
$ docker container cp .\Login.java b03d590b8ce9:/app/Login.java
Successfully copied 2.56kB to b03d590b8ce9:/app/Login.java
````

**DONDE**  
`cp`, copia archivos/carpetas entre un contenedor y el sistema de archivos local.  
`.\Login.java b03d590b8ce9:/app/Login.java`, el archivo `Login.java` que será copiado en el contenedor con ID
`b03d590b8ce9` y dentro del contenedor se copiará en el `WORKDIR /app`.

Verificamos que el archivo `Login.java` esté en el `WORKDIR /app` y lo ejecutamos para ver que funciona:

````bash
$ docker container run -p 8001:8001 --rm -it dk-ms-users /bin/sh
/app # ls
app.jar
/app # ls
Login.java  app.jar
/app # cat Login.java
import java.util.Scanner;

public class Login {
    public static void main(String[] args) {

        String[] usernames = {"andres", "admin", "pepe"};
        String[] passwords = {"123", "1234", "12345"};

        Scanner scanner = new Scanner(System.in);

        System.out.println("Ingrese el username");
        String u = scanner.next();

        System.out.println("Ingrese el password");
        String p = scanner.next();

        boolean esAutenticado = false;

        for(int i = 0; i < usernames.length; i++){
            esAutenticado = (usernames[i].equals(u) && passwords[i].equals(p))? true: esAutenticado;
        }

        String mensaje = esAutenticado ? "Bienvenido usuario ".concat(u).concat("!") :
                "Username o contraseña incorrecto!\nLo sentimos, requiere autenticación";
        System.out.println("mensaje = " + mensaje);

    }
}
/app # javac Login.java
/app # ls
Login.class  Login.java   app.jar
/app # java Login
Ingrese el username
admin
Ingrese el password
1234
mensaje = Bienvenido usuario admin!
/app #
````

Como observamos en el comando anterior, vemos que estamos interactuando con nuestro archivo dentro del contenedor en
ejecución.

Ahora, **copiaremos algún archivo que está dentro del contenedor hacia nuestra pc local**:

````bash
$ docker container cp b03d590b8ce9:/app/Login.java LoginCopy.java
Successfully copied 2.56kB to M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\INTELLIJ_IDEA\01.udemy\02.udemy_andres_guzman\03.docker_y_kubernetes_2023\LoginCopy.java
````

**DONDE**  
`b03d590b8ce9`, ID del contenedor de donde copiaremos.  
`/app/Login.java`, archivo que copiaremos del contenedor hacia nuestra pc local. También se puede copiar directorios.  
`LoginCopy.java`, archivo de destino, lo renombramos a `LoginCopy.java`

Al verificar si se realizó la copia, vemos que sí, en nuestra pc local ya tenemos el archivo `LoginCopy.java`:

````bash
M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\INTELLIJ_IDEA\01.udemy\02.udemy_andres_guzman\03.docker_y_kubernetes_2023
$ ls
antiguo/  docker-kubernetes/  Login.java  LoginCopy.java
````

### Copiando archivos logs de spring desde el contenedor

Recordar que en el `application.yml` configuramos la ruta y el nombre del archivo log a generar y luego en el
`Dockerfile` agregamos el comando `RUN mkdir ./logs` para crear el directorio donde almacenaremos el archivo log.

Entonces, para copiar un archivo log de spring desde el contenedor, lo primero que haremos será construir la imagen,
puesto que hicimos modificaciones en el `dk-ms-users`:

````bash
$ docker build -t dk-ms-users . -f .\business-domain\dk-ms-users\Dockerfile

$ docker image ls
REPOSITORY    TAG       IMAGE ID       CREATED             SIZE
dk-ms-users   latest    8048af278d64   16 seconds ago      387MB
````

Levantamos un contenedor en modo dettached y con autoeliminación:

````bash
$ docker container run -d -p 8001:8001 --rm dk-ms-users
8a3c5fb0663918ff1be3c392315b58e42da200ba1fa5ff7e834505b49900362c
````

Verificamos en consola el log generado por nuestra aplicación dentro del contenedor:

````bash
$ docker container logs 8a3c5fb06639

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.4)
...
...
2023-10-30T15:35:57.085Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8001 (http)
...
2023-10-30T15:36:05.319Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8001 (http) with context path ''
...
````

Ahora, debemos verificar que ese mismo log generado en consola debe estar guardado dentro del contenedor en la ruta que
especificamos en el `application.yml`. Para eso usamos el comando `exec -it`:

````bash
$ docker exec -it 8a3c5fb06639 /bin/sh
/app # ls
app.jar  logs
/app # cd logs
/app/logs # ls
dk-ms-users.log
/app/logs # cat dk-ms-users.log
...
2023-10-30T15:35:57.085Z  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8001 (http)
...
2023-10-30T15:36:05.319Z  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8001 (http) with context path ''
2023-10-30T15:36:05.352Z  INFO 1 --- [main] c.m.d.b.d.u.app.DkMsUsersApplication     : Started DkMsUsersApplication in 14.24 seconds (process running for 15.611)
/app/logs #
````

Podemos copiar el log generado en el contenedor hacia nuestra máquina local usando el comando `cp`. En nuestro caso
lo copiaremos en la ruta en la que actualmente nos encontramos posicionados en la línea de comandos. Por defecto, no
tenemos creado el directorio `./logs` en nuestra máquina local, `docker` lo creará por nosotros para que haga la copia.

````bash
$ docker container cp 8a3c5fb06639:/app/logs ./logs
Successfully copied 6.14kB to M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\INTELLIJ_IDEA\01.udemy\02.udemy_andres_guzman\03.docker_y_kubernetes_2023\docker-kubernetes\logs
````

Ahora, revisamos el contenido del archivo log copiado en nuestra máquina local:

````bash
$ ls
dk-ms-users.log

$ cat dk-ms-users.log
...
2023-10-30T15:35:57.085Z  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8001 (http)
...
2023-10-30T15:36:05.319Z  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8001 (http) with context path ''
...
````

---

# Imágenes

---

### Listar las imágenes

````bash
$ docker image ls
REPOSITORY    TAG       IMAGE ID       CREATED       SIZE
dk-ms-users   latest    db7d7d6737b1   6 hours ago   387MB
dk-ms-users   test      db7d7d6737b1   6 hours ago   387MB
````

### Eliminar una imagen con tag por defecto

La instrucción siguiente eliminó la imagen `dk-ms-users`. Como no especificamos la etiqueta, por defecto elimina el
`(TAG) latest` que es el tag por defecto. Si la imagen tiene contenedores ejecutándose mostrará mensajes de error y no
podrá eliminarse.

````bash
$ docker image rm dk-ms-users
Untagged: dk-ms-users:latest
Deleted: sha256:db7d7d6737b1cd72856f5d250b0b4da95e9a7d98e00b145c1aacecc8755b1bf0
````

### Eliminar una imagen especificando su tag

La instrucción siguiente eliminó la imagen `dk-ms-users` con `TAG test`. Si la imagen tiene contenedores ejecutándose
mostrará mensajes de error y no podrá eliminarse.

````bash
$ docker image rm dk-ms-users:test
Untagged: dk-ms-users:test
````

### Eliminar imágenes no utilizadas `<none>:<none>`

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
