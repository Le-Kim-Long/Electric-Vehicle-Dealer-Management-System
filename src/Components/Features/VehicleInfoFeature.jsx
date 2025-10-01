import React, { useState, useEffect } from 'react';
import './VehicleInfoFeature.css';

const VehicleInfoFeature = () => {
  const [vehicles, setVehicles] = useState([]);
  const [filteredVehicles, setFilteredVehicles] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterBrand, setFilterBrand] = useState('all');
  const [filterVersion, setFilterVersion] = useState('all');
  const [filterPrice, setFilterPrice] = useState('all');
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [selectedColor, setSelectedColor] = useState({});

  // Mock data for vehicles với hình ảnh từ public/images/
  const mockVehicles = [
    // VF3 Eco - Giữ nguyên 2 màu ban đầu
    {
      id: 1,
      name: 'VinFast VF3 Eco',
      brand: 'VinFast',
      type: 'Hatchback',
      price: 240000000,
      priceRange: '240 - 250 triệu',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 240000000, 'Đen': 250000000 },
      images: {
        'Trắng': '/images/vf3 trang.png',
        'Đen': '/images/vf3 den.png'
      },
      defaultImage: '/images/vf3 trang.png',
      stock: 15,
      range: 210,
      charging: '240 phút (AC)',
      power: 32,
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
    // VF3 Plus - 4 màu
    {
      id: 2,
      name: 'VinFast VF3 Plus',
      brand: 'VinFast',
      type: 'Hatchback',
      price: 300000000,
      priceRange: '300 - 310 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 300000000, 'Đen': 310000000, 'Đỏ': 305000000, 'Xanh dương': 310000000 },
      images: {
        'Trắng': '/images/vf3 trang.png',
        'Đen': '/images/vf3 den.png',
        'Đỏ': '/images/vf3 do.png',
        'Xanh dương': '/images/vf3 xanh duong.png'
      },
      defaultImage: '/images/vf3 trang.png',
      stock: 12,
      range: 210,
      charging: '240 phút (AC)',
      power: 35,
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
    // VF5 Eco - Giữ nguyên 2 màu ban đầu
    {
      id: 3,
      name: 'VinFast VF5 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 370000000,
      priceRange: '370 - 375 triệu',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 370000000, 'Đen': 375000000 },
      images: {
        'Trắng': '/images/vf5 trang.png',
        'Đen': '/images/vf5 den.png'
      },
      defaultImage: '/images/vf5 trang.png',
      stock: 10,
      range: 320,
      charging: '360 phút (AC)',
      power: 70,
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
    // VF5 Plus - 4 màu
    {
      id: 4,
      name: 'VinFast VF5 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 420000000,
      priceRange: '420 - 435 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương'],
      colorPrices: { 'Trắng': 420000000, 'Đen': 425000000, 'Đỏ': 430000000, 'Xanh dương': 435000000 },
      images: {
        'Trắng': '/images/vf5 trang.png',
        'Đen': '/images/vf5 den.png',
        'Đỏ': '/images/vf5 do.png',
        'Xanh dương': '/images/vf5 xanh duong.png'
      },
      defaultImage: '/images/vf5 trang.png',
      stock: 8,
      range: 320,
      charging: '360 phút (AC)',
      power: 75,
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
    // VF7 Eco - Giữ nguyên 2 màu ban đầu
    {
      id: 5,
      name: 'VinFast VF7 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 650000000,
      priceRange: '650 - 655 triệu',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 650000000, 'Đen': 655000000 },
      images: {
        'Trắng': '/images/vf7 trang.jpg',
        'Đen': '/images/vf7 den.jpg'
      },
      defaultImage: '/images/vf7 trang.jpg',
      stock: 6,
      range: 450,
      charging: '480 phút (AC)',
      power: 130,
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
    // VF7 Plus - 5 màu
    {
      id: 6,
      name: 'VinFast VF7 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 720000000,
      priceRange: '720 - 745 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 720000000, 'Đen': 725000000, 'Đỏ': 735000000, 'Xanh dương': 740000000, 'Xanh rêu': 745000000 },
      images: {
        'Trắng': '/images/vf7 trang.jpg',
        'Đen': '/images/vf7 den.jpg',
        'Đỏ': '/images/vf7 do.jpg',
        'Xanh dương': '/images/vf7 xanh duong.jpg',
        'Xanh rêu': '/images/vf7 xanh reu.jpg'
      },
      defaultImage: '/images/vf7 trang.jpg',
      stock: 4,
      range: 450,
      charging: '480 phút (AC)',
      power: 150,
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
    // VF8 Eco - Giữ nguyên 3 màu ban đầu
    {
      id: 7,
      name: 'VinFast VF8 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 950000000,
      priceRange: '950 - 970 triệu',
      colors: ['Trắng', 'Đen', 'Đỏ'],
      colorPrices: { 'Trắng': 950000000, 'Đen': 960000000, 'Đỏ': 970000000 },
      images: {
        'Trắng': '/images/vf8 trang.webp',
        'Đen': '/images/vf8 den.png',
        'Đỏ': '/images/vf8 do.jpg'
      },
      defaultImage: '/images/vf8 trang.webp',
      stock: 5,
      range: 460,
      charging: '500 phút (AC)',
      power: 150,
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
    // VF8 Plus - 5 màu
    {
      id: 8,
      name: 'VinFast VF8 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 1050000000,
      priceRange: '1.05 - 1.08 tỷ',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1050000000, 'Đen': 1060000000, 'Đỏ': 1070000000, 'Xanh dương': 1075000000, 'Xanh rêu': 1080000000 },
      images: {
        'Trắng': '/images/vf8 trang.webp',
        'Đen': '/images/vf8 den.png',
        'Đỏ': '/images/vf8 do.jpg',
        'Xanh dương': '/images/vf8 xanh duong.png',
        'Xanh rêu': '/images/vf8 xanh reu.webp'
      },
      defaultImage: '/images/vf8 trang.webp',
      stock: 3,
      range: 470,
      charging: '500 phút (AC)',
      power: 220,
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
    // VF9 Eco - Giữ nguyên 2 màu ban đầu
    {
      id: 9,
      name: 'VinFast VF9 Eco',
      brand: 'VinFast',
      type: 'SUV',
      price: 1250000000,
      priceRange: '1.25 - 1.26 tỷ',
      colors: ['Trắng', 'Đen'],
      colorPrices: { 'Trắng': 1250000000, 'Đen': 1260000000 },
      images: {
        'Trắng': '/images/vf9 trang.jpg',
        'Đen': '/images/vf9 den.png'
      },
      defaultImage: '/images/vf9 trang.jpg',
      stock: 2,
      range: 480,
      charging: '600 phút (AC)',
      power: 300,
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
    // VF9 Plus - 5 màu
    {
      id: 10,
      name: 'VinFast VF9 Plus',
      brand: 'VinFast',
      type: 'SUV',
      price: 1350000000,
      priceRange: '1.35 - 1.39 tỷ',
      colors: ['Trắng', 'Đen', 'Đỏ', 'Xanh dương', 'Xanh rêu'],
      colorPrices: { 'Trắng': 1350000000, 'Đen': 1360000000, 'Đỏ': 1370000000, 'Xanh dương': 1380000000, 'Xanh rêu': 1390000000 },
      images: {
        'Trắng': '/images/vf9 trang.jpg',
        'Đen': '/images/vf9 den.png',
        'Đỏ': '/images/vf9 do.png',
        'Xanh dương': '/images/vf9 xanh duong.png',
        'Xanh rêu': '/images/vf9 xanh reu.png'
      },
      defaultImage: '/images/vf9 trang.jpg',
      stock: 1,
      range: 480,
      charging: '600 phút (AC)',
      power: 320,
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
    
    // Initialize selected color for each vehicle (default to first color)
    const initialColors = {};
    mockVehicles.forEach(vehicle => {
      initialColors[vehicle.id] = vehicle.colors[0];
    });
    setSelectedColor(initialColors);
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

  const getStatusBadge = (status, stock) => {
    if (status === 'out-of-stock' || stock === 0) {
      return <span className="status-badge out-of-stock">Hết hàng</span>;
    } else if (status === 'low-stock' || stock < 5) {
      return <span className="status-badge low-stock">Sắp hết</span>;
    } else {
      return <span className="status-badge available">Có sẵn</span>;
    }
  };

  const handleColorChange = (vehicleId, color) => {
    setSelectedColor(prev => ({
      ...prev,
      [vehicleId]: color
    }));
  };

  const getCurrentImage = (vehicle) => {
    const currentColor = selectedColor[vehicle.id] || vehicle.colors[0];
    return vehicle.images[currentColor] || vehicle.defaultImage;
  };

  const getCurrentPrice = (vehicle) => {
    const currentColor = selectedColor[vehicle.id] || vehicle.colors[0];
    return vehicle.colorPrices[currentColor];
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
              <img 
                src={getCurrentImage(vehicle)} 
                alt={`${vehicle.name} - ${selectedColor[vehicle.id] || vehicle.colors[0]}`}
                onError={(e) => {
                  // Fallback to placeholder if image fails to load
                  e.target.src = 'https://via.placeholder.com/300x200?text=VinFast+' + vehicle.name.split(' ')[1];
                }}
              />
              {getStatusBadge(vehicle.status, vehicle.stock)}
            </div>
            
            <div className="vehicle-info">
              <h3>{vehicle.name}</h3>
              <p className="vehicle-brand">{vehicle.brand} • {vehicle.type}</p>
              
              <div className="price-and-details">
                <div className="vehicle-price">
                  {new Intl.NumberFormat('vi-VN', { 
                    style: 'currency', 
                    currency: 'VND' 
                  }).format(getCurrentPrice(vehicle))}
                </div>
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
                    <span 
                      key={index} 
                      className={`color-tag ${selectedColor[vehicle.id] === color ? 'active' : ''}`}
                      onClick={() => handleColorChange(vehicle.id, color)}
                      style={{ cursor: 'pointer' }}
                    >
                      {color}
                    </span>
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
    </div>
  );
};

const VehicleDetailModal = ({ vehicle, onClose }) => {
  const [selectedModalColor, setSelectedModalColor] = useState(vehicle.colors[0]);

  const getCurrentModalImage = () => {
    return vehicle.images[selectedModalColor] || vehicle.defaultImage;
  };

  const getCurrentModalPrice = () => {
    return vehicle.colorPrices[selectedModalColor];
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{vehicle.name}</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <div className="vehicle-detail-image">
            <img 
              src={getCurrentModalImage()} 
              alt={`${vehicle.name} - ${selectedModalColor}`}
              onError={(e) => {
                e.target.src = 'https://via.placeholder.com/400x300?text=VinFast+' + vehicle.name.split(' ')[1];
              }}
            />
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
              <h3>Chọn màu sắc</h3>
              <div className="color-selector">
                {vehicle.colors.map((color, index) => (
                  <div 
                    key={index} 
                    className={`color-option ${selectedModalColor === color ? 'selected' : ''}`}
                    onClick={() => setSelectedModalColor(color)}
                  >
                    <span className="color-name">{color}</span>
                    <span className="color-price">
                      {new Intl.NumberFormat('vi-VN', { 
                        style: 'currency', 
                        currency: 'VND' 
                      }).format(vehicle.colorPrices[color])}
                    </span>
                  </div>
                ))}
              </div>
              <div className="selected-price">
                <strong>Giá đã chọn: {new Intl.NumberFormat('vi-VN', { 
                  style: 'currency', 
                  currency: 'VND' 
                }).format(getCurrentModalPrice())}</strong>
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

export default VehicleInfoFeature;