# Comandos Docker

---

## Contenedores

- **Crear un contenedor**

````bash
$ docker container run -p 8001:8001 dk-ms-users
````

**DONDE:**  
`docker container run`, corre un nuevo contenedor.  
`-p 8001:8001`, asigna un puerto `externo:interno` al contenedor.  
`dk-ms-users`, nombre de la imagen a partir del cual se crea el contenedor.

- **Listar contenedores**

````bash
$ docker container ls -a
CONTAINER ID   IMAGE         COMMAND               CREATED          STATUS                       PORTS                    NAMES
1493e4efbe4a   dk-ms-users   "java -jar app.jar"   4 minutes ago    Up 4 minutes                 0.0.0.0:8001->8001/tcp   gifted_hypatia
b3e0fd8e029d   dk-ms-users   "java -jar app.jar"   12 minutes ago   Exited (143) 4 minutes ago                            competent_ramanujan
````

**DONDE**  
`-a` (--all), muestra todos los contenedores, aquellos que se están ejecutando y aquellos que están detenidos **(por
defecto sólo muestra los que se están ejecutando)**

- **Detener un contenedor**

````bash
$ docker container stop 1493e4efbe4a
````

**DONDE**  
`1493e4efbe4a`, nombre o ID del contenedor a detener.

- **Reiniciar un contenedor**

````bash
$ docker container start 1493e4efbe4a
````

**DONDE**  
`1493e4efbe4a`, nombre o ID del contenedor a iniciar (actualmente está detenido).
