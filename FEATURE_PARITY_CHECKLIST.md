# QuizDuel Feature Parity Checklist

This document tracks feature parity between the existing React web application, the Spring Boot backend, and the native Android application.

## Legend
- **IMPLEMENTED AND TESTED**: Fully developed in Android and verified against the backend.
- **IMPLEMENTED BUT NOT TESTED**: Code developed in Android, pending emulator or live verification.
- **BLOCKED BY MISSING BACKEND FUNCTIONALITY**: Backend lacks an endpoint or logic for this feature.
- **NOT IMPLEMENTED**: Feature planned but not yet implemented.

---

## 1. Authentication & Session Management

| Feature | Web Support | Backend Support | Android Status | Notes |
| :--- | :---: | :---: | :---: | :--- |
| Teacher Login | Yes | `POST /api/auth/login` | IMPLEMENTED BUT NOT TESTED | JWT stored securely |
| Student Login | Rejected in Web (mobile only) | `POST /api/auth/login` | IMPLEMENTED BUT NOT TESTED | Native mobile feature |
| Teacher Registration | Yes | `POST /api/auth/register` | IMPLEMENTED BUT NOT TESTED | Sets `role="teacher"` |
| Student Registration | No (Web restricts to teacher) | `POST /api/auth/register` | IMPLEMENTED BUT NOT TESTED | Sets `role="student"` |
| Session Restoration | Yes | `GET /api/auth/me` | IMPLEMENTED BUT NOT TESTED | Validates token on app launch |
| Logout & Clear Session | Yes | Client-side token removal | IMPLEMENTED BUT NOT TESTED | Clears secure storage |
| Forgot Password | No | Not implemented in backend | BLOCKED BY MISSING BACKEND FUNCTIONALITY | Documented limitation; backend has no password reset endpoint |

---

## 2. Student Features

| Feature | Web Support | Backend Support | Android Status | Notes |
| :--- | :---: | :---: | :---: | :--- |
| Student Dashboard | No | `GET /api/sections`, `GET /api/battles/me/today`, `GET /api/auth/me` | IMPLEMENTED BUT NOT TESTED | Shows stats, sections, battles |
| Join Section via Join Code | No | `POST /api/sections/join` | IMPLEMENTED BUT NOT TESTED | Dialog with instant join code validation |
| View Joined Sections | No | `GET /api/sections` | IMPLEMENTED BUT NOT TESTED | Lists enrolled classes |
| View Today's Battles | No | `GET /api/battles/me/today` | IMPLEMENTED BUT NOT TESTED | Shows pending/completed battles |
| Interactive Quiz Participation | No | `GET /api/battles/{id}` | IMPLEMENTED BUT NOT TESTED | 4-choice questions, per-question timer |
| Submit Quiz Answers | No | `POST /api/battles/{id}/answers` | IMPLEMENTED BUT NOT TESTED | Evaluates answers, returns score & updated battle |
| View Battle Results & Score | No | Returned from submit or `GET /api/battles/{id}` | IMPLEMENTED BUT NOT TESTED | Shows score, winner, correct answers if completed |
| View Section Leaderboard | No (Student role) | `GET /api/leaderboard?sectionId=` | IMPLEMENTED BUT NOT TESTED | Shows rank, points, streak |
| View Rank Badges & Tiers | No | `GET /api/badges` | IMPLEMENTED BUT NOT TESTED | Automatic rank tiers (Newcomer to Kasaysayan Master) |
| Student Profile & Stats | No | `GET /api/auth/me` | IMPLEMENTED BUT NOT TESTED | Displays user name, school, points, streak |

---

## 3. Teacher Features

| Feature | Web Support | Backend Support | Android Status | Notes |
| :--- | :---: | :---: | :---: | :--- |
| Teacher Dashboard Stats | Yes | `GET /api/dashboard` | IMPLEMENTED BUT NOT TESTED | Active battles, students, avg score, sections |
| Topic Mastery Chart | Yes | `GET /api/dashboard`, `GET /api/analytics` | IMPLEMENTED BUT NOT TESTED | Visual mastery percentages per topic |
| Today's Battles Overview | Yes | `GET /api/battles/today` | IMPLEMENTED BUT NOT TESTED | Filter by section or view all |
| Section List & Cards | Yes | `GET /api/sections` | IMPLEMENTED BUT NOT TESTED | Card layout with student counts |
| Section Join Code Reveal | Yes | `GET /api/sections` | IMPLEMENTED BUT NOT TESTED | Tap to reveal / copy join code |
| Create New Section | Yes | `POST /api/sections` | IMPLEMENTED BUT NOT TESTED | Modal with name, grade, subject, member emails |
| Update Section Settings | Yes | `PUT /api/sections/{id}` | IMPLEMENTED BUT NOT TESTED | Rename, battle time, weekend toggle |
| Delete Section | Yes | `DELETE /api/sections/{id}` | IMPLEMENTED BUT NOT TESTED | Confirmation dialog |
| View Section Roster | Yes | `GET /api/sections/{id}/roster` | IMPLEMENTED BUT NOT TESTED | Student list with points and rank |
| Add Students to Roster | Yes | `POST /api/sections/{id}/roster` | IMPLEMENTED BUT NOT TESTED | Batch add students by email |
| Remove Student from Roster | Yes | `DELETE /api/sections/{id}/roster/{studentId}` | IMPLEMENTED BUT NOT TESTED | Confirmation dialog |
| Topic Management | Yes | `GET /api/topics`, `POST /api/topics`, `PUT /api/topics/{id}`, `DELETE /api/topics/{id}` | IMPLEMENTED BUT NOT TESTED | Create, rename, copy, delete topics |
| Question Bank List & Filter | Yes | `GET /api/questions?sectionId=&topic=` | IMPLEMENTED BUT NOT TESTED | Paginated / scrollable question list |
| Create Question | Yes | `POST /api/questions` | IMPLEMENTED BUT NOT TESTED | Choices A-D, difficulty, correct answer |
| Edit Question | Yes | `PUT /api/questions/{id}` | IMPLEMENTED BUT NOT TESTED | Form with prefilled question data |
| Delete Question | Yes | `DELETE /api/questions/{id}` | IMPLEMENTED BUT NOT TESTED | Delete confirmation |
| Generate Daily Battles | Yes | `POST /api/battles/generate` | IMPLEMENTED BUT NOT TESTED | Time picker + weekend battles toggle |
| Battle Results Log | Yes | `GET /api/battles/log` | IMPLEMENTED BUT NOT TESTED | History of completed battles with winners |
| Analytics Overview | Yes | `GET /api/analytics` | IMPLEMENTED BUT NOT TESTED | Avg score, active students, mastery |
| Multi-Section Leaderboard | Yes | `GET /api/leaderboard?sectionIds=` | IMPLEMENTED BUT NOT TESTED | Multi-section selection and ranking |
| Section Battle Settings | Yes | `PUT /api/sections/{id}` | IMPLEMENTED BUT NOT TESTED | Daily battle time, weekend battles |
