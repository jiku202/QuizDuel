# QuizDuel

Daily random quiz battles for Grade 3 Araling Panlipunan (Philippine History).

```
QuizDuel/
├── src/                  React + Vite teacher web app
└── springboot-backend/   Spring Boot REST API (H2 database, JWT login)
```

## Run it (two terminals)

**1. Backend** — needs JDK 17+ and Maven (IntelliJ bundles Maven):
```bash
cd springboot-backend
mvn spring-boot:run          # http://localhost:5000
```
(or open `springboot-backend` in IntelliJ and run `QuizDuelApplication`)

**2. Frontend:**
```bash
npm install
npm run dev                  # http://localhost:5173
```

Log in as `teacher@school.edu.ph` / `password123` (demo data is created on the first backend start).
Keep the frontend on port 5173 — the API only accepts requests from that origin
(change `app.client-origin` in `springboot-backend/src/main/resources/application.properties` if needed).

See `springboot-backend/README.md` for the endpoint list, game rules and settings.
