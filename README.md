# Real-Time Auction Platform

A full-stack real-time auction platform built with **ReactJS**, **Spring Boot**, and **PostgreSQL**, featuring live bidding via WebSockets, JWT authentication, and comprehensive exception handling.

---

## Tech Stack

### Backend
- **Java 17** + **Spring Boot 3.2.3**
- **Spring Security** + **JWT** (access & refresh tokens)
- **Spring WebSocket** (STOMP + SockJS) for real-time bidding
- **Spring Data JPA** + **PostgreSQL**
- **Spring Scheduling** for auction lifecycle management
- **Lombok** for boilerplate reduction

### Frontend
- **React 18** + **Vite 5**
- **Tailwind CSS 3.4** (custom design system)
- **Zustand** for state management
- **Axios** with JWT interceptors
- **STOMP.js + SockJS** for WebSocket client
- **React Router 6** for routing
- **React Hot Toast** for notifications

---

## Features

- **User Authentication**: Register, login, JWT-based sessions with auto-refresh
- **Real-Time Bidding**: Live price updates via WebSocket — all connected users see bids instantly
- **Auction Management**: Create, browse, search, filter by category, sort by various criteria
- **Snipe Protection**: Auctions auto-extend 5 minutes if a bid arrives in the last 30 seconds
- **Auto Lifecycle**: Scheduled jobs auto-start pending auctions and close expired ones
- **Notifications**: Real-time in-app notifications for outbid, auction won/ended events
- **Dashboard**: View your auctions, bids, won items, and notifications
- **Profile Management**: Edit profile, change password
- **Responsive UI**: Mobile-first design with Tailwind CSS
- **Exception Handling**: Global handler covering validation, auth, data integrity, and more

---

## Project Structure

```
FinalProject/
├── auction-backend/          # Spring Boot API
│   └── src/main/java/com/auction/
│       ├── config/           # Security, WebSocket config
│       ├── controller/       # REST & WebSocket controllers
│       ├── dto/              # Request/response DTOs
│       ├── entity/           # JPA entities
│       ├── exception/        # Custom exceptions + global handler
│       ├── repository/       # Spring Data repositories
│       ├── security/         # JWT, filters, user details
│       └── service/          # Business logic + scheduler
├── auction-frontend/         # React SPA
│   └── src/
│       ├── components/       # Navbar, Footer, AuctionCard, etc.
│       ├── pages/            # Home, Login, Auction Detail, Dashboard...
│       ├── services/         # Axios API, WebSocket service
│       └── store/            # Zustand auth store
└── docker-compose.yml        # One-command deployment
```

---

## Getting Started

### Prerequisites
- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 14+** (or Docker)
- **Maven 3.8+**

### 1. Database Setup

```sql
CREATE DATABASE auction_db;
```

Or use Docker:
```bash
docker run -d --name auction-db -p 5432:5432 \
  -e POSTGRES_DB=auction_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16-alpine
```

### 2. Backend

```bash
cd auction-backend
./mvnw spring-boot:run
```

The API starts at **http://localhost:8080**. Tables are auto-created via `ddl-auto=update`.

### 3. Frontend

```bash
cd auction-frontend
npm install
npm run dev
```

The app runs at **http://localhost:3000** and proxies API calls to the backend.

### 4. Docker Compose (Full Stack)

```bash
docker-compose up --build
```

This starts PostgreSQL, the Spring Boot backend, and the React frontend (served via Nginx).

---

## API Endpoints

### Auth
| Method | Endpoint               | Description     |
|--------|------------------------|-----------------|
| POST   | `/api/auth/register`   | Register        |
| POST   | `/api/auth/login`      | Login           |
| GET    | `/api/auth/me`         | Current user    |
| PUT    | `/api/auth/profile`    | Update profile  |
| PUT    | `/api/auth/password`   | Change password |

### Auctions
| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| GET    | `/api/auctions`                 | List all (paginated)     |
| GET    | `/api/auctions/{id}`            | Get by ID                |
| POST   | `/api/auctions`                 | Create auction (auth)    |
| PUT    | `/api/auctions/{id}`            | Update auction (owner)   |
| DELETE | `/api/auctions/{id}`            | Delete auction (owner)   |
| GET    | `/api/auctions/search?keyword=` | Search                   |
| GET    | `/api/auctions/category/{cat}`  | Filter by category       |
| GET    | `/api/auctions/featured`        | Featured auctions        |
| GET    | `/api/auctions/ending-soon`     | Ending within 1 hour     |
| GET    | `/api/auctions/my-auctions`     | User's auctions (auth)   |
| GET    | `/api/auctions/my-bids`         | Auctions user bid on     |
| GET    | `/api/auctions/won`             | Auctions user won        |

### Bids
| Method | Endpoint                   | Description         |
|--------|----------------------------|---------------------|
| POST   | `/api/bids`                | Place bid (auth)    |
| GET    | `/api/bids/auction/{id}`   | Bid history         |
| GET    | `/api/bids/my-bids`        | User's bid history  |

### Notifications
| Method | Endpoint                      | Description       |
|--------|-------------------------------|-------------------|
| GET    | `/api/notifications`          | All notifications |
| GET    | `/api/notifications/unread`   | Unread count      |
| PUT    | `/api/notifications/{id}/read`| Mark as read      |
| PUT    | `/api/notifications/read-all` | Mark all read     |
| DELETE | `/api/notifications/{id}`     | Delete             |

### WebSocket
- **Connect**: `ws://localhost:8080/ws` (SockJS)
- **Subscribe**: `/topic/auction/{id}` — live bid updates for a specific auction
- **Subscribe**: `/topic/auctions` — global auction events
- **Subscribe**: `/user/{username}/queue/notifications` — personal notifications
- **Send**: `/app/bid` — place bid via WebSocket

---

## Environment Variables

| Variable                        | Default                                      |
|--------------------------------|----------------------------------------------|
| `SPRING_DATASOURCE_URL`       | `jdbc:postgresql://localhost:5432/auction_db` |
| `SPRING_DATASOURCE_USERNAME`  | `postgres`                                    |
| `SPRING_DATASOURCE_PASSWORD`  | `postgres`                                    |
| `JWT_SECRET`                   | (configured in application.properties)       |
| `JWT_EXPIRATION`               | `86400000` (24 hours)                        |

---

## License

This project is for educational purposes.
