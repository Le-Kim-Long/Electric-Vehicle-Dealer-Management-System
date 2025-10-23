import React, { useEffect, useState } from 'react';
import { addCarToDealer,getVariantConfiguration, transformConfigurationData,getCarVariantDetails, transformCarVariantData, searchCarVariantsByModelAndVariant, getCarVariantsByDealerName, fetchDealerNames, addCompleteCar, fetchAllModelNames, fetchSegmentByModelName, fetchDescriptionByModelAndVariant, fetchConfigurationByModelAndVariant, fetchVariantNamesByModel } from '../../services/carVariantApi';
import './CarManagement.css';
// Modal hiển thị chi tiết xe
const VehicleDetailModal = ({ vehicle, selectedColor, onColorChange, loading, onClose }) => {
	if (!vehicle) return null;
	// Close modal when clicking outside modal-content
	const handleOverlayClick = (e) => {
		if (e.target.classList.contains('modal-overlay')) {
			onClose();
		}
	};
	return (
		<div className="modal-overlay" onClick={handleOverlayClick}>
			<div className="modal-content" onClick={e => e.stopPropagation()}>
				<div className="modal-header">
					<h2>{vehicle.name || vehicle.modelName}</h2>
					<button className="close-btn" onClick={onClose}>×</button>
				</div>
				<div className="modal-body">
					<div className="vehicle-detail-image">
						<img
							src={vehicle.images && vehicle.images[selectedColor] ? vehicle.images[selectedColor] : vehicle.defaultImage}
							alt={`${vehicle.name || vehicle.modelName} - ${selectedColor}`}
							onError={e => { e.target.src = vehicle.defaultImage; }}
						/>
					</div>
					{loading ? (
						<div className="vehicle-detail-loading">⏳ Đang tải thông tin chi tiết...</div>
					) : (
						<>
							<div className="detail-section">
								<h3>Thông tin cơ bản</h3>
								<div className="detail-grid">
									<div className="detail-item">
										<span>Phiên bản:</span>
										<span>{vehicle.variantName || (vehicle.variant && vehicle.variant.variantName)}</span>
									</div>
									<div className="detail-item">
										<span>Giá:</span>
										<span>{vehicle.price ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(vehicle.price) : ''}</span>
									</div>
								</div>
							</div>
							{vehicle.colors && (
								<div className="detail-section">
									<h3>Chọn màu</h3>
									<div className="colors-list">
										{vehicle.colors.map((color, idx) => (
											<span
												key={color}
												className={`color-tag${selectedColor === color ? ' active' : ''}`}
												onClick={() => onColorChange(color)}
												title={`Tồn kho: ${vehicle.colorQuantities ? vehicle.colorQuantities[color] : ''} xe`}
											>{color}</span>
										))}
									</div>
								</div>
							)}
							{vehicle.specs && (
								<div className="detail-section">
									<h3>Thông số kỹ thuật</h3>
									<div className="detail-grid">
										{[
											{ key: 'battery', label: 'Pin', alt: ['batteryCapacity'] },
											{ key: 'range', label: 'Phạm vi hoạt động', alt: ['rangeKm'] },
											{ key: 'charging', label: 'Thời gian sạc', alt: ['fullChargeTime'] },
											{ key: 'power', label: 'Công suất' },
											{ key: 'torque', label: 'Mô-men xoắn' },
											{ key: 'seats', label: 'Số ghế' },
											{ key: 'dimensions', label: 'Kích thước' },
											{ key: 'weight', label: 'Trọng lượng', alt: ['weightKg'] },
											{ key: 'wheelbase', label: 'Chiều dài cơ sở', alt: ['wheelbaseMm'] },
											{ key: 'batteryType', label: 'Loại pin' }
										].map(field => {
											// Tìm giá trị theo key hoặc alt
											let value = vehicle.specs[field.key];
											if (!value && field.alt) {
												for (const altKey of field.alt) {
													if (vehicle.specs[altKey]) {
														value = vehicle.specs[altKey];
														break;
													}
												}
											}
											return (
												<div key={field.key} className="detail-item">
													<span>{field.label}:</span>
													<span>{value || ''}</span>
												</div>
											);
										})}
									</div>
								</div>
							)}
							{vehicle.range && (
								<div className="detail-section">
									<div className="detail-item">
										<span>Quãng đường:</span>
										<span>{vehicle.range} km</span>
									</div>
								</div>
							)}
							{vehicle.charging && (
								<div className="detail-section">
									<div className="detail-item">
										<span>Thời gian sạc:</span>
										<span>{vehicle.charging}</span>
									</div>
								</div>
							)}
							{vehicle.power && (
								<div className="detail-section">
									<div className="detail-item">
										<span>Công suất:</span>
										<span>{vehicle.power}</span>
									</div>
								</div>
							)}
						</>
					)}
				</div>
			</div>
		</div>
	);
};

