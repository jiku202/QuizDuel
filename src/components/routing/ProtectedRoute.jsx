import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../context/useAuth";

export default function ProtectedRoute() {
  const { teacher } = useAuth();
  if (!teacher) return <Navigate to="/login" replace />;
  return <Outlet />;
}
