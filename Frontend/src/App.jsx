import React, { useState, useEffect } from 'react';
import './App.css';
import Login from './Components/Login';
import DealerStaff from './Components/DealerStaff';
import DealerManager from './Components/DealerManager';
import Admin from './Components/Admin';
import EVMStaff from './Components/EVMStaff';

function App() {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Check if user is logged in when app starts
  useEffect(() => {
    const checkExistingAuth = () => {
      try {
        // Đổi từ 'user' thành 'userData' để match với Login.jsx
        const storedUser = localStorage.getItem('userData') || sessionStorage.getItem('userData');
        const storedToken = localStorage.getItem('token') || sessionStorage.getItem('token');
        
        if (storedUser && storedToken) {
          const userData = JSON.parse(storedUser);
          
          if (userData.username && userData.role) {
            setUser(userData);
            window.authToken = storedToken;
            console.log('Auto-login successful:', userData.username);
          } else {
            clearStoredAuth();
          }
        }
      } catch (error) {
        console.error('Error checking existing auth:', error);
        clearStoredAuth();
      } finally {
        setIsLoading(false);
      }
    };

    checkExistingAuth();
  }, []);

  // Clear stored authentication data
  const clearStoredAuth = () => {
    localStorage.removeItem('userData');  // Đổi từ 'user'
    localStorage.removeItem('token');
    sessionStorage.removeItem('userData');  // Đổi từ 'user'
    sessionStorage.removeItem('token');
    delete window.authToken;
  };

  // Handle successful login
  const handleLogin = (userData) => {
    console.log('Login successful:', userData);
    setUser(userData);
  };

  // Handle logout
  const handleLogout = async () => {
    try {
      // Optional: Call logout API
      // const API_BASE_URL = 'http://localhost:8080/api';
      // await fetch(`${API_BASE_URL}/auth/logout`, {
      //   method: 'POST',
      //   headers: {
      //     'Authorization': `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token')}`,
      //     'Content-Type': 'application/json',
      //   },
      // });
      
      console.log('Logging out user:', user?.username);
      
      // Clear stored data
      clearStoredAuth();
      
      // Clear user state
      setUser(null);
      
      console.log('Logout successful');
    } catch (error) {
      console.error('Logout error:', error);
      // Force logout even if API fails
      clearStoredAuth();
      setUser(null);
    }
  };

  // Loading screen
  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Đang tải...</p>
      </div>
    );
  }

  // Show login if no user
  if (!user) {
    return <Login onLogin={handleLogin} />;
  }

  // Render appropriate component based on user role
  const renderUserInterface = () => {
    switch (user.role) {
      case 'DealerStaff':  // Thay từ 'dealer-staff'
        return <DealerStaff user={user} onLogout={handleLogout} />;
      
      case 'DealerManager':  // Thay từ 'dealer-manager'
        return <DealerManager user={user} onLogout={handleLogout} />;
      
      case 'EVMStaff':  // Thay từ 'evm-staff'
        return <EVMStaff user={user} onLogout={handleLogout} />;
      
      case 'Admin':  // Thay từ 'admin'
        return <Admin user={user} onLogout={handleLogout} />;
      
      default:
        console.warn('Unknown user role:', user.role);
        handleLogout();
        return <Login onLogin={handleLogin} />;
    }
  };

  return (
    <div className="App">
      {renderUserInterface()}
    </div>
  );
}

export default App;
