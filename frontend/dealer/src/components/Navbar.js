import React from "react";
import { Link, useNavigate } from "react-router-dom";
import useAuthStore from "../stores/useAuthStore";
import useWebSocketStore from "../stores/useWebSocketStore";
import "./Navbar.css";

function Navbar() {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const hasRole = useAuthStore((state) => state.hasRole);
  const wsStatus = useWebSocketStore((state) => state.wsStatus);
  const navigate = useNavigate();

  const handleLogout = async () => {
    console.log("Logout initiated...");
    await logout();
    navigate("/login", { replace: true });
  };

  const getStatusText = () => {
    switch (wsStatus) {
      case "Connected":
        return "WebSocket Connected";
      case "Connecting...":
        return "WebSocket Connecting...";
      case "Disconnected":
        return "WebSocket Disconnected";
      case "Error":
        return "WebSocket Error";
      case "Connection Failed":
        return "WebSocket Connection Failed";
      default:
        return "WebSocket Status Unknown";
    }
  };

  const getStatusEmoji = () => {
    switch (wsStatus) {
      case "Connected":
        return "🟢";
      case "Connecting...":
        return "🟡";
      case "Disconnected":
      case "Error":
      case "Connection Failed":
        return "🔴";
      default:
        return "⚪";
    }
  };

  const formatRole = (role) => {
    if (!role) return "";
    return role.replace(/_/g, " ");
  };

  const getFirstRole = () => {
    if (!user?.roles || user.roles.length === 0) return "";
    return formatRole(user.roles[0]);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* Brand Section */}
        <div className="navbar-brand">
          <span className="logo">🎰</span>
          <span className="brand-name">Roulette Game</span>
        </div>

        {/* Navigation Links */}
        {/* <div className="navbar-menu">
          {hasRole("DEALER") && (
            <Link to="/dealer" className="nav-link">
              🎲 Dealer
            </Link>
          )}

          {hasRole("ADMIN") && (
            <Link to="/admin" className="nav-link">
              👥 Users
            </Link>
          )}

          {hasRole("SUPER_ADMIN") && (
            <>
              <Link to="/super" className="nav-link">
                📊 Dashboard
              </Link>
              <Link to="/admin" className="nav-link">
                👥 Users
              </Link>
            </>
          )}
        </div> */}

        {/* User Section */}
        <div className="navbar-user">
          {/* WebSocket Status Indicator - Only show for ADMIN and SUPER_ADMIN */}
          {(hasRole("ADMIN") || hasRole("SUPER_ADMIN")) && (
            <div
              className="ws-status-badge"
              title={getStatusText()}
              data-status={getStatusText()}
            >
              <span
                className={`ws-indicator ws-${wsStatus.toLowerCase().replace(/\s/g, "-")}`}
              >
                {getStatusEmoji()}
              </span>
            </div>
          )}

          {/* User Info */}
          <div className="user-info">
            <span className="user-icon">👤</span>
            <div className="user-details">
              <span className="username">{user?.username || "Guest"}</span>
              {user?.roles && user.roles.length > 0 && (
                <span className="user-role">{getFirstRole()}</span>
              )}
            </div>
          </div>

          {/* Logout Button */}
          <button onClick={handleLogout} className="logout-btn">
            🚪 Logout
          </button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
