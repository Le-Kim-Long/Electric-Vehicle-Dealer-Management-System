import React, { useState, useEffect } from 'react';
import './CustomerManagementFeature.css';

const CustomerManagementFeature = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [showCustomerModal, setShowCustomerModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [customers, setCustomers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState(null);

  // Load customers from localStorage or API
  const loadCustomers = async () => {
    setIsLoading(true);
    try {
      // Chỉ load dữ liệu từ localStorage, không tạo dữ liệu ảo
      const savedCustomers = localStorage.getItem('customers');
      
      if (savedCustomers) {
        const parsedCustomers = JSON.parse(savedCustomers);
        setCustomers(parsedCustomers);
        console.log('Loaded customers:', parsedCustomers);
      } else {
        // Nếu chưa có dữ liệu, để mảng rỗng
        setCustomers([]);
      }
    } catch (error) {
      console.error('Error loading customers:', error);
      setCustomers([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  // Filter customers based on search term
  const filteredCustomers = customers.filter(customer =>
    customer.FullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.Email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.PhoneNumber?.includes(searchTerm)
  );

  // Format date for display
  const formatDate = (dateString) => {
    if (!dateString) return new Date().toLocaleDateString('vi-VN');
    try {
      return new Date(dateString).toLocaleDateString('vi-VN');
    } catch (error) {
      return dateString;
    }
  };

  // Open customer detail modal
  const openCustomerModal = (customer) => {
    setSelectedCustomer(customer);
    setShowCustomerModal(true);
  };

  // Close customer detail modal
  const closeCustomerModal = () => {
    setSelectedCustomer(null);
    setShowCustomerModal(false);
  };

  // Handle edit customer
  const handleEditCustomer = (customer) => {
    setEditingCustomer({ ...customer });
    setShowEditModal(true);
    setShowCustomerModal(false);
  };

  // Save edited customer
  const saveEditedCustomer = () => {
    if (!editingCustomer) return;
    
    if (!editingCustomer.FullName || !editingCustomer.PhoneNumber) {
      alert('Vui lòng điền đầy đủ thông tin bắt buộc (Họ tên, Số điện thoại)');
      return;
    }

    // Kiểm tra email trùng (nếu có email và trừ chính khách hàng đang sửa)
    if (editingCustomer.Email) {
      const emailConflict = customers.find(c => 
        c.Email === editingCustomer.Email && c.CustomerId !== editingCustomer.CustomerId
      );
      if (emailConflict) {
        alert(`Email "${editingCustomer.Email}" đã được sử dụng bởi khách hàng khác: ${emailConflict.FullName} (${emailConflict.PhoneNumber})`);
        return;
      }
    }

    // Kiểm tra số điện thoại trùng (trừ chính khách hàng đang sửa)
    const phoneConflict = customers.find(c => 
      c.PhoneNumber === editingCustomer.PhoneNumber && c.CustomerId !== editingCustomer.CustomerId
    );
    if (phoneConflict) {
      alert(`Số điện thoại "${editingCustomer.PhoneNumber}" đã được sử dụng bởi khách hàng khác: ${phoneConflict.FullName} (${phoneConflict.Email})`);
      return;
    }
    
    const updatedCustomers = customers.map(customer =>
      customer.CustomerId === editingCustomer.CustomerId ? {
        ...editingCustomer,
        lastActivity: new Date().toISOString() // Cập nhật thời gian sửa đổi
      } : customer
    );
    
    localStorage.setItem('customers', JSON.stringify(updatedCustomers));
    setCustomers(updatedCustomers);
    setShowEditModal(false);
    setEditingCustomer(null);
    
    alert('Thông tin khách hàng đã được cập nhật thành công!');
  };

  // Handle delete customer
  const handleDeleteCustomer = async (customerId) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa khách hàng này?')) {
      try {
        const updatedCustomers = customers.filter(c => c.CustomerId !== customerId);
        localStorage.setItem('customers', JSON.stringify(updatedCustomers));
        setCustomers(updatedCustomers);
      } catch (error) {
        console.error('Error deleting customer:', error);
      }
    }
  };

  // Calculate total spent from orders
  const calculateTotalSpent = (customer) => {
    if (!customer.orders || customer.orders.length === 0) return '0 VNĐ';
    
    const total = customer.orders.reduce((sum, order) => {
      // Parse amount from string like "500,000,000 VNĐ"
      const amount = order.amount?.replace(/[^\d]/g, '') || '0';
      return sum + parseInt(amount);
    }, 0);
    
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(total);
  };

  return (
    <div className="customer-management-feature">
      {/* Header */}
      <div className="customer-management-header">
        <div className="customer-management-header-content">
          <div className="customer-management-header-icon">👥</div>
          <div className="customer-management-header-text">
            <h2>Quản lý khách hàng</h2>
            <p>Quản lý thông tin và dữ liệu khách hàng trong hệ thống</p>
          </div>
        </div>
      </div>

      {/* Controls */}
      <div className="customer-controls">
        <div className="search-section">
          <div className="search-box">
            <input
              type="text"
              placeholder="Tìm kiếm khách hàng (Tên, Email, SĐT)..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>
        </div>
      </div>

      {/* Customer List */}
      <div className="customers-content">
        {isLoading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Đang tải danh sách khách hàng...</p>
          </div>
        ) : (
          <>
            <div className="customers-grid">
              {filteredCustomers.map(customer => (
                <div key={customer.CustomerId} className="customer-card">
                  <div className="customer-header">
                    <h3>{customer.FullName}</h3>
                    <span className="customer-id">
                      #KH{customer.CustomerId?.toString().padStart(4, '0')}
                    </span>
                  </div>
                  
                  <div className="customer-info">
                    <div className="info-row">
                      <span className="info-label">👤 Họ và tên:</span>
                      <span className="info-value">{customer.FullName}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">📧 Email:</span>
                      <span className="info-value">{customer.Email || 'Không có'}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">📱 Số điện thoại:</span>
                      <span className="info-value">{customer.PhoneNumber}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">📅 Ngày tạo:</span>
                      <span className="info-value">{formatDate(customer.CreatedDate)}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">🚗 Số đơn hàng:</span>
                      <span className="info-value">{customer.orders?.length || 0}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">💰 Tổng chi tiêu:</span>
                      <span className="info-value highlight">{calculateTotalSpent(customer)}</span>
                    </div>
                  </div>
                  
                  <div className="customer-actions">
                    <button 
                      className="btn-view"
                      onClick={() => openCustomerModal(customer)}
                      title="Xem chi tiết"
                    >
                       Chi tiết
                    </button>
                    <button 
                      className="btn-delete"
                      onClick={() => handleDeleteCustomer(customer.CustomerId)}
                      title="Xóa khách hàng"
                    >
                       Xóa
                    </button>
                  </div>
                </div>
              ))}
            </div>
            
            {filteredCustomers.length === 0 && !isLoading && (
              <div className="no-results">
                <div className="no-results-icon">🔍</div>
                <h3>Không tìm thấy khách hàng</h3>
                <p>
                  {searchTerm ? 
                    'Không có khách hàng nào phù hợp với từ khóa tìm kiếm.' :
                    'Chưa có khách hàng nào trong hệ thống. Hãy tạo đơn hàng để thêm khách hàng mới.'
                  }
                </p>
              </div>
            )}
          </>
        )}
      </div>

      {/* Customer Detail Modal */}
      {showCustomerModal && selectedCustomer && (
        <div className="modal-overlay" onClick={closeCustomerModal}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Chi tiết khách hàng</h2>
              <button className="modal-close" onClick={closeCustomerModal}>×</button>
            </div>
            
            <div className="modal-body">
              <div className="customer-detail-section">
                <h3>Thông tin cá nhân</h3>
                <div className="detail-grid">
                  <div className="detail-item">
                    <span className="detail-label">Mã khách hàng:</span>
                    <span className="detail-value">
                      #KH{selectedCustomer.CustomerId?.toString().padStart(4, '0')}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Họ và tên:</span>
                    <span className="detail-value">{selectedCustomer.FullName}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Email:</span>
                    <span className="detail-value">{selectedCustomer.Email || 'Không có'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Số điện thoại:</span>
                    <span className="detail-value">{selectedCustomer.PhoneNumber}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Ngày tạo:</span>
                    <span className="detail-value">{formatDate(selectedCustomer.CreatedDate)}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Tổng chi tiêu:</span>
                    <span className="detail-value highlight">{calculateTotalSpent(selectedCustomer)}</span>
                  </div>
                </div>
              </div>

              {/* Vehicle Purchase History */}
              <div className="customer-detail-section">
                <h3>🚗 Lịch sử mua xe ({selectedCustomer.orders?.length || 0} đơn hàng)</h3>
                <div className="orders-history">
                  {selectedCustomer.orders?.length > 0 ? (
                    selectedCustomer.orders.map((order, index) => (
                      <div key={index} className="order-history-item">
                        <div className="order-header">
                          <span className="order-id">📋 {order.id}</span>
                          <span className="order-date">📅 {order.date}</span>
                          <span className={`order-status ${order.status?.toLowerCase().replace(/\s/g, '')}`}>
                            {order.status}
                          </span>
                        </div>
                        <div className="order-details">
                          <div className="vehicle-info">
                            <h4>🚗 Xe đã mua:</h4>
                            <p className="vehicle-name">{order.vehicle}</p>
                          </div>
                          <div className="order-amount">
                            <span className="amount-label">💰 Tổng tiền:</span>
                            <span className="amount-value">{order.amount}</span>
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="no-orders">
                      <div className="no-orders-icon">🚗</div>
                      <p>Khách hàng chưa mua xe nào.</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
            
            <div className="modal-footer">
              <button className="btn-secondary" onClick={closeCustomerModal}>
                Đóng
              </button>
              <button 
                className="btn-primary" 
                onClick={() => handleEditCustomer(selectedCustomer)}
              >
                Chỉnh sửa thông tin
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Customer Modal */}
      {showEditModal && editingCustomer && (
        <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Chỉnh sửa thông tin khách hàng</h2>
              <button className="modal-close" onClick={() => setShowEditModal(false)}>×</button>
            </div>
            
            <div className="modal-body">
              <div className="edit-form">
                <div className="form-group">
                  <label>Họ và tên: <span className="required">*</span></label>
                  <input
                    type="text"
                    value={editingCustomer.FullName || ''}
                    onChange={(e) => setEditingCustomer({...editingCustomer, FullName: e.target.value})}
                    placeholder="Nhập họ và tên"
                  />
                </div>
                <div className="form-group">
                  <label>Email:</label>
                  <input
                    type="email"
                    value={editingCustomer.Email || ''}
                    onChange={(e) => setEditingCustomer({...editingCustomer, Email: e.target.value})}
                    placeholder="Nhập email (không bắt buộc)"
                  />
                </div>
                <div className="form-group">
                  <label>Số điện thoại: <span className="required">*</span></label>
                  <input
                    type="tel"
                    value={editingCustomer.PhoneNumber || ''}
                    onChange={(e) => setEditingCustomer({...editingCustomer, PhoneNumber: e.target.value})}
                    placeholder="Nhập số điện thoại"
                  />
                </div>
              </div>
            </div>
            
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setShowEditModal(false)}>
                Hủy
              </button>
              <button className="btn-primary" onClick={saveEditedCustomer}>
                Lưu thay đổi
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomerManagementFeature;