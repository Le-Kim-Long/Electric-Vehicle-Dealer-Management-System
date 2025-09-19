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
);

INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Quận 1');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Quận 3');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Quận 5');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Quận 7');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Quận 10');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Quận 12');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Bình Thạnh');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Gò Vấp');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Tân Bình');
INSERT INTO KhuVuc (TenKhuVuc) VALUES (N'Thủ Đức');

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

-- Quận 1
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Nguyễn Huệ', N'123 Nguyễn Huệ, Quận 1, TP.HCM', '0283111222', 'evnguyenhue@example.com', 'HD001', 1500000000, 1);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Lê Lợi', N'45 Lê Lợi, Quận 1, TP.HCM', '0283222333', 'evleloi@example.com', 'HD002', 1400000000, 1);


-- Quận 3
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Võ Văn Tần', N'56 Võ Văn Tần, Quận 3, TP.HCM', '0283444555', 'evvovantan@example.com', 'HD003', 1200000000, 2);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Nguyễn Đình Chiểu', N'78 Nguyễn Đình Chiểu, Quận 3, TP.HCM', '0283555666', 'evnguyendinhchieu@example.com', 'HD004', 1300000000, 2);


-- Quận 5
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Trần Hưng Đạo', N'90 Trần Hưng Đạo, Quận 5, TP.HCM', '0283666777', 'evtranhungdao@example.com', 'HD005', 1000000000, 3);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV An Dương Vương', N'45 An Dương Vương, Quận 5, TP.HCM', '0283777888', 'evanduongvuong@example.com', 'HD006', 1100000000, 3);


-- Quận 7
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Nguyễn Văn Linh', N'12 Nguyễn Văn Linh, Quận 7, TP.HCM', '0283888999', 'evnguyenvanlinh@example.com', 'HD007', 1500000000, 4);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Phú Mỹ Hưng', N'88 Tôn Dật Tiên, Quận 7, TP.HCM', '0283999000', 'evphumyhung@example.com', 'HD008', 1600000000, 4);


-- Quận 10
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV 3 Tháng 2', N'45 Đường 3/2, Quận 10, TP.HCM', '0284000111', 'ev3thang2@example.com', 'HD009', 1200000000, 5);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Sư Vạn Hạnh', N'67 Sư Vạn Hạnh, Quận 10, TP.HCM', '0284111222', 'evsuvanhahn@example.com', 'HD010', 1300000000, 5);


-- Quận 12
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Hà Huy Giáp', N'23 Hà Huy Giáp, Quận 12, TP.HCM', '0284222333', 'evhahuygiap@example.com', 'HD011', 1100000000, 6);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Nguyễn Ảnh Thủ', N'78 Nguyễn Ảnh Thủ, Quận 12, TP.HCM', '0284333444', 'evnguyenanhthu@example.com', 'HD012', 1150000000, 6);


-- Bình Thạnh
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Điện Biên Phủ', N'123 Điện Biên Phủ, Bình Thạnh, TP.HCM', '0284444555', 'evdienbienphu@example.com', 'HD013', 1250000000, 7);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Bạch Đằng', N'56 Bạch Đằng, Bình Thạnh, TP.HCM', '0284555666', 'evbachdang@example.com', 'HD014', 1300000000, 7);


-- Gò Vấp
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Quang Trung', N'45 Quang Trung, Gò Vấp, TP.HCM', '0284666777', 'evquangtrung@example.com', 'HD015', 1150000000, 8);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Nguyễn Oanh', N'90 Nguyễn Oanh, Gò Vấp, TP.HCM', '0284777888', 'evnguyenoanh@example.com', 'HD016', 1180000000, 8);


-- Tân Bình
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Lý Thường Kiệt', N'34 Lý Thường Kiệt, Tân Bình, TP.HCM', '0284888999', 'evlythuongkiet@example.com', 'HD017', 1220000000, 9);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Cộng Hòa', N'78 Cộng Hòa, Tân Bình, TP.HCM', '0284999000', 'evconghoa@example.com', 'HD018', 1250000000, 9);


