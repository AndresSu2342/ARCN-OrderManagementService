# ARCN - Order Management Service (Backend)

Este repositorio corresponde al servicio de backend encargado de la gestión de pedidos (creación, consulta, actualización y control del ciclo de vida de las órdenes).
Su objetivo es proveer endpoints y lógica de negocio para la administración y seguimiento de pedidos.

---

## Integrantes

- María Paula Sánchez Macías [(hakki17)](https://github.com/hakki17)
- Cesar Andres Borray Suarez [(AndresSu2342)](https://github.com/AndresSu2342)
- Juan David Rodríguez Rodríguez [(Enigmus12)](https://github.com/Enigmus12)
- Juan David Martínez Méndez [(Fataltester)](https://github.com/Fataltester)
- Santiago Gualdrón Rincón [(Waldron63)](https://github.com/Waldron63)

## Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instaladas las siguientes herramientas:

- Java JDK 21 o superior
- Apache Maven 3.8+
- Git
- Docker

Verifica las versiones con:

```bash
java -version
mvn -version
git --version
docker --version
```

---

## Estructura del Proyecto

``` bash
.
├── Dockerfile
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/arcn/OrderManagementService
    │   │             └── OrderManagementServiceApplication.java
    │   └── resources
    │       └── application.properties
    └── test/java/arcn/OrderManagementService
                       └── OrderManagementServiceApplicationTests.java

```

---

## Clonar el repositorio

Abre una terminal en el directorio donde quieras guardar el proyecto y ejecuta:

```bash
git clone https://github.com/AndresSu2342/ARCN-OrderManagementService.git
cd ARCN-OrderManagementService
```

---

## Ejecutar con Docker

Para construir y ejecutar el servicio:

```bash
docker compose up --build
```

Si quieres ejecutarlo en segundo plano:

```bash
docker compose up --build -d
```

Luego accede a la aplicación (si el servicio expone HTTP en 8080):

http://localhost:8080/

Nota: si el servicio usa otro puerto, revisa `docker-compose.yml` o la configuración del proyecto.

---

## Ejecución local (sin Docker)

1. Compilar el proyecto:
```bash
mvn clean package
```

2. Ejecutar la aplicación en local:
```bash
mvn spring-boot:run
```

3. Acceder al servicio (si aplica):
```
http://localhost:8080/
```

---

## Pruebas

Ejecuta las pruebas unitarias con:

```bash
mvn test
```

---

## Contribuciones

1. Crea una nueva rama:
```bash
git checkout -b feat/nueva-funcionalidad
```

2. Realiza tus cambios y súbelos:
```bash
git commit -m "Agrega nueva funcionalidad"
git push origin feat/nueva-funcionalidad
```

3. Abre un Pull Request y espera la revisión de otro miembro del equipo.

---

## Construido con

* [Java (JDK 21+ recomendado)](https://www.oracle.com/java/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Apache Maven](https://maven.apache.org/)
* [Docker](https://www.docker.com/)
* [Docker Compose](https://docs.docker.com/compose/)
* [Git](https://git-scm.com/)
