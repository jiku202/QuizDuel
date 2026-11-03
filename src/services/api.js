// Thin fetch wrapper for the QuizDuel API (see /springboot-backend).
// Base URL can be overridden with VITE_API_URL in a .env file.
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:5000/api";
const TOKEN_KEY = "quizduel_token";

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token) =>
  token ? localStorage.setItem(TOKEN_KEY, token) : localStorage.removeItem(TOKEN_KEY);

async function request(method, path, body) {
  const token = getToken();
  let res;
  try {
    res = await fetch(BASE_URL + path, {
      method,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error("Can't reach the server. Is the backend running on port 5000?");
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const err = new Error(data?.error || `Request failed (${res.status})`);
    err.status = res.status;
    throw err;
  }
  return data;
}

// Builds "?a=1&b=2", skipping empty values.
const qs = (params = {}) => {
  const q = new URLSearchParams(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== "")
  ).toString();
  return q ? `?${q}` : "";
};

export const api = {
  // auth
  register: (body) => request("POST", "/auth/register", body),
  login: (body) => request("POST", "/auth/login", body),
  me: () => request("GET", "/auth/me"),

  // sections
  getSections: () => request("GET", "/sections"),
  createSection: (body) => request("POST", "/sections", body),
  updateSection: (id, body) => request("PUT", `/sections/${id}`, body),
  getRoster: (id) => request("GET", `/sections/${id}/roster`),
  awardBadge: (sectionId, studentId, badgeId) =>
    request("POST", `/sections/${sectionId}/roster/${studentId}/badges`, { badgeId }),

  // badges
  getBadges: () => request("GET", "/badges"),

  // questions
  getQuestions: (params) => request("GET", `/questions${qs(params)}`),
  getTopics: (params) => request("GET", `/questions/topics${qs(params)}`),
  createQuestion: (body) => request("POST", "/questions", body),
  updateQuestion: (id, body) => request("PUT", `/questions/${id}`, body),
  deleteQuestion: (id) => request("DELETE", `/questions/${id}`),

  // battles
  getTodaysBattles: (params) => request("GET", `/battles/today${qs(params)}`),
  getBattleLog: (params) => request("GET", `/battles/log${qs(params)}`),
  generateBattles: (sectionId) => request("POST", "/battles/generate", { sectionId }),

  // insights
  getDashboard: (params) => request("GET", `/dashboard${qs(params)}`),
  getAnalytics: (params) => request("GET", `/analytics${qs(params)}`),
  getLeaderboard: (params) => request("GET", `/leaderboard${qs(params)}`),
};
