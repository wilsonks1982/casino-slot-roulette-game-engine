import React, { useEffect, useMemo, useRef, useState } from "react";
import Navbar from "../components/Navbar";
import useWebSocketStore from "../stores/useWebSocketStore";
import "./AdminDashboard.css";

const TABS = {
  LIVE: "LIVE",
  HISTORY: "HISTORY",
  PLAYGROUND: "PLAYGROUND",
  MESSAGES: "MESSAGES",
  BETTING: "BETTING",
};

const TABLE_OPTIONS = [
  { id: "1", label: "Table 1" },
  { id: "2", label: "Table 2" },
];

const CHIP_VALUES = [1, 5, 10, 25, 50, 100, 250, 500];

// Roulette grid layout (standard table: 3 columns x 12 rows)
// Row 1: 1,2,3 ; Row 2: 4,5,6 ; ... Row 12: 34,35,36
const GRID_ROWS = Array.from({ length: 12 }, (_, r) => {
  const base = r * 3 + 1;
  return [base, base + 1, base + 2];
});

const RED_NUMBERS = new Set([
  1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36,
]);

function safeJsonParse(value) {
  if (typeof value !== "string") return { ok: false, data: value };
  try {
    return { ok: true, data: JSON.parse(value) };
  } catch {
    return { ok: false, data: value };
  }
}

function getServerMessageType(msg) {
  if (!msg) return "";
  if (msg.type !== "received") return "";
  if (msg.content && typeof msg.content === "object" && msg.content.type)
    return msg.content.type;

  const parsed = safeJsonParse(msg.content);
  if (parsed.ok && parsed.data?.type) return parsed.data.type;

  return "";
}

function isBettingOpenFromStatus(status) {
  // Per your requirement: open from ROUND_CREATED until BETTING_CLOSED.
  // Interpreting "open" as: ROUND_CREATED or BETTING_OPENED.
  return status === "ROUND_CREATED" || status === "BETTING_OPENED";
}

/** Straight-up betIndex mapping from your BET_INDEX_MAP:
 *  0 => 100
 *  1 => 101
 *  ...
 *  36 => 136
 */
function getStraightBetIndex(n) {
  const num = Number(n);
  if (!Number.isFinite(num) || num < 0 || num > 36) return null;
  return 100 + num;
}

function toBetsListFromBoard(boardBets) {
  return Object.entries(boardBets)
    .map(([betIndex, betAmount]) => ({
      betIndex: Number(betIndex),
      betAmount: Number(betAmount),
    }))
    .filter(
      (b) =>
        Number.isFinite(b.betIndex) &&
        Number.isFinite(b.betAmount) &&
        b.betAmount > 0,
    );
}

function safeServerObject(msg) {
  if (!msg || msg.type !== "received") return null;

  const msgObj =
    msg.content && typeof msg.content === "object"
      ? msg.content
      : safeJsonParse(msg.content).data;

  if (!msgObj || typeof msgObj !== "object") return null;
  return msgObj;
}

function buildHotspots() {
  const spots = [];

  // Streets (500..511)
  for (let r = 0; r < 12; r++) {
    spots.push({ betIndex: 500 + r, kind: "STREET", r, c: 1 });
  }

  // Six-lines (600..610)
  for (let r = 0; r < 11; r++) {
    spots.push({ betIndex: 600 + r, kind: "SIXLINE", r, c: 1 });
  }

  // Horizontal splits:
  // a-b (1-2,4-5,...,34-35) => 236..247
  // b-c (2-3,5-6,...,35-36) => 212..223
  for (let r = 0; r < 12; r++) {
    spots.push({ betIndex: 236 + r, kind: "SPLIT_H", r, c: 0 });
    spots.push({ betIndex: 212 + r, kind: "SPLIT_H", r, c: 1 });
  }

  // Vertical splits:
  // col0 (1-4..31-34) => 249..259
  // col1 (2-5..32-35) => 225..235
  // col2 (3-6..33-36) => 201..211
  for (let r = 0; r < 11; r++) {
    spots.push({ betIndex: 249 + r, kind: "SPLIT_V", r, c: 0 });
    spots.push({ betIndex: 225 + r, kind: "SPLIT_V", r, c: 1 });
    spots.push({ betIndex: 201 + r, kind: "SPLIT_V", r, c: 2 });
  }

  // Corners:
  // (1,2,4,5) => 411..421
  // (2,3,5,6) => 400..410
  for (let r = 0; r < 11; r++) {
    spots.push({ betIndex: 411 + r, kind: "CORNER", r, c: 0 });
    spots.push({ betIndex: 400 + r, kind: "CORNER", r, c: 1 });
  }
  // Special 0-1-2-3 corner
  spots.push({ betIndex: 422, kind: "CORNER_ZERO", r: 0, c: -1 });

  // Zero splits: 0-1 (248), 0-2 (224), 0-3 (200)
  spots.push({ betIndex: 248, kind: "SPLIT_ZERO", r: 0, c: -1 });
  spots.push({ betIndex: 224, kind: "SPLIT_ZERO", r: 0, c: -1 });
  spots.push({ betIndex: 200, kind: "SPLIT_ZERO", r: 0, c: -1 });

  // Trios: 0-1-2 (301), 0-2-3 (300)
  spots.push({ betIndex: 301, kind: "TRIO_ZERO", r: 0, c: -1 });
  spots.push({ betIndex: 300, kind: "TRIO_ZERO", r: 0, c: -1 });

  return spots;
}

const ROULETTE_HOTSPOTS = buildHotspots();

