import React from 'react';
import './DealerManager.css';

const DealerManager = ({ user, onLogout }) => {
  return (
    <div className="dealer-manager-container">
      <header className="dealer-manager-header">
        <div className="header-left">
          <div className="header-brand">
            <div className="brand-icon">👨‍💼</div>
            <div className="header-title">
              <h1>Dealer Manager</h1>
              <span className="header-subtitle">Quản lý đại lý</span>
            </div>
          </div>
        </div>
        
        <div className="user-info">
          <div className="user-profile">
            <div className="user-avatar">{user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}</div>
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
      
      <main className="dealer-manager-main">
        <div className="welcome-container">
          <div className="welcome-section">
            <h2>Chào mừng đến với hệ thống quản lý bán xe điện</h2>
            <p>Vai trò: {user.roleLabel}</p>
          </div>
        </div>
        
        <div className="features-container">
          <div className="features-preview">
            <h3>Tính năng sẽ có:</h3>
            <div className="feature-list">
            <div className="feature-card">
              <h4>Quản lý xe điện</h4>
              <p>Xem và quản lý danh mục xe, cấu hình, giá bán</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý nhân viên</h4>
              <p>Thêm, sửa, xóa thông tin nhân viên đại lý</p>
            </div>
            
            <div className="feature-card">
              <h4>Báo cáo doanh số</h4>
              <p>Xem báo cáo bán hàng, thống kê hiệu suất</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý báo giá</h4>
              <p>Duyệt và quản lý các báo giá của nhân viên</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý khuyến mãi</h4>
              <p>Tạo và quản lý các chương trình khuyến mãi</p>
            </div>
            
            <div className="feature-card">
              <h4>Thiết lập mục tiêu</h4>
              <p>Đặt target doanh số cho team và cá nhân</p>
            </div>
            </div>
          </div>
        </div>
      </main>      <footer className="dealer-manager-footer">
        <div className="footer-content">
          <div className="footer-section">
            <h4>Liên hệ hỗ trợ</h4>
            <div className="contact-item">
              <span>manager@evdealer.com</span>
            </div>
            <div className="contact-item">
              <span>Hotline: 1900-MANAGER</span>
            </div>
            <div className="contact-item">
              <span>Live Chat: 24/7</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>Thông tin quản lý</h4>
            <div className="contact-item">
              <span>456 Manager St, District 3, HCM</span>
            </div>
            <div className="contact-item">
              <span>manager.evdealer.com</span>
            </div>
            <div className="contact-item">
              <span>Giờ làm việc: 7:30 - 18:00</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>Dashboard</h4>
            <div className="service-list">
              <span className="service-item">• Quản lý team</span>
              <span className="service-item">• Báo cáo chi tiết</span>
              <span className="service-item">• Phân tích hiệu suất</span>
              <span className="service-item">• Quản lý target</span>
            </div>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p>&copy; 2025 EV Dealer Management System - Manager Portal. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default DealerManager;
