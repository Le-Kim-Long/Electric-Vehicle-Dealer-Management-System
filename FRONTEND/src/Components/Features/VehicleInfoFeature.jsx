import React, { useState, useEffect } from 'react';
import { 
  getCarVariantDetails, 
  searchCarVariants,
  transformCarVariantData, 
  getVariantConfiguration,
  transformConfigurationData,
  getCurrentUser 
} from '../../services/carVariantApi';
import { searchCarVariantsByVariantName, searchCarVariantsByModelName, searchCarVariantsByModelAndVariant } from '../../services/carVariantApi';

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

  useEffect(() => {
    setCurrentUser(getCurrentUser());
    loadVehiclesFromAPI();
  }, []);

  const loadVehiclesFromAPI = async () => {
    setIsLoading(true);
    setError('');
    try {
      const apiData = await getCarVariantDetails();
      const transformedData = transformCarVariantData(apiData);
      if (transformedData.length === 0) {
        setError('Không có xe nào được tìm thấy tại đại lý này.');
        setVehicles([]);
        setFilteredVehicles([]);
      } else {
        setVehicles(transformedData);
        setFilteredVehicles(transformedData);
        // Initialize selected color
        const initialColors = {};
        transformedData.forEach(vehicle => {
          initialColors[vehicle.id] = vehicle.colors[0];
        });
        setSelectedColor(initialColors);
      }
    } catch (err) {
      setError(err.message || 'Không thể tải danh sách xe. Vui lòng thử lại.');
      setVehicles([]);
      setFilteredVehicles([]);
    } finally {
      setIsLoading(false);
    }
  };


  useEffect(() => {
    if (isLoading) return; // Không filter khi đang loading dữ liệu ban đầu
    if (searchTerm) {
      const delaySearch = setTimeout(() => {
        handleSearch(searchTerm);
      }, 500);
      return () => clearTimeout(delaySearch);
    } else if (
      filterBrand !== 'all' &&
      filterVersion !== 'all' &&
      filterBrand && filterVersion
    ) {
      handleSearchByModelAndVariant(filterBrand, filterVersion);
    } else if (filterBrand !== 'all') {
      handleSearchByModelName(filterBrand);
    } else if (filterVersion !== 'all') {
      handleSearchByVariantName(filterVersion);
    } else {
      setFilteredVehicles(vehicles);
    }
  }, [searchTerm, filterBrand, filterVersion, isLoading]);


  const handleSearchByModelName = async (modelName) => {
    setIsSearching(true);
    setError('');
    try {
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
      setError('Lỗi khi tìm kiếm theo dòng xe. Vui lòng thử lại.');
      setFilteredVehicles([]);
    } finally {
      setIsSearching(false);
    }
  };

  const handleSearchByVariantName = async (variantName) => {
    setIsSearching(true);
    setError('');
    try {
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
      setError('Lỗi khi tìm kiếm theo phiên bản. Vui lòng thử lại.');
      setFilteredVehicles([]);
    } finally {
      setIsSearching(false);
    }
  };

  const handleSearchByModelAndVariant = async (modelName, variantName) => {
    setIsSearching(true);
    setError('');
    try {
      const searchResults = await searchCarVariantsByModelAndVariant(modelName, variantName);
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
      setError('Lỗi khi tìm kiếm theo dòng xe và phiên bản. Vui lòng thử lại.');
      setFilteredVehicles([]);
    } finally {
      setIsSearching(false);
    }
  };

  const handleSearch = async (query) => {
    setIsSearching(true);
    setError('');
    try {
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
      setError('Lỗi khi tìm kiếm. Vui lòng thử lại.');
      setFilteredVehicles([]);
    } finally {
      setIsSearching(false);
    }
  };

  const handleViewDetail = async (vehicle) => {
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
        }
      } catch (err) {
        // Silent fail
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
        <div className="loading-spinner-container">
          <div className="spinner"></div>
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
        <div className="error-retry-container">
          <button 
            className="refresh-btn"
            onClick={loadVehiclesFromAPI}
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
        <div className="search-box">
          <input
            type="text"
            placeholder="🔍 Tìm kiếm xe (VD: VF3, Eco, VF5 Plus)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {isSearching && (
            <span className="searching-status">⏳ Đang tìm...</span>
          )}
        </div>

        <div className="filters">
          <select
            value={filterBrand}
            onChange={(e) => setFilterBrand(e.target.value)}
            disabled={!!searchTerm}
            className={searchTerm ? 'disabled-filter' : ''}
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
            className={searchTerm ? 'disabled-filter' : ''}
          >
            <option value="all">Tất cả phiên bản</option>
            <option value="Eco">Eco</option>
            <option value="Plus">Plus</option>
          </select>

          {/* Nút Làm mới */}
          <button
            className="refresh-btn"
            onClick={() => {
              setSearchTerm('');
              setFilterBrand('all');
              setFilterVersion('all');
              setError('');
              loadVehiclesFromAPI();
            }}
            title="Làm mới bộ lọc và dữ liệu xe"
          >
            🔄 Làm mới
          </button>
        </div>
      </div>

      {searchTerm && (
        <div className="search-result-info">
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
          <div className="no-results-icon">🔍</div>
          <p>Không tìm thấy xe nào {searchTerm ? `với từ khóa "${searchTerm}"` : 'phù hợp với bộ lọc'}.</p>
          {searchTerm && (
            <button 
              className="refresh-btn"
              onClick={() => setSearchTerm('')}
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
              <div className="modal-loading-detail">
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
                  >
                    <div>
                      <div className="color-name">{color}</div>
                      <div className={`color-qty-info ${selectedModalColor === color ? 'active' : ''}`}>
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
              <div className="selected-price">
                <strong className="selected-price-value">
                  Giá đã chọn ({selectedModalColor}): {' '}
                  {new Intl.NumberFormat('vi-VN', { 
                    style: 'currency', 
                    currency: 'VND' 
                  }).format(getCurrentModalPrice())}
                </strong>
                <div className="selected-price-qty">
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