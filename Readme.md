# 🏠 Pravesh — Intelligent Access Control System for Gated Communities

<p align="center">

**A secure, smart and centralized digital platform for managing gated communities, residents, visitors, security operations and society administration.**

Built with **Spring Boot + React + MySQL**, Pravesh brings everyday society operations into a single modern platform.

</p>

---

## 📌 Overview

**Pravesh** is an intelligent access control and society-management platform designed to digitize and simplify the operations of gated residential communities.

Traditional gated communities often rely on manual visitor registers, phone calls, paper-based approvals and disconnected administrative processes. Pravesh provides a centralized solution where **residents, security guards, society administrators and super administrators** can interact through role-based dashboards.

The platform combines:

- 🔐 Secure authentication & authorization
- 👥 Resident and user management
- 🚪 Gate & visitor management
- 🎫 Digital visitor passes
- 🚨 Real-time SOS emergency alerts
- 💳 Maintenance payments
- 📊 Society analytics
- 💬 Community forum
- 🤖 AI-powered assistant
- 📱 Email & SMS notifications
- 🔄 Resident relocation management
- 🚌 Community trip coordination
- 📷 QR-based pass validation

The current implementation uses a **modular monolithic Spring Boot architecture**, providing the simplicity of a single deployable application while maintaining clear separation between business modules.

---

# ✨ Key Features

## 🔐 Authentication & Security

- JWT-based authentication
- Secure password hashing
- Role-based access control
- Protected frontend routes
- Registration OTP verification
- Forgot password & password reset
- Secure authentication filters
- Multiple user roles

### Supported Roles

| Role | Responsibility |
|---|---|
| 👑 `SUPER_ADMIN` | Platform-level administration |
| 🏢 `SOCIETY_ADMIN` | Society management and administration |
| 🛡️ `GUARD` | Gate and visitor security operations |
| 🏠 `RESIDENT` | Resident services and community access |

---

## 🏠 Resident Management

Residents can:

- Register and complete onboarding
- View and manage their profile
- Access society information
- Create visitor passes
- View active and previous passes
- Track visitor entries
- Request relocation
- Make maintenance payments
- View payment history
- Participate in community forums
- Join community trips
- Raise emergency SOS alerts

---

## 🚪 Smart Gate & Visitor Management

Pravesh digitizes the complete visitor-entry workflow.

### Visitor Flow

```text
Resident
   │
   ▼
Create Visitor Pass
   │
   ▼
QR Code Generated
   │
   ▼
Visitor Arrives at Gate
   │
   ▼
Guard Scans QR
   │
   ▼
Pass Validation
   │
   ├── Valid ──────► Entry Approved
   │
   └── Invalid ────► Entry Rejected
                         │
                         ▼
                    Entry Logged
```

Features include:

- Digital visitor passes
- QR code generation
- QR code scanning
- Pass validation
- Walk-in entry logging
- Entry history
- Gate management
- Visitor tracking
- Pass expiry handling
- Recurring passes

---

# 🚨 SOS Emergency System

Pravesh includes an emergency assistance module designed to quickly notify relevant users when an emergency is raised.

### Emergency Categories

- 🏥 Medical
- 🔥 Fire
- 🛡️ Security Threat
- 🚗 Accident
- 👶 Child Emergency
- 👴 Senior Citizen Assistance
- ⚠️ Other

### SOS Lifecycle

```text
SOS Raised
    │
    ▼
ACTIVE
    │
    ▼
ACKNOWLEDGED
    │
    ▼
HELP_ON_THE_WAY
    │
    ▼
RESOLVED
```

The system supports **real-time notifications using WebSocket and STOMP**, allowing emergency updates to be delivered without requiring constant page refreshes.

---

# 💳 Payment Management

Residents can manage society-related payments through the platform.

### Features

- Maintenance payment
- Payment order creation
- Razorpay integration
- Payment verification
- Transaction history
- Payment status tracking
- Admin payment monitoring
- Webhook support

