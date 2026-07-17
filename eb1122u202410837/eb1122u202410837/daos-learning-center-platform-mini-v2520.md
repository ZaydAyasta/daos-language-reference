# Toyota Connected Services Mini Guide v2520

Esta guia resume como construir una solucion como la del examen usando el estilo de Learning Center Platform.

## 1. Crear el proyecto

Desde Spring Initializr se crea un proyecto Maven con Java, Spring Web MVC, Spring Data JPA, Validation, Lombok, MySQL Driver, DevTools y SpringDoc OpenAPI.

Comando de verificacion despues de abrir el proyecto:

```powershell
.\mvnw.cmd -DskipTests compile
```

## 2. Configurar MySQL

En `src/main/resources/application.properties` se configura la conexion:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/toyota_connected_services?useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=12345678
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database=mysql
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming.physical-strategy=com.toyota.platform.eb1122u202410837.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy
```

Crear la base antes de ejecutar:

```sql
CREATE DATABASE IF NOT EXISTS toyota_connected_services;
```

Si usas MySQL Workbench por primera vez, crea la conexion sin `Default Schema`, entra al servidor y ejecuta el comando anterior. Luego refresca la lista de schemas y confirma que aparece `toyota_connected_services`.

## 3. Preparar shared

Crear el bounded context `shared` para elementos reutilizables:

```text
shared
  domain
    exceptions
    model
      aggregates
      entities
    valueobjects
  infrastructure
    documentation.openapi.configuration
    persistence.jpa.configuration.strategy
  interfaces
    rest
      exceptions
      resources
```

Archivos clave:

- `AuditableAbstractAggregateRoot.java`: base para aggregates con `id`, `createdAt`, `updatedAt`.
- `SnakeCaseWithPluralizedTablePhysicalNamingStrategy.java`: convierte nombres Java a tablas plurales en `snake_case`.
- `Period.java`: value object del examen con `startDate` y `endDate`.
- `BusinessRuleException.java`: excepcion para reglas de negocio.
- `GlobalExceptionHandler.java`: respuestas centralizadas de error.

Compilar despues de esta etapa:

```powershell
.\mvnw.cmd -DskipTests compile
```

## 4. Crear bounded context subscriptions

Estructura:

```text
subscriptions
  application.internal
    commandservices
    eventhandlers
    queryservices
  domain
    model
      aggregates
      commands
      valueobjects
    services
  infrastructure.persistence.jpa.repositories
  interfaces
    acl
    rest
      resources
      transform
```

Archivos creados:

- `VehicleSubscription.java`: aggregate principal.
- `SubscriptionStatus.java`: enum calculado, no persistido.
- `CreateVehicleSubscriptionCommand.java`: comando de creacion.
- `VehicleSubscriptionRepository.java`: repository Spring Data JPA.
- `VehicleSubscriptionCommandService.java`: contrato del caso de uso.
- `VehicleSubscriptionCommandServiceImpl.java`: implementa duplicidad y persistencia.
- `VehicleSubscriptionsController.java`: expone `POST /api/v1/vehicle-subscriptions`.

Reglas colocadas en el aggregate:

- `subscriptionCode` debe cumplir `TCS-XXXXX`.
- `vehicleVin` debe tener 17 caracteres.
- `servicePeriod` es obligatorio.
- `subscriptionStatus` se calcula con `getSubscriptionStatus()`.
- `isEligibleForAssistance()` retorna `false` si `lastAssistanceRequestId` tiene valor.
- `@Version` aplica optimistic locking.

## 5. Crear bounded context assistance

Estructura:

```text
assistance
  application.internal
    commandservices
  domain
    model
      aggregates
      commands
      events
      valueobjects
    services
  infrastructure.persistence.jpa.repositories
  interfaces.rest
    resources
    transform
