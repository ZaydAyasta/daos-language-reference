# Toyota Connected Services Mini Guide v2520

Esta guia explica que se implemento, por que se implemento y que archivo corresponde a cada restriccion del enunciado. La idea no es solo copiar clases, sino entender como convertir el PDF en codigo.

## 1. Proyecto y dependencias

El PDF pide: "Java 26, Spring Boot Framework 4.1 como development framework y Spring Data JPA como ORM".

Esto significa que la solucion debe ser un backend Spring Boot con persistencia JPA. En esta practica se uso Java 21 porque es el JDK disponible en Windows, pero la estructura es la misma para Java 26.

Archivos relacionados:

- `pom.xml`: contiene Spring Web MVC, Spring Data JPA, Validation, Lombok, MySQL Driver y SpringDoc OpenAPI.
- `src/main/java/.../Eb1122u202410837Application.java`: clase principal de Spring Boot.

Comando para verificar que las dependencias y el codigo compilan:

```powershell
.\mvnw.cmd -DskipTests compile
```

## 2. Configuracion de MySQL

El PDF pide: "La informacion debe ser persistente en una base de datos relacional (MySQL), en un esquema toyota_connected_services".

Por eso se cambio `application.properties` para usar MySQL y el schema exacto:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/toyota_connected_services?useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=12345678
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database=mysql
spring.jpa.hibernate.ddl-auto=update
```

Archivo relacionado:

- `src/main/resources/application.properties`

Comando SQL que debes ejecutar en MySQL Workbench:

```sql
CREATE DATABASE IF NOT EXISTS toyota_connected_services;
```

Si Workbench muestra `Unknown database 'toyota_connected_services'`, significa que intentaste conectarte a un schema que todavia no existe. En ese caso, conectate sin default schema, ejecuta el `CREATE DATABASE`, y luego refresca los schemas.

## 3. Shared bounded context

El PDF pide: "Considere el bounded context shared para elementos base comunes o reutilizables".

Por eso `shared` contiene infraestructura y piezas reutilizables, no logica propia de Toyota Assistance o Subscriptions.

Archivos relacionados:

- `shared/domain/model/aggregates/AuditableAbstractAggregateRoot.java`: base para aggregates con `id`, `createdAt` y `updatedAt`.
- `shared/domain/model/entities/AuditableModel.java`: base auditable para entidades no aggregate.
- `shared/infrastructure/persistence/jpa/configuration/strategy/SnakeCaseWithPluralizedTablePhysicalNamingStrategy.java`: naming strategy para tablas plurales y `snake_case`.
- `shared/domain/exceptions/BusinessRuleException.java`: excepcion de reglas de negocio.
- `shared/interfaces/rest/exceptions/GlobalExceptionHandler.java`: manejo centralizado de errores.
- `shared/infrastructure/documentation/openapi/configuration/OpenApiConfiguration.java`: informacion general de OpenAPI.

## 4. Period como Value Object

El PDF dice: "servicePeriod Value Object (de tipo Period que contiene startDate, endDate)" y tambien dice que "Period pertenece al bounded context shared".

Eso significa que `servicePeriod` no debe ser una clase propia en `subscriptions`; debe ser un atributo dentro de `VehicleSubscription`, pero su tipo es `Period`, y `Period` vive en `shared`.

Archivo relacionado:

- `shared/domain/valueobjects/Period.java`

Implementacion:

- Es un `record`, porque el PDF pide usar records para value objects inmutables.
- Tiene `startDate` y `endDate`.
- Tiene `includes(LocalDate date)` para responder si una fecha esta dentro del periodo.

Esto permite que `VehicleSubscription` calcule su estado sin guardar `subscriptionStatus` en la base de datos.

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: servicePeriod Value Object, de tipo Period, contiene startDate y endDate.
Codigo: shared/domain/valueobjects/Period.java
```

```java
@Embeddable
public record Period(LocalDate startDate, LocalDate endDate) {
  public boolean includes(LocalDate date) {
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }
}
```

