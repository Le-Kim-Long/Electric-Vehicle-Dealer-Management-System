use MASTER
go
-- 0. Xóa Database RentalDB nếu đã tồn tại trong DBMS --------------------
IF EXISTS (
    SELECT name 
    FROM sys.databases 
    WHERE name = N'DB_EVM'
)
BEGIN
    -- Tùy chọn: đưa DB về SINGLE_USER để tránh lỗi đang có kết nối
    ALTER DATABASE [DB_EVM] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    
    -- Xóa database
    DROP DATABASE [DB_EVM];
END

go
-- 1. Tạo database
CREATE DATABASE DB_EVM;
go

USE DB_EVM;
go

--3. Tạo bảng đại lý đại lý
CREATE TABLE DEALER (
    DealerID INT PRIMARY KEY IDENTITY(1,1),     -- Mã đại lý
    DealerName NVARCHAR(100) NOT NULL,          -- Tên đại lý
    Address NVARCHAR(200),                      -- Địa chỉ
    PhoneNumber NVARCHAR(20),                   -- Số điện thoại
    Email NVARCHAR(100)                         -- Email
);

INSERT INTO DEALER (DealerName, Address, PhoneNumber, Email)
VALUES 
(N'VinFast Đại lý Quận 1', N'123 Lê Lợi, Quận 1, TP.HCM', N'0909123456', N'dailyq1@vinfast.vn'),
(N'VinFast Đại lý Quận 3', N'45 Cách Mạng Tháng 8, Quận 3, TP.HCM', N'0909234567', N'dailyq3@vinfast.vn'),
(N'VinFast Đại lý Quận 7', N'678 Nguyễn Văn Linh, Quận 7, TP.HCM', N'0909345678', N'dailyq7@vinfast.vn'),
(N'VinFast Đại lý Bình Thạnh', N'89 Điện Biên Phủ, Quận Bình Thạnh, TP.HCM', N'0909456789', N'dailybt@vinfast.vn'),
(N'VinFast Đại lý Thủ Đức', N'12 Võ Văn Ngân, TP. Thủ Đức, TP.HCM', N'0909567890', N'dailytđ@vinfast.vn');

--4. Tạo bảng loại xe
CREATE TABLE CAR_MODEL (
    ModelId INT PRIMARY KEY IDENTITY(1,1),   -- Primary Key
    ModelName NVARCHAR(100) NOT NULL,        -- VF3, VF5, VF7, VF8, VF9...
    Segment NVARCHAR(50) NOT NULL            -- A-SUV, B-SUV, C-SUV, D-SUV, E-SUV, Sedan...
);

-- Insert sample data
INSERT INTO CAR_MODEL (ModelName, Segment)
VALUES
(N'VF3',  N'A-SUV'),
(N'VF5',  N'B-SUV'),
(N'VF7',  N'C-SUV'),
(N'VF8',  N'D-SUV'),
(N'VF9',  N'E-SUV');

--5. Tạo bảng phiên bản xe vì mỗi loại xe có xe có nhiều phiên bản
CREATE TABLE CAR_VARIANT (
    VariantId INT PRIMARY KEY IDENTITY(1,1),
    ModelId INT NOT NULL,
    VariantName NVARCHAR(100) NOT NULL,   -- Eco, Plus, Luxury...
    Description NVARCHAR(500) NULL,
    FOREIGN KEY (ModelId) REFERENCES CAR_MODEL(ModelId)
);

-- VF3 (ModelId = 1)
INSERT INTO CAR_VARIANT (ModelId, VariantName, Description)
VALUES 
(1, N'Eco', N'VF3 Eco Standard Version'),
(1, N'Plus', N'VF3 Plus Premium Version');

-- VF5 (ModelId = 2)
INSERT INTO CAR_VARIANT (ModelId, VariantName, Description)
VALUES 
(2, N'Eco', N'VF5 Eco Standard Version'),
(2, N'Plus', N'VF5 Plus Premium Version');

-- VF7 (ModelId = 3)
INSERT INTO CAR_VARIANT (ModelId, VariantName, Description)
VALUES 
(3, N'Eco', N'VF7 Eco Standard Version'),
(3, N'Plus', N'VF7 Plus Premium Version');

