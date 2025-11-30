# Tour Package Backend

Spring Boot 3.5.7 backend for the Tour Package platform. It exposes authenticated REST APIs for activity vendors, tour package vendors, finance (billing), and end-user management. This document summarizes how to run the service and documents every controller endpoint with full request/response examples.

## Tech Stack

- Java 21, Gradle 8
- Spring Boot (Web, Security, Data JPA, Validation)
- PostgreSQL + HikariCP
- JWT authentication (custom filter + `Authorization: Bearer` header)
- Docker & Kubernetes manifests (`k8s/`)

## Project Layout (key folders)

```
src/main/java           # application code
src/main/resources      # Spring configuration (application.yaml, profiles)
src/test/java           # unit and integration tests
docs/                   # diagrams & assets
k8s/                    # deployment, service, ingress manifests
```

## Local Development

### Prerequisites

- JDK 21 (`sdk install java 21-tem` or Azul/Zulu)
- Gradle Wrapper (already in repo)
- PostgreSQL 13+ reachable via `DATABASE_URL_DEV`
- Node 20 (only if you need to run the Vite frontend separately)

### Environment variables

Create `.env` (the dev profile imports it through `spring.config.import`):

| Variable                | Description                                                    |
| ----------------------- | -------------------------------------------------------------- |
| `DATABASE_URL_DEV`      | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/tour_package` |
| `DEV_USERNAME`          | Database username for dev profile                              |
| `DEV_PASSWORD`          | Database password for dev profile                              |
| `JWT_SECRET_KEY`        | Secret used to sign JWT tokens                                 |
| `BILL_SERVICE_BASE_URL` | Billing service base URL (default `http://localhost:8081`)     |
| `BILL_SERVICE_API_KEY`  | API key to call external billing service                       |
| `BILL_API_KEY`          | Shared secret required by `/api/package/{id}/payment/confirm`  |
| `CORS_ALLOWED_ORIGINS`  | Comma-separated origins allowed by `CorsConfig`                |

### Useful commands

```bash
# Install dependencies & verify build
./gradlew clean build

# Run the service with dev profile (loads .env)
./gradlew bootRun

# Format generated OpenAPI (if you later add springdoc)
./gradlew spotlessApply
```

### Testing & Coverage

```bash
# Run the full JUnit + Mockito suite
./gradlew test

# Generate JaCoCo HTML report (output under build/reports/jacoco/test/html/index.html)
./gradlew jacocoTestReport
```

## API Reference

- **Base URL**: `/api`
- **Authentication**: JWT via `Authorization: Bearer <token>` (except `/api/auth/**` & `/api/locations`).
- **Response envelope**: every controller returns `BaseResponseDTO<T>`.

```json
{
  "status": 200,
  "message": "Successfully retrieved activities",
  "timestamp": "2025-12-01T19:00:00.000+07:00",
  "data": {}
}
```

### Authentication (`AuthRestController`)

#### POST `/api/auth/login`

Request:

```json
{
  "username": "alice",
  "password": "Secret123"
}
```

Response `200`:

```json
{
  "status": 200,
  "message": "Login berhasil.",
  "timestamp": "2025-12-01T19:01:00.000+07:00",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresAt": "2025-12-02T19:01:00Z",
    "user": {
      "id": "8b6a8134-dc5c-4d1f-ab39-6bfb603a9165",
      "username": "alice",
      "email": "alice@example.com",
      "fullName": "Alice Customer",
      "gender": "FEMALE",
      "phoneNumber": "+6281234567890",
      "role": "CUSTOMER",
      "roleDisplayName": "Customer",
      "responsibility": "Book tour packages",
      "active": true,
      "organizationName": null,
      "notes": null,
      "saldo": 1500000,
      "createdAt": "2025-10-01T09:15:00",
      "updatedAt": "2025-11-20T13:45:00",
      "locations": ["JKT", "DPS"]
    }
  }
}
```

#### POST `/api/auth/register`

Request:

