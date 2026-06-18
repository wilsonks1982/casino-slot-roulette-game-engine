import React, { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import "./DealerDashboard.css";

function DealerDashboard() {
  const [gameState, setGameState] = useState({
    status: "idle",
    lastNumber: null,
    balance: 10000,
  });

  const spinRoulette = () => {
    setGameState((prev) => ({ ...prev, status: "spinning" }));

    setTimeout(() => {
      const randomNumber = Math.floor(Math.random() * 37);
      setGameState({
        status: "completed",
        lastNumber: randomNumber,
        balance: gameState.balance - 100,
      });
    }, 3000);
  };

  return (
    <div className="dashboard">
      <Navbar />

      <div className="dashboard-content">
        <header className="dashboard-header">
          <h1>🎰 Dealer Dashboard</h1>
          <p>Control the roulette game</p>
        </header>

        <div className="game-container">
          <div className="roulette-wheel">
            <div
              className={`wheel ${gameState.status === "spinning" ? "spinning" : ""}`}
            >
              {gameState.lastNumber !== null ? (
                <div className="number-display">{gameState.lastNumber}</div>
              ) : (
                <div className="number-display">?</div>
              )}
            </div>
          </div>

          <div className="game-controls">
            <button
              onClick={spinRoulette}
              disabled={gameState.status === "spinning"}
              className="spin-button"
            >
              {gameState.status === "spinning" ? "Spinning..." : "Spin Wheel"}
            </button>
          </div>

          <div className="game-stats">
            <div className="stat-card">
              <h3>Status</h3>
              <p className="stat-value">{gameState.status}</p>
            </div>
            <div className="stat-card">
              <h3>Last Number</h3>
              <p className="stat-value">{gameState.lastNumber ?? "-"}</p>
            </div>
            <div className="stat-card">
              <h3>Balance</h3>
              <p className="stat-value">${gameState.balance}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default DealerDashboard;