-- VF8 (ModelId = 4)
INSERT INTO CAR_VARIANT (ModelId, VariantName, Description)
VALUES 
(4, N'Eco', N'VF8 Eco Standard Version'),
(4, N'Plus', N'VF8 Plus Premium Version');

-- VF9 (ModelId = 5)
INSERT INTO CAR_VARIANT (ModelId, VariantName, Description)
VALUES 
(5, N'Eco', N'VF9 Eco Standard Version'),
(5, N'Plus', N'VF9 Plus Premium Version');

--6. Mỗi phiên bản có 1 bảng cấu hình riêng
CREATE TABLE CONFIGURATION (
    ConfigId INT PRIMARY KEY IDENTITY(1,1),
    VariantId INT NOT NULL UNIQUE,          -- Each variant has only one configuration

    BatteryCapacity DECIMAL(6,2) NULL,      -- kWh
    BatteryType NVARCHAR(50) NULL,          -- LFP, NMC...
    FullChargeTime INT NULL,                -- Minutes
    RangeKm INT NULL,                       -- km

    Power DECIMAL(6,2) NULL,                -- kW
    Torque DECIMAL(6,2) NULL,               -- Nm

    LengthMm INT NULL,                      -- mm
    WidthMm INT NULL,                       -- mm
    HeightMm INT NULL,                      -- mm
    WheelbaseMm INT NULL,                   -- mm
    WeightKg INT NULL,                      -- kg
    TrunkVolumeL INT NULL,                  -- L

    Seats INT NULL,                         -- Number of seats

    FOREIGN KEY (VariantId) REFERENCES CAR_VARIANT(VariantId)
);

-- VF3 Eco
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (1, 18.64, N'LFP', 240, 210,
        32.0, 110.0, 3190, 1670, 1600, 2075, 1200, 285, 4);

-- VF3 Plus
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (2, 18.64, N'LFP', 240, 210,
        35.0, 120.0, 3190, 1670, 1600, 2075, 1250, 285, 4);

-- VF5 Eco
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (3, 37.23, N'NMC', 360, 320,
        70.0, 135.0, 3965, 1720, 1580, 2510, 1300, 330, 5);

-- VF5 Plus
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (4, 37.23, N'NMC', 360, 320,
        75.0, 140.0, 3965, 1720, 1580, 2510, 1350, 330, 5);

-- VF7 Eco
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (5, 75.30, N'NMC', 480, 450,
        130.0, 250.0, 4450, 1870, 1640, 2735, 1650, 420, 5);

-- VF7 Plus
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (6, 75.30, N'NMC', 480, 450,
        150.0, 280.0, 4450, 1870, 1640, 2735, 1700, 420, 5);

-- VF8 Eco
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (7, 82.00, N'NMC', 500, 460,
        150.0, 320.0, 4750, 1930, 1660, 2950, 2000, 550, 5);

-- VF8 Plus
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (8, 87.70, N'NMC', 500, 470,
        220.0, 400.0, 4750, 1930, 1660, 2950, 2100, 550, 5);

-- VF9 Eco
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (9, 92.00, N'NMC', 600, 480,
        300.0, 500.0, 5110, 2010, 1690, 3150, 2500, 700, 7);

-- VF9 Plus
INSERT INTO CONFIGURATION (VariantId, BatteryCapacity, BatteryType, FullChargeTime, RangeKm,
                           Power, Torque, LengthMm, WidthMm, HeightMm, WheelbaseMm, WeightKg, TrunkVolumeL, Seats)
VALUES (10, 92.00, N'NMC', 600, 480,
        320.0, 520.0, 5110, 2010, 1690, 3150, 2600, 700, 7);