-- Thủ Đức
INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Võ Văn Ngân', N'123 Võ Văn Ngân, Thủ Đức, TP.HCM', '0285111222', 'evvovanngan@example.com', 'HD019', 1350000000, 10);

INSERT INTO DaiLy (TenDaiLy, DiaChi, SoDienThoai, Email, SoHopDong, HanMucCongNo, MaKhuVuc)
VALUES (N'Đại lý EV Kha Vạn Cân', N'67 Kha Vạn Cân, Thủ Đức, TP.HCM', '0285222333', 'evkhavancan@example.com', 'HD020', 1380000000, 10);


--Tạo bảng vai trò người dùng
CREATE TABLE VaiTro (
    MaVaiTro INT PRIMARY KEY IDENTITY(1,1),
    TenVaiTro NVARCHAR(50) NOT NULL UNIQUE,   -- Admin / EVMStaff / DealerStaff / DealerManager / KhachHang
    MoTa NVARCHAR(255) NULL
);

INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES 
(N'Admin', N'Quản trị viên hệ thống với toàn quyền quản lý'),
(N'EVMStaff', N'Nhân viên quản lý hệ thống xe điện'),
(N'DealerStaff', N'Nhân viên đại lý'),
(N'DealerManager', N'Quản lý đại lý'),
(N'KhachHang', N'Khách hàng sử dụng hệ thống');