```text
Resident
   │
   ▼
Create Payment Order
   │
   ▼
Razorpay Checkout
   │
   ▼
Payment
   │
   ▼
Verification
   │
   ▼
Transaction Recorded
```

---

# 📊 Analytics & Dashboards

Society administrators get centralized operational insights.

Analytics include:

- Visitor statistics
- Gate activity
- Entry trends
- Hourly visitor counts
- Daily visitor counts
- Denied-entry statistics
- Operational summaries

The frontend provides dedicated dashboards for different roles.

---

# 💬 Community Forum

Residents can communicate with their society community through the forum module.

Features include:

- Create posts
- View posts
- Add comments
- Participate in discussions
- Community interaction

This helps transform Pravesh from a simple security application into a broader **digital community platform**.

---

# 🚌 Community Trips

Residents can coordinate community trips through the platform.

Features include:

- Create/propose trips
- View available trips
- Join trips
- Trip details
- Trip comments
- Participant management
- Trip status management

---

# 🤖 AI Assistant

Pravesh integrates a **Gemini-powered AI assistant** to provide an interactive assistant experience inside the application.

The assistant can be accessed directly from the application interface and is designed to help users interact with the platform more conveniently.

---

# 📱 Notification System

Pravesh supports multiple notification channels.

### Email

Implemented using:

- Spring Boot Mail
- Gmail SMTP

### SMS

Implemented using:

- Twilio

### Real-Time

Implemented using:

- Spring WebSocket
- STOMP
- SockJS

This allows the application to support both traditional notifications and real-time event-driven updates.

---

# 🏗️ Architecture

Pravesh currently follows a **Modular Monolithic Architecture**.

```text
                         ┌─────────────────────┐
                         │    React Frontend   │
                         │   React + Bootstrap  │
                         └──────────┬──────────┘
                                    │
                              REST / WebSocket
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot Monolith                       │
│                                                             │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────┐   │
│  │    Auth    │  │   Users    │  │ Society Management   │   │
│  └────────────┘  └────────────┘  └──────────────────────┘   │
│                                                             │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────┐   │
│  │ Gate/Entry │  │   Passes   │  │    SOS / Emergency   │   │
│  └────────────┘  └────────────┘  └──────────────────────┘   │
│                                                             │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────┐   │
│  │ Payments   │  │ Analytics  │  │ Forum / Community    │   │
│  └────────────┘  └────────────┘  └──────────────────────┘   │
│                                                             │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────┐   │
│  │ Trips      │  │ Assistant  │  │ Notifications        │   │
│  └────────────┘  └────────────┘  └──────────────────────┘   │
│                                                             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │      MySQL      │
                  │    Database     │
                  └─────────────────┘
```

---

# 📂 Backend Architecture

The backend follows a layered Spring Boot architecture:

```text
pravesh-backend/
│
├── src/main/java/com/pravesh/
│   │
│   ├── config/
│   ├── controller/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   │
│   ├── entity/
│   │   └── enums/
│   │
│   ├── exception/
│   ├── repository/
│   ├── scheduler/
│   ├── security/
│   ├── service/
│   │
│   └── PraveshApplication.java
│
└── src/main/resources/
    └── application.properties
```

### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| Controller | REST API endpoints |
| Service | Business logic |
| Repository | Database operations |
| Entity | JPA database models |
| DTO | API request/response models |
| Security | JWT authentication & authorization |
| Exception | Global exception handling |
| Scheduler | Background scheduled operations |
| Config | Application configuration |

---

# 🎨 Frontend Architecture

The frontend is built using React and follows a component/page-based architecture.

```text
pravesh-frontend/
│
├── src/
│   ├── components/
│   │   └── common/
│   │
│   ├── context/
│   │
│   ├── pages/
│   │   ├── admin/
│   │   ├── auth/
│   │   ├── forum/
│   │   ├── public/
│   │   ├── resident/
│   │   └── guard/
│   │
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
│
├── public/
├── package.json
└── vite.config.js
```

