# Project specification and requisites
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

Los vehículos pueden ser reconvertidos o no, o sea, llevados de un tipo de combustible a otro. La empresa solo admite reconversión de vehículos eléctricos a gasolina.

La empresa desea contar con una aplicación web mediante la cuál pueda realizar las siguientes operaciones:

•⁠  ⁠Obtener el inventario de vehículos en el taller. Poder realizar búsqueda sobre el inventario por el tipo de vehículo.
•⁠  Dar entrada y salida de vehículos, evitar duplicados con la misma matrícula y VIN
•⁠  ⁠Obtener la información de registro de los vehículos en el inventario. Esta información se codifica de la siguiente manera:
Vehículos diesel: Matrícula + tipo de bomba de inyección
Vehículos eléctricos: VIN + Voltaje + Corriente + Tipo de batería
Vehículos de gasolina: Matricula + Tipos de combustible que usa.

En caso que uno de los vehículos sea reconvertible, además de la información de registro se debe obtener los datos de reconversión: Matrícula + tipo de combustible que usará luego de reconvertido.

# Vehicle Workshop Inventory Management System

This is a Spring Boot application that provides inventory management for a vehicle workshop, with features like user authentication, vehicle tracking, and conversion history.

## Features

- **User Management**: Secure authentication and role-based access control (ADMIN, USER).
- **Vehicle Inventory**: Track different types of vehicles (Diesel, Electric, Gasoline).
- **Conversion Tracking**: Record history when electric vehicles are converted to gasoline.
- **Optimistic Locking**: Prevent concurrent updates to the same vehicle.
- **Real-time Updates**: Get live inventory changes via Server-Sent Events (SSE).
- **Audit Trail**: Track all entity changes with Hibernate Envers.

## Technology Stack

- **Backend**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Security**: Spring Security
- **ORM**: Hibernate with JPA
- **API**: RESTful endpoints
- **Real-time**: Server-Sent Events (SSE)
- **Auditing**: Hibernate Envers

## API Endpoints

### Vehicle Management

- `GET /api/vehicles` - List all vehicles
- `GET /api/vehicles/{id}` - Get a specific vehicle
- `POST /api/vehicles/diesel` - Create a diesel vehicle
- `POST /api/vehicles/electric` - Create an electric vehicle
- `POST /api/vehicles/gas` - Create a gas vehicle
- `PUT /api/vehicles/{id}` - Update a vehicle
- `DELETE /api/vehicles/{id}` - Delete a vehicle
- `GET /api/vehicles/type/{type}` - Get vehicles by type
- `POST /api/vehicles/{id}/convert-to-gas` - Convert an electric vehicle to gas
- `GET /api/vehicles/stream` - SSE endpoint for real-time updates

### User Management (Admin only)

- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get a specific user
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update a user
- `DELETE /api/users/{id}` - Delete a user

## Setup and Installation

### Prerequisites

- Java 17 or newer
- PostgreSQL database
- Maven

### Configuration

1. Clone the repository
2. Configure database connection in `application.properties`
3. Run the application:

```bash
mvn spring:boot:run
