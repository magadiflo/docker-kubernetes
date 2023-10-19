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

