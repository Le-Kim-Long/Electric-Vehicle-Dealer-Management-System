import React from 'react';
import './Admin.css';

const Admin = ({ user, onLogout }) => {
  return (
    <div className="dealer-staff-container">
      <header className="dealer-staff-header">
        <div className="header-left">
          <div className="header-brand">
            <div className="brand-icon">🔧</div>
            <div className="header-title">
              <h1>Admin</h1>
              <span className="header-subtitle">Quản trị hệ thống</span>
            </div>
          </div>
        </div>
        
        <div className="user-info">
          <div className="user-profile">
            <div className="user-avatar">{user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}</div>
            <div className="user-details">
              <span className="user-name">{user.name || user.username}</span>
              <span className="user-role">Quản trị viên</span>
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
            <h2>Chào mừng đến với hệ thống quản trị EV Dealer</h2>
            <p>Vai trò: {user.roleLabel}</p>
          </div>
        </div>
        
        <div className="features-container">
          <div className="features-preview">
            <h3>Tính năng sẽ có:</h3>
            <div className="feature-list">
            <div className="feature-card">
              <h4>Quản lý người dùng</h4>
              <p>Tạo, sửa, xóa tài khoản các vai trò trong hệ thống</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý đại lý</h4>
              <p>Thêm, sửa thông tin đại lý, phân quyền</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý sản phẩm</h4>
              <p>Cập nhật danh mục xe, giá bán, thông số kỹ thuật</p>
            </div>
            
            <div className="feature-card">
              <h4>Báo cáo tổng hợp</h4>
              <p>Thống kê doanh số, hiệu suất toàn hệ thống</p>
            </div>
            
            <div className="feature-card">
              <h4>Cấu hình hệ thống</h4>
              <p>Thiết lập tham số, backup, bảo mật</p>
            </div>
            
            <div className="feature-card">
              <h4>Giám sát hoạt động</h4>
              <p>Theo dõi log, audit trail, security monitoring</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý thanh toán</h4>
              <p>Thiết lập gateway, kiểm soát giao dịch</p>
            </div>
            
            <div className="feature-card">
              <h4>Phân tích KPI</h4>
              <p>Đánh giá hiệu suất, ROI, conversion rate</p>
            </div>
            </div>
          </div>
        </div>
      </main>      <footer className="dealer-staff-footer">
        <div className="footer-content">
          <div className="footer-section">
            <h4>Hỗ trợ kỹ thuật</h4>
            <div className="contact-item">
              <span>tech@evdealer.com</span>
            </div>
            <div className="contact-item">
              <span>Hotline: 1900-ADMIN</span>
            </div>
            <div className="contact-item">
              <span>Emergency: 24/7/365</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>Data Center</h4>
            <div className="contact-item">
              <span>789 Tech Park, District 1, HCM</span>
            </div>
            <div className="contact-item">
              <span>admin.evdealer.com</span>
            </div>
            <div className="contact-item">
              <span>Monitoring: 24/7</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>System Status</h4>
            <div className="service-list">
              <span className="service-item">• Database: ✅ Online</span>
              <span className="service-item">• API Gateway: ✅ Online</span>
              <span className="service-item">• Payment: ✅ Online</span>
              <span className="service-item">• Backup: ✅ Active</span>
            </div>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p>&copy; 2025 EV Dealer Management System - Admin Portal. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default Admin;
