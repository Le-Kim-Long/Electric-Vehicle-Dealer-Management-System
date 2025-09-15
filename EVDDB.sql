USE master
CREATE DATABASE EVDealerDB
GO
USE EVDealerDB;
GO

-- =======================
-- NGƯỜI DÙNG & ĐẠI LÝ
-- =======================
CREATE TABLE NguoiDung (
    NguoiDungID INT IDENTITY PRIMARY KEY,
    TenDangNhap NVARCHAR(50) UNIQUE NOT NULL,
    MatKhau NVARCHAR(255) NOT NULL,
    HoTen NVARCHAR(100),
    VaiTro NVARCHAR(50), -- DaiLyNhanVien, DaiLyQuanLy, HangNhanVien, Admin
    DaiLyID INT NULL,
    TrangThai BIT DEFAULT 1
);

CREATE TABLE DaiLy (
    DaiLyID INT IDENTITY PRIMARY KEY,
    TenDaiLy NVARCHAR(100),
    KhuVuc NVARCHAR(50),
    SoHopDong NVARCHAR(50),
    ChiTieuDoanhSo INT,
    CongNo DECIMAL(18,2) DEFAULT 0
);

-- =======================
-- SẢN PHẨM & KHO
-- =======================
CREATE TABLE XeDien (
    XeID INT IDENTITY PRIMARY KEY,
    Mau NVARCHAR(100),
    PhienBan NVARCHAR(100),
    MauSac NVARCHAR(50),
    GiaBanCoBan DECIMAL(18,2),
    TinhNang NVARCHAR(MAX)
);

CREATE TABLE TonKho (
    TonKhoID INT IDENTITY PRIMARY KEY,
    DaiLyID INT FOREIGN KEY REFERENCES DaiLy(DaiLyID),
    XeID INT FOREIGN KEY REFERENCES XeDien(XeID),
    SoLuong INT,
    NgayCapNhat DATETIME DEFAULT GETDATE()
);

CREATE TABLE ChinhSachGia (
    GiaID INT IDENTITY PRIMARY KEY,
    DaiLyID INT FOREIGN KEY REFERENCES DaiLy(DaiLyID),
    XeID INT FOREIGN KEY REFERENCES XeDien(XeID),
    GiaSi DECIMAL(18,2),
    TyLeChietKhau DECIMAL(5,2),
    KhuyenMai NVARCHAR(255)
);

-- =======================
-- KHÁCH HÀNG & BÁN HÀNG
-- =======================
CREATE TABLE KhachHang (
    KhachHangID INT IDENTITY PRIMARY KEY,
    HoTen NVARCHAR(100),
    DienThoai NVARCHAR(20),
    Email NVARCHAR(100),
    DiaChi NVARCHAR(255)
);

CREATE TABLE LaiThu (
    LaiThuID INT IDENTITY PRIMARY KEY,
    KhachHangID INT FOREIGN KEY REFERENCES KhachHang(KhachHangID),
    DaiLyID INT FOREIGN KEY REFERENCES DaiLy(DaiLyID),
    XeID INT FOREIGN KEY REFERENCES XeDien(XeID),
    NgayHen DATETIME,
    PhanHoi NVARCHAR(MAX)
);

CREATE TABLE DonHang (
    DonHangID INT IDENTITY PRIMARY KEY,
    DaiLyID INT FOREIGN KEY REFERENCES DaiLy(DaiLyID),
    KhachHangID INT FOREIGN KEY REFERENCES KhachHang(KhachHangID),
    XeID INT FOREIGN KEY REFERENCES XeDien(XeID),
    NgayDat DATETIME DEFAULT GETDATE(),
    TrangThai NVARCHAR(50) -- BaoGia, ChoDuyet, HopDong, DaGiao
);

CREATE TABLE ThanhToan (
    ThanhToanID INT IDENTITY PRIMARY KEY,
    DonHangID INT FOREIGN KEY REFERENCES DonHang(DonHangID),
    HinhThuc NVARCHAR(50), -- TraThang, TraGop
    SoTien DECIMAL(18,2),
    NgayThanhToan DATETIME DEFAULT GETDATE()
);

