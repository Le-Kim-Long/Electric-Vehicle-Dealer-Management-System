import React, { useState, useEffect } from 'react';
import { 
  getCarVariantDetails, 
  searchCarVariants,
  transformCarVariantData, 
  getVariantConfiguration,
  transformConfigurationData,
  getCurrentUser 
} from '../../services/carVariantApi';
import { searchCarVariantsByVariantName, searchCarVariantsByModelName } from '../../services/carVariantApi';

import './VehicleInfoFeature.css';

const VehicleInfoFeature = () => {
  const [vehicles, setVehicles] = useState([]);
  const [filteredVehicles, setFilteredVehicles] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterBrand, setFilterBrand] = useState('all');
  const [filterVersion, setFilterVersion] = useState('all');
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [selectedColor, setSelectedColor] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState('');
  const [currentUser, setCurrentUser] = useState(null);

  // ✅ Load dữ liệu khi component mount
  useEffect(() => {
    const user = getCurrentUser();
    setCurrentUser(user);
    loadVehiclesFromAPI();
  }, []); // Chỉ chạy 1 lần

  const loadVehiclesFromAPI = async () => {
    try {
      setIsLoading(true);
      setError('');
      
      console.log('🚀 Loading vehicles from API...');
      const apiData = await getCarVariantDetails();
      console.log('📦 Raw API data:', apiData);
      
      const transformedData = transformCarVariantData(apiData);
      console.log('✅ Transformed data:', transformedData);
      
      if (transformedData.length === 0) {
        setError('Không có xe nào được tìm thấy tại đại lý này.');
        setVehicles([]);
        setFilteredVehicles([]);
      } else {
        setVehicles(transformedData);
        setFilteredVehicles(transformedData); // ✅ Hiển thị TẤT CẢ xe ban đầu
        
        // Initialize selected color
        const initialColors = {};
        transformedData.forEach(vehicle => {
          initialColors[vehicle.id] = vehicle.colors[0];
        });
        setSelectedColor(initialColors);
        
        console.log('✅ Loaded', transformedData.length, 'vehicles successfully!');
      }
      
    } catch (err) {
      console.error('❌ Error loading vehicles:', err);
      setError(err.message || 'Không thể tải danh sách xe. Vui lòng thử lại.');
      setVehicles([]);
      setFilteredVehicles([]);
    } finally {
      setIsLoading(false);
    }
  };


  // Debounce search and filter logic

  useEffect(() => {
    if (searchTerm) {
      const delaySearch = setTimeout(() => {
        handleSearch(searchTerm);
      }, 500);
      return () => clearTimeout(delaySearch);
    } else if (filterVersion !== 'all') {
      // Nếu không search nhưng filter phiên bản
      handleSearchByVariantName(filterVersion);
    } else if (filterBrand !== 'all') {
      // Nếu filter dòng xe
      handleSearchByModelName(filterBrand);
    } else {
      // Không search, không filter → hiển thị tất cả
      setFilteredVehicles(vehicles);
    }
  }, [searchTerm, filterBrand, filterVersion]);


  // Search API theo modelName
  const handleSearchByModelName = async (modelName) => {
    try {
      setIsSearching(true);
      setError('');
      const searchResults = await searchCarVariantsByModelName(modelName);
      const transformedResults = transformCarVariantData(searchResults);
      setFilteredVehicles(transformedResults);
      // Initialize colors for search results
      const newColors = {};
      transformedResults.forEach(vehicle => {
        if (!selectedColor[vehicle.id]) {
          newColors[vehicle.id] = vehicle.colors[0];
        }
      });
      if (Object.keys(newColors).length > 0) {
        setSelectedColor(prev => ({ ...prev, ...newColors }));
      }
    } catch (err) {
      console.error('❌ Search by modelName error:', err);
      setError('Lỗi khi tìm kiếm theo dòng xe. Vui lòng thử lại.');
      setFilteredVehicles([]);
    } finally {
      setIsSearching(false);
    }
  };

  // Search API theo variantName
  const handleSearchByVariantName = async (variantName) => {
    try {
      setIsSearching(true);
      setError('');
      const searchResults = await searchCarVariantsByVariantName(variantName);
      const transformedResults = transformCarVariantData(searchResults);
      setFilteredVehicles(transformedResults);
      // Initialize colors for search results
      const newColors = {};
      transformedResults.forEach(vehicle => {
        if (!selectedColor[vehicle.id]) {
          newColors[vehicle.id] = vehicle.colors[0];
        }
      });
      if (Object.keys(newColors).length > 0) {
        setSelectedColor(prev => ({ ...prev, ...newColors }));
      }
    } catch (err) {
      console.error('❌ Search by variantName error:', err);
      setError('Lỗi khi tìm kiếm theo phiên bản. Vui lòng thử lại.');
      setFilteredVehicles([]);
    } finally {
      setIsSearching(false);
    }
  };

  // Search API theo từ khóa
  const handleSearch = async (query) => {
    try {
      setIsSearching(true);
      setError('');
      const searchResults = await searchCarVariants(query);
      const transformedResults = transformCarVariantData(searchResults);
      setFilteredVehicles(transformedResults);
      // Initialize colors for search results
      const newColors = {};
      transformedResults.forEach(vehicle => {
        if (!selectedColor[vehicle.id]) {
          newColors[vehicle.id] = vehicle.colors[0];
        }
      });
      if (Object.keys(newColors).length > 0) {
        setSelectedColor(prev => ({ ...prev, ...newColors }));
      }
    } catch (err) {
      console.error('❌ Search error:', err);
      setError('Lỗi khi tìm kiếm. Vui lòng thử lại.');
      setFilteredVehicles([]);
    } finally {
      setIsSearching(false);
    }
  };

  // ✅ Load configuration khi click "Chi tiết"
  const handleViewDetail = async (vehicle) => {
    console.log('🔍 Loading configuration for variant:', vehicle.id);
    setSelectedVehicle(vehicle);
    
    if (!vehicle.configLoaded) {
      try {
        const configData = await getVariantConfiguration(vehicle.id);
        if (configData) {
          const specs = transformConfigurationData(configData);
          
          const updatedVehicle = {
            ...vehicle,
            specs: specs,
            range: configData.rangeKm,
            charging: `${configData.fullChargeTime} phút (AC)`,
            power: configData.power,
            configLoaded: true
          };
          
          setSelectedVehicle(updatedVehicle);
          
          setVehicles(prevVehicles => 
            prevVehicles.map(v => v.id === vehicle.id ? updatedVehicle : v)
          );
          
          console.log('✅ Configuration loaded:', specs);
        }
      } catch (err) {
        console.error('❌ Error loading configuration:', err);
      }
    }
  };

  const getStatusBadge = (status, stock) => {
    if (status === 'out-of-stock' || stock === 0) {
      return <span className="status-badge out-of-stock">Hết hàng</span>;
    } else if (status === 'low-stock' || stock < 10) {
      return <span className="status-badge low-stock">Sắp hết ({stock} xe)</span>;
    } else {
      return <span className="status-badge available">Có sẵn ({stock} xe)</span>;
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

  const getCurrentQuantity = (vehicle) => {
    const currentColor = selectedColor[vehicle.id] || vehicle.colors[0];
    return vehicle.colorQuantities[currentColor] || 0;
  };

  // Loading state
  if (isLoading) {
    return (
      <div className="vehicle-info-feature">
        <div className="vehicle-info-header">
          <div className="vehicle-header-content">
            <div className="vehicle-header-icon">🚗</div>
            <div className="vehicle-header-text">
              <h2>Đang tải dữ liệu xe...</h2>
              <p>Vui lòng chờ trong giây lát</p>
            </div>
          </div>
        </div>
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="spinner" style={{
            border: '4px solid #f3f3f3',
            borderTop: '4px solid #667eea',
            borderRadius: '50%',
            width: '50px',
            height: '50px',
            animation: 'spin 1s linear infinite',
            margin: '0 auto'
          }}></div>
        </div>
      </div>
    );
  }

  // Error state
  if (error && vehicles.length === 0) {
    return (
      <div className="vehicle-info-feature">
        <div className="vehicle-info-header">
          <div className="vehicle-header-content">
            <div className="vehicle-header-icon">⚠️</div>
            <div className="vehicle-header-text">
              <h2>Lỗi tải dữ liệu</h2>
              <p>{error}</p>
            </div>
          </div>
        </div>
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <button 
            onClick={loadVehiclesFromAPI}
            style={{
              padding: '12px 24px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontSize: '1rem',
              cursor: 'pointer'
            }}
          >
            🔄 Thử lại
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="vehicle-info-feature">
      {/* Header Section */}
      <div className="vehicle-info-header">
        <div className="vehicle-header-content">
          <div className="vehicle-header-icon">🚗</div>
          <div className="vehicle-header-text">
            <h2>Truy vấn thông tin xe</h2>
            <p>
              Xe có sẵn tại {currentUser?.dealerName || 'đại lý'} 
              {' • '}{vehicles.length} mẫu xe
            </p>
          </div>
        </div>
      </div>

      <div className="search-filters">
        <div className="search-box" style={{ position: 'relative' }}>
          <input
            type="text"
            placeholder="🔍 Tìm kiếm xe (VD: VF3, Eco, VF5 Plus)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {isSearching && (
            <span style={{ 
              position: 'absolute', 
              right: '15px', 
              top: '50%', 
              transform: 'translateY(-50%)',
              fontSize: '0.9rem',
              color: '#667eea'
            }}>
              ⏳ Đang tìm...
            </span>
          )}
        </div>

        <div className="filters">
          <select
            value={filterBrand}
            onChange={(e) => setFilterBrand(e.target.value)}
            disabled={!!searchTerm}
            style={{ opacity: searchTerm ? 0.5 : 1 }}
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
            disabled={!!searchTerm}
            style={{ opacity: searchTerm ? 0.5 : 1 }}
          >
            <option value="all">Tất cả phiên bản</option>
            <option value="Eco">Eco</option>
            <option value="Plus">Plus</option>
          </select>
        </div>
      </div>

      {searchTerm && (
        <div style={{
          padding: '12px 20px',
          background: 'linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%)',
          borderRadius: '12px',
          marginBottom: '20px',
          color: '#667eea',
          fontWeight: '500'
        }}>
          🔍 Tìm thấy <strong>{filteredVehicles.length}</strong> xe với từ khóa "<strong>{searchTerm}</strong>"
        </div>
      )}

      <div className="vehicle-grid">
        {filteredVehicles.map(vehicle => (
          <div key={vehicle.id} className="vehicle-card">
            <div className="vehicle-image">
              <img 
                src={getCurrentImage(vehicle)} 
                alt={`${vehicle.name} - ${selectedColor[vehicle.id] || vehicle.colors[0]}`}
                onError={(e) => {
                  console.error('Image load error:', e.target.src);
                  e.target.src = vehicle.defaultImage;
                }}
              />
              {getStatusBadge(vehicle.status, getCurrentQuantity(vehicle))}
            </div>
            
            <div className="vehicle-info">
              <h3>{vehicle.name}</h3>
              {/* ✅ XÓA HOÀN TOÀN dòng brand */}
              {/* <p className="vehicle-brand">{vehicle.brand}</p> */}
              
              <div className="price-and-details">
                <div className="vehicle-price">
                  {new Intl.NumberFormat('vi-VN', { 
                    style: 'currency', 
                    currency: 'VND' 
                  }).format(getCurrentPrice(vehicle))}
                </div>
                <button 
                  className="action-btn view-details-btn"
                  onClick={() => handleViewDetail(vehicle)}
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
                      title={`Tồn kho: ${vehicle.colorQuantities[color]} xe`}
                    >
                      {color}
                    </span>
                  ))}
                </div>
              </div>
              
              {/* ✅ XÓA phần vehicle-specs */}
              <div className="vehicle-stock-info">
                <div className="spec-item">
                  <span className="spec-label">Tồn kho màu này:</span>
                  <span className="spec-value">{getCurrentQuantity(vehicle)} xe</span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {filteredVehicles.length === 0 && !isSearching && !isLoading && (
        <div className="no-results">
          <div style={{ fontSize: '3rem', marginBottom: '20px' }}>🔍</div>
          <p>Không tìm thấy xe nào {searchTerm ? `với từ khóa "${searchTerm}"` : 'phù hợp với bộ lọc'}.</p>
          {searchTerm && (
            <button 
              onClick={() => setSearchTerm('')}
              style={{
                marginTop: '20px',
                padding: '10px 20px',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: 'pointer'
              }}
            >
              ← Xem tất cả xe
            </button>
          )}
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

// VehicleDetailModal component
const VehicleDetailModal = ({ vehicle, onClose }) => {
  const [selectedModalColor, setSelectedModalColor] = useState(vehicle.colors[0]);

  const getCurrentModalImage = () => {
    return vehicle.images[selectedModalColor] || vehicle.defaultImage;
  };

  const getCurrentModalPrice = () => {
    return vehicle.colorPrices[selectedModalColor];
  };

  const getCurrentModalQuantity = () => {
    return vehicle.colorQuantities[selectedModalColor] || 0;
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
                e.target.src = vehicle.defaultImage;
              }}
            />
          </div>
          
          <div className="vehicle-detail-info">
            {!vehicle.configLoaded && (
              <div style={{
                padding: '12px',
                background: '#fff3cd',
                border: '1px solid #ffc107',
                borderRadius: '8px',
                marginBottom: '20px',
                textAlign: 'center',
                color: '#856404'
              }}>
                ⏳ Đang tải thông tin chi tiết...
              </div>
            )}

            <div className="detail-section">
              <h3>Thông tin cơ bản</h3>
              <div className="detail-grid">
                {/* ✅ XÓA dòng Hãng xe */}
                {/* <div className="detail-item">
                  <span>Hãng xe:</span>
                  <span>{vehicle.brand}</span>
                </div> */}
                <div className="detail-item">
                  <span>Khoảng giá:</span>
                  <span>{vehicle.priceRange}</span>
                </div>
                <div className="detail-item">
                  <span>Tổng tồn kho:</span>
                  <span>{vehicle.stock} xe</span>
                </div>
              </div>
            </div>

            <div className="detail-section">
              <h3>Chọn màu sắc</h3>
              <div className="color-price-grid">
                {vehicle.colors.map((color, index) => (
                  <div 
                    key={index} 
                    className={`color-price-item ${selectedModalColor === color ? 'active' : ''}`}
                    onClick={() => setSelectedModalColor(color)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div>
                      <div className="color-name">{color}</div>
                      <div style={{ fontSize: '0.8rem', color: selectedModalColor === color ? 'rgba(255,255,255,0.8)' : '#999' }}>
                        Tồn: {vehicle.colorQuantities[color]} xe
                      </div>
                    </div>
                    <div className="color-price">
                      {new Intl.NumberFormat('vi-VN', { 
                        style: 'currency', 
                        currency: 'VND' 
                      }).format(vehicle.colorPrices[color])}
                    </div>
                  </div>
                ))}
              </div>
              <div className="selected-price" style={{ 
                marginTop: '15px', 
                padding: '15px',
                background: 'linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%)',
                borderRadius: '10px',
                textAlign: 'center'
              }}>
                <strong style={{ color: '#667eea', fontSize: '1.2rem' }}>
                  Giá đã chọn ({selectedModalColor}): {' '}
                  {new Intl.NumberFormat('vi-VN', { 
                    style: 'currency', 
                    currency: 'VND' 
                  }).format(getCurrentModalPrice())}
                </strong>
                <div style={{ marginTop: '5px', fontSize: '0.9rem', color: '#666' }}>
                  Tồn kho: {getCurrentModalQuantity()} xe
                </div>
              </div>
            </div>

            {vehicle.specs && (
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
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default VehicleInfoFeature;