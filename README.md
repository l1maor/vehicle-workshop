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
