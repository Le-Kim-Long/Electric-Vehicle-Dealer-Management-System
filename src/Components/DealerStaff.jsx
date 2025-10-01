import React, { useState, createContext, useContext } from 'react';
import './DealerStaff.css';
import HomePage from './Features/Home-page'; // Import component mới
import VehicleInfoFeature from './Features/VehicleInfoFeature';
import CreateOrderFeature from './Features/CreateOrderFeature';
import CustomerManagementFeature from './Features/CustomerManagementFeature';
import FeedbackTestDriveFeature from './Features/FeedbackTestDriveFeature';

// Create TestDrive Context - Giữ để FeedbackTestDriveFeature sử dụng
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
        return <HomePage onMenuClick={handleMenuClick} />; // Sử dụng component mới
      case 'vehicle-info':
        return <VehicleInfoFeature />;
      case 'create-order':
        return <CreateOrderFeature />;
      case 'customer-management':
        return <CustomerManagementFeature />;
      case 'payment':
        return (
          <div className="feature-content">
            <h3>💳 Quản lý Thanh toán</h3>
            <div className="payment-dashboard">
              <div className="payment-stats">
                <div className="payment-stat-card">
                  <h4>Tổng doanh thu tháng</h4>
                  <p className="payment-amount">45.2 tỷ VNĐ</p>
                </div>
                <div className="payment-stat-card">
                  <h4>Đã thanh toán</h4>
                  <p className="payment-amount">38.7 tỷ VNĐ</p>
                </div>
                <div className="payment-stat-card">
                  <h4>Chờ thanh toán</h4>
                  <p className="payment-amount">6.5 tỷ VNĐ</p>
                </div>
              </div>
              
              <div className="payment-table">
                <h4>Giao dịch gần đây</h4>
                <div className="table-container">
                  <div className="table-header">
                    <div>Mã GD</div>
                    <div>Khách hàng</div>
                    <div>Số tiền</div>
                    <div>Phương thức</div>
                    <div>Trạng thái</div>
                    <div>Ngày</div>
                  </div>
                  <div className="table-row">
                    <div>PAY001</div>
                    <div>Nguyễn Văn A</div>
                    <div>1.2 tỷ VNĐ</div>
                    <div>Chuyển khoản</div>
                    <div><span className="status-completed">HOÀN THÀNH</span></div>
                    <div>26/09/2025</div>
                  </div>
                  <div className="table-row">
                    <div>PAY002</div>
                    <div>Trần Thị B</div>
                    <div>500 triệu VNĐ</div>
                    <div>Tiền mặt</div>
                    <div><span className="status-confirmed">CHỜ XÁC NHẬN</span></div>
                    <div>25/09/2025</div>
                  </div>
                  <div className="table-row">
                    <div>PAY003</div>
                    <div>Lê Văn C</div>
                    <div>950 triệu VNĐ</div>
                    <div>Vay ngân hàng</div>
                    <div><span className="status-new">ĐANG XỬ LÝ</span></div>
                    <div>24/09/2025</div>
                  </div>
                </div>
              </div>
              
              <div className="payment-actions">
                <button className="payment-button">Tạo hóa đơn mới</button>
                <button className="payment-button">Xử lý thanh toán</button>
                <button className="payment-button">Xuất báo cáo</button>
              </div>
            </div>
          </div>
        );
      case 'feedback-test-drive':
        return <FeedbackTestDriveFeature testDriveBookings={testDriveBookings} quoteRequests={quoteRequests} />;
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
              className={`menu-item ${activeFeature === 'customer-management' ? 'active' : ''}`}
              onClick={() => handleMenuClick('customer-management')}
            >
              <span className="menu-icon">👥</span>
              <span className="menu-text">Quản lý khách hàng</span>
            </div>
            
            <div 
              className={`menu-item ${activeFeature === 'payment' ? 'active' : ''}`}
              onClick={() => handleMenuClick('payment')}
            >
              <span className="menu-icon">💳</span>
              <span className="menu-text">Thanh toán</span>
            </div>
            
            <div 
              className={`menu-item ${activeFeature === 'feedback-test-drive' ? 'active' : ''}`}
              onClick={() => handleMenuClick('feedback-test-drive')}
            >
              <span className="menu-icon">💬</span>
              <span className="menu-text">Phản hồi & Lái thử</span>
            </div>
          </nav>
        </div>

        <div className="main-content">
          <header className="top-header">
            <div className="header-left">
              <h1>Phần mềm quản lý bán xe điện thông qua kênh đại lý</h1>
            </div>
            
            <div className="header-right">
              <div className="user-info">
                <div className="user-avatar">
                  {user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}
                </div>
                <div className="user-details">
                  <span className="user-name">{user.name || user.username}</span>
                  <span className="user-role">Nhân viên đại lý</span>
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
    </TestDriveContext.Provider>
  );
};

export default DealerStaff;