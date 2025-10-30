
import React, { useState } from 'react';
import './DealerManager.css';
import DealerCarManagement from '../ManagerFeatures/DealerCarManagement';
import OrderManagement from '../ManagerFeatures/OrderManagement';
import CustomerManagement from '../ManagerFeatures/CustomerManagement';
import PromotionManagement from '../ManagerFeatures/PromotionManagement';

const DealerManager = ({ user, onLogout }) => {
  const [activeFeature, setActiveFeature] = useState('car-management');

  const handleMenuClick = (featureId) => {
    setActiveFeature(featureId);
  };

  const renderMainContent = () => {
    switch (activeFeature) {
      case 'car-management':
        return <DealerCarManagement />;
      case 'order-management':
        return <OrderManagement />;
      case 'customer-management':
        return <CustomerManagement />;
      case 'promotion-management':
        return <PromotionManagement />;
      default:
        return null;
    }
  };

  return (
    <div className="new-dealer-manager-layout">
      <div className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">👔</span>
            <span className="logo-text">Manager</span>
          </div>
        </div>
        <nav className="sidebar-menu">
          <div
            className={`menu-item ${activeFeature === 'car-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('car-management')}
          >
            <span className="menu-icon">🚗</span>
            <span className="menu-text">Quản lý xe cho đại lý</span>
          </div>
          <div
            className={`menu-item ${activeFeature === 'order-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('order-management')}
          >
            <span className="menu-icon">📦</span>
            <span className="menu-text">Quản lý đơn hàng</span>
          </div>
          <div
            className={`menu-item ${activeFeature === 'customer-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('customer-management')}
          >
            <span className="menu-icon">👥</span>
            <span className="menu-text">Quản lý khách hàng</span>
          </div>
          <div
            className={`menu-item ${activeFeature === 'promotion-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('promotion-management')}
          >
            <span className="menu-icon">🏷️</span>
            <span className="menu-text">Quản lý khuyến mãi</span>
          </div>
        </nav>
      </div>
      <div className="main-content">
        <header className="top-header">
          <div className="header-left">
            <h1>
              EV Dealer Management{user.dealerName ? ` - ${user.dealerName}` : ''}
            </h1>
          </div>
          <div className="header-right">
            <div className="user-info">
              <div className="user-avatar">
                {user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}
              </div>
              <div className="user-details">
                <span className="user-name">{user.name || user.username}</span>
                <span className="user-role">Quản lý đại lý</span>
                
              </div>
            </div>
            <button onClick={onLogout} className="logout-button">
              Đăng xuất
            </button>
          </div>
        </header>
        <main className="content-area">
          {renderMainContent()}
        </main>
      </div>
    </div>
  );
}

export default DealerManager;
