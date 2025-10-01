import React, { useState } from 'react';
import './Admin.css';

const Admin = ({ user, onLogout }) => {
  const [activeFeature, setActiveFeature] = useState('dashboard');

  const handleMenuClick = (featureId) => {
    setActiveFeature(featureId);
  };

  const renderMainContent = () => {
    switch (activeFeature) {
      case 'dashboard':
        return (
          <div className="dashboard-content">
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-number">25</div>
                <div className="stat-label">Đại lý hoạt động</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">1,247</div>
                <div className="stat-label">Tổng số xe bán</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">98%</div>
                <div className="stat-label">Hiệu suất hệ thống</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">156</div>
                <div className="stat-label">Người dùng online</div>
              </div>
            </div>

            <div className="recent-orders">
              <h4>Hoạt động hệ thống</h4>
              <div className="orders-table">
                <div className="table-header">
                  <div>Người dùng</div>
                  <div>Vai trò</div>
                  <div>Hoạt động</div>
                  <div>Thời gian</div>
                  <div>Trạng thái</div>
                </div>
                <div className="table-row">
                  <div>Admin_001</div>
                  <div>Quản trị viên</div>
                  <div>Cập nhật hệ thống</div>
                  <div>10:30 AM</div>
                  <div><span className="status-completed">THÀNH CÔNG</span></div>
                </div>
                <div className="table-row">
                  <div>Dealer_HN01</div>
                  <div>Đại lý</div>
                  <div>Tạo đơn hàng mới</div>
                  <div>10:15 AM</div>
                  <div><span className="status-confirmed">ĐANG XỬ LÝ</span></div>
                </div>
                <div className="table-row">
                  <div>EVM_Staff_01</div>
                  <div>Nhân viên hãng</div>
                  <div>Cập nhật catalog</div>
                  <div>09:45 AM</div>
                  <div><span className="status-new">CHỜ DUYỆT</span></div>
                </div>
              </div>
            </div>

            <div className="features-overview">
              <h4>Chức năng quản trị</h4>
              <div className="feature-quick-access">
                <div className="quick-feature-card" onClick={() => handleMenuClick('user-management')}>
                  <div className="quick-feature-icon">👥</div>
                  <div className="quick-feature-content">
                    <h5>Quản lý người dùng</h5>
                    <p>Tạo, sửa, xóa tài khoản người dùng</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('dealer-management')}>
                  <div className="quick-feature-icon">🏢</div>
                  <div className="quick-feature-content">
                    <h5>Quản lý đại lý</h5>
                    <p>Quản lý thông tin và hoạt động đại lý</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('system-config')}>
                  <div className="quick-feature-icon">⚙️</div>
                  <div className="quick-feature-content">
                    <h5>Cấu hình hệ thống</h5>
                    <p>Thiết lập và cấu hình hệ thống</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('reports')}>
                  <div className="quick-feature-icon">📊</div>
                  <div className="quick-feature-content">
                    <h5>Báo cáo tổng hợp</h5>
                    <p>Xem báo cáo và thống kê toàn hệ thống</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      case 'user-management':
        return <div className="feature-content"><h3>Quản lý người dùng</h3><p>Chức năng quản lý người dùng sẽ được phát triển...</p></div>;
      case 'system-settings':
        return <div className="feature-content"><h3>Cài đặt hệ thống</h3><p>Chức năng cài đặt hệ thống sẽ được phát triển...</p></div>;
      case 'reports':
        return <div className="feature-content"><h3>Báo cáo tổng hợp</h3><p>Chức năng báo cáo sẽ được phát triển...</p></div>;
      case 'security':
        return <div className="feature-content"><h3>Bảo mật</h3><p>Chức năng bảo mật sẽ được phát triển...</p></div>;
      default:
        return null;
    }
  };

  return (
    <div className="new-admin-layout">
      <div className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">🔧</span>
            <span className="logo-text">Admin</span>
          </div>
        </div>
        
        <nav className="sidebar-menu">
          <div 
            className={`menu-item ${activeFeature === 'dashboard' ? 'active' : ''}`}
            onClick={() => handleMenuClick('dashboard')}
          >
            <span className="menu-icon">📊</span>
            <span className="menu-text">Dashboard</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'user-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('user-management')}
          >
            <span className="menu-icon">👥</span>
            <span className="menu-text">Quản lý người dùng</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'system-settings' ? 'active' : ''}`}
            onClick={() => handleMenuClick('system-settings')}
          >
            <span className="menu-icon">⚙️</span>
            <span className="menu-text">Cài đặt hệ thống</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'reports' ? 'active' : ''}`}
            onClick={() => handleMenuClick('reports')}
          >
            <span className="menu-icon">📊</span>
            <span className="menu-text">Báo cáo</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'security' ? 'active' : ''}`}
            onClick={() => handleMenuClick('security')}
          >
            <span className="menu-icon">🔒</span>
            <span className="menu-text">Bảo mật</span>
          </div>

          <div className="menu-item">
            <span className="menu-icon">🗃️</span>
            <span className="menu-text">Backup</span>
          </div>
          
          <div className="menu-item">
            <span className="menu-icon">📝</span>
            <span className="menu-text">Logs</span>
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
                <span className="user-role">Quản trị viên</span>
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
};

export default Admin;
