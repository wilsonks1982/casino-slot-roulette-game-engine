import React from "react";
import { Navigate } from "react-router-dom";
import useAuthStore from "../stores/useAuthStore";

function ProtectedRoute({ children, requiredRoles = [] }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated());
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}

export default ProtectedRoute;
