import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../context/useAuth";

export default function ProtectedRoute() {
  const { teacher, loading } = useAuth();
  if (loading) return null; // still restoring the saved session
  if (!teacher) return <Navigate to="/login" replace />;
  return <Outlet />;
}
