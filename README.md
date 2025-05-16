# Vehicle Workshop Management System

## Technology Stack
- Java 17
- Spring Boot
- PostgreSQL
- React frontend

## Running the Application
```bash
docker compose up
```

## Accessing the Application
The web interface is available at http://localhost:8080

## Authentication
Default credentials:
| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin    | Admin |
| user     | user     | User  |

# Project specification and requirements
A workshop company records each incoming vehicle's license plate and VIN number. It accepts diesel, gasoline and electric vehicles. For diesel vehicles, it also records the type of injection pump used, which can be linear or rotary. For electric vehicles, it records the type of battery (GEL or LITHIUM) and the battery voltage and current. In the case of gasoline vehicles, the type of fuel used is recorded, which may be B83, B90, B94 or B100, or a combination of these.

The vehicles can be converted or not, that is, converted from one type of fuel to another. The company only supports the conversion of electric vehicles to gasoline.

The company would like to have a web application through which it can perform the following operations:

- Obtain the inventory of vehicles in the workshop. To be able to search the inventory by vehicle type.
- Check in and check out vehicles, avoid duplicates with the same license plate and VIN.
- Obtain the registration information of the vehicles in the inventory. This information is coded as follows:
   Diesel vehicles: License plate + type of injection pump.
   Electric vehicles: VIN + Voltage + Current + Battery Type
   Gasoline vehicles: License plate + Type of fuel used.

In case one of the vehicles is reconvertible, in addition to the registration information, you must obtain the reconversion data: License plate + type of fuel to be used by the vehicle after conversion.

# Requisitos del proyecto (es)

Una empresa de talleres registra de cada vehículo que entra su matrícula y el número de identificación (VIN). Admite vehículos de diesel, gasolina y eléctricos. De los vehículos de diesel registra además el tipo de bomba de inyección que usa, la cual puede ser lineal o rotatoria. De los eléctricos registra el tipo de batería (GEL o LITIO) y el voltaje y la corriente de la batería. En el caso de los de gasolina se registra el tipo de combustible que usa, que puede ser B83, B90, B94 o B100, o una combinación de estos.

Los vehículos pueden ser reconvertibles o no, es decir, reconvertidos de un tipo de combustible a otro. La empresa solamente soporta la reconversión de vehículos eléctricos a gasolina.

A la empresa le gustaría tener una aplicación web a través de la cual pueda realizar las siguientes operaciones:

- Obtener el inventario de vehículos en taller. Poder buscar el inventario por tipo de vehículo.
- Registrar la entrada y salida de vehículos, evitar duplicados con la misma matrícula y VIN.
- Obtener la información de matrícula de los vehículos en inventario. Esta información se codifica del siguiente modo:
   Vehículos diésel: Matrícula + tipo de bomba de inyección.
   Vehículos eléctricos: VIN + Voltaje + Corriente + Tipo de Batería
   Vehículos gasolina: Matrícula + Tipo de combustible que usa.

En caso de que alguno de los vehículos sea reconvertible, además de la información de matrícula se debe obtener los datos de reconversión: Matrícula + tipo de combustible que usará el vehículo tras la reconversión.

# Dockerization

The project has been containerized for easy development and deployment.

## Docker Setup

The containerization process follows modern best practices:

1. Multi-stage build process to optimize final image size
2. Integration of React frontend and Spring Boot backend in a single container
3. PostgreSQL database container with data persistence
4. Environment variable configuration
5. Proper dependency caching for faster builds

## How to Run with Docker (Production)

1. Ensure Docker and Docker Compose are installed on your system
2. Build and run the application using Docker Compose with BuildKit enabled:

```bash
DOCKER_BUILDKIT=1 docker compose up -d
```

This will:
- Start a PostgreSQL database container with persistent storage
- Build the Spring Boot application with the React frontend
- Serve the application on port 8080 (configurable in docker-compose.yml)
- Cache both pnpm and Maven dependencies for faster subsequent builds

3. To stop the application:

```bash
docker compose down
```

4. For a clean rebuild of the application (ignoring caches):

```bash
DOCKER_BUILDKIT=1 docker compose build --no-cache
DOCKER_BUILDKIT=1 docker compose up -d
```

5. For incremental builds (using dependency caching):

```bash
DOCKER_BUILDKIT=1 docker compose build
DOCKER_BUILDKIT=1 docker compose up -d
```

### Dependency Caching

The Docker build has been optimized with BuildKit cache mounts for both package managers:

- **Frontend (pnpm)**: Dependencies are cached in a persistent volume at `/pnpm-store`
- **Backend (Maven)**: Dependencies are cached in the host's Maven repository through a BuildKit cache mount

This significantly speeds up builds when only source code changes, not dependencies.

### Docker Architecture

The application uses a multi-stage Docker build process to optimize the final image size and performance:

1. **Frontend Build Stage**: 
   - Uses Node.js 18 with Alpine
   - Builds the React application with pnpm
   - Outputs optimized static assets

2. **Backend Build Stage**:
   - Uses Maven with Eclipse Temurin JDK 17
   - Compiles the Spring Boot application
   - Embeds the frontend build in the Spring static resources directory

3. **Runtime Stage**:
   - Uses a lightweight JRE 17 Alpine image
   - Contains only the necessary runtime components
   - Runs as a non-root user for improved security

The application in Docker exposes the frontend directly from the Spring Boot static resources, eliminating the need for a separate frontend server in production.