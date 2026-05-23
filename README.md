# SALMA ACCESORIOS

Proyecto final universitario de Ingenieria de Software.
SALMA ACCESORIOS es un e-commerce de accesorios artesanales como collares, pulseras, anillos y llaveros.

## Tecnologias

- Java 17
- Spring Boot
- Spring Security
- JWT
- JPA/Hibernate
- PostgreSQL
- Thymeleaf
- HTML, CSS, Bootstrap y JavaScript
- Lombok

## Estructura del Proyecto

Esta estructura sigue la convencion profesional de Maven y Spring Boot:

```text
salma_accesorios/
|-- database/
|   `-- schema.sql
|-- src/
|   |-- main/
|   |   |-- java/com/salma/salma_accesorios/
|   |   |   |-- config/
|   |   |   |-- controller/
|   |   |   |-- dto/
|   |   |   |-- model/
|   |   |   |-- repository/
|   |   |   |-- security/
|   |   |   `-- service/
|   |   `-- resources/
|   |       |-- static/
|   |       |   |-- css/
|   |       |   |-- img/
|   |       |   `-- js/
|   |       |-- templates/
|   |       `-- application.properties
|   `-- test/
|-- uploads/
|   |-- products/
|   `-- reviews/
|-- .gitignore
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
`-- README.md
```

No se recomienda meter todo dentro de otra carpeta llamada `proyecto`, porque GitHub ya muestra el repositorio como carpeta principal. Ademas, Maven espera encontrar `pom.xml` en la raiz.

## Base de Datos

La base de datos se llama:

```text
salma_accesorios
```

El script completo esta en:

```text
database/schema.sql
```

En pgAdmin:

1. Crear o seleccionar la base `salma_accesorios`.
2. Abrir Query Tool sobre esa base.
3. Ejecutar el contenido de `database/schema.sql`.

## Usuarios de Prueba

Estos usuarios se crean automaticamente al iniciar la aplicacion si no existen.

| Rol | Correo | Contrasena | Usuario |
| --- | --- | --- | --- |
| ADMIN | admin@salma.com | Admin123* | admin |
| CLIENTE | cliente1@salma.com | Cliente123* | cliente1 |
| CLIENTE | cliente2@salma.com | Compra456* | cliente2 |
| CLIENTE | laura.mendez@example.com | Laura2026* | lauramendez |
| CLIENTE | natalia.giraldo@example.com | Natalia789* | natalia.g |
| CLIENTE | sofia.ramirez@example.com | Sofia321* | sofiaramirez |

## Ejecucion Local

Desde la raiz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

Luego abrir:

```text
http://localhost:8080
```

## Imagenes

Las imagenes se suben desde archivos del computador.

La aplicacion guarda los archivos en:

```text
uploads/products
uploads/reviews
```

En PostgreSQL se guarda la ruta publica, por ejemplo:

```text
/uploads/products/imagen-generada.jpg
```

Las imagenes subidas por pruebas no deben subirse al repositorio. Por eso la carpeta `uploads` queda preparada, pero sus archivos se ignoran con `.gitignore`.

## Reglas Importantes

- El inicio es publico y no muestra informacion de roles.
- El panel administrador solo se muestra despues de iniciar sesion como ADMIN.
- El cliente puede usar catalogo, carrito, favoritos y pedidos.
- El carrito permite seleccionar solo algunos productos para comprar.
- Las resenas solo se pueden crear si el usuario compro el producto.
- Los precios se muestran en pesos colombianos.

## Pruebas

```powershell
.\mvnw.cmd test
```

## Notas para GitHub

Si se debe subir:

- `src/`
- `database/`
- `.mvn/`
- `mvnw`
- `mvnw.cmd`
- `pom.xml`
- `.gitignore`
- `README.md`

No se debe subir:

- `target/`
- `.idea/`
- archivos reales dentro de `uploads/products/`
- archivos reales dentro de `uploads/reviews/`