```json
{
  "username": "bob",
  "email": "bob@example.com",
  "fullName": "Bob Vendor",
  "phoneNumber": "+6281111111",
  "password": "Secret123",
  "organizationName": "Bob Travel",
  "notes": "Need reviewer access",
  "role": "TOUR_PACKAGE_VENDOR"
}
```

Response `201`:

```json
{
  "status": 201,
  "message": "User berhasil diproses (upsert).",
  "timestamp": "2025-12-01T19:05:00.000+07:00",
  "data": {
    "id": "4cf06065-57aa-41de-9a0c-57b743834316",
    "username": "bob",
    "email": "bob@example.com",
    "fullName": "Bob Vendor",
    "gender": null,
    "phoneNumber": "+6281111111",
    "role": "TOUR_PACKAGE_VENDOR",
    "roleDisplayName": "Tour Package Vendor",
    "responsibility": "Create tour packages",
    "active": true,
    "organizationName": "Bob Travel",
    "notes": "Need reviewer access",
    "saldo": 0,
    "createdAt": "2025-12-01T19:05:00",
    "updatedAt": "2025-12-01T19:05:00",
    "locations": []
  }
}
```

### Activities (`ActivityRestController`)

#### GET `/api/activities`

Example: `/api/activities?activityType=Flight&startLocation=CGK&search=air`.
Response `200`:

```json
{
  "status": 200,
  "message": "Successfully retrieved activities",
  "timestamp": "2025-12-01T19:10:00.000+07:00",
  "data": [
    {
      "id": "ACT-20251201-001",
      "activityName": "Eco Flight",
      "activityItem": "CGK-DPS",
      "capacity": 120,
      "price": 1200000,
      "activityType": "Flight",
      "creatorId": "4cf06065-57aa-41de-9a0c-57b743834316",
      "startDate": "2026-01-01T08:00:00",
      "endDate": "2026-01-01T10:00:00",
      "startLocation": "CGK",
      "endLocation": "DPS",
      "isDeleted": false
    }
  ]
}
```

#### GET `/api/activities/{id}`

Response `200`:

```json
{
  "status": 200,
  "message": "Successfully retrieved activity",
  "timestamp": "2025-12-01T19:12:00.000+07:00",
  "data": {
    "id": "ACT-20251201-001",
    "activityName": "Eco Flight",
    "activityItem": "CGK-DPS",
    "capacity": 120,
    "price": 1200000,
    "activityType": "Flight",
    "creatorId": "4cf06065-57aa-41de-9a0c-57b743834316",
    "startDate": "2026-01-01T08:00:00",
    "endDate": "2026-01-01T10:00:00",
    "startLocation": "CGK",
    "endLocation": "DPS",
    "isDeleted": false
  }
}
```

#### POST `/api/activities`

Request:

```json
{
  "activityName": "Eco Flight",
  "activityItem": "CGK-DPS",
  "capacity": 120,
  "price": 1200000,
  "activityType": "Flight",
  "startDate": "2026-01-01T08:00:00",
  "endDate": "2026-01-01T10:00:00",
  "startLocation": "CGK",
  "endLocation": "DPS"
}
```

Response `201`: same shape as detail response.

#### PUT `/api/activities/{id}`

Request:

```json
{
  "activityName": "Eco Flight Premium",
  "activityItem": "CGK-DPS",
  "capacity": 110,
  "price": 1350000,
  "startDate": "2026-01-01T09:00:00",
  "endDate": "2026-01-01T11:00:00",
  "startLocation": "CGK",
  "endLocation": "DPS"
}
```

Response `200`: updated `ActivityResponseDTO`.

#### DELETE `/api/activities/{id}`

Response `200`:

```json
{
  "status": 200,
  "message": "Successfully deleted activity",
  "timestamp": "2025-12-01T19:20:00.000+07:00",
  "data": null
}
```

### Packages (`PackageRestController`)

#### GET `/api/package`

Response `200`:

