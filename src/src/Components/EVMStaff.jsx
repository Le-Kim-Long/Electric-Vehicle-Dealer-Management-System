import React, { useState } from 'react';
import './EVMStaff.css';
import CarManagement from './EVMStaffFeatures/CarManagement';

const EVMStaff = ({ user, onLogout }) => {
  const [activeFeature, setActiveFeature] = useState('product-management');

  const handleMenuClick = () => {
    setActiveFeature('product-management');
  };

  const renderMainContent = () => {
    return <div className="feature-content"><h3>Quản lý xe</h3><p>Chức năng quản lý xe sẽ được phát triển...</p></div>;
  };

  return (
    <div className="evmstaff-layout">
      <header className="evmstaff-header">
        <div className="header-left">
          <h1>EV Dealer Management System - EVM Staff</h1>
        </div>
        <div className="header-right">
          <div className="user-info">
            <div className="user-avatar">
              {user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}
            </div>
            <div className="user-details">
              <span className="user-name">{user.name || user.username}</span>
              <span className="user-role">Nhân viên hãng xe</span>
            </div>
          </div>
          <button onClick={onLogout} className="logout-button">
            Đăng xuất
          </button>
        </div>
      </header>
      <main className="evmstaff-content">
        <CarManagement />
      </main>
    </div>
  );
};

export default EVMStaff;
