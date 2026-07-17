# Toyota Connected Services API

Backend RESTful API for managing connected vehicle subscriptions and remote assistance requests.

## Requirements

- Java 21 for this practice workspace. The official exam statement targets Java 26.
- Maven Wrapper included in the project.
- MySQL running locally.
- Database schema: `toyota_connected_services`.

## Run

Create the database schema before starting the application:

```sql
CREATE DATABASE IF NOT EXISTS toyota_connected_services;
```

Open PowerShell in the project folder:

```powershell
cd "C:\Users\familia\Desktop\Nueva carpeta\DEOS-intento1\eb1122u202410837\eb1122u202410837"
```

Check that the folder contains `mvnw.cmd`:

```powershell
dir
```

Compile the project:

```powershell
.\mvnw.cmd -DskipTests compile
```

Run the application:

```powershell
.\mvnw.cmd spring-boot:run
```

If PowerShell cannot find `.\mvnw.cmd`, you are in the parent folder. Enter the inner project folder first.

Swagger UI is available at:

```text
http://localhost:8096/swagger-ui/index.html
```

## Endpoints

- `POST /api/v1/vehicle-subscriptions`
- `POST /api/v1/assistance-requests`

## Author

Practice implementation for `eb1122u202410837`.
