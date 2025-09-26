import React, { useState, useEffect } from 'react';
import './CustomerManagementFeature.css';

const CustomerManagementFeature = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [showCustomerModal, setShowCustomerModal] = useState(false);
  const [customers, setCustomers] = useState([]);

  // Refresh customer data
  const refreshCustomers = () => {
    const savedCustomers = JSON.parse(localStorage.getItem('customers') || '[]');
    
    // Standardize saved customers to ensure they have all required fields
    const standardizedSavedCustomers = savedCustomers.map(customer => ({
      MaKhachHang: customer.MaKhachHang || Date.now(),
      HoTen: customer.HoTen,
      Email: customer.Email,
      SoDienThoai: customer.SoDienThoai,
      DiaChi: null,
      NgayTao: customer.NgayTao ? formatDisplayDate(customer.NgayTao) : new Date().toLocaleDateString('vi-VN'),
      // Giữ lại lịch sử đơn hàng
      orders: customer.orders || [],
      totalOrders: customer.totalOrders || 0,
      totalSpent: customer.totalSpent || '0 VNĐ',
      lastActivity: customer.lastActivity || customer.NgayTao
    }));
    
    const allCustomers = [...mockCustomers, ...standardizedSavedCustomers];
    
    // Remove duplicates based on Email or SoDienThoai
    const uniqueCustomers = allCustomers.reduce((acc, current) => {
      const existingCustomer = acc.find(c => 
        c.Email === current.Email || c.SoDienThoai === current.SoDienThoai
      );
      if (!existingCustomer) {
        acc.push(current);
      }
      return acc;
    }, []);
    
    setCustomers(uniqueCustomers);
  };

  // Load customers from localStorage and merge with mock data
  useEffect(() => {
    refreshCustomers();
    
    // Listen for storage changes to auto-refresh when new orders are created
    const handleStorageChange = (e) => {
      if (e.key === 'customers') {
        refreshCustomers();
      }
    };
    
    window.addEventListener('storage', handleStorageChange);
    
    // Also refresh every 3 seconds to catch updates from same tab
    const interval = setInterval(refreshCustomers, 3000);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
      clearInterval(interval);
    };
  }, []);

  // Format date for display
  const formatDisplayDate = (dateString) => {
    try {
      if (dateString.includes('T')) {
        // ISO format from CreateOrderFeature
        return new Date(dateString).toLocaleDateString('vi-VN');
      } else {
        // Already formatted date
        return dateString;
      }
    } catch (error) {
      return new Date().toLocaleDateString('vi-VN');
    }
  };

  // Mock data theo cấu trúc bảng KHACHHANG
  const mockCustomers = [];

  const filteredCustomers = customers.filter(customer =>
    customer.HoTen.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.Email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.SoDienThoai.includes(searchTerm)
  );

  const openCustomerModal = (customer) => {
    setSelectedCustomer(customer);
    setShowCustomerModal(true);
  };

  const closeCustomerModal = () => {
    setSelectedCustomer(null);
    setShowCustomerModal(false);
  };

  return (
    <div className="customer-management-feature">
      <div className="feature-header">
        <h2>Quản lý Khách hàng</h2>
        <p>Quản lý thông tin khách hàng trong hệ thống</p>
      </div>

      {/* Search */}
      <div className="customer-actions">
        <div className="search-box">
          <input
            type="text"
            placeholder="Tìm kiếm khách hàng (Tên, Email, SĐT)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="action-buttons">
          <button 
            className="btn-refresh"
            onClick={refreshCustomers}
            title="Làm mới danh sách"
          >
            🔄 Làm mới
          </button>
          <div className="customer-count">
            <span>{filteredCustomers.length} khách hàng</span>
          </div>
        </div>
      </div>

      {/* Customers List */}
      <div className="customers-content">
        <div className="customers-grid">
          {filteredCustomers.map(customer => (
            <div key={customer.MaKhachHang} className="customer-card">
              <div className="customer-header">
                <h3>{customer.HoTen}</h3>
                <span className="customer-id">#{customer.MaKhachHang.toString().padStart(4, '0')}</span>
              </div>
              <div className="customer-info">
                <p><strong>Email:</strong> {customer.Email}</p>
                <p><strong>Số điện thoại:</strong> {customer.SoDienThoai}</p>
                <p><strong>Ngày tạo:</strong> {formatDisplayDate(customer.NgayTao)}</p>
                <p><strong>Số đơn hàng:</strong> {(customer.orders || []).length}</p>
                {customer.totalSpent && customer.totalSpent !== '0 VNĐ' && (
                  <p><strong>Tổng chi tiêu:</strong> {customer.totalSpent}</p>
                )}
              </div>
              <div className="customer-actions-buttons">
                <button 
                  className="btn-view"
                  onClick={() => openCustomerModal(customer)}
                >
                  Xem chi tiết
                </button>
                <button className="btn-edit">Chỉnh sửa</button>
              </div>
            </div>
          ))}
        </div>
        
        {filteredCustomers.length === 0 && (
          <div className="no-results">
            <p>Không tìm thấy khách hàng nào phù hợp với từ khóa tìm kiếm.</p>
          </div>
        )}
      </div>

      {/* Customer Detail Modal */}
      {showCustomerModal && selectedCustomer && (
        <div className="modal-overlay" onClick={closeCustomerModal}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Chi tiết khách hàng: {selectedCustomer.HoTen}</h2>
              <button className="modal-close" onClick={closeCustomerModal}>×</button>
            </div>
            <div className="modal-body">
              <div className="customer-detail-grid">
                <div className="customer-detail-info">
                  <h3>Thông tin cá nhân</h3>
                  <div className="info-grid">
                    <p><strong>Mã khách hàng:</strong> #{selectedCustomer.MaKhachHang.toString().padStart(4, '0')}</p>
                    <p><strong>Họ tên:</strong> {selectedCustomer.HoTen}</p>
                    <p><strong>Email:</strong> {selectedCustomer.Email}</p>
                    <p><strong>Số điện thoại:</strong> {selectedCustomer.SoDienThoai}</p>
                    <p><strong>Ngày tạo:</strong> {formatDisplayDate(selectedCustomer.NgayTao)}</p>
                    <p><strong>Số đơn hàng:</strong> {(selectedCustomer.orders || []).length}</p>
                    {selectedCustomer.totalSpent && selectedCustomer.totalSpent !== '0 VNĐ' && (
                      <p><strong>Tổng chi tiêu:</strong> {selectedCustomer.totalSpent}</p>
                    )}
                  </div>
                </div>
                
                <div className="customer-detail-orders">
                  <h3>Lịch sử đơn hàng ({(selectedCustomer.orders || []).length})</h3>
                  <div className="orders-list">
                    {(selectedCustomer.orders || []).length > 0 ? (
                      (selectedCustomer.orders || []).map(order => (
                        <div key={order.id} className="order-item">
                          <div className="order-header">
                            <span className="order-id">{order.id}</span>
                            <span className="order-date">{order.date}</span>
                          </div>
                          <div className="order-details">
                            <p><strong>Xe:</strong> {order.vehicle}</p>
                            <p><strong>Số tiền:</strong> {order.amount}</p>
                            <p><strong>Trạng thái:</strong> 
                              <span className={`order-status ${order.status.replace(/\s/g, '').toLowerCase()}`}>
                                {order.status}
                              </span>
                            </p>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="no-orders">Khách hàng chưa có đơn hàng nào.</p>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomerManagementFeature;