# [Guía Completa de Docker & Kubernetes con Spring Boot 2023](https://www.udemy.com/course/guia-completa-de-docker-kubernetes-con-spring-boot/)

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

Como nuestro proyecto raíz, es un proyecto padre que albergará módulos, automáticamente al crear un nuevo módulo se
cambia el `<packaging>` que por defecto es implícitamente un `jar` a un `<packaging>pom</packaging>`. Además, vemos
que se agrega el módulo creado entre las etiquetas `<module>`.

El `pom.xml` de nuestro módulo `business-domain` quedaría de esta manera:

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

