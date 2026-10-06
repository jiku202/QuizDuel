import { createContext, useState } from "react";

export const AuthContext = createContext(null);

// Demo-only auth: no backend, no password check.
export function AuthProvider({ children }) {
  const [teacher, setTeacher] = useState(null);

  function login(email) {
    setTeacher({
      name: "Mrs. Santos",
      email,
    });
  }

  function logout() {
    setTeacher(null);
  }

  return (
    <AuthContext.Provider value={{ teacher, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
} 