import { create } from "zustand";
import { persist } from "zustand/middleware";

/**
 * Replacement store that matches your current API shape (connect/sendMessage/etc)
 * but adds:
 * 1) NO initial "CONNECTION" message on open
 * 2) HEARTBEAT sent on interval (payload: {egmId, uid})
 * 3) Switch between 2 tables and KEEP messages/history per table
 * 4) Persistence across reloads via localStorage (Zustand persist)
 *
 * Backwards compatibility notes:
 * - connect(url) still works
 * - manualReconnect(), disconnect(), clearMessages(), isConnected() still exist
 *
 * New additions for AdminDashboard:
 * - setActiveTable(tableId)
 * - clearAllTables()
 *
 * Storage size is bounded to avoid localStorage bloat.
 */

const STORAGE_KEY = "dealer-ws-store-v1";
const DEFAULT_HEARTBEAT_SECONDS = 30;
const MAX_MESSAGES_PER_TABLE = 500;

function parseWsUrl(url) {
  try {
    const u = new URL(url);
    const egmId = u.searchParams.get("egmId");
    const uid = u.searchParams.get("uid");
    const tableId = u.searchParams.get("tableId");
    return { egmId, uid, tableId };
  } catch {
    return { egmId: null, uid: null, tableId: null };
  }
}

function trimToMax(list) {
  if (!Array.isArray(list)) return [];
  if (list.length <= MAX_MESSAGES_PER_TABLE) return list;
  return list.slice(list.length - MAX_MESSAGES_PER_TABLE);
}

function appendMessage(messagesByTable, tableId, entry) {
  const key = String(tableId || "unknown");
  const list = messagesByTable[key] || [];
  const next = trimToMax([...list, entry]);
  return { ...messagesByTable, [key]: next };
}

