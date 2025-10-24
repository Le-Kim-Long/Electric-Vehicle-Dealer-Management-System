import React from 'react';
import './Admin.css';
import UserManagement from './AdminFeatures/UserManagement';

const Admin = ({ user, onLogout }) => {
  return (
    <div className="admin-layout">
      <header className="admin-header">
        <div className="header-left">
          <h1>EV Dealer Management System - Admin</h1>
        </div>
        
        <div className="header-right">
          <div className="user-info">
            <div className="user-avatar">
              {user.username ? user.username.charAt(0).toUpperCase() : 'A'}
            </div>
            <div className="user-details">
              <span className="user-name">{user.username || 'Admin'}</span>
              <span className="user-role">Quản trị viên</span>
            </div>
          </div>
          <button onClick={onLogout} className="logout-button">
            Đăng xuất
          </button>
        </div>
      </header>
      
      <main className="admin-content">
        <UserManagement />
      </main>
    </div>
  );
};

export default Admin;
