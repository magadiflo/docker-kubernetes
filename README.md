# [Guía Completa de Docker & Kubernetes con Spring Boot 2023](https://www.udemy.com/course/guia-completa-de-docker-kubernetes-con-spring-boot/)

---

## Panorama general

A continuación se muestra lo que en términos generales se hará en el curso:

![1.creando-servicios](./assets/1.creando-servicios.png)

![2.contenerizar-servicios](./assets/2.contenerizar-servicios.png)

![3.kubernetes](./assets/3.kubernetes.png)

![4.integracion-con-spring-cloud](./assets/4.integracion-con-spring-cloud.png)

---
Este proyecto contendrá todos los microservicios desarrollados en Spring Boot 3. Para eso trabajaremos
usando `Multiple Maven Modules` en `IntelliJ IDEA`.

## Creación del proyecto raíz multi-module

1. Creamos `Nuevo Proyecto Maven` desde `IntelliJ IDEA`.
2. Eliminamos el directorio `/src` creado por defecto.
3. Agregamos en el `.gitignore` el `.idea`.

### Referencias

Se usaron las siguientes referencias para crear el proyecto **multi-module con maven**:

- [Multiple Maven Modules](https://github.com/magadiflo/springboot-multiple-maven-modules.git)
- [Multi-Module Maven](https://github.com/magadiflo/my-company-project_multi-module-maven)
- [Microservices Project](https://github.com/magadiflo/microservices-project.git)

## Creación del módulo business-domain

Crearemos el módulo `business-domain` que contendrá los microservicios de usuarios y cursos, para eso debemos hacer:

1. Click derecho en el proyecto raíz (docker-kubernetes) **/ New / Module...**
2. Agregamos las siguientes configuraciones:

````
Name: business-domain
Parent: docker-kubernetes
````

Luego de crear nuestro módulo `business-domain`, en el `pom.xml` del proyecto raíz se agrega automáticamente los
siguientes tags:

````xml

<project>
    <!--Other tags-->
    <packaging>pom</packaging>

    <modules>
        <module>business-domain</module>
    </modules>

    <!--Other tags-->
</project>
````

En el código anterior podemos observar que, como nuestro proyecto raíz, es un proyecto padre que albergará módulos,
automáticamente al crear un nuevo módulo se cambia el `<packaging>` que por defecto es implícitamente un `jar` a
un `<packaging>pom</packaging>`. Además, vemos que se agrega el módulo creado entre las etiquetas `<module>`.

Ahora, si abrimos el `pom.xml` de nuestro módulo `business-domain` creado recientemente, veríamos lo siguiente:

````xml

<project>
    <!--Other configurations-->

    <parent>
        <groupId>com.magadiflo.dk</groupId>
        <artifactId>docker-kubernetes</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>business-domain</artifactId>

</project>
````

Observamos que entre las etiquetas `<parent>` estamos referenciando a nuestro módulo padre y además solo se definió un
`<artifactId>` mientras que el `<groupId>` y `<version>` las hereda de su padre.

## Creación del módulo dk-ms-users (microservicio)

Creamos el proyecto de Spring Boot en la página de [spring initializr](https://start.spring.io/) que corresponderá al
microservicio de users. Una vez descargado el proyecto, lo colocaremos dentro del módulo `business-domain` y
realizaremos configuraciones en el `pom.xml` de todos los módulos:

A continuación se muestra cómo quedaría el `pom.xml` **raíz de todo el proyecto**:

````xml

<project>
    <!--omitted properties from the project tag and modelVersion tag-->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>com.magadiflo.dk</groupId>
    <artifactId>docker-kubernetes</artifactId>
    <version>1.0-SNAPSHOT</version>

    <packaging>pom</packaging>

    <modules>
        <module>business-domain</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2022.0.4</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
````

Ahora, el `pom.xml` del módulo `business-domain` se convertirá en padre de otros módulos que agregaremos como
el `dk-ms-users` y el `dk-ms-courses` (microservicios), por lo tanto, necesitamos cambiar su `<packaging>` que
implícitamente es `jar` a un `<packaging>pom</packaging>`. Finalmente, el `pom.xml` del módulo `business-domain`
quedaría de la siguiente manera:

````xml

<project>
    <!--omitted properties from the project tag and modelVersion tag-->
    <parent>
        <groupId>com.magadiflo.dk</groupId>
        <artifactId>docker-kubernetes</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>business-domain</artifactId>

    <packaging>pom</packaging>

    <modules>
        <module>dk-ms-users</module>
    </modules>

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
    </dependencies>
</project>
````

Por último, luego de agregar el microservicio `dk-ms-users` dentro del módulo `business-domain` necesitamos configurar
su `pom.xml`:

````xml

<project>
    <!--omitted properties from the project tag and modelVersion tag-->
    <parent>
        <groupId>com.magadiflo.dk</groupId>
        <artifactId>business-domain</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <groupId>com.magadiflo.dk.business.domain</groupId>
    <artifactId>dk-ms-users</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>dk-ms-users</name>
    <description>Demo project for Spring Boot</description>

    <dependencies>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
````

**NOTA**

> Al estar trabajando con módulo de maven podemos organizar mejor las dependencias del proyecto, esto incluye el
> **poder heredar dependencias**. Por ejemplo, nuestro módulo `dk-ms-users` además de tener su propia dependencia
> definida en su `pom.xml`, tendrá todas las dependencias desde el proyecto raíz, pues tiene como
> padre `business-domain` y este a su vez tiene como padre al proyecto raíz `docker-kubernetes`.

---

# Sección 5: Cliente HTTP Feign: Comunicación entre microservicios

---

## Introducción conectando microservicios

Luego de que hayamos construido nuestros dos microservicios `dk-ms-users` y `dk-ms-courses`, ahora debemos lograr que
estos se comuniquen. Para eso utilizaremos el cliente `HTTP Feign Client`.

La imagen siguiente muestra el panorama general de lo que haremos en esta sección:

![5.relacionando-servicios](./assets/5.relacionando-servicios.png)

---

# Sección 6: Docker: Introducción

---

## Arquitectura de Docker: Imágenes y Contenedores

A continuación se muestra información gráfica del curso:

![6.contenedores-vs-maquinas-virtuales](./assets/6.contenedores-vs-maquinas-virtuales.png)

![7.imagenes-vs-contenedore](./assets/7.imagenes-vs-contenedores.png)

![8.contenedores](./assets/8.contenedores.png)

![9.imagenes-vs-contenedores](./assets/9.imagenes-vs-contenedores.png)

## Generando archivo .jar para dockerizar

Veremos de manera general cómo generar el `jar` de nuestra aplicación de spring boot y cómo ejecutarlo sin el IDE.

Tomaremos como ejemplo el microservicio `dk-ms-users` para generar el `jar` de ejemplo. Nos posicionaremos en la
terminal, en la raíz de ese microservicio y ejecutaremos el siguiente comando.

**¡Importante!** debemos tener levantado la base de datos que está usando ese microservicio, ya que cuando se construya
el jar requerirá realizar los test. Si queremos saltarnos los test podemos agregar `-DskipTests`, en mi caso sí
tengo levantado la base de datos:

````bash
$ mvnw clean package
````

**DONDE**

- `clean`, elimina todo el empaquetado que tengamos en el directorio `/target` que se haya generado en una compilación
  anterior.
- `package`, genera el `jar` dentro del directorio `/target`.

Una vez finalizado el comando anterior, veremos que en el microservicio se creó el directorio `/target` y dentro el
archivo `jar` compilado: `dk-ms-users-0.0.1-SNAPSHOT.jar`.

### Ejecutando jar desde línea de comandos

Supongamos que el `jar` generado anteriormente lo hemos llevado a una máquina remota y lo queremos levantar. Para eso
nos posicionaremos mediante el cmd en el directorio donde hayamos puesto el `jar` y ejecutaremos el comando:

````bash
$ java -jar dk-ms-users-0.0.1-SNAPSHOT.jar
````

A continuación veremos que nuestra aplicación se empieza a levantar. La idea ahora es realizar todos estos pasos pero
usando Docker, es decir `dockerizar` la aplicación, configurar un contenedor, una imagen, etc.

---

# Sección 9: Docker Networks: Comunicación entre contenedores

---

En esta sección veremos **cómo trabajar con redes dentro de docker**, veremos cómo es que los contenedores se pueden
comunicar unos con otros, cómo comunicar un contenedor con el exterior, etc.

![10.contenedores-network.png](./assets/10.contenedores-network.png)

## Dockerizando microservicio cursos y configurando la red o network

Crearemos una red al que le llamaremos `spring-net`, con la finalidad de que los contenedores se conecten a esa red
para que puedan comunicarse:

````bash
$ docker network create spring-net
b56a0e223484cd5905e3890566b0e6767dd6bb28627ea446cedf02f03980c323

$ docker network ls
NETWORK ID     NAME         DRIVER    SCOPE
1cef9871b914   bridge       bridge    local
6dac92048c81   host         host      local
4eea7e69fe4f   none         null      local
b56a0e223484   spring-net   bridge    local
````

## Comunicación entre contenedores

Hasta este punto los dos microservicios de este proyecto `dk-ms-users` y el `dk-ms-courses` cuentan con sus
respectivos `Dockerfile`, así que ahora procederemos a generar una imagen a partir del código fuente.

A continuación se listan las imágenes ya creadas de los microservicios. He creado dos versiones para cada microservicio,
con la finalidad de trabajar siempre con versiones etiquetadas y en lo preferible evitar usar el `latest` como
buena práctica.

````bash
$ docker image ls
REPOSITORY      TAG       IMAGE ID       CREATED       SIZE
dk-ms-courses   latest    b579ec873861   5 hours ago   385MB
dk-ms-courses   v2        b579ec873861   5 hours ago   385MB
dk-ms-users     latest    583a7919c097   5 hours ago   387MB
dk-ms-users     v2        583a7919c097   5 hours ago   387MB
````

Ahora, a partir de las imágenes creadas, crearemos un contenedor para cada uno de ellas:

````bash
$ docker container run -d -p 8001:8001 --rm --name dk-ms-users --network spring-net dk-ms-users:v2
c12fa66e43f3c7dcde7e92e9bfd883c74c3d1298ae0e54d3a030f1980571fcda
````

**NOTA**

- `--name dk-ms-users`, notar que aquí le estamos dando un nombre al contenedor. Este nombre es muy importante, porque
  lo utiliza el microservicio `dk-ms-courses` para poder comunicarse con él. Si vamos a la interfaz `IUserFeignClient`
  del microservicio `dk-ms-courses` veremos que lo estamos usando en la `url` así como se muestra en el siguiente
  fragmento `@FeignClient(url = "dk-ms-users:8001"...)`.
- `--network spring-net`, especificamos la red que creamos para que sea usado por el contenedor.

````bash
$ docker container run -d -p 8002:8002 --rm --name dk-ms-courses --network spring-net dk-ms-courses:v2
040dd8b4457254e7395695602be4fdf8d760c4bd04aa6e1513e0718287a3b44
````

**NOTA**

- `--name dk-ms-courses`, notar que aquí le estamos dando un nombre al contenedor. Este nombre es muy importante, porque
  lo utiliza el microservicio `dk-ms-users` para poder comunicarse con él. Si vamos a la interfaz `ICourseFeignClient`
  del microservicio `dk-ms-users` veremos que lo estamos usando en la `url` así como se muestra en el siguiente
  fragmento `@FeignClient(url = "dk-ms-users:8002"...)`.
- `--network spring-net`, especificamos la red que creamos para que sea usado por el contenedor.

Listamos los dos contenedores creados:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE              COMMAND               CREATED         STATUS         PORTS                    NAMES
040dd8b44572   dk-ms-courses:v2   "java -jar app.jar"   4 seconds ago   Up 2 seconds   0.0.0.0:8002->8002/tcp   dk-ms-courses
c12fa66e43f3   dk-ms-users:v2     "java -jar app.jar"   2 minutes ago   Up 2 minutes   0.0.0.0:8001->8001/tcp   dk-ms-users
````

Si inspeccionamos cada contenedor podemos ver que están usando la misma red que le asignamos:

````bash
$ docker container inspect dk-ms-courses
[
  {
    "Ports": {
        "8002/tcp": [
            {
                "HostIp": "0.0.0.0",
                "HostPort": "8002"
            }
        ]
    },
    "Networks": {
        "spring-net": {
            "IPAMConfig": null,
            ...
        }
    }
  }
]
````

Otra forma de saber qué contenedores tiene la red `spring-net` es ejecutando el siguiente comando de `network`:

````bash
$ docker network inspect spring-net
[
    {
        "Name": "spring-net",
        ...
        "IPAM": {
            ...
            "Config": [
                {
                    "Subnet": "172.18.0.0/16",
                    "Gateway": "172.18.0.1"
                }
            ]
        },
        ...
        "Containers": {
            "040dd8b4457254e7395695602be4fdf8d760c4bd04aa6e1513e0718287a3b445": {
                "Name": "dk-ms-courses",
                "EndpointID": "776a22e5dc5ee363a8b9ad54ea19121f573b6a4b89555bf3f90e906b333ab593",
                "MacAddress": "02:42:ac:12:00:03",
                "IPv4Address": "172.18.0.3/16",
                "IPv6Address": ""
            },
            "c12fa66e43f3c7dcde7e92e9bfd883c74c3d1298ae0e54d3a030f1980571fcda": {
                "Name": "dk-ms-users",
                "EndpointID": "4390745f4b15a05b1a8055dc95b39fb972a626327e411ce35144bf400a2d3693",
                "MacAddress": "02:42:ac:12:00:02",
                "IPv4Address": "172.18.0.2/16",
                "IPv6Address": ""
            }
        },
        ...
    }
]
````

### Funcionamiento de Contenedores

Realizamos una petición al contenedor de usuarios y vemos que nos responde exitosamente:

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

Realizamos una petición al contenedor de cursos y vemos que nos responde exitosamente:

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
]
````

### Comunicando contenedores

Ahora, verificamos si ambos contenedores se están comunicando. Si realizamos la petición al contenedor courses para ver
el detalle de un curso, éste internamente se tiene que comunicar con el microservicio de usuarios para obtener el
detalle completo de los usuarios que pertenecen al curso consultado.

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

Para hacerlo más interesante aún, vamos a crear un usuario y a continuación asignarlo al curso con `id=1`. Todo esto se
realiza mediante la comunicación entre los contenedores:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"Alicia\", \"email\": \"alicia@gmail.com\", \"password\": \"12345\"}" http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1 | jq

>
< HTTP/1.1 201
< Location: http://localhost:8002/api/v1/courses/create-user-and-assign-to-course/1/7
< Content-Type: application/json
<
{
  "id": 7,
  "name": "Alicia",
  "email": "alicia@gmail.com",
  "password": "12345"
}
````