The frontend uses **React Router** for navigation and protected routes based on authenticated user roles.

---

# 🛠️ Technology Stack

## Backend

| Technology | Purpose |
|---|---|
| ☕ Java 21 | Programming language |
| 🌱 Spring Boot | Backend framework |
| 🔐 Spring Security | Authentication & authorization |
| 🎟️ JWT | Stateless authentication |
| 🗃️ Spring Data JPA | Persistence layer |
| 🐬 MySQL | Relational database |
| 📡 WebSocket | Real-time communication |
| 🔄 STOMP | WebSocket messaging |
| 📧 Spring Mail | Email notifications |
| 📱 Twilio | SMS notifications |
| 💳 Razorpay | Payment processing |
| 📷 ZXing | QR code generation |
| 🤖 Google Gemini | AI assistant |
| 📊 Spring Actuator | Application monitoring |
| 🧪 JUnit / Spring Test | Testing |
| 🧰 Maven | Build & dependency management |

## Frontend

| Technology | Purpose |
|---|---|
| ⚛️ React 18 | UI development |
| ⚡ Vite | Frontend build tool |
| 🎨 Bootstrap 5 | UI styling |
| 🧭 React Router | Routing |
| 🔗 Axios | API communication |
| 📡 STOMP.js | WebSocket communication |
| 🔄 SockJS | WebSocket fallback |
| 📷 HTML5 QR Code | QR scanning |
| 📊 Recharts | Data visualization |

---

# 🔒 Security Model

Pravesh follows a role-based security model.

```text
                    ┌──────────────┐
                    │   JWT Token  │
                    └──────┬───────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │ JWT Authentication│
                  │      Filter       │
                  └────────┬─────────┘
                           │
                           ▼
                    Authenticated User
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
        SUPER_ADMIN  SOCIETY_ADMIN     GUARD
                           │
                           ▼
                        RESIDENT
```

Protected resources are controlled using role-based authorization at both the frontend and backend levels.

---

# 🗄️ Database

Pravesh uses **MySQL with Spring Data JPA / Hibernate**.

Major domain entities include:

- User
- Resident
- Society
- Society Admin
- Flat
- Gate
- Guard
- Guard Shift
- Visitor Pass
- Entry Log
- Gate Entry Request
- SOS Alert
- SOS Status History
- Payment Order
- Forum Post
- Trip
- Trip Comment
- Trip Join Request
- Resident Relocation
- Password Reset Token
- Registration Verification

The database schema is managed using JPA/Hibernate.

---

# 🚀 Getting Started

## Prerequisites

Make sure the following are installed:

- Java 21+
- Maven
- Node.js 18+
- npm
- MySQL 8+
- Git

Optional services depending on enabled features:

- Gmail SMTP
- Twilio
- Razorpay
- Google Gemini

---

# 📥 Clone the Repository

```bash
git clone https://github.com/<your-username>/<your-repository>.git

cd <your-repository>
```

---

# ⚙️ Backend Setup

Navigate to the backend:

```bash
cd pravesh-backend
```

Create the MySQL database:

```sql
CREATE DATABASE praveshmonolith;
```

Configure the required environment variables.

### Required Environment Variables

```text
DB_PASSWORD
Jwt__Key

GMAIL_USERNAME
GMAIL_APP_PASSWORD

TWILIO_ACCOUNT_SID
TWILIO_AUTH_TOKEN
TWILIO_PHONE_NUMBER

PRAVESH_APP_LINK

RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET
RAZORPAY_WEBHOOK_SECRET

GEMINI_API_KEY

SUPERADMIN_EMAIL
SUPERADMIN_PASSWORD
```

Build the backend:

```bash
mvn clean install
```

Run the application:

