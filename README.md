# QuizDuel — Teacher Web App (Frontend Mockup)

A React + Vite frontend mockup for the QuizDuel teacher console, built from the
wireframe prototypes in the project proposal. Grayscale/wireframe styling only —
no visual design pass yet, and no backend. All data is hardcoded in
`src/data/mockData.js`.

## Project structure

```
quizduel-web/
├── index.html              # Vite entry HTML
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx             # React root, mounts <App/> inside <BrowserRouter/>
    ├── App.jsx               # All route definitions
    ├── index.css             # Global styles / shared wireframe primitives
    ├── config/
    │   └── navigation.js     # Sidebar nav items (single source of truth)
    ├── data/
    │   └── mockData.js       # All hardcoded demo data (topics, questions, battles, etc.)
    ├── context/
    │   ├── authContext.js    # React context object
    │   ├── AuthContext.jsx   # Provider (demo-only login/logout, no real auth)
    │   └── useAuth.js        # Hook: const { teacher, login, logout } = useAuth()
    ├── components/
    │   ├── layout/
    │   │   ├── AppShell.jsx  # Sidebar + Topbar + <Outlet/> wrapper
    │   │   ├── Sidebar.jsx
    │   │   └── Topbar.jsx
    │   ├── routing/
    │   │   └── ProtectedRoute.jsx  # Redirects to /login if not "logged in"
    │   └── ui/
    │       ├── Card.jsx
    │       ├── Chip.jsx
    │       ├── StatBox.jsx
    │       └── MasteryChart.jsx
    └── pages/
        ├── Login.jsx
        ├── Register.jsx
        ├── Dashboard.jsx
        ├── Sections.jsx       # Create section + class roster / award badge
        ├── QuestionBank.jsx   # Topic filter chips + add-question form
        ├── Battles.jsx        # Today's battles + results log
        ├── Leaderboard.jsx
        ├── Analytics.jsx
        └── Settings.jsx       # Daily battle schedule
```

## Running it locally

Requires [Node.js](https://nodejs.org) (v18+).

```bash
cd quizduel-web
npm install
npm run dev
```

Then open the URL it prints — by default:

```
http://localhost:5173
```

On Windows, same steps in PowerShell/Command Prompt:

```powershell
cd path\to\quizduel-web
npm install
npm run dev
```

## Notes

- Login/Register don't check real credentials — submitting the login form just
  sets a `teacher` object in `AuthContext` and routes you into the app.
  Replace the `login()` function in `src/context/AuthContext.jsx` with a real
  API call once there's a backend.
- All tables/lists (battles, leaderboard, question bank, roster) read from
  `src/data/mockData.js` — edit that file to change the demo content, or wire
  each page up to a real data source later.
- No CSS framework; all shared styles live in `src/index.css`.
