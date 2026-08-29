import { createContext, useContext, useState } from 'react';
import { decodeJwtClaims } from '../api.js';

const AuthContext = createContext(null);
const TOKEN_KEY = 'venuego.console.token';

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY) || '');
  const [claims, setClaims] = useState(() => decodeJwtClaims(localStorage.getItem(TOKEN_KEY) || ''));

  function login(newToken) {
    localStorage.setItem(TOKEN_KEY, newToken);
    setToken(newToken);
    setClaims(decodeJwtClaims(newToken));
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    setToken('');
    setClaims(null);
  }

  return (
    <AuthContext.Provider value={{ token, claims, login, logout, isAuthed: Boolean(token) }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