CREATE TABLE GiaoXe (
    GiaoXeID INT IDENTITY PRIMARY KEY,
    DonHangID INT FOREIGN KEY REFERENCES DonHang(DonHangID),
    TinhTrang NVARCHAR(50), -- ChoGiao, DangGiao, DaGiao
    NgayGiao DATETIME NULL
);

-- =======================
-- BÁO CÁO
-- =======================
CREATE TABLE BaoCao (
    BaoCaoID INT IDENTITY PRIMARY KEY,
    DaiLyID INT FOREIGN KEY REFERENCES DaiLy(DaiLyID),
    LoaiBaoCao NVARCHAR(50), -- DoanhSo, CongNo, TonKho
    NgayBaoCao DATETIME DEFAULT GETDATE(),
    NoiDung NVARCHAR(MAX)
);




--Testing
-- Đại lý
INSERT INTO DaiLy (TenDaiLy, KhuVuc, SoHopDong, ChiTieuDoanhSo, CongNo)
VALUES 
(N'Dai Ly Ha Noi', N'Ha Noi', 'HD001', 100, 50000000),
(N'Dai Ly HCM', N'TP HCM', 'HD002', 120, 30000000),
(N'Dai Ly Da Nang', N'Da Nang', 'HD003', 80, 20000000);

-- Người dùng
INSERT INTO NguoiDung (TenDangNhap, MatKhau, HoTen, VaiTro, DaiLyID)
VALUES
('nv_hn', '123456', N'Nguyen Van A', N'DaiLyNhanVien', 1),
('ql_hn', '123456', N'Tran Thi B', N'DaiLyQuanLy', 1),
('nv_hcm', '123456', N'Le Van C', N'DaiLyNhanVien', 2),
('ql_hcm', '123456', N'Pham Thi D', N'DaiLyQuanLy', 2),
('nv_dn', '123456', N'Do Van E', N'DaiLyNhanVien', 3),
('admin', 'admin123', N'Admin He Thong', N'Admin', NULL);
-- Xe điện
INSERT INTO XeDien (Mau, PhienBan, MauSac, GiaBanCoBan, TinhNang)
VALUES
(N'Model X', N'2024', N'Trang', 900000000, N'Pin 80kWh, Tu dong'),
(N'Model Y', N'2024', N'Den', 700000000, N'Pin 60kWh, Che do Eco'),
(N'Model Z', N'2024', N'Do', 600000000, N'Pin 50kWh, Gia re');

-- Chính sách giá
INSERT INTO ChinhSachGia (DaiLyID, XeID, GiaSi, TyLeChietKhau, KhuyenMai)
VALUES
(1, 1, 850000000, 5, N'Khuyen mai 10tr'),
(1, 2, 670000000, 3, N'Tang goi bao duong 1 nam'),
(2, 3, 580000000, 2, N'Khuyen mai 5tr'),
(3, 1, 860000000, 4, N'Ho tro vay tra gop 0%');
-- Tồn kho đại lý
INSERT INTO TonKho (DaiLyID, XeID, SoLuong)
VALUES
(1, 1, 5),  -- Hà Nội có 5 xe Model X
(1, 2, 10), -- Hà Nội có 10 xe Model Y
(2, 2, 7),  -- HCM có 7 xe Model Y
(2, 3, 12), -- HCM có 12 xe Model Z
(3, 1, 3),  -- Đà Nẵng có 3 xe Model X
(3, 3, 6);  -- Đà Nẵng có 6 xe Model Z
-- Khách hàng
INSERT INTO KhachHang (HoTen, DienThoai, Email, DiaChi)
VALUES
(N'Nguyen Van F', '0901111111', 'f@gmail.com', N'Ha Noi'),
(N'Le Thi G', '0902222222', 'g@gmail.com', N'TP HCM'),
(N'Tran Van H', '0903333333', 'h@gmail.com', N'Da Nang');

