import React, { useState } from 'react';
import './CreateOrderFeature.css';

const CreateOrderFeature = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSeries, setSelectedSeries] = useState('');
  const [orderData, setOrderData] = useState({
    customer: { name: '', phone: '', email: '' },
    selectedVehicles: [],
    promotion: null,
    financing: { phuongThucThanhToan: 'Trả thẳng', loanTerm: 12, laiSuat: 8.5 },
    payment: { phuongThuc: 'Tiền mặt', ghiChu: '' }
  });

  // Data xe - rút gọn properties không cần thiết
  const vehicles = [
    {
      maXe: 1, name: 'VinFast VF3', variant: 'Eco', donGia: 240000000,
      colors: ['Trắng', 'Đen'], colorPrices: { 'Trắng': 240000000, 'Đen': 250000000 },
      images: { 'Trắng': '/images/vf3 trang.png', 'Đen': '/images/vf3 den.png' },
      defaultImage: '/images/vf3 trang.png', stock: 15
    },
    {
      maXe: 2, name: 'VinFast VF3', variant: 'Plus', donGia: 300000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 310000000, 'Đen': 300000000, 'Đỏ': 300000000, 'Xanh dương': 310000000 },
      images: { 'Trắng': '/images/vf3 trang.png', 'Đen': '/images/vf3 den.png', 'Đỏ': '/images/vf3 do.png', 'Xanh dương': '/images/vf3 xanh duong.png' },
      defaultImage: '/images/vf3 trang.png', stock: 12
    },
    {
      maXe: 3, name: 'VinFast VF5', variant: 'Eco', donGia: 370000000,
      colors: ['Trắng', 'Đen'], colorPrices: { 'Trắng': 370000000, 'Đen': 375000000 },
      images: { 'Trắng': '/images/vf5 trang.png', 'Đen': '/images/vf5 den.png' },
      defaultImage: '/images/vf5 trang.png', stock: 10
    },
    {
      maXe: 4, name: 'VinFast VF5', variant: 'Plus', donGia: 420000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 420000000, 'Đen': 425000000, 'Đỏ': 430000000, 'Xanh dương': 435000000 },
      images: { 'Trắng': '/images/vf5 trang.png', 'Đen': '/images/vf5 den.png', 'Đỏ': '/images/vf5 do.png', 'Xanh dương': '/images/vf5 xanh duong.png' },
      defaultImage: '/images/vf5 trang.png', stock: 8
    },
    {
      maXe: 5, name: 'VinFast VF7', variant: 'Eco', donGia: 650000000,
      colors: ['Trắng', 'Đen'], colorPrices: { 'Trắng': 650000000, 'Đen': 655000000 },
      images: { 'Trắng': '/images/vf7 trang.jpg', 'Đen': '/images/vf7 den.jpg' },
      defaultImage: '/images/vf7 trang.jpg', stock: 6
    },
    {
      maXe: 6, name: 'VinFast VF7', variant: 'Plus', donGia: 720000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 720000000, 'Đen': 725000000, 'Đỏ': 730000000, 'Xanh dương': 735000000, 'Xanh rêu': 740000000 },
      images: { 'Trắng': '/images/vf7 trang.jpg', 'Đen': '/images/vf7 den.jpg', 'Đỏ': '/images/vf7 do.jpg', 'Xanh dương': '/images/vf7 xanh duong.jpg', 'Xanh rêu': '/images/vf7 xanh reu.jpg' },
      defaultImage: '/images/vf7 trang.jpg', stock: 4
    },
    {
      maXe: 7, name: 'VinFast VF8', variant: 'Eco', donGia: 950000000,
      colors: ['Trắng', 'Đen', 'Đỏ'],
      colorPrices: { 'Trắng': 950000000, 'Đen': 960000000, 'Đỏ': 970000000 },
      images: { 'Trắng': '/images/vf8 trang.webp', 'Đen': '/images/vf8 den.png', 'Đỏ': '/images/vf8 do.jpg' },
      defaultImage: '/images/vf8 trang.webp', stock: 5
    },
    {
      maXe: 8, name: 'VinFast VF8', variant: 'Plus', donGia: 1050000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1050000000, 'Đen': 1060000000, 'Đỏ': 1070000000, 'Xanh dương': 1080000000, 'Xanh rêu': 1090000000 },
      images: { 'Trắng': '/images/vf8 trang.webp', 'Đen': '/images/vf8 den.png', 'Đỏ': '/images/vf8 do.jpg', 'Xanh dương': '/images/vf8 xanh duong.png', 'Xanh rêu': '/images/vf8 xanh reu.webp' },
      defaultImage: '/images/vf8 trang.webp', stock: 3
    },
    {
      maXe: 9, name: 'VinFast VF9', variant: 'Eco', donGia: 1250000000,
      colors: ['Trắng', 'Đen'], colorPrices: { 'Trắng': 1250000000, 'Đen': 1260000000 },
      images: { 'Trắng': '/images/vf9 trang.jpg', 'Đen': '/images/vf9 den.png' },
      defaultImage: '/images/vf9 trang.jpg', stock: 2
    },
    {
      maXe: 10, name: 'VinFast VF9', variant: 'Plus', donGia: 1350000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1350000000, 'Đen': 1360000000, 'Đỏ': 1370000000, 'Xanh dương': 1380000000, 'Xanh rêu': 1390000000 },
      images: { 'Trắng': '/images/vf9 trang.jpg', 'Đen': '/images/vf9 den.png', 'Đỏ': '/images/vf9 do.png', 'Xanh dương': '/images/vf9 xanh duong.png', 'Xanh rêu': '/images/vf9 xanh reu.png' },
      defaultImage: '/images/vf9 trang.jpg', stock: 1
    }
  ];

  const promotions = [
    { maKhuyenMai: 1, tenKhuyenMai: 'Ưu đãi cuối năm 2024', moTa: 'Giảm giá trực tiếp cho khách hàng mua xe trong tháng 12', giaTri: 50000000, loai: 'VNĐ', ngayBatDau: '2024-12-01', ngayKetThuc: '2024-12-31' },
    { maKhuyenMai: 2, tenKhuyenMai: 'Giảm giá theo %', moTa: 'Giảm 5% tổng giá trị đơn hàng', giaTri: 5, loai: '%', ngayBatDau: '2024-11-01', ngayKetThuc: '2024-12-31' }
  ];

  const formatPrice = (price) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);

  // Helper functions - rút gọn
  const addVehicleToCart = (vehicle, color, quantity) => {
    const colorPrice = vehicle.colorPrices?.[color] || vehicle.donGia;
    setOrderData(prev => ({
      ...prev,
      selectedVehicles: [...prev.selectedVehicles, { vehicle, color, quantity, colorPrice }]
    }));
  };

  const removeVehicleFromCart = (index) => {
    setOrderData(prev => ({
      ...prev,
      selectedVehicles: prev.selectedVehicles.filter((_, i) => i !== index)
    }));
  };

  const updateVehicleInCart = (index, quantity) => {
    setOrderData(prev => ({
      ...prev,
      selectedVehicles: prev.selectedVehicles.map((item, i) => 
        i === index ? { ...item, quantity } : item
      )
    }));
  };

  const calculateTotal = () => {
    let total = orderData.selectedVehicles.reduce((sum, item) => sum + (item.colorPrice * item.quantity), 0);
    
    if (orderData.promotion) {
      if (orderData.promotion.loai === 'VNĐ') {
        total -= orderData.promotion.giaTri;
      } else if (orderData.promotion.loai === '%') {
        total = total * (1 - orderData.promotion.giaTri / 100);
      }
    }
    
    return Math.max(total, 0);
  };

  const handleCustomerChange = (field, value) => {
    setOrderData(prev => ({
      ...prev,
      customer: { ...prev.customer, [field]: value }
    }));
  };

  // Filter vehicles - rút gọn
  const filteredVehicles = vehicles.filter(vehicle => {
    const matchesSearch = vehicle.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         vehicle.variant.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesSeries = selectedSeries === '' || vehicle.name.includes(selectedSeries);
    return matchesSearch && matchesSeries;
  });

  const vehicleSeries = [...new Set(vehicles.map(v => v.name.split(' ')[1]))];

  const canProceedToNextStep = () => {
    switch (currentStep) {
      case 1: return orderData.customer.name && orderData.customer.phone && orderData.customer.email;
      case 2: return orderData.selectedVehicles.length > 0;
      case 3: return true;
      case 4: return orderData.financing.phuongThucThanhToan && orderData.payment.phuongThuc;
      default: return true;
    }
  };

  const nextStep = () => {
    if (currentStep < 5 && canProceedToNextStep()) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  // Rút gọn submitOrder - loại bỏ console.log và comment không cần thiết
  const submitOrder = () => {
    try {
      if (!orderData.customer.name || !orderData.customer.phone || !orderData.customer.email) {
        alert('Vui lòng nhập đầy đủ thông tin khách hàng!');
        setCurrentStep(1);
        return;
      }

      if (orderData.selectedVehicles.length === 0) {
        alert('Vui lòng chọn ít nhất một xe!');
        setCurrentStep(2);
        return;
      }

      if (!orderData.financing.phuongThucThanhToan || !orderData.payment.phuongThuc) {
        alert('Vui lòng chọn phương thức thanh toán!');
        setCurrentStep(4);
        return;
      }

      const khachHang = {
        MaKhachHang: Date.now(),
        HoTen: orderData.customer.name,
        Email: orderData.customer.email,
        SoDienThoai: orderData.customer.phone,
        NgayTao: new Date().toISOString(),
        orders: [],
        totalOrders: 0,
        totalSpent: '0 VNĐ',
        lastActivity: new Date().toISOString()
      };

      const existingCustomers = JSON.parse(localStorage.getItem('customers') || '[]');
      let customerExists = existingCustomers.find(c => c.Email === khachHang.Email || c.SoDienThoai === khachHang.SoDienThoai);
      
      const orderForHistory = {
        id: `DH${Date.now()}`,
        date: new Date().toLocaleDateString('vi-VN'),
        vehicle: orderData.selectedVehicles.map(item => `${item.vehicle.name} (${item.color})`).join(', '),
        amount: formatPrice(calculateTotal()),
        status: 'Chờ xử lý'
      };
      
      if (!customerExists) {
        khachHang.orders = [orderForHistory];
        khachHang.totalOrders = 1;
        khachHang.totalSpent = formatPrice(calculateTotal());
        existingCustomers.push(khachHang);
      } else {
        customerExists.orders = customerExists.orders || [];
        customerExists.orders.push(orderForHistory);
        customerExists.totalOrders = (customerExists.totalOrders || 0) + 1;
        const currentSpent = parseFloat(customerExists.totalSpent.replace(/[^\d]/g, '')) || 0;
        customerExists.totalSpent = formatPrice(currentSpent + calculateTotal());
        customerExists.lastActivity = new Date().toISOString();
      }

      localStorage.setItem('customers', JSON.stringify(existingCustomers));

      alert(`🎉 ĐÔN HÀNG ĐÃ ĐƯỢC TẠO THÀNH CÔNG!\n\n📋 Mã: DH${Date.now()}\n👤 KH: ${khachHang.HoTen}\n💰 Tổng: ${formatPrice(calculateTotal())}`);
      
      // Reset form
      setOrderData({
        customer: { name: '', phone: '', email: '' },
        selectedVehicles: [],
        promotion: null,
        financing: { phuongThucThanhToan: 'Trả thẳng', loanTerm: 12, laiSuat: 8.5 },
        payment: { phuongThuc: 'Tiền mặt', ghiChu: '' }
      });
      setCurrentStep(1);
      
    } catch (error) {
      alert('Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại!');
    }
  };

  return (
    <div className="create-order-feature">
      <div className="feature-header">
        <h2>Tạo đơn hàng mới</h2>
        <p>Tạo đơn hàng cho khách hàng một cách nhanh chóng và chính xác</p>
      </div>

      <div className="order-progress">
        <div className="progress-steps">
          {[
            { step: 1, title: 'Thông tin KH' },
            { step: 2, title: 'Chọn xe' },
            { step: 3, title: 'Khuyến mãi' },
            { step: 4, title: 'Thanh toán' },
            { step: 5, title: 'Xác nhận' }
          ].map(({ step, title }) => (
            <div key={step} className={`progress-step ${currentStep >= step ? 'active' : ''} ${currentStep > step ? 'completed' : ''}`}>
              <div className="step-number">{step}</div>
              <div className="step-title">{title}</div>
            </div>
          ))}
        </div>
      </div>

      <div className="order-content">
        {currentStep === 1 && <CustomerInfoStep orderData={orderData} handleChange={handleCustomerChange} />}
        {currentStep === 2 && <VehicleSelectionStep 
          vehicles={filteredVehicles} 
          selectedVehicles={orderData.selectedVehicles}
          searchTerm={searchTerm}
          selectedSeries={selectedSeries}
          vehicleSeries={vehicleSeries}
          onSearchChange={setSearchTerm}
          onSeriesChange={setSelectedSeries}
          addVehicleToCart={addVehicleToCart}
          removeVehicleFromCart={removeVehicleFromCart}
          updateVehicleInCart={updateVehicleInCart}
        />}
        {currentStep === 3 && <PromotionStep 
          promotions={promotions} 
          selectedPromotion={orderData.promotion} 
          onSelect={(promotion) => setOrderData(prev => ({ ...prev, promotion }))}
          orderData={orderData} 
        />}
        {currentStep === 4 && <PaymentStep orderData={orderData} setOrderData={setOrderData} total={calculateTotal()} />}
        {currentStep === 5 && <OrderSummary orderData={orderData} total={calculateTotal()} formatPrice={formatPrice} />}
      </div>

      <div className="order-actions">
        {currentStep > 1 && (
          <button className="btn-secondary" onClick={prevStep}>
            Quay lại
          </button>
        )}
        {currentStep < 5 ? (
          <button className="btn-primary" onClick={nextStep} disabled={!canProceedToNextStep()}>
            Tiếp tục
          </button>
        ) : (
          <button className="btn-success" onClick={submitOrder}>
            Tạo đơn hàng
          </button>
        )}
      </div>
    </div>
  );
};

