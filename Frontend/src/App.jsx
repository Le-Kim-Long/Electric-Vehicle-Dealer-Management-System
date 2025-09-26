import React, { useState, useEffect } from 'react';
import Login from './Components/Login';
import DealerManager from './Components/DealerManager';
import DealerStaff from './Components/DealerStaff';
import Admin from './Components/Admin';
import EVMStaff from './Components/EVMStaff';
import './App.css';

function App() {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in (from localStorage)
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (error) {
        localStorage.removeItem('user');
      }
    }
    setIsLoading(false);
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Đang tải...</p>
      </div>
    );
  }

  if (!user) {
    return <Login onLogin={handleLogin} />;
  }

  // Render appropriate dashboard based on user role
  const renderDashboard = () => {
    switch (user.role) {
      case 'dealer-staff':
        return <DealerStaff user={user} onLogout={handleLogout} />;
      case 'dealer-manager':
        return <DealerManager user={user} onLogout={handleLogout} />;
      case 'evm-staff':
        return <EVMStaff user={user} onLogout={handleLogout} />;
      case 'admin':
        return <Admin user={user} onLogout={handleLogout} />;
      default:
        return (
          <div className="dashboard-container">
            <h1>❌ Lỗi</h1>
            <p>Vai trò không được hỗ trợ</p>
            <button onClick={handleLogout} className="logout-button">Đăng xuất</button>
          </div>
        );
    }
  };

  return (
    <div className="App">
      {renderDashboard()}
    </div>
  );
}

export default App;
