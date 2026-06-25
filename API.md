# API Documentation - MediConnect

The MediConnect API is a RESTful service that provides endpoints for authentication, profile management, and appointment scheduling.

---

## 🔐 Authentication

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user | No |
| `POST` | `/api/v1/auth/login` | Authenticate and receive tokens | No |
| `POST` | `/api/v1/auth/refresh` | Refresh access token | No |
| `GET` | `/api/v1/auth/me` | Get current user details | Yes |
| `POST` | `/api/v1/auth/send-otp` | Send OTP to email | No |
| `POST` | `/api/v1/auth/verify-otp` | Verify OTP and enable account | No |

---

## 🩺 Doctors

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/doctors` | List all doctors (paginated) | No |
| `GET` | `/api/v1/doctors/{id}` | Get doctor details | No |
| `GET` | `/api/v1/doctors/specializations` | Get all specializations | No |
| `GET` | `/api/v1/doctors/specialization/{name}`| Filter doctors by specialization | No |
| `POST` | `/api/v1/doctors` | Create doctor profile | Yes (Doctor) |
| `PUT` | `/api/v1/doctors/{id}` | Update doctor profile | Yes (Owner/Admin) |

---

## 👤 Patients

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/patients/me` | Get current patient profile | Yes (Patient) |
| `POST` | `/api/v1/patients` | Create patient profile | Yes (Patient) |
| `PUT` | `/api/v1/patients/{id}` | Update patient profile | Yes (Owner/Admin) |
| `GET` | `/api/v1/patients` | List all patients | Yes (Admin) |

---

## 📅 Appointments

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/appointments` | Book a new appointment | Yes (Patient) |
| `GET` | `/api/v1/appointments` | List appointments (role-scoped) | Yes |
| `GET` | `/api/v1/appointments/{id}` | Get appointment details | Yes |
| `PUT` | `/api/v1/appointments/{id}/status` | Update appointment status | Yes |
| `PATCH` | `/api/v1/appointments/{id}/confirm` | Confirm appointment | Yes (Doctor/Admin)|
| `PATCH` | `/api/v1/appointments/{id}/complete`| Mark appointment as completed | Yes (Doctor/Admin)|
| `PATCH` | `/api/v1/appointments/{id}/cancel` | Cancel appointment | Yes |

---

## 🛠️ Global Responses

### Success (200 OK / 201 Created)
Standard JSON responses with the requested data.

### Error (4xx / 5xx)
MediConnect uses a standardized error format:
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
Authentication endpoints are rate-limited to prevent brute-force attacks. If the limit is exceeded, the API returns:
- **Status**: `429 Too Many Requests`
- **Body**: Standard error response with "Too many requests" message.

---

## 📝 Pagination
Endpoints that return lists support pagination via query parameters:
- `page`: Page number (default: 0)
- `size`: Items per page (default: 10)
- `sort`: Sorting criteria (e.g., `id,desc`)
