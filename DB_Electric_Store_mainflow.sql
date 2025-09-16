use MASTER
go
-- 0. Xóa Database RentalDB nếu đã tồn tại trong DBMS --------------------
IF EXISTS (
    SELECT name 
    FROM sys.databases 
    WHERE name = N'DB_Electric_Store_mainflow'
)
BEGIN
    -- Tùy chọn: đưa DB về SINGLE_USER để tránh lỗi đang có kết nối
    ALTER DATABASE [DB_Electric_Store_mainflow] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    
    -- Xóa database
    DROP DATABASE [DB_Electric_Store_mainflow];
END

go
-- 1. Tạo database
CREATE DATABASE DB_Electric_Store_mainflow;
go

USE DB_Electric_Store_mainflow;
go

--2. Tạo bảng đại lý đại lý
CREATE TABLE DaiLy (
    MaDaiLy INT PRIMARY KEY IDENTITY(1,1),
    TenDaiLy NVARCHAR(100) NOT NULL,
    DiaChi NVARCHAR(200),
    SoDienThoai NVARCHAR(20),
    Email NVARCHAR(100),
    SoHopDong NVARCHAR(50),
    HanMucCongNo DECIMAL(18,2),
    TrangThai NVARCHAR(50) DEFAULT N'Đang hoạt động', -- Đang hoạt động, Ngừng hợp tác
    NgayTao DATETIME DEFAULT GETDATE()
);

--3. Tạo bảng quản lý nhân viên để theo dõi doanh số bán hàng của nhân viên
CREATE TABLE NhanVienDaiLy (
    MaNhanVien INT PRIMARY KEY IDENTITY(1,1),
    MaDaiLy INT NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    ChucVu NVARCHAR(50),        -- Nhân viên bán hàng, quản lý...
    SoDienThoai NVARCHAR(20),
    Email NVARCHAR(100),
    NgayTuyenDung DATE,
    TrangThai NVARCHAR(50) DEFAULT N'Đang làm việc',
    CONSTRAINT FK_NhanVien_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);

--4. Tạo bảng người dùng
CREATE TABLE NguoiDung (
    MaNguoiDung INT PRIMARY KEY IDENTITY(1,1),
    TenDangNhap NVARCHAR(50) UNIQUE NOT NULL,
    MatKhau NVARCHAR(255) NOT NULL,
    HoTen NVARCHAR(100),
    Email NVARCHAR(100),
    SoDienThoai NVARCHAR(20),
    VaiTro NVARCHAR(50) NOT NULL, -- Admin, NhanVienDaiLy, QuanLyDaiLy, NhanVienHang
    MaDaiLy INT NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Hoạt động', -- Hoạt động, Bị khóa
    NgayTao DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_NguoiDung_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);

--5. Tạo bảng hảng xe
CREATE TABLE HangXe (
    MaHangXe INT PRIMARY KEY IDENTITY(1,1),
    TenHangXe NVARCHAR(100) NOT NULL,
    DiaChi NVARCHAR(200),
    SoDienThoai NVARCHAR(20),
    Email NVARCHAR(100) UNIQUE
);

--6. Tạo bảng xe
CREATE TABLE Xe (
    MaXe INT PRIMARY KEY IDENTITY(1,1),
	MaHangXe INT NOT NULL,
    TenXe NVARCHAR(100) NOT NULL,       -- Tên mẫu xe (VD: VinFast VF e34)
    PhienBan NVARCHAR(50),              -- Phiên bản (Standard, Premium,...)
    MauSac NVARCHAR(50),                -- Màu sắc
    GiaBan DECIMAL(18,2),               -- Giá bán niêm yết 
	XuatXu NVARCHAR(50),
	NamSanXuat INT NULL,
	TrangThai NVARCHAR(50) DEFAULT N'Đang bán', -- Đang bán, Ngừng bán
	CONSTRAINT FK_Xe_HangXe FOREIGN KEY (MaHangXe) REFERENCES HangXe(MaHangXe)
);

--6.1 Tạo các bảng cấu hình xe
CREATE TABLE PinXe (
    MaPin INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    DungLuong DECIMAL(5,2),          -- kWh
    LoaiPin NVARCHAR(50) NULL,         -- Lithium-ion, LFP...
    QuangDuong INT,                  -- km
    CONSTRAINT FK_Pin_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);
