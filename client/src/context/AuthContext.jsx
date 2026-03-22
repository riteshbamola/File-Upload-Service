import { createContext, useContext, useEffect, useState } from "react";
import api from "../api/axios";
import { setAuthToken } from "../api/authToken";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [accessToken, setAccessToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const restoreSession = async () => {
      try {
        const res = await api.post("/refresh");
        setAccessToken(res.data.accessToken);
        setAuthToken(res.data.accessToken);
      } catch {
        // not logged in (refresh token missing/expired)
      } finally {
        setLoading(false); // 🔥 critical
      }
    };

    restoreSession();
  }, []);

  return (
    <AuthContext.Provider value={{ accessToken, setAccessToken, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