-- Lái thử
INSERT INTO LaiThu (KhachHangID, DaiLyID, XeID, NgayHen, PhanHoi)
VALUES
(1, 1, 1, '2025-09-15 10:00', N'Xe chay em, gia hop ly'),
(2, 2, 3, '2025-09-16 14:00', N'Gia re, pin hoi thap'),
(3, 3, 1, '2025-09-17 09:00', N'Toc do manh, thich hop di duong dai');
-- Đơn hàng
INSERT INTO DonHang (DaiLyID, KhachHangID, XeID, TrangThai)
VALUES
(1, 1, 1, N'DaGiao'),
(1, 1, 2, N'BaoGia'),
(2, 2, 3, N'DaGiao'),
(3, 3, 1, N'HopDong');

-- Thanh toán
INSERT INTO ThanhToan (DonHangID, HinhThuc, SoTien)
VALUES
(1, N'TraThang', 900000000),
(3, N'TraGop', 300000000);

-- Giao xe
INSERT INTO GiaoXe (DonHangID, TinhTrang, NgayGiao)
VALUES
(1, N'DaGiao', '2025-09-10'),
(3, N'DaGiao', '2025-09-12');
SELECT d.TenDaiLy, COUNT(dh.DonHangID) AS TongDon, SUM(tt.SoTien) AS TongDoanhThu
FROM DonHang dh
JOIN DaiLy d ON dh.DaiLyID = d.DaiLyID
JOIN ThanhToan tt ON dh.DonHangID = tt.DonHangID
GROUP BY d.TenDaiLy;
SELECT x.Mau, SUM(t.SoLuong) AS TongTonKho, COUNT(dh.DonHangID) AS SoLuongBanRa
FROM XeDien x
LEFT JOIN TonKho t ON x.XeID = t.XeID
LEFT JOIN DonHang dh ON x.XeID = dh.XeID AND dh.TrangThai = N'DaGiao'
GROUP BY x.Mau;



--Flow 1
 -- Xem danh mục xe
SELECT * FROM XeDien;

-- So sánh xe theo ID
SELECT Mau, PhienBan, GiaBanCoBan, TinhNang
FROM XeDien
WHERE XeID IN (1,2);

-- Tạo báo giá (đơn hàng ở trạng thái "BaoGia")
INSERT INTO DonHang (DaiLyID, KhachHangID, XeID, TrangThai)
VALUES (1, 3, 2, N'BaoGia');

-- Ghi nhận thanh toán
INSERT INTO ThanhToan (DonHangID, HinhThuc, SoTien)
VALUES (5, N'TraGop', 150000000);

--Flow 2
-- Xem tồn kho toàn hệ thống
SELECT d.TenDaiLy, x.Mau, x.PhienBan, t.SoLuong
FROM TonKho t
JOIN DaiLy d ON t.DaiLyID = d.DaiLyID
JOIN XeDien x ON t.XeID = x.XeID;

-- Cập nhật phân bổ xe cho đại lý
UPDATE TonKho
SET SoLuong = SoLuong + 10, NgayCapNhat = GETDATE()
WHERE DaiLyID = 1 AND XeID = 2;

--Flow 3
-- Thêm khách hàng mới
INSERT INTO KhachHang (HoTen, DienThoai, Email, DiaChi)
VALUES (N'Nguyen Van A', '0909123456', 'a@gmail.com', N'Ha Noi');

-- Báo cáo doanh số theo đại lý
SELECT d.TenDaiLy, COUNT(dh.DonHangID) AS TongDon, SUM(tt.SoTien) AS TongDoanhThu
FROM DonHang dh
JOIN DaiLy d ON dh.DaiLyID = d.DaiLyID
JOIN ThanhToan tt ON dh.DonHangID = tt.DonHangID
GROUP BY d.TenDaiLy;

-- Báo cáo tồn kho & tốc độ tiêu thụ
SELECT x.Mau, SUM(t.SoLuong) AS TongTonKho, COUNT(dh.DonHangID) AS SoLuongBanRa
FROM XeDien x
LEFT JOIN TonKho t ON x.XeID = t.XeID
LEFT JOIN DonHang dh ON x.XeID = dh.XeID AND dh.TrangThai = N'DaGiao'
GROUP BY x.Mau;