--6.2. Tạo bảng động cơ xe để dễ dàng so sánh
CREATE TABLE DongCo (
    MaDongCo INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    CongSuat NVARCHAR(50) NULL,       -- Công suất động cơ
    MoMenXoan NVARCHAR(50) NULL,      -- Mô-men xoắn
    SoDongCo NVARCHAR(50) NULL,       -- Số động cơ (single/dual motor)
    TangToc NVARCHAR(50) NULL,        -- Tăng tốc 0–100 km/h
    TocDoToiDa NVARCHAR(50) NULL,     -- Tốc độ tối đa
    CONSTRAINT FK_DongCo_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);
--6.3. Tạo bảng kichhs thước trọng lượng để dễ dàng so sánh
CREATE TABLE KichThuocTrongLuong (
    MaKT INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    KichThuoc NVARCHAR(100) NULL,          -- Dài x Rộng x Cao
    ChieuDaiCoSo NVARCHAR(50) NULL,        -- Chiều dài cơ sở
    TrongLuong NVARCHAR(50) NULL,          -- Trọng lượng (kg)
    DungTichKhoangHanhLy NVARCHAR(50) NULL,-- Khoang hành lý (L)
    CONSTRAINT FK_KichThuocTrongLuong_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
); 
--6.4. Tạo bảng tính năng để dễ dàng so sánh tính năng giữa các xe
CREATE TABLE TinhNangXe (
    MaTinhNang INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,

    -- Hệ truyền động
    HeDanDong NVARCHAR(50) NULL,    -- FWD, RWD, AWD

    -- Hệ thống an toàn
    PhanhABS BIT NULL,              -- Chống bó cứng phanh
    TuiKhi BIT NULL,                -- Túi khí
    CanhBaoVaCham BIT NULL,         -- Cảnh báo va chạm
    HoTroGiuLanDuong BIT NULL,      -- Hỗ trợ giữ làn
    Camera360 BIT NULL,             -- Camera 360 độ
    CruiseControl BIT NULL,         -- Kiểm soát hành trình

    -- Hệ thống giải trí & tiện nghi
    ManHinh NVARCHAR(50) NULL,      -- Kích thước màn hình
    AmThanh NVARCHAR(50) NULL,      -- Âm thanh (Bose, B&O…)
    KetNoiBluetooth BIT NULL,       
    HoTroAppleCarPlay BIT NULL,     
    HoTroAndroidAuto BIT NULL,      
    DieuHoaTuDong BIT NULL,         
    GheDa BIT NULL,                 
    GheNgoiCoSuoi BIT NULL,         

    -- Hệ thống sạc (xe điện)
    LoaiSac NVARCHAR(50) NULL,      
    CongSuatSac NVARCHAR(50) NULL,  
    ThoiGianSacDay NVARCHAR(50) NULL,  
    ThoiGianSacNhanh NVARCHAR(50) NULL, 
    CongSacChuan NVARCHAR(50) NULL,     

    CONSTRAINT FK_TinhNangXe_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT UQ_TinhNangXe UNIQUE (MaXe)  -- Mỗi xe chỉ có 1 bộ tính năng
);

--7. Tạo bảng kho tổng từ hãng xe
CREATE TABLE TonKhoXe (
    MaTonKho INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    SoLuong INT NOT NULL CHECK (SoLuong >= 0),
    NgayCapNhat DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_TonKhoXe_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT UQ_TonKhoXe UNIQUE (MaXe) -- Mỗi xe chỉ có 1 bản ghi trong kho tổng
);

--8. Tạo bảng lưu trữ thông tin khách hàng
CREATE TABLE KhachHang (
    MaKhachHang INT PRIMARY KEY IDENTITY(1,1),
    HoTen NVARCHAR(100) NOT NULL,
    SoDienThoai NVARCHAR(20) UNIQUE,
    Email NVARCHAR(100) UNIQUE,
    DiaChi NVARCHAR(200),
    NgayTao DATETIME DEFAULT GETDATE()
);

--9. Tạo bảng lịch hẹn 
CREATE TABLE LichHenLaiThu (
    MaLichHen INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT NOT NULL,
    MaDaiLy INT NOT NULL,
    MaXe INT NOT NULL,
    NgayHen DATETIME NOT NULL,
	GioHen TIME NOT NULL,
	MaNhanVien INT NOT NULL,            -- Nhân viên phụ trách
    GhiChu NVARCHAR(200) NULL,      -- Ghi chú thêm từ khách hàng
    TrangThai NVARCHAR(50) DEFAULT N'Đã lên lịch', -- Đã lên lịch, Hoàn thành, Hủy

    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_LichHen_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
    CONSTRAINT FK_LichHen_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
	CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVienDaiLy(MaNhanVien)
);

