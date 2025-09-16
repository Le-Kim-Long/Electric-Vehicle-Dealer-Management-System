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

--Bảng khu vực đại lý (thành phố)
CREATE TABLE KhuVuc (
    MaKhuVuc INT PRIMARY KEY IDENTITY(1,1),
    TenKhuVuc NVARCHAR(100) NOT NULL UNIQUE, -- Ví dụ: Hà Nội, TP.HCM, Đà Nẵng
    MoTa NVARCHAR(255) NULL
);

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
    NgayTao DATETIME DEFAULT GETDATE(),

    MaKhuVuc INT NOT NULL,
    CONSTRAINT FK_DaiLy_KhuVuc FOREIGN KEY (MaKhuVuc) REFERENCES KhuVuc(MaKhuVuc)
);

--Tạo bảng vai trò người dùng
CREATE TABLE VaiTro (
    MaVaiTro INT PRIMARY KEY IDENTITY(1,1),
    TenVaiTro NVARCHAR(50) NOT NULL UNIQUE,   -- Admin / EVMStaff / DaiLy / KhachHang
    MoTa NVARCHAR(255) NULL
);

--3. Tạo bảng người dùng
CREATE TABLE NguoiDung (
    MaNguoiDung INT PRIMARY KEY IDENTITY(1,1),
    TenDangNhap NVARCHAR(50) NOT NULL UNIQUE,
    MatKhau NVARCHAR(255) NOT NULL,            -- lưu mật khẩu hash
    HoTen NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100) NULL,
    SoDienThoai NVARCHAR(20) NULL,
    DiaChi NVARCHAR(255) NULL,

    MaVaiTro INT NOT NULL,                     -- liên kết đến bảng VaiTro
    TrangThai NVARCHAR(50) DEFAULT N'Hoạt động', -- Hoạt động / Khóa

    NgayTao DATETIME DEFAULT GETDATE(),
    NgayCapNhat DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_NguoiDung_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro)
);

--4. Tạo bảng xe
CREATE TABLE Xe (
    MaXe INT PRIMARY KEY IDENTITY(1,1),
	MaDaiLy INT NOT NULL,
    TenXe NVARCHAR(100) NOT NULL,       -- Tên mẫu xe (VD: VinFast VF e34)
    PhienBan NVARCHAR(50),              -- Phiên bản (Standard, Premium,...)
    MauSac NVARCHAR(50),                -- Màu sắc
    GiaBan DECIMAL(18,2),               -- Giá bán niêm yết 
	XuatXu NVARCHAR(50),
	NamSanXuat INT NULL,
	TrangThai NVARCHAR(50) DEFAULT N'Đang bán' -- Đang bán, Ngừng bán

	CONSTRAINT FK_Xe_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);