```json
{
  "status": 200,
  "message": "Successfully retrieved packages",
  "timestamp": "2025-12-01T19:25:00.000+07:00",
  "data": [
    {
      "id": "PKG-20251201-001",
      "userId": "4cf06065-57aa-41de-9a0c-57b743834316",
      "packageName": "Bali Escape",
      "quota": 20,
      "price": 8500000,
      "status": "PENDING",
      "startDate": "2026-01-01T00:00:00",
      "endDate": "2026-01-07T00:00:00"
    }
  ]
}
```

#### GET `/api/package/{id}`

Returns single `PackageResponseDTO`.

#### GET `/api/package/{id}/detail`

Response `200`:

```json
{
  "status": 200,
  "message": "Successfully retrieved package detail",
  "timestamp": "2025-12-01T19:26:00.000+07:00",
  "data": {
    "id": "PKG-20251201-001",
    "userId": "4cf06065-57aa-41de-9a0c-57b743834316",
    "packageName": "Bali Escape",
    "quota": 20,
    "price": 8500000,
    "status": "PENDING",
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-01-07T00:00:00",
    "plans": [
      {
        "id": "06f6a140-c1ee-48c8-87f8-9f4f8c70b0a5",
        "planName": "Outbound Day 1",
        "price": 3500000,
        "activityType": "Outdoor",
        "status": "DRAFT",
        "startDate": "2026-01-02T08:00:00",
        "endDate": "2026-01-02T18:00:00",
        "startLocation": "Kuta",
        "endLocation": "Ubud",
        "activitiesCount": 2,
        "capacity": 20
      }
    ]
  }
}
```

#### POST `/api/package`

Request:

```json
{
  "userId": "4cf06065-57aa-41de-9a0c-57b743834316",
  "packageName": "Bali Escape",
  "quota": 20,
  "price": null,
  "status": null,
  "startDate": "2026-01-01T00:00:00",
  "endDate": "2026-01-07T00:00:00"
}
```

Response `201`: `PackageResponseDTO` with generated id + status `PENDING`.

#### PUT `/api/package/{id}`

Same request shape as create, response `200` with updated package.

#### DELETE `/api/package/{id}`

Response `200` when package is still `PENDING`.

#### PUT `/api/package/{id}/process`

Response `200` with processed package (status `WAITING_FOR_PAYMENT`) when all plans contain enough ordered activities.

#### PUT `/api/package/{id}/payment/confirm`

Headers: `X-API-KEY: <BILL_API_KEY>`.
Response `200` sets status to `PAID`:

```json
{
  "status": 200,
  "message": "Payment confirmed",
  "timestamp": "2025-12-01T19:40:00.000+07:00",
  "data": {
    "id": "PKG-20251201-001",
    "userId": "4cf06065-57aa-41de-9a0c-57b743834316",
    "packageName": "Bali Escape",
    "quota": 20,
    "price": 9000000,
    "status": "PAID",
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-01-07T00:00:00"
  }
}
```

### Plans (`PlanRestController` & `PlanEditRestController`)

#### GET `/api/packages/{packageId}/plans`

Response `200`: list of `PlanResponseDTO`.

#### POST `/api/packages/{packageId}/plans/create`

Request:

```json
{
  "planName": "Outbound Day 1",
  "activityType": "Outdoor",
  "startDate": "2026-01-02T08:00:00",
  "endDate": "2026-01-02T18:00:00",
  "startLocation": "Kuta",
  "endLocation": "Ubud"
}
```

Response `201`: `PlanResponseDTO`.

#### GET `/api/packages/plans/{planId}`

Response includes ordered activities:

```json
{
  "status": 200,
  "message": "Successfully retrieved plan detail",
  "timestamp": "2025-12-01T19:45:00.000+07:00",
  "data": {
    "id": "06f6a140-c1ee-48c8-87f8-9f4f8c70b0a5",
    "planName": "Outbound Day 1",
    "activityType": "Outdoor",
    "status": "DRAFT",
    "totalPrice": 3500000,
    "startDate": "2026-01-02T08:00:00",
    "endDate": "2026-01-02T18:00:00",
    "startLocation": "Kuta",
    "endLocation": "Ubud",
    "packageId": "PKG-20251201-001",
    "packageName": "Bali Escape",
    "orderedQuantities": [
      {
        "id": "97d09156-8c18-4aa7-8e25-7e87c2642cb3",
        "activityName": "Eco Flight",
        "activityId": "ACT-20251201-001",
        "startDate": "2026-01-02T09:00:00",
        "endDate": "2026-01-02T12:00:00",
        "price": 1200000,
        "quota": 120,
        "orderedQuota": 20,
        "total": 24000000
      }
    ]
  }
}
```

