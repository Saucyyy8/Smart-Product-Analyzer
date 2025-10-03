import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';

interface AuthContextType {
  isAuthenticated: boolean;
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);

const AUTH_TOKEN_KEY = 'authToken';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem(AUTH_TOKEN_KEY);
  });
  const navigate = useNavigate();

  useEffect(() => {
    // This will handle the OAuth2 redirect with token
    const handleOAuthRedirect = () => {
      const urlParams = new URLSearchParams(window.location.search);
      const tokenFromUrl = urlParams.get('token');
      
      if (tokenFromUrl) {
        // Store the token and clean up the URL
        login(tokenFromUrl);
        const cleanUrl = window.location.origin + window.location.pathname;
        window.history.replaceState({}, document.title, cleanUrl);
      }
    };

    handleOAuthRedirect();
  }, []);

  const login = (newToken: string) => {
    localStorage.setItem(AUTH_TOKEN_KEY, newToken);
    setToken(newToken);
    navigate('/');
  };

  const logout = async () => {
    try {
      // Call your backend logout endpoint if needed
      await fetch('http://localhost:8080/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      });
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      localStorage.removeItem(AUTH_TOKEN_KEY);
      setToken(null);
      navigate('/login');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated: !!token,
        token,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
