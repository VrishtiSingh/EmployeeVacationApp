# Vacation API

A RESTful API for managing employee vacation requests, including approval workflow, overlapping checks, and manager overviews.

---

## Overview

The API allows employees to create vacation requests, check for overlapping requests, and view their request history. Managers can approve or reject requests and view detailed employee vacation summaries.

### Flow Diagram (Mermaid)

```mermaid
flowchart LR
    E[Employee] -->|Create Vacation Request POST /requests| S[Vacation API]
    E -->|Check Employee Requests GET /requests/employee/{id}| S
    E -->|Check Overlaps Boolean GET /api/employee/{id}/overlaps| S
    E -->|Get Overlapping Requests GET /api/employee/{id}/overlaps/list| S
    M[Manager] -->|Process Vacation Request PUT /requests/{id}/process| S
    M -->|Employee Vacation Overview GET /api/employee/{id}/overview| S
````

---

## Features

* Create, update, and retrieve vacation requests.
* Check for overlapping vacation periods.
* Manager endpoints for approving/rejecting requests.
* Employee overview with filtering by status.
* Swagger/OpenAPI documentation.

---

## Setup

### Requirements

* Java 17+
* Maven
* H2 Database (or your preferred DB)

### Run Locally

1. Clone the repository:

   ```bash
   git clone <repo-url>
   cd vacation-api
   ```
2. Build and run:

   ```bash
   mvn spring-boot:run
   ```
3. Swagger UI is available at:

   ```
   http://localhost:8082/swagger-ui/index.html
   ```

---

## Endpoints

### Employee Endpoints

| Method | Endpoint                                      | Description                                                                                |
| ------ | --------------------------------------------- | ------------------------------------------------------------------------------------------ |
| POST   | `/requests`                                   | Create a new vacation request                                                              |
| GET    | `/requests/employee/{id}`                     | List all requests for an employee                                                          |
| GET    | `/api/employee/{id}/overlaps`                 | Check if a given period overlaps with existing requests (boolean)                          |
| GET    | `/api/employee/{id}/overlaps/list`            | Get full list of overlapping requests                                                      |
| GET    | `/api/employee/{id}/overview?status={status}` | Get employee vacation overview, optional status filter (`PENDING`, `APPROVED`, `REJECTED`) |

### Manager Endpoints

| Method | Endpoint                                      | Description                                 |
| ------ | --------------------------------------------- | ------------------------------------------- |
| PUT    | `/requests/{id}/process`                      | Approve or reject a vacation request        |
| GET    | `/api/employee/{id}/overview?status={status}` | Retrieve detailed employee vacation summary |

---

## Testing

* Unit tests: `VacationServiceTest`, `VacationServiceEdgeCaseTest`
* Controller tests: `VacationRequestControllerTest`
* Run all tests using Maven:

  ```bash
  mvn test
  ```

---

## Notes

* Ensure the database is properly seeded for overlap checks.
* Swagger annotations provide interactive documentation via Swagger UI.
* Role-based authorization is optional and can be added later.

---

## Author

Vrishti Singh
vrishtisingh98@gmail.com

```
