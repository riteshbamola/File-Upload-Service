import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute() {
  const { accessToken, loading } = useAuth();

  // ⏳ wait for refresh check
  if (loading) return null; // or spinner

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