const CarManagement = () => {
	// 1. State khai báo
	const [showAddCarModal, setShowAddCarModal] = useState(false);
	const [addCarFormData, setAddCarFormData] = useState({ variantId: '', colorName: '', dealerName: '', quantity: 1 });
	const [addCarVehicle, setAddCarVehicle] = useState(null);
	const [addCarLoading, setAddCarLoading] = useState(false);
	const [addCarMessage, setAddCarMessage] = useState('');
	const [selectedVehicle, setSelectedVehicle] = useState(null);
	const [vehicleDetail, setVehicleDetail] = useState(null);
	const [vehicleDetailLoading, setVehicleDetailLoading] = useState(false);
	const [dealerNames, setDealerNames] = useState([]);
	const [selectedDealer, setSelectedDealer] = useState("");
	const [vehicles, setVehicles] = useState([]);
	const [selectedColor, setSelectedColor] = useState({});
	const [isLoading, setIsLoading] = useState(true);
	const [error, setError] = useState("");
	const [searchTerm, setSearchTerm] = useState("");
	const [searchModel, setSearchModel] = useState("all");
	const [searchVariant, setSearchVariant] = useState("all");
	const [allModels, setAllModels] = useState([]);
	const [allVariants, setAllVariants] = useState([]);
	const [searching, setSearching] = useState(false);
	const [showCreateForm, setShowCreateForm] = useState(false);
	const [createCarData, setCreateCarData] = useState({
		model: { modelName: "", segment: "" },
		variant: { variantName: "", description: "" },
		configuration: {
			batteryCapacity: "", batteryType: "", fullChargeTime: "", rangeKm: "", power: "", torque: "", lengthMm: "", widthMm: "", heightMm: "", wheelbaseMm: "", weightKg: "", trunkVolumeL: "", seats: ""
		},
		color: "",
		car: { productionYear: "", price: "", status: "", imagePath: "" }
	});
	const [createCarLoading, setCreateCarLoading] = useState(false);
	const [createCarError, setCreateCarError] = useState("");
	const [createCarSuccess, setCreateCarSuccess] = useState("");
	const [modelOptions, setModelOptions] = useState([]);
	const [variantOptions, setVariantOptions] = useState([]);
	const [isCustomModel, setIsCustomModel] = useState(false);
	const [customModelName, setCustomModelName] = useState("");
	const [isCustomVariant, setIsCustomVariant] = useState(false);
	const [customVariantName, setCustomVariantName] = useState("");

	// 2. Các hàm xử lý logic
	const handleViewDetail = async (vehicle) => {
		setSelectedVehicle(vehicle);
		setVehicleDetailLoading(true);
		try {
			const configData = await getVariantConfiguration(vehicle.id);
			if (configData) {
				setVehicleDetail({
					...vehicle,
					specs: transformConfigurationData(configData),
					range: configData.rangeKm,
					charging: `${configData.fullChargeTime} phút (AC)`,
					power: configData.power
				});
			} else {
				setVehicleDetail(vehicle);
			}
		} catch {
			setVehicleDetail(vehicle);
		} finally {
			setVehicleDetailLoading(false);
		}
	};

	const handleModelChange = async (modelName) => {
		if (modelName === "__custom__") {
			setIsCustomModel(true);
			setCustomModelName("");
			setCreateCarData(d => ({ ...d, model: { ...d.model, modelName: "", segment: "" }, variant: { ...d.variant, variantName: "", description: "" } }));
			setVariantOptions([]);
			return;
		} else {
			setIsCustomModel(false);
			setCustomModelName("");
		}
		setCreateCarData(d => ({ ...d, model: { ...d.model, modelName }, variant: { ...d.variant, variantName: "", description: "" } }));
		setVariantOptions([]);
		if (modelName) {
			try {
				const segmentRes = await fetchSegmentByModelName(modelName);
				setCreateCarData(d => ({ ...d, model: { ...d.model, segment: segmentRes || "" } }));
			} catch (err) {
				setCreateCarData(d => ({ ...d, model: { ...d.model, segment: "" } }));
			}
			try {
				const variantRes = await fetchVariantNamesByModel(modelName);
				setVariantOptions(Array.isArray(variantRes) ? variantRes : (variantRes.variantNames || []));
			} catch {
				setVariantOptions([]);
			}
		} else {
			setCreateCarData(d => ({ ...d, model: { ...d.model, segment: "" } }));
			setVariantOptions([]);
		}
	};

	const handleVariantChange = async (variantName) => {
		if (variantName === "__custom__") {
			setIsCustomVariant(true);
			setCustomVariantName("");
			setCreateCarData(d => ({ ...d, variant: { ...d.variant, variantName: "", description: "" } }));
			return;
		} else {
			setIsCustomVariant(false);
			setCustomVariantName("");
		}
		setCreateCarData(d => ({ ...d, variant: { ...d.variant, variantName } }));
		const modelName = isCustomModel ? customModelName : createCarData.model.modelName;
		if (modelName && variantName) {
			try {
				const descRes = await fetchDescriptionByModelAndVariant(modelName, variantName);
				const description = typeof descRes === 'string' ? descRes : (descRes.description || "");
				setCreateCarData(d => ({ ...d, variant: { ...d.variant, description } }));
			} catch {
				setCreateCarData(d => ({ ...d, variant: { ...d.variant, description: "" } }));
			}
			try {
				const configRes = await fetchConfigurationByModelAndVariant(modelName, variantName);
				setCreateCarData(d => ({ ...d, configuration: {
					batteryCapacity: configRes.batteryCapacity || "",
					batteryType: configRes.batteryType || "",
					fullChargeTime: configRes.fullChargeTime || "",
					rangeKm: configRes.rangeKm || "",
					power: configRes.power || "",
					torque: configRes.torque || "",
					lengthMm: configRes.lengthMm || "",
					widthMm: configRes.widthMm || "",
					heightMm: configRes.heightMm || "",
					wheelbaseMm: configRes.wheelbaseMm || "",
					weightKg: configRes.weightKg || "",
					trunkVolumeL: configRes.trunkVolumeL || "",
					seats: configRes.seats || ""
				}}));
			} catch {
				setCreateCarData(d => ({ ...d, configuration: {
					batteryCapacity: "", batteryType: "", fullChargeTime: "", rangeKm: "", power: "", torque: "", lengthMm: "", widthMm: "", heightMm: "", wheelbaseMm: "", weightKg: "", trunkVolumeL: "", seats: ""
				}}));
			}
		}
	};

	const loadVehicles = async (opts = {}) => {
		try {
			setIsLoading(true);
			setError('');
			let apiData;
			if (selectedDealer) {
				apiData = await getCarVariantsByDealerName(selectedDealer);
			} else {
				apiData = await getCarVariantDetails();
			}
			const transformed = transformCarVariantData(apiData);
			setVehicles(transformed);
			const initialColors = {};
			transformed.forEach(v => {
				initialColors[v.id] = v.colors[0];
			});
			setSelectedColor(initialColors);
		} catch (err) {
			setError('Không thể tải danh sách xe. Vui lòng thử lại.');
			setVehicles([]);
		} finally {
			setIsLoading(false);
		}
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
	const handleColorChange = (vehicleId, color) => {
		setSelectedColor(prev => ({ ...prev, [vehicleId]: color }));
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

	// 3. useEffect cho dữ liệu
	useEffect(() => {
		loadVehicles();
		fetchDealerNames().then(names => setDealerNames(names)).catch(() => setDealerNames([]));
		fetchAllModelNames().then(models => setModelOptions(models)).catch(() => setModelOptions([]));
	}, [])

	useEffect(() => {
		const models = Array.from(new Set(vehicles.map(v => v.modelName))).filter(Boolean);
		setAllModels(models);
		const variants = Array.from(new Set(vehicles.map(v => v.variantName))).filter(Boolean);
		setAllVariants(variants);
	}, [vehicles]);

	// 4. Render UI
	if (isLoading) {
		return <div className="car-loading">Đang tải dữ liệu xe...</div>;
	}
	if (error) {
		return <div className="car-error">{error}</div>;
	}

	const filteredVehicles = vehicles.filter(vehicle => {
		let match = true;
		if (searchTerm && searchTerm.trim() !== "") {
			const term = searchTerm.toLowerCase();
			match = (
				vehicle.name.toLowerCase().includes(term) ||
				(vehicle.code && vehicle.code.toLowerCase().includes(term)) ||
				(vehicle.modelName && vehicle.modelName.toLowerCase().includes(term)) ||
				(vehicle.variantName && vehicle.variantName.toLowerCase().includes(term))
			);
		}
		if (searchModel !== "all" && vehicle.modelName !== searchModel) {
			match = false;
		}
		if (searchVariant !== "all" && vehicle.variantName !== searchVariant) {
			match = false;
		}
		return match;
	});

	return (
			<div className="car-management">
				<div className="car-management-container">
					<h2 className="car-management-title">Quản lý xe</h2>
					<div className="search-create-row">
						<div className="search-form-container">
							<form className="search-form" onSubmit={e => e.preventDefault()}>
								<input
									type="text"
									placeholder="🔍 Tìm kiếm xe (VD: VF3, Eco, VF5 Plus)..."
									value={searchTerm}
									onChange={e => setSearchTerm(e.target.value)}
									className="car-search-input search-main-input"
								/>
								<select
									className="car-search-input car-search-dealer"
									value={selectedDealer}
									onChange={async e => {
										setSelectedDealer(e.target.value);
										setSearchTerm("");
										setSearchModel("all");
										setSearchVariant("all");
										setIsLoading(true);
										try {
											let apiData = e.target.value ? await getCarVariantsByDealerName(e.target.value) : await getCarVariantDetails();
											const transformed = transformCarVariantData(apiData);
											setVehicles(transformed);
											const initialColors = {};
											transformed.forEach(v => {
												initialColors[v.id] = v.colors[0];
											});
											setSelectedColor(initialColors);
										} catch {
											setVehicles([]);
										} finally {
											setIsLoading(false);
										}
									}}
								>
									<option value="">Chọn đại lý để xem xe</option>
									{dealerNames.map(name => (
										<option key={name} value={name}>{name}</option>
									))}
								</select>
							<div className="search-model-variant-row">
								<select
									className="car-search-input car-search-model"
									value={searchModel}
									onChange={e => setSearchModel(e.target.value)}
								>
									<option value="all">Tất cả dòng xe</option>
									{allModels.map(model => (
										<option key={model} value={model}>{model}</option>
									))}
								</select>
								<select
									className="car-search-input car-search-variant"
									value={searchVariant}
									onChange={e => setSearchVariant(e.target.value)}
								>
									<option value="all">Tất cả phiên bản</option>
									{allVariants.map(variant => (
										<option key={variant} value={variant}>{variant}</option>
									))}
								</select>
							</div>
								<div className="search-action-group">
									<button type="button" className="reset-search-btn" onClick={() => {
										setSearchTerm("");
										setSearchModel("all");
										setSearchVariant("all");
										setSelectedDealer("");
										setIsLoading(true);
										loadVehicles();
									}}>Làm mới</button>
								</div>
							</form>
						</div>
					</div>
				</div>
				<div className="create-car-btn-row">
					<button className="create-car-btn" onClick={() => {
						setShowCreateForm(true);
						setCreateCarError("");
						setCreateCarSuccess("");
					}}>
						Tạo xe mới
					</button>
				</div>
				{/* Hiển thị danh sách xe */}
				<div className="vehicle-grid">
					{filteredVehicles.map(vehicle => (
						<div key={vehicle.id} className="vehicle-card">
							<div className="vehicle-image">
								<img 
									src={getCurrentImage(vehicle)} 
									alt={`${vehicle.name} - ${selectedColor[vehicle.id] || vehicle.colors[0]}`}
									onError={e => { e.target.src = vehicle.defaultImage; }}
								/>
								{selectedDealer && getStatusBadge(vehicle.status, getCurrentQuantity(vehicle))}
							</div>
							<div className="vehicle-info">
								<h3>{vehicle.name}</h3>
								<div className="price-and-details">
									<div className="vehicle-price">
										{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(getCurrentPrice(vehicle))}
									</div>
									<div className="card-action-btns">
										{!selectedDealer && (
											<div className="add-car-btn-wrapper">
												<button
													className="action-btn add-car-btn"
													onClick={() => {
														setAddCarFormData({
															variantId: vehicle.id,
															colorName: '', // Để mặc định là 'Chọn màu xe'
															dealerName: '',
															quantity: 1
														});
														setAddCarMessage('');
														setAddCarVehicle(vehicle); // Chỉ dùng cho modal Thêm xe
														setShowAddCarModal(true);
													}}
												>
													Thêm xe
												</button>
											</div>
										)}
										<div className="view-details-btn-wrapper">
											<button
												className="action-btn view-details-btn"
												onClick={() => handleViewDetail(vehicle)}
											>
												Chi tiết
											</button>
										</div>
									</div>
								</div>
								<div className="vehicle-colors">
									<span className="colors-label">Màu sắc:</span>
									<div className="colors-list">
										{vehicle.colors.map((color, idx) => (
											<span
												key={idx}
												className={`color-tag ${selectedColor[vehicle.id] === color ? 'active' : ''}`}
												onClick={() => handleColorChange(vehicle.id, color)}
												style={{ cursor: 'pointer' }}
												title={selectedDealer ? `Tồn kho: ${vehicle.colorQuantities[color]} xe` : undefined}
											>
												{color}
											</span>
										))}
									</div>
								</div>
								{selectedDealer && (
									<div className="vehicle-stock-info">
										<div className="spec-item">
											<span className="spec-label">Tồn kho màu này:</span>
											<span className="spec-value">{getCurrentQuantity(vehicle)} xe</span>
										</div>
									</div>
								)}
							</div>
						</div>
					))}
					{/* Modal chi tiết xe - render outside the map, only when selectedVehicle is set */}
					{selectedVehicle && (
						<VehicleDetailModal
							vehicle={vehicleDetail || selectedVehicle}
							selectedColor={selectedColor[selectedVehicle.id] || selectedVehicle.colors[0]}
							onColorChange={color => handleColorChange(selectedVehicle.id, color)}
							loading={vehicleDetailLoading}
							onClose={() => { setSelectedVehicle(null); setVehicleDetail(null); }}
						/>
					)}
					{/* Modal thêm xe vào đại lý */}
					{showAddCarModal && (
						<div className="user-modal-overlay">
							<div className="create-user-modal">
								<div className="create-user-modal-header">
									<h3>Thêm xe vào đại lý</h3>
									<button className="create-user-modal-close" onClick={() => setShowAddCarModal(false)}>&times;</button>
								</div>
								<form className="create-user-form" onSubmit={async e => {
									e.preventDefault();
									setAddCarLoading(true);
									setAddCarMessage('');
									try {
										// Gọi API thêm xe vào đại lý
										await addCarToDealer({
											variantId: addCarFormData.variantId,
											colorName: addCarFormData.colorName,
											dealerName: addCarFormData.dealerName,
											quantity: addCarFormData.quantity
										});
										setAddCarMessage('Thêm xe thành công!');
										// Không đóng modal, chỉ cập nhật lại danh sách xe
										loadVehicles();
									} catch (err) {
										setAddCarMessage('Thêm xe thất bại. Vui lòng thử lại.');
									} finally {
										setAddCarLoading(false);
									}
								}}>
									<div className="form-section">
										<h4 className="form-section-title">Thông tin xe cần thêm</h4>
										<div className="form-row">
														<div className="form-group">
															<label htmlFor="variantId-input">Mã phiên bản (variantId)</label>
															<input id="variantId-input" type="number" placeholder="Nhập mã phiên bản" required value={addCarFormData.variantId} onChange={e => setAddCarFormData(f => ({ ...f, variantId: Number(e.target.value) }))} />
														</div>
														<div className="form-group">
															<label htmlFor="colorName-select">Màu xe</label>
															<select id="colorName-select" required value={addCarFormData.colorName} onChange={e => setAddCarFormData(f => ({ ...f, colorName: e.target.value }))}>
																<option value="">Chọn màu xe</option>
																{addCarVehicle && addCarVehicle.colors && addCarVehicle.colors.map(color => (
																	<option key={color} value={color}>{color}</option>
																))}
															</select>
														</div>
														<div className="form-group">
															<label htmlFor="dealerName-select">Đại lý</label>
															<select id="dealerName-select" required value={addCarFormData.dealerName} onChange={e => setAddCarFormData(f => ({ ...f, dealerName: e.target.value }))}>
																<option value="">Chọn đại lý</option>
																{dealerNames.map(name => (
																	<option key={name} value={name}>{name}</option>
																))}
															</select>
														</div>
														<div className="form-group">
															<label htmlFor="quantity-input">Số lượng (Quantity)</label>
															<input id="quantity-input" type="number" placeholder="Nhập số lượng" required min={1} value={addCarFormData.quantity} onChange={e => setAddCarFormData(f => ({ ...f, quantity: Number(e.target.value) }))} />
														</div>
										</div>
									</div>
									{addCarMessage && <div className="error-message">{addCarMessage}</div>}
									<button className="create-user-submit-btn" type="submit" disabled={addCarLoading}>
										{addCarLoading ? "Đang thêm..." : "Thêm xe"}
									</button>
								</form>
							</div>
						</div>
					)}
				</div>
				{/* Modal tạo xe mới */}
				{showCreateForm && (
					<div className="user-modal-overlay">
						<div className="create-user-modal">
											<div className="create-user-modal-header">
												<h3>Tạo xe mới</h3>
												<button className="create-user-modal-close" onClick={() => setShowCreateForm(false)}>&times;</button>
											</div>
											<form className="create-user-form" onSubmit={async e => {
												e.preventDefault();
												setCreateCarLoading(true);
												setCreateCarError("");
												setCreateCarSuccess("");
												try {
													// Chuyển đổi kiểu dữ liệu cho các trường số
													const carData = {
														model: {
															modelName: createCarData.model.modelName,
															segment: createCarData.model.segment
														},
														variant: {
															variantName: createCarData.variant.variantName,
															description: createCarData.variant.description
														},
														configuration: {
															batteryCapacity: Number(createCarData.configuration.batteryCapacity),
															batteryType: createCarData.configuration.batteryType,
															fullChargeTime: Number(createCarData.configuration.fullChargeTime),
															rangeKm: Number(createCarData.configuration.rangeKm),
															power: Number(createCarData.configuration.power),
															torque: Number(createCarData.configuration.torque),
															lengthMm: Number(createCarData.configuration.lengthMm),
															widthMm: Number(createCarData.configuration.widthMm),
															heightMm: Number(createCarData.configuration.heightMm),
															wheelbaseMm: Number(createCarData.configuration.wheelbaseMm),
															weightKg: Number(createCarData.configuration.weightKg),
															trunkVolumeL: Number(createCarData.configuration.trunkVolumeL),
															seats: Number(createCarData.configuration.seats)
														},
														color: createCarData.color,
														car: {
															productionYear: Number(createCarData.car.productionYear),
															price: Number(createCarData.car.price),
															status: createCarData.car.status,
															imagePath: createCarData.car.imagePath
														}
													};
													await addCompleteCar(carData);
													setCreateCarSuccess("Tạo xe mới thành công!");
													setCreateCarData({
														model: { modelName: "", segment: "" },
														variant: { variantName: "", description: "" },
														configuration: {
															batteryCapacity: "", batteryType: "", fullChargeTime: "", rangeKm: "", power: "", torque: "", lengthMm: "", widthMm: "", heightMm: "", wheelbaseMm: "", weightKg: "", trunkVolumeL: "", seats: ""
														},
														color: "",
														car: { productionYear: "", price: "", status: "", imagePath: "" }
													});
													loadVehicles();
													// Cập nhật lại danh sách modelOptions sau khi tạo xe mới
													fetchAllModelNames().then(models => setModelOptions(models)).catch(() => {});
												} catch (err) {
													setCreateCarError(err.message || "Lỗi khi tạo xe mới");
												} finally {
													setCreateCarLoading(false);
												}
											}}>
												<div className="reset-create-car-btn-row">
													<button
														type="button"
														className="reset-create-car-btn margin-right-8"
														onClick={() => {
															setCreateCarData({
																model: { modelName: "", segment: "" },
																variant: { variantName: "", description: "" },
																configuration: {
																	batteryCapacity: "", batteryType: "", fullChargeTime: "", rangeKm: "", power: "", torque: "", lengthMm: "", widthMm: "", heightMm: "", wheelbaseMm: "", weightKg: "", trunkVolumeL: "", seats: ""
																},
																color: "",
																car: { productionYear: "", price: "", status: "", imagePath: "" }
															});
															setCreateCarError("");
															setCreateCarSuccess("");
														}}
													>
														Làm mới
													</button>
												</div>
												<div className="form-section">
													<h4 className="form-section-title">Model</h4>
													<div className="form-row">
														<select
															required={!isCustomModel}
															value={isCustomModel ? "__custom__" : createCarData.model.modelName}
															onChange={e => handleModelChange(e.target.value)}
														>
															<option value="">Chọn dòng xe</option>
															{modelOptions.map(model => (
																<option key={model} value={model}>{model}</option>
															))}
															<option value="__custom__">Tạo mới...</option>
														</select>
														{isCustomModel ? (
															<>
																<input
																	type="text"
																	placeholder="Nhập dòng xe mới"
																	required
																	value={customModelName}
																	onChange={e => {
																		setCustomModelName(e.target.value);
																		setCreateCarData(d => ({ ...d, model: { ...d.model, modelName: e.target.value } }));
																	}}
																/>
																<input
																	type="text"
																	placeholder="Nhập phân khúc"
																	required
																	value={createCarData.model.segment}
																	onChange={e => setCreateCarData(d => ({ ...d, model: { ...d.model, segment: e.target.value } }))}
																/>
															</>
														) : (
															<input type="text" placeholder="Phân khúc" required value={createCarData.model.segment} readOnly />
														)}
													</div>
												</div>
												<div className="form-section">
													<h4 className="form-section-title">Variant</h4>
													<div className="form-row">
														<select
															required={!isCustomVariant}
															value={isCustomVariant ? "__custom__" : createCarData.variant.variantName}
															onChange={e => handleVariantChange(e.target.value)}
															disabled={isCustomModel ? !customModelName : !createCarData.model.modelName}
														>
															<option value="">Chọn phiên bản</option>
															{variantOptions.map(variant => (
																<option key={variant} value={variant}>{variant}</option>
															))}
															<option value="__custom__">Tạo mới...</option>
														</select>
														{isCustomVariant ? (
															<>
																<input
																	type="text"
																	placeholder="Nhập phiên bản mới"
																	required
																	value={customVariantName}
																	onChange={e => {
																		setCustomVariantName(e.target.value);
																		setCreateCarData(d => ({ ...d, variant: { ...d.variant, variantName: e.target.value } }));
																	}}
																/>
																<input
																	type="text"
																	placeholder="Nhập mô tả phiên bản"
																	required
																	value={createCarData.variant.description}
																	onChange={e => setCreateCarData(d => ({ ...d, variant: { ...d.variant, description: e.target.value } }))}
																/>
															</>
														) : (
															<input type="text" placeholder="Mô tả phiên bản" required value={createCarData.variant.description} readOnly />
														)}
													</div>
												</div>
												<div className="form-section">
													<h4 className="form-section-title">Configuration</h4>
													<div className="form-row">
														<input type="number" placeholder="Dung lượng pin" required value={createCarData.configuration.batteryCapacity} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, batteryCapacity: e.target.value } }))} />
														<input type="text" placeholder="Loại pin" required value={createCarData.configuration.batteryType} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, batteryType: e.target.value } }))} />
														<input type="number" placeholder="Thời gian sạc" required value={createCarData.configuration.fullChargeTime} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, fullChargeTime: e.target.value } }))} />
														<input type="number" placeholder="Quãng đường" required value={createCarData.configuration.rangeKm} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, rangeKm: e.target.value } }))} />
														<input type="number" step="0.01" placeholder="Công suất" required value={createCarData.configuration.power} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, power: e.target.value } }))} />
														<input type="number" step="0.01" placeholder="Mô men xoắn" required value={createCarData.configuration.torque} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, torque: e.target.value } }))} />
														<input type="number" placeholder="Chiều dài" required value={createCarData.configuration.lengthMm} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, lengthMm: e.target.value } }))} />
														<input type="number" placeholder="Chiều rộng" required value={createCarData.configuration.widthMm} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, widthMm: e.target.value } }))} />
														<input type="number" placeholder="Chiều cao" required value={createCarData.configuration.heightMm} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, heightMm: e.target.value } }))} />
														<input type="number" placeholder="Chiều dài cơ sở" required value={createCarData.configuration.wheelbaseMm} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, wheelbaseMm: e.target.value } }))} />
														<input type="number" placeholder="Khối lượng" required value={createCarData.configuration.weightKg} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, weightKg: e.target.value } }))} />
														<input type="number" placeholder="Dung tích cốp" required value={createCarData.configuration.trunkVolumeL} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, trunkVolumeL: e.target.value } }))} />
														<input type="number" placeholder="Số ghế" required value={createCarData.configuration.seats} onChange={e => setCreateCarData(d => ({ ...d, configuration: { ...d.configuration, seats: e.target.value } }))} />
													</div>
												</div>
												<div className="form-section">
													<h4 className="form-section-title">Color</h4>
													<div className="form-row">
														<input type="text" placeholder="Màu xe" required value={createCarData.color} onChange={e => setCreateCarData(d => ({ ...d, color: e.target.value }))} />
													</div>
												</div>
												<div className="form-section">
													<h4 className="form-section-title">Car</h4>
													<div className="form-row">
														<input type="number" placeholder="Năm sản xuất" required value={createCarData.car.productionYear} onChange={e => setCreateCarData(d => ({ ...d, car: { ...d.car, productionYear: e.target.value } }))} />
														<input type="number" placeholder="Giá xe" required value={createCarData.car.price} onChange={e => setCreateCarData(d => ({ ...d, car: { ...d.car, price: e.target.value } }))} />
														<input type="text" placeholder="Trạng thái xe" required value={createCarData.car.status} onChange={e => setCreateCarData(d => ({ ...d, car: { ...d.car, status: e.target.value } }))} />
														<input type="text" placeholder="Đường dẫn ảnh" required value={createCarData.car.imagePath} onChange={e => setCreateCarData(d => ({ ...d, car: { ...d.car, imagePath: e.target.value } }))} />
													</div>
												</div>
								{createCarError && <div className="error-message">{createCarError}</div>}
								{createCarSuccess && <div style={{ color: '#667eea', marginTop: 8, textAlign: 'center' }}>{createCarSuccess}</div>}
								<button className="create-user-submit-btn" type="submit" disabled={createCarLoading}>
									{createCarLoading ? "Đang tạo..." : "Tạo xe mới"}
								</button>
							</form>
						</div>
					</div>
				)}
			</div>
	);
}
export default CarManagement;




