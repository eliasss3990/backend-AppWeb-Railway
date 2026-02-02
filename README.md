# Cartones App - Backend API

Sistema de gestión para vendedores y cartones de bingo. Este repositorio contiene el **Backend** desarrollado en Java con Spring Boot, desplegado en la nube.

## Tecnologías

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.3.1
* **Base de Datos:** PostgreSQL (Railway)
* **Infraestructura:** Railway (Backend + DB)
* **Frontend:** Next.js (Desplegado en Vercel)

---

## ⚙Configuración del Entorno (Variables)

Para que la aplicación funcione correctamente (tanto en local como en Railway), es necesario configurar las siguientes variables de entorno.

### En Producción (Railway)
Estas variables se configuran en la pestaña *Variables* del servicio en Railway.

| Variable | Valor / Ejemplo | Descripción                                     |
| :--- | :--- |:------------------------------------------------|
| `PORT` | `9001` | Puerto expuesto por el contenedor.              |
| `SERVER_PORT` | `9001` | Puerto donde escucha Spring Boot.               |
| `SPRING_PROFILES_ACTIVE`| `prod` | Activa la configuración de producción.          |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://host:port/db` | **Importante:** Debe incluir `jdbc:` al inicio. |
| `SPRING_DATASOURCE_USERNAME`| `postgres` | Usuario de la BD.                               |
| `SPRING_DATASOURCE_PASSWORD`| `*****` | Contraseña de la BD.                            |
| `APP_CORS_ORIGINS` | `https://rgq-web.vercel.app` | URL del Frontend                                |
| `APP_DDL_AUTO` | `update` | Gestión automática del esquema de la BD.        |

---

## Instalación y Ejecución Local

Si deseas correr el proyecto en tu máquina local:

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/eliasss3990/backend-AppWeb-Railway.git
    cd backend-AppWeb-Railway
    ```

2.  **Configurar Variables (.env o IDE):**
    Renombra el archivo `.env.example` a `.env` con el siguiente comando.
    ```bash
    cp .env.example .env
    ```
    Luego, edítalo para ajustar las variables para el entorno de desarrollo.

3.  **Configurar los secretos:**
    Ejecuta estos comandos y luego edita los archivos por un user y una password segura.
    ```bash
    cp secrets_store/db_user.txt.example secrets_store/db_user.txt && 
    cp secrets_store/db_password.txt.example secrets/db_password.txt
    ```

3.  **Levantar los servicios:**
    ```bash
    docker compose up -d --build
    ```

---

## Despliegue (CI/CD)

El proyecto cuenta con integración continua.

1.  Al hacer un **Push** a la rama `master` en GitHub.
2.  **Railway** detecta el cambio automáticamente.
3.  Se compila el proyecto, se generan las imágenes de Docker y se despliega la nueva versión sin tiempo de inactividad.

---

## Endpoints Principales

Todos los endpoints están prefijados con `/api`.

### Vendedores

*   `POST /api/vendedores/carga` - Carga vendedores desde un archivo Excel.
*   `GET /api/vendedores/{procesoId}` - Lista los vendedores válidos para un `procesoId` específico.
*   `DELETE /api/vendedores` - Elimina todos los vendedores.

### Distribuciones (PDFs)

*   `POST /api/distribuciones/{procesoId}/simular` - Simula la distribución de cartones para un `procesoId` dado.
*   `GET /api/distribuciones/{procesoId}/pdfs` - Descarga los PDFs generados para un `procesoId` como un archivo ZIP.

---

**Autor:** Elías González