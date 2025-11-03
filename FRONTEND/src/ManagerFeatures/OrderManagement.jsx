import React, { useState, useEffect } from 'react';
import './OrderManagement.css';
import { getAllDealerOrders, updateOrderStatus } from '../services/carVariantApi';

const OrderManagement = () => {
  const [orders, setOrders] = useState([]);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [filterMethod, setFilterMethod] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState(false);

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

  // Xác nhận đơn hàng
  const handleConfirmOrder = async (orderId, paymentMethod) => {
    if (!window.confirm('Bạn có chắc chắn muốn xác nhận đơn hàng này?')) {
      return;
    }
    
    try {
      setUpdating(true);
      
      // Kiểm tra phương thức thanh toán để quyết định status mới
      let newStatus;
      if (paymentMethod === 'Trả góp') {
        newStatus = 'Đang trả góp';
      } else {
        newStatus = 'Chưa thanh toán';
      }
      
      await updateOrderStatus(orderId, newStatus);
      await loadOrders(); // Reload data
      alert(`Xác nhận đơn hàng thành công! Trạng thái: ${newStatus}`);
    } catch (error) {
      alert('Lỗi khi xác nhận đơn hàng: ' + error.message);
    } finally {
      setUpdating(false);
    }
  };

  // Từ chối đơn hàng
  const handleRejectOrder = async (orderId) => {
    if (!window.confirm('Bạn có chắc chắn muốn từ chối đơn hàng này?')) {
      return;
    }
    
    try {
      setUpdating(true);
      await updateOrderStatus(orderId, 'Đã hủy');
      await loadOrders(); // Reload data
      alert('Từ chối đơn hàng thành công!');
    } catch (error) {
      alert('Lỗi khi từ chối đơn hàng: ' + error.message);
    } finally {
      setUpdating(false);
    }
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
      'Trả thẳng': { icon: '💰', class: 'method-cash' },
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
          <div className="order-management-header-icon">📋</div>
          <div className="order-management-header-text">
            <h2>Quản lý Đơn hàng (Manager)</h2>
            <p>Xác nhận và quản lý các đơn hàng của đại lý ({orders.length} đơn hàng)</p>
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
                  placeholder="Tìm kiếm theo Order ID, khách hàng..."
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
                <option value="Trả thẳng">Trả thẳng</option>
                <option value="Trả góp">Trả góp</option>
                <option value="Thanh toán trả góp">Thanh toán trả góp</option>
              </select>
            </div>
          </div>

          {/* CARDS LAYOUT */}
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
                  </div>

              {/* Customer Info Section */}
              <div className="order-card-section customer-section">
                <div className="section-icon">👤</div>
                <div className="section-content">
                  <h4>Khách hàng</h4>
                  <div className="info-row">
                    <span className="info-label">Họ tên:</span>
                    <span className="info-value">{payment.customerName}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">SĐT:</span>
                    <span className="info-value">{payment.customerPhone}</span>
                  </div>
                </div>
              </div>

              {/* Vehicles Section */}
              <div className="order-card-section vehicles-section">
                <div className="section-icon">🚗</div>
                <div className="section-content">
                  <h4>Xe đã đặt</h4>
                  <div className="vehicles-list">
                    {payment.vehicles.length > 0 ? (
                      <>
                        {payment.vehicles.slice(0, 1).map((vehicle, index) => (
                          <div key={index} className="vehicle-item">
                            <span className="vehicle-name">
                              {vehicle.name} {vehicle.variant}
                            </span>
                            <span className="vehicle-details">
                              ({vehicle.color}) x{vehicle.quantity}
                            </span>
                          </div>
                        ))}
                        {payment.vehicles.length > 1 && (
                          <div className="more-vehicles">
                            +{payment.vehicles.length - 1} xe khác
                          </div>
                        )}
                      </>
                    ) : (
                      <div className="vehicle-item">
                        <span className="vehicle-name vehicle-name-empty">
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
                  <span className="info-label">Trạng thái:</span>
                  <span className="info-value">{renderStatusBadge(payment.status)}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Thanh toán:</span>
                  <span className="info-value">{renderMethodBadge(payment.paymentMethod)}</span>
                </div>
              </div>

              {/* Summary Section */}
              <div className="order-card-summary">
                <div className="summary-row total">
                  <span className="summary-label">Tổng tiền:</span>
                  <span className="summary-amount">{formatCurrency(payment.total)}</span>
                </div>
                {payment.discountAmount > 0 && (
                  <div className="summary-row discount">
                    <span className="summary-label">Đã giảm:</span>
                    <span className="summary-value">{formatCurrency(payment.discountAmount)}</span>
                  </div>
                )}
              </div>

                  {/* Actions - Manager có quyền xác nhận/từ chối */}
                  <div className="order-card-actions">
                    <button
                      className="btn-view"
                      onClick={() => setSelectedPayment(payment)}
                    >
                      📋 Chi tiết
                    </button>
                    {(payment.status === 'Chưa xác nhận' || payment.status === 'Đang xử lý') && (
                      <>
                        <button
                          className="btn-success"
                          onClick={() => handleConfirmOrder(payment.orderId, payment.paymentMethod)}
                          disabled={updating}
                        >
                          ✓ Xác nhận
                        </button>
                        <button
                          className="btn-failed"
                          onClick={() => handleRejectOrder(payment.orderId)}
                          disabled={updating}
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
                    'Chưa có đơn hàng nào được tạo.' : 
                    'Không tìm thấy đơn hàng nào phù hợp với bộ lọc.'
                  }
                </p>
              </div>
            )}
          </div>

          {/* Payment Detail Modal */}
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
                      <div className="no-data-message">
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
                  {(selectedPayment.status === 'Chưa xác nhận' || selectedPayment.status === 'Đang xử lý') && (
                    <>
                      <button 
                        className="confirm-btn" 
                        onClick={() => {
                          handleConfirmOrder(selectedPayment.orderId, selectedPayment.paymentMethod);
                          setSelectedPayment(null);
                        }}
                        disabled={updating}
                      >
                        ✓ Xác nhận đơn hàng
                      </button>
                      <button 
                        className="reject-btn" 
                        onClick={() => {
                          handleRejectOrder(selectedPayment.orderId);
                          setSelectedPayment(null);
                        }}
                        disabled={updating}
                      >
                        ✕ Từ chối đơn hàng
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default OrderManagement;