```bash
mvn spring-boot:run
```

Backend will start on:

```text
http://localhost:8080
```

---

# 🎨 Frontend Setup

Open a new terminal:

```bash
cd pravesh-frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will normally be available at:

```text
http://localhost:5173
```

---

# 🔗 Application Flow

```text
                    ┌────────────────┐
                    │     Client     │
                    │ React Frontend │
                    └───────┬────────┘
                            │
                 REST API / WebSocket
                            │
                            ▼
                  ┌──────────────────┐
                  │  Spring Boot API │
                  └────────┬─────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
      MySQL            Razorpay         External APIs
                       Twilio           Gemini / SMTP
```

---

# 👥 User Journey

### 🏠 Resident

```text
Register
   ↓
OTP Verification
   ↓
Onboarding
   ↓
Society Approval
   ↓
Resident Dashboard
   ↓
Use Society Services
```

### 🛡️ Guard

```text
Login
   ↓
Guard Dashboard
   ↓
Check Shift
   ↓
Scan Visitor Pass
   ↓
Validate Entry
   ↓
Record Gate Entry
```

### 🏢 Society Admin

```text
Login
   ↓
Society Dashboard
   ↓
Manage Users
   ↓
Manage Flats / Gates / Guards
   ↓
Manage Onboarding
   ↓
Monitor Entries
   ↓
View Analytics
```

### 👑 Super Admin

```text
Login
   ↓
Super Admin Dashboard
   ↓
Manage Society Administration
   ↓
Review Society Requests
   ↓
Platform-level Management
```

---

# 📡 Real-Time Communication

Pravesh uses WebSocket communication for time-sensitive events.

```text
Frontend
   │
   │ WebSocket / STOMP
   ▼
Spring Boot WebSocket
   │
   ▼
Event / Notification
   │
   ├── SOS Alert
   ├── Status Update
   └── Real-Time Notification
```

This is especially useful for emergency SOS workflows where users should receive updates immediately.

---

# ⏰ Scheduled Operations

The backend includes scheduled processes for automated operations such as:

- Visitor pass expiry
- Recurring pass processing
- Other time-based business operations

This reduces the need for manual administrative intervention.

---

# 🧪 Testing

Run backend tests using:

```bash
mvn test
```

The project uses:

- Spring Boot Test
- Spring Security Test
- JUnit

---

# 📈 Future Enhancements

The architecture is designed to allow future expansion without requiring an immediate move to microservices.

Potential future enhancements:

- 📱 Native Android/iOS application
- ☁️ AWS deployment
- 🐳 Docker containerization
- 📊 Advanced analytics
- 🔔 Push notifications
- 🧠 More advanced AI capabilities
- 📹 CCTV integration
- 🪪 Face recognition for access verification
- 🔎 Advanced audit logging
- ⚙️ CI/CD pipeline
- 📦 Redis caching
- 📨 Event-driven architecture with Kafka when scale requires it

---

# 🎯 Why Pravesh?

Pravesh is more than a visitor-management application.

It combines **security, community management, emergency response, payments and digital society operations** into a single platform.

### The goal is simple:

> **Make gated-community management smarter, safer and easier.**

---

# 🧑‍💻 Development Approach

The project follows software engineering practices including:

- Layered architecture
- DTO-based API design
- RESTful APIs
- Role-based access control
- Centralized exception handling
- Validation
- Repository abstraction
- Secure authentication
- Modular business domains
- Separation of frontend and backend
- Environment-based configuration

---

# 📜 License

This project is developed for **academic, learning and demonstration purposes**.

---

# 👨‍💻 Team

**Pravesh — Intelligent Access Control System for Gated Communities**

Developed as a collaborative software engineering project using modern full-stack technologies.

---

<p align="center">

### 🏠 Pravesh
**Secure Gates. Smarter Communities. Better Living.**

⭐ If you find this project useful, consider giving the repository a star!

</p>
