import React, { useState, createContext, useContext } from 'react';
import './DealerStaff.css';
import VehicleInfoFeature from './Features/VehicleInfoFeature';
import CreateOrderFeature from './Features/CreateOrderFeature';
import CustomerManagementFeature from './Features/CustomerManagementFeature';
import FeedbackTestDriveFeature from './Features/FeedbackTestDriveFeature';

// Create TestDrive Context
const TestDriveContext = createContext();

export const useTestDrive = () => {
  const context = useContext(TestDriveContext);
  if (!context) {
    throw new Error('useTestDrive must be used within TestDriveProvider');
  }
  return context;
};

const DealerStaff = ({ user, onLogout }) => {
  const [activeFeature, setActiveFeature] = useState(null);
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

  const handleFeatureClick = (featureId) => {
    setActiveFeature(featureId);
  };

  const renderFeatureContent = () => {
    switch (activeFeature) {
      case 'vehicle-info':
        return <VehicleInfoFeature />;
      case 'create-order':
        return <CreateOrderFeature />;
      case 'customer-management':
        return <CustomerManagementFeature />;
      case 'feedback-test-drive':
        return <FeedbackTestDriveFeature testDriveBookings={testDriveBookings} quoteRequests={quoteRequests} />;
      default:
        return null;
    }
  };

  if (activeFeature) {
    return (
      <TestDriveContext.Provider value={{ 
        testDriveBookings, 
        addTestDriveBooking, 
        updateTestDriveStatus,
        quoteRequests,
        addQuoteRequest,
        updateQuoteStatus 
      }}>
        <div className="dealer-staff-container feature-fullscreen">
        <header className="dealer-staff-header">
          <div className="header-left">
            <button 
              className="back-button"
              onClick={() => setActiveFeature(null)}
            >
               Quay lại Dashboard
            </button>
            <div className="header-brand">
              <div className="brand-icon">⚡</div>
              <div className="header-title">
                <h1>Dealer Staff</h1>
                <span className="header-subtitle">Nhân viên bán hàng</span>
              </div>
            </div>
          </div>
          
          <div className="user-info">
            <div className="user-profile">
              <div className="user-avatar">{user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}</div>
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
        
        <main className="feature-main">
          {renderFeatureContent()}
        </main>
      </div>
      </TestDriveContext.Provider>
    );
  }

  return (
    <TestDriveContext.Provider value={{ 
      testDriveBookings, 
      addTestDriveBooking, 
      updateTestDriveStatus,
      quoteRequests,
      addQuoteRequest,
      updateQuoteStatus 
    }}>
      <div className="dealer-staff-container">
      <header className="dealer-staff-header">
        <div className="header-left">
          <div className="header-brand">
            <div className="brand-icon">⚡</div>
            <div className="header-title">
              <h1>Dealer Staff</h1>
              <span className="header-subtitle">Nhân viên bán hàng</span>
            </div>
          </div>
        </div>
        
        <div className="user-info">
          <div className="user-profile">
            <div className="user-avatar">{user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}</div>
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
      
      <main className="dealer-staff-main">
        <div className="welcome-container">
          <div className="welcome-section">
            <h2>Chào mừng đến với hệ thống quản lý bán xe điện</h2>
            <p>Vai trò: {user.roleLabel}</p>
          </div>
        </div>
        
        <div className="features-container">
          <div className="features-preview">
            <h3>Chức năng chính:</h3>
            <div className="feature-list">
              <div className="feature-card" onClick={() => handleFeatureClick('vehicle-info')}>
                <div className="feature-icon">🚗</div>
                <div className="feature-content">
                  <h4>Truy vấn thông tin xe</h4>
                  <p>Xem danh mục xe điện, thông số kỹ thuật, giá bán và tình trạng tồn kho</p>
                </div>
              </div>
              
              <div className="feature-card" onClick={() => handleFeatureClick('create-order')}>
                <div className="feature-icon">📋</div>
                <div className="feature-content">
                  <h4>Tạo đơn hàng</h4>
                  <p>Lập đơn hàng mới, tính toán giá, áp dụng khuyến mãi và theo dõi tiến trình</p>
                </div>
              </div>
              
              <div className="feature-card" onClick={() => handleFeatureClick('customer-management')}>
                <div className="feature-icon">👥</div>
                <div className="feature-content">
                  <h4>Quản lý khách hàng</h4>
                  <p>Quản lý thông tin khách hàng, lịch sử mua hàng và chăm sóc sau bán</p>
                </div>
              </div>
              
              <div className="feature-card" onClick={() => handleFeatureClick('feedback-test-drive')}>
                <div className="feature-icon">💬</div>
                <div className="feature-content">
                  <h4>Phản hồi & Lái thử</h4>
                  <p>Xử lý phản hồi khách hàng, đặt lịch lái thử và báo cáo trải nghiệm</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
      <footer className="dealer-staff-footer">
        <div className="footer-content">
          <div className="footer-section">
            <h4>Liên hệ hỗ trợ</h4>
            <div className="contact-item">
              <span>support@evdealer.com</span>
            </div>
            <div className="contact-item">
              <span>Hotline: 1900-123-456</span>
            </div>
            <div className="contact-item">
              <span>Live Chat: 24/7</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>Thông tin công ty</h4>
            <div className="contact-item">
              <span>123 Đường ABC, Quận XYZ, TP.HCM</span>
            </div>
            <div className="contact-item">
              <span>www.evdealer.com</span>
            </div>
            <div className="contact-item">
              <span>Giờ làm việc: 8:00 - 17:30</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>Dịch vụ</h4>
            <div className="service-list">
              <span className="service-item"> Tư vấn xe điện</span>
              <span className="service-item"> Hỗ trợ đăng ký</span>
              <span className="service-item"> Bảo dưỡng định kỳ</span>
              <span className="service-item"> Hỗ trợ kỹ thuật</span>
            </div>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p>&copy; 2025 EV Dealer Management System. All rights reserved.</p>
        </div>
      </footer>
      </div>
    </TestDriveContext.Provider>
  );
};

export default DealerStaff;