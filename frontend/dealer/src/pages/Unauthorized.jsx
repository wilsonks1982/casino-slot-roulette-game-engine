import React from "react";
import { Link } from "react-router-dom";
import useAuthStore from "../stores/useAuthStore";

function Unauthorized() {
  const getDefaultRoute = useAuthStore((state) => state.getDefaultRoute);

  return (
    <div style={styles.container}>
      <div style={styles.content}>
        <div style={styles.icon}>🚫</div>
        <h1 style={styles.title}>403 - Unauthorized</h1>
        <p style={styles.message}>
          You don't have permission to access this page.
        </p>
        <Link to={getDefaultRoute()} style={styles.link}>
          Go to Your Dashboard
        </Link>
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    minHeight: "100vh",
    background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
  },
  content: {
    textAlign: "center",
    background: "white",
    padding: "60px 40px",
    borderRadius: "20px",
    boxShadow: "0 20px 60px rgba(0, 0, 0, 0.3)",
    maxWidth: "500px",
  },
  icon: {
    fontSize: "80px",
    marginBottom: "20px",
  },
  title: {
    fontSize: "32px",
    color: "#333",
    margin: "0 0 15px 0",
  },
  message: {
    fontSize: "18px",
    color: "#666",
    marginBottom: "30px",
  },
  link: {
    display: "inline-block",
    padding: "14px 30px",
    background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
    color: "white",
    textDecoration: "none",
    borderRadius: "10px",
    fontWeight: "600",
    transition: "all 0.3s ease",
  },
};

export default Unauthorized;
