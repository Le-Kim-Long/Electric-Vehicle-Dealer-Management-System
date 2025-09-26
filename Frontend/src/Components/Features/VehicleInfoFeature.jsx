import React, { useState, useEffect } from 'react';
import './VehicleInfoFeature.css';
import { useTestDrive } from '../DealerStaff';

const VehicleInfoFeature = () => {
  const { addTestDriveBooking, addQuoteRequest } = useTestDrive();
  const [vehicles, setVehicles] = useState([]);
  const [filteredVehicles, setFilteredVehicles] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterBrand, setFilterBrand] = useState('all');
  const [filterVersion, setFilterVersion] = useState('all');
  const [filterPrice, setFilterPrice] = useState('all');
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [showTestDriveModal, setShowTestDriveModal] = useState(false);
  const [testDriveVehicle, setTestDriveVehicle] = useState(null);
  const [showQuoteModal, setShowQuoteModal] = useState(false);
  const [quoteVehicle, setQuoteVehicle] = useState(null);

  // Mock data for vehicles - Based on actual database XE table with colors and prices
  const mockVehicles = [
    // VF3 Eco
    {
      id: 1,
      name: 'VinFast VF3 Eco',
      brand: 'VinFast',
      type: 'Hatchback',
      price: 240000000,
      priceRange: '240 - 250 triệu',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 240000000, 'Đen': 250000000 },
      stock: 15,
      range: 210,
      charging: '240 phút (AC)',
      power: 32,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '18.64 kWh (LFP)',
        seats: 4,
        topSpeed: 100,
        acceleration: '12.0s (0-100km/h)',
        torque: '110 Nm',
        dimensions: '3190 x 1670 x 1600 mm',
        wheelbase: '2075 mm',
        weight: '1200 kg',
        trunkCapacity: '285 L'
      },
      status: 'available'
    },
    // VF3 Plus
    {
      id: 2,
      name: 'VinFast VF3 Plus',
      brand: 'VinFast',
      type: 'Hatchback',
      price: 300000000,
      priceRange: '300 - 310 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 310000000, 'Đen': 300000000, 'Đỏ': 300000000, 'Xanh dương': 310000000 },
      stock: 12,
      range: 210,
      charging: '240 phút (AC)',
      power: 35,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '18.64 kWh (LFP)',
        seats: 4,
        topSpeed: 105,
        acceleration: '11.5s (0-100km/h)',
        torque: '120 Nm',
        dimensions: '3190 x 1670 x 1600 mm',
        wheelbase: '2075 mm',
        weight: '1250 kg',
        trunkCapacity: '285 L'
      },
      status: 'available'
    },
    // VF5 Eco
    {
      id: 3,
      name: 'VinFast VF5 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 370000000,
      priceRange: '370 - 375 triệu',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 370000000, 'Đen': 375000000 },
      stock: 10,
      range: 320,
      charging: '360 phút (AC)',
      power: 70,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '37.23 kWh (NMC)',
        seats: 5,
        topSpeed: 140,
        acceleration: '10.0s (0-100km/h)',
        torque: '135 Nm',
        dimensions: '3965 x 1720 x 1580 mm',
        wheelbase: '2510 mm',
        weight: '1300 kg',
        trunkCapacity: '330 L'
      },
      status: 'available'
    },
    // VF5 Plus
    {
      id: 4,
      name: 'VinFast VF5 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 420000000,
      priceRange: '420 - 435 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 420000000, 'Đen': 425000000, 'Đỏ': 430000000, 'Xanh dương': 435000000 },
      stock: 8,
      range: 320,
      charging: '360 phút (AC)',
      power: 75,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '37.23 kWh (NMC)',
        seats: 5,
        topSpeed: 145,
        acceleration: '9.5s (0-100km/h)',
        torque: '140 Nm',
        dimensions: '3965 x 1720 x 1580 mm',
        wheelbase: '2510 mm',
        weight: '1350 kg',
        trunkCapacity: '330 L'
      },
      status: 'available'
    },
    // VF7 Eco
    {
      id: 5,
      name: 'VinFast VF7 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 650000000,
      priceRange: '650 - 655 triệu',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 650000000, 'Đen': 655000000 },
      stock: 6,
      range: 450,
      charging: '480 phút (AC)',
      power: 130,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '75.30 kWh (NMC)',
        seats: 5,
        topSpeed: 160,
        acceleration: '8.5s (0-100km/h)',
        torque: '250 Nm',
        dimensions: '4450 x 1870 x 1640 mm',
        wheelbase: '2735 mm',
        weight: '1650 kg',
        trunkCapacity: '420 L'
      },
      status: 'available'
    },
    // VF7 Plus
    {
      id: 6,
      name: 'VinFast VF7 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 720000000,
      priceRange: '720 - 740 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 720000000, 'Đen': 725000000, 'Đỏ': 730000000, 'Xanh dương': 735000000, 'Xanh rêu': 740000000 },
      stock: 4,
      range: 450,
      charging: '480 phút (AC)',
      power: 150,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '75.30 kWh (NMC)',
        seats: 5,
        topSpeed: 170,
        acceleration: '7.8s (0-100km/h)',
        torque: '280 Nm',
        dimensions: '4450 x 1870 x 1640 mm',
        wheelbase: '2735 mm',
        weight: '1700 kg',
        trunkCapacity: '420 L'
      },
      status: 'low-stock'
    },
    // VF8 Eco
    {
      id: 7,
      name: 'VinFast VF8 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 950000000,
      priceRange: '950 - 970 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ'],
      colorPrices: { 'Trắng': 950000000, 'Đen': 960000000, 'Đỏ': 970000000 },
      stock: 5,
      range: 460,
      charging: '500 phút (AC)',
      power: 150,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '82.00 kWh (NMC)',
        seats: 5,
        topSpeed: 180,
        acceleration: '8.0s (0-100km/h)',
        torque: '320 Nm',
        dimensions: '4750 x 1930 x 1660 mm',
        wheelbase: '2950 mm',
        weight: '2000 kg',
        trunkCapacity: '550 L'
      },
      status: 'available'
    },
    // VF8 Plus
    {
      id: 8,
      name: 'VinFast VF8 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 1050000000,
      priceRange: '1.05 - 1.09 tỷ',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1050000000, 'Đen': 1060000000, 'Đỏ': 1070000000, 'Xanh dương': 1080000000, 'Xanh rêu': 1090000000 },
      stock: 3,
      range: 470,
      charging: '500 phút (AC)',
      power: 220,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '87.70 kWh (NMC)',
        seats: 5,
        topSpeed: 200,
        acceleration: '6.5s (0-100km/h)',
        torque: '400 Nm',
        dimensions: '4750 x 1930 x 1660 mm',
        wheelbase: '2950 mm',
        weight: '2100 kg',
        trunkCapacity: '550 L'
      },
      status: 'low-stock'
    },
    // VF9 Eco
    {
      id: 9,
      name: 'VinFast VF9 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 1250000000,
      priceRange: '1.25 - 1.26 tỷ',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 1250000000, 'Đen': 1260000000 },
      stock: 2,
      range: 480,
      charging: '600 phút (AC)',
      power: 300,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '92.00 kWh (NMC)',
        seats: 7,
        topSpeed: 190,
        acceleration: '7.5s (0-100km/h)',
        torque: '500 Nm',
        dimensions: '5110 x 2010 x 1690 mm',
        wheelbase: '3150 mm',
        weight: '2500 kg',
        trunkCapacity: '700 L'
      },
      status: 'low-stock'
    },
    // VF9 Plus
    {
      id: 10,
      name: 'VinFast VF9 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 1350000000,
      priceRange: '1.35 - 1.39 tỷ',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1350000000, 'Đen': 1360000000, 'Đỏ': 1370000000, 'Xanh dương': 1380000000, 'Xanh rêu': 1390000000 },
      stock: 1,
      range: 480,
      charging: '600 phút (AC)',
      power: 320,
      image: 'https://via.placeholder.com/300x200',
      specs: {
        battery: '92.00 kWh (NMC)',
        seats: 7,
        topSpeed: 200,
        acceleration: '7.0s (0-100km/h)',
        torque: '520 Nm',
        dimensions: '5110 x 2010 x 1690 mm',
        wheelbase: '3150 mm',
        weight: '2600 kg',
        trunkCapacity: '700 L'
      },
      status: 'low-stock'
    }
  ];

  useEffect(() => {
    setVehicles(mockVehicles);
    setFilteredVehicles(mockVehicles);
  }, []);

  useEffect(() => {
    let filtered = vehicles.filter(vehicle =>
      vehicle.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      vehicle.brand.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (filterBrand !== 'all') {
      filtered = filtered.filter(vehicle => 
        vehicle.name.toLowerCase().includes(filterBrand.toLowerCase())
      );
    }

    if (filterVersion !== 'all') {
      filtered = filtered.filter(vehicle => 
        vehicle.name.toLowerCase().includes(filterVersion.toLowerCase())
      );
    }

    if (filterPrice !== 'all') {
      if (filterPrice === 'under-800') {
        filtered = filtered.filter(vehicle => vehicle.price < 800000000);
      } else if (filterPrice === '800-1200') {
        filtered = filtered.filter(vehicle => vehicle.price >= 800000000 && vehicle.price < 1200000000);
      } else if (filterPrice === 'over-1200') {
        filtered = filtered.filter(vehicle => vehicle.price >= 1200000000);
      }
    }

    setFilteredVehicles(filtered);
  }, [searchTerm, filterBrand, filterVersion, filterPrice, vehicles]);

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  const getStatusBadge = (status, stock) => {
    if (status === 'out-of-stock' || stock === 0) {
      return <span className="status-badge out-of-stock">Hết hàng</span>;
    } else if (status === 'low-stock' || stock < 5) {
      return <span className="status-badge low-stock">Sắp hết</span>;
    } else {
      return <span className="status-badge available">Có sẵn</span>;
    }
  };

  const handleTestDrive = (vehicle) => {
    setTestDriveVehicle(vehicle);
    setShowTestDriveModal(true);
  };

  const handleQuote = (vehicle) => {
    setQuoteVehicle(vehicle);
    setShowQuoteModal(true);
  };

  return (
    <div className="vehicle-info-feature">
      <div className="feature-header">
        <h2>Truy vấn thông tin xe</h2>
        <p>Tìm kiếm và xem thông tin chi tiết các mẫu xe điện</p>
      </div>

      <div className="search-filters">
        <div className="search-box">
          <input
            type="text"
            placeholder="Tìm kiếm theo tên xe hoặc hãng..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filters">
          <select
            value={filterBrand}
            onChange={(e) => setFilterBrand(e.target.value)}
          >
            <option value="all">Tất cả dòng xe</option>
            <option value="VF3">VF3</option>
            <option value="VF5">VF5</option>
            <option value="VF7">VF7</option>
            <option value="VF8">VF8</option>
            <option value="VF9">VF9</option>
          </select>

          <select
            value={filterVersion}
            onChange={(e) => setFilterVersion(e.target.value)}
          >
            <option value="all">Tất cả phiên bản</option>
            <option value="Eco">Eco</option>
            <option value="Plus">Plus</option>
          </select>

          <select
            value={filterPrice}
            onChange={(e) => setFilterPrice(e.target.value)}
          >
            <option value="all">Tất cả mức giá</option>
            <option value="under-800">Dưới 800 triệu</option>
            <option value="800-1200">800 triệu - 1.2 tỷ</option>
            <option value="over-1200">Trên 1.2 tỷ</option>
          </select>
        </div>
      </div>

      <div className="vehicle-grid">
        {filteredVehicles.map(vehicle => (
          <div key={vehicle.id} className="vehicle-card">
            <div className="vehicle-image">
              <img src={vehicle.image} alt={vehicle.name} />
              {getStatusBadge(vehicle.status, vehicle.stock)}
            </div>
            
            <div className="vehicle-info">
              <h3>{vehicle.name}</h3>
              <p className="vehicle-brand">{vehicle.brand} • {vehicle.type}</p>
              
              <div className="price-and-details">
                <div className="vehicle-price">{vehicle.priceRange}</div>
                <button 
                  className="action-btn view-details-btn"
                  onClick={() => setSelectedVehicle(vehicle)}
                >
                  Chi tiết
                </button>
              </div>

              <div className="vehicle-colors">
                <span className="colors-label">Màu sắc:</span>
                <div className="colors-list">
                  {vehicle.colors.map((color, index) => (
                    <span key={index} className="color-tag">{color}</span>
                  ))}
                </div>
              </div>
              
              <div className="vehicle-specs">
                <div className="spec-item">
                  <span className="spec-label">Phạm vi:</span>
                  <span className="spec-value">{vehicle.range} km</span>
                </div>
                <div className="spec-item">
                  <span className="spec-label">Công suất:</span>
                  <span className="spec-value">{vehicle.power} kW</span>
                </div>
                <div className="spec-item">
                  <span className="spec-label">Tồn kho:</span>
                  <span className="spec-value">{vehicle.stock} xe</span>
                </div>
              </div>

              <div className="bottom-actions">
                <button 
                  className="action-btn test-drive-btn"
                  onClick={() => handleTestDrive(vehicle)}
                >
                  Lái thử
                </button>
                <button 
                  className="action-btn quote-btn"
                  onClick={() => handleQuote(vehicle)}
                >
                  Báo giá
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {filteredVehicles.length === 0 && (
        <div className="no-results">
          <p>Không tìm thấy xe nào phù hợp với tiêu chí tìm kiếm.</p>
        </div>
      )}

      {selectedVehicle && (
        <VehicleDetailModal 
          vehicle={selectedVehicle} 
          onClose={() => setSelectedVehicle(null)} 
        />
      )}

      {showTestDriveModal && (
        <TestDriveModal 
          vehicle={testDriveVehicle}
          addTestDriveBooking={addTestDriveBooking}
          onClose={() => {
            setShowTestDriveModal(false);
            setTestDriveVehicle(null);
          }} 
        />
      )}

      {showQuoteModal && (
        <QuoteModal 
          vehicle={quoteVehicle}
          addQuoteRequest={addQuoteRequest}
          onClose={() => {
            setShowQuoteModal(false);
            setQuoteVehicle(null);
          }} 
        />
      )}
    </div>
  );
};

const VehicleDetailModal = ({ vehicle, onClose }) => {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{vehicle.name}</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <div className="vehicle-detail-image">
            <img src={vehicle.image} alt={vehicle.name} />
          </div>
          
          <div className="vehicle-detail-info">
            <div className="detail-section">
              <h3>Thông tin cơ bản</h3>
              <div className="detail-grid">
                <div className="detail-item">
                  <span>Hãng xe:</span>
                  <span>{vehicle.brand}</span>
                </div>
                <div className="detail-item">
                  <span>Loại xe:</span>
                  <span>{vehicle.type}</span>
                </div>
                <div className="detail-item">
                  <span>Khoảng giá:</span>
                  <span>{vehicle.priceRange}</span>
                </div>
                <div className="detail-item">
                  <span>Tồn kho:</span>
                  <span>{vehicle.stock} xe</span>
                </div>
              </div>
            </div>

            <div className="detail-section">
              <h3>Màu sắc & Giá chi tiết</h3>
              <div className="color-price-grid">
                {vehicle.colors.map((color, index) => (
                  <div key={index} className="color-price-item">
                    <span className="color-name">{color}:</span>
                    <span className="color-price">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(vehicle.colorPrices[color])}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="detail-section">
              <h3>Thông số kỹ thuật</h3>
              <div className="detail-grid">
                <div className="detail-item">
                  <span>Pin:</span>
                  <span>{vehicle.specs.battery}</span>
                </div>
                <div className="detail-item">
                  <span>Phạm vi hoạt động:</span>
                  <span>{vehicle.range} km</span>
                </div>
                <div className="detail-item">
                  <span>Thời gian sạc:</span>
                  <span>{vehicle.charging}</span>
                </div>
                <div className="detail-item">
                  <span>Công suất:</span>
                  <span>{vehicle.power} kW</span>
                </div>
                <div className="detail-item">
                  <span>Mô-men xoắn:</span>
                  <span>{vehicle.specs.torque}</span>
                </div>
                <div className="detail-item">
                  <span>Số ghế:</span>
                  <span>{vehicle.specs.seats} ghế</span>
                </div>
                <div className="detail-item">
                  <span>Tốc độ tối đa:</span>
                  <span>{vehicle.specs.topSpeed} km/h</span>
                </div>
                <div className="detail-item">
                  <span>Tăng tốc 0-100km/h:</span>
                  <span>{vehicle.specs.acceleration}</span>
                </div>
                <div className="detail-item">
                  <span>Kích thước:</span>
                  <span>{vehicle.specs.dimensions}</span>
                </div>
                <div className="detail-item">
                  <span>Chiều dài cơ sở:</span>
                  <span>{vehicle.specs.wheelbase}</span>
                </div>
                <div className="detail-item">
                  <span>Trọng lượng:</span>
                  <span>{vehicle.specs.weight}</span>
                </div>
                <div className="detail-item">
                  <span>Dung tích khoang hành lý:</span>
                  <span>{vehicle.specs.trunkCapacity}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const TestDriveModal = ({ vehicle, addTestDriveBooking, onClose }) => {
  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    
    const bookingData = {
      maLichHen: Date.now(), // Tạo ID tạm thời
      maKhachHang: null, // Chưa có trong hệ thống
      customerName: formData.get('customerName'),
      customerPhone: formData.get('customerPhone'),
      customerEmail: formData.get('customerEmail') || '',
      maXe: vehicle.id,
      vehicle: vehicle.name,
      maDaiLy: 1, // Default dealer
      dealerName: formData.get('location'),
      ngayHen: formData.get('date'),
      gioHen: formData.get('time'),
      trangThai: 'Đang chờ',
      ghiChu: formData.get('notes') || '',
      ngayTao: new Date().toISOString()
    };
    
    // Save to localStorage for FeedbackTestDriveFeature
    try {
      const existingBookings = JSON.parse(localStorage.getItem('testDriveBookings') || '[]');
      existingBookings.push(bookingData);
      localStorage.setItem('testDriveBookings', JSON.stringify(existingBookings));
      
      // Verify save was successful
      const savedData = localStorage.getItem('testDriveBookings');
      console.log('✅ Booking saved successfully:', JSON.parse(savedData));
      
      // Trigger custom event to notify other components
      window.dispatchEvent(new CustomEvent('testDriveBookingAdded', { detail: bookingData }));
    } catch (error) {
      console.error('❌ Error saving booking:', error);
      alert('Có lỗi khi lưu thông tin. Vui lòng thử lại!');
      return;
    }
    
    // Add to test drive bookings list (cho DealerStaff context)
    if (addTestDriveBooking) {
      addTestDriveBooking(bookingData);
    }
    
    alert(`Đặt lịch lái thử ${vehicle.name} thành công! 
    
✅ Thông tin đã được lưu vào hệ thống.
🔍 Bạn có thể xem chi tiết tại mục "Phản hồi và lái thử".
📱 ID booking: ${bookingData.maLichHen}`);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Đặt lịch lái thử - {vehicle.name}</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <form className="test-drive-form" onSubmit={handleSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Họ và tên khách hàng *</label>
                <input type="text" name="customerName" placeholder="Nhập họ và tên" required />
              </div>
              <div className="form-group">
                <label>Số điện thoại *</label>
                <input type="tel" name="customerPhone" placeholder="Nhập số điện thoại" required />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" name="customerEmail" placeholder="Nhập email" />
              </div>
              <div className="form-group">
                <label>Xe muốn lái thử *</label>
                <input type="text" value={vehicle.name} readOnly className="readonly-input" />
              </div>
              <div className="form-group">
                <label>Ngày *</label>
                <input type="date" name="date" required min={new Date().toISOString().split('T')[0]} />
              </div>
              <div className="form-group">
                <label>Giờ *</label>
                <select name="time" required>
                  <option value="">Chọn giờ</option>
                  <option value="09:00">09:00</option>
                  <option value="10:00">10:00</option>
                  <option value="11:00">11:00</option>
                  <option value="14:00">14:00</option>
                  <option value="15:00">15:00</option>
                  <option value="16:00">16:00</option>
                </select>
              </div>
              <div className="form-group">
                <label>Showroom *</label>
                <select name="location" required>
                  <option value="">Chọn showroom</option>
                  <option value="Showroom Quận 1">Showroom Quận 1</option>
                  <option value="Showroom Quận 3">Showroom Quận 3</option>
                  <option value="Showroom Quận 5">Showroom Quận 5</option>
                  <option value="Showroom Quận 7">Showroom Quận 7</option>
                </select>
              </div>
              <div className="form-group full-width">
                <label>Ghi chú</label>
                <textarea name="notes" placeholder="Ghi chú đặc biệt (nếu có)" rows="3"></textarea>
              </div>
            </div>
            
            <div className="form-actions">
              <button type="button" className="btn-secondary" onClick={onClose}>
                Hủy
              </button>
              <button type="submit" className="btn-primary">
                Đặt lịch lái thử
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

const QuoteModal = ({ vehicle, addQuoteRequest, onClose }) => {
  const handleSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    
    const quoteData = {
      id: `YC${Date.now()}`, // Generate unique ID
      customerName: formData.get('customerName'),
      customerPhone: formData.get('customerPhone'),
      customerEmail: formData.get('customerEmail') || '',
      vehicle: vehicle.name,
      selectedColor: formData.get('selectedColor'),
      notes: formData.get('notes') || '',
      status: 'Chờ xử lý',
      createdDate: new Date().toLocaleDateString('vi-VN'),
      maXe: vehicle.id // For database reference
    };
    
    // Save to localStorage for FeedbackTestDriveFeature
    try {
      const existingQuotes = JSON.parse(localStorage.getItem('quoteRequests') || '[]');
      existingQuotes.push(quoteData);
      localStorage.setItem('quoteRequests', JSON.stringify(existingQuotes));
      
      console.log('✅ Quote saved successfully:', quoteData);
    } catch (error) {
      console.error('❌ Error saving quote:', error);
      alert('Có lỗi khi lưu thông tin. Vui lòng thử lại!');
      return;
    }
    
    // Add to quote requests list (for context)
    if (addQuoteRequest) {
      addQuoteRequest(quoteData);
    }
    
    alert(`Yêu cầu báo giá ${vehicle.name} thành công! Thông tin đã được lưu vào hệ thống.`);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Yêu cầu báo giá - {vehicle.name}</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <form className="quote-form" onSubmit={handleSubmit}>
            <div className="form-grid">
              <div className="form-group">
                <label>Họ và tên khách hàng *</label>
                <input type="text" name="customerName" placeholder="Nhập họ và tên" required />
              </div>
              <div className="form-group">
                <label>Số điện thoại *</label>
                <input type="tel" name="customerPhone" placeholder="Nhập số điện thoại" required />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" name="customerEmail" placeholder="Nhập email" />
              </div>
              <div className="form-group">
                <label>Xe quan tâm *</label>
                <input type="text" value={vehicle.name} readOnly className="readonly-input" />
              </div>
              <div className="form-group">
                <label>Màu sắc quan tâm</label>
                <select name="selectedColor">
                  <option value="">Tất cả màu sắc</option>
                  {vehicle.colors.map((color, index) => (
                    <option key={index} value={color}>
                      {color} - {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(vehicle.colorPrices[color])}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Khoảng giá hiện tại</label>
                <input type="text" value={vehicle.priceRange} readOnly className="readonly-input" />
              </div>
              <div className="form-group full-width">
                <label>Ghi chú yêu cầu</label>
                <textarea 
                  name="notes" 
                  placeholder="VD: Muốn biết giá lăn bánh, ưu đãi hiện tại, điều kiện trả góp..."
                  rows="3"
                ></textarea>
              </div>
            </div>
            
            <div className="form-actions">
              <button type="button" className="btn-secondary" onClick={onClose}>
                Hủy
              </button>
              <button type="submit" className="btn-primary quote-btn">
                Gửi yêu cầu báo giá
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default VehicleInfoFeature;