#### DELETE `/api/packages/plans/{planId}/delete`

Response `200`.

#### GET `/api/plans/{planId}/edit`

Same payload as plan detail, used by admin UI.

#### PUT `/api/plans/{planId}/edit`

Request:

```json
{
  "planName": "Outbound Day 1",
  "startDate": "2026-01-02T09:00:00",
  "endDate": "2026-01-02T19:00:00",
  "startLocation": "Legian",
  "endLocation": "Ubud"
}
```

Response `200`: updated plan detail.

#### GET `/api/plans/{planId}/available-activities`

Returns list of `PlanResponseDTO` that can share activities (used internally by UI when picking substitute activities).

### Ordered Activities (`OrderedQuantityRestController`)

#### POST `/api/ordered-activities/create?planId={planId}`

Request:

```json
{
  "activityId": "ACT-20251201-001",
  "orderedQuantity": 20
}
```

Response `200`: updated `PlanDetailResponseDTO` (see plan detail example).

#### PUT `/api/ordered-activities/{id}/edit`

Request:

```json
{
  "orderedQuantity": 18
}
```

Response `200`: updated plan detail.

#### DELETE `/api/ordered-activities/{id}/delete`

Response `200`: updated plan detail after soft-deleting the ordered activity.

### End User Management (`EndUserRestController`)

#### GET `/api/end-users`

Response `200`:

```json
{
  "status": 200,
  "message": "End users retrieved successfully",
  "timestamp": "2025-12-01T20:00:00.000+07:00",
  "data": [
    {
      "id": "8b6a8134-dc5c-4d1f-ab39-6bfb603a9165",
      "username": "alice",
      "email": "alice@example.com",
      "fullName": "Alice Customer",
      "gender": "FEMALE",
      "phoneNumber": "+6281234567890",
      "role": "CUSTOMER",
      "roleDisplayName": "Customer",
      "responsibility": "Book tour packages",
      "active": true,
      "organizationName": null,
      "notes": null,
      "saldo": 1500000,
      "createdAt": "2025-10-01T09:15:00",
      "updatedAt": "2025-11-20T13:45:00",
      "locations": ["JKT", "DPS"]
    }
  ]
}
```

#### GET `/api/end-users/role/{roleType}`

Returns filtered users (`roleType` values follow `RoleType` enum: `SUPERADMIN`, `TOUR_PACKAGE_VENDOR`, `FLIGHT_AIRLINE`, `ACCOMMODATION_OWNER`, `RENTAL_VENDOR`, `CUSTOMER`).

#### GET `/api/end-users/customers`

Query params: `name`, `email`. Response data array of `CustomerResponseDTO`.

#### GET `/api/end-users/{identifier}`

`identifier` can be UUID, username, or email (service resolves automatically).

#### POST `/api/end-users/create`

Request:

```json
{
  "username": "new_customer",
  "email": "new.customer@example.com",
  "fullName": "New Customer",
  "gender": "FEMALE",
  "phoneNumber": "+6287770000",
  "password": "Secret123",
  "organizationName": null,
  "notes": "Invited from expo",
  "role": "CUSTOMER",
  "active": true,
  "saldo": 1000000
}
```

Response `201`: `EndUserResponseDTO`.

#### PUT `/api/end-users/{id}/edit`

Request uses `UpdateEndUserRequestDTO` (same fields as create, all optional except `id`). Response `200` returns updated user.

#### DELETE `/api/end-users/{id}/delete`

Soft deletes / deactivates depending on role.