--5. Tạo bảng cấu hình xe
CREATE TABLE CauHinhXe (
    MaCauHinh INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,

    -- Pin
    DungLuongPin DECIMAL(5,2) NULL,        -- kWh
    LoaiPin NVARCHAR(50) NULL,             -- Lithium-ion, LFP...
    QuangDuong INT NULL,                   -- km

    -- Động cơ
    CongSuat NVARCHAR(50) NULL,            -- Công suất động cơ
    MoMenXoan NVARCHAR(50) NULL,           -- Mô-men xoắn
    SoDongCo NVARCHAR(50) NULL,            -- Số động cơ (single/dual motor)
    TangToc NVARCHAR(50) NULL,             -- Tăng tốc 0–100 km/h
    TocDoToiDa NVARCHAR(50) NULL,          -- Tốc độ tối đa

    -- Kích thước & trọng lượng
    KichThuoc NVARCHAR(100) NULL,          -- Dài x Rộng x Cao
    ChieuDaiCoSo NVARCHAR(50) NULL,        -- Chiều dài cơ sở
    TrongLuong NVARCHAR(50) NULL,          -- Trọng lượng (kg)
    DungTichKhoangHanhLy NVARCHAR(50) NULL,-- Khoang hành lý (L)

    CONSTRAINT FK_CauHinh_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

--6. Tạo bảng tính năng xe
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

--9. Tạo bảng lịch hẹn 
CREATE TABLE LichHenLaiThu (
    MaLichHenLaiThu INT PRIMARY KEY IDENTITY(1,1),
    NguoiDatHen INT NOT NULL,                  -- FK: khách hàng đặt lịch
    NhanVienPhuTrach INT NULL,                 -- FK: nhân viên phụ trách
    MaXe INT NOT NULL,                         -- Xe muốn lái thử
    MaDaiLy INT NOT NULL,                      -- Đại lý tổ chức
    NgayHen DATE NOT NULL,
    GioHen TIME NOT NULL,
    HinhThucHen NVARCHAR(50) DEFAULT N'Tại đại lý',
    YeuCauKhachHang NVARCHAR(200) NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Đã lên lịch', -- Đã lên lịch, Xác nhận, Hoàn thành, Hủy
    NgayTao DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_LichHenLaiThu_NguoiDat FOREIGN KEY (NguoiDatHen) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT FK_LichHenLaiThu_NhanVien FOREIGN KEY (NhanVienPhuTrach) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT FK_LichHenLaiThu_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT FK_LichHenLaiThu_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);
--Tạo bảng lịch hẹn xem xe
CREATE TABLE LichHenXemXe (
    MaLichHenXemXe INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT NOT NULL,
    MaDaiLy INT NOT NULL,
    MaXe INT NULL,                  -- KH có thể chọn mẫu xe muốn xem/lái thử
    NgayHen DATE NOT NULL,
    GioHen TIME NOT NULL,
    MaNhanVien INT NULL,            -- Nhân viên phụ trách tiếp khách
    GhiChu NVARCHAR(200) NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Đã lên lịch',  
    -- Đã lên lịch, Đã xác nhận, Hoàn thành, Hủy

    CONSTRAINT FK_LichHenXemXe_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT FK_LichHenXemXe_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
    CONSTRAINT FK_LichHenXemXe_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT FK_LichHenXemXe_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NguoiDung(MaNguoiDung)
);

--10. Tạo bảng đơn hàng
CREATE TABLE DonHang (
    MaDonHang INT PRIMARY KEY IDENTITY(1,1),
    NguoiDatHang INT NOT NULL,                 -- Khách hàng mua xe
    NhanVienPhuTrach INT NULL,                 -- Nhân viên phụ trách xử lý đơn
    MaDaiLy INT NOT NULL,                      -- Đại lý bán xe
	MaLichHen INT NULL,
    NgayDat DATETIME NOT NULL DEFAULT GETDATE(),
    TongTien DECIMAL(18,2) NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Chờ xử lý',   -- Chờ xử lý, Xác nhận, Đang giao, Hoàn thành, Hủy
    TinhTrangGiaoXe NVARCHAR(100) DEFAULT N'Chưa giao', -- Chưa giao, Đã giao, Đang giao
    NgayCapNhat DATETIME NULL,

    CONSTRAINT FK_DonHang_Khach FOREIGN KEY (NguoiDatHang) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT FK_DonHang_NhanVien FOREIGN KEY (NhanVienPhuTrach) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT FK_DonHang_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
	CONSTRAINT FK_DonHang_LichHenXemXe FOREIGN KEY (MaLichHen) REFERENCES LichHenXemXe(MaLichHenXemXe)
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
    MaNguoiDung INT NOT NULL,                        -- Người gửi phản hồi (thường là khách hàng)
    MaDaiLy INT NOT NULL,                            -- Đại lý liên quan
    MaDonHang INT NULL,                              -- Phản hồi gắn với đơn hàng (nếu có)
    MaNhanVienXuLy INT NULL,                         -- Nhân viên tiếp nhận/xử lý
    NoiDung NVARCHAR(MAX) NOT NULL,                  -- Nội dung phản hồi
    NgayTao DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chưa xử lý',    -- Chưa xử lý, Đang xử lý, Đã xử lý

    CONSTRAINT FK_PhanHoi_NguoiDung FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT FK_PhanHoi_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
    CONSTRAINT FK_PhanHoi_DonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang),
    CONSTRAINT FK_PhanHoi_NhanVien FOREIGN KEY (MaNhanVienXuLy) REFERENCES NguoiDung(MaNguoiDung)
);