## 5. Subscriptions bounded context

El PDF dice: "VehicleSubscription pertenece al bounded context subscriptions".

Por eso se creo esta estructura:

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

Archivos principales:

- `subscriptions/domain/model/aggregates/VehicleSubscription.java`
- `subscriptions/domain/model/valueobjects/SubscriptionStatus.java`
- `subscriptions/domain/model/commands/CreateVehicleSubscriptionCommand.java`
- `subscriptions/domain/services/VehicleSubscriptionCommandService.java`
- `subscriptions/application/internal/commandservices/VehicleSubscriptionCommandServiceImpl.java`
- `subscriptions/infrastructure/persistence/jpa/repositories/VehicleSubscriptionRepository.java`
- `subscriptions/interfaces/rest/VehicleSubscriptionsController.java`

## 6. VehicleSubscription aggregate

El PDF dice: "El aggregate VehicleSubscription contiene: id (Long, PK), subscriptionCode (String, unico, formato: TCS-XXXXX), vehicleVin (String, unico, obligatorio y de 17 caracteres), servicePeriod (Period, obligatorio) y lastAssistanceRequestId (Long, nullable)".

Eso corresponde a:

- `VehicleSubscription.java`: define el aggregate.
- `AuditableAbstractAggregateRoot`: aporta `id`, `createdAt`, `updatedAt`.
- `@Column(unique = true)`: se aplica a `subscriptionCode` y `vehicleVin`.
- `@Embedded`: se aplica a `Period servicePeriod`.
- `Long lastAssistanceRequestId`: nullable porque `Long` puede ser `null`.

Tambien se implementaron validaciones dentro del aggregate:

- `validateSubscriptionCode`: valida `TCS-XXXXX`.
- `validateVehicleVin`: valida 17 caracteres.
- `validateServicePeriod`: valida que el periodo exista.

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: subscriptionCode unico, formato TCS-XXXXX.
Codigo: @Column(unique = true) + validateSubscriptionCode(...)
```

```java
@Column(nullable = false, unique = true)
private String subscriptionCode;

private String validateSubscriptionCode(String subscriptionCode) {
  if (subscriptionCode == null || !subscriptionCode.matches("TCS-\\d{5}")) {
    throw new IllegalArgumentException("Subscription code must match format TCS-XXXXX");
  }
  return subscriptionCode;
}
```

```text
PDF: vehicleVin unico, obligatorio y de 17 caracteres.
Codigo: @Column(unique = true, length = 17) + validateVehicleVin(...)
```

```java
@Column(nullable = false, unique = true, length = 17)
private String vehicleVin;
```

## 7. subscriptionStatus calculado

El PDF dice: "subscriptionStatus no debe ser persistido en base de datos, sino calculado como un invariante del dominio".

Por eso no existe un atributo persistido `subscriptionStatus` en `VehicleSubscription`. En su lugar se implemento:

```java
public SubscriptionStatus getSubscriptionStatus()
```

Archivos relacionados:

- `subscriptions/domain/model/aggregates/VehicleSubscription.java`
- `subscriptions/domain/model/valueobjects/SubscriptionStatus.java`
- `shared/domain/valueobjects/Period.java`

El metodo usa `servicePeriod.includes(LocalDate.now())`. Si la fecha actual esta dentro del periodo, retorna `ACTIVE`; si no, retorna `EXPIRED`.

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: subscriptionStatus no debe ser persistido.
Codigo: no existe private SubscriptionStatus subscriptionStatus; solo existe getSubscriptionStatus().
```

```java
public SubscriptionStatus getSubscriptionStatus() {
  return servicePeriod.includes(LocalDate.now())
      ? SubscriptionStatus.ACTIVE
      : SubscriptionStatus.EXPIRED;
}
```

## 8. Regla isEligibleForAssistance

El PDF dice: "Mientras el atributo lastAssistanceRequestId contenga un valor, el metodo de negocio isEligibleForAssistance() del aggregate debe retornar false".