#### POST `/api/end-users/{id}/deduct-balance`

Request:

```json
{
  "amount": 250000.0
}
```

Response `200`:

```json
{
  "status": 200,
  "message": "Saldo berhasil dipotong.",
  "timestamp": "2025-12-01T20:10:00.000+07:00",
  "data": "Success"
}
```

### Profile (`ProfileRestController`)

#### POST `/api/profile/users`

Request uses `UpsertEndUserRequestDTO` (same fields as register). Response `200` returns `EndUserResponseDTO`.

#### GET `/api/profile/users?includeInactive=false`

Response data array of users (same as `EndUserResponseDTO`).

#### GET `/api/profile/users/{username}`

Returns single user.

#### GET `/api/profile/roles`

Response `200`:

```json
{
  "status": 200,
  "message": "Berhasil mengambil data role.",
  "timestamp": "2025-12-01T20:15:00.000+07:00",
  "data": [
    {
      "code": "SUPERADMIN",
      "name": "Super Admin",
      "responsibility": "Maintain platform"
    },
    {
      "code": "CUSTOMER",
      "name": "Customer",
      "responsibility": "Purchase tour packages"
    }
  ]
}
```

### Statistics (`StatisticsRestController`)

#### GET `/api/statistics/revenue?year=2025`

Response `200`:

```json
{
  "status": 200,
  "message": "Successfully retrieved revenue statistics",
  "timestamp": "2025-12-01T20:20:00.000+07:00",
  "data": {
    "period": "2025",
    "totalRevenue": 125000000,
    "monthlyRevenues": [
      {
        "period": "2025-01",
        "totalRevenue": 10000000
      },
      {
        "period": "2025-02",
        "totalRevenue": 15000000
      }
    ],
    "revenueByActivityType": null
  }
}
```

#### GET `/api/statistics/revenue?year=2025&month=11`

Response `200`:

```json
{
  "status": 200,
  "message": "Successfully retrieved revenue statistics",
  "timestamp": "2025-12-01T20:21:00.000+07:00",
  "data": {
    "period": "2025-11",
    "totalRevenue": 18000000,
    "monthlyRevenues": null,
    "revenueByActivityType": [
      {
        "activityType": "Flight",
        "totalRevenue": 8000000
      },
      {
        "activityType": "Accommodation",
        "totalRevenue": 6000000
      }
    ]
  }
}
```

#### GET `/api/statistics/revenue/yearly/{year}`

Response data array of `MonthlyRevenueDTO` for the given year.

#### GET `/api/statistics/revenue/monthly/{year}/{month}`

Same payload as `revenue?year=&month=` but path-based.

### Locations (`LocationRestController`)

#### GET `/api/locations`

Response `200`:

```json
{
  "status": 200,
  "message": "Successfully retrieved locations",
  "timestamp": "2025-12-01T20:25:00.000+07:00",
  "data": [
    {
      "code": "11",
      "name": "Aceh"
    },
    {
      "code": "12",
      "name": "Sumatera Utara"
    }
  ]
}
```

## Troubleshooting

- **`CORS_ALLOWED_ORIGINS` missing**: supply this variable in `.env` or the container environment to let the `CorsConfig` bean start.
- **`column creator_id does not exist` in production**: production DB schema lags behind dev. Apply an `ALTER TABLE activity ADD COLUMN creator_id VARCHAR(255);` migration (or configure Flyway/Liquibase) before deploying new builds.
- **Billing integration**: `/api/package/{id}/payment/confirm` rejects calls unless `X-API-KEY` matches the server `BILL_API_KEY`. Ensure CI/CD pipelines set both `BILL_API_KEY` (backend) and `VITE_API_BASE_URL`/`VITE_TOPUP_SERVICE_URL` (frontend) before building images.

## Next Steps

1. Automate schema migrations with Flyway so production matches local changes.
2. Publish an OpenAPI/Swagger spec (springdoc) and host it under `/swagger-ui.html` for consumers.
3. Add Postman collections under `docs/` for QA and partner onboarding if required.
