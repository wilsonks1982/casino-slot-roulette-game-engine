import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      token: null,

      login: async (credentials) => {
        try {
          const response = await fetch("/api/v1/login", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(credentials),
          });

          if (!response.ok) {
            throw new Error("Login failed");
          }

          const data = await response.json();
          set({ user: data.user, token: data.token });

          return data.user; // Return user for role checking
        } catch (error) {
          console.error("Login failed:", error);
          throw error;
        }
      },

      logout: async () => {
        const token = get().token;

        try {
          // Import WebSocket store dynamically to avoid circular dependencies
          const { default: useWebSocketStore } =
            await import("./useWebSocketStore");

          // Disconnect WebSocket before logout
          useWebSocketStore.getState().disconnect();

          await fetch("/api/v1/logout", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          });
        } catch (error) {
          console.error("Logout request failed:", error);
        } finally {
          set({ user: null, token: null });
        }
      },

      isAuthenticated: () => {
        return get().token !== null;
      },

      hasRole: (role) => {
        const user = get().user;
        return user?.roles?.includes(role) || false;
      },

      hasAnyRole: (roles) => {
        const user = get().user;
        return roles.some((role) => user?.roles?.includes(role)) || false;
      },

      getDefaultRoute: () => {
        const user = get().user;
        if (!user || !user.roles) return "/";

        // Priority-based routing
        if (user.roles.includes("SUPER_ADMIN")) return "/super";
        if (user.roles.includes("ADMIN")) return "/admin";
        if (user.roles.includes("DEALER")) return "/dealer";

        return "/";
      },
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => localStorage),
    },
  ),
);

export default useAuthStore;
