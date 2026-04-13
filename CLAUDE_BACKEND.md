# Backend Architecture

Framework:
Spring Boot

Language:
Java

Architecture:

Controller
Service
Repository

DTO-based design.

---

# Core Entities

User

Habit

HabitProgress

Streak

XP

---

# API Conventions

REST endpoints.

Examples:

GET /api/habits
POST /api/habits
PUT /api/habits/{id}
DELETE /api/habits/{id}

---

# Response Format

Success:

{
"data": {},
"message": "",
"timestamp": "",
"status": 200
}

Error:

{
"error": "",
"message": "",
"timestamp": ""
}

---

# Backend Responsibilities

The backend supports:

• habit CRUD
• daily completion logging
• streak calculation
• XP progression
• dashboard statistics

---

# Coding Rules

• controllers handle HTTP only
• services contain business logic
• repositories manage database access
• DTOs used instead of exposing entities
