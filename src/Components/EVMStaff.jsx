import React, { useState } from 'react';
import './EVMStaff.css';

const EVMStaff = ({ user, onLogout }) => {
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
                <div className="stat-number">50,000</div>
                <div className="stat-label">Sản lượng/năm</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">25</div>
                <div className="stat-label">Đại lý toàn quốc</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">95%</div>
                <div className="stat-label">Chất lượng sản phẩm</div>
              </div>
              <div className="stat-card">
                <div className="stat-number">1,234</div>
                <div className="stat-label">Xe sản xuất tháng này</div>
              </div>
            </div>

            <div className="recent-orders">
              <h4>Hoạt động sản xuất</h4>
              <div className="orders-table">
                <div className="table-header">
                  <div>Model</div>
                  <div>Sản lượng</div>
                  <div>Trạng thái</div>
                  <div>Tiến độ</div>
                  <div>Giao hàng</div>
                </div>
                <div className="table-row">
                  <div>VF8 2025</div>
                  <div>500 xe</div>
                  <div><span className="status-completed">SẢN XUẤT</span></div>
                  <div>85%</div>
                  <div>15/10/2025</div>
                </div>
                <div className="table-row">
                  <div>Tesla Model Y</div>
                  <div>300 xe</div>
                  <div><span className="status-confirmed">LẮP RÁP</span></div>
                  <div>60%</div>
                  <div>20/10/2025</div>
                </div>
                <div className="table-row">
                  <div>BYD Tang</div>
                  <div>200 xe</div>
                  <div><span className="status-new">CHUẨN BỊ</span></div>
                  <div>30%</div>
                  <div>25/10/2025</div>
                </div>
              </div>
            </div>

            <div className="features-overview">
              <h4>Chức năng chính</h4>
              <div className="feature-quick-access">
                <div className="quick-feature-card" onClick={() => handleMenuClick('product-management')}>
                  <div className="quick-feature-icon">🚗</div>
                  <div className="quick-feature-content">
                    <h5>Quản lý sản phẩm</h5>
                    <p>Cập nhật catalog xe điện, thông số kỹ thuật</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('inventory-management')}>
                  <div className="quick-feature-icon">📦</div>
                  <div className="quick-feature-content">
                    <h5>Quản lý kho</h5>
                    <p>Theo dõi inventory, nhập xuất kho</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('dealer-support')}>
                  <div className="quick-feature-icon">🤝</div>
                  <div className="quick-feature-content">
                    <h5>Hỗ trợ đại lý</h5>
                    <p>Training, chính sách bán hàng</p>
                  </div>
                </div>
                
                <div className="quick-feature-card" onClick={() => handleMenuClick('production-reports')}>
                  <div className="quick-feature-icon">📊</div>
                  <div className="quick-feature-content">
                    <h5>Báo cáo sản xuất</h5>
                    <p>Thống kê sản lượng và chất lượng</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );
      case 'product-management':
        return <div className="feature-content"><h3>Quản lý sản phẩm</h3><p>Chức năng quản lý sản phẩm sẽ được phát triển...</p></div>;
      case 'inventory-management':
        return <div className="feature-content"><h3>Quản lý kho</h3><p>Chức năng quản lý kho sẽ được phát triển...</p></div>;
      case 'dealer-support':
        return <div className="feature-content"><h3>Hỗ trợ đại lý</h3><p>Chức năng hỗ trợ đại lý sẽ được phát triển...</p></div>;
      case 'production-reports':
        return <div className="feature-content"><h3>Báo cáo sản xuất</h3><p>Chức năng báo cáo sản xuất sẽ được phát triển...</p></div>;
      default:
        return null;
    }
  };

  return (
    <div className="new-evm-staff-layout">
      <div className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon">🏭</span>
            <span className="logo-text">EVM Staff</span>
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
            className={`menu-item ${activeFeature === 'product-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('product-management')}
          >
            <span className="menu-icon">🚗</span>
            <span className="menu-text">Quản lý sản phẩm</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'inventory-management' ? 'active' : ''}`}
            onClick={() => handleMenuClick('inventory-management')}
          >
            <span className="menu-icon">📦</span>
            <span className="menu-text">Quản lý kho</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'dealer-support' ? 'active' : ''}`}
            onClick={() => handleMenuClick('dealer-support')}
          >
            <span className="menu-icon">🤝</span>
            <span className="menu-text">Hỗ trợ đại lý</span>
          </div>
          
          <div 
            className={`menu-item ${activeFeature === 'production-reports' ? 'active' : ''}`}
            onClick={() => handleMenuClick('production-reports')}
          >
            <span className="menu-icon">📊</span>
            <span className="menu-text">Báo cáo sản xuất</span>
          </div>

          <div className="menu-item">
            <span className="menu-icon">🚛</span>
            <span className="menu-text">Vận chuyển</span>
          </div>
          
          <div className="menu-item">
            <span className="menu-icon">🔧</span>
            <span className="menu-text">Hỗ trợ kỹ thuật</span>
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
                <span className="user-role">Nhân viên hãng xe</span>
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

export default EVMStaff;
