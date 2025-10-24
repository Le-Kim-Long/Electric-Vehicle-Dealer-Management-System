import React, { useState, useEffect } from 'react';
import './FeedbackTestDriveFeature.css';

const FeedbackTestDriveFeature = ({ testDriveBookings = [], quoteRequests = [] }) => {
  const [activeTab, setActiveTab] = useState('testdrives');
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [showFeedbackModal, setShowFeedbackModal] = useState(false);
  const [selectedTestDrive, setSelectedTestDrive] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [quotes, setQuotes] = useState([]);

  // Mock data cho lịch hẹn lái thử (theo cấu trúc LICHHENLAITHU)
  const mockTestDrives = [
    {
      maLichHen: 1,
      maKhachHang: 101,
      customerName: 'Nguyễn Văn A',
      customerPhone: '0901234567',
      customerEmail: 'nguyenvana@gmail.com',
      maXe: 1,
      vehicle: 'VinFast VF8',
      maDaiLy: 1,
      dealerName: 'VinFast Quận 1',
      ngayHen: '2024-02-25',
      gioHen: '09:00',
      trangThai: 'Xác nhận',
      ghiChu: 'Khách hàng quan tâm đến tính năng tự lái',
      ngayTao: '2024-02-20T10:30:00'
    },
    {
      maLichHen: 2,
      maKhachHang: 102,
      customerName: 'Trần Thị B',
      customerPhone: '0902345678',
      customerEmail: 'tranthib@yahoo.com',
      maXe: 2,
      vehicle: 'VinFast VF9',
      maDaiLy: 2,
      dealerName: 'VinFast Quận 3',
      ngayHen: '2024-02-26',
      gioHen: '14:30',
      trangThai: 'Đang chờ',
      ghiChu: 'Lần đầu lái xe điện, cần hướng dẫn kỹ',
      ngayTao: '2024-02-22T15:20:00'
    },
    {
      maLichHen: 3,
      maKhachHang: 103,
      customerName: 'Lê Minh C',
      customerPhone: '0903456789',
      customerEmail: 'leminhc@outlook.com',
      maXe: 3,
      vehicle: 'VinFast VF6',
      maDaiLy: 3,
      dealerName: 'VinFast Quận 7',
      ngayHen: '2024-02-24',
      gioHen: '16:00',
      trangThai: 'Hoàn thành',
      ghiChu: 'Khách hàng hài lòng với hiệu suất',
      ngayTao: '2024-02-18T09:15:00',
      feedback: {
        rating: 5,
        comment: 'Xe chạy êm ái, tính năng hiện đại. Rất hài lòng với trải nghiệm lái thử.',
        recommend: true
      }
    },
    {
      maLichHen: 4,
      maKhachHang: 104,
      customerName: 'Phạm Văn D',
      customerPhone: '0904567890',
      customerEmail: 'phamvand@gmail.com',
      maXe: 4,
      vehicle: 'VinFast VF7',
      maDaiLy: 1,
      dealerName: 'VinFast Quận 5',
      ngayHen: '2024-02-23',
      gioHen: '11:00',
      trangThai: 'Đã hủy',
      ghiChu: 'Khách hàng có việc đột xuất',
      ngayTao: '2024-02-19T14:45:00'
    }
  ];

  // Load test drive bookings from localStorage
  useEffect(() => {
    const loadBookings = () => {
      try {
        const savedBookings = JSON.parse(localStorage.getItem('testDriveBookings') || '[]');
        // Combine saved bookings with mock data
        const combined = [...savedBookings, ...mockTestDrives];
        
        // Sort by creation date (newest first)
        const sorted = combined.sort((a, b) => new Date(b.ngayTao) - new Date(a.ngayTao));
        
        setBookings(sorted);
      } catch (error) {
        console.error('Error loading bookings:', error);
        setBookings(mockTestDrives);
      }
    };

    const loadQuotes = () => {
      try {
        const savedQuotes = JSON.parse(localStorage.getItem('quoteRequests') || '[]');
        const combined = [...savedQuotes, ...mockQuoteRequests];
        setQuotes(combined);
      } catch (error) {
        console.error('Error loading quotes:', error);
        setQuotes(mockQuoteRequests);
      }
    };

    // Load initial data
    loadBookings();
    loadQuotes();

    // Set up auto-refresh interval
    const interval = setInterval(() => {
      loadBookings();
      loadQuotes();
    }, 3000);

    // Clean up interval on unmount
    return () => clearInterval(interval);
  }, []);

  // Combine mock data with real bookings from localStorage
  const allTestDrives = bookings || [];

  // Mock data for quote requests (to show alongside real ones)
  const mockQuoteRequests = [
    {
      id: 'YC001',
      customerName: 'Nguyễn Văn E',
      customerPhone: '0908123456',
      customerEmail: 'nguyenvane@gmail.com',
      vehicle: 'VinFast VF8 Plus',
      selectedColor: 'Đen',
      notes: 'Muốn biết giá lăn bánh và chương trình ưu đãi hiện tại',
      status: 'Đã phản hồi',
      createdDate: '2024-02-23'
    },
    {
      id: 'YC002',
      customerName: 'Trần Thị F',
      customerPhone: '0909234567',
      customerEmail: 'tranthif@yahoo.com',
      vehicle: 'VinFast VF9 Eco',
      selectedColor: 'Trắng',
      notes: 'Quan tâm đến điều kiện trả góp và bảo hiểm',
      status: 'Chờ xử lý',
      createdDate: '2024-02-24'
    }
  ];

  const allQuoteRequests = quotes || [];

  // Mock data cho phản hồi (theo cấu trúc PHANHOI)
  const mockFeedbacks = [
    {
      maPhanHoi: 1,
      maDonHang: 1001,
      orderCode: 'DH001',
      customerName: 'Nguyễn Thị E',
      customerEmail: 'nguyenthie@gmail.com',
      customerPhone: '0905678901',
      noiDung: 'Thời gian chờ tại showroom hơi lâu, mong shop cải thiện để khách hàng có trải nghiệm tốt hơn.',
      ngayGui: '2024-02-22T14:30:00',
      trangThai: 'Đang xử lý',
      nguoiXuLy: 101,
      staffName: 'Nguyễn Văn Staff',
      ngayXuLy: null,
      ghiChu: 'Khách hàng phản ánh về thời gian chờ'
    },
    {
      maPhanHoi: 2,
      maDonHang: 1002,
      orderCode: 'DH002',
      customerName: 'Hoàng Minh F',
      customerEmail: 'hoangminhf@yahoo.com',
      customerPhone: '0906789012',
      noiDung: 'Xe có tiếng kêu lạ ở bánh trước sau 1 tuần sử dụng. Yêu cầu kiểm tra và bảo hành.',
      ngayGui: '2024-02-21T10:15:00',
      trangThai: 'Đã xử lý',
      nguoiXuLy: 102,
      staffName: 'Trần Thị Manager',
      ngayXuLy: '2024-02-22T09:00:00',
      ghiChu: 'Đã liên hệ và hẹn lịch kiểm tra xe'
    },
    {
      maPhanHoi: 3,
      maDonHang: 1003,
      orderCode: 'DH003',
      customerName: 'Vũ Thị G',
      customerEmail: 'vuthig@outlook.com',
      customerPhone: '0907890123',
      noiDung: 'Đội ngũ tư vấn rất nhiệt tình và chuyên nghiệp. Rất hài lòng với dịch vụ của showroom.',
      ngayGui: '2024-02-20T16:45:00',
      trangThai: 'Đã phản hồi',
      nguoiXuLy: 103,
      staffName: 'Lê Văn Staff',
      ngayXuLy: '2024-02-21T08:30:00',
      ghiChu: 'Phản hồi tích cực từ khách hàng'
    },
    {
      maPhanHoi: 4,
      maDonHang: 1004,
      orderCode: 'DH004',
      customerName: 'Phạm Văn H',
      customerEmail: 'phamvanh@gmail.com',
      customerPhone: '0908901234',
      noiDung: 'Cần hướng dẫn sử dụng các tính năng của xe rõ ràng hơn. Một số chức năng còn khó hiểu.',
      ngayGui: '2024-02-23T11:20:00',
      trangThai: 'Chưa xử lý',
      nguoiXuLy: null,
      staffName: null,
      ngayXuLy: null,
      ghiChu: null
    }
  ];

  const getStatusColor = (trangThai) => {
    switch (trangThai) {
      case 'Xác nhận':
      case 'Đã phản hồi':
        return '#27ae60';
      case 'Đang chờ':
      case 'Đang xử lý':
        return '#f39c12';
      case 'Hoàn thành':
      case 'Đã xử lý':
        return '#3498db';
      case 'Đã hủy':
      case 'Chưa xử lý':
        return '#e74c3c';
      default:
        return '#95a5a6';
    }
  };

  const openFeedbackModal = (feedback) => {
    setSelectedTestDrive(feedback);
    setShowFeedbackModal(true);
  };

  return (
    <div className="feedback-testdrive-feature">
      {/* Header Section - THAY ĐỔI */}
      <div className="feedback-testdrive-header">
        <div className="feedback-testdrive-header-content">
          <div className="feedback-testdrive-header-icon">💬</div>
          <div className="feedback-testdrive-header-text">
            <h2>Phản hồi & Lái thử</h2>
            <p>Quản lý phản hồi khách hàng và lịch hẹn lái thử xe</p>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="feedback-tabs">
        <button 
          className={`tab-button ${activeTab === 'testdrives' ? 'active' : ''}`}
          onClick={() => setActiveTab('testdrives')}
        >
          Lịch lái thử ({bookings.length})
        </button>
        <button 
          className={`tab-button ${activeTab === 'feedbacks' ? 'active' : ''}`}
          onClick={() => setActiveTab('feedbacks')}
        >
          Phản hồi khách hàng ({mockFeedbacks.length})
        </button>
        <button 
          className={`tab-button ${activeTab === 'quotes' ? 'active' : ''}`}
          onClick={() => setActiveTab('quotes')}
        >
          Yêu cầu báo giá ({allQuoteRequests.length})
        </button>
      </div>

      {/* Action Buttons - Removed Booking Button */}
      <div className="feature-actions" style={{ display: 'none' }}>
      </div>

      {/* Test Drives Tab */}
      {activeTab === 'testdrives' && (
        <div className="testdrives-content">
          <div className="testdrives-grid">
            {allTestDrives.map(testdrive => (
              <div key={testdrive.maLichHen} className="testdrive-card">
                <div className="testdrive-header">
                  <div className="testdrive-id">LH{testdrive.maLichHen.toString().padStart(3, '0')}</div>
                  <span 
                    className="testdrive-status"
                    style={{ backgroundColor: getStatusColor(testdrive.trangThai) }}
                  >
                    {testdrive.trangThai}
                  </span>
                </div>
                
                <div className="testdrive-info">
                  <h3>{testdrive.customerName}</h3>
                  <div className="info-row">
                    <span className="info-label">Mã KH:</span>
                    <span className="info-value">
                      {testdrive.maKhachHang ? `KH${testdrive.maKhachHang.toString().padStart(3, '0')}` : 'Chưa có'}
                    </span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Xe:</span>
                    <span className="info-value">{testdrive.vehicle}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Ngày hẹn:</span>
                    <span className="info-value">{testdrive.ngayHen} - {testdrive.gioHen}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Đại lý:</span>
                    <span className="info-value">{testdrive.dealerName}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">SĐT:</span>
                    <span className="info-value">{testdrive.customerPhone}</span>
                  </div>
                  <div className="info-row full-width">
                    <span className="info-label">Ghi chú:</span>
                    <span className="info-value">{testdrive.ghiChu}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Ngày tạo:</span>
                    <span className="info-value">{new Date(testdrive.ngayTao).toLocaleDateString('vi-VN')}</span>
                  </div>
                </div>

                <div className="testdrive-actions">
                  <button className="btn-contact">Liên hệ</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Feedbacks Tab */}
      {activeTab === 'feedbacks' && (
        <div className="feedbacks-content">
          <div className="feedbacks-grid">
            {mockFeedbacks.map(feedback => (
              <div key={feedback.maPhanHoi} className="feedback-card">
                <div className="feedback-header">
                  <div className="feedback-id">PH{feedback.maPhanHoi.toString().padStart(3, '0')}</div>
                  <div className="feedback-badges">
                    <span 
                      className="feedback-status"
                      style={{ backgroundColor: getStatusColor(feedback.trangThai) }}
                    >
                      {feedback.trangThai}
                    </span>
                  </div>
                </div>

                <div className="feedback-info">
                  <h3>{feedback.customerName}</h3>
                  <div className="feedback-order">Đơn hàng: {feedback.orderCode}</div>
                  
                  <div className="info-row">
                    <span className="info-label">Ngày gửi:</span>
                    <span className="info-value">{new Date(feedback.ngayGui).toLocaleDateString('vi-VN')}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Email:</span>
                    <span className="info-value">{feedback.customerEmail}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">SĐT:</span>
                    <span className="info-value">{feedback.customerPhone}</span>
                  </div>
                  {feedback.staffName && (
                    <div className="info-row">
                      <span className="info-label">Người xử lý:</span>
                      <span className="info-value">{feedback.staffName}</span>
                    </div>
                  )}
                  {feedback.ngayXuLy && (
                    <div className="info-row">
                      <span className="info-label">Ngày xử lý:</span>
                      <span className="info-value">{new Date(feedback.ngayXuLy).toLocaleDateString('vi-VN')}</span>
                    </div>
                  )}
                  
                  <div className="feedback-message">
                    <strong>Nội dung phản hồi:</strong>
                    <p>{feedback.noiDung}</p>
                  </div>
                </div>

                <div className="feedback-actions">
                  <button className="btn-respond">Phản hồi</button>
                  <button className="btn-view">Xem chi tiết</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Quotes Tab */}
      {activeTab === 'quotes' && (
        <div className="quotes-content">
          <div className="quotes-grid">
            {allQuoteRequests.map(quote => (
              <div key={quote.id} className="quote-card">
                <div className="quote-header">
                  <div className="quote-id">{quote.id}</div>
                  <span 
                    className="quote-status"
                    style={{ backgroundColor: getStatusColor(quote.status) }}
                  >
                    {quote.status}
                  </span>
                </div>
                
                <div className="quote-info">
                  <h4>{quote.customerName}</h4>
                  <div className="quote-contact">
                    <div>📞 {quote.customerPhone}</div>
                    {quote.customerEmail && <div>✉️ {quote.customerEmail}</div>}
                  </div>
                  
                  <div className="quote-vehicle">
                    <strong>Xe quan tâm:</strong> {quote.vehicle}
                    {quote.selectedColor && (
                      <div className="quote-color">
                        <strong>Màu sắc:</strong> <span className="color-highlight">{quote.selectedColor}</span>
                      </div>
                    )}
                  </div>
                  
                  <div className="quote-footer">
                    <span className="quote-date">Ngày tạo: {quote.createdDate}</span>
                  </div>
                </div>
                
                <div className="quote-actions">
                  <button className="btn-primary">Gửi báo giá</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Removed Booking Modal - No longer needed */}

      {/* Feedback Detail Modal */}
      {showFeedbackModal && selectedTestDrive && (
        <div className="modal-overlay" onClick={() => setShowFeedbackModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Chi tiết phản hồi lái thử</h2>
              <button className="modal-close" onClick={() => setShowFeedbackModal(false)}>×</button>
            </div>
            <div className="modal-body">
              <div className="feedback-detail">
                <div className="feedback-rating">
                  <h3>Đánh giá tổng thể</h3>
                  <div className="rating-display">
                    <span className="rating-stars">
                      {'★'.repeat(selectedTestDrive.feedback.rating)}
                      {'☆'.repeat(5-selectedTestDrive.feedback.rating)}
                    </span>
                    <span className="rating-text">({selectedTestDrive.feedback.rating}/5 sao)</span>
                  </div>
                </div>
                
                <div className="feedback-comment">
                  <h3>Nhận xét</h3>
                  <p>"{selectedTestDrive.feedback.comment}"</p>
                </div>
                
                <div className="feedback-recommendation">
                  <h3>Có giới thiệu cho bạn bè không?</h3>
                  <p>{selectedTestDrive.feedback.recommend ? 'Có, sẽ giới thiệu' : 'Không giới thiệu'}</p>
                </div>
                
                <div className="testdrive-summary">
                  <h3>Thông tin lái thử</h3>
                  <div className="info-grid">
                    <p><strong>Khách hàng:</strong> {selectedTestDrive.customerName}</p>
                    <p><strong>Xe lái thử:</strong> {selectedTestDrive.vehicle}</p>
                    <p><strong>Ngày:</strong> {selectedTestDrive.date} - {selectedTestDrive.time}</p>
                    <p><strong>Địa điểm:</strong> {selectedTestDrive.location}</p>
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

export default FeedbackTestDriveFeature;