Por eso se implemento en:

- `subscriptions/domain/model/aggregates/VehicleSubscription.java`

Metodo:

```java
public boolean isEligibleForAssistance()
```

La regla es simple: si `lastAssistanceRequestId == null`, la suscripcion puede registrar asistencia; si ya tiene valor, no puede.

Ejemplo de correspondencia PDF -> codigo:

```java
public boolean isEligibleForAssistance() {
  return lastAssistanceRequestId == null;
}
```

## 9. Optimistic locking

El PDF dice que el handler debe "aplicar Optimistic Locking en la entidad VehicleSubscription".

Por eso se agrego:

```java
@Version
private Long version;
```

Archivo relacionado:

- `subscriptions/domain/model/aggregates/VehicleSubscription.java`

Esto ayuda a prevenir inconsistencias si dos solicitudes intentan modificar la misma suscripcion al mismo tiempo.

Ejemplo de correspondencia PDF -> codigo:

```java
@Version
private Long version;
```

## 10. Assistance bounded context

El PDF dice: "AssistanceRequest pertenece al bounded context assistance".

Por eso se creo esta estructura:

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

Archivos principales:

- `assistance/domain/model/aggregates/AssistanceRequest.java`
- `assistance/domain/model/valueobjects/AssistanceStatus.java`
- `assistance/domain/model/commands/CreateAssistanceRequestCommand.java`
- `assistance/domain/model/events/AssistanceRequestCreatedEvent.java`
- `assistance/domain/services/AssistanceRequestCommandService.java`
- `assistance/application/internal/commandservices/AssistanceRequestCommandServiceImpl.java`
- `assistance/infrastructure/persistence/jpa/repositories/AssistanceRequestRepository.java`
- `assistance/interfaces/rest/AssistanceRequestsController.java`

## 11. AssistanceRequest aggregate

El PDF dice: "El aggregate AssistanceRequest contiene: id (Long, PK), subscriptionId (Long, obligatorio), issueDescription (String, obligatorio), assistanceStatus (AssistanceStatus, obligatorio) y requestedAt (LocalDateTime, obligatorio, no mayor que la fecha actual)".

Eso corresponde a:

- `assistance/domain/model/aggregates/AssistanceRequest.java`

Implementacion:

- Hereda de `AuditableAbstractAggregateRoot`, por eso obtiene `id`, `createdAt` y `updatedAt`.
- Tiene `subscriptionId`, `issueDescription`, `assistanceStatus` y `requestedAt`.
- Valida que `requestedAt` no sea futuro.
- Valida que los campos obligatorios no sean nulos o vacios.

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: requestedAt obligatorio, no mayor que la fecha actual.
Codigo: validateRequestedAt(...)
```

```java
private LocalDateTime validateRequestedAt(LocalDateTime requestedAt) {
  if (requestedAt == null) {
    throw new IllegalArgumentException("Requested at cannot be null");
  }
  if (requestedAt.isAfter(LocalDateTime.now())) {
    throw new IllegalArgumentException("Requested at cannot be in the future");
  }
  return requestedAt;
}
```

## 12. AssistanceStatus enum

El PDF dice: "AssistanceStatus es un enumeration con los valores: REQUESTED, ASSIGNED, COMPLETED, CANCELLED".

Eso corresponde a:

- `assistance/domain/model/valueobjects/AssistanceStatus.java`

Se guarda como texto con:

```java
@Enumerated(EnumType.STRING)
```

Esto evita guardar numeros magicos en la base de datos.

Ejemplo de correspondencia PDF -> codigo:

```java
public enum AssistanceStatus {
  REQUESTED,
  ASSIGNED,
  COMPLETED,
  CANCELLED
}
```

## 13. ACL entre Subscriptions y Assistance

El PDF dice: "validar la existencia de la suscripcion asociada mediante una capa de Anti-Corruption Layer (ACL) entre los bounded contexts Subscriptions y Assistance, evitando una dependencia directa entre ambos contextos".

Por eso `assistance` no consulta directamente el aggregate `VehicleSubscription`. En su lugar se usa:

- `subscriptions/interfaces/acl/SubscriptionsContextFacade.java`

Ese facade expone un `SubscriptionSnapshot` con:

- `id`
- `subscriptionStatus`
- `eligibleForAssistance`

Archivo que consume el ACL:

- `assistance/application/internal/commandservices/AssistanceRequestCommandServiceImpl.java`

Ese service valida:

- que la suscripcion exista.
- que no este `EXPIRED`.
- que sea elegible para asistencia.

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: Assistance debe validar la suscripcion mediante ACL.
Codigo: AssistanceRequestCommandServiceImpl usa SubscriptionsContextFacade.
```

