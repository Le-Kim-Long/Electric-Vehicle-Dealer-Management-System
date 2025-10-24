import React from 'react';
import './Home-page.css';

const HomePage = ({ onMenuClick }) => {
  const quickFeatures = [
    {
      id: 'vehicle-info',
      icon: '🚗',
      title: 'Truy vấn thông tin xe',
      description: 'Xem danh mục xe điện, thông số kỹ thuật'
    },
    {
      id: 'create-order',
      icon: '📋',
      title: 'Tạo đơn hàng',
      description: 'Lập đơn hàng mới, tính toán giá'
    },
    {
      id: 'customer-management',
      icon: '👥',
      title: 'Quản lý khách hàng',
      description: 'Quản lý thông tin khách hàng'
    },
    {
      id: 'payment',
      icon: '💳',
      title: 'Thanh toán',
      description: 'Xử lý thanh toán và hóa đơn'
    },
    {
      id: 'feedback-test-drive',
      icon: '💬',
      title: 'Phản hồi & Lái thử',
      description: 'Xử lý phản hồi và đặt lịch lái thử'
    }
  ];

  return (
    <div className="home-page">
      {/* Local Video Section */}
      <div className="video-section">
        <div className="video-container">
          <video 
            width="100%" 
            height="100%" 
            autoPlay 
            muted 
            loop 
            playsInline
            controls={false}
            className="local-video"
            onError={(e) => {
              console.log('Video load error:', e);
              e.target.style.display = 'none';
              e.target.parentElement.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
              e.target.parentElement.innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: white; font-size: 1.2rem;">Video không thể tải</div>';
            }}
          >
            <source src="/video-banner/videoplayback.mp4" type="video/mp4" />
            Trình duyệt của bạn không hỗ trợ video.
          </video>
        </div>
      </div>

      {/* Dashboard Content */}
      <div className="dashboard-content">
        {/* Features Overview */}
        <div className="features-overview">
          <h4>Chức năng chính</h4>
          <div className="feature-quick-access">
            {quickFeatures.map((feature) => (
              <div 
                key={feature.id}
                className="quick-feature-card" 
                onClick={() => onMenuClick(feature.id)}
              >
                <div className="quick-feature-icon">{feature.icon}</div>
                <div className="quick-feature-content">
                  <h5>{feature.title}</h5>
                  <p>{feature.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;