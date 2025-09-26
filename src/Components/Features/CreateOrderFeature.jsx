import React, { useState } from 'react';
import './CreateOrderFeature.css';

const CreateOrderFeature = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSeries, setSelectedSeries] = useState('');
  const [orderData, setOrderData] = useState({
    customer: {
      maKhachHang: null,
      name: '',
      phone: '',
      email: '',
      address: '',
      idCard: ''
    },
    dealer: {
      maDaiLy: 1,
      name: 'VinFast Quận 1'
    },
    selectedVehicles: [], // Array chứa nhiều xe với màu sắc và số lượng khác nhau
    // Ví dụ: [{vehicle: vehicleObj, color: 'Đỏ', quantity: 2, colorPrice: 970000000}, {vehicle: vehicleObj, color: 'Trắng', quantity: 1, colorPrice: 950000000}]
    vehicle: null, // Deprecated - giữ để tương thích
    selectedColor: '',
    quantity: 1,
    accessories: [],
    promotion: null,
    financing: {
      phuongThucThanhToan: 'Trả thẳng',
      downPayment: 0,
      loanTerm: 12,
      laiSuat: 8.5,
      nganHangHoTro: ''
    },
    payment: {
      phuongThuc: 'Tiền mặt',
      ghiChu: ''
    }
  });

  const vehicles = [
    // VF3 Eco
    {
      maXe: 1,
      name: 'VinFast VF3',
      variant: 'Eco',
      donGia: 240000000, // Giá cơ bản
      giaSauKhuyenMai: 240000000,
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 240000000, 'Đen': 250000000 },
      stock: 15,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '32 kW',
        momen: '110 Nm',
        pin: '18.64 kWh (LFP)',
        doiXe: '4 chỗ'
      }
    },
    // VF3 Plus
    {
      maXe: 2,
      name: 'VinFast VF3',
      variant: 'Plus',
      donGia: 300000000, // Giá cơ bản
      giaSauKhuyenMai: 300000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 310000000, 'Đen': 300000000, 'Đỏ': 300000000, 'Xanh dương': 310000000 },
      stock: 12,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '35 kW',
        momen: '120 Nm',
        pin: '18.64 kWh (LFP)',
        doiXe: '4 chỗ'
      }
    },
    // VF5 Eco
    {
      maXe: 3,
      name: 'VinFast VF5',
      variant: 'Eco',
      donGia: 370000000, // Giá cơ bản
      giaSauKhuyenMai: 370000000,
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 370000000, 'Đen': 375000000 },
      stock: 10,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '70 kW',
        momen: '135 Nm',
        pin: '37.23 kWh (NMC)',
        doiXe: '5 chỗ'
      }
    },
    // VF5 Plus
    {
      maXe: 4,
      name: 'VinFast VF5',
      variant: 'Plus',
      donGia: 420000000, // Giá cơ bản
      giaSauKhuyenMai: 420000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 420000000, 'Đen': 425000000, 'Đỏ': 430000000, 'Xanh dương': 435000000 },
      stock: 8,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '75 kW',
        momen: '140 Nm',
        pin: '37.23 kWh (NMC)',
        doiXe: '5 chỗ'
      }
    },
    // VF7 Eco
    {
      maXe: 5,
      name: 'VinFast VF7',
      variant: 'Eco',
      donGia: 650000000, // Giá cơ bản
      giaSauKhuyenMai: 650000000,
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 650000000, 'Đen': 655000000 },
      stock: 6,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '130 kW',
        momen: '250 Nm',
        pin: '75.30 kWh (NMC)',
        doiXe: '5 chỗ'
      }
    },
    // VF7 Plus
    {
      maXe: 6,
      name: 'VinFast VF7',
      variant: 'Plus',
      donGia: 720000000, // Giá cơ bản
      giaSauKhuyenMai: 720000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 720000000, 'Đen': 725000000, 'Đỏ': 730000000, 'Xanh dương': 735000000, 'Xanh rêu': 740000000 },
      stock: 4,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '150 kW',
        momen: '280 Nm',
        pin: '75.30 kWh (NMC)',
        doiXe: '5 chỗ'
      }
    },
    // VF8 Eco
    {
      maXe: 7,
      name: 'VinFast VF8',
      variant: 'Eco',
      donGia: 950000000, // Giá cơ bản
      giaSauKhuyenMai: 950000000,
      colors: ['Trắng', 'Đen', 'Đỏ'],
      colorPrices: { 'Trắng': 950000000, 'Đen': 960000000, 'Đỏ': 970000000 },
      stock: 5,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '150 kW',
        momen: '320 Nm',
        pin: '82.00 kWh (NMC)',
        doiXe: '5 chỗ'
      }
    },
    // VF8 Plus
    {
      maXe: 8,
      name: 'VinFast VF8',
      variant: 'Plus',
      donGia: 1050000000, // Giá cơ bản
      giaSauKhuyenMai: 1050000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1050000000, 'Đen': 1060000000, 'Đỏ': 1070000000, 'Xanh dương': 1080000000, 'Xanh rêu': 1090000000 },
      stock: 3,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '220 kW',
        momen: '400 Nm',
        pin: '87.70 kWh (NMC)',
        doiXe: '5 chỗ'
      }
    },
    // VF9 Eco
    {
      maXe: 9,
      name: 'VinFast VF9',
      variant: 'Eco',
      donGia: 1250000000, // Giá cơ bản
      giaSauKhuyenMai: 1250000000,
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 1250000000, 'Đen': 1260000000 },
      stock: 2,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '300 kW',
        momen: '500 Nm',
        pin: '92.00 kWh (NMC)',
        doiXe: '7 chỗ'
      }
    },
    // VF9 Plus
    {
      maXe: 10,
      name: 'VinFast VF9',
      variant: 'Plus',
      donGia: 1350000000, // Giá cơ bản
      giaSauKhuyenMai: 1350000000,
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1350000000, 'Đen': 1360000000, 'Đỏ': 1370000000, 'Xanh dương': 1380000000, 'Xanh rêu': 1390000000 },
      stock: 1,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        dongCo: 'Điện',
        congSuat: '320 kW',
        momen: '520 Nm',
        pin: '92.00 kWh (NMC)',
        doiXe: '7 chỗ'
      }
    }
  ];

  // Khuyến mãi có sẵn
  const promotions = [
    {
      maKhuyenMai: 1,
      tenKhuyenMai: 'Ưu đãi cuối năm 2024',
      moTa: 'Giảm giá trực tiếp cho khách hàng mua xe trong tháng 12',
      giaTri: 50000000,
      loai: 'VNĐ',
      ngayBatDau: '2024-12-01',
      ngayKetThuc: '2024-12-31',
      trangThai: 'Đang hoạt động'
    },
    {
      maKhuyenMai: 2,
      tenKhuyenMai: 'Giảm giá theo %',
      moTa: 'Giảm 5% tổng giá trị đơn hàng',
      giaTri: 5,
      loai: '%',
      ngayBatDau: '2024-11-01',
      ngayKetThuc: '2024-12-31',
      trangThai: 'Đang hoạt động'
    }
  ];

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  // Helper functions cho việc chọn nhiều xe
  const addVehicleToCart = (vehicle, color, quantity) => {
    const colorPrice = vehicle.colorPrices ? vehicle.colorPrices[color] : vehicle.donGia;
    const newVehicleItem = {
      vehicle,
      color,
      quantity,
      colorPrice
    };
    
    setOrderData(prev => ({
      ...prev,
      selectedVehicles: [...prev.selectedVehicles, newVehicleItem]
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
    let tongTien = 0;
    
    // Tính tổng từ selectedVehicles (mới)
    orderData.selectedVehicles.forEach(item => {
      tongTien += item.colorPrice * item.quantity;
    });

    // Fallback cho vehicle cũ (tương thích ngược)
    if (orderData.vehicle && orderData.selectedVehicles.length === 0) {
      tongTien = orderData.vehicle.giaSauKhuyenMai * orderData.quantity;
    }
    
    // Áp dụng khuyến mãi nếu có
    if (orderData.promotion) {
      if (orderData.promotion.loai === 'VNĐ') {
        tongTien -= orderData.promotion.giaTri;
      } else if (orderData.promotion.loai === '%') {
        tongTien = tongTien * (1 - orderData.promotion.giaTri / 100);
      }
    }
    
    return Math.max(tongTien, 0);
  };

  const handleCustomerChange = (field, value) => {
    setOrderData(prev => ({
      ...prev,
      customer: { ...prev.customer, [field]: value }
    }));
  };

  const handleVehicleSelect = (vehicle) => {
    setOrderData(prev => ({ 
      ...prev, 
      vehicle,
      selectedColor: vehicle.colors[0] || '',
      quantity: 1
    }));
  };

  const handleColorSelect = (color) => {
    setOrderData(prev => ({ ...prev, selectedColor: color }));
  };

  const handleQuantityChange = (quantity) => {
    setOrderData(prev => ({ ...prev, quantity: Math.max(1, quantity) }));
  };

  // Filter vehicles based on search term and selected series
  const filteredVehicles = vehicles.filter(vehicle => {
    const matchesSearch = vehicle.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         vehicle.variant.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesSeries = selectedSeries === '' || vehicle.name.includes(selectedSeries);
    return matchesSearch && matchesSeries;
  });

  const vehicleSeries = [...new Set(vehicles.map(v => v.name.split(' ')[1]))]; // VF3, VF5, VF6, etc.

  const handlePromotionSelect = (promotion) => {
    setOrderData(prev => ({ ...prev, promotion }));
  };

  const handleAccessoryToggle = (accessory) => {
    setOrderData(prev => {
      const isSelected = prev.accessories.find(acc => acc.id === accessory.id);
      if (isSelected) {
        return {
          ...prev,
          accessories: prev.accessories.filter(acc => acc.id !== accessory.id)
        };
      } else {
        return {
          ...prev,
          accessories: [...prev.accessories, accessory]
        };
      }
    });
  };

  const canProceedToNextStep = () => {
    switch (currentStep) {
      case 1:
        return orderData.customer.name && orderData.customer.phone && orderData.customer.email;
      case 2:
        return orderData.selectedVehicles.length > 0; // Phải có xe trong giỏ hàng
      case 3:
        return true; // Promotion step is optional
      case 4:
        return orderData.financing.phuongThucThanhToan && orderData.payment.phuongThuc;
      default:
        return true;
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

  const submitOrder = () => {
    try {
      // Validate dữ liệu trước khi tạo đơn hàng
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

      // Tạo thông tin khách hàng mới (sẽ lưu vào database KHACHHANG)
      const khachHang = {
        MaKhachHang: orderData.customer.maKhachHang || Date.now(), // Tạo mã tạm thời
        HoTen: orderData.customer.name,
        Email: orderData.customer.email,
        SoDienThoai: orderData.customer.phone,
        DiaChi: null, // Không cần địa chỉ theo yêu cầu
        NgayTao: new Date().toISOString(),
        // Add default fields for customer management
        orders: [],
        totalOrders: 0,
        totalSpent: '0 VNĐ',
        lastActivity: new Date().toISOString()
      };

      // Tạo đơn hàng
      const donHang = {
        maKhachHang: khachHang.MaKhachHang,
        maDaiLy: orderData.dealer.maDaiLy,
        ngayDat: new Date().toISOString(),
        tongTien: calculateTotal(),
        phuongThucThanhToan: orderData.financing.phuongThucThanhToan,
        trangThai: 'Chờ xử lý',
        ghiChu: orderData.payment.ghiChu || ''
      };

      // Tạo chi tiết đơn hàng cho từng xe đã chọn
      const chiTietDonHang = orderData.selectedVehicles.map(item => ({
        maXe: item.vehicle.maXe,
        soLuong: item.quantity,
        mauSac: item.color,
        donGia: item.colorPrice,
        thanhTien: item.colorPrice * item.quantity
      }));

      // Log dữ liệu để debug
      console.log('=== TẠO ĐÔN HÀNG ===');
      console.log('Khách hàng:', khachHang);
      console.log('Đơn hàng:', donHang);
      console.log('Chi tiết đơn hàng:', chiTietDonHang);

      // TODO: Gọi API để lưu vào database
      // - Lưu khách hàng vào bảng KHACHHANG
      // - Lưu đơn hàng vào bảng DONHANG
      // - Lưu chi tiết đơn hàng vào bảng CHITIETDONHANG

      // Mô phỏng lưu khách hàng vào localStorage để demo kết nối với Quản lý khách hàng
      const existingCustomers = JSON.parse(localStorage.getItem('customers') || '[]');
      let customerExists = existingCustomers.find(c => c.Email === khachHang.Email || c.SoDienThoai === khachHang.SoDienThoai);
      
      // Tạo thông tin đơn hàng cho lịch sử khách hàng
      const orderForHistory = {
        id: `DH${Date.now()}`,
        date: new Date().toLocaleDateString('vi-VN'),
        vehicle: orderData.selectedVehicles.map(item => `${item.vehicle.name} (${item.color})`).join(', '),
        amount: formatPrice(calculateTotal()),
        status: 'Chờ xử lý'
      };
      
      if (!customerExists) {
        // Khách hàng mới - thêm đơn hàng đầu tiên
        khachHang.orders = [orderForHistory];
        khachHang.totalOrders = 1;
        khachHang.totalSpent = formatPrice(calculateTotal());
        khachHang.lastActivity = new Date().toISOString();
        
        existingCustomers.push(khachHang);
        localStorage.setItem('customers', JSON.stringify(existingCustomers));
        console.log('✅ Khách hàng mới đã được thêm vào hệ thống:', khachHang);
      } else {
        // Khách hàng đã tồn tại - cập nhật đơn hàng
        customerExists.orders = customerExists.orders || [];
        customerExists.orders.push(orderForHistory);
        customerExists.totalOrders = (customerExists.totalOrders || 0) + 1;
        
        // Tính tổng chi tiêu (parse và cộng dồn)
        const currentSpent = parseFloat(customerExists.totalSpent.replace(/[^\d]/g, '')) || 0;
        const newSpent = currentSpent + calculateTotal();
        customerExists.totalSpent = formatPrice(newSpent);
        customerExists.lastActivity = new Date().toISOString();
        
        localStorage.setItem('customers', JSON.stringify(existingCustomers));
        console.log('ℹ️ Đã cập nhật đơn hàng cho khách hàng:', customerExists);
      }

      const orderSummary = `
      ═══════════════════════════════════════
      🎉 ĐÔN HÀNG ĐÃ ĐƯỢC TẠO THÀNH CÔNG!
      ═══════════════════════════════════════
      
      📋 Mã đơn hàng: DH${Date.now()}
      👤 Khách hàng: ${khachHang.HoTen}
      📞 SĐT: ${khachHang.SoDienThoai}
      📧 Email: ${khachHang.Email}
      
      🚗 Xe đã chọn:
      ${chiTietDonHang.map(item => 
        `   • ${item.soLuong}x ${orderData.selectedVehicles.find(v => v.vehicle.maXe === item.maXe).vehicle.name} (${item.mauSac}) - ${formatPrice(item.thanhTien)}`
      ).join('\n')}
      
      💰 Tổng tiền: ${formatPrice(calculateTotal())}
      💳 Phương thức: ${donHang.phuongThucThanhToan}
      
      ✅ Thông tin khách hàng đã được lưu vào hệ thống
      📊 Bạn có thể xem chi tiết tại mục "Quản lý khách hàng"
      `;
      
      alert(orderSummary);
      
      // Reset form sau khi tạo thành công
      setOrderData({
        customer: {
          maKhachHang: null,
          name: '',
          phone: '',
          email: '',
          address: '',
          idCard: ''
        },
        dealer: {
          maDaiLy: 1,
          name: 'VinFast Quận 1'
        },
        selectedVehicles: [],
        vehicle: null,
        selectedColor: '',
        quantity: 1,
        accessories: [],
        promotion: null,
        financing: {
          phuongThucThanhToan: 'Trả thẳng',
          downPayment: 0,
          loanTerm: 12,
          laiSuat: 8.5,
          nganHangHoTro: ''
        },
        payment: {
          phuongThuc: 'Tiền mặt',
          ghiChu: ''
        }
      });
      setCurrentStep(1);
      
    } catch (error) {
      console.error('Lỗi khi tạo đơn hàng:', error);
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
          allVehicles={vehicles}
          selectedVehicle={orderData.vehicle}
          selectedColor={orderData.selectedColor}
          quantity={orderData.quantity}
          selectedVehicles={orderData.selectedVehicles}
          searchTerm={searchTerm}
          selectedSeries={selectedSeries}
          vehicleSeries={vehicleSeries}
          onSelect={handleVehicleSelect}
          onColorSelect={handleColorSelect}
          onQuantityChange={handleQuantityChange}
          onSearchChange={setSearchTerm}
          onSeriesChange={setSelectedSeries}
          addVehicleToCart={addVehicleToCart}
          removeVehicleFromCart={removeVehicleFromCart}
          updateVehicleInCart={updateVehicleInCart}
        />}
        {currentStep === 3 && <PromotionStep promotions={promotions} selectedPromotion={orderData.promotion} onSelect={handlePromotionSelect} orderData={orderData} />}
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

const CustomerInfoStep = ({ orderData, handleChange }) => (
  <div className="step-content">
    <h3>Thông tin khách hàng</h3>
    <div className="form-grid">
      <div className="form-group">
        <label>Họ và tên *</label>
        <input
          type="text"
          value={orderData.customer.name}
          onChange={(e) => handleChange('name', e.target.value)}
          placeholder="Nhập họ và tên"
        />
      </div>
      <div className="form-group">
        <label>Số điện thoại *</label>
        <input
          type="tel"
          value={orderData.customer.phone}
          onChange={(e) => handleChange('phone', e.target.value)}
          placeholder="Nhập số điện thoại"
        />
      </div>
      <div className="form-group">
        <label>Email *</label>
        <input
          type="email"
          value={orderData.customer.email}
          onChange={(e) => handleChange('email', e.target.value)}
          placeholder="Nhập email"
        />
      </div>
    </div>
  </div>
);

const VehicleSelectionStep = ({ 
  vehicles, 
  allVehicles, 
  selectedVehicle, 
  selectedColor,
  quantity,
  searchTerm, 
  selectedSeries, 
  vehicleSeries,
  selectedVehicles,
  onSelect, 
  onColorSelect,
  onQuantityChange,
  onSearchChange, 
  onSeriesChange,
  addVehicleToCart,
  removeVehicleFromCart,
  updateVehicleInCart
}) => {
  const [tempSelectedVehicle, setTempSelectedVehicle] = useState(null);
  const [tempColor, setTempColor] = useState('');
  const [tempQuantity, setTempQuantity] = useState(1);

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  };

  const handleAddToCart = () => {
    if (tempSelectedVehicle && tempColor && tempQuantity > 0) {
      addVehicleToCart(tempSelectedVehicle, tempColor, tempQuantity);
      setTempSelectedVehicle(null);
      setTempColor('');
      setTempQuantity(1);
    }
  };

  return (
    <div className="step-content">
      <h3>Chọn xe</h3>
      
      {/* Search and Filter */}
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
            <select
              value={selectedSeries}
              onChange={(e) => onSeriesChange(e.target.value)}
              className="series-filter"
            >
              <option value="">Tất cả dòng xe</option>
              {vehicleSeries.map(series => (
                <option key={series} value={series}>{series}</option>
              ))}
            </select>
          </div>
        </div>
        <div className="search-results">
          Tìm thấy {vehicles.length} xe phù hợp
        </div>
      </div>

      {/* Vehicle Grid */}
      <div className="vehicle-selection-grid">
        {vehicles.map(vehicle => (
          <div 
            key={vehicle.maXe} 
            className={`vehicle-option ${tempSelectedVehicle?.maXe === vehicle.maXe ? 'selected' : ''}`}
            onClick={() => {
              setTempSelectedVehicle(vehicle);
              setTempColor(vehicle.colors[0]); // Set default color
            }}
          >
            <img src={vehicle.image} alt={vehicle.name} />
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

      {/* Selected Vehicle Configuration */}
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
                      data-color={color.toLowerCase()}
                    >
                      <div className={`color-swatch color-${color.toLowerCase().replace(/ /g, '-')}`}></div>
                      <span>{color}</span>
                    </button>
                  ))}
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
              <div className="summary-item">
                <strong>Xe:</strong> {tempSelectedVehicle.name} {tempSelectedVehicle.variant}
              </div>
              <div className="summary-item">
                <strong>Màu:</strong> {tempColor}
              </div>
              <div className="summary-item">
                <strong>Số lượng:</strong> {tempQuantity} xe
              </div>
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

      {/* Shopping Cart */}
      {selectedVehicles.length > 0 && (
        <div className="shopping-cart">
          <h4>🛒 Giỏ hàng ({selectedVehicles.length} mặt hàng)</h4>
          <div className="cart-items">
            {selectedVehicles.map((item, index) => (
              <div key={index} className="cart-item">
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

const PromotionStep = ({ promotions, selectedPromotion, onSelect, orderData }) => (
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
    
    {orderData.vehicle && (
      <div className="promotion-preview">
        <h4>Tổng quan giá</h4>
        <div className="price-breakdown">
          <div className="price-line">
            <span>Giá niêm yết:</span>
            <span>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(orderData.vehicle.donGia)}</span>
          </div>
          {selectedPromotion && (
            <div className="price-line discount">
              <span>Khuyến mãi ({selectedPromotion.tenKhuyenMai}):</span>
              <span>-{selectedPromotion.loai === 'VNĐ' 
                ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedPromotion.giaTri)
                : `${selectedPromotion.giaTri}%`
              }</span>
            </div>
          )}
          <div className="price-line total">
            <span>Tổng tiền:</span>
            <span>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(
              selectedPromotion 
                ? (selectedPromotion.loai === 'VNĐ' 
                    ? orderData.vehicle.donGia - selectedPromotion.giaTri 
                    : orderData.vehicle.donGia * (1 - selectedPromotion.giaTri / 100))
                : orderData.vehicle.donGia
            )}</span>
          </div>
        </div>
      </div>
    )}
  </div>
);

const AccessoriesStep = ({ accessories, selectedAccessories, onToggle }) => (
  <div className="step-content">
    <h3>Chọn phụ kiện (tùy chọn)</h3>
    <div className="accessories-grid">
      {accessories.map(accessory => (
        <div 
          key={accessory.id} 
          className={`accessory-item ${selectedAccessories.find(acc => acc.id === accessory.id) ? 'selected' : ''}`}
          onClick={() => onToggle(accessory)}
        >
          <div className="accessory-info">
            <h4>{accessory.name}</h4>
            <div className="accessory-price">
              {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(accessory.price)}
            </div>
          </div>
          <div className="accessory-checkbox">
            {selectedAccessories.find(acc => acc.id === accessory.id) ? '✓' : ''}
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
      <div className="payment-method">
        <label>
          <input
            type="radio"
            name="paymentMethod"
            value="Trả thẳng"
            checked={orderData.financing.phuongThucThanhToan === 'Trả thẳng'}
            onChange={(e) => setOrderData(prev => ({
              ...prev,
              financing: { ...prev.financing, phuongThucThanhToan: e.target.value }
            }))}
          />
          Trả thẳng (Thanh toán toàn bộ)
        </label>
        <p>Thanh toán 100% giá trị xe ngay khi ký hợp đồng</p>
      </div>
      
      <div className="payment-method">
        <label>
          <input
            type="radio"
            name="paymentMethod"
            value="Trả góp"
            checked={orderData.financing.phuongThucThanhToan === 'Trả góp'}
            onChange={(e) => setOrderData(prev => ({
              ...prev,
              financing: { ...prev.financing, phuongThucThanhToan: e.target.value }
            }))}
          />
          Trả góp
        </label>
        <p>Trả trước một phần, phần còn lại trả theo tháng</p>
        
        {orderData.financing.phuongThucThanhToan === 'Trả góp' && (
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
                <option value="12">12 tháng</option>
                <option value="24">24 tháng</option>
                <option value="36">36 tháng</option>
                <option value="48">48 tháng</option>
                <option value="60">60 tháng</option>
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
    </div>
    
    <div className="total-summary">
      <h4>Tổng giá trị đơn hàng: {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(total)}</h4>
    </div>
  </div>
);

const OrderSummary = ({ orderData, total, formatPrice }) => {

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
          {orderData.selectedVehicles.length > 0 ? (
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
          ) : orderData.vehicle && (
            <div>
              <p><strong>{orderData.vehicle.name} {orderData.vehicle.variant}</strong></p>
              <p>Màu sắc: {orderData.selectedColor}</p>
              <p>Số lượng: {orderData.quantity} xe</p>
              <p>Đơn giá: {formatPrice(orderData.vehicle.donGia)}</p>
            </div>
          )}
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
          {(() => {
            // Tính tổng trước khuyến mãi
            let subtotal = 0;
            if (orderData.selectedVehicles.length > 0) {
              subtotal = orderData.selectedVehicles.reduce((sum, item) => sum + (item.colorPrice * item.quantity), 0);
            } else if (orderData.vehicle) {
              subtotal = orderData.vehicle.donGia * orderData.quantity;
            }
            
            return (
              <>
                <h3>Tổng cộng: {formatPrice(subtotal)}</h3>
                {orderData.promotion && (
                  <div className="total-after-promotion">
                    <h3>Tổng cộng sau khuyến mãi: {formatPrice(total)}</h3>
                  </div>
                )}
              </>
            );
          })()}
        </div>
      </div>
    </div>
  );
};

export default CreateOrderFeature;