import React, { useState, useEffect } from 'react';
import './OrderFeatureManagement&Payment.css';
import { getAllDealerOrders } from '../../services/carVariantApi';

const OrderFeatureManagementPayment = () => {
  const [orders, setOrders] = useState([]);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [filterMethod, setFilterMethod] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load orders từ API khi component mount
  useEffect(() => {
    loadOrders();
    
    // Refresh every 30 seconds
    const interval = setInterval(loadOrders, 30000);
    return () => clearInterval(interval);
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAllDealerOrders();
      
      // Transform API data to match expected format
      const transformedOrders = response.map(order => {
        const orderInfo = order.orderInfo || {};
        const customer = order.customer || {};
        const dealer = order.dealer || {};
        const orderDetails = order.orderDetails || [];
        
        return {
          paymentId: orderInfo.orderId,
          orderId: orderInfo.orderId,
          orderCode: `ORD-${String(orderInfo.orderId).padStart(6, '0')}`,
          customerName: customer.customerName,
          customerEmail: customer.customerEmail,
          customerPhone: customer.customerPhone,
          dealerName: dealer.dealerName,
          dealerAddress: dealer.dealerAddress,
          dealerPhone: dealer.dealerPhone,
          subTotal: orderInfo.subTotal || 0,
          discountAmount: orderInfo.discountAmount || 0,
          total: orderInfo.totalAmount || 0,
          paymentMethod: orderInfo.paymentMethod,
          createdDate: orderInfo.orderDate,
          status: orderInfo.status,
          promotionId: orderInfo.promotionId,
          promotionName: orderInfo.promotionName,
          vehicles: orderDetails.map(detail => ({
            orderDetailId: detail.orderDetailId,
            carId: detail.carId,
            name: detail.carName,
            modelName: detail.modelName,
            variant: detail.variantName,
            color: detail.colorName,
            quantity: detail.quantity,
            unitPrice: detail.unitPrice,
            finalPrice: detail.finalPrice,
            totalPrice: detail.finalPrice
          }))
        };
      });
      
      setOrders(transformedOrders);
    } catch (error) {
      setError(error.message || 'Không thể tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  // Sử dụng trực tiếp orders, không cần transform lại
  const payments = orders;

  // Lọc đơn hàng
  const filteredPayments = payments.filter(payment => {
    const searchLower = searchTerm.toLowerCase();
    const paymentIdStr = payment.paymentId ? payment.paymentId.toString() : '';
    const orderCodeStr = payment.orderCode ? payment.orderCode.toLowerCase() : '';
    const customerNameStr = payment.customerName ? payment.customerName.toLowerCase() : '';
    
    const matchesSearch = paymentIdStr.includes(searchTerm) ||
                         orderCodeStr.includes(searchLower) ||
                         customerNameStr.includes(searchLower);
    
    const matchesStatus = filterStatus === 'all' || payment.status === filterStatus;
    const matchesMethod = filterMethod === 'all' || payment.paymentMethod === filterMethod;
    
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

  // Render status badge - CHỈ HIỂN THỊ ORDER STATUS (không còn payment status)
  const renderStatusBadge = (status) => {
    const statusConfig = {
      'Chưa xác nhận': { text: 'Chưa xác nhận', class: 'status-pending' },
      'Đang xử lý': { text: 'Đang xử lý', class: 'status-processing' },
      'Chưa thanh toán': { text: 'Chưa thanh toán', class: 'status-unpaid' },
      'Đang trả góp': { text: 'Đang trả góp', class: 'status-installment' },
      'Đã thanh toán': { text: 'Đã thanh toán', class: 'status-success' },
      'Đã hủy': { text: 'Đã hủy', class: 'status-failed' }
    };
    
    const config = statusConfig[status] || { text: status, class: 'status-pending' };
    return <span className={`status-badge ${config.class}`}>{config.text}</span>;
  };

  // Render method badge
  const renderMethodBadge = (method) => {
    const methodConfig = {
      'Tiền mặt': { icon: '💵', class: 'method-cash' },
      'Chuyển khoản': { icon: '🏦', class: 'method-bank' },
      'Thẻ tín dụng': { icon: '💳', class: 'method-card' },
      'Trả góp': { icon: '📅', class: 'method-ewallet' },
      'Thanh toán trả góp': { icon: '📅', class: 'method-ewallet' }
    };
    
    const config = methodConfig[method] || { icon: '💰', class: 'method-other' };
    return (
      <span className={`method-badge ${config.class}`}>
        {config.icon} {method || 'Chưa xác định'}
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
            <h2>Quản lý Đơn hàng & Thanh toán (Staff)</h2>
            <p>Theo dõi và xử lý các giao dịch thanh toán đơn hàng ({orders.length} đơn hàng)</p>
          </div>
          <button 
            className="refresh-btn" 
            onClick={loadOrders}
            disabled={loading}
            title="Làm mới dữ liệu"
          >
            🔄 Làm mới
          </button>
        </div>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="no-orders">
          <div className="no-orders-icon">⏳</div>
          <h3>Đang tải dữ liệu...</h3>
          <p>Vui lòng chờ trong giây lát</p>
        </div>
      )}

      {/* Error State */}
      {error && !loading && (
        <div className="no-orders">
          <div className="no-orders-icon">⚠️</div>
          <h3>Có lỗi xảy ra</h3>
          <p>{error}</p>
        </div>
      )}

      {/* Main Content - Only show when not loading and no error */}
      {!loading && !error && (
        <>
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
            <option value="Chưa xác nhận">Chưa xác nhận</option>
            <option value="Đang xử lý">Đang xử lý</option>
            <option value="Chưa thanh toán">Chưa thanh toán</option>
            <option value="Đang trả góp">Đang trả góp</option>
            <option value="Đã thanh toán">Đã thanh toán</option>
            <option value="Đã hủy">Đã hủy</option>
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
            <option value="Trả góp">Trả góp</option>
            <option value="Thanh toán trả góp">Thanh toán trả góp</option>
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
                    ID: {payment.orderId}
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
                    {payment.vehicles.length > 0 ? (
                      <>
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
                      </>
                    ) : (
                      <div className="vehicle-item">
                        <span className="vehicle-name" style={{ fontStyle: 'italic', color: '#999' }}>
                          Chưa có xe nào
                        </span>
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
                  <span className="info-label">💳 Thanh toán:</span>
                  <span className="info-value">{renderMethodBadge(payment.paymentMethod)}</span>
                </div>
                {payment.promotionName && (
                  <div className="info-row">
                    <span className="info-label">🎁 Khuyến mãi:</span>
                    <span className="info-value">{payment.promotionName}</span>
                  </div>
                )}
              </div>

              {/* Summary Section */}
              <div className="order-card-summary">
                {payment.discountAmount > 0 && (
                  <div className="summary-row">
                    <span className="summary-label">Tạm tính:</span>
                    <span className="summary-value">{formatCurrency(payment.subTotal)}</span>
                  </div>
                )}
                {payment.discountAmount > 0 && (
                  <div className="summary-row discount">
                    <span className="summary-label">Giảm giá:</span>
                    <span className="summary-value">-{formatCurrency(payment.discountAmount)}</span>
                  </div>
                )}
                <div className="summary-row total">
                  <span className="summary-label">Tổng tiền:</span>
                  <span className="summary-amount">{formatCurrency(payment.total)}</span>
                </div>
              </div>

              {/* Actions - DealerStaff chỉ có nút xem chi tiết */}
              <div className="order-card-actions">
                <button
                  className="btn-view"
                  onClick={() => setSelectedPayment(payment)}
                >
                  📋 Chi tiết
                </button>
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
                <h4>Thông tin đơn hàng</h4>
                <div className="summary-grid">
                  <div>Mã đơn hàng:</div>
                  <div><strong>{selectedPayment.orderCode}</strong></div>
                  <div>Trạng thái:</div>
                  <div>{renderStatusBadge(selectedPayment.status)}</div>
                  <div>Ngày tạo:</div>
                  <div>{formatDateTime(selectedPayment.createdDate)}</div>
                </div>
              </div>

              <div className="order-summary">
                <h4>Thông tin khách hàng</h4>
                <div className="summary-grid">
                  <div>Họ tên:</div>
                  <div><strong>{selectedPayment.customerName}</strong></div>
                  <div>Số điện thoại:</div>
                  <div>{selectedPayment.customerPhone}</div>
                  <div>Email:</div>
                  <div>{selectedPayment.customerEmail}</div>
                </div>
              </div>

              <div className="order-summary">
                <h4>Thông tin đại lý</h4>
                <div className="summary-grid">
                  <div>Tên đại lý:</div>
                  <div><strong>{selectedPayment.dealerName}</strong></div>
                  <div>Địa chỉ:</div>
                  <div>{selectedPayment.dealerAddress}</div>
                  <div>Số điện thoại:</div>
                  <div>{selectedPayment.dealerPhone}</div>
                </div>
              </div>

              <div className="vehicles-detail">
                <h4>Danh sách xe</h4>
                {selectedPayment.vehicles && selectedPayment.vehicles.length > 0 ? (
                  selectedPayment.vehicles.map((vehicle, index) => (
                    <div key={index} className="vehicle-detail-item">
                      <div><strong>Xe:</strong> {vehicle.name}</div>
                      <div><strong>Dòng xe:</strong> {vehicle.modelName}</div>
                      <div><strong>Phiên bản:</strong> {vehicle.variant}</div>
                      <div><strong>Màu sắc:</strong> {vehicle.color}</div>
                      <div><strong>Số lượng:</strong> {vehicle.quantity}</div>
                      <div><strong>Đơn giá:</strong> {formatCurrency(vehicle.unitPrice)}</div>
                      <div><strong>Thành tiền:</strong> {formatCurrency(vehicle.finalPrice)}</div>
                      <hr />
                    </div>
                  ))
                ) : (
                  <div style={{ fontStyle: 'italic', color: '#999', padding: '10px' }}>
                    Chưa có xe nào trong đơn hàng
                  </div>
                )}
              </div>

              {selectedPayment.promotionName && (
                <div className="promotion-detail">
                  <h4>Khuyến mãi</h4>
                  <div><strong>Chương trình:</strong> {selectedPayment.promotionName}</div>
                  <div><strong>Giá trị giảm:</strong> {formatCurrency(selectedPayment.discountAmount)}</div>
                </div>
              )}

              <div className="financing-detail">
                <h4>Thông tin thanh toán</h4>
                <div className="summary-grid">
                  <div>Phương thức:</div>
                  <div>{renderMethodBadge(selectedPayment.paymentMethod)}</div>
                  <div>Tạm tính:</div>
                  <div>{formatCurrency(selectedPayment.subTotal)}</div>
                  {selectedPayment.discountAmount > 0 && (
                    <>
                      <div>Giảm giá:</div>
                      <div className="discount-text">-{formatCurrency(selectedPayment.discountAmount)}</div>
                    </>
                  )}
                  <div><strong>Tổng cộng:</strong></div>
                  <div className="highlight"><strong>{formatCurrency(selectedPayment.total)}</strong></div>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button className="cancel-btn" onClick={() => setSelectedPayment(null)}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
        </>
      )}
    </div>
  );
};

export default OrderFeatureManagementPayment;