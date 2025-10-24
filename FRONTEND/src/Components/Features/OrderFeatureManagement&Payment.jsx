import React, { useState, useEffect } from 'react';
import './OrderFeatureManagement&Payment.css';

const OrderFeatureManagementPayment = () => {
  const [orders, setOrders] = useState([]);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [filterMethod, setFilterMethod] = useState('all');

  // Load orders từ localStorage khi component mount
  useEffect(() => {
    const loadOrders = () => {
      try {
        const savedOrders = JSON.parse(localStorage.getItem('orders') || '[]');
        setOrders(savedOrders);
        console.log('Loaded orders:', savedOrders);
      } catch (error) {
        console.error('Error loading orders:', error);
        setOrders([]);
      }
    };

    loadOrders();

    // Listen cho localStorage changes (khi có đơn hàng mới)
    const handleStorageChange = () => {
      loadOrders();
    };

    window.addEventListener('storage', handleStorageChange);
    
    // Check mỗi 1 giây để update real-time
    const interval = setInterval(loadOrders, 1000);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      clearInterval(interval);
    };
  }, []);

  // Tạo payments từ orders
  const payments = orders.map(order => ({
    paymentId: order.paymentId,
    orderId: order.orderId,
    orderCode: order.orderCode,
    customerName: order.customerName,
    customerEmail: order.customerEmail,
    customerPhone: order.customerPhone,
    amount: order.total,
    method: order.paymentMethod,
    status: order.paymentStatus || 'Pending',
    createdDate: order.createdDate,
    notes: order.paymentNotes,
    vehicles: order.vehicles,
    promotion: order.promotion,
    orderStatus: order.status,
    financing: order.financing,
    payment: order.payment
  }));

  // Lọc thanh toán
  const filteredPayments = payments.filter(payment => {
    const matchesSearch = payment.paymentId.toString().includes(searchTerm) ||
                         payment.orderCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         payment.customerName.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = filterStatus === 'all' || payment.status === filterStatus;
    const matchesMethod = filterMethod === 'all' || payment.method === filterMethod;
    
    return matchesSearch && matchesStatus && matchesMethod;
  });

  // Format tiền tệ
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  // Format ngày giờ
  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Cập nhật trạng thái thanh toán
  const updatePaymentStatus = (paymentId, newStatus) => {
    const updatedOrders = orders.map(order => 
      order.paymentId === paymentId 
        ? { ...order, paymentStatus: newStatus }
        : order
    );
    
    setOrders(updatedOrders);
    localStorage.setItem('orders', JSON.stringify(updatedOrders));
  };

  // Cập nhật trạng thái đơn hàng
  const updateOrderStatus = (orderId, newStatus) => {
    const updatedOrders = orders.map(order => 
      order.orderId === orderId 
        ? { ...order, status: newStatus }
        : order
    );
    
    setOrders(updatedOrders);
    localStorage.setItem('orders', JSON.stringify(updatedOrders));
  };

  // Render status badge
  const renderStatusBadge = (status) => {
    const statusConfig = {
      Pending: { text: 'Chờ xử lý', class: 'status-pending' },
      Success: { text: 'Thành công', class: 'status-success' },
      Failed: { text: 'Thất bại', class: 'status-failed' }
    };
    
    const config = statusConfig[status] || { text: status, class: 'status-pending' };
    return <span className={`status-badge ${config.class}`}>{config.text}</span>;
  };

  // Render method badge
  const renderMethodBadge = (method) => {
    const methodConfig = {
      'Tiền mặt': { icon: '💵', class: 'method-cash' },
      'Chuyển khoản': { icon: '🏦', class: 'method-bank' },
      'Thẻ tín dụng': { icon: '💳', class: 'method-card' }
    };
    
    const config = methodConfig[method] || { icon: '💰', class: 'method-other' };
    return (
      <span className={`method-badge ${config.class}`}>
        {config.icon} {method}
      </span>
    );
  };

  return (
    <div className="order-management-payment-feature">
      {/* Header Section */}
      <div className="order-management-payment-header">
        <div className="order-management-header-content">
          <div className="order-management-header-icon">💳</div>
          <div className="order-management-header-text">
            <h2>Quản lý Đơn hàng & Thanh toán</h2>
            <p>Theo dõi và xử lý các giao dịch thanh toán đơn hàng ({orders.length} đơn hàng)</p>
          </div>
        </div>
      </div>

      {/* Search and Filter Controls */}
      <div className="order-management-controls">
        <div className="search-section">
          <div className="search-box">
            <input
              type="text"
              placeholder="Tìm kiếm theo Payment ID, Order ID, khách hàng..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>
        </div>
        
        <div className="filter-section">
          <label className="filter-label">Trạng thái:</label>
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="filter-select"
          >
            <option value="all">Tất cả</option>
            <option value="Pending">Chờ xử lý</option>
            <option value="Success">Thành công</option>
            <option value="Failed">Thất bại</option>
          </select>
        </div>

        <div className="filter-section">
          <label className="filter-label">Phương thức:</label>
          <select
            value={filterMethod}
            onChange={(e) => setFilterMethod(e.target.value)}
            className="filter-select"
          >
            <option value="all">Tất cả</option>
            <option value="Tiền mặt">Tiền mặt</option>
            <option value="Chuyển khoản">Chuyển khoản</option>
            <option value="Thẻ tín dụng">Thẻ tín dụng</option>
          </select>
        </div>
      </div>

      {/* CARDS LAYOUT - THAY THẾ TABLE */}
      <div className="orders-content">
        <div className="orders-grid">
          {filteredPayments.map(payment => (
            <div key={payment.paymentId} className="order-card">
              {/* Card Header */}
              <div className="order-card-header">
                <div className="order-code-section">
                  <h3>{payment.orderCode}</h3>
                  <span className="payment-id-badge">
                    #{payment.paymentId.slice(-6)}
                  </span>
                </div>
                {renderStatusBadge(payment.status)}
              </div>

              {/* Customer Info Section */}
              <div className="order-card-section customer-section">
                <div className="section-icon">👤</div>
                <div className="section-content">
                  <h4>Thông tin khách hàng</h4>
                  <div className="info-row">
                    <span className="info-label">Họ tên:</span>
                    <span className="info-value">{payment.customerName}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">SĐT:</span>
                    <span className="info-value">{payment.customerPhone}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Email:</span>
                    <span className="info-value">{payment.customerEmail}</span>
                  </div>
                </div>
              </div>

              {/* Vehicles Section */}
              <div className="order-card-section vehicles-section">
                <div className="section-icon">🚗</div>
                <div className="section-content">
                  <h4>Xe đã đặt ({payment.vehicles.length})</h4>
                  <div className="vehicles-list">
                    {payment.vehicles.slice(0, 2).map((vehicle, index) => (
                      <div key={index} className="vehicle-item">
                        <span className="vehicle-name">
                          {vehicle.name} {vehicle.variant}
                        </span>
                        <span className="vehicle-details">
                          ({vehicle.color}) x{vehicle.quantity}
                        </span>
                      </div>
                    ))}
                    {payment.vehicles.length > 2 && (
                      <div className="more-vehicles">
                        +{payment.vehicles.length - 2} xe khác
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Payment Info Section */}
              <div className="order-card-section payment-info-section">
                <div className="info-row">
                  <span className="info-label">📅 Ngày tạo:</span>
                  <span className="info-value">{formatDateTime(payment.createdDate)}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Thanh toán:</span>
                  <span className="info-value">{renderMethodBadge(payment.method)}</span>
                </div>
              </div>

              {/* Summary Section */}
              <div className="order-card-summary">
                <div className="summary-label">Tổng tiền:</div>
                <div className="summary-amount">{formatCurrency(payment.amount)}</div>
              </div>

              {/* Actions */}
              <div className="order-card-actions">
                <button
                  className="btn-view"
                  onClick={() => setSelectedPayment(payment)}
                >
                  📋 Chi tiết
                </button>
                {payment.status === 'Pending' && (
                  <>
                    <button
                      className="btn-success"
                      onClick={() => updatePaymentStatus(payment.paymentId, 'Success')}
                    >
                      ✓ Xác nhận
                    </button>
                    <button
                      className="btn-failed"
                      onClick={() => updatePaymentStatus(payment.paymentId, 'Failed')}
                    >
                      ✕ Từ chối
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>

        {filteredPayments.length === 0 && (
          <div className="no-orders">
            <div className="no-orders-icon">📄</div>
            <h3>
              {orders.length === 0 ? 
                'Chưa có đơn hàng nào' : 
                'Không tìm thấy đơn hàng phù hợp'
              }
            </h3>
            <p>
              {orders.length === 0 ? 
                'Chưa có đơn hàng nào được tạo. Hãy tạo đơn hàng mới để bắt đầu!' : 
                'Không tìm thấy giao dịch nào phù hợp với bộ lọc.'
              }
            </p>
          </div>
        )}
      </div>

      {/* Payment Detail Modal - GIỮ NGUYÊN */}
      {selectedPayment && (
        <div className="modal-overlay" onClick={() => setSelectedPayment(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Chi tiết đơn hàng #{selectedPayment.orderCode}</h3>
              <button className="modal-close" onClick={() => setSelectedPayment(null)}>×</button>
            </div>

            <div className="modal-body">
              <div className="order-summary">
                <h4>Thông tin thanh toán</h4>
                <div className="summary-grid">
                  <div>Payment ID:</div>
                  <div>#{selectedPayment.paymentId.slice(-8)}</div>
                  <div>Order ID:</div>
                  <div>{selectedPayment.orderCode}</div>
                  <div>Khách hàng:</div>
                  <div>{selectedPayment.customerName}</div>
                  <div>Số điện thoại:</div>
                  <div>{selectedPayment.customerPhone}</div>
                  <div>Email:</div>
                  <div>{selectedPayment.customerEmail}</div>
                  <div>Số tiền:</div>
                  <div className="highlight">{formatCurrency(selectedPayment.amount)}</div>
                  <div>Phương thức:</div>
                  <div>{renderMethodBadge(selectedPayment.method)}</div>
                  <div>Trạng thái:</div>
                  <div>{renderStatusBadge(selectedPayment.status)}</div>
                  <div>Ngày tạo:</div>
                  <div>{formatDateTime(selectedPayment.createdDate)}</div>
                </div>
              </div>

              <div className="vehicles-detail">
                <h4>Danh sách xe</h4>
                {selectedPayment.vehicles.map((vehicle, index) => (
                  <div key={index} className="vehicle-detail-item">
                    <div><strong>Xe:</strong> {vehicle.name} {vehicle.variant}</div>
                    <div><strong>Màu:</strong> {vehicle.color}</div>
                    <div><strong>Số lượng:</strong> {vehicle.quantity}</div>
                    <div><strong>Đơn giá:</strong> {formatCurrency(vehicle.unitPrice)}</div>
                    <div><strong>Thành tiền:</strong> {formatCurrency(vehicle.totalPrice)}</div>
                    <hr />
                  </div>
                ))}
              </div>

              {selectedPayment.promotion && (
                <div className="promotion-detail">
                  <h4>Khuyến mãi</h4>
                  <div><strong>Chương trình:</strong> {selectedPayment.promotion.tenKhuyenMai}</div>
                  <div><strong>Mô tả:</strong> {selectedPayment.promotion.moTa}</div>
                  <div><strong>Giá trị:</strong> {selectedPayment.promotion.loai === 'VNĐ' ? 
                    formatCurrency(selectedPayment.promotion.giaTri) : 
                    `${selectedPayment.promotion.giaTri}%`
                  }</div>
                </div>
              )}

              <div className="financing-detail">
                <h4>Thông tin tài chính</h4>
                <div><strong>Hình thức:</strong> {selectedPayment.financing?.phuongThucThanhToan}</div>
                {selectedPayment.financing?.phuongThucThanhToan === 'Trả góp' && (
                  <>
                    <div><strong>Số kỳ hạn:</strong> {selectedPayment.financing.loanTerm} tháng</div>
                    <div><strong>Lãi suất:</strong> {selectedPayment.financing.laiSuat}%/năm</div>
                  </>
                )}
                <div><strong>Phương thức thanh toán:</strong> {selectedPayment.method}</div>
              </div>
            </div>

            <div className="modal-footer">
              <button className="cancel-btn" onClick={() => setSelectedPayment(null)}>
                Đóng
              </button>
              {selectedPayment.status === 'Pending' && (
                <>
                  <button 
                    className="confirm-btn" 
                    onClick={() => {
                      updatePaymentStatus(selectedPayment.paymentId, 'Success');
                      updateOrderStatus(selectedPayment.orderId, 'Đã thanh toán');
                      setSelectedPayment(null);
                    }}
                  >
                    Xác nhận thanh toán
                  </button>
                  <button 
                    className="reject-btn" 
                    onClick={() => {
                      updatePaymentStatus(selectedPayment.paymentId, 'Failed');
                      updateOrderStatus(selectedPayment.orderId, 'Thanh toán thất bại');
                      setSelectedPayment(null);
                    }}
                  >
                    Từ chối thanh toán
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderFeatureManagementPayment;