import React, { useState, useEffect } from 'react';
import { updateDealerCarPriceAndStatus } from '../services/carVariantApi';
import {
    getCarVariantDetails,
    searchCarVariants,
    transformCarVariantData,
    getVariantConfiguration,
    transformConfigurationData,
    getCurrentUser,
    searchCarVariantsByStatus
} from '../services/carVariantApi';
import {
    searchCarVariantsByVariantName,
    searchCarVariantsByModelName,
    searchCarVariantsByModelAndVariant,
    fetchAllModelNames,
    fetchVariantNamesByModel
} from '../services/carVariantApi';
import '../ManagerFeatures/DealerCarManagement.css';


const DealerCarManagement = () => {
    // State cho modal cập nhật giá & trạng thái
    const [updateModal, setUpdateModal] = useState({ open: false, vehicle: null, color: null });
    const [updateForm, setUpdateForm] = useState({ price: '', status: '', loading: false, error: '', success: false });
    const [vehicles, setVehicles] = useState([]);
    const [filteredVehicles, setFilteredVehicles] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterBrand, setFilterBrand] = useState('all');
    const [filterVersion, setFilterVersion] = useState('all');
    const [modelOptions, setModelOptions] = useState([]);
    const [variantOptions, setVariantOptions] = useState([]);
    const [selectedVehicle, setSelectedVehicle] = useState(null);
    const [selectedColor, setSelectedColor] = useState({});
    const [isLoading, setIsLoading] = useState(true);
    const [isSearching, setIsSearching] = useState(false);
    const [error, setError] = useState('');
    const [currentUser, setCurrentUser] = useState(null);
    const [filterStatus, setFilterStatus] = useState('all');

    useEffect(() => {
        fetchAllModelNames()
            .then(models => setModelOptions(models))
            .catch(() => setModelOptions([]));
    }, []);

    useEffect(() => {
        if (filterBrand && filterBrand !== 'all') {
            fetchVariantNamesByModel(filterBrand)
                .then(variants => setVariantOptions(Array.isArray(variants) ? variants : (variants.variantNames || [])))
                .catch(() => setVariantOptions([]));
        } else {
            setVariantOptions([]);
        }
        setFilterVersion('all');
    }, [filterBrand]);

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
            const withRaw = transformedData.map((v, idx) => ({ ...v, colorPricesRaw: apiData[idx]?.colorPrices || [] }));
            if (withRaw.length === 0) {
                setVehicles([]);
                setFilteredVehicles([]);
            } else {
                setVehicles(withRaw);
                setFilteredVehicles(withRaw);
                const initialColors = {};
                withRaw.forEach(vehicle => {
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
        if (isLoading) return;
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
        } else if (filterStatus && filterStatus !== 'all') {
            handleSearchByStatus(filterStatus);
        } else {
            setFilteredVehicles(vehicles);
        }
    }, [searchTerm, filterBrand, filterVersion, filterStatus, isLoading]);
    const handleSearchByStatus = async (status) => {
        setIsSearching(true);
        setError('');
        try {
            let allVehicles = vehicles;
            if (!allVehicles || allVehicles.length === 0) {
                const apiData = await getCarVariantDetails();
                const transformedData = transformCarVariantData(apiData);
                allVehicles = transformedData.map((v, idx) => ({ ...v, colorPricesRaw: apiData[idx]?.colorPrices || [] }));
            }

            // Lọc và chỉ giữ lại các màu đúng trạng thái filter cho từng xe
            const filtered = allVehicles
                .map(vehicle => {
                    if (vehicle.colorPricesRaw && Array.isArray(vehicle.colorPricesRaw)) {
                        // Lấy danh sách màu đúng trạng thái
                        const validColors = vehicle.colorPricesRaw
                            .filter(cp => cp.status === status)
                            .map(cp => cp.colorName);
                        if (validColors.length === 0) return null;
                        // Chỉ giữ lại các màu, giá, tồn kho đúng trạng thái
                        return {
                            ...vehicle,
                            colors: validColors,
                            colorPricesRaw: vehicle.colorPricesRaw.filter(cp => validColors.includes(cp.colorName)),
                            colorQuantities: Object.fromEntries(Object.entries(vehicle.colorQuantities).filter(([color]) => validColors.includes(color))),
                            images: Object.fromEntries(Object.entries(vehicle.images).filter(([color]) => validColors.includes(color)))
                        };
                    }
                    return null;
                })
                .filter(Boolean);
            setFilteredVehicles(filtered);
            const newColors = {};
            filtered.forEach(vehicle => {
                if (!selectedColor[vehicle.id] || !vehicle.colors.includes(selectedColor[vehicle.id])) {
                    newColors[vehicle.id] = vehicle.colors[0];
                }
            });
            if (Object.keys(newColors).length > 0) {
                setSelectedColor(prev => ({ ...prev, ...newColors }));
            }
        } catch (err) {
            setError('Lỗi khi tìm kiếm theo trạng thái. Vui lòng thử lại.');
            setFilteredVehicles([]);
        } finally {
            setIsSearching(false);
        }
    };

    const handleSearchByModelName = async (modelName) => {
        setIsSearching(true);
        setError('');
        try {
            const searchResults = await searchCarVariantsByModelName(modelName);
            const transformedResults = transformCarVariantData(searchResults);
            const withRaw = transformedResults.map((v, idx) => ({ ...v, colorPricesRaw: searchResults[idx]?.colorPrices || [] }));
            setFilteredVehicles(withRaw);
            const newColors = {};
            withRaw.forEach(vehicle => {
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
            const withRaw = transformedResults.map((v, idx) => ({ ...v, colorPricesRaw: searchResults[idx]?.colorPrices || [] }));
            setFilteredVehicles(withRaw);
            const newColors = {};
            withRaw.forEach(vehicle => {
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
            const withRaw = transformedResults.map((v, idx) => ({ ...v, colorPricesRaw: searchResults[idx]?.colorPrices || [] }));
            setFilteredVehicles(withRaw);
            const newColors = {};
            withRaw.forEach(vehicle => {
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
            const withRaw = transformedResults.map((v, idx) => ({ ...v, colorPricesRaw: searchResults[idx]?.colorPrices || [] }));
            setFilteredVehicles(withRaw);
            const newColors = {};
            withRaw.forEach(vehicle => {
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
        if (vehicle.colorPricesRaw) {
            const colorObj = vehicle.colorPricesRaw.find(c => c.colorName === currentColor);
            if (colorObj && colorObj.dealerPrice != null) return colorObj.dealerPrice;
        }
        return 0;
    };

    const getCurrentQuantity = (vehicle) => {
        const currentColor = selectedColor[vehicle.id] || vehicle.colors[0];
        return vehicle.colorQuantities[currentColor] || 0;
    };

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
            <div className="vehicle-info-header">
                <div className="vehicle-header-content">
                    <div className="vehicle-header-icon">🚗</div>
                    <div className="vehicle-header-text">
                        <h2>Quản lý xe cho đại lý</h2>
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
                        {modelOptions.map(model => (
                            <option key={model} value={model}>{model}</option>
                        ))}
                    </select>

                    <select
                        value={filterVersion}
                        onChange={(e) => setFilterVersion(e.target.value)}
                        disabled={!!searchTerm || filterBrand === 'all'}
                        className={searchTerm ? 'disabled-filter' : ''}
                    >
                        <option value="all">Tất cả phiên bản</option>
                        {variantOptions.map(variant => (
                            <option key={variant} value={variant}>{variant}</option>
                        ))}
                    </select>

                    <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className={`filter-status-select${searchTerm ? ' disabled-filter' : ''}`}
                    >
                        <option value="all">Tất cả trạng thái xe</option>
                        <option value="On Sale">On Sale</option>
                        <option value="Pending">Pending</option>
                    </select>

                    <button
                        className="refresh-btn"
                        onClick={() => {
                            setSearchTerm('');
                            setFilterBrand('all');
                            setFilterVersion('all');
                            setFilterStatus('all');
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
                {filteredVehicles.map(vehicle => {
                    const currentColor = selectedColor[vehicle.id] || vehicle.colors[0];
                    // Nút Cập nhật cho từng card xe
                    return (
                        <div key={vehicle.id} className="vehicle-card">
                            <div className="vehicle-image">
                                <img
                                    src={getCurrentImage(vehicle)}
                                    alt={`${vehicle.name} - ${currentColor}`}
                                    onError={(e) => {
                                        console.error('Image load error:', e.target.src);
                                        e.target.src = vehicle.defaultImage;
                                    }}
                                />
                                {getStatusBadge(vehicle.status, getCurrentQuantity(vehicle))}
                            </div>
                            <div className="vehicle-info">
                                <h3>{vehicle.name}</h3>
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
                                                className={`color-tag clickable ${currentColor === color ? 'active' : ''}`}
                                                onClick={() => handleColorChange(vehicle.id, color)}
                                                title={`Tồn kho: ${vehicle.colorQuantities[color]} xe`}
                                            >
                                                {color}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                                <div className="vehicle-stock-info">
                                    <div className="spec-item">
                                        <span className="spec-label">Tồn kho màu này:</span>
                                        <span className="spec-value">{getCurrentQuantity(vehicle)} xe</span>
                                    </div>
                                </div>
                                <div className="update-btn-row">
                                    <button
                                        className="update-btn"
                                        onClick={() => {
                                            const colorObj = vehicle.colorPricesRaw?.find(c => c.colorName === currentColor);
                                            setUpdateForm({
                                                price: colorObj?.dealerPrice || '',
                                                status: colorObj?.status || '',
                                                loading: false,
                                                error: '',
                                                success: false
                                            });
                                            setUpdateModal({ open: true, vehicle, color: currentColor });
                                        }}
                                    >
                                        Cập nhật
                                    </button>
                                </div>
                            </div>
                        </div>
                    );
                })}
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

            {/* Modal cập nhật giá & trạng thái */}
            {updateModal.open && (
                <div className="modal-overlay" onClick={() => setUpdateModal({ open: false, vehicle: null, color: null })}>
                    <div className="modal-content update-modal-content" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Cập nhật giá & trạng thái</h2>
                            <button className="close-btn" onClick={() => setUpdateModal({ open: false, vehicle: null, color: null })}>×</button>
                        </div>
                        <div className="modal-body">
                            <div className="update-form-row">
                                <input
                                    type="number"
                                    min={0}
                                    value={updateForm.price}
                                    onChange={e => setUpdateForm(f => ({ ...f, price: e.target.value, success: false, error: '' }))}
                                    placeholder="Giá mới (VND)"
                                    className="update-form-input"
                                />
                                <select
                                    value={updateForm.status}
                                    onChange={e => setUpdateForm(f => ({ ...f, status: e.target.value, success: false, error: '' }))}
                                    className="update-form-select"
                                >
                                    <option value="">Chọn trạng thái</option>
                                    <option value="On Sale">On Sale</option>
                                    <option value="Pending">Pending</option>
                                </select>
                            </div>
                            <button
                                className="update-form-btn"
                                disabled={updateForm.loading || !updateForm.price || !updateForm.status}
                                onClick={async () => {
                                    setUpdateForm(f => ({ ...f, loading: true, error: '', success: false }));
                                    try {
                                        await updateDealerCarPriceAndStatus({
                                            modelName: updateModal.vehicle.modelName,
                                            variantName: updateModal.vehicle.variantName,
                                            colorName: updateModal.color,
                                            dealerPrice: updateForm.price,
                                            status: updateForm.status
                                        });
                                        setUpdateForm(f => ({ ...f, loading: false, error: '', success: true }));
                                        setTimeout(() => setUpdateModal({ open: false, vehicle: null, color: null }), 800);
                                        await loadVehiclesFromAPI();
                                    } catch (err) {
                                        setUpdateForm(f => ({ ...f, loading: false, error: err.message || 'Lỗi cập nhật', success: false }));
                                    }
                                }}
                            >
                                {updateForm.loading ? 'Đang cập nhật...' : 'Lưu'}
                            </button>
                            {updateForm.error && <div className="update-form-error">{updateForm.error}</div>}
                            {updateForm.success && <div className="update-form-success">Cập nhật thành công!</div>}
                        </div>
                    </div>
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


// Đã xóa component UpdatePriceStatusModal

const VehicleDetailModal = ({ vehicle, onClose }) => {
    const [selectedModalColor, setSelectedModalColor] = useState(vehicle.colors[0]);

    const getCurrentModalImage = () => {
        return vehicle.images[selectedModalColor] || vehicle.defaultImage;
    };

    const getCurrentModalPrice = () => {
        if (vehicle.colorPricesRaw) {
            const found = vehicle.colorPricesRaw.find(cp => cp.colorName === selectedModalColor);
            return found && found.dealerPrice ? found.dealerPrice : 0;
        }
        return 0;
    };

    const getCurrentModalQuantity = () => {
        return vehicle.colorQuantities[selectedModalColor] || 0;
    };

    // Lấy giá niêm yết (manufacturerPrice) cho màu đang chọn
    const getCurrentManufacturerPrice = () => {
        if (vehicle.colorPricesRaw) {
            const found = vehicle.colorPricesRaw.find(cp => cp.colorName === selectedModalColor);
            return found && found.manufacturerPrice ? found.manufacturerPrice : 0;
        }
        return 0;
    };

    useEffect(() => {
        if (!vehicle.configLoaded) {
            (async () => {
                try {
                    const configData = await getVariantConfiguration(vehicle.id);
                    if (configData) {
                        // No-op: parent already loads config on open
                    }
                } catch (err) { }
            })();
        }
    }, [vehicle]);

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
                                <div className="detail-item">
                                    <span>Giá niêm yết:</span>
                                    <span>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(getCurrentManufacturerPrice())}</span>
                                </div>
                                <div className="detail-item">
                                    <span>Tổng tồn kho:</span>
                                    <span>{vehicle.stock} xe</span>
                                </div>
                                <div className="detail-item">
                                    <span>Trạng thái xe:</span>
                                    <span>{(() => {
                                        if (vehicle.colorPricesRaw) {
                                            const found = vehicle.colorPricesRaw.find(cp => cp.colorName === selectedModalColor);
                                            return found && found.status ? found.status : 'Không rõ';
                                        }
                                        return 'Không rõ';
                                    })()}</span>
                                </div>
                            </div>
                        </div>
                        <div className="detail-section">
                            <h3>Chọn màu sắc</h3>
                            <div className="color-price-grid">
                                {vehicle.colors.map((color, index) => {
                                    let price = 0;
                                    if (vehicle.colorPricesRaw) {
                                        const found = vehicle.colorPricesRaw.find(cp => cp.colorName === color);
                                        price = found && found.dealerPrice ? found.dealerPrice : 0;
                                    }
                                    return (
                                        <div
                                            key={index}
                                            className={`color-price-item clickable ${selectedModalColor === color ? 'active' : ''}`}
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
                                                }).format(price)}
                                            </div>
                                        </div>
                                    );
                                })}
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

export default DealerCarManagement;
