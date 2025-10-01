import React, { useState } from 'react';
import './DealerManager.css';

const DealerManager = ({ user, onLogout }) => {
  const [activeFeature, setActiveFeature] = useState('dashboard');

  const handleMenuClick = (featureId) => {
    setActiveFeature(featureId);
  };

  const renderMainContent = () => {
    switch (activeFeature) {
      case 'dashboard':
        return (
          <div className="dashboard-content">
            <div className="recent-orders">
              <h4>Đơn hàng gần đây</h4>
              <div className="orders-table">
                <div className="table-header">
                  <div>Mã đơn</div>
                  <div>Nhân viên</div>
                  <div>Khách hàng</div>
                  <div>Xe</div>
                  <div>Trạng thái</div>
                </div>
                <div className="table-row">
                  <div>#MG001</div>
                  <div>Nguyễn Văn A</div>
                  <div>Trần Thị B</div>
                  <div>VinFast VF8</div>
                  <div><span className="status-new">MỚI</span></div>
                </div>
                <div className="table-row">
                  <div>#MG002</div>
                  <div>Lê Văn C</div>
                  <div>Phạm Thị D</div>
                  <div>Tesla Model Y</div>
                  <div><span className="status-confirmed">DUYỆT</span></div>
                </div>
                <div className="table-row">
                  <div>#MG003</div>
                  <div>Hoàng Văn E</div>
                  <div>Võ Thị F</div>
                  <div>BYD Tang</div>
                  <div><span className="status-completed">HOÀN THÀNH</span></div>
                </div>
              </div>
            </div>

            <div className="features-overview">
              <h4>Chức năng quản lý</h4>
              <div className="feature-quick-access">
                <div className="quick-feature-card" onClick={() => handleMenuClick('staff-management')}>
                  <div className="quick-feature-icon">👥</div>
                  <div className="quick-feature-content">
                    <h5>Quản lý nhân viên</h5>
                    <p>Quản lý danh sách và hiệu suất nhân viên</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('sales-reports')}>
                  <div className="quick-feature-icon">📊</div>
                  <div className="quick-feature-content">
                    <h5>Báo cáo bán hàng</h5>
                    <p>Thống kê doanh số và hiệu quả bán hàng</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('inventory-overview')}>
                  <div className="quick-feature-icon">📦</div>
                  <div className="quick-feature-content">
                    <h5>Tổng quan kho</h5>
                    <p>Xem tình trạng tồn kho và nhập xuất</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('dealer-settings')}>
                  <div className="quick-feature-icon">⚙️</div>
                  <div className="quick-feature-content">
                    <h5>Cài đặt đại lý</h5>
                    <p>Cấu hình thông tin và chính sách đại lý</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      case 'staff-management':
        return <div className="feature-content"><h3>Quản lý nhân viên</h3><p>Chức năng quản lý nhân viên sẽ được phát triển...</p></div>;
      case 'inventory':
        return <div className="feature-content"><h3>Quản lý kho</h3><p>Chức năng quản lý kho sẽ được phát triển...</p></div>;
      case 'sales-reports':
        return <div className="feature-content"><h3>Báo cáo bán hàng</h3><p>Chức năng báo cáo bán hàng sẽ được phát triển...</p></div>;
      case 'customer-service':
        return <div className="feature-content"><h3>Chăm sóc khách hàng</h3><p>Chức năng chăm sóc khách hàng sẽ được phát triển...</p></div>;
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
            className={`menu-item ${activeFeature === 'dashboard' ? 'active' : ''}`}
            onClick={() => handleMenuClick('dashboard')}
          >
            <span className="menu-icon">📊</span>
            <span className="menu-text">Dashboard</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'staff-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('staff-management')}
          >
            <span className="menu-icon">👥</span>
            <span className="menu-text">Quản lý nhân viên</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'inventory' ? 'active' : ''}`}
            onClick={() => handleMenuClick('inventory')}
          >
            <span className="menu-icon">📦</span>
            <span className="menu-text">Quản lý kho</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'sales-reports' ? 'active' : ''}`}
            onClick={() => handleMenuClick('sales-reports')}
          >
            <span className="menu-icon">📊</span>
            <span className="menu-text">Báo cáo bán hàng</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'customer-service' ? 'active' : ''}`}
            onClick={() => handleMenuClick('customer-service')}
          >
            <span className="menu-icon">🎧</span>
            <span className="menu-text">Chăm sóc KH</span>
          </div>

          <div className="menu-item">
            <span className="menu-icon">💰</span>
            <span className="menu-text">Tài chính</span>
          </div>
          
          <div className="menu-item">
            <span className="menu-icon">⚙️</span>
            <span className="menu-text">Cài đặt</span>
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
};

export default DealerManager;
