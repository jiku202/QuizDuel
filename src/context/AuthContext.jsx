import { createContext, useEffect, useState } from "react";
import { api, getToken, setToken } from "../services/api";

export const AuthContext = createContext(null);

// Real auth against the QuizDuel API. The JWT is kept in localStorage so a
// page refresh keeps you logged in.
export function AuthProvider({ children }) {
  const [teacher, setTeacher] = useState(null);
  const [loading, setLoading] = useState(!!getToken()); // true while restoring a saved session

  useEffect(() => {
    if (!getToken()) return;
    api
      .me()
      .then(({ user }) => (user.role === "teacher" ? setTeacher(user) : setToken(null)))
      .catch(() => setToken(null))
      .finally(() => setLoading(false));
  }, []);

  async function login(email, password) {
    const { token, user } = await api.login({ email, password });
    if (user.role !== "teacher") {
      throw new Error("This console is for teachers. Students use the mobile app.");
    }
    setToken(token);
    setTeacher(user);
  }

  // Creates the account only; the user is sent to the login page afterwards.
  async function register({ name, school, email, password }) {
    await api.register({ name, school, email, password, role: "teacher" });
  }

  function logout() {
    setToken(null);
    setTeacher(null);
  }

  return (
    <AuthContext.Provider value={{ teacher, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
