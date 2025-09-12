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

--2. Bảng thông tin cơ bản của xe
CREATE TABLE XE (
    MaXe INT PRIMARY KEY IDENTITY(1,1),   -- Mã xe
    TenXe NVARCHAR(100) NOT NULL,         -- Tên xe
    HangXe NVARCHAR(100) NOT NULL,        -- Hãng xe (VD: VinFast, Tesla)
    PhienBan NVARCHAR(50) NULL,           -- Phiên bản (Standard, Premium…)
    GiaBan DECIMAL(18,2) NOT NULL,        -- Giá bán
    MauSac NVARCHAR(200) NULL,            -- Màu sắc có sẵn
	XuatXu NVARCHAR(100) null			  -- Nguồn gốc xuất xứ
);
go
INSERT INTO XE (TenXe, HangXe, PhienBan, GiaBan, MauSac, XuatXu)
VALUES 
(N'VinFast VF e34', N'VinFast', N'Standard', 690000000, N'Xanh, Trắng, Đen', N'Việt Nam'),
(N'Tesla Model 3', N'Tesla', N'Standard Range', 1250000000, N'Đỏ, Đen, Trắng, Xanh', N'Mỹ');
go

----2.1 Bảng cấu hình của xe
CREATE TABLE CAUHINHXE (
    MaCH INT PRIMARY KEY IDENTITY(1,1),   -- Mã cấu hình
    MaXe INT NOT NULL,                    -- Khóa ngoại liên kết đến XE
	SoLuongGhe int null,			  -- Số lượng ghế ngồi
	HeDanDong NVARCHAR(50) NULL,             -- Hệ dẫn động (FWD, RWD, AWD)
	DungLuongPin NVARCHAR(50) NULL,       -- Dung lượng pin (VD: 75 kWh)
    LoaiPin NVARCHAR(50) NULL,            -- Loại pin (Lithium-ion, LFP...)
    CongSuatDongCo NVARCHAR(50) NULL,     -- Công suất động cơ (kW)
    MoMenXoan NVARCHAR(50) NULL,          -- Mô-men xoắn (Nm)
    SoDongCo NVARCHAR(50) NULL,           -- Số động cơ (single/dual motor)
    QuangDuong NVARCHAR(50) NULL,         -- Quãng đường 1 lần sạc (km)
    TangToc NVARCHAR(50) NULL,            -- Tăng tốc 0–100 km/h
    TocDoToiDa NVARCHAR(50) NULL,         -- Tốc độ tối đa
    KichThuoc NVARCHAR(100) NULL,         -- Dài x Rộng x Cao
    ChieuDaiCoSo NVARCHAR(50) NULL,       -- Chiều dài cơ sở
    TrongLuong NVARCHAR(50) NULL,         -- Trọng lượng
    DungTichKhoangHanhLy NVARCHAR(50) NULL, -- Dung tích khoang hành lý


    CONSTRAINT FK_CAUHINH_XE FOREIGN KEY (MaXe)
        REFERENCES XE(MaXe) ON DELETE CASCADE
);
go
INSERT INTO CAUHINHXE 
(MaXe, SoLuongGhe, HeDanDong, DungLuongPin, LoaiPin, CongSuatDongCo, MoMenXoan, SoDongCo, 
 QuangDuong, TangToc, TocDoToiDa, KichThuoc, ChieuDaiCoSo, TrongLuong, DungTichKhoangHanhLy)
VALUES
(1, 5, N'FWD', N'42 kWh', N'Lithium-ion', N'110 kW', N'242 Nm', N'Single Motor',
 N'300 km', N'9 giây', N'150 km/h', N'4300 x 1793 x 1613 mm', N'2610 mm', N'1530 kg', N'300 L'),
(2, 5, N'RWD', N'57.5 kWh', N'Lithium-ion', N'208 kW', N'375 Nm', N'Single Motor',
 N'491 km', N'5.8 giây', N'225 km/h', N'4694 x 1849 x 1443 mm', N'2875 mm', N'1760 kg', N'425 L');

 go

----2.2 Bảng tính năng của xe
CREATE TABLE TINHNANG (
    MaTN INT PRIMARY KEY IDENTITY(1,1),   -- Mã tính năng
    MaXe INT NOT NULL,                    -- Khóa ngoại liên kết đến XE
    -- Hệ thống an toàn
    PhanhABS BIT NULL,                    -- Chống bó cứng phanh
    TuiKhi BIT NULL,                       -- Túi khí
    CanhBaoVaCham BIT NULL,               -- Cảnh báo va chạm
    PhanhTuDong BIT NULL,                 -- Phanh khẩn cấp tự động
    -- Hỗ trợ lái
    ADAS BIT NULL,                        -- Hệ thống hỗ trợ lái nâng cao
    GiuLanDuong BIT NULL,                 -- Giữ làn đường
    CanhBaoDiemMu BIT NULL,               -- Cảnh báo điểm mù
    CruiseControl BIT NULL,               -- Ga tự động thích ứng
    CameraSau BIT NULL,                    
    -- Nội thất
    GheDa BIT NULL,                       -- Ghế da
    DieuHoaTuDong BIT NULL,               -- Điều hòa tự động
    CuaSoTroi BIT NULL,                   -- Cửa sổ trời
    GheSua BIT NULL,                      -- Ghế sưởi / làm mát
    -- Giải trí
    ManHinh NVARCHAR(50) NULL,            -- Màn hình trung tâm (VD: 12.3 inch)
    HeThongLoa NVARCHAR(100) NULL,        -- Hệ thống loa (Bose, JBL…)
    CarPlay BIT NULL,                     -- Apple CarPlay
    AndroidAuto BIT NULL,                 -- Android Auto
    -- Kết nối
    GPS BIT NULL,                         -- Định vị GPS
    OTA BIT NULL,                         -- Cập nhật phần mềm OTA
    UngDungDiDong BIT NULL,               -- Kết nối qua ứng dụng di động
	-- Hệ thống chiếu sáng
    DenLED BIT NULL,                      -- Đèn chiếu sáng LED
    -- Hệ thống sạc
    SacNhanhDC BIT NULL,                  -- Sạc nhanh DC dong dien 1 chieu
    SacAC BIT NULL,                       -- Sạc AC dien 2 cheiu
    QuanLySacQuaApp BIT NULL,             -- Quản lý sạc qua ứng dụng

    CONSTRAINT FK_TINHNANG_XE FOREIGN KEY (MaXe) 
        REFERENCES XE(MaXe) ON DELETE CASCADE
);
go
INSERT INTO TINHNANG
(MaXe, PhanhABS, TuiKhi, CanhBaoVaCham, PhanhTuDong, ADAS, GiuLanDuong, CanhBaoDiemMu, 
 CruiseControl, CameraSau, GheDa, DieuHoaTuDong, CuaSoTroi, GheSua, ManHinh, HeThongLoa, 
 CarPlay, AndroidAuto, GPS, OTA, UngDungDiDong, DenLED, SacNhanhDC, SacAC, QuanLySacQuaApp)
VALUES
(1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 0, N'10 inch', N'6 loa', 1, 1, 1, 1, 1, 1, 1, 1, 1),
(2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, N'15 inch', N'Loa Premium 14 loa', 1, 1, 1, 1, 1, 1, 1, 1, 1);
go