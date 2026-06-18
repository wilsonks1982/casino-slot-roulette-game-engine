import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import "./SuperAdminDashboard.css";

function SuperAdminDashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    activeDealers: 0,
    totalGames: 0,
    revenue: 0,
  });

  const [health, setHealth] = useState(null);

  useEffect(() => {
    // Simulate stats
    setStats({
      totalUsers: 24,
      activeDealers: 8,
      totalGames: 1543,
      revenue: 125430,
    });

    // Fetch actuator health
    fetch("/actuator/health")
      .then((res) => res.json())
      .then((data) => setHealth(data))
      .catch((err) => console.error("Health check failed:", err));
  }, []);

  return (
    <div className="dashboard">
      <Navbar />

      <div className="dashboard-content">
        <header className="dashboard-header">
          <h1>⚡ Super Admin Dashboard</h1>
          <p>System overview and analytics</p>
        </header>

        <div className="stats-grid">
          <div className="stat-card-large">
            <div className="stat-icon">👥</div>
            <div className="stat-info">
              <h3>Total Users</h3>
              <p className="stat-number">{stats.totalUsers}</p>
            </div>
          </div>

          <div className="stat-card-large">
            <div className="stat-icon">🎰</div>
            <div className="stat-info">
              <h3>Active Dealers</h3>
              <p className="stat-number">{stats.activeDealers}</p>
            </div>
          </div>

          <div className="stat-card-large">
            <div className="stat-icon">🎲</div>
            <div className="stat-info">
              <h3>Total Games</h3>
              <p className="stat-number">{stats.totalGames.toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card-large">
            <div className="stat-icon">💰</div>
            <div className="stat-info">
              <h3>Revenue</h3>
              <p className="stat-number">${stats.revenue.toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="admin-grid">
          <div className="admin-card">
            <h2>Quick Actions</h2>
            <div className="quick-actions">
              <Link to="/admin/users" className="action-link">
                👥 Manage Users
              </Link>
              <Link to="/dealer" className="action-link">
                🎰 View Dealer Interface
              </Link>
              <a href="/actuator" className="action-link">
                📊 System Metrics
              </a>
              <a href="/swagger-ui.html" className="action-link">
                📚 API Documentation
              </a>
            </div>
          </div>

          <div className="admin-card">
            <h2>System Health</h2>
            {health ? (
              <div className="health-status">
                <div
                  className={`health-indicator ${health.status?.toLowerCase()}`}
                >
                  {health.status === "UP" ? "✅" : "❌"} {health.status}
                </div>
                <div className="health-details">
                  {Object.entries(health.components || {}).map(
                    ([key, value]) => (
                      <div key={key} className="health-item">
                        <span className="health-key">{key}:</span>
                        <span
                          className={`health-value ${value.status?.toLowerCase()}`}
                        >
                          {value.status}
                        </span>
                      </div>
                    ),
                  )}
                </div>
              </div>
            ) : (
              <p>Loading health status...</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default SuperAdminDashboard;