function useRouletteGridMetrics() {
  const containerRef = useRef(null);
  const sampleCellRef = useRef(null);
  const sampleCell2Ref = useRef(null);
  const sampleRowRef = useRef(null);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const ro = new ResizeObserver(() => {
      const cell = sampleCellRef.current;
      const row = sampleRowRef.current;
      if (!cell || !row) return;

      const cellRect = cell.getBoundingClientRect();
      const cellW = cellRect.width;
      const cellH = cellRect.height;

      let gapY = 8;
      let gapX = 8;

      const grid = row.parentElement; // .grid-1to36
      if (grid) {
        const gridStyles = window.getComputedStyle(grid);
        const rg = parseFloat(gridStyles.rowGap || "0");
        const cg = parseFloat(gridStyles.columnGap || "0");
        if (Number.isFinite(rg) && rg >= 0) gapY = rg;
        if (Number.isFinite(cg) && cg >= 0) gapX = cg;
      }

      const cell2 = sampleCell2Ref.current;
      if (cell2) {
        const r1 = cell.getBoundingClientRect();
        const r2 = cell2.getBoundingClientRect();
        const measuredGapX = r2.left - r1.right;
        if (Number.isFinite(measuredGapX) && measuredGapX >= 0)
          gapX = measuredGapX;
      }

      container.style.setProperty("--cellW", `${cellW}px`);
      container.style.setProperty("--cellH", `${cellH}px`);
      container.style.setProperty("--gapX", `${gapX}px`);
      container.style.setProperty("--gapY", `${gapY}px`);
    });

    ro.observe(container);
    if (sampleCellRef.current) ro.observe(sampleCellRef.current);
    if (sampleCell2Ref.current) ro.observe(sampleCell2Ref.current);
    if (sampleRowRef.current) ro.observe(sampleRowRef.current);

    return () => ro.disconnect();
  }, []);

  return { containerRef, sampleCellRef, sampleCell2Ref, sampleRowRef };
}