-- Tạo bảng lưu lịch sử phản hồi
CREATE TABLE LichSuXuLyPhanHoi (
    MaLichSu INT PRIMARY KEY IDENTITY(1,1),
    MaPhanHoi INT NOT NULL,
    MaNhanVien INT NOT NULL,
    NgayXuLy DATETIME DEFAULT GETDATE(),
    NoiDungXuLy NVARCHAR(MAX),
    TrangThaiSauXuLy NVARCHAR(50),  -- Trạng thái cập nhật sau khi xử lý

    CONSTRAINT FK_LichSu_PhanHoi FOREIGN KEY (MaPhanHoi) REFERENCES PhanHoi(MaPhanHoi),
    CONSTRAINT FK_LichSu_NhanVien FOREIGN KEY (MaNhanVien) REFERENCES NguoiDung(MaNguoiDung)
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

--16. Tạo bảng thông tin khuyến mãi để quản lý khuyến mãi trực tiếp trên xe
CREATE TABLE KhuyenMaiXe (
    MaKhuyenMaiXe INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,                           -- Xe nào được áp dụng
    MaDaiLy INT NULL,                            -- Nếu NULL thì áp dụng toàn hệ thống, nếu có thì chỉ áp dụng cho đại lý đó
    TenKhuyenMai NVARCHAR(100) NOT NULL,         -- Tên chương trình (VD: Khuyến mãi Tết 2025)
    MoTa NVARCHAR(MAX),                          -- Mô tả chi tiết
    PhanTramGiam DECIMAL(5,2) NULL,              -- % giảm giá
    SoTienGiam DECIMAL(18,2) NULL,               -- giảm cố định (VNĐ)
    NgayBatDau DATE NOT NULL,                    -- Ngày hiệu lực
    NgayKetThuc DATE NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Hoạt động', -- Hoạt động / Hết hạn / Tạm dừng
    GhiChu NVARCHAR(200),

    CONSTRAINT FK_KM_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT FK_KM_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);

--17. Tạo bảng lưu thông tin phân phối từ hãng xe cho đại lý
CREATE TABLE LenhPhanPhoi (
    MaLenh INT PRIMARY KEY IDENTITY(1,1),
    MaDaiLy INT NOT NULL,                           -- Phân phối cho đại lý nào
    NguoiTao INT NOT NULL,                          -- EVM staff tạo lệnh
    NgayTao DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Đang xử lý',   -- Đang xử lý, Đã giao, Hủy
    
    CONSTRAINT FK_PhanPhoi_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
    CONSTRAINT FK_PhanPhoi_NguoiDung FOREIGN KEY (NguoiTao) REFERENCES NguoiDung(MaNguoiDung)
);

--18. Tạo bảng chi tiết phân phối để lưu chi tiết lệnh phân phân phối
CREATE TABLE ChiTietPhanPhoi (
    MaChiTiet INT PRIMARY KEY IDENTITY(1,1),
    MaLenh INT NOT NULL,
    MaXe INT NOT NULL,
    SoLuong INT NOT NULL CHECK (SoLuong > 0),

    CONSTRAINT FK_CTPhanPhoi_Lenh FOREIGN KEY (MaLenh) REFERENCES LenhPhanPhoi(MaLenh),
    CONSTRAINT FK_CTPhanPhoi_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);
