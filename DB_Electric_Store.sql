use MASTER
go
-- 0. Xóa Database RentalDB nếu đã tồn tại trong DBMS --------------------
IF EXISTS (
    SELECT name 
    FROM sys.databases 
    WHERE name = N'DB_Electric_Store'
)
BEGIN
    -- Tùy chọn: đưa DB về SINGLE_USER để tránh lỗi đang có kết nối
    ALTER DATABASE [DB_Electric_Store] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    
    -- Xóa database
    DROP DATABASE [DB_Electric_Store];
END

go
-- 1. Tạo database
CREATE DATABASE DB_Electric_Store;
go

USE DB_Electric_Store;
go
--2. Tạo bảng người dùng
CREATE TABLE NguoiDung (
    MaNguoiDung INT PRIMARY KEY IDENTITY(1,1),
    TenDangNhap NVARCHAR(50) UNIQUE NOT NULL,
    MatKhau NVARCHAR(255) NOT NULL,
    HoTen NVARCHAR(100),
    Email NVARCHAR(100),
    SoDienThoai NVARCHAR(20),
    VaiTro NVARCHAR(50) NOT NULL, -- Admin, NhanVienDaiLy, QuanLyDaiLy, NhanVienHang
    MaDaiLy INT NULL,
    NgayTao DATETIME DEFAULT GETDATE()
);

--3. Tạo bảng đại lý đại lý
CREATE TABLE DaiLy (
    MaDaiLy INT PRIMARY KEY IDENTITY(1,1),
    TenDaiLy NVARCHAR(100) NOT NULL,
    DiaChi NVARCHAR(200),
    SoDienThoai NVARCHAR(20),
    Email NVARCHAR(100),
    SoHopDong NVARCHAR(50),
    ChiTieuDoanhSo DECIMAL(18,2),
    HanMucCongNo DECIMAL(18,2),
    NgayTao DATETIME DEFAULT GETDATE()
);

--4. Tạo bảng hảng xe
CREATE TABLE HangXe (
    MaHangXe INT PRIMARY KEY IDENTITY(1,1),
    TenHangXe NVARCHAR(100) NOT NULL,
    DiaChi NVARCHAR(200),
    SoDienThoai NVARCHAR(20),
    Email NVARCHAR(100)
);

--5. Tạo bảng xe
CREATE TABLE Xe (
    MaXe INT PRIMARY KEY IDENTITY(1,1),
	MaHangXe INT NOT NULL,
    TenXe NVARCHAR(100) NOT NULL,       -- Tên mẫu xe (VD: VinFast VF e34)
    PhienBan NVARCHAR(50),              -- Phiên bản (Standard, Premium,...)
    MauSac NVARCHAR(50),                -- Màu sắc
    GiaBan DECIMAL(18,2),               -- Giá bán niêm yết 
	XuatXu NVARCHAR(50),
	NamSanXuat INT NULL,
	CONSTRAINT FK_Xe_HangXe FOREIGN KEY (MaHangXe) REFERENCES HangXe(MaHangXe)
);