--Bảng màu
CREATE TABLE COLOR (
    ColorID INT PRIMARY KEY IDENTITY(1,1),
    ColorName NVARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO COLOR (ColorName) VALUES (N'Trắng');
INSERT INTO COLOR (ColorName) VALUES (N'Đen');
INSERT INTO COLOR (ColorName) VALUES (N'Đỏ');
INSERT INTO COLOR (ColorName) VALUES (N'Xanh dương');
INSERT INTO COLOR (ColorName) VALUES (N'Xanh rêu');

--7. Bảng xe nè
CREATE TABLE CAR (
    CarID INT PRIMARY KEY IDENTITY(1,1),
    VariantId INT NOT NULL,
    ColorID INT NOT NULL,
    ProductionYear INT NOT NULL,
    Price DECIMAL(18,2) NOT NULL,
    Status NVARCHAR(50) DEFAULT N'On Sale',

    CONSTRAINT FK_CAR_VERSION FOREIGN KEY (VariantId) REFERENCES CAR_VARIANT(VariantId),
    CONSTRAINT FK_CAR_COLOR FOREIGN KEY (ColorID) REFERENCES COLOR(ColorID)
);

-------------------------------------------------
-- VF3
-------------------------------------------------
-- VF3 Eco (VersionID = 1)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES 
(1, 1, 2025, 240000000),  -- White
(1, 2, 2025, 250000000);  -- Black

-- VF3 Plus (VersionID = 2)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES 
(2, 1, 2025, 310000000),  -- White
(2, 2, 2025, 300000000),  -- Black
(2, 3, 2025, 300000000),  -- Red
(2, 4, 2025, 310000000);  -- Blue


-------------------------------------------------
-- VF5
-------------------------------------------------
-- VF5 Eco (VersionID = 3)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(3, 1, 2025, 370000000),  -- White
(3, 2, 2025, 375000000);  -- Black

-- VF5 Plus (VersionID = 4)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(4, 1, 2025, 420000000),  -- White
(4, 2, 2025, 425000000),  -- Black
(4, 3, 2025, 430000000),  -- Red
(4, 4, 2025, 435000000);  -- Blue


-------------------------------------------------
-- VF7
-------------------------------------------------
-- VF7 Eco (VersionID = 5)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(5, 1, 2025, 650000000),  -- White
(5, 2, 2025, 655000000);  -- Black

-- VF7 Plus (VersionID = 6)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(6, 1, 2025, 720000000),  -- White
(6, 2, 2025, 725000000),  -- Black
(6, 3, 2025, 730000000),  -- Red
(6, 4, 2025, 735000000),  -- Blue
(6, 5, 2025, 740000000);  -- Moss Green


-------------------------------------------------
-- VF8
-------------------------------------------------
-- VF8 Eco (VersionID = 7)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(7, 1, 2025, 950000000),  -- White
(7, 2, 2025, 960000000),  -- Black
(7, 3, 2025, 970000000);  -- Red

-- VF8 Plus (VersionID = 8)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(8, 1, 2025, 1050000000),  -- White
(8, 2, 2025, 1060000000),  -- Black
(8, 3, 2025, 1070000000),  -- Red
(8, 4, 2025, 1080000000),  -- Blue
(8, 5, 2025, 1090000000);  -- Moss Green


-------------------------------------------------
-- VF9
-------------------------------------------------
-- VF9 Eco (VersionID = 9)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(9, 1, 2025, 1250000000),  -- White
(9, 2, 2025, 1260000000);  -- Black

-- VF9 Plus (VersionID = 10)
INSERT INTO CAR (VariantId, ColorID, ProductionYear, Price)
VALUES
(10, 1, 2025, 1350000000),  -- White
(10, 2, 2025, 1360000000),  -- Black
(10, 3, 2025, 1370000000),  -- Red
(10, 4, 2025, 1380000000),  -- Blue
(10, 5, 2025, 1390000000);  -- Moss Green

--8. Tạo bảng vai trò người dùng
CREATE TABLE ROLE (
    RoleID INT PRIMARY KEY IDENTITY(1,1),
    RoleName NVARCHAR(50) NOT NULL UNIQUE,   -- Admin / EVMStaff / DealerStaff / DealerManager

);

INSERT INTO ROLE (RoleName)
VALUES 
(N'Admin'),
(N'EVMStaff'),
(N'DealerStaff'),
(N'DealerManager');

--9. Tạo bảng người dùng
CREATE TABLE USER_ACCOUNT (
    UserID INT PRIMARY KEY IDENTITY(1,1),        -- User ID
    FullName NVARCHAR(100) NOT NULL,             -- Full name
    Email NVARCHAR(100) NOT NULL UNIQUE,         -- Login email
    PasswordHash NVARCHAR(255) NOT NULL,         -- Password (hash, not plain text)
    PhoneNumber NVARCHAR(20) NULL,               -- Contact phone
    RoleID INT NOT NULL,                         -- FK -> ROLE
    DealerID INT NULL,                           -- If dealer staff/manager, belongs to which dealer
    Status NVARCHAR(50) DEFAULT N'Active',       -- Active / Locked
    CreatedDate DATETIME DEFAULT GETDATE(),      -- Account creation date

    FOREIGN KEY (RoleID) REFERENCES ROLE(RoleID),
    FOREIGN KEY (DealerID) REFERENCES DEALER(DealerID)
);

-- Admin
INSERT INTO USER_ACCOUNT (FullName, Email, PasswordHash, PhoneNumber, RoleID, DealerID)
VALUES 
(N'Nguyen Van An', N'admin@vinfast.com', N'123456', N'0909000001', 1, NULL);

-- EVM Staff
INSERT INTO USER_ACCOUNT (FullName, Email, PasswordHash, PhoneNumber, RoleID, DealerID)
VALUES 
(N'Tran Thi Hoa', N'evm.staff@vinfast.com', N'123456', N'0909000002', 2, NULL);

-- Dealer Manager (belongs to Dealer 1)
INSERT INTO USER_ACCOUNT (FullName, Email, PasswordHash, PhoneNumber, RoleID, DealerID)
VALUES 
(N'Le Van Minh', N'manager.dl1@vinfast.com', N'123456', N'0909000003', 4, 1);

-- Dealer Staff (belongs to Dealer 1)
INSERT INTO USER_ACCOUNT (FullName, Email, PasswordHash, PhoneNumber, RoleID, DealerID)
VALUES 
(N'Pham Van Nam', N'staff.dl1@vinfast.com', N'123456', N'0909000004', 3, 1);

-- Dealer Staff (belongs to Dealer 2)
INSERT INTO USER_ACCOUNT (FullName, Email, PasswordHash, PhoneNumber, RoleID, DealerID)
VALUES 
(N'Ngo Thi Lan', N'staff.dl2@vinfast.com', N'123456', N'0909000005', 3, 2);

--10. Bảng lưu thông tin khách hàng
CREATE TABLE CUSTOMER (
    CustomerId INT PRIMARY KEY IDENTITY(1,1),   -- Customer ID
    FullName NVARCHAR(100) NOT NULL,            -- Full name
    Email NVARCHAR(100) NULL,                   -- Email
    PhoneNumber NVARCHAR(20) NOT NULL,          -- Phone number
    CreatedDate DATETIME DEFAULT GETDATE()      -- Created date
);

-- Insert 10 Vietnamese customers
INSERT INTO CUSTOMER (FullName, Email, PhoneNumber)
VALUES 
(N'Nguyễn Văn An', N'an.nguyen@example.com', N'0909001001'),
(N'Trần Thị Bình', N'binh.tran@example.com', N'0909001002'),
(N'Lê Hoàng Cường', N'cuong.le@example.com', N'0909001003'),
(N'Phạm Thị Dung', N'dung.pham@example.com', N'0909001004'),
(N'Võ Văn Hùng', N'hung.vo@example.com', N'0909001005'),
(N'Đặng Thị Lan', N'lan.dang@example.com', N'0909001006'),
(N'Huỳnh Văn Minh', N'minh.huynh@example.com', N'0909001007'),
(N'Ngô Thị Oanh', N'oanh.ngo@example.com', N'0909001008'),
(N'Bùi Văn Quang', N'quang.bui@example.com', N'0909001009'),
(N'Phan Thị Thảo', N'thao.phan@example.com', N'0909001010');

--11. Tạo bảng lịch hẹn lái thử
CREATE TABLE TESTDRIVE_APPOINTMENT (
    AppointmentId INT PRIMARY KEY IDENTITY(1,1),   -- Mã lịch hẹn
    CustomerId INT NOT NULL,                       -- Ai đặt (FK -> CUSTOMER)
    CarId INT NOT NULL,                            -- Xe muốn lái thử (FK -> CAR)
    DealerId INT NOT NULL,                         -- Đại lý tổ chức (FK -> DEALER)
    
    AppointmentDate DATE NOT NULL,                 -- Ngày hẹn
    AppointmentTime TIME NOT NULL,                 -- Giờ hẹn
    
    Status NVARCHAR(50) DEFAULT N'Đang chờ',       -- Đang chờ, Xác nhận, Đã hủy, Hoàn thành
    Notes NVARCHAR(255) NULL,                      -- Ghi chú
    
    CreatedDate DATETIME DEFAULT GETDATE(),        -- Ngày tạo

    FOREIGN KEY (CustomerId) REFERENCES CUSTOMER(CustomerId),
    FOREIGN KEY (CarId) REFERENCES CAR(CarId),
    FOREIGN KEY (DealerId) REFERENCES DEALER(DealerId)
);

-- Thêm 5 lịch hẹn lái thử (dữ liệu tiếng Việt)
INSERT INTO TESTDRIVE_APPOINTMENT (CustomerId, CarId, DealerId, AppointmentDate, AppointmentTime, Status, Notes)
VALUES
(1, 2, 1, '2025-10-01', '09:00', N'Đang chờ', N'Khách muốn lái thử buổi sáng'),
(2, 3, 1, '2025-10-02', '14:30', N'Xác nhận', N'Đại lý đã gọi xác nhận lịch hẹn'),
(3, 1, 2, '2025-10-03', '10:00', N'Đang chờ', N'Khách muốn so sánh với mẫu VF 6'),
(4, 5, 2, '2025-10-04', '15:00', N'Đã hủy', N'Khách hủy do bận công việc'),
(5, 4, 1, '2025-10-05', '11:30', N'Hoàn thành', N'Lái thử diễn ra thành công, khách hài lòng');

CREATE TABLE PROMOTION (
    PromotionId INT PRIMARY KEY IDENTITY(1,1),
    PromotionName NVARCHAR(100) NOT NULL,       -- Tên chương trình
    Description NVARCHAR(255) NULL,             -- Mô tả chi tiết
    Value DECIMAL(18,2) NULL,                   -- Giá trị giảm (VNĐ hoặc %)
    Type NVARCHAR(50) NOT NULL,                 -- 'VND', '%'
    Scope NVARCHAR(20) NOT NULL,                -- 'ORDER' hoặc 'PRODUCT'
    StartDate DATE NOT NULL,
    EndDate DATE NOT NULL,
    Status NVARCHAR(50) DEFAULT N'Đang hoạt động'
);

INSERT INTO PROMOTION (PromotionName, Description, Value, Type, Scope, StartDate, EndDate, Status)
VALUES
(N'Giảm 10 triệu cho VF3', N'Ưu đãi 10,000,000 VNĐ cho mỗi xe VF3', 10000000, N'VND', N'PRODUCT', '2025-01-01', '2025-06-30', N'Đang hoạt động'),
(N'Giảm 5% toàn đơn hàng VF5', N'Giảm 5% cho các đơn hàng có VF5', 5, N'%', N'ORDER', '2025-02-01', '2025-07-31', N'Đang hoạt động'),
(N'Giảm 20 triệu cho VF8', N'Ưu đãi đặc biệt 20,000,000 VNĐ cho VF8', 20000000, N'VND', N'PRODUCT', '2025-03-01', '2025-09-30', N'Đang hoạt động');

--12. Tạo bảng đơn hàng
CREATE TABLE ORDERS (
    OrderId INT PRIMARY KEY IDENTITY(1,1),
    CustomerId INT NOT NULL,
    DealerId INT NOT NULL,

    OrderDate DATETIME DEFAULT GETDATE(),
    SubTotal DECIMAL(18,2) NOT NULL,             -- Tổng trước giảm giá
    DiscountAmount DECIMAL(18,2) DEFAULT 0,      -- Giảm giá hóa đơn
    TotalAmount AS (SubTotal - DiscountAmount) PERSISTED,

    PaymentMethod NVARCHAR(50) NOT NULL,
    Status NVARCHAR(50) DEFAULT N'Chờ xử lý',

    PromotionId INT NULL,                        -- Nếu khuyến mãi áp dụng toàn đơn
    FOREIGN KEY (PromotionId) REFERENCES PROMOTION(PromotionId),
    FOREIGN KEY (CustomerId) REFERENCES CUSTOMER(CustomerId),
    FOREIGN KEY (DealerId) REFERENCES DEALER(DealerId)
);

INSERT INTO ORDERS (CustomerId, DealerId, SubTotal, DiscountAmount, PaymentMethod, Status, PromotionId)
VALUES
(1, 1, 1490000000, 0, N'Thanh toán chuyển khoản', N'Hoàn thành', NULL),
(2, 1, 1200000000, 0, N'Thanh toán trả góp', N'Chờ xử lý', NULL);

--13. Tạo bảng chi tiết đơn hàng
CREATE TABLE ORDER_DETAILS (
    OrderDetailId INT PRIMARY KEY IDENTITY(1,1),
    OrderId INT NOT NULL,
    CarId INT NOT NULL,
    Quantity INT NOT NULL DEFAULT 1,
    UnitPrice DECIMAL(18,2) NOT NULL,
    DiscountAmount DECIMAL(18,2) DEFAULT 0,      -- Giảm giá theo sản phẩm
    FinalPrice AS ((UnitPrice * Quantity) - DiscountAmount) PERSISTED,

    PromotionId INT NULL,                        -- Nếu khuyến mãi áp dụng riêng cho sản phẩm
    FOREIGN KEY (PromotionId) REFERENCES PROMOTION(PromotionId),
    FOREIGN KEY (OrderId) REFERENCES ORDERS(OrderId),
    FOREIGN KEY (CarId) REFERENCES CAR(CarId)
);

-- VF3 Eco White (CarId = giả sử 1)
INSERT INTO ORDER_DETAILS (OrderId, CarId, Quantity, UnitPrice, DiscountAmount, PromotionId)
VALUES
(1, 1, 1, 240000000, 10000000, 1);  -- Áp dụng khuyến mãi giảm 10 triệu cho VF3

-- VF9 Eco White (CarId = giả sử 21)
INSERT INTO ORDER_DETAILS (OrderId, CarId, Quantity, UnitPrice, DiscountAmount, PromotionId)
VALUES
(1, 21, 1, 1250000000, 0, NULL);    -- Không có khuyến mãi

 -- Bảng quan hệ nhiều nhiều 
CREATE TABLE Promotion_Dealer (
    PromotionId INT NOT NULL,
    DealerId INT NOT NULL,
    PRIMARY KEY (PromotionId, DealerId),
    FOREIGN KEY (PromotionId) REFERENCES PROMOTION(PromotionId),
    FOREIGN KEY (DealerId) REFERENCES DEALER(DealerId)
);

-- PromotionId = 1 (Giảm 10 triệu VF3) áp dụng Quận 1, Quận 3
INSERT INTO Promotion_Dealer (PromotionId, DealerId)
VALUES
(1, 1),
(1, 2);

-- PromotionId = 2 (Giảm 5% VF5) áp dụng Quận 7, Bình Thạnh
INSERT INTO Promotion_Dealer (PromotionId, DealerId)
VALUES
(2, 3),
(2, 4);

-- PromotionId = 3 (Giảm 20 triệu VF8) áp dụng tất cả các đại lý
INSERT INTO Promotion_Dealer (PromotionId, DealerId)
VALUES
(3, 1),
(3, 2),
(3, 3),
(3, 4),
(3, 5);

--13. Tạo bảng lưu thông tin thanh toán
CREATE TABLE Payment (
    PaymentId INT PRIMARY KEY IDENTITY(1,1),       -- Payment transaction ID
    OrderId INT NOT NULL,                          -- FK -> Order
    Amount DECIMAL(18,2) NOT NULL,                 -- Payment amount
    PaymentDate DATETIME DEFAULT GETDATE(),        -- Payment date
    Method NVARCHAR(50) NOT NULL,                  -- Cash, Bank Transfer, Card, E-Wallet
    Status NVARCHAR(50) DEFAULT N'Success',        -- Success, Pending, Failed
    Note NVARCHAR(255) NULL,                       -- Additional notes
    
    FOREIGN KEY (OrderId) REFERENCES ORDERS(OrderId)
);

INSERT INTO Payment (OrderId, Amount, Method, Status, Note)
VALUES
(1, 1490000000, N'Thanh toán chuyển khoản', N'Thành công', N'Khách đã chuyển khoản toàn bộ số tiền');

--14. Tạo bảng chi tiết trả góp 
CREATE TABLE Installment (
    InstallmentId INT PRIMARY KEY IDENTITY(1,1),   -- Installment ID
    OrderId INT NOT NULL,                          -- FK -> Order
    TermCount INT NOT NULL,                        -- Number of terms (e.g., 12 months, 24 months)
    AmountPerTerm DECIMAL(18,2) NOT NULL,          -- Payment amount per term
    InterestRate DECIMAL(5,2) NOT NULL,            -- Annual interest rate (% per year)
    Note NVARCHAR(200) NULL,                       -- Additional information
    
    FOREIGN KEY (OrderId) REFERENCES ORDERS(OrderId)
);

INSERT INTO Installment (OrderId, TermCount, AmountPerTerm, InterestRate, Note)
VALUES
(2, 12, 100000000, 8.00, N'Trả góp 12 tháng, lãi suất 8%/năm');

--12. Tạo bảng phản hồi để lưu thông tin phản hồi từ khách hàng
CREATE TABLE Feedback (
    FeedbackId INT PRIMARY KEY IDENTITY(1,1),         -- Feedback ID
    OrderId INT NOT NULL,                             -- FK -> Orders
    Content NVARCHAR(MAX) NOT NULL,                   -- Feedback content
    SubmittedDate DATETIME NOT NULL DEFAULT GETDATE(),-- Date submitted
    Status NVARCHAR(50) NOT NULL DEFAULT N'Pending',  -- Pending, In progress, Resolved
    HandleID INT NULL,                               -- User who handles feedback
    HandledDate DATETIME NULL,                        -- Date handled

    -- Foreign key constraints
    FOREIGN KEY (OrderId) REFERENCES Orders(OrderId),
    FOREIGN KEY (HandleID) REFERENCES USER_ACCOUNT(UserId)
);

INSERT INTO Feedback (OrderId, Content, Status, HandleID, HandledDate)
VALUES
(1, N'Xe chạy êm nhưng giao xe hơi trễ.', N'Đã xử lý', 4, GETDATE()),
(2, N'Thủ tục trả góp mất nhiều thời gian, mong hỗ trợ nhanh hơn.', N'Đang xử lý', 5, GETDATE());

-- Tạo bảng lưu lịch sử phản hồi
CREATE TABLE FeedbackHistory (
    HistoryId INT PRIMARY KEY IDENTITY(1,1),      -- History ID

    FeedbackId INT NOT NULL,                      -- FK -> Feedback
    EmployeeId INT NOT NULL,                      -- FK -> Users (staff who handled)
    ProcessedDate DATETIME NOT NULL DEFAULT GETDATE(),  -- Date processed
    HandlingNotes NVARCHAR(MAX) NULL,             -- Notes on handling
    StatusAfterHandling NVARCHAR(50) NOT NULL,    -- Status after handling

    -- Foreign key constraints
    FOREIGN KEY (FeedbackId) REFERENCES Feedback(FeedbackId),
    FOREIGN KEY (EmployeeId) REFERENCES USER_ACCOUNT(UserId)
);

INSERT INTO FeedbackHistory (FeedbackId, EmployeeId, HandlingNotes, StatusAfterHandling)
VALUES
(1, 4, N'Đã liên hệ khách hàng, giải thích lý do giao xe trễ và tặng voucher bảo dưỡng miễn phí.', N'Đã xử lý'),
(2, 5, N'Đang hỗ trợ khách hàng bổ sung hồ sơ để hoàn tất thủ tục trả góp.', N'Đang xử lý');