const useWebSocketStore = create(
  persist(
    (set, get) => ({
      // runtime-only
      ws: null,
      wsStatus: "Disconnected",
      reconnectTimeout: null,
      reconnectAttempts: 0,
      maxReconnectAttempts: 5,
      isManualDisconnect: false,
      wsUrl: null,

      // heartbeat runtime-only
      heartbeatTimer: null,
      heartbeatIntervalSeconds: DEFAULT_HEARTBEAT_SECONDS,

      // persisted active context
      activeTableId: "1",
      activeEgmId: "123456",
      activeUid: "23869508",

      // persisted caches
      messagesByTable: {},
      // convenience view of current table
      messages: [],

      hydrateMessagesView: () => {
        const st = get();
        const key = String(st.activeTableId || "unknown");
        set({ messages: st.messagesByTable[key] || [] });
      },

      setActiveTable: (tableId) => {
        const key = String(tableId);
        const st = get();
        set({
          activeTableId: key,
          messages: st.messagesByTable[key] || [],
        });
      },

      setHeartbeatIntervalSeconds: (seconds) => {
        const s = Number(seconds);
        if (!Number.isFinite(s) || s <= 0) return;

        set({ heartbeatIntervalSeconds: s });

        const st = get();
        if (st.ws && st.ws.readyState === WebSocket.OPEN) {
          get()._stopHeartbeat();
          get()._startHeartbeat();
        }
      },

      _stopHeartbeat: () => {
        const st = get();
        if (st.heartbeatTimer) {
          clearInterval(st.heartbeatTimer);
          set({ heartbeatTimer: null });
        }
      },

      _startHeartbeat: () => {
        const st = get();
        if (st.heartbeatTimer) return;

        const intervalMs = Math.max(
          1000,
          (st.heartbeatIntervalSeconds || DEFAULT_HEARTBEAT_SECONDS) * 1000,
        );

        const timer = setInterval(() => {
          const s = get();
          if (!s.ws || s.ws.readyState !== WebSocket.OPEN) return;
          if (!s.activeEgmId || !s.activeUid) return;

          // Per API doc:
          // { "type":"HEARTBEAT", "payload": { "egmId":"...", "uid":"..." } }
          get().sendMessage({
            type: "HEARTBEAT",
            payload: { egmId: String(s.activeEgmId), uid: String(s.activeUid) },
          });
        }, intervalMs);

        set({ heartbeatTimer: timer });
      },

      // Initialize WebSocket connection (backward compatible signature: connect(url))
      connect: (url) => {
        const st = get();
        const parsed = parseWsUrl(url);

        const nextTableId = String(
          parsed.tableId ?? st.activeTableId ?? "unknown",
        );
        const nextEgmId = String(parsed.egmId ?? st.activeEgmId ?? "");
        const nextUid = String(parsed.uid ?? st.activeUid ?? "");

        // set active context + show cached messages immediately
        set({
          activeTableId: nextTableId,
          activeEgmId: nextEgmId,
          activeUid: nextUid,
          messages: st.messagesByTable[nextTableId] || [],
        });

        // If already connected to same URL, skip
        if (st.ws && st.ws.url === url && st.ws.readyState === WebSocket.OPEN) {
          console.log("WebSocket already connected");
          return;
        }

        // clean up old socket/timers
        if (st.ws) st.ws.close();
        get()._stopHeartbeat();

        try {
          set({
            wsStatus: "Connecting...",
            wsUrl: url,
            isManualDisconnect: false,
          });
          const ws = new WebSocket(url);

          ws.onopen = () => {
            console.log("WebSocket connected successfully");
            set({
              wsStatus: "Connected",
              reconnectAttempts: 0,
              isManualDisconnect: false,
            });

            // IMPORTANT: do not send initial "CONNECTION" message
            get()._startHeartbeat();
          };

          ws.onmessage = (event) => {
            console.log("WebSocket message received:", event.data);

            let content = event.data;
            try {
              const data = JSON.parse(event.data);
              content = data;

              // If INITIAL_DATA provides session heartbeatInterval, adopt it
              if (
                data?.type === "INITIAL_DATA" &&
                data?.payload?.session?.heartbeatInterval
              ) {
                get().setHeartbeatIntervalSeconds(
                  data.payload.session.heartbeatInterval,
                );
              }
            } catch {
              // keep string
            }

            const s = get();
            const entry = {
              type: "received",
              content,
              timestamp: new Date().toISOString(),
            };

            const updated = appendMessage(
              s.messagesByTable,
              s.activeTableId,
              entry,
            );
            set({
              messagesByTable: updated,
              messages: updated[s.activeTableId] || [],
            });
          };

          ws.onerror = (error) => {
            console.error("WebSocket error:", error);
            set({ wsStatus: "Error" });
          };

          ws.onclose = (event) => {
            console.log("WebSocket closed:", event.code, event.reason);

            get()._stopHeartbeat();
            set({ wsStatus: "Disconnected", ws: null });

            const s = get();

            if (
              !s.isManualDisconnect &&
              s.reconnectAttempts < s.maxReconnectAttempts
            ) {
              const delay = Math.min(
                1000 * Math.pow(2, s.reconnectAttempts),
                30000,
              );
              console.log(`Attempting to reconnect in ${delay}ms...`);

              const timeout = setTimeout(() => {
                set((prev) => ({
                  reconnectAttempts: prev.reconnectAttempts + 1,
                }));
                get().connect(s.wsUrl);
              }, delay);

              set({ reconnectTimeout: timeout });
            } else if (s.reconnectAttempts >= s.maxReconnectAttempts) {
              set({ wsStatus: "Connection Failed" });
              console.error("Max reconnection attempts reached");
            }
          };

          set({ ws });
        } catch (error) {
          console.error("Error creating WebSocket:", error);
          set({ wsStatus: "Connection Failed" });
        }
      },

      // Send message through WebSocket
      sendMessage: (message) => {
        const st = get();

        if (st.ws && st.ws.readyState === WebSocket.OPEN) {
          const msg =
            typeof message === "string" ? message : JSON.stringify(message);
          st.ws.send(msg);

          const entry = {
            type: "sent",
            content: message,
            timestamp: new Date().toISOString(),
          };

          const updated = appendMessage(
            st.messagesByTable,
            st.activeTableId,
            entry,
          );
          set({
            messagesByTable: updated,
            messages: updated[st.activeTableId] || [],
          });

          return true;
        }
        return false;
      },

      // Manually disconnect WebSocket (does NOT clear cached messages)
      disconnect: () => {
        const st = get();
        console.log("Manually disconnecting WebSocket...");

        set({ isManualDisconnect: true });

        if (st.reconnectTimeout) {
          clearTimeout(st.reconnectTimeout);
          set({ reconnectTimeout: null });
        }

        get()._stopHeartbeat();

        if (st.ws && st.ws.readyState === WebSocket.OPEN) {
          st.ws.close(1000, "User logout");
        }

        set({
          ws: null,
          wsStatus: "Disconnected",
          reconnectAttempts: 0,
          isManualDisconnect: true,
        });
      },

      // Manual reconnect
      manualReconnect: () => {
        const st = get();

        if (st.reconnectTimeout) clearTimeout(st.reconnectTimeout);

        get()._stopHeartbeat();
        set({ reconnectAttempts: 0, isManualDisconnect: false });

        if (st.ws) st.ws.close();

        if (st.wsUrl) {
          get().connect(st.wsUrl);
        }
      },

      // Clear messages ONLY for active table (keeps other table cache)
      clearMessages: () => {
        const st = get();
        const key = String(st.activeTableId || "unknown");
        const updated = { ...st.messagesByTable, [key]: [] };
        set({ messagesByTable: updated, messages: [] });
      },

      // Clear all tables cache (and persisted storage)
      clearAllTables: () => {
        set({ messagesByTable: {}, messages: [] });
      },

      // Check if connected
      isConnected: () => {
        const st = get();
        return st.ws && st.ws.readyState === WebSocket.OPEN;
      },
    }),
    {
      name: STORAGE_KEY,
      version: 1,
      partialize: (state) => ({
        activeTableId: state.activeTableId,
        activeEgmId: state.activeEgmId,
        activeUid: state.activeUid,
        messagesByTable: state.messagesByTable,
        messages: state.messages,
        heartbeatIntervalSeconds: state.heartbeatIntervalSeconds,
      }),
      onRehydrateStorage: () => (state, error) => {
        if (error) {
          console.error("Failed to rehydrate ws store:", error);
          return;
        }
        try {
          state?.hydrateMessagesView?.();
        } catch (e) {
          console.error("hydrateMessagesView failed:", e);
        }
      },
    },
  ),
);

export default useWebSocketStore;