// Rút gọn các component con
const CustomerInfoStep = ({ orderData, handleChange }) => (
  <div className="step-content">
    <h3>Thông tin khách hàng</h3>
    <div className="form-grid">
      {[
        { key: 'name', label: 'Họ và tên *', type: 'text', placeholder: 'Nhập họ và tên' },
        { key: 'phone', label: 'Số điện thoại *', type: 'tel', placeholder: 'Nhập số điện thoại' },
        { key: 'email', label: 'Email *', type: 'email', placeholder: 'Nhập email' }
      ].map(({ key, label, type, placeholder }) => (
        <div key={key} className="form-group">
          <label>{label}</label>
          <input
            type={type}
            value={orderData.customer[key]}
            onChange={(e) => handleChange(key, e.target.value)}
            placeholder={placeholder}
          />
        </div>
      ))}
    </div>
  </div>
);

const VehicleSelectionStep = ({ 
  vehicles, selectedVehicles, searchTerm, selectedSeries, vehicleSeries,
  onSearchChange, onSeriesChange, addVehicleToCart, removeVehicleFromCart, updateVehicleInCart
}) => {
  const [tempSelectedVehicle, setTempSelectedVehicle] = useState(null);
  const [tempColor, setTempColor] = useState('');
  const [tempQuantity, setTempQuantity] = useState(1);

  const formatPrice = (price) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);

  const handleAddToCart = () => {
    if (tempSelectedVehicle && tempColor && tempQuantity > 0) {
      addVehicleToCart(tempSelectedVehicle, tempColor, tempQuantity);
      setTempSelectedVehicle(null);
      setTempColor('');
      setTempQuantity(1);
    }
  };

  const getCurrentImage = (vehicle, color) => vehicle.images?.[color] || vehicle.defaultImage;

  return (
    <div className="step-content">
      <h3>Chọn xe</h3>
      
      <div className="vehicle-search-section">
        <div className="search-controls">
          <div className="search-input-group">
            <input
              type="text"
              placeholder="Tìm kiếm xe (VF8, Plus, Eco...)"
              value={searchTerm}
              onChange={(e) => onSearchChange(e.target.value)}
              className="search-input"
            />
          </div>
          <div className="filter-group">
            <select value={selectedSeries} onChange={(e) => onSeriesChange(e.target.value)} className="series-filter">
              <option value="">Tất cả dòng xe</option>
              {vehicleSeries.map(series => (
                <option key={series} value={series}>{series}</option>
              ))}
            </select>
          </div>
        </div>
        <div className="search-results">Tìm thấy {vehicles.length} xe phù hợp</div>
      </div>

      <div className="vehicle-selection-grid">
        {vehicles.map(vehicle => (
          <div 
            key={vehicle.maXe} 
            className={`vehicle-option ${tempSelectedVehicle?.maXe === vehicle.maXe ? 'selected' : ''}`}
            onClick={() => {
              setTempSelectedVehicle(vehicle);
              setTempColor(vehicle.colors[0]);
            }}
          >
            <img 
              src={getCurrentImage(vehicle, vehicle.colors[0])} 
              alt={`${vehicle.name} ${vehicle.variant}`}
              onError={(e) => {
                e.target.src = 'https://via.placeholder.com/300x200?text=VinFast+' + vehicle.name.split(' ')[1];
              }}
            />
            <div className="vehicle-option-info">
              <h4>{vehicle.name} {vehicle.variant}</h4>
              <div className="price">Từ {formatPrice(Math.min(...Object.values(vehicle.colorPrices || {})) || vehicle.donGia)}</div>
              <div className="stock">Còn {vehicle.stock} xe</div>
              
              <div className="vehicle-colors">
                <span className="colors-label">Màu sắc:</span>
                <div className="colors-list">
                  {vehicle.colors.map(color => (
                    <span key={color} className="color-tag-simple">{color}</span>
                  ))}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {tempSelectedVehicle && (
        <div className="vehicle-customization">
          <div className="customization-section">
            <h4>🚗 Cấu hình xe: {tempSelectedVehicle.name} {tempSelectedVehicle.variant}</h4>
            
            <div className="customization-controls">
              <div className="color-selection">
                <label>Chọn màu sắc:</label>
                <div className="color-options">
                  {tempSelectedVehicle.colors.map(color => (
                    <button
                      key={color}
                      className={`color-option-visual ${tempColor === color ? 'selected' : ''}`}
                      onClick={() => setTempColor(color)}
                    >
                      <div className={`color-swatch color-${color.toLowerCase().replace(/ /g, '-')}`}></div>
                      <span>{color}</span>
                    </button>
                  ))}
                </div>
                <div className="vehicle-preview">
                  <img 
                    src={getCurrentImage(tempSelectedVehicle, tempColor)} 
                    alt={`${tempSelectedVehicle.name} ${tempSelectedVehicle.variant} - ${tempColor}`}
                    onError={(e) => {
                      e.target.src = 'https://via.placeholder.com/400x250?text=VinFast+' + tempSelectedVehicle.name.split(' ')[1];
                    }}
                    style={{
                      width: '100%',
                      maxWidth: '400px',
                      height: '250px',
                      objectFit: 'cover',
                      borderRadius: '12px',
                      marginTop: '15px',
                      border: '2px solid #e9ecef',
                      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'
                    }}
                  />
                </div>
              </div>
              
              <div className="quantity-selection">
                <div className="price-display">
                  <label>Giá niêm yết:</label>
                  <div className="listed-price">{formatPrice(tempSelectedVehicle.colorPrices?.[tempColor] || tempSelectedVehicle.donGia)}</div>
                </div>
                <label>Số lượng:</label>
                <div className="quantity-controls">
                  <button 
                    className="quantity-btn"
                    onClick={() => setTempQuantity(Math.max(1, tempQuantity - 1))}
                    disabled={tempQuantity <= 1}
                  >
                    -
                  </button>
                  <input
                    type="number"
                    value={tempQuantity}
                    onChange={(e) => setTempQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                    min="1"
                    max={tempSelectedVehicle.stock}
                    className="quantity-input"
                  />
                  <button 
                    className="quantity-btn"
                    onClick={() => setTempQuantity(Math.min(tempSelectedVehicle.stock, tempQuantity + 1))}
                    disabled={tempQuantity >= tempSelectedVehicle.stock}
                  >
                    +
                  </button>
                </div>
                <span className="quantity-limit">Tối đa: {tempSelectedVehicle.stock} xe</span>
              </div>
            </div>
            
            <div className="selection-summary">
              <div className="summary-item"><strong>Xe:</strong> {tempSelectedVehicle.name} {tempSelectedVehicle.variant}</div>
              <div className="summary-item"><strong>Màu:</strong> {tempColor}</div>
              <div className="summary-item"><strong>Số lượng:</strong> {tempQuantity} xe</div>
              <div className="summary-item total-price">
                <strong>Thành tiền:</strong> {formatPrice((tempSelectedVehicle.colorPrices?.[tempColor] || tempSelectedVehicle.donGia) * tempQuantity)}
              </div>
              <button 
                className="add-to-cart-btn"
                onClick={handleAddToCart}
                disabled={!tempColor || tempQuantity <= 0}
              >
                Thêm vào giỏ hàng
              </button>
            </div>
          </div>
        </div>
      )}

      {selectedVehicles.length > 0 && (
        <div className="shopping-cart">
          <h4>🛒 Giỏ hàng ({selectedVehicles.length} mặt hàng)</h4>
          <div className="cart-items">
            {selectedVehicles.map((item, index) => (
              <div key={index} className="cart-item">
                <div className="cart-item-image">
                  <img 
                    src={getCurrentImage(item.vehicle, item.color)} 
                    alt={`${item.vehicle.name} ${item.vehicle.variant} - ${item.color}`}
                    onError={(e) => {
                      e.target.src = 'https://via.placeholder.com/120x80?text=VinFast+' + item.vehicle.name.split(' ')[1];
                    }}
                    style={{
                      width: '120px',
                      height: '80px',
                      objectFit: 'cover',
                      borderRadius: '8px',
                      marginRight: '15px'
                    }}
                  />
                </div>
                <div className="cart-item-info">
                  <h5>{item.vehicle.name} {item.vehicle.variant}</h5>
                  <p>Màu: {item.color}</p>
                  <p>Số lượng: {item.quantity}</p>
                  <p className="cart-item-price">Thành tiền: {formatPrice(item.colorPrice * item.quantity)}</p>
                </div>
                <div className="cart-item-controls">
                  <input
                    type="number"
                    value={item.quantity}
                    min="1"
                    max={item.vehicle.stock}
                    onChange={(e) => updateVehicleInCart(index, parseInt(e.target.value) || 1)}
                    className="cart-quantity-input"
                  />
                  <button 
                    className="remove-btn"
                    onClick={() => removeVehicleFromCart(index)}
                  >
                    Xóa
                  </button>
                </div>
              </div>
            ))}
          </div>
          <div className="cart-total">
            <h4>Tổng cộng: {formatPrice(selectedVehicles.reduce((sum, item) => sum + (item.colorPrice * item.quantity), 0))}</h4>
          </div>
        </div>
      )}
    </div>
  );
};

const PromotionStep = ({ promotions, selectedPromotion, onSelect }) => (
  <div className="step-content">
    <h3>Chọn chương trình khuyến mãi (tùy chọn)</h3>
    <div className="promotions-grid">
      <div 
        className={`promotion-item ${!selectedPromotion ? 'selected' : ''}`}
        onClick={() => onSelect(null)}
      >
        <div className="promotion-info">
          <h4>Không áp dụng khuyến mãi</h4>
          <div className="promotion-desc">Giá niêm yết gốc</div>
        </div>
        <div className="promotion-checkbox">
          {!selectedPromotion ? '✓' : ''}
        </div>
      </div>
      
      {promotions.map(promotion => (
        <div 
          key={promotion.maKhuyenMai} 
          className={`promotion-item ${selectedPromotion?.maKhuyenMai === promotion.maKhuyenMai ? 'selected' : ''}`}
          onClick={() => onSelect(promotion)}
        >
          <div className="promotion-info">
            <h4>{promotion.tenKhuyenMai}</h4>
            <div className="promotion-desc">{promotion.moTa}</div>
            <div className="promotion-value">
              Giảm: {promotion.loai === 'VNĐ' 
                ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(promotion.giaTri)
                : `${promotion.giaTri}%`
              }
            </div>
            <div className="promotion-period">
              Từ {new Date(promotion.ngayBatDau).toLocaleDateString('vi-VN')} đến {new Date(promotion.ngayKetThuc).toLocaleDateString('vi-VN')}
            </div>
          </div>
          <div className="promotion-checkbox">
            {selectedPromotion?.maKhuyenMai === promotion.maKhuyenMai ? '✓' : ''}
          </div>
        </div>
      ))}
    </div>
  </div>
);

const PaymentStep = ({ orderData, setOrderData, total }) => (
  <div className="step-content">
    <h3>Phương thức thanh toán</h3>
    <div className="payment-options">
      {[
        { value: 'Trả thẳng', label: 'Trả thẳng (Thanh toán toàn bộ)', desc: 'Thanh toán 100% giá trị xe ngay khi ký hợp đồng' },
        { value: 'Trả góp', label: 'Trả góp', desc: 'Trả trước một phần, phần còn lại trả theo tháng' }
      ].map(({ value, label, desc }) => (
        <div key={value} className="payment-method">
          <label>
            <input
              type="radio"
              name="paymentMethod"
              value={value}
              checked={orderData.financing.phuongThucThanhToan === value}
              onChange={(e) => setOrderData(prev => ({
                ...prev,
                financing: { ...prev.financing, phuongThucThanhToan: e.target.value }
              }))}
            />
            {label}
          </label>
          <p>{desc}</p>
          
          {value === 'Trả góp' && orderData.financing.phuongThucThanhToan === 'Trả góp' && (
            <div className="installment-details">
              <div className="form-group">
                <label>Số kỳ hạn (tháng)</label>
                <select
                  value={orderData.financing.loanTerm}
                  onChange={(e) => setOrderData(prev => ({
                    ...prev,
                    financing: { ...prev.financing, loanTerm: parseInt(e.target.value) }
                  }))}
                >
                  {[12, 24, 36, 48, 60].map(term => (
                    <option key={term} value={term}>{term} tháng</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Lãi suất (%/năm)</label>
                <input
                  type="number"
                  step="0.1"
                  value={orderData.financing.laiSuat}
                  onChange={(e) => setOrderData(prev => ({
                    ...prev,
                    financing: { ...prev.financing, laiSuat: parseFloat(e.target.value) || 0 }
                  }))}
                  min="0"
                  max="25"
                />
              </div>
            </div>
          )}
        </div>
      ))}
    </div>
    
    <div className="total-summary">
      <h4>Tổng giá trị đơn hàng: {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(total)}</h4>
    </div>
  </div>
);

const OrderSummary = ({ orderData, total, formatPrice }) => {
  const subtotal = orderData.selectedVehicles.reduce((sum, item) => sum + (item.colorPrice * item.quantity), 0);
  
  return (
    <div className="step-content">
      <h3>Xác nhận đơn hàng</h3>
      <div className="order-summary">
        <div className="summary-section">
          <h4>Thông tin khách hàng</h4>
          <p><strong>Tên:</strong> {orderData.customer.name}</p>
          <p><strong>Điện thoại:</strong> {orderData.customer.phone}</p>
          <p><strong>Email:</strong> {orderData.customer.email}</p>
        </div>
        
        <div className="summary-section">
          <h4>Xe đã chọn</h4>
          <div className="selected-vehicles-list">
            {orderData.selectedVehicles.map((item, index) => (
              <div key={index} className="selected-vehicle-item">
                <p><strong>{item.vehicle.name} {item.vehicle.variant}</strong></p>
                <p>Màu sắc: {item.color}</p>
                <p>Số lượng: {item.quantity} xe</p>
                <p>Đơn giá: {formatPrice(item.colorPrice)}</p>
                <hr />
              </div>
            ))}
          </div>
          {orderData.promotion && (
            <p>Khuyến mãi: {orderData.promotion.tenKhuyenMai}</p>
          )}
        </div>
        
        <div className="summary-section">
          <h4>Thanh toán</h4>
          <p><strong>Phương thức:</strong> {orderData.financing.phuongThucThanhToan}</p>
          <p><strong>Hình thức:</strong> {orderData.payment.phuongThuc}</p>
          {orderData.financing.phuongThucThanhToan === 'Trả góp' && (
            <>
              <p><strong>Số kỳ hạn:</strong> {orderData.financing.loanTerm} tháng</p>
              <p><strong>Lãi suất:</strong> {orderData.financing.laiSuat}%/năm</p>
            </>
          )}
          {orderData.payment.ghiChu && (
            <p><strong>Ghi chú:</strong> {orderData.payment.ghiChu}</p>
          )}
        </div>
        
        <div className="total-final">
          <h3>Tổng cộng: {formatPrice(subtotal)}</h3>
          {orderData.promotion && (
            <div className="total-after-promotion">
              <h3>Tổng cộng sau khuyến mãi: {formatPrice(total)}</h3>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CreateOrderFeature;