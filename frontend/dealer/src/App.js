import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginForm from "./components/LoginForm";
import DealerDashboard from "./pages/DealerDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import SuperAdminDashboard from "./pages/SuperAdminDashboard";
import Unauthorized from "./pages/Unauthorized";
import ProtectedRoute from "./components/ProtectedRoute";
import useAuthStore from "./stores/useAuthStore";
import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginForm />} />
        <Route path="/unauthorized" element={<Unauthorized />} />

        <Route
          path="/dealer"
          element={
            <ProtectedRoute requiredRoles={["DEALER", "ADMIN", "SUPER_ADMIN"]}>
              <DealerDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRoles={["ADMIN", "SUPER_ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/super"
          element={
            <ProtectedRoute requiredRoles={["SUPER_ADMIN"]}>
              <SuperAdminDashboard />
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<HomeRedirect />} />
      </Routes>
    </BrowserRouter>
  );
}

function HomeRedirect() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated());
  const getDefaultRoute = useAuthStore((state) => state.getDefaultRoute);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const defaultRoute = getDefaultRoute();
  return <Navigate to={defaultRoute} replace />;
}

export default App;
