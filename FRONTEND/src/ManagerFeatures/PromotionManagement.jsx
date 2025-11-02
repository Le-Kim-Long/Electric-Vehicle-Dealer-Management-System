import React, { useState, useEffect, useCallback } from 'react';
import {
	fetchPromotionsByDealer,
	createPromotion,
	searchPromotionsByType,
	searchPromotionsByStatus,
	searchPromotionsByTypeAndStatus,
	fetchPromotionById,
	updatePromotion,
	deletePromotion,
	getCurrentUser
} from '../services/carVariantApi';
import './PromotionManagement.css';

const PromotionManagement = () => {
	const [promotions, setPromotions] = useState([]);
	const [filteredPromotions, setFilteredPromotions] = useState([]);
	const [filterType, setFilterType] = useState('all');
	const [filterStatus, setFilterStatus] = useState('all');
	const [isLoading, setIsLoading] = useState(true);
	const [isSearching, setIsSearching] = useState(false);
	const [error, setError] = useState('');
	const [currentUser, setCurrentUser] = useState(null);

	// Modal states
	const [createModal, setCreateModal] = useState({ open: false });
	const [editModal, setEditModal] = useState({ open: false, promotion: null });
	const [deleteModal, setDeleteModal] = useState({ open: false, promotion: null });
	const [detailModal, setDetailModal] = useState({ open: false, promotion: null });

	// Form states
	const [formData, setFormData] = useState({
		promotionName: '',
		description: '',
		value: '',
		type: 'VND',
		scope: '',
		status: 'Đang hoạt động',
		startDate: '',
		endDate: ''
	});
	const [formLoading, setFormLoading] = useState(false);
	const [formError, setFormError] = useState('');
	const [formSuccess, setFormSuccess] = useState(false);

	useEffect(() => {
		const user = getCurrentUser();
		setCurrentUser(user);
		// Set scope to dealerName when user is loaded
		if (user && user.dealerName) {
			setFormData(prev => ({ ...prev, scope: user.dealerName }));
		}
		loadPromotionsFromAPI();
	}, []);

	const loadPromotionsFromAPI = async () => {
		setIsLoading(true);
		setError('');
		try {
			const data = await fetchPromotionsByDealer();
			
			// Handle nested array - API might return [[...]] instead of [...]
			let promotionsArray = data;
			if (Array.isArray(data) && data.length > 0 && Array.isArray(data[0])) {
				promotionsArray = data[0]; // Extract the inner array
			}
			
			// Ensure data is an array
			if (Array.isArray(promotionsArray)) {
				setPromotions(promotionsArray);
				setFilteredPromotions(promotionsArray);
			} else {
				setPromotions([]);
				setFilteredPromotions([]);
			}
		} catch (err) {
			setError(err.message || 'Không thể tải danh sách khuyến mãi. Vui lòng thử lại.');
			setPromotions([]);
			setFilteredPromotions([]);
		} finally {
			setIsLoading(false);
		}
	};

	const handleSearchByType = useCallback(async (type) => {
		setIsSearching(true);
		try {
			const data = await searchPromotionsByType(type);
			setFilteredPromotions(data);
		} catch (err) {
			setError(err.message);
		} finally {
			setIsSearching(false);
		}
	}, []);

	const handleSearchByStatus = useCallback(async (status) => {
		setIsSearching(true);
		try {
			const data = await searchPromotionsByStatus(status);
			setFilteredPromotions(data);
		} catch (err) {
			setError(err.message);
		} finally {
			setIsSearching(false);
		}
	}, []);

	const handleSearchByTypeAndStatus = useCallback(async (type, status) => {
		setIsSearching(true);
		try {
			const data = await searchPromotionsByTypeAndStatus(type, status);
			setFilteredPromotions(data);
		} catch (err) {
			setError(err.message);
		} finally {
			setIsSearching(false);
		}
	}, []);

	useEffect(() => {
		if (isLoading) return;

		if (filterType !== 'all' && filterStatus !== 'all') {
			handleSearchByTypeAndStatus(filterType, filterStatus);
		} else if (filterType !== 'all') {
			handleSearchByType(filterType);
		} else if (filterStatus !== 'all') {
			handleSearchByStatus(filterStatus);
		} else {
			setFilteredPromotions(promotions);
		}
	}, [filterType, filterStatus, isLoading, promotions, handleSearchByType, handleSearchByStatus, handleSearchByTypeAndStatus]);

	const resetForm = () => {
		setFormData({
			promotionName: '',
			description: '',
			value: '',
			type: 'VND',
			scope: currentUser?.dealerName || '',
			status: 'Đang hoạt động',
			startDate: '',
			endDate: ''
		});
		setFormError('');
		setFormSuccess(false);
	};

	const handleCreatePromotion = async () => {
		setFormLoading(true);
		setFormError('');
		setFormSuccess(false);
		try {
			// Auto-calculate status based on startDate
			const today = new Date();
			today.setHours(0, 0, 0, 0);
			const startDate = new Date(formData.startDate);
			startDate.setHours(0, 0, 0, 0);
			
			const status = startDate.getTime() === today.getTime() ? 'Đang hoạt động' : 'Không hoạt động';
			
			const dataToSubmit = {
				...formData,
				status: status
			};
			
			await createPromotion(dataToSubmit);
			setFormSuccess(true);
			setTimeout(() => {
				setCreateModal({ open: false });
				resetForm();
				loadPromotionsFromAPI();
			}, 1500);
		} catch (err) {
			setFormError(err.message || 'Không thể tạo khuyến mãi');
		} finally {
			setFormLoading(false);
		}
	};

	const handleUpdatePromotion = async () => {
		setFormLoading(true);
		setFormError('');
		setFormSuccess(false);
		try {
			// Auto-calculate status based on startDate
			const today = new Date();
			today.setHours(0, 0, 0, 0);
			const startDate = new Date(formData.startDate);
			startDate.setHours(0, 0, 0, 0);
			
			const status = startDate.getTime() <= today.getTime() ? 'Đang hoạt động' : 'Không hoạt động';
			
			const dataToSubmit = {
				...formData,
				status: status
			};
			
			await updatePromotion(editModal.promotion.promotionId, dataToSubmit);
			setFormSuccess(true);
			setTimeout(() => {
				setEditModal({ open: false, promotion: null });
				resetForm();
				loadPromotionsFromAPI();
			}, 1500);
		} catch (err) {
			setFormError(err.message || 'Không thể cập nhật khuyến mãi');
		} finally {
			setFormLoading(false);
		}
	};

	const handleDeletePromotion = async () => {
		setFormLoading(true);
		setFormError('');
		try {
			await deletePromotion(deleteModal.promotion.promotionId);
			setDeleteModal({ open: false, promotion: null });
			loadPromotionsFromAPI();
		} catch (err) {
			setFormError(err.message || 'Không thể xóa khuyến mãi');
		} finally {
			setFormLoading(false);
		}
	};

	const openCreateModal = () => {
		resetForm();
		setCreateModal({ open: true });
	};

	const openEditModal = (promotion) => {
		// Format dates for input type="date" (YYYY-MM-DD)
		const formatDateForInput = (dateValue) => {
			if (!dateValue) return '';
			try {
				let date;
				if (Array.isArray(dateValue)) {
					// Backend returns [year, month, day]
					const year = dateValue[0];
					const month = String(dateValue[1]).padStart(2, '0');
					const day = String(dateValue[2]).padStart(2, '0');
					return `${year}-${month}-${day}`;
				} else if (typeof dateValue === 'string') {
					return dateValue.split('T')[0]; // Handle ISO string
				}
				return '';
			} catch (error) {
				return '';
			}
		};

		setFormData({
			promotionName: promotion.promotionName,
			description: promotion.description,
			value: promotion.value,
			type: promotion.type,
			scope: promotion.scope || currentUser?.dealerName || '',
			status: promotion.status,
			startDate: formatDateForInput(promotion.startDate),
			endDate: formatDateForInput(promotion.endDate)
		});
		setEditModal({ open: true, promotion });
	};

	const openDetailModal = async (promotion) => {
		setDetailModal({ open: true, promotion });
	};

	const getStatusBadge = (status) => {
		const statusMap = {
			'Đang hoạt động': 'active',
			'Không hoạt động': 'ended'
		};
		return statusMap[status] || 'ended';
	};

	const getTypeBadge = (type) => {
		return type === 'VND' ? '💰 VND' : '📊 %';
	};

	const formatDate = (dateString) => {
		if (!dateString) return 'N/A';
		try {
			// Handle both array format [year, month, day] and string format
			let date;
			if (Array.isArray(dateString)) {
				// Backend returns array like [2024, 11, 2]
				date = new Date(dateString[0], dateString[1] - 1, dateString[2]);
			} else {
				date = new Date(dateString);
			}
			
			if (isNaN(date.getTime())) return 'N/A';
			
			return date.toLocaleDateString('vi-VN', {
				day: '2-digit',
				month: '2-digit',
				year: 'numeric'
			});
		} catch (error) {
			return 'N/A';
		}
	};

	const formatValue = (value, type) => {
		if (!value) return '0';
		if (type === 'VND') {
			return `${parseInt(value).toLocaleString('vi-VN')} VND`;
		} else {
			return `${value}%`;
		}
	};

	if (isLoading) {
		return (
			<div className="promotion-loading">
				<div className="loading-spinner-promo">
					<div className="spinner-circle-promo"></div>
					<div className="spinner-circle-promo"></div>
					<div className="spinner-circle-promo"></div>
				</div>
				<p className="loading-text-promo">Đang tải danh sách khuyến mãi...</p>
			</div>
		);
	}

	if (error && promotions.length === 0) {
		return (
			<div className="promotion-error">
				<div className="error-icon-promo">⚠️</div>
				<h2>Không thể tải danh sách khuyến mãi</h2>
				<p>{error}</p>
				<button className="refresh-btn-promo" onClick={loadPromotionsFromAPI}>
					🔄 Thử lại
				</button>
			</div>
		);
	}

	return (
		<div className="promotion-management">
			<div className="promotion-header">
				<div className="promotion-header-content">
					<div className="promotion-header-icon">🎁</div>
					<div className="promotion-header-text">
						<h2>Quản lý khuyến mãi</h2>
						<p>
							Khuyến mãi của {currentUser?.dealerName || 'đại lý'}
							{' • '}{promotions.length} chương trình
						</p>
					</div>
				</div>
				<button className="create-promotion-btn" onClick={openCreateModal}>
					➕ Tạo khuyến mãi mới
				</button>
			</div>

			<div className="search-filters-promo">
				<div className="filters-promo">
					<select
						value={filterType}
						onChange={(e) => setFilterType(e.target.value)}
					>
						<option value="all">Tất cả loại</option>
						<option value="VND">VND</option>
						<option value="%">%</option>
					</select>

					<select
						value={filterStatus}
						onChange={(e) => setFilterStatus(e.target.value)}
					>
						<option value="all">Tất cả trạng thái</option>
						<option value="Đang hoạt động">Đang hoạt động</option>
						<option value="Không hoạt động">Không hoạt động</option>
					</select>

					<button
						className="refresh-btn-promo"
						onClick={() => {
							setFilterType('all');
							setFilterStatus('all');
							loadPromotionsFromAPI();
						}}
						title="Làm mới bộ lọc và dữ liệu"
					>
						🔄 Làm mới
					</button>
				</div>
			</div>

			<div className="promotion-grid">
				{filteredPromotions.map((promotion) => (
					<div key={promotion.promotionId} className="promotion-card">
						<div className="promotion-card-header">
							<div className={`status-badge-promo ${getStatusBadge(promotion.status)}`}>
								{promotion.status}
							</div>
							<div className="type-badge-promo">
								{getTypeBadge(promotion.type)}
							</div>
						</div>
						<div className="promotion-card-body">
							<h3 className="promotion-name">{promotion.promotionName}</h3>
							<p className="promotion-description">{promotion.description}</p>
							<div className="promotion-value">
								{formatValue(promotion.value, promotion.type)}
							</div>
							<div className="promotion-dates">
								<div className="date-item">
									<span className="date-label">📅 Bắt đầu:</span>
									<span className="date-value">{formatDate(promotion.startDate)}</span>
								</div>
								<div className="date-item">
									<span className="date-label">📅 Kết thúc:</span>
									<span className="date-value">{formatDate(promotion.endDate)}</span>
								</div>
							</div>
						</div>
						<div className="promotion-card-actions">
							<button className="btn-view" onClick={() => openDetailModal(promotion)}>
								👁️ Chi tiết
							</button>
							<button className="btn-edit" onClick={() => openEditModal(promotion)}>
								✏️ Sửa
							</button>
							<button className="btn-delete" onClick={() => setDeleteModal({ open: true, promotion })}>
								🗑️ Xóa
							</button>
						</div>
					</div>
				))}
			</div>

			{filteredPromotions.length === 0 && !isSearching && !isLoading && (
				<div className="no-results-promo">
					<div className="no-results-icon-promo">🔍</div>
					<p>Không tìm thấy khuyến mãi nào phù hợp với bộ lọc.</p>
				</div>
			)}

			{/* Create Modal */}
			{createModal.open && (
				<div className="modal-overlay-promo" onClick={() => setCreateModal({ open: false })}>
					<div className="modal-content-promo" onClick={(e) => e.stopPropagation()}>
						<div className="modal-header-promo">
							<h2>➕ Tạo khuyến mãi mới</h2>
							<button className="close-btn-promo" onClick={() => setCreateModal({ open: false })}>×</button>
						</div>
						<div className="modal-body-promo">
							<div className="form-group-promo">
								<label>Tên khuyến mãi <span className="required">*</span></label>
								<input
									type="text"
									value={formData.promotionName}
									onChange={(e) => setFormData({ ...formData, promotionName: e.target.value })}
									placeholder="VD: Giảm giá mùa hè"
								/>
							</div>
							<div className="form-group-promo">
								<label>Mô tả</label>
								<textarea
									value={formData.description}
									onChange={(e) => setFormData({ ...formData, description: e.target.value })}
									placeholder="Mô tả chi tiết về khuyến mãi"
									rows="3"
								/>
							</div>
							<div className="form-row-promo">
								<div className="form-group-promo">
									<label>Giá trị <span className="required">*</span></label>
									<input
										type="number"
										min="0"
										value={formData.value}
										onChange={(e) => setFormData({ ...formData, value: e.target.value })}
										placeholder={formData.type === 'VND' ? 'VD: 5000000' : 'VD: 10'}
									/>
								</div>
								<div className="form-group-promo">
									<label>Loại <span className="required">*</span></label>
									<select
										value={formData.type}
										onChange={(e) => setFormData({ ...formData, type: e.target.value })}
									>
										<option value="VND">VND</option>
										<option value="%">%</option>
									</select>
								</div>
							</div>
							<div className="form-row-promo">
								<div className="form-group-promo full-width-promo">
									<label>Phạm vi</label>
									<input
										type="text"
										value={formData.scope}
										className="readonly-input"
										readOnly
										placeholder="Phạm vi đại lý"
									/>
								</div>
							</div>
							<div className="form-row-promo">
								<div className="form-group-promo">
									<label>Ngày bắt đầu <span className="required">*</span></label>
									<input
										type="date"
										value={formData.startDate}
										onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
									/>
								</div>
								<div className="form-group-promo">
									<label>Ngày kết thúc <span className="required">*</span></label>
									<input
										type="date"
										value={formData.endDate}
										onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
									/>
								</div>
							</div>
							{formError && <div className="form-error-promo">{formError}</div>}
							{formSuccess && <div className="form-success-promo">✅ Tạo khuyến mãi thành công!</div>}
							<div className="form-actions-promo">
								<button
									className="btn-cancel-promo"
									onClick={() => setCreateModal({ open: false })}
									disabled={formLoading}
								>
									Hủy
								</button>
								<button
									className="btn-submit-promo"
									onClick={handleCreatePromotion}
									disabled={formLoading || !formData.promotionName || !formData.value || !formData.startDate || !formData.endDate}
								>
									{formLoading ? '⏳ Đang tạo...' : '✅ Tạo khuyến mãi'}
								</button>
							</div>
						</div>
					</div>
				</div>
			)}

			{/* Edit Modal */}
			{editModal.open && (
				<div className="modal-overlay-promo" onClick={() => setEditModal({ open: false, promotion: null })}>
					<div className="modal-content-promo" onClick={(e) => e.stopPropagation()}>
						<div className="modal-header-promo">
							<h2>✏️ Cập nhật khuyến mãi</h2>
							<button className="close-btn-promo" onClick={() => setEditModal({ open: false, promotion: null })}>×</button>
						</div>
						<div className="modal-body-promo">
							<div className="form-group-promo">
								<label>Tên khuyến mãi <span className="required">*</span></label>
								<input
									type="text"
									value={formData.promotionName}
									onChange={(e) => setFormData({ ...formData, promotionName: e.target.value })}
									placeholder="VD: Giảm giá mùa hè"
								/>
							</div>
							<div className="form-group-promo">
								<label>Mô tả</label>
								<textarea
									value={formData.description}
									onChange={(e) => setFormData({ ...formData, description: e.target.value })}
									placeholder="Mô tả chi tiết về khuyến mãi"
									rows="3"
								/>
							</div>
							<div className="form-row-promo">
								<div className="form-group-promo">
									<label>Giá trị <span className="required">*</span></label>
									<input
										type="number"
										min="0"
										value={formData.value}
										onChange={(e) => setFormData({ ...formData, value: e.target.value })}
										placeholder={formData.type === 'VND' ? 'VD: 5000000' : 'VD: 10'}
									/>
								</div>
								<div className="form-group-promo">
									<label>Loại <span className="required">*</span></label>
									<select
										value={formData.type}
										onChange={(e) => setFormData({ ...formData, type: e.target.value })}
									>
										<option value="VND">VND</option>
										<option value="%">%</option>
									</select>
								</div>
							</div>
							<div className="form-row-promo">
								<div className="form-group-promo full-width-promo">
									<label>Phạm vi (Đại lý)</label>
									<input
										type="text"
										value={formData.scope}
										readOnly
										placeholder="Tên đại lý"
										className="readonly-input"
									/>
								</div>
							</div>
							<div className="form-row-promo">
								<div className="form-group-promo">
									<label>Ngày bắt đầu <span className="required">*</span></label>
									<input
										type="date"
										value={formData.startDate}
										onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
									/>
								</div>
								<div className="form-group-promo">
									<label>Ngày kết thúc <span className="required">*</span></label>
									<input
										type="date"
										value={formData.endDate}
										onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
									/>
								</div>
							</div>
							{formError && <div className="form-error-promo">{formError}</div>}
							{formSuccess && <div className="form-success-promo">✅ Cập nhật thành công!</div>}
							<div className="form-actions-promo">
								<button
									className="btn-cancel-promo"
									onClick={() => setEditModal({ open: false, promotion: null })}
									disabled={formLoading}
								>
									Hủy
								</button>
								<button
									className="btn-submit-promo"
									onClick={handleUpdatePromotion}
									disabled={formLoading || !formData.promotionName || !formData.value || !formData.startDate || !formData.endDate}
								>
									{formLoading ? '⏳ Đang cập nhật...' : '✅ Lưu thay đổi'}
								</button>
							</div>
						</div>
					</div>
				</div>
			)}

			{/* Delete Modal */}
			{deleteModal.open && (
				<div className="modal-overlay-promo" onClick={() => setDeleteModal({ open: false, promotion: null })}>
					<div className="modal-content-promo modal-small-promo" onClick={(e) => e.stopPropagation()}>
						<div className="modal-header-promo">
							<h2>🗑️ Xác nhận xóa</h2>
							<button className="close-btn-promo" onClick={() => setDeleteModal({ open: false, promotion: null })}>×</button>
						</div>
						<div className="modal-body-promo">
							<p className="delete-confirm-text">
								Bạn có chắc chắn muốn xóa khuyến mãi <strong>"{deleteModal.promotion?.promotionName}"</strong>?
							</p>
							<p className="delete-warning">⚠️ Hành động này không thể hoàn tác!</p>
							{formError && <div className="form-error-promo">{formError}</div>}
							<div className="form-actions-promo">
								<button
									className="btn-cancel-promo"
									onClick={() => setDeleteModal({ open: false, promotion: null })}
									disabled={formLoading}
								>
									Hủy
								</button>
								<button
									className="btn-delete-confirm-promo"
									onClick={handleDeletePromotion}
									disabled={formLoading}
								>
									{formLoading ? '⏳ Đang xóa...' : '🗑️ Xóa khuyến mãi'}
								</button>
							</div>
						</div>
					</div>
				</div>
			)}

			{/* Detail Modal */}
			{detailModal.open && (
				<div className="modal-overlay-promo" onClick={() => setDetailModal({ open: false, promotion: null })}>
					<div className="modal-content-promo" onClick={(e) => e.stopPropagation()}>
						<div className="modal-header-promo">
							<h2>👁️ Chi tiết khuyến mãi</h2>
							<button className="close-btn-promo" onClick={() => setDetailModal({ open: false, promotion: null })}>×</button>
						</div>
						<div className="modal-body-promo">
							<div className="detail-section-promo">
								<h3>Thông tin chung</h3>
								<div className="detail-grid-promo">
									<div className="detail-item-promo">
										<span className="detail-label-promo">Tên khuyến mãi:</span>
										<span className="detail-value-promo">{detailModal.promotion?.promotionName}</span>
									</div>
									<div className="detail-item-promo">
										<span className="detail-label-promo">Mã ID:</span>
										<span className="detail-value-promo">#{detailModal.promotion?.promotionId}</span>
									</div>
									<div className="detail-item-promo">
										<span className="detail-label-promo">Loại:</span>
										<span className="detail-value-promo">{getTypeBadge(detailModal.promotion?.type)}</span>
									</div>
									<div className="detail-item-promo">
										<span className="detail-label-promo">Giá trị:</span>
										<span className="detail-value-promo detail-value-highlight">
											{formatValue(detailModal.promotion?.value, detailModal.promotion?.type)}
										</span>
									</div>
									<div className="detail-item-promo">
										<span className="detail-label-promo">Phạm vi:</span>
										<span className="detail-value-promo">{detailModal.promotion?.scope || currentUser?.dealerName || 'N/A'}</span>
									</div>
									<div className="detail-item-promo">
										<span className="detail-label-promo">Trạng thái:</span>
										<span className={`status-badge-promo ${getStatusBadge(detailModal.promotion?.status)}`}>
											{detailModal.promotion?.status}
										</span>
									</div>
								</div>
							</div>
							<div className="detail-section-promo">
								<h3>Mô tả</h3>
								<p className="detail-description-promo">{detailModal.promotion?.description || 'Không có mô tả'}</p>
							</div>
							<div className="detail-section-promo">
								<h3>Thời gian áp dụng</h3>
								<div className="detail-dates-promo">
									<div className="detail-date-item-promo">
										<span className="detail-label-promo">📅 Ngày bắt đầu:</span>
										<span className="detail-value-promo">{formatDate(detailModal.promotion?.startDate)}</span>
									</div>
									<div className="detail-date-item-promo">
										<span className="detail-label-promo">📅 Ngày kết thúc:</span>
										<span className="detail-value-promo">{formatDate(detailModal.promotion?.endDate)}</span>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			)}
		</div>
	);
};

export default PromotionManagement;