```java
var subscription = subscriptionsContextFacade.fetchSubscriptionById(command.subscriptionId());
if (subscription.subscriptionStatus() == SubscriptionStatus.EXPIRED) {
  throw new BusinessRuleException("assistance-request.subscription-expired");
}
if (!subscription.eligibleForAssistance()) {
  throw new BusinessRuleException("assistance-request.subscription-not-eligible");
}
```

## 14. Evento AssistanceRequestCreatedEvent

El PDF dice: "Al registrar una AssistanceRequest valida, se debe emitir un AssistanceRequestCreatedEvent que contenga subscriptionId, assistanceRequestId y requestedAt".

Por eso se creo:

- `assistance/domain/model/events/AssistanceRequestCreatedEvent.java`

Y se publica desde:

- `assistance/application/internal/commandservices/AssistanceRequestCommandServiceImpl.java`

Cuando se guarda una asistencia valida, el service publica:

```java
new AssistanceRequestCreatedEvent(subscriptionId, assistanceRequestId, requestedAt)
```

Ejemplo de correspondencia PDF -> codigo:

```java
public record AssistanceRequestCreatedEvent(
    Long subscriptionId,
    Long assistanceRequestId,
    LocalDateTime requestedAt
) {
}
```

## 15. Event handler en Subscriptions

El PDF dice que el Subscriptions Bounded Context debe implementar un Event Handler para `AssistanceRequestCreatedEvent`, que sea idempotente, transaccional, aplique optimistic locking y actualice `lastAssistanceRequestId`.

Eso corresponde a:

- `subscriptions/application/internal/eventhandlers/AssistanceRequestCreatedEventHandler.java`

Implementacion:

- Escucha el evento con `@EventListener`.
- Corre con `@Transactional`.
- Carga la suscripcion.
- Llama `markAssistanceRequestInProgress`.
- Guarda la suscripcion.

La idempotencia se apoya en:

- `VehicleSubscription.markAssistanceRequestInProgress(Long assistanceRequestId)`

Si el mismo `assistanceRequestId` ya fue procesado, no vuelve a modificar.

Ejemplo de correspondencia PDF -> codigo:

```java
@EventListener
@Transactional
public void on(AssistanceRequestCreatedEvent event) {
  var subscription = vehicleSubscriptionRepository.findById(event.subscriptionId())
      .orElseThrow(() -> new IllegalArgumentException("Vehicle subscription not found"));
  subscription.markAssistanceRequestInProgress(event.assistanceRequestId());
  vehicleSubscriptionRepository.save(subscription);
}
```

## 16. Data seeding

El PDF dice: "La plataforma debe realizar un Data Seeding automatico al iniciar mediante el evento ApplicationReadyEvent de Spring Boot".

Por eso se creo:

- `subscriptions/application/internal/eventhandlers/VehicleSubscriptionsDataSeeder.java`

Este componente escucha:

```java
@EventListener(ApplicationReadyEvent.class)
```

E inserta los cuatro registros del enunciado. Para no duplicar datos, primero verifica si ya existe el `subscriptionCode`.

Ejemplo de correspondencia PDF -> codigo:

