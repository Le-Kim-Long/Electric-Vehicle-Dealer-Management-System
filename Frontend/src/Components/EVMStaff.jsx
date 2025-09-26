import React from 'react';
import './EVMStaff.css';

const EVMStaff = ({ user, onLogout }) => {
  return (
    <div className="dealer-staff-container">
      <header className="dealer-staff-header">
        <div className="header-left">
          <div className="header-brand">
            <div className="brand-icon">🏭</div>
            <div className="header-title">
              <h1>EVM Staff</h1>
              <span className="header-subtitle">Nhân viên hãng xe</span>
            </div>
          </div>
        </div>
        
        <div className="user-info">
          <div className="user-profile">
            <div className="user-avatar">{user.name ? user.name.charAt(0) : user.username.charAt(0).toUpperCase()}</div>
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
      
      <main className="dealer-staff-main">
        <div className="welcome-container">
          <div className="welcome-section">
            <h2>Chào mừng đến với hệ thống quản lý EVM</h2>
            <p>Vai trò: {user.roleLabel}</p>
          </div>
        </div>
        
        <div className="features-container">
          <div className="features-preview">
            <h3>Tính năng sẽ có:</h3>
            <div className="feature-list">
            <div className="feature-card">
              <h4>Quản lý sản phẩm</h4>
              <p>Cập nhật catalog xe điện, thông số kỹ thuật, giá bán</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý kho</h4>
              <p>Theo dõi inventory, nhập xuất kho, dự báo nhu cầu</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý đại lý</h4>
              <p>Hỗ trợ đại lý, training, chính sách bán hàng</p>
            </div>
            
            <div className="feature-card">
              <h4>Báo cáo doanh số</h4>
              <p>Thống kê bán hàng qua đại lý, performance analysis</p>
            </div>
            
            <div className="feature-card">
              <h4>Quản lý vận chuyển</h4>
              <p>Điều phối giao hàng, tracking, logistics</p>
            </div>
            
            <div className="feature-card">
              <h4>Hỗ trợ kỹ thuật</h4>
              <p>Technical support, warranty, maintenance guide</p>
            </div>
            
            <div className="feature-card">
              <h4>Chính sách giá</h4>
              <p>Thiết lập pricing, discount, promotion campaigns</p>
            </div>
            
            <div className="feature-card">
              <h4>Phân tích thị trường</h4>
              <p>Market research, competitor analysis, trends</p>
            </div>
            </div>
          </div>
        </div>
      </main>      <footer className="dealer-staff-footer">
        <div className="footer-content">
          <div className="footer-section">
            <h4>EVM Headquarters</h4>
            <div className="contact-item">
              <span>456 Industrial Park, District 9, HCM</span>
            </div>
            <div className="contact-item">
              <span>Hotline: 1900-EVM-SUPPORT</span>
            </div>
            <div className="contact-item">
              <span>support@evmanufacturer.com</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>Production Center</h4>
            <div className="contact-item">
              <span>Factory A: Dong Nai Province</span>
            </div>
            <div className="contact-item">
              <span>Factory B: Binh Duong Province</span>
            </div>
            <div className="contact-item">
              <span>Capacity: 50,000 units/year</span>
            </div>
          </div>
          
          <div className="footer-section">
            <h4>Production Status</h4>
            <div className="service-list">
              <span className="service-item">• Production Line: ✅ Active</span>
              <span className="service-item">• Quality Control: ✅ Online</span>
              <span className="service-item">• Distribution: ✅ Ready</span>
              <span className="service-item">• R&D Department: ✅ Active</span>
            </div>
          </div>
        </div>
        
        <div className="footer-bottom">
          <p>&copy; 2025 EV Manufacturer - Staff Portal. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default EVMStaff;