----5.1 Tạo các bảng cấu hình xe
CREATE TABLE PinXe (
    MaPin INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    PinDungLuong NVARCHAR(50) NULL,    -- Dung lượng pin (VD: 75 kWh)
    LoaiPin NVARCHAR(50) NULL,         -- Lithium-ion, LFP...
    QuangDuong NVARCHAR(50) NULL,      -- Quãng đường 1 lần sạc
    CONSTRAINT FK_Pin_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

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

CREATE TABLE KichThuocTrongLuong (
    MaKT INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    KichThuoc NVARCHAR(100) NULL,          -- Dài x Rộng x Cao
    ChieuDaiCoSo NVARCHAR(50) NULL,        -- Chiều dài cơ sở
    TrongLuong NVARCHAR(50) NULL,          -- Trọng lượng (kg)
    DungTichKhoangHanhLy NVARCHAR(50) NULL,-- Khoang hành lý (L)
    CONSTRAINT FK_KichThuocTrongLuong_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
); 

CREATE TABLE HeTruyenDong (
    MaHTD INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    HeDanDong NVARCHAR(50) NULL,   -- FWD, RWD, AWD
    HopSo NVARCHAR(50) NULL,       -- 1 cấp, số tay, tự động
    CONSTRAINT FK_HeTruyenDong_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);
--5.2 Tạo các bảng tính năng xe
CREATE TABLE HeThongAnToan (
    MaAT INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    PhanhABS BIT NULL,           -- Chống bó cứng phanh
    TuiKhi BIT NULL,             -- Túi khí
    CanhBaoVaCham BIT NULL,      -- Cảnh báo va chạm
    HoTroGiuLanDuong BIT NULL,   -- Hỗ trợ giữ làn
    Camera360 BIT NULL,          -- Camera 360 độ
    CruiseControl BIT NULL,      -- Kiểm soát hành trình
    CONSTRAINT FK_HeThongAnToan_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

CREATE TABLE HeThongGiaiTri (
    MaGT INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    ManHinh NVARCHAR(50) NULL,       -- Kích thước màn hình
    AmThanh NVARCHAR(50) NULL,       -- Hệ thống âm thanh (Bose, Bang & Olufsen…)
    KetNoiBluetooth BIT NULL,        -- Hỗ trợ Bluetooth
    HoTroAppleCarPlay BIT NULL,      -- Apple CarPlay
    HoTroAndroidAuto BIT NULL,       -- Android Auto
    DieuHoaTuDong BIT NULL,          -- Điều hòa tự động
    GheDa BIT NULL,                  -- Ghế da
    GheNgoiCoSuoi BIT NULL,          -- Ghế ngồi có sưởi
    CONSTRAINT FK_HeThongGiaiTri_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

CREATE TABLE HeThongSac (
    MaSac INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    LoaiSac NVARCHAR(50) NULL,         -- AC, DC, sạc nhanh, sạc chậm
    CongSuatSac NVARCHAR(50) NULL,     -- Công suất sạc (kW)
    ThoiGianSacDay NVARCHAR(50) NULL,  -- Thời gian sạc đầy (VD: 8h)
    ThoiGianSacNhanh NVARCHAR(50) NULL,-- Thời gian sạc nhanh (VD: 30 phút 80%)
    CongSacChuan NVARCHAR(50) NULL,    -- Chuẩn sạc (CCS, CHAdeMO, Tesla…)
    CONSTRAINT FK_HeThongSac_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

--6. Tạo bảng tồn kho của đại lý
CREATE TABLE TonKhoXe (
    MaTonKho INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,
    MaDaiLy INT NOT NULL,
    SoLuong INT NOT NULL,
    NgayCapNhat DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_TonKhoXe_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT FK_TonKhoXe_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);

--7. Tạo bảng lưu trữ thông tin khách hàng
CREATE TABLE KhachHang (
    MaKhachHang INT PRIMARY KEY IDENTITY(1,1),
    HoTen NVARCHAR(100) NOT NULL,
    SoDienThoai NVARCHAR(20),
    Email NVARCHAR(100),
    DiaChi NVARCHAR(200),
    NgayTao DATETIME DEFAULT GETDATE()
);

--8. Tạo bảng lịch hẹn 
CREATE TABLE LichHenLaiThu (
    MaLichHen INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT NOT NULL,
    MaDaiLy INT NOT NULL,
    MaXe INT NOT NULL,
    NgayHen DATETIME NOT NULL,
    TrangThai NVARCHAR(50) DEFAULT N'Đã lên lịch', -- Đã lên lịch, Hoàn thành, Hủy

    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_LichHen_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
    CONSTRAINT FK_LichHen_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

--9. Tạo bảng phản hồi
CREATE TABLE PhanHoi (
    MaPhanHoi INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT NOT NULL,
    MaDaiLy INT NOT NULL,
    NoiDung NVARCHAR(MAX) NOT NULL,
    NgayTao DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chưa xử lý', -- Chưa xử lý, Đã xử lý

    CONSTRAINT FK_PhanHoi_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_PhanHoi_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
);
--10. Tạo bảng đơn hàng
CREATE TABLE DonHang (
    MaDonHang INT PRIMARY KEY IDENTITY(1,1),
    MaKhachHang INT NOT NULL,
    MaDaiLy INT NOT NULL,
    NgayDat DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) DEFAULT N'Chờ xử lý', -- Chờ xử lý, Xác nhận, Đang giao, Hoàn thành, Hủy

    CONSTRAINT FK_DonHang_KhachHang FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    CONSTRAINT FK_DonHang_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy)
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
    CONSTRAINT FK_ChiTietDonHang_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

--12. Tạo bảng phương thức thanh toán
CREATE TABLE ThanhToan (
    MaThanhToan INT PRIMARY KEY IDENTITY(1,1),
    MaDonHang INT NOT NULL,
    SoTien DECIMAL(18,2) NOT NULL,
    NgayThanhToan DATETIME DEFAULT GETDATE(),
    PhuongThuc NVARCHAR(50) NOT NULL, -- Tiền mặt, Chuyển khoản, Trả góp

    CONSTRAINT FK_ThanhToan_DonHang FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang)
);

--13. Tạo bảng thông tin khuyến mãi để quản lý tất cả các khuyến mãi bao gồm cả khuyến mãi trực tiếp trên xe
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


--13.1 Tạo bảng quản lý khuyến mãi trực tiếp trên xe
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

--13.2 Tạo bảng quản lý voucher khuyến mãi
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