```java
@EventListener(ApplicationReadyEvent.class)
public void seedVehicleSubscriptions() {
  var subscriptions = List.of(
      new VehicleSubscription("TCS-10001", "JTDBR32E720123456",
          new Period(LocalDate.parse("2026-01-01"), LocalDate.parse("2027-12-31")), null)
  );
}
```

## 17. Endpoints REST

El PDF pide trabajar especificamente sobre:

```text
/api/v1/vehicle-subscriptions
/api/v1/assistance-requests
```

Y dice que ambos deben exponer solo una operacion: `POST`.

Por eso se implementaron:

- `subscriptions/interfaces/rest/VehicleSubscriptionsController.java`
- `assistance/interfaces/rest/AssistanceRequestsController.java`

Ambos devuelven `201 Created` al crear correctamente.

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: /api/v1/vehicle-subscriptions debe exponer POST.
Codigo: VehicleSubscriptionsController.
```

```java
@RequestMapping(value = "/api/v1/vehicle-subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<VehicleSubscriptionResource> createVehicleSubscription(...)
```

```text
PDF: /api/v1/assistance-requests debe exponer POST.
Codigo: AssistanceRequestsController.
```

```java
@RequestMapping(value = "/api/v1/assistance-requests", produces = MediaType.APPLICATION_JSON_VALUE)
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<AssistanceRequestResource> createAssistanceRequest(...)
```

## 18. Resources y Assemblers

El PDF pide usar el patron Assembler para object mapping en `interfaces layer`.

Por eso cada endpoint tiene:

- `...Resource.java`: objeto de entrada o salida.
- `...CommandFromResourceAssembler.java`: convierte request resource a command.
- `...ResourceFromEntityAssembler.java`: convierte aggregate/entity a response resource.

Archivos relacionados:

- `subscriptions/interfaces/rest/resources/CreateVehicleSubscriptionResource.java`
- `subscriptions/interfaces/rest/resources/VehicleSubscriptionResource.java`
- `subscriptions/interfaces/rest/transform/CreateVehicleSubscriptionCommandFromResourceAssembler.java`
- `subscriptions/interfaces/rest/transform/VehicleSubscriptionResourceFromEntityAssembler.java`
- `assistance/interfaces/rest/resources/CreateAssistanceRequestResource.java`
- `assistance/interfaces/rest/resources/AssistanceRequestResource.java`
- `assistance/interfaces/rest/transform/CreateAssistanceRequestCommandFromResourceAssembler.java`
- `assistance/interfaces/rest/transform/AssistanceRequestResourceFromEntityAssembler.java`

Los responses no incluyen `createdAt` ni `updatedAt`, porque el PDF dice que los atributos de auditoria son de uso interno.

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: usar patron Assembler para Object Mapping.
Codigo: CreateVehicleSubscriptionCommandFromResourceAssembler.
```

```java
public static CreateVehicleSubscriptionCommand toCommandFromResource(
    CreateVehicleSubscriptionResource resource) {
  return new CreateVehicleSubscriptionCommand(
      resource.subscriptionCode(),
      resource.vehicleVin(),
      LocalDate.parse(resource.startDate()),
      LocalDate.parse(resource.endDate())
  );
}
```

## 19. requestedAt como String

El PDF dice: "Al momento del request, debe soportar que se proporcione el valor de requestedAt como un String con la estructura yyyy-MM-dd HH:mm:ss".

Por eso el request resource usa:

```java
String requestedAt
```

Archivo relacionado:

- `assistance/interfaces/rest/resources/CreateAssistanceRequestResource.java`

Y el assembler lo convierte a `LocalDateTime`:

- `assistance/interfaces/rest/transform/CreateAssistanceRequestCommandFromResourceAssembler.java`

Formato usado:

```java
DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
```

Ejemplo de correspondencia PDF -> codigo:

```java
public record CreateAssistanceRequestResource(
    Long subscriptionId,
    String issueDescription,
    String assistanceStatus,
    String requestedAt
) {
}
```

```java
LocalDateTime.parse(resource.requestedAt(), REQUESTED_AT_FORMATTER)
```

## 20. i18n y errores

El PDF dice: "Los responses deben soportar localization (i18n)" y pide mensajes en English y Espanol segun `Accept-Language`.

Por eso se crearon:

- `src/main/resources/messages.properties`
- `src/main/resources/messages_es.properties`
- `shared/interfaces/rest/exceptions/GlobalExceptionHandler.java`

En `application.properties` se configuro:

```properties
spring.messages.basename=messages
spring.messages.encoding=UTF-8
```

Ejemplo de correspondencia PDF -> codigo:

```text
PDF: English y Espanol segun Accept-Language.
Codigo: messages.properties + messages_es.properties + MessageSource.
```

```java
messageSource.getMessage(code, null, code, locale)
```

## 21. OpenAPI

El PDF pide: "Incluya documentacion de los Endpoints mediante OpenAPI".

Por eso se agrego:

- `shared/infrastructure/documentation/openapi/configuration/OpenApiConfiguration.java`
- `@Tag` y `@Operation` en ambos controllers.

Archivos relacionados:

- `subscriptions/interfaces/rest/VehicleSubscriptionsController.java`
- `assistance/interfaces/rest/AssistanceRequestsController.java`

Nota: esto cubre documentacion basica. Para maximizar rubrica se puede mejorar agregando `@ApiResponses`, ejemplos y `@Schema` en resources.

Ejemplo de correspondencia PDF -> codigo:

```java
@Tag(name = "Vehicle Subscriptions", description = "Vehicle subscription management endpoints")
@Operation(summary = "Create a vehicle subscription")
```

```java
@Tag(name = "Assistance Requests", description = "Assistance request management endpoints")
@Operation(summary = "Create an assistance request")
```

## 22. Comandos para compilar y ejecutar

Primero entra a la carpeta real del proyecto:

```powershell
cd "C:\Users\familia\Desktop\Nueva carpeta\DEOS-intento1\eb1122u202410837\eb1122u202410837"
```

Verifica que estas en la carpeta correcta:

```powershell
dir
```

Debe aparecer:

```text
mvnw.cmd
pom.xml
src
```

Crear schema en MySQL Workbench:

```sql
CREATE DATABASE IF NOT EXISTS toyota_connected_services;
```

Compilar:

```powershell
.\mvnw.cmd -DskipTests compile
```

Ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir Swagger:

```text
http://localhost:8096/swagger-ui/index.html
```

## 23. Requests de prueba

Crear `VehicleSubscription`:

```json
{
  "subscriptionCode": "TCS-20001",
  "vehicleVin": "JTDBR32E720999999",
  "startDate": "2026-01-01",
  "endDate": "2027-12-31"
}
```

Crear `AssistanceRequest`:

```json
{
  "subscriptionId": 1,
  "issueDescription": "Flat tire assistance required",
  "assistanceStatus": "REQUESTED",
  "requestedAt": "2026-07-17 10:30:00"
}
```

## 24. Checklist final contra el PDF

- MySQL apunta a `toyota_connected_services`.
- Existen `subscriptions`, `assistance` y `shared`.
- `Period` vive en `shared`.
- `VehicleSubscription` vive en `subscriptions`.
- `AssistanceRequest` vive en `assistance`.
- `subscriptionStatus` no se persiste; se calcula.
- `isEligibleForAssistance()` existe en el aggregate.
- Existe ACL entre `assistance` y `subscriptions`.
- Se emite `AssistanceRequestCreatedEvent`.
- El handler actualiza `lastAssistanceRequestId`.
- Hay `@Version` para optimistic locking.
- Hay seeding con `ApplicationReadyEvent`.
- Los endpoints POST devuelven `201 Created`.
- Los responses no exponen auditoria.
- Hay i18n basico en ingles y espanol.
- Hay documentacion OpenAPI basica.