```

Archivos creados:

- `AssistanceRequest.java`: aggregate principal.
- `AssistanceStatus.java`: enum `REQUESTED`, `ASSIGNED`, `COMPLETED`, `CANCELLED`.
- `CreateAssistanceRequestCommand.java`: comando de creacion.
- `AssistanceRequestCreatedEvent.java`: evento de dominio.
- `AssistanceRequestRepository.java`: repository Spring Data JPA.
- `AssistanceRequestCommandServiceImpl.java`: valida ACL, estado, elegibilidad y emite evento.
- `AssistanceRequestsController.java`: expone `POST /api/v1/assistance-requests`.

Reglas colocadas:

- `subscriptionId` obligatorio.
- `issueDescription` obligatorio.
- `assistanceStatus` obligatorio.
- `requestedAt` obligatorio y no mayor que la fecha actual.
- `requestedAt` se recibe como String con formato `yyyy-MM-dd HH:mm:ss`.

## 6. Implementar ACL

El examen pide evitar dependencia directa entre bounded contexts. Para eso se crea:

```text
subscriptions/interfaces/acl/SubscriptionsContextFacade.java
```

Este facade devuelve un `SubscriptionSnapshot` con:

- `id`
- `subscriptionStatus`
- `eligibleForAssistance`

El command service de `assistance` usa este facade antes de guardar una solicitud.

## 7. Implementar evento y handler

Cuando se crea una asistencia valida, `AssistanceRequestCommandServiceImpl` publica:

```java
new AssistanceRequestCreatedEvent(subscriptionId, assistanceRequestId, requestedAt)
```

Luego `AssistanceRequestCreatedEventHandler` en `subscriptions`:

- carga la suscripcion.
- verifica idempotencia dentro de `markAssistanceRequestInProgress`.
- actualiza `lastAssistanceRequestId`.
- corre dentro de transaccion.
- usa `@Version` para optimistic locking.

## 8. Implementar seeding

Crear `VehicleSubscriptionsDataSeeder.java` y escuchar:

```java
@EventListener(ApplicationReadyEvent.class)
```

Insertar los cuatro registros del enunciado solo si no existen por `subscriptionCode`.

## 9. Implementar REST

Para cada endpoint se crean tres piezas:

- `...Resource.java`: request y response.
- `...Assembler.java`: transforma resource a command o entity a resource.
- `...Controller.java`: expone el POST.

Endpoints finales:

```text
POST /api/v1/vehicle-subscriptions
POST /api/v1/assistance-requests
```

Ejemplo `vehicle-subscriptions`:

```json
{
  "subscriptionCode": "TCS-20001",
  "vehicleVin": "JTDBR32E720999999",
  "startDate": "2026-01-01",
  "endDate": "2027-12-31"
}
```

Ejemplo `assistance-requests`:

```json
{
  "subscriptionId": 1,
  "issueDescription": "Flat tire assistance required",
  "assistanceStatus": "REQUESTED",
  "requestedAt": "2026-07-17 10:30:00"
}
```

## 10. Compilar y ejecutar

Primero abre PowerShell y entra a la carpeta real del proyecto. Si estas en la carpeta padre `eb1122u202410837`, debes entrar una carpeta mas:

```powershell
cd "C:\Users\familia\Desktop\Nueva carpeta\DEOS-intento1\eb1122u202410837\eb1122u202410837"
```

Verificar que estas en la carpeta correcta:

```powershell
dir
```

Debe aparecer `mvnw.cmd`, `pom.xml` y la carpeta `src`.

Crear el schema en MySQL Workbench antes de iniciar Spring Boot:

```sql
CREATE DATABASE IF NOT EXISTS toyota_connected_services;
```

Compilar despues de terminar dominio:

```powershell
.\mvnw.cmd -DskipTests compile
```

Compilar despues de crear controllers:

```powershell
.\mvnw.cmd -DskipTests compile
```

Ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

Si PowerShell dice que `.\mvnw.cmd` no existe, estas en la carpeta incorrecta. Vuelve a ejecutar el `cd` mostrado al inicio de esta seccion.

Abrir Swagger:

```text
http://localhost:8096/swagger-ui/index.html
```

Probar `POST /api/v1/vehicle-subscriptions`:

```json
{
  "subscriptionCode": "TCS-20001",
  "vehicleVin": "JTDBR32E720999999",
  "startDate": "2026-01-01",
  "endDate": "2027-12-31"
}
```

Probar `POST /api/v1/assistance-requests`:

```json
{
  "subscriptionId": 1,
  "issueDescription": "Flat tire assistance required",
  "assistanceStatus": "REQUESTED",
  "requestedAt": "2026-07-17 10:30:00"
}
```

Si Workbench muestra `Unknown database 'toyota_connected_services'`, significa que el schema aun no existe o que intentaste conectarte usando ese schema como default antes de crearlo. Conectate sin default schema y ejecuta `CREATE DATABASE IF NOT EXISTS toyota_connected_services;`.

## 11. Checklist final

- Proyecto compila.
- MySQL usa `toyota_connected_services`.
- Existen bounded contexts `subscriptions`, `assistance`, `shared`.
- `Period` vive en `shared`.
- `VehicleSubscription` vive en `subscriptions`.
- `AssistanceRequest` vive en `assistance`.
- Los dos endpoints POST devuelven `201 Created`.
- Los responses no exponen `createdAt` ni `updatedAt`.
- Los errores pasan por `GlobalExceptionHandler`.
- Hay mensajes en ingles y espanol.
- Hay seeding automatico con `ApplicationReadyEvent`.
- Swagger esta disponible.