--10. Tạo bảng đơn hàng
CREATE TABLE DonHang (
    MaDonHang INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT NOT NULL,
    MaDaiLy INT NOT NULL,
	MaNhanVien INT NOT NULL,
    NgayDat DATETIME DEFAULT GETDATE() NOT NULL,
	TongTien DECIMAL(18,2) NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Chờ xử lý', -- Chờ xử lý, Xác nhận, Đang giao, Hoàn thành, Hủy
	TinhTrangGiaoXe NVARCHAR(100) DEFAULT N'Chưa giao', --Chưa giao, Đã giao, Đang giao

    CONSTRAINT FK_DonHang_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_DonHang_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
	CONSTRAINT FK_DonHang_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NhanVienDaiLy(MaNhanVien)
);

--11. Tạo bảng chi tiết đơn hàng
CREATE TABLE ChiTietDonHang (
    MaChiTiet INT PRIMARY KEY IDENTITY(1,1),
    MaDonHang INT NOT NULL,
    MaXe INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ChietKhau DECIMAL(18,2) DEFAULT 0,

    CONSTRAINT FK_ChiTietDonHang_DonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    CONSTRAINT FK_ChiTietDonHang_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
	CONSTRAINT UQ_ChiTietDonHang UNIQUE (MaDonHang, MaXe)
);

--12. Tạo bảng phản hồi để lưu thông tin phản hồi từ khách hàng
CREATE TABLE PhanHoi (
    MaPhanHoi INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT NOT NULL,
    MaDaiLy INT NOT NULL,
	MaNhanVienXuLy INT NULL,                      -- Nhân viên nào xử lý phản hồi
	MaDonHang INT NULL,                           -- Phản hồi liên quan đến đơn hàng nào (nếu có)
    NoiDung NVARCHAR(MAX) NOT NULL,
    NgayTao DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chưa xử lý', -- Chưa xử lý, Đã xử lý, Đang xử lý

    CONSTRAINT FK_PhanHoi_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_PhanHoi_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
	CONSTRAINT FK_PhanHoi_DonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    CONSTRAINT FK_PhanHoi_NhanVien FOREIGN KEY (MaNhanVienXuLy) REFERENCES NguoiDung(MaNguoiDung)
);


--13. Tạo bảng lưu thông tin thanh toán
CREATE TABLE ThanhToan (
    MaThanhToan INT PRIMARY KEY IDENTITY(1,1),
    MaDonHang INT NOT NULL,
    SoTien DECIMAL(18,2) NOT NULL,
    NgayThanhToan DATETIME DEFAULT GETDATE(),
    PhuongThuc NVARCHAR(50) NOT NULL, -- Tiền mặt, Chuyển khoản, Trả góp,
	TrangThai NVARCHAR(50) DEFAULT N'Thành công' --Thành công, Chờ xác nhận, Hoàn tiền

    CONSTRAINT FK_ThanhToan_DonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang)
);

--14. Tạo bảng chi tiết trả góp 
CREATE TABLE TraGop (
    MaTraGop INT PRIMARY KEY IDENTITY(1,1),
    MaDonHang INT NOT NULL,
    KyHan INT NOT NULL,                      -- Số tháng (12, 24, 36…)
    SoTienTraHangThang DECIMAL(18,2) NOT NULL,
    LaiSuat DECIMAL(5,2) NOT NULL,           -- %
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Đang trả',  -- Đang trả, Hoàn thành, Quá hạn
    CONSTRAINT FK_TraGop_DonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang)
);

--15. Tạo bảng quản lý chi tiết các kỳ thanh toán
CREATE TABLE LichTraGop (
    MaLichTra INT PRIMARY KEY IDENTITY(1,1),
    MaTraGop INT NOT NULL,
    KyThu INT NOT NULL,                     -- Kỳ thứ mấy (1, 2, 3…)
    NgayDenHan DATE NOT NULL,
    SoTienPhaiTra DECIMAL(18,2) NOT NULL,
    NgayThanhToan DATE NULL,                -- Ngày KH thực sự thanh toán
    TrangThai NVARCHAR(50) DEFAULT N'Chưa thanh toán', -- Chưa, Đã thanh toán, Quá hạn
    CONSTRAINT FK_LichTraGop_TraGop FOREIGN KEY (MaTraGop) REFERENCES TraGop(MaTraGop)
);

