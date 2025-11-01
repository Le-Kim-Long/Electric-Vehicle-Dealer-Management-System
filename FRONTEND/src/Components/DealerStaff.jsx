import React, { useState, createContext, useContext } from 'react';
import './DealerStaff.css';
import HomePage from './Features/Home-page';
import VehicleInfoFeature from './Features/VehicleInfoFeature';
import CreateOrderFeature from './Features/CreateOrderFeature';
import OrderFeatureManagementPayment from './Features/OrderFeatureManagement&Payment';

const TestDriveContext = createContext();

export const useTestDrive = () => {
  const context = useContext(TestDriveContext);
  if (!context) {
    throw new Error('useTestDrive must be used within TestDriveProvider');
  }
  return context;
};

const DealerStaff = ({ user, onLogout }) => {
  const [activeFeature, setActiveFeature] = useState('dashboard');
  const [testDriveBookings, setTestDriveBookings] = useState([]);
  const [quoteRequests, setQuoteRequests] = useState([]);

  const addTestDriveBooking = (booking) => {
    const newBooking = {
      id: `TD${String(testDriveBookings.length + 1).padStart(3, '0')}`,
      ...booking,
      status: 'Chờ xác nhận',
      createdDate: new Date().toISOString().split('T')[0]
    };
    setTestDriveBookings(prev => [...prev, newBooking]);
  };

  const addQuoteRequest = (request) => {
    const newRequest = {
      id: `YC${String(quoteRequests.length + 1).padStart(3, '0')}`,
      ...request,
      status: 'Chờ xử lý',
      createdDate: new Date().toISOString().split('T')[0]
    };
    setQuoteRequests(prev => [...prev, newRequest]);
  };

  const updateTestDriveStatus = (id, newStatus) => {
    setTestDriveBookings(prev => 
      prev.map(booking => 
        booking.id === id ? { ...booking, status: newStatus } : booking
      )
    );
  };

  const updateQuoteStatus = (id, newStatus) => {
    setQuoteRequests(prev => 
      prev.map(request => 
        request.id === id ? { ...request, status: newStatus } : request
      )
    );
  };

  const handleMenuClick = (featureId) => {
    setActiveFeature(featureId);
  };

  const renderMainContent = () => {
    switch (activeFeature) {
      case 'dashboard':
        return <HomePage onMenuClick={handleMenuClick} />;
      case 'vehicle-info':
        return <VehicleInfoFeature />;
      case 'create-order':
        return <CreateOrderFeature />;
      case 'payment':
        return <OrderFeatureManagementPayment />;
      default:
        return null;
    }
  };

  return (
    <TestDriveContext.Provider value={{ 
      testDriveBookings, 
      addTestDriveBooking, 
      updateTestDriveStatus,
      quoteRequests,
      addQuoteRequest,
      updateQuoteStatus 
    }}>
      <div className="new-dealer-staff-layout">
        {/* Sidebar bên trái */}
        <div className="sidebar">
          <div className="sidebar-header">
            <div className="logo">
              <span className="logo-icon">🚗</span>
              <span className="logo-text">Dealer Staff</span>
            </div>
          </div>
          
          <nav className="sidebar-menu">
            <div 
              className={`menu-item ${activeFeature === 'dashboard' ? 'active' : ''}`}
              onClick={() => handleMenuClick('dashboard')}
            >
              <span className="menu-icon">🏠</span>
              <span className="menu-text">Trang chủ</span>
            </div>
            
            <div 
              className={`menu-item ${activeFeature === 'vehicle-info' ? 'active' : ''}`}
              onClick={() => handleMenuClick('vehicle-info')}
            >
              <span className="menu-icon">🚗</span>
              <span className="menu-text">Truy vấn thông tin xe</span>
            </div>
            
            <div 
              className={`menu-item ${activeFeature === 'create-order' ? 'active' : ''}`}
              onClick={() => handleMenuClick('create-order')}
            >
              <span className="menu-icon">📋</span>
              <span className="menu-text">Tạo đơn hàng</span>
            </div>
            
            <div 
              className={`menu-item ${activeFeature === 'payment' ? 'active' : ''}`}
              onClick={() => handleMenuClick('payment')}
            >
              <span className="menu-icon">💳</span>
              <span className="menu-text">Quản lý Đơn hàng & Thanh toán</span>
            </div>
          </nav>
        </div>

        {/* Main content bên phải */}
        <div className="main-content">
          {/* Header */}
          <header className="top-header">
            <div className="header-left">
              <h1>EV Dealer Management - {user.dealerName}</h1>
            </div>
            
            <div className="header-right">
              <div className="user-info">
                <div className="user-avatar">
                  {user.username ? user.username.charAt(0).toUpperCase() : 'U'}
                </div>
                <div className="user-details">
                  <span className="user-name">{user.username}</span>
                  <span className="user-role">Nhân viên đại lý</span>
                </div>
              </div>
              <button onClick={onLogout} className="logout-button">
                Đăng xuất
              </button>
            </div>
          </header>

          {/* Content area */}
          <main className="content-area">
            {renderMainContent()}
          </main>
        </div>
      </div>
    </TestDriveContext.Provider>
  );
};

export default DealerStaff;