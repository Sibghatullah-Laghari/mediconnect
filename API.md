# API Documentation - MediConnect

The MediConnect API is a RESTful service that exposes endpoints for authentication, profile management, and appointment scheduling.

---

## 🔐 Authentication

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user account | No |
| `POST` | `/api/v1/auth/login` | Authenticate a user and receive access tokens | No |
| `POST` | `/api/v1/auth/refresh` | Refresh the access token | No |
| `GET` | `/api/v1/auth/me` | Retrieve the current user's details | Yes |
| `POST` | `/api/v1/auth/send-otp` | Send an OTP to the user's email | No |
| `POST` | `/api/v1/auth/verify-otp` | Verify the OTP and activate the account | No |

---

## 🩺 Doctors

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/doctors` | Retrieve a paginated list of doctors | No |
| `GET` | `/api/v1/doctors/{id}` | Retrieve doctor details | No |
| `GET` | `/api/v1/doctors/specializations` | Retrieve all available specializations | No |
| `GET` | `/api/v1/doctors/specialization/{name}` | Retrieve doctors by specialization | No |
| `POST` | `/api/v1/doctors` | Create a doctor profile | Yes (Doctor) |
| `PUT` | `/api/v1/doctors/{id}` | Update a doctor profile | Yes (Owner/Admin) |

---

## 👤 Patients

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/patients/me` | Retrieve the current patient's profile | Yes (Patient) |
| `POST` | `/api/v1/patients` | Create a patient profile | Yes (Patient) |
| `PUT` | `/api/v1/patients/{id}` | Update a patient profile | Yes (Owner/Admin) |
| `GET` | `/api/v1/patients` | Retrieve a list of all patients | Yes (Admin) |

---

## 📅 Appointments

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/appointments` | Book a new appointment | Yes (Patient) |
| `GET` | `/api/v1/appointments` | Retrieve appointments based on the user's role | Yes |
| `GET` | `/api/v1/appointments/{id}` | Retrieve appointment details | Yes |
| `PUT` | `/api/v1/appointments/{id}/status` | Update the appointment status | Yes |
| `PATCH` | `/api/v1/appointments/{id}/confirm` | Confirm an appointment | Yes (Doctor/Admin) |
| `PATCH` | `/api/v1/appointments/{id}/complete` | Mark an appointment as completed | Yes (Doctor/Admin) |
| `PATCH` | `/api/v1/appointments/{id}/cancel` | Cancel an appointment | Yes |

---

## 🛠️ Global Responses

### Success (200 OK / 201 Created)

Successful requests return standard JSON responses containing the requested data.

### Error (4xx / 5xx)

MediConnect uses a standardized JSON error response format:

```json
{
  "timestamp": "2024-03-27T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for object...",
  "path": "/api/v1/appointments"
}
```

---

## 🚦 Rate Limiting

Authentication endpoints are protected by rate limiting to help prevent brute-force attacks. If the rate limit is exceeded, the API returns:

- **Status**: `429 Too Many Requests`
- **Body**: A standard error response with a "Too many requests" message.

---

## 📝 Pagination

Endpoints that return collections support pagination through query parameters:

- `page`: Page number (default: `0`)
- `size`: Number of items per page (default: `10`)
- `sort`: Sorting criteria (for example, `id,desc`)