--3. Tạo bảng người dùng
CREATE TABLE NguoiDung (
    MaNguoiDung INT PRIMARY KEY IDENTITY(1,1),
    TenDangNhap NVARCHAR(50) NOT NULL UNIQUE,
    MatKhau NVARCHAR(255) NOT NULL,            
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

INSERT INTO NguoiDung (TenDangNhap, MatKhau, HoTen, Email, SoDienThoai, DiaChi, MaVaiTro) VALUES 
(N'admin', N'admin123', N'Nguyễn Quản Trị', N'admin@evms.com', N'0901234567', N'Hà Nội', 1),
(N'evmstaff1', N'evm123', N'Trần Văn Quản Lý', N'evm1@evms.com', N'0912345678', N'TP. Hồ Chí Minh', 2),
(N'dealer1', N'dealer123', N'Lê Thị Nhân Viên', N'dealer1@evms.com', N'0923456789', N'Đà Nẵng', 3),
(N'manager1', N'manager123', N'Phạm Văn Quản Lý', N'manager1@evms.com', N'0934567890', N'Cần Thơ', 4),
(N'customer1', N'cust123', N'Hoàng Thị Khách', N'customer1@gmail.com', N'0945678901', N'Hải Phòng', 5),
(N'evmstaff2', N'evm456', N'Đỗ Thị Nhân Viên', N'evm2@evms.com', N'0956789012', N'Nha Trang', 2),
(N'dealer2', N'dealer456', N'Vũ Văn Đại Lý', N'dealer2@evms.com', N'0967890123', N'Huế', 3),
(N'manager2', N'manager456', N'Mai Thị Quản Lý', N'manager2@evms.com', N'0978901234', N'Bình Dương', 4),
(N'customer2', N'cust456', N'Ngô Văn Khách', N'customer2@gmail.com', N'0989012345', N'Đồng Nai', 5),
(N'customer3', N'cust789', N'Đặng Thị Khách', N'customer3@gmail.com', N'0990123456', N'Bà Rịa - Vũng Tàu', 5);

--4. Tạo bảng xe
CREATE TABLE Xe (
    MaXe INT PRIMARY KEY IDENTITY(1,1),
    TenXe NVARCHAR(100) NOT NULL,       -- Tên mẫu xe (VD: VinFast VF e34)
    MauSac NVARCHAR(50),                -- Màu sắc
    GiaBan DECIMAL(18,2),               -- Giá bán niêm yết 
	XuatXu NVARCHAR(50),
	NamSanXuat INT NULL,
	TrangThai NVARCHAR(50) DEFAULT N'Đang bán' -- Đang bán, Ngừng bán

);

-- VF3
INSERT INTO Xe (TenXe, MauSac, GiaBan, XuatXu, NamSanXuat)
VALUES 
(N'VinFast VF3', N'Trắng', 340000000, N'Việt Nam', 2025),
(N'VinFast VF3', N'Đen',   350000000, N'Việt Nam', 2025),
(N'VinFast VF3', N'Đỏ',    360000000, N'Việt Nam', 2025);

-- VF5
INSERT INTO Xe (TenXe, MauSac, GiaBan, XuatXu, NamSanXuat)
VALUES 
(N'VinFast VF5', N'Trắng', 440000000, N'Việt Nam', 2025),
(N'VinFast VF5', N'Đen',   450000000, N'Việt Nam', 2025),
(N'VinFast VF5', N'Đỏ',    465000000, N'Việt Nam', 2025);

-- VF7
INSERT INTO Xe (TenXe, MauSac, GiaBan, XuatXu, NamSanXuat)
VALUES 
(N'VinFast VF7', N'Trắng', 740000000, N'Việt Nam', 2025),
(N'VinFast VF7', N'Đen',   755000000, N'Việt Nam', 2025),
(N'VinFast VF7', N'Đỏ',    770000000, N'Việt Nam', 2025);

-- VF8
INSERT INTO Xe (TenXe, MauSac, GiaBan, XuatXu, NamSanXuat)
VALUES 
(N'VinFast VF8', N'Trắng', 940000000, N'Việt Nam', 2025),
(N'VinFast VF8', N'Đen',   960000000, N'Việt Nam', 2025),
(N'VinFast VF8', N'Đỏ',    980000000, N'Việt Nam', 2025);

-- VF9
INSERT INTO Xe (TenXe, MauSac, GiaBan, XuatXu, NamSanXuat)
VALUES 
(N'VinFast VF9', N'Trắng', 1180000000, N'Việt Nam', 2025),
(N'VinFast VF9', N'Đen',   1200000000, N'Việt Nam', 2025),
(N'VinFast VF9', N'Đỏ',    1220000000, N'Việt Nam', 2025);

CREATE TABLE TonKho (
    MaTonKho INT PRIMARY KEY IDENTITY(1,1),
    MaDaiLy INT NOT NULL,
    MaXe INT NOT NULL,
    SoLuong INT NOT NULL DEFAULT 0,

    CONSTRAINT FK_TonKho_DaiLy FOREIGN KEY (MaDaiLy) REFERENCES DaiLy(MaDaiLy),
    CONSTRAINT FK_TonKho_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    CONSTRAINT UQ_TonKho UNIQUE (MaDaiLy, MaXe) -- Mỗi đại lý chỉ có 1 dòng tồn kho cho 1 loại xe
);

INSERT INTO TonKho (MaDaiLy, MaXe, SoLuong)
SELECT d.MaDaiLy, x.MaXe, ABS(CHECKSUM(NEWID())) % 16 + 5
FROM DaiLy d
CROSS JOIN Xe x;

--5. Tạo bảng cấu hình xe
CREATE TABLE CauHinhXe (
    MaCauHinh INT PRIMARY KEY IDENTITY(1,1),
    MaXe INT NOT NULL,

    -- Pin
    DungLuongPin DECIMAL(6,2) NULL,        -- kWh
    LoaiPin NVARCHAR(50) NULL,             -- Lithium-ion, LFP...
	ThoiGianSacDay INT NULL,			   -- Phut
    QuangDuong INT NULL,                   -- km

    -- Động cơ
    CongSuat DECIMAL(6,2) NULL,            -- kW
    MoMenXoan DECIMAL(6,2) NULL,           -- Nm

    -- Kích thước & trọng lượng
    Dai DECIMAL(6,2) NULL,                 -- mm
    Rong DECIMAL(6,2) NULL,                -- mm
    Cao DECIMAL(6,2) NULL,                 -- mm
    ChieuDaiCoSo DECIMAL(6,2) NULL,        -- mm
    TrongLuong DECIMAL(6,2) NULL,          -- kg
    DungTichKhoangHanhLy DECIMAL(6,2) NULL,-- L

    CONSTRAINT FK_CauHinh_Xe FOREIGN KEY (MaXe) REFERENCES Xe(MaXe)
);

-- VF3 (Mini EV)
INSERT INTO CauHinhXe (MaXe, DungLuongPin, LoaiPin, ThoiGianSacDay, QuangDuong, CongSuat, MoMenXoan, Dai, Rong, Cao, ChieuDaiCoSo, TrongLuong, DungTichKhoangHanhLy)
SELECT MaXe, 18.64, N'LFP', 240, 210, 32, 110, 3190, 1670, 1600, 2075, 990, 285
FROM Xe WHERE TenXe = N'VinFast VF3';

-- VF5 (Crossover hạng A)
INSERT INTO CauHinhXe (MaXe, DungLuongPin, LoaiPin, ThoiGianSacDay, QuangDuong, CongSuat, MoMenXoan, Dai, Rong, Cao, ChieuDaiCoSo, TrongLuong, DungTichKhoangHanhLy)
SELECT MaXe, 37.23, N'LFP', 300, 300, 70, 135, 3967, 1723, 1570, 2510, 1320, 350
FROM Xe WHERE TenXe = N'VinFast VF5';

-- VF7 (SUV cỡ C)
INSERT INTO CauHinhXe (MaXe, DungLuongPin, LoaiPin, ThoiGianSacDay, QuangDuong, CongSuat, MoMenXoan, Dai, Rong, Cao, ChieuDaiCoSo, TrongLuong, DungTichKhoangHanhLy)
SELECT MaXe, 75.30, N'Lithium-ion', 420, 450, 150, 320, 4545, 1890, 1635, 2850, 1800, 450
FROM Xe WHERE TenXe = N'VinFast VF7';

-- VF8 (SUV cỡ D)
INSERT INTO CauHinhXe (MaXe, DungLuongPin, LoaiPin, ThoiGianSacDay, QuangDuong, CongSuat, MoMenXoan, Dai, Rong, Cao, ChieuDaiCoSo, TrongLuong, DungTichKhoangHanhLy)
SELECT MaXe, 87.70, N'Lithium-ion', 480, 471, 300, 500, 4750, 1934, 1667, 2950, 2100, 500
FROM Xe WHERE TenXe = N'VinFast VF8';

-- VF9 (SUV cỡ E)
INSERT INTO CauHinhXe (MaXe, DungLuongPin, LoaiPin, ThoiGianSacDay, QuangDuong, CongSuat, MoMenXoan, Dai, Rong, Cao, ChieuDaiCoSo, TrongLuong, DungTichKhoangHanhLy)
SELECT MaXe, 92.00, N'Lithium-ion', 500, 485, 300, 620, 5118, 2000, 1696, 3150, 2500, 600
FROM Xe WHERE TenXe = N'VinFast VF9';


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

INSERT INTO KhuyenMaiXe (MaXe, MaDaiLy, TenKhuyenMai, MoTa, PhanTramGiam, NgayBatDau, NgayKetThuc)
VALUES 
(1, NULL, N'Khuyến mãi Tết 2025 - VF3', N'Giảm 5% cho VF3 (Trắng)', 5.00, '2025-01-20', '2025-02-10'),
(2, NULL, N'Khuyến mãi Tết 2025 - VF3', N'Giảm 5% cho VF3 (Đen)', 5.00, '2025-01-20', '2025-02-10'),
(3, NULL, N'Khuyến mãi Tết 2025 - VF3', N'Giảm 5% cho VF3 (Đỏ)', 5.00, '2025-01-20', '2025-02-10');

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
