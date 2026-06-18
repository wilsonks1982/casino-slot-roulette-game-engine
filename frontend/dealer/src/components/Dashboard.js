import React from 'react';

function Dashboard({ user, onLogout }) {
  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Welcome, {user?.name || 'User'}!</h1>
        <button onClick={onLogout} className="logout-btn">
          Logout
        </button>
      </header>
      <main className="dashboard-content">
        <div className="card">
          <h2>User Information</h2>
          <p><strong>Email:</strong> {user?.email || 'N/A'}</p>
          <p><strong>ID:</strong> {user?.id || 'N/A'}</p>
        </div>
      </main>
    </div>
  );
}

export default Dashboard;