function AdminDashboard() {
  const [activeTab, setActiveTab] = useState(TABS.LIVE);

  // local WS defaults
  const [wsHost, setWsHost] = useState("localhost");
  const [wsPort, setWsPort] = useState("9090");
  const [egmId, setEgmId] = useState("123456");
  const [uid, setUid] = useState("23869508");
  const [tableId, setTableId] = useState("1");

  // Messages tab filter
  const [filterType, setFilterType] = useState("ALL");
  const messagesEndRef = useRef(null);

  // Playground state
  const [rawJson, setRawJson] = useState("");
  const [placeBetStake, setPlaceBetStake] = useState(100);
  const [placeBetRoundIdOverride, setPlaceBetRoundIdOverride] = useState("");
  const [betsList, setBetsList] = useState([
    { betIndex: 117, betAmount: 50 },
    { betIndex: 127, betAmount: 50 },
  ]);

  // Betting panel state
  const [selectedChip, setSelectedChip] = useState(10);
  const [boardBets, setBoardBets] = useState(() => ({}));
  const [chipActions, setChipActions] = useState([]);
  const [lastAckedBetPlacedByTable, setLastAckedBetPlacedByTable] = useState(
    {},
  );

  const { containerRef, sampleCellRef, sampleCell2Ref, sampleRowRef } =
    useRouletteGridMetrics();

  const {
    wsStatus,
    messages,
    connect,
    sendMessage,
    disconnect,
    manualReconnect,
    isConnected,
    clearMessages,
    clearAllTables,
    setActiveTable,
  } = useWebSocketStore();

  const connected = isConnected();

  const wsUrl = useMemo(() => {
    const base = `ws://${wsHost}:${wsPort}/websocket/game`;
    const qs = new URLSearchParams({
      egmId: String(egmId),
      uid: String(uid),
      tableId: String(tableId),
    });
    return `${base}?${qs.toString()}`;
  }, [wsHost, wsPort, egmId, uid, tableId]);

  // Connect whenever URL changes
  useEffect(() => {
    connect(wsUrl);
    return () => disconnect();
  }, [connect, disconnect, wsUrl]);

  // Scroll in messages tab only
  useEffect(() => {
    if (activeTab !== TABS.MESSAGES) return;
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, activeTab]);

  const handleSwitchTable = (nextTableId) => {
    if (nextTableId === tableId) return;
    setActiveTable(nextTableId); // show cached messages instantly
    setTableId(nextTableId); // changes URL -> connect effect
    setActiveTab(TABS.LIVE);
  };

  const handleClearTable = () => {
    if (window.confirm("Clear messages for CURRENT table only?"))
      clearMessages();
  };

  const handleClearAll = () => {
    if (
      window.confirm("Clear ALL tables cache (also clears localStorage cache)?")
    )
      clearAllTables();
  };

  // ---------- Derived roulette state ----------
  const derived = useMemo(() => {
    const state = {
      tableInfo: null,
      wallet: null,
      session: null,

      roundId: "",
      status: "UNKNOWN",
      secondsRemaining: null,

      lastResult: null,
      lastPayout: null,
      lastBetResponse: null,

      history: [],
      hotNumbers: [],
      coldNumbers: [],
      lastNumbers: [],
    };

    const historyMap = new Map();

    for (const m of messages) {
      if (m.type !== "received") continue;

      const msgObj =
        m.content && typeof m.content === "object"
          ? m.content
          : safeJsonParse(m.content).data;

      if (!msgObj || typeof msgObj !== "object") continue;

      const { type, payload } = msgObj;

      if (type === "INITIAL_DATA" && payload) {
        state.tableInfo = payload.tableInfo || state.tableInfo;
        state.wallet = payload.wallet || state.wallet;
        state.session = payload.session || state.session;

        if (payload.gameState) {
          state.roundId = payload.gameState.roundId || state.roundId;
          state.status = payload.gameState.status || state.status;
          if (typeof payload.gameState.secondsRemaining === "number") {
            state.secondsRemaining = payload.gameState.secondsRemaining;
          }
        }

        const hist = payload.history || {};
        state.hotNumbers = hist.hotNumbers?.hotNumbers || state.hotNumbers;
        state.coldNumbers = hist.coldNumbers?.coldNumbers || state.coldNumbers;
        state.lastNumbers = hist.lastNumbers?.lastNumbers || state.lastNumbers;

        if (Array.isArray(state.lastNumbers)) {
          for (const entry of state.lastNumbers) {
            const rid = String(entry?.roundId ?? "");
            if (!rid) continue;
            if (!historyMap.has(rid)) {
              historyMap.set(rid, {
                roundId: rid,
                winningNumber: entry.winningNumber,
                ts: m.timestamp,
                color: "",
                parity: "",
                dozen: "",
                column: "",
              });
            }
          }
        }
      }

      if (type === "ROUND_CREATED" && payload?.roundId) {
        state.roundId = payload.roundId;
        state.status = "ROUND_CREATED";
      }

      if (type === "BETTING_OPENED" && payload) {
        if (payload.roundId) state.roundId = payload.roundId;
        state.status = "BETTING_OPENED";
        if (typeof payload.secondsRemaining === "number")
          state.secondsRemaining = payload.secondsRemaining;
      }

      if (type === "BETTING_CLOSED" && payload?.roundId) {
        state.roundId = payload.roundId;
        state.status = "BETTING_CLOSED";
        state.secondsRemaining = 0;
      }

      if (type === "BALL_RELEASED" && payload?.roundId) {
        state.roundId = payload.roundId;
        state.status = "BALL_RELEASED";
      }

      if (type === "WHEEL_SPINNING" && payload?.roundId) {
        state.roundId = payload.roundId;
        state.status = "WHEEL_SPINNING";
      }

      if (type === "RESULT_DECLARED" && payload) {
        state.status = "RESULT_DECLARED";
        state.lastResult = { ...payload };

        const rid = String(payload.roundId ?? "");
        if (rid) {
          historyMap.set(rid, {
            roundId: rid,
            winningNumber: payload.winningNumber,
            ts: m.timestamp,
            color: payload.color || "",
            parity: payload.parity || "",
            dozen: payload.dozen ?? "",
            column: payload.column ?? "",
          });
        }

        state.hotNumbers = payload.hotNumbers || state.hotNumbers;
        state.coldNumbers = payload.coldNumbers || state.coldNumbers;
        state.lastNumbers = payload.lastNumbers || state.lastNumbers;
      }

      if (type === "PAYOUT_COMPLETED" && payload) {
        state.lastPayout = payload;
      }

      if (type === "BALANCE_UPDATED" && payload) {
        state.wallet = state.wallet
          ? { ...state.wallet, balance: payload.newBalance }
          : { balance: payload.newBalance, currency: "" };
      }

      if (
        type === "BET_PLACED" ||
        type === "BET_REJECTED" ||
        type === "BET_CANCELLED"
      ) {
        state.lastBetResponse = { type, payload, ts: m.timestamp };
        if (type === "BET_PLACED" && payload?.newBalance != null) {
          state.wallet = state.wallet
            ? { ...state.wallet, balance: payload.newBalance }
            : { balance: payload.newBalance, currency: "" };
        }
      }
    }

    state.history = Array.from(historyMap.values()).sort((a, b) => {
      const ta = new Date(a.ts).getTime();
      const tb = new Date(b.ts).getTime();
      if (!Number.isNaN(ta) && !Number.isNaN(tb) && ta !== tb) return tb - ta;
      return String(b.roundId).localeCompare(String(a.roundId));
    });

    return state;
  }, [messages]);

  const bettingOpen = isBettingOpenFromStatus(derived.status);

  const totalStake = useMemo(() => {
    return Object.values(boardBets).reduce((sum, v) => sum + Number(v || 0), 0);
  }, [boardBets]);

  const placeChipOnBetIndex = (betIndex) => {
    if (!bettingOpen) return;

    const idx = Number(betIndex);
    if (!Number.isFinite(idx)) return;

    const delta = Number(selectedChip);

    setBoardBets((prev) => {
      const key = String(idx);
      return { ...prev, [key]: (prev[key] || 0) + delta };
    });

    setChipActions((prev) => [...prev, { betIndex: String(idx), delta }]);
  };

  const handleUndo = () => {
    if (chipActions.length === 0) return;
    const last = chipActions[chipActions.length - 1];
    setChipActions((prev) => prev.slice(0, -1));

    setBoardBets((prev) => {
      const current = Number(prev[last.betIndex] || 0);
      const next = current - Number(last.delta || 0);

      if (next <= 0) {
        const copy = { ...prev };
        delete copy[last.betIndex];
        return copy;
      }

      return { ...prev, [last.betIndex]: next };
    });
  };

  const handleReset = () => {
    setBoardBets({});
    setChipActions([]);
  };

  // Track last ACKED BET_PLACED by table (server success)
  useEffect(() => {
    for (let i = messages.length - 1; i >= 0; i--) {
      const m = messages[i];
      const obj = safeServerObject(m);
      if (!obj) continue;

      if (obj.type !== "BET_PLACED") continue;

      const p = obj.payload || {};
      const betsList = Array.isArray(p.betsList) ? p.betsList : null;
      const stake = Number(p.stake);

      if (!betsList || betsList.length === 0) break;

      const normalized = betsList
        .map((b) => ({
          betIndex: Number(b.betIndex),
          betAmount: Number(b.betAmount),
        }))
        .filter(
          (b) =>
            Number.isFinite(b.betIndex) &&
            Number.isFinite(b.betAmount) &&
            b.betAmount > 0,
        );

      if (normalized.length === 0) break;

      const computedStake =
        Number.isFinite(stake) && stake > 0
          ? stake
          : normalized.reduce((s, b) => s + b.betAmount, 0);

      const key = String(tableId);
      const prev = lastAckedBetPlacedByTable[key];
      const prevSig = prev?.betsList ? JSON.stringify(prev.betsList) : "";
      const nextSig = JSON.stringify(normalized);

      if (prevSig !== nextSig || prev?.roundId !== p.roundId) {
        setLastAckedBetPlacedByTable((prevMap) => ({
          ...prevMap,
          [key]: {
            roundId: String(p.roundId || ""),
            stake: computedStake,
            betsList: normalized,
            ackedAt: m.timestamp,
          },
        }));
      }

      break;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [messages, tableId]);

  const handleRebet = () => {
    if (!bettingOpen) return;

    const roundId = derived.roundId || "";
    if (!roundId) {
      alert("No active roundId yet.");
      return;
    }

    const last = lastAckedBetPlacedByTable[String(tableId)];
    if (!last) {
      alert("No server-acknowledged BET_PLACED found for this table.");
      return;
    }

    // Load into board for visual confirmation
    const nextBoard = {};
    for (const b of last.betsList) nextBoard[String(b.betIndex)] = b.betAmount;
    setBoardBets(nextBoard);
    setChipActions([]);

    const payload = {
      roundId: String(roundId),
      stake: Number(last.stake),
      betsList: last.betsList,
    };

    const sent = sendMessage({ type: "PLACE_BET", payload });
    if (!sent) alert("WebSocket is not connected. Please reconnect.");
  };

  const handlePlaceBetFromBoard = () => {
    if (!bettingOpen) {
      alert("Betting is closed.");
      return;
    }
    const roundId = derived.roundId || "";
    if (!roundId) {
      alert("No active roundId yet.");
      return;
    }

    const betsListOut = toBetsListFromBoard(boardBets);
    if (betsListOut.length === 0) {
      alert("No bets on the board.");
      return;
    }

    const payload = {
      roundId: String(roundId),
      stake: Number(totalStake),
      betsList: betsListOut,
    };

    const sent = sendMessage({ type: "PLACE_BET", payload });
    if (!sent) alert("WebSocket is not connected. Please reconnect.");
  };

  // ---------- Messages tab helpers ----------
  const filteredMessages = useMemo(() => {
    return messages.filter((msg) =>
      filterType === "ALL" ? true : msg.type === filterType,
    );
  }, [messages, filterType]);

  const renderMessageContent = (content) => {
    if (typeof content === "string") {
      const parsed = safeJsonParse(content);
      if (parsed.ok && typeof parsed.data === "object") {
        return (
          <pre className="json-content">
            {JSON.stringify(parsed.data, null, 2)}
          </pre>
        );
      }
      return <span>{content}</span>;
    }
    if (typeof content === "object") {
      return (
        <pre className="json-content">{JSON.stringify(content, null, 2)}</pre>
      );
    }
    return <span>{String(content)}</span>;
  };

  const getMessageTypeLabel = (msg) => {
    if (msg.type === "sent") return "Sent";
    if (msg.type === "received") return getServerMessageType(msg) || "Received";
    return msg.type;
  };

  // ---------- Playground ----------
  const activeRoundIdForBet = useMemo(() => {
    return placeBetRoundIdOverride.trim() || derived.roundId || "";
  }, [placeBetRoundIdOverride, derived.roundId]);

  const addBetRow = () =>
    setBetsList((prev) => [...prev, { betIndex: 0, betAmount: 0 }]);
  const removeBetRow = (idx) =>
    setBetsList((prev) => prev.filter((_, i) => i !== idx));
  const updateBetRow = (idx, patch) =>
    setBetsList((prev) =>
      prev.map((b, i) => (i === idx ? { ...b, ...patch } : b)),
    );

  const handleSendPlaceBet = () => {
    if (!activeRoundIdForBet) {
      alert(
        "RoundId not available yet. Wait for INITIAL_DATA / ROUND_CREATED / BETTING_OPENED.",
      );
      return;
    }

    const cleanBets = betsList
      .map((b) => ({
        betIndex: Number(b.betIndex),
        betAmount: Number(b.betAmount),
      }))
      .filter(
        (b) =>
          Number.isFinite(b.betIndex) &&
          Number.isFinite(b.betAmount) &&
          b.betAmount > 0,
      );

    const payload = {
      roundId: String(activeRoundIdForBet),
      stake: Number(placeBetStake),
      betsList: cleanBets,
    };

    const sent = sendMessage({ type: "PLACE_BET", payload });
    if (!sent) alert("WebSocket is not connected. Please reconnect.");
  };

  const handleSendRawJson = () => {
    if (!rawJson.trim()) return;

    const parsed = safeJsonParse(rawJson);
    if (!parsed.ok) {
      alert("Invalid JSON");
      return;
    }

    const sent = sendMessage(parsed.data);
    if (!sent) alert("WebSocket is not connected. Please reconnect.");
  };

  return (
    <div className="dashboard">
      <Navbar />

      <div className="dashboard-content">
        <header className="dashboard-header">
          <div className="ws-status">
            <span
              className={`status-indicator ${connected ? "status-connected" : "status-disconnected"}`}
            >
              {connected ? "🟢" : "🔴"}
            </span>
            <span className="ws-status-text">
              {wsStatus} — {connected ? "Connected" : "Disconnected"}
            </span>
            <button className="btn-reconnect" onClick={manualReconnect}>
              🔄 Reconnect
            </button>
          </div>

          <div className="ws-connection-form">
            <div className="ws-form-row">
              <div className="ws-form-field">
                <label>UID</label>
                <input value={uid} onChange={(e) => setUid(e.target.value)} />
              </div>
            </div>
          </div>

          <div className="wheel-switcher">
            <label>Wheel / Table</label>
            <select
              value={tableId}
              onChange={(e) => handleSwitchTable(e.target.value)}
            >
              {TABLE_OPTIONS.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.label}
                </option>
              ))}
            </select>
            <small>
              <strong>URL:</strong> {wsUrl}
            </small>
          </div>
        </header>

        <div className="admin-tabs">
          <button
            className={`tab-btn ${activeTab === TABS.LIVE ? "active" : ""}`}
            onClick={() => setActiveTab(TABS.LIVE)}
          >
            Live Status
          </button>
          <button
            className={`tab-btn ${activeTab === TABS.HISTORY ? "active" : ""}`}
            onClick={() => setActiveTab(TABS.HISTORY)}
          >
            Round History
          </button>
          <button
            className={`tab-btn ${activeTab === TABS.BETTING ? "active" : ""}`}
            onClick={() => setActiveTab(TABS.BETTING)}
          >
            Betting
          </button>
          <button
            className={`tab-btn ${activeTab === TABS.PLAYGROUND ? "active" : ""}`}
            onClick={() => setActiveTab(TABS.PLAYGROUND)}
          >
            Command Playground
          </button>
          <button
            className={`tab-btn ${activeTab === TABS.MESSAGES ? "active" : ""}`}
            onClick={() => setActiveTab(TABS.MESSAGES)}
          >
            Messages ({messages.length})
          </button>
        </div>

        <div className="admin-container">
          {/* LIVE */}
          {activeTab === TABS.LIVE && (
            <div className="ws-panel">
              <div className="ws-panel-header">
                <h3>
                  🎡 Live Status —{" "}
                  {TABLE_OPTIONS.find((t) => t.id === tableId)?.label}
                </h3>
                <div className="ws-panel-actions">
                  <button
                    className="btn-action btn-clear"
                    onClick={handleClearTable}
                    disabled={messages.length === 0}
                  >
                    🗑️ Clear Table
                  </button>
                  <button
                    className="btn-action btn-clear-all"
                    onClick={handleClearAll}
                  >
                    🧹 Clear All
                  </button>
                </div>
              </div>

              <div className="live-grid">
                <div className="live-card">
                  <h4>Game</h4>
                  <div className="kv">
                    <div>
                      <span className="k">Table</span>
                      <span className="v">
                        {derived.tableInfo?.tableId || tableId}
                      </span>
                    </div>
                    <div>
                      <span className="k">Wheel Type</span>
                      <span className="v">
                        {derived.tableInfo?.wheelType || "-"}
                      </span>
                    </div>
                    <div>
                      <span className="k">Round</span>
                      <span className="v">{derived.roundId || "-"}</span>
                    </div>
                    <div>
                      <span className="k">Status</span>
                      <span className="v">{derived.status}</span>
                    </div>
                    <div>
                      <span className="k">Seconds</span>
                      <span className="v">
                        {derived.secondsRemaining === null
                          ? "-"
                          : derived.secondsRemaining}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="live-card">
                  <h4>Wallet</h4>
                  <div className="kv">
                    <div>
                      <span className="k">Balance</span>
                      <span className="v">
                        {derived.wallet?.balance ?? "-"}{" "}
                        {derived.wallet?.currency || ""}
                      </span>
                    </div>
                    <div>
                      <span className="k">Heartbeat</span>
                      <span className="v">
                        {derived.session?.heartbeatInterval ?? "-"}s
                      </span>
                    </div>
                  </div>

                  {derived.lastBetResponse && (
                    <div className="inline-note">
                      <small>
                        <strong>Last bet response:</strong>{" "}
                        {derived.lastBetResponse.type}
                      </small>
                    </div>
                  )}
                </div>

                <div className="live-card">
                  <h4>Last Result</h4>
                  {derived.lastResult ? (
                    <div className="result-card">
                      <div className="result-number">
                        {derived.lastResult.winningNumber}
                      </div>
                      <div className="result-meta">
                        <div>
                          <span className="k">Color</span>{" "}
                          <span className="v">{derived.lastResult.color}</span>
                        </div>
                        <div>
                          <span className="k">Parity</span>{" "}
                          <span className="v">{derived.lastResult.parity}</span>
                        </div>
                        <div>
                          <span className="k">Dozen</span>{" "}
                          <span className="v">{derived.lastResult.dozen}</span>
                        </div>
                        <div>
                          <span className="k">Column</span>{" "}
                          <span className="v">{derived.lastResult.column}</span>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <small>No result yet (wait for RESULT_DECLARED).</small>
                  )}

                  {derived.lastPayout && (
                    <div className="inline-note">
                      <small>
                        <strong>Payout:</strong> stake{" "}
                        {derived.lastPayout.totalStake} | payout{" "}
                        {derived.lastPayout.totalPayout} | net{" "}
                        {derived.lastPayout.netResult}
                      </small>
                    </div>
                  )}
                </div>

                <div className="live-card">
                  <h4>Hot / Cold / Last Numbers</h4>
                  <div className="numbers-row">
                    <div>
                      <div className="numbers-title">Hot</div>
                      <div className="chips">
                        {(derived.hotNumbers || []).slice(0, 10).map((n) => (
                          <span key={`hot-${n}`} className="chip chip-hot">
                            {n}
                          </span>
                        ))}
                        {(!derived.hotNumbers ||
                          derived.hotNumbers.length === 0) && <small>-</small>}
                      </div>
                    </div>

                    <div>
                      <div className="numbers-title">Cold</div>
                      <div className="chips">
                        {(derived.coldNumbers || []).slice(0, 10).map((n) => (
                          <span key={`cold-${n}`} className="chip chip-cold">
                            {n}
                          </span>
                        ))}
                        {(!derived.coldNumbers ||
                          derived.coldNumbers.length === 0) && <small>-</small>}
                      </div>
                    </div>

                    <div>
                      <div className="numbers-title">Last</div>
                      <div className="chips">
                        {(derived.lastNumbers || [])
                          .slice(0, 10)
                          .map((x, i) => (
                            <span
                              key={`last-${x.roundId}-${i}`}
                              className="chip chip-last"
                            >
                              {x.winningNumber}
                            </span>
                          ))}
                        {(!derived.lastNumbers ||
                          derived.lastNumbers.length === 0) && <small>-</small>}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* HISTORY */}
          {activeTab === TABS.HISTORY && (
            <div className="ws-panel">
              <div className="ws-panel-header">
                <h3>
                  🧾 Round History —{" "}
                  {TABLE_OPTIONS.find((t) => t.id === tableId)?.label}
                </h3>
                <div className="ws-panel-actions">
                  <button
                    className="btn-action btn-clear"
                    onClick={handleClearTable}
                    disabled={messages.length === 0}
                  >
                    🗑️ Clear Table
                  </button>
                  <button
                    className="btn-action btn-clear-all"
                    onClick={handleClearAll}
                  >
                    🧹 Clear All
                  </button>
                </div>
              </div>

              {derived.history.length === 0 ? (
                <div className="no-messages">
                  <p>📭 No history yet</p>
                  <small>
                    Wait for INITIAL_DATA.history.lastNumbers and
                    RESULT_DECLARED.
                  </small>
                </div>
              ) : (
                <div className="history-table">
                  <table>
                    <thead>
                      <tr>
                        <th>Time</th>
                        <th>Round</th>
                        <th>Winning</th>
                        <th>Color</th>
                        <th>Parity</th>
                        <th>Dozen</th>
                        <th>Column</th>
                      </tr>
                    </thead>
                    <tbody>
                      {derived.history.slice(0, 80).map((h) => (
                        <tr key={`hist-${h.roundId}`}>
                          <td>{new Date(h.ts).toLocaleTimeString()}</td>
                          <td>{h.roundId}</td>
                          <td>
                            <span className="winning-pill">
                              {h.winningNumber}
                            </span>
                          </td>
                          <td>{h.color || "-"}</td>
                          <td>{h.parity || "-"}</td>
                          <td>{h.dozen === "" ? "-" : h.dozen}</td>
                          <td>{h.column === "" ? "-" : h.column}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* BETTING */}
          {activeTab === TABS.BETTING && (
            <div className="ws-panel">
              <div className="ws-panel-header">
                <h3>
                  🎲 Roulette Betting —{" "}
                  {TABLE_OPTIONS.find((t) => t.id === tableId)?.label}
                </h3>

                <div className="ws-panel-actions">
                  <button className="btn-action" onClick={handleUndo}>
                    ↩ Undo
                  </button>

                  <button
                    className="btn-action btn-clear"
                    onClick={handleReset}
                    disabled={Object.keys(boardBets).length === 0}
                  >
                    ♻ Reset
                  </button>

                  <button
                    className="btn-action"
                    onClick={handleRebet}
                    disabled={
                      !bettingOpen ||
                      !lastAckedBetPlacedByTable[String(tableId)]
                    }
                    title="Re-apply last server-acknowledged BET_PLACED for this table"
                  >
                    🔁 Rebet
                  </button>

                  <button
                    className="btn-action btn-broadcast"
                    onClick={handlePlaceBetFromBoard}
                    disabled={!connected || !bettingOpen}
                  >
                    📤 Place Bet ({totalStake})
                  </button>
                </div>
              </div>

              {!bettingOpen ? (
                <div className="no-messages" style={{ height: "260px" }}>
                  <p>🔒 Betting is CLOSED</p>
                  <small>
                    Enabled during <strong>ROUND_CREATED</strong> and{" "}
                    <strong>BETTING_OPENED</strong>.
                    <br />
                    Current status: <strong>{derived.status}</strong>
                  </small>
                </div>
              ) : (
                <div className="roulette-bet-layout">
                  {/* Left: Coins + status */}
                  <div className="betting-card">
                    <h4>Coins</h4>
                    <div className="chip-row">
                      {CHIP_VALUES.map((v) => (
                        <button
                          key={v}
                          className={`chip-btn ${selectedChip === v ? "active" : ""}`}
                          onClick={() => setSelectedChip(v)}
                        >
                          {v}
                        </button>
                      ))}
                    </div>

                    <div className="inline-note">
                      <small>
                        Status: <strong>{derived.status}</strong>
                        <br />
                        Round: <strong>{derived.roundId || "-"}</strong>
                        <br />
                        Stake: <strong>{totalStake}</strong>
                      </small>
                    </div>

                    {lastAckedBetPlacedByTable[String(tableId)] && (
                      <div className="inline-note">
                        <small>
                          <strong>Last ACK:</strong>{" "}
                          {new Date(
                            lastAckedBetPlacedByTable[String(tableId)].ackedAt,
                          ).toLocaleTimeString()}
                          <br />
                          Round:{" "}
                          {lastAckedBetPlacedByTable[String(tableId)].roundId ||
                            "-"}
                        </small>
                      </div>
                    )}
                  </div>

                  {/* Center: table + overlay + outside */}
                  <div className="betting-card">
                    <h4>Table</h4>

                    <div className="roulette-table-wrap">
                      {/* ZERO */}
                      <button
                        className="cell cell-zero"
                        onClick={() =>
                          placeChipOnBetIndex(getStraightBetIndex(0))
                        }
                        title="Straight 0 (betIndex 100)"
                      >
                        <span className="cell-label">0</span>
                        <span className="cell-amt">
                          {boardBets[String(getStraightBetIndex(0))] || ""}
                        </span>
                      </button>

                      <div
                        className="grid-overlay-container"
                        ref={containerRef}
                      >
                        <div className="grid-1to36">
                          {GRID_ROWS.map((row, rIdx) => (
                            <div
                              className="grid-row"
                              key={`row-${rIdx}`}
                              ref={rIdx === 0 ? sampleRowRef : undefined}
                            >
                              {row.map((n, cIdx) => {
                                const betIndex = getStraightBetIndex(n);
                                const amt = boardBets[String(betIndex)] || 0;
                                const isRed = RED_NUMBERS.has(n);

                                const isSample0 = rIdx === 0 && cIdx === 0;
                                const isSample1 = rIdx === 0 && cIdx === 1;

                                return (
                                  <button
                                    key={n}
                                    className={`cell ${isRed ? "cell-red" : "cell-black"}`}
                                    onClick={() =>
                                      placeChipOnBetIndex(betIndex)
                                    }
                                    title={`Straight ${n} (betIndex ${betIndex})`}
                                    ref={
                                      isSample0
                                        ? sampleCellRef
                                        : isSample1
                                          ? sampleCell2Ref
                                          : undefined
                                    }
                                  >
                                    <span className="cell-label">{n}</span>
                                    <span className="cell-amt">
                                      {amt || ""}
                                    </span>
                                  </button>
                                );
                              })}
                            </div>
                          ))}
                        </div>

                        {/* Overlay hotspots */}
                        <div
                          className="roulette-hotspots"
                          aria-label="Roulette bet hotspots overlay"
                        >
                          {ROULETTE_HOTSPOTS.map((s) => {
                            const amt = boardBets[String(s.betIndex)] || 0;

                            return (
                              <button
                                key={`${s.kind}-${s.betIndex}`}
                                className={`spot spot-${s.kind.toLowerCase()}`}
                                style={{
                                  ["--r"]: s.r,
                                  ["--c"]: s.c,
                                }}
                                onClick={() => placeChipOnBetIndex(s.betIndex)}
                                title={`${s.kind} betIndex ${s.betIndex}`}
                              >
                                {amt > 0 && (
                                  <span className="spot-amt">{amt}</span>
                                )}
                              </button>
                            );
                          })}
                        </div>
                      </div>
                    </div>

                    {/* Outside bets */}
                    <div className="outside-grid">
                      <button
                        className="outside-btn"
                        onClick={() => placeChipOnBetIndex(900)}
                      >
                        1 to 18 <span>{boardBets["900"] || ""}</span>
                      </button>
                      <button
                        className="outside-btn"
                        onClick={() => placeChipOnBetIndex(901)}
                      >
                        EVEN <span>{boardBets["901"] || ""}</span>
                      </button>
                      <button
                        className="outside-btn outside-red"
                        onClick={() => placeChipOnBetIndex(902)}
                      >
                        RED <span>{boardBets["902"] || ""}</span>
                      </button>
                      <button
                        className="outside-btn outside-black"
                        onClick={() => placeChipOnBetIndex(903)}
                      >
                        BLACK <span>{boardBets["903"] || ""}</span>
                      </button>
                      <button
                        className="outside-btn"
                        onClick={() => placeChipOnBetIndex(904)}
                      >
                        ODD <span>{boardBets["904"] || ""}</span>
                      </button>
                      <button
                        className="outside-btn"
                        onClick={() => placeChipOnBetIndex(905)}
                      >
                        19 to 36 <span>{boardBets["905"] || ""}</span>
                      </button>
                    </div>

                    {/* Dozens + Columns */}
                    <div className="dc-row">
                      <button
                        className="dc-btn"
                        onClick={() => placeChipOnBetIndex(800)}
                      >
                        1st 12 <span>{boardBets["800"] || ""}</span>
                      </button>
                      <button
                        className="dc-btn"
                        onClick={() => placeChipOnBetIndex(801)}
                      >
                        2nd 12 <span>{boardBets["801"] || ""}</span>
                      </button>
                      <button
                        className="dc-btn"
                        onClick={() => placeChipOnBetIndex(802)}
                      >
                        3rd 12 <span>{boardBets["802"] || ""}</span>
                      </button>

                      <button
                        className="dc-btn"
                        onClick={() => placeChipOnBetIndex(702)}
                      >
                        Col 1 <span>{boardBets["702"] || ""}</span>
                      </button>
                      <button
                        className="dc-btn"
                        onClick={() => placeChipOnBetIndex(701)}
                      >
                        Col 2 <span>{boardBets["701"] || ""}</span>
                      </button>
                      <button
                        className="dc-btn"
                        onClick={() => placeChipOnBetIndex(700)}
                      >
                        Col 3 <span>{boardBets["700"] || ""}</span>
                      </button>
                    </div>
                  </div>

                  {/* Right: bet slip */}
                  <div className="betting-card">
                    <h4>Bet Slip</h4>
                    {Object.keys(boardBets).length === 0 ? (
                      <small>No bets yet.</small>
                    ) : (
                      <div className="bet-slip">
                        {Object.entries(boardBets)
                          .sort((a, b) => Number(a[0]) - Number(b[0]))
                          .map(([betIndex, betAmount]) => (
                            <div className="bet-slip-row" key={betIndex}>
                              <span>#{betIndex}</span>
                              <strong>{betAmount}</strong>
                              <button
                                className="btn-small danger"
                                onClick={() => {
                                  setBoardBets((prev) => {
                                    const next = { ...prev };
                                    delete next[betIndex];
                                    return next;
                                  });
                                }}
                              >
                                Remove
                              </button>
                            </div>
                          ))}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* PLAYGROUND */}
          {activeTab === TABS.PLAYGROUND && (
            <div className="ws-panel">
              <div className="ws-panel-header">
                <h3>
                  🧪 Command Playground —{" "}
                  {TABLE_OPTIONS.find((t) => t.id === tableId)?.label}
                </h3>
                <div className="ws-panel-actions"></div>
              </div>

              <div className="playground-grid">
                <div className="playground-card">
                  <h4>PLACE_BET Builder</h4>

                  <div className="form-grid">
                    <div className="ws-form-field">
                      <label>Round ID</label>
                      <input
                        value={activeRoundIdForBet}
                        onChange={(e) =>
                          setPlaceBetRoundIdOverride(e.target.value)
                        }
                        placeholder="Auto-filled from server"
                      />
                      <small className="hint">
                        Current derived round:{" "}
                        <strong>{derived.roundId || "-"}</strong>
                      </small>
                    </div>

                    <div className="ws-form-field">
                      <label>Stake</label>
                      <input
                        type="number"
                        value={placeBetStake}
                        onChange={(e) => setPlaceBetStake(e.target.value)}
                        min={0}
                      />
                    </div>
                  </div>

                  <div className="bets-editor">
                    <div className="bets-header">
                      <strong>betsList</strong>
                      <button className="btn-small" onClick={addBetRow}>
                        + Add bet
                      </button>
                    </div>

                    {betsList.map((b, idx) => (
                      <div className="bet-row" key={`bet-${idx}`}>
                        <div className="ws-form-field">
                          <label>betIndex</label>
                          <input
                            type="number"
                            value={b.betIndex}
                            onChange={(e) =>
                              updateBetRow(idx, { betIndex: e.target.value })
                            }
                          />
                        </div>
                        <div className="ws-form-field">
                          <label>betAmount</label>
                          <input
                            type="number"
                            value={b.betAmount}
                            onChange={(e) =>
                              updateBetRow(idx, { betAmount: e.target.value })
                            }
                            min={0}
                          />
                        </div>
                        <button
                          className="btn-small danger"
                          onClick={() => removeBetRow(idx)}
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                  </div>

                  <div className="playground-actions">
                    <button
                      className="btn-send"
                      onClick={handleSendPlaceBet}
                      disabled={!connected}
                    >
                      📤 Send PLACE_BET
                    </button>
                  </div>

                  <div className="inline-note">
                    <small>
                      Expect <strong>BET_PLACED</strong> or{" "}
                      <strong>BET_REJECTED</strong>.
                    </small>
                  </div>
                </div>

                <div className="playground-card">
                  <h4>Raw JSON Sender</h4>
                  <textarea
                    className="raw-json"
                    value={rawJson}
                    onChange={(e) => setRawJson(e.target.value)}
                    placeholder={`Example:\n{\n  "type": "HEARTBEAT",\n  "payload": { "egmId": "${egmId}", "uid": "${uid}" }\n}`}
                  />
                  <div className="playground-actions">
                    <button
                      className="btn-send"
                      onClick={handleSendRawJson}
                      disabled={!connected || !rawJson.trim()}
                    >
                      📤 Send JSON
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* MESSAGES */}
          {activeTab === TABS.MESSAGES && (
            <div className="ws-panel">
              <div className="ws-panel-header">
                <h3>
                  📡 Messages —{" "}
                  {TABLE_OPTIONS.find((t) => t.id === tableId)?.label}
                </h3>
                <div className="ws-panel-actions">
                  <button
                    className="btn-action btn-clear"
                    onClick={handleClearTable}
                    disabled={messages.length === 0}
                  >
                    🗑️ Clear Table
                  </button>
                  <button
                    className="btn-action btn-clear-all"
                    onClick={handleClearAll}
                  >
                    🧹 Clear All
                  </button>
                  <button
                    className="btn-action btn-reconnect"
                    onClick={manualReconnect}
                  >
                    🔄 Reconnect
                  </button>
                </div>
              </div>

              <div className="message-filter">
                <label>Filter:</label>
                <select
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value)}
                  className="filter-select"
                >
                  <option value="ALL">All Messages ({messages.length})</option>
                  <option value="sent">
                    Sent ({messages.filter((m) => m.type === "sent").length})
                  </option>
                  <option value="received">
                    Received (
                    {messages.filter((m) => m.type === "received").length})
                  </option>
                </select>
              </div>

              <div className="messages-container">
                {filteredMessages.length === 0 ? (
                  <div className="no-messages">
                    <p>📭 No messages yet</p>
                    <small>
                      {connected
                        ? "Wait for server messages"
                        : "Connect to start communicating"}
                    </small>
                  </div>
                ) : (
                  <>
                    {filteredMessages.map((msg, index) => (
                      <div
                        key={index}
                        className={`message message-${msg.type}`}
                      >
                        <div className="message-header">
                          <span className="message-type-badge">
                            {getMessageTypeLabel(msg)}
                          </span>
                          <span className="message-time">
                            {new Date(msg.timestamp).toLocaleTimeString()}
                          </span>
                        </div>
                        <div className="message-content">
                          {renderMessageContent(msg.content)}
                        </div>
                      </div>
                    ))}
                    <div ref={messagesEndRef} />
                  </>
                )}
              </div>

              {connected && (
                <div className="connection-info">
                  <small>
                    ✅ Connected |{" "}
                    <strong>
                      {TABLE_OPTIONS.find((t) => t.id === tableId)?.label}
                    </strong>{" "}
                    | {messages.length} cached messages
                  </small>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;