--16. Tạo bảng thông tin khuyến mãi để quản lý tất cả các khuyến mãi bao gồm cả khuyến mãi trực tiếp trên xe
-- và voucher khuyến mãi
CREATE TABLE KhuyenMai (
    MaKhuyenMai INT PRIMARY KEY IDENTITY(1,1),
    MaDaiLy INT NULL,
    TenKhuyenMai NVARCHAR(100) NOT NULL,   -- Tên chương trình (VD: Khuyến mãi Tết 2025)
    MoTa NVARCHAR(MAX),                    -- Mô tả chi tiết
    NgayBatDau DATE NOT NULL,              -- Thời gian hiệu lực
    NgayKetThuc DATE NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Hoạt động', -- Hoạt động / Hết hạn / Tạm dừng

    CONSTRAINT FK_KM_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);


--17. Tạo bảng quản lý khuyến mãi trực tiếp trên xe
CREATE TABLE XeKhuyenMai (
    MaXeKhuyenMai INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    MaKhuyenMai INT NOT NULL,
    PhanTramGiam DECIMAL(5,2) NULL,     -- % giảm giá
    SoTienGiam DECIMAL(18,2) NULL,      -- giảm số tiền VNĐ
    GhiChu NVARCHAR(200),

    CONSTRAINT FK_XeKM_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT FK_XeKM_KM FOREIGN KEY (MaKhuyenMai) REFERENCES KhuyenMai(MaKhuyenMai)
);

--18. Tạo bảng quản lý voucher khuyến mãi
CREATE TABLE Voucher (
    MaVoucher INT PRIMARY KEY IDENTITY(1,1),
    MaKhuyenMai INT NOT NULL,                 -- Liên kết đến chương trình khuyến mãi gốc
    MaKhachHang INT NULL,                     -- Nếu NULL: ai cũng dùng được, nếu có: KH đó mới dùng
    MaCode NVARCHAR(50) UNIQUE NOT NULL,      -- Mã code (VD: SUMMER2025)
    SoLanSuDungToiDa INT DEFAULT 1,           -- Giới hạn số lần dùng
    SoLanDaSuDung INT DEFAULT 0,              -- Đếm số lần đã dùng
    PhanTramGiam DECIMAL(5,2) NULL,           -- % giảm giá
    SoTienGiam DECIMAL(18,2) NULL,            -- giảm cố định (VNĐ)
    TrangThai NVARCHAR(50) DEFAULT N'Hoạt động',

    CONSTRAINT FK_Voucher_KM FOREIGN KEY (MaKhuyenMai) REFERENCES KhuyenMai(MaKhuyenMai),
    CONSTRAINT FK_Voucher_KH FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang)
);

--19. Tạo bảng lưu thông tin phân phối từ hãng xe cho đại lý
CREATE TABLE LenhPhanPhoi (
    MaLenh INT PRIMARY KEY IDENTITY(1,1),
    MaDaiLy INT NOT NULL,                           -- Phân phối cho đại lý nào
    NguoiTao INT NOT NULL,                          -- EVM staff tạo lệnh
    NgayTao DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Đang xử lý',   -- Đang xử lý, Đã giao, Hủy
    
    CONSTRAINT FK_PhanPhoi_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
    CONSTRAINT FK_PhanPhoi_NguoiDung FOREIGN KEY (NguoiTao) REFERENCES NguoiDung(MaNguoiDung)
);

--20. Tạo bảng chi tiết phân phối để lưu chi tiết lệnh phân phân phối
CREATE TABLE ChiTietPhanPhoi (
    MaChiTiet INT PRIMARY KEY IDENTITY(1,1),
    MaLenh INT NOT NULL,
    MaXe INT NOT NULL,
    SoLuong INT NOT NULL CHECK (SoLuong > 0),

    CONSTRAINT FK_CTPhanPhoi_Lenh FOREIGN KEY (MaLenh) REFERENCES LenhPhanPhoi(MaLenh),
    CONSTRAINT FK_CTPhanPhoi_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);
