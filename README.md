# Hospital Management System

A microservices-based Hospital Management System for hospitals/clinics. It covers end‑to‑end flows including authentication, patient registration, doctor management, appointment booking with doctor availability, and billing/transactions with payment gateway integration.

## Architecture Overview

- api-gateway: Edge service (Spring) to route requests to internal services
- auth-service: User signup/login, JWT issuance and validation
- patient-service: CRUD for patients and lookup by user ID
- doctor-service: CRUD for doctors and search/list operations
- appointment-service: Booking and managing appointments; managing time slots; doctor availability search
- billing-service: Billing account details and payment transactions (create, verify, list, webhooks)
- analytics-service: Reserved for analytics (not implemented yet)

Service-to-service communication and contract boundaries are kept via DTOs. IDs are primarily UUIDs.


## Running Locally (High-level)

- Each service is a Spring Boot application with its own module.
- Ensure Java 17+ and Maven are installed.
- In each module, you can typically run: `mvn spring-boot:run`.
- Configure environment variables/properties for DB, message brokers, payment gateways, etc. as required by each service (consult each service's application.yml/properties).


## Authentication and Authorization

- Login returns a JWT.
- Protected endpoints should be called with header: `Authorization: Bearer <token>`.
- Token validation endpoint is provided by auth-service.
- Some services (e.g., billing) also read supplemental headers like `X-User-Role` for role-based behavior.


## Services and APIs

Below are the REST APIs grouped by service. Paths are relative to each service base URL. If routed via API Gateway, prefix and route rules may differ.


### 1) auth-service

Base: /

- POST /login
  - Summary: Authenticate user and receive JWT + user object
  - Request Body (application/json):
    ```json
    {
      "email": "user@example.com",
      "password": "string (min 8)"
    }
    ```
  - Responses:
    - 200 OK (application/json):
      ```json
      {
        "token": "<jwt>",
        "user": { "...": "user fields" }
      }
      ```
    - 401 Unauthorized if credentials are invalid

- GET /validate
  - Summary: Validate a JWT and return claims
  - Headers:
    Authorization: Bearer <token>
  - Responses:
    - 200 OK (application/json):
      ```json
      {
        "valid": true,
        "email": "subject@example.com",
        "role": "<role>",
        "userId": "<uuid>",
        "patientId": "<uuid>"
      }
      ```
    - 401 Unauthorized (application/json):
      ```json
      { "valid": false, "error": "..." }
      ```

- POST /signup
  - Summary: Register a new user (role PATIENT) and return JWT + user
  - Request Body (application/json):
    ```json
    {
      "name": "string",
      "email": "string",
      "password": "string (min 8)",
      "address": "string",
      "dateOfBirth": "YYYY-MM-DD",
      "gender": "MALE|FEMALE|OTHER",
      "phoneNumber": "string (<=10)",
      "bloodGroup": "A_POSITIVE|...",
      "emergencyContactNumber": "string (<=10)",
      "emergencyContactName": "string",
      "registeredDate": "YYYY-MM-DD"
    }
    ```
  - Response 201 Created (application/json):
    ```json
    {
      "token": "<jwt>",
      "user": { "...": "user fields" }
    }
    ```

### 2) patient-service

Base: /patients

- GET /
  - Summary: Get all patients
  - Response 200 OK (application/json): Array of PatientResponseDTO

- GET /{id}
  - Summary: Get patient by ID
  - Response 200 OK (application/json): PatientResponseDTO

- POST /
  - Summary: Create a new patient
  - Request Body (application/json): PatientRequestDTO
    ```json
    {
      "userId": "<uuid>",
      "name": "string",
      "email": "string",
      "address": "string",
      "dateOfBirth": "YYYY-MM-DD",
      "gender": "MALE|FEMALE|OTHER",
      "phoneNumber": "string (<=10)",
      "bloodGroup": "A_POSITIVE|...",
      "emergencyContactName": "string",
      "emergencyContactNumber": "string (<=10)",
      "registeredDate": "YYYY-MM-DD"
    }
    ```
  - Response 200 OK (application/json): PatientResponseDTO

- PUT /{id}
  - Summary: Update an existing patient
  - Request Body (application/json): PatientRequestDTO (registeredDate may be optional on update)
  - Response 200 OK (application/json): PatientResponseDTO

- DELETE /{id}
  - Summary: Delete an existing patient
  - Response 204 No Content

- GET /user/{userId}
  - Summary: Get patient details by user ID
  - Response 200 OK (application/json): PatientResponseDTO

PatientResponseDTO example:
```json
{
  "id": "<uuid>",
  "name": "John Doe",
  "email": "john@example.com",
  "address": "...",
  "dateOfBirth": "YYYY-MM-DD",
  "gender": "MALE",
  "userId": "<uuid>",
  "phoneNumber": "1234567890",
  "bloodGroup": "A_POSITIVE",
  "emergencyContactName": "Jane Doe",
  "emergencyContactNumber": "0987654321"
}
```


### 3) doctor-service

Base: /doctors

- POST /
  - Summary: Create a new doctor
  - Request Body (application/json): DoctorRequestDTO
  - Response 200 OK (application/json): DoctorDTO

- GET /{id}
  - Summary: Get doctor by ID
  - Response 200 OK (application/json): DoctorDTO

- GET /
  - Summary: Get all doctors
  - Response 200 OK (application/json): Array of DoctorDTO

- PUT /{id}
  - Summary: Update doctor by ID
  - Request Body (application/json): DoctorRequestDTO
  - Response 200 OK (application/json): DoctorDTO

- DELETE /{id}
  - Summary: Delete doctor by ID
  - Response 204 No Content

- POST /batch
  - Summary: Get multiple doctors by IDs
  - Request Body (application/json): [ "<uuid>", "<uuid>" ]
  - Response 200 OK (application/json): Array of DoctorDTO

- GET /search?specialty={specialty}&name={name}
  - Summary: Search doctors by specialty and/or name
  - Response 200 OK (application/json): Array of DoctorDTO

DoctorDTO example:
```json
{
  "doctorId": "<uuid>",
  "name": "Dr. Alice Smith",
  "specialty": "Cardiology",
  "pricePerSession": 500.0,
  "email": "alice@hospital.com",
  "phone": "1234567890",
  "avatar": "https://...",
  "qualifications": "MBBS, MD",
  "experienceMonths": 120
}
```


### 4) appointment-service

Base: /appointments and /timeslots and /appointment-slots

Appointments
- POST /appointments
  - Summary: Book an appointment
  - Request Body (application/json): AppointmentRequestDTO
    ```json
    {
      "patientId": "<uuid>",
      "doctorId": "<uuid>",
      "date": "YYYY-MM-DD",
      "time": "HH:mm",
      "reasonForVisit": "string",
      "visitType": "IN_PERSON|VIRTUAL"
    }
    ```
  - Response 201 Created (application/json): AppointmentResponseDTO

- PUT /appointments/{appointmentId}
  - Summary: Cancel an appointment
  - Response 204 No Content

- GET /appointments/{appointmentId}
  - Summary: Get appointment by ID
  - Response 200 OK (application/json): AppointmentResponseDTO

- GET /appointments/patient/{patientId}
  - Summary: List appointments by patient ID
  - Response 200 OK (application/json): Array of AppointmentResponseDTO

AppointmentResponseDTO example (fields):
```json
{
  "appointmentId": "<uuid>",
  "patientId": "<uuid>",
  "doctorId": "<uuid>",
  "doctorName": "Dr. Alice Smith",
  "doctorSpecialty": "Cardiology",
  "doctorAvatar": "https://...",
  "appointmentDateTime": "2025-10-01T10:30:00",
  "status": "SCHEDULED|CANCELLED|COMPLETED",
  "type": "IN_PERSON|VIRTUAL",
  "reasonForVisit": "string",
  "createdAt": "2025-09-30T12:00:00",
  "amount": 500.0,
  "paymentStatus": "PENDING|PAID|FAILED",
  "transactionId": "<uuid>"
}
```

Time Slots
- POST /timeslots
  - Summary: Create a time slot (doctor availability)
  - Request Body (application/json): TimeSlotRequestDTO
    ```json
    {
      "doctorId": "<uuid>",
      "date": "YYYY-MM-DD",
      "startTime": "HH:mm",
      "endTime": "HH:mm"
    }
    ```
  - Response 201 Created (application/json): TimeSlotResponseDTO

- GET /timeslots/{id}
  - Summary: Get a time slot by ID
  - Response 200 OK (application/json): TimeSlotResponseDTO

- GET /timeslots
  - Summary: List all time slots
  - Response 200 OK (application/json): Array of TimeSlotResponseDTO

- PUT /timeslots/{id}
  - Summary: Update a time slot
  - Request Body: TimeSlotRequestDTO
  - Response 200 OK (application/json): TimeSlotResponseDTO

- DELETE /timeslots/{id}
  - Summary: Delete a time slot
  - Response 204 No Content

Doctor Availability Search
- GET /appointment-slots/search?speciality={speciality}&name={name}
  - Summary: Search doctors with available time slots
  - Response 200 OK (application/json): Array of DoctorTimeSlotsDTO

DoctorTimeSlotsDTO example:
```json
{
  "doctorId": "<uuid>",
  "doctorName": "Dr. Alice Smith",
  "doctorSpecialty": "Cardiology",
  "doctorAvatar": "https://...",
  "doctorPricePerSession": 500.0,
  "timeSlots": [ { "id": "<uuid>", "date": "YYYY-MM-DD", "startTime": "HH:mm", "endTime": "HH:mm", "status": "AVAILABLE|BOOKED" } ]
}
```


### 5) billing-service

Base: /billing and /billing/transactions

Billing
- GET /billing/{patientId}
  - Summary: Get billing account details for a patient
  - Response 200 OK (application/json):
    ```json
    {
      "patientId": "<uuid>",
      "balance": 0.0,
      "status": "ACTIVE|SUSPENDED"
    }
    ```

Transactions
- POST /billing/transactions
  - Summary: Create a transaction (initiates payment)
  - Headers: Optionally `X-User-Role: ADMIN|PATIENT|...`
  - Request Body (application/json): TransactionRequestDTO
    ```json
    {
      "patientId": "<uuid>",
      "appointmentId": "<uuid>",
      "description": "string (<=256)",
      "paymentMethod": "CARD|UPI|NET_BANKING|...",
      "paymentType": "CONSULTATION|...",
      "paymentGateway": "RAZORPAY|..."
    }
    ```
  - Response 200 OK (application/json): TransactionResponseDTO
    ```json
    {
      "gatewayOrderDetails": { "orderId": "..." },
      "transactionId": "<uuid>",
      "amount": 500.0,
      "billingAccount": "<uuid>",
      "status": "PENDING|SUCCESS|FAILED",
      "paymentMethod": "CARD",
      "paymentGateway": "RAZORPAY"
    }
    ```

- POST /billing/transactions/verify
  - Summary: Verify a payment with gateway callback data
  - Request Body (application/json): VerifyPaymentRequestDTO
    ```json
    {
      "transactionId": "<uuid>",
      "paymentGateway": "RAZORPAY|...",
      "paymentId": "string",
      "orderId": "string",
      "signature": "string"
    }
    ```
  - Response 200 OK (application/json): TransactionResponseDTO

- POST /billing/transactions/webhooks/{gateway}
  - Summary: Receive webhook events from payment gateways
  - Path Variable: gateway (e.g., RAZORPAY)
  - Request Body: raw string payload
  - Headers: Will be forwarded to service for signature verification
  - Response 200 OK (text/plain): "Payment processed successfully"

- GET /billing/transactions/{transactionId}
  - Summary: Get transaction by ID
  - Response 200 OK (application/json): TransactionResponseDTO

- GET /billing/transactions/all/{patientId}?page=0&size=10
  - Summary: Paginated list of a patient’s transactions
  - Response 200 OK (application/json): TransactionListResponseDTO
    ```json
    {
      "transactions": [ { "id": "...", "description": "...", "amount": 500.0, "status": "SUCCESS", "timestamp": "...", "paymentType": "CONSULTATION" } ],
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 1,
      "totalPages": 1,
      "lastPage": true
    }
    ```
