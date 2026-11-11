# QuizDuel — Backend (Spring Boot)

REST API for the QuizDuel React app. Spring Boot 3, Spring MVC controllers, `JdbcTemplate`, an embedded
H2 database (no installation), BCrypt password hashing and JWT login. Same endpoints and JSON as the
React app expects, so the frontend needs no changes.

## Run it

Requires **JDK 17 or newer** and Maven (IntelliJ bundles Maven, so you don't need to install it).

**IntelliJ:** open the `springboot-backend` folder → let Maven import → run `QuizDuelApplication`.

**Command line:**
```bash
cd springboot-backend
mvn spring-boot:run
```

It starts on **http://localhost:5000** (so the React app's default API URL works). On the first start it
creates `data/quizduel.mv.db` and inserts demo data. Delete the `data/` folder to start fresh.

Then start the frontend as usual (`npm run dev` in the project root → http://localhost:5173).

Demo logins (password `password123`):

| Who | Email |
|---|---|
| Teacher | `teacher@school.edu.ph` |
| Students | `ana@`, `miguel@`, `ella@`, `liza@`, `paolo@`, `carlo@`, `nina@`, `joy@` + `school.edu.ph` |

The demo section's join code is `MABINI`.

## Project structure

```
springboot-backend/
├── pom.xml
└── src/main/
    ├── resources/
    │   ├── application.properties   port, database, JWT secret, CORS origin, time zone
    │   └── schema.sql               tables (follows the ERD), runs on every start
    └── java/com/quizduel/
        ├── QuizDuelApplication.java
        ├── config/       AppConfig (BCrypt), WebConfig (CORS), GameRules (points), DataInitializer (seed on start)
        ├── controller/   Auth, Section, Question, Battle, Insight, Health  → thin HTTP layer
        ├── service/      AuthService, SectionService, QuestionService, BattleService, InsightService, DemoDataService
        ├── dto/          request/response records (the JSON shapes)
        ├── security/     JwtService (HS256), AuthGuard (reads the Bearer token), CurrentUser
        ├── exception/    ApiException + GlobalExceptionHandler  → every error is {"error": "message"}
        └── util/         Validation, Jdbc, AppClock, SectionScope
```

Each controller method starts with `guard.require(auth)` or `guard.requireRole(auth, "teacher")`, then calls
a service. The SQL lives in the services.

## Endpoints

Send `Authorization: Bearer <token>` on everything except register/login.

| Method | Path | Who | What |
|---|---|---|---|
| POST | `/api/auth/register` | public | `{name, email, password, school?, role?}` — role is `teacher` (default) or `student` |
| POST | `/api/auth/login` | public | `{email, password}` → `{token, user}` |
| GET | `/api/auth/me` | any | current user |
| GET | `/api/sections` | any | teacher: own sections; student: joined sections |
| POST | `/api/sections` | teacher | `{name, gradeLevel?, subject?}` → creates section + join code |
| POST | `/api/sections/join` | student | `{joinCode}` |
| PUT | `/api/sections/{id}` | teacher | edit / Settings: `{battleTime:"15:00", weekendBattles:true}` |
| GET | `/api/sections/{id}/roster` | teacher | students with points + badge count |
| POST | `/api/sections/{id}/roster/{studentId}/badges` | teacher | `{badgeId}` — manual badge award |
| GET | `/api/questions?sectionId=&topic=` | teacher | question bank |
| GET | `/api/questions/topics?sectionId=` | teacher | distinct topics |
| POST | `/api/questions` | teacher | `{sectionId, topic, text, choices:{A,B,C,D}, correctAnswer, difficulty?}` |
| PUT / DELETE | `/api/questions/{id}` | teacher | edit / remove |
| POST | `/api/battles/generate` | teacher | `{sectionId}` — run today's random matchmaking |
| GET | `/api/battles/today?sectionId=` | teacher | Today's Battles |
| GET | `/api/battles/log?sectionId=&limit=` | teacher | Battle Results Log |
| GET | `/api/battles/me/today` | student | my battles today |
| GET | `/api/battles/{id}` | player / teacher | battle + questions (answers hidden from students until it's finished) |
| POST | `/api/battles/{id}/answers` | student | `{answers:[{questionId, selected:"A", timeTaken}]}` |
| GET | `/api/dashboard?sectionId=` | teacher | stats + topic mastery + today's battles |
| GET | `/api/analytics?sectionId=` | teacher | average score, battles today, active students, topic mastery |
| GET | `/api/leaderboard?sectionId=` | any | ranked students (students must pass a section they joined) |
| GET | `/api/badges` | any | all badges (students also see `earned`) |
| POST | `/api/badges` | teacher | `{name, description?}` — custom badge |

`sectionId` is optional for teachers on list endpoints: leave it out to cover all of your sections.

## Game rules (`config/GameRules.java` and `service/BattleService.java`)

- **Matchmaking:** `POST /api/battles/generate` pairs enrolled students at random. An odd student out plays the
  Practice Bot. It picks one topic of the day and draws up to 10 questions from it for each battle. Running it twice
  on the same day does nothing the second time.
- **Weekends:** blocked unless the section has `weekendBattles` switched on (Settings page).
- **"Today"** is calculated in `app.timezone` (default `Asia/Manila`).
- **Scoring:** higher score wins. A tie between two students is broken by faster total time, otherwise it's a draw.
  The Practice Bot answers 50–90% correctly.
- **Points:** winner gets 50 (25 for beating the bot) and a streak +1. The loser's streak resets (not on a draw).
- **Badges:** awarded automatically at 50 / 150 / 300 / 500 points; teachers can also create and hand out custom ones.
- Teachers only ever see their own sections.

## Settings (`application.properties`, or environment variables)

| Property | Env var | Default |
|---|---|---|
| `server.port` | — | `5000` |
| `app.jwt-secret` | `JWT_SECRET` | a dev-only value — **set your own** for anything real |
| `app.client-origin` | `CLIENT_ORIGIN` | `http://localhost:5173,http://127.0.0.1:5173` (comma-separated) |
| `app.timezone` | `APP_TIMEZONE` | `Asia/Manila` |
| `app.seed-demo-data` | `SEED_DEMO_DATA` | `true` |

If you change the port, set `VITE_API_URL` for the frontend (e.g. `VITE_API_URL=http://localhost:8080/api` in a `.env` file).

## Using MySQL instead of H2

1. In `pom.xml` replace the `h2` dependency with `com.mysql:mysql-connector-j` (runtime scope).
2. In `application.properties`:
   ```
   spring.datasource.url=jdbc:mysql://localhost:3306/quizduel?createDatabaseIfNotExist=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   ```
`schema.sql` uses plain SQL that MySQL 8 accepts, but it has only been run against H2.
