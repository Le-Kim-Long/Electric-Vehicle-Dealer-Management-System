import React, { useState, useEffect } from 'react';
import './Dashboard.css';

const Dashboard = () => {
  const [timeFilter, setTimeFilter] = useState('year'); // month, quarter, year
  const [selectedPeriod, setSelectedPeriod] = useState(new Date().getFullYear());
  const [dashboardData, setDashboardData] = useState(null);

  // Mock data generator
  const generateMockData = (filter, period) => {
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;
    
    // Generate labels based on filter
    let labels = [];
    if (filter === 'month') {
      const daysInMonth = new Date(currentYear, period, 0).getDate();
      labels = Array.from({ length: daysInMonth }, (_, i) => `${i + 1}`);
    } else if (filter === 'quarter') {
      labels = ['Tháng 1', 'Tháng 2', 'Tháng 3'];
    } else {
      labels = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
    }

    // Mock revenue data
    const revenueData = labels.map(() => Math.floor(Math.random() * 5000000000) + 1000000000);
    
    // Mock cars sold and revenue
    const carsSold = labels.map(() => Math.floor(Math.random() * 50) + 10);
    const carRevenue = carsSold.map(cars => cars * (Math.floor(Math.random() * 50000000) + 300000000));
    
    // Mock revenue by staff
    const staffNames = ['Phạm Văn Nam', 'Nguyễn Thị Lan', 'Trần Văn Minh', 'Lê Thị Hoa'];
    const staffRevenue = staffNames.map(() => labels.map(() => Math.floor(Math.random() * 2000000000) + 500000000));
    
    // Mock import cost
    const importCost = labels.map(() => Math.floor(Math.random() * 3000000000) + 800000000);
    
    // Calculate summary stats
    const totalRevenue = revenueData.reduce((a, b) => a + b, 0);
    const totalImportCost = importCost.reduce((a, b) => a + b, 0);
    const totalProfit = totalRevenue - totalImportCost;
    const totalCarsSold = carsSold.reduce((a, b) => a + b, 0);
    const completedOrders = Math.floor(totalCarsSold * 0.85);
    const pendingOrders = totalCarsSold - completedOrders;
    const totalCarsImported = Math.floor(totalCarsSold * 1.2);

    return {
      summary: {
        completedOrders,
        pendingOrders,
        totalCarsSold,
        totalRevenue,
        totalProfit,
        totalCarsImported
      },
      charts: {
        revenue: { labels, data: revenueData },
        carsSoldRevenue: { labels, carsSold, revenue: carRevenue },
        staffRevenue: { labels, staffNames, data: staffRevenue },
        revenueImport: { labels, revenue: revenueData, import: importCost }
      }
    };
  };

  useEffect(() => {
    // Load data based on filter
    const data = generateMockData(timeFilter, selectedPeriod);
    setDashboardData(data);
  }, [timeFilter, selectedPeriod]);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const getPeriodOptions = () => {
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth() + 1;
    
    if (timeFilter === 'month') {
      return Array.from({ length: 12 }, (_, i) => ({
        value: i + 1,
        label: `Tháng ${i + 1}/${currentYear}`
      }));
    } else if (timeFilter === 'quarter') {
      return [
        { value: 1, label: 'Quý 1 (T1-T3)' },
        { value: 2, label: 'Quý 2 (T4-T6)' },
        { value: 3, label: 'Quý 3 (T7-T9)' },
        { value: 4, label: 'Quý 4 (T10-T12)' }
      ];
    } else {
      return Array.from({ length: 5 }, (_, i) => ({
        value: currentYear - i,
        label: `Năm ${currentYear - i}`
      }));
    }
  };

  if (!dashboardData) {
    return <div className="dashboard-loading">Đang tải dữ liệu...</div>;
  }

  return (
    <div className="dashboard-container">
      {/* Time Filter */}
      <div className="dashboard-filters">
        <div className="filter-group">
          <label>Chọn loại thời gian:</label>
          <select 
            value={timeFilter} 
            onChange={(e) => {
              setTimeFilter(e.target.value);
              setSelectedPeriod(e.target.value === 'year' ? new Date().getFullYear() : 
                               e.target.value === 'quarter' ? 1 : new Date().getMonth() + 1);
            }}
          >
            <option value="month">Theo tháng</option>
            <option value="quarter">Theo quý</option>
            <option value="year">Theo năm</option>
          </select>
        </div>
        <div className="filter-group">
          <label>Chọn khoảng thời gian:</label>
          <select 
            value={selectedPeriod} 
            onChange={(e) => setSelectedPeriod(Number(e.target.value))}
          >
            {getPeriodOptions().map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="dashboard-summary">
        <div className="summary-card completed">
          <div className="card-icon">✓</div>
          <div className="card-content">
            <h3>Đơn hàng hoàn thành</h3>
            <p className="card-value">{dashboardData.summary.completedOrders}</p>
          </div>
        </div>
        <div className="summary-card pending">
          <div className="card-icon">⏳</div>
          <div className="card-content">
            <h3>Đơn hàng chưa hoàn thành</h3>
            <p className="card-value">{dashboardData.summary.pendingOrders}</p>
          </div>
        </div>
        <div className="summary-card cars-sold">
          <div className="card-icon">🚗</div>
          <div className="card-content">
            <h3>Tổng xe bán được</h3>
            <p className="card-value">{dashboardData.summary.totalCarsSold}</p>
          </div>
        </div>
        <div className="summary-card revenue">
          <div className="card-icon">💰</div>
          <div className="card-content">
            <h3>Doanh thu</h3>
            <p className="card-value">{formatCurrency(dashboardData.summary.totalRevenue)}</p>
          </div>
        </div>
        <div className="summary-card profit">
          <div className="card-icon">📈</div>
          <div className="card-content">
            <h3>Lợi nhuận</h3>
            <p className="card-value">{formatCurrency(dashboardData.summary.totalProfit)}</p>
          </div>
        </div>
        <div className="summary-card imported">
          <div className="card-icon">📦</div>
          <div className="card-content">
            <h3>Số xe đã nhập</h3>
            <p className="card-value">{dashboardData.summary.totalCarsImported}</p>
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="dashboard-charts">
        {/* Chart 1: Total Revenue */}
        <div className="chart-container">
          <h3>Tổng doanh thu</h3>
          <div className="bar-chart">
            {dashboardData.charts.revenue.data.map((value, index) => {
              const maxValue = Math.max(...dashboardData.charts.revenue.data);
              const height = (value / maxValue) * 100;
              return (
                <div key={index} className="bar-wrapper">
                  <div 
                    className="bar revenue-bar" 
                    style={{ height: `${height}%` }}
                    title={formatCurrency(value)}
                  >
                    <span className="bar-value">{(value / 1000000000).toFixed(1)}B</span>
                  </div>
                  <div className="bar-label">{dashboardData.charts.revenue.labels[index]}</div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Chart 2: Cars Sold and Revenue */}
        <div className="chart-container">
          <h3>Tổng xe bán được và doanh thu</h3>
          <div className="dual-bar-chart">
            {dashboardData.charts.carsSoldRevenue.carsSold.map((cars, index) => {
              const revenue = dashboardData.charts.carsSoldRevenue.revenue[index];
              const maxCars = Math.max(...dashboardData.charts.carsSoldRevenue.carsSold);
              const maxRevenue = Math.max(...dashboardData.charts.carsSoldRevenue.revenue);
              return (
                <div key={index} className="dual-bar-wrapper">
                  <div className="dual-bars">
                    <div 
                      className="bar cars-bar" 
                      style={{ height: `${(cars / maxCars) * 100}%` }}
                      title={`${cars} xe`}
                    >
                      <span className="bar-value">{cars}</span>
                    </div>
                    <div 
                      className="bar revenue-bar-2" 
                      style={{ height: `${(revenue / maxRevenue) * 100}%` }}
                      title={formatCurrency(revenue)}
                    >
                      <span className="bar-value">{(revenue / 1000000000).toFixed(1)}B</span>
                    </div>
                  </div>
                  <div className="bar-label">{dashboardData.charts.carsSoldRevenue.labels[index]}</div>
                </div>
              );
            })}
          </div>
          <div className="chart-legend">
            <div className="legend-item">
              <span className="legend-color cars"></span>
              <span>Số xe bán</span>
            </div>
            <div className="legend-item">
              <span className="legend-color revenue"></span>
              <span>Doanh thu</span>
            </div>
          </div>
        </div>

        {/* Chart 3: Revenue by Staff */}
        <div className="chart-container">
          <h3>Doanh thu theo nhân viên</h3>
          <div className="stacked-bar-chart">
            {dashboardData.charts.staffRevenue.labels.map((label, index) => {
              const staffData = dashboardData.charts.staffRevenue.data.map(staff => staff[index]);
              const total = staffData.reduce((a, b) => a + b, 0);
              const maxTotal = Math.max(...dashboardData.charts.staffRevenue.labels.map((_, i) => 
                dashboardData.charts.staffRevenue.data.reduce((sum, staff) => sum + staff[i], 0)
              ));
              return (
                <div key={index} className="stacked-bar-wrapper">
                  <div className="stacked-bar" style={{ height: `${(total / maxTotal) * 100}%` }}>
                    {staffData.map((value, staffIndex) => {
                      const percentage = (value / total) * 100;
                      return (
                        <div 
                          key={staffIndex}
                          className={`stack-segment staff-${staffIndex}`}
                          style={{ height: `${percentage}%` }}
                          title={`${dashboardData.charts.staffRevenue.staffNames[staffIndex]}: ${formatCurrency(value)}`}
                        />
                      );
                    })}
                    <span className="bar-value">{(total / 1000000000).toFixed(1)}B</span>
                  </div>
                  <div className="bar-label">{label}</div>
                </div>
              );
            })}
          </div>
          <div className="chart-legend">
            {dashboardData.charts.staffRevenue.staffNames.map((name, index) => (
              <div key={index} className="legend-item">
                <span className={`legend-color staff-${index}`}></span>
                <span>{name}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Chart 4: Revenue and Import Cost */}
        <div className="chart-container">
          <h3>Doanh thu và Số tiền nhập xe</h3>
          <div className="dual-bar-chart">
            {dashboardData.charts.revenueImport.revenue.map((revenue, index) => {
              const importCost = dashboardData.charts.revenueImport.import[index];
              const maxValue = Math.max(...dashboardData.charts.revenueImport.revenue, ...dashboardData.charts.revenueImport.import);
              return (
                <div key={index} className="dual-bar-wrapper">
                  <div className="dual-bars">
                    <div 
                      className="bar revenue-bar-3" 
                      style={{ height: `${(revenue / maxValue) * 100}%` }}
                      title={`Doanh thu: ${formatCurrency(revenue)}`}
                    >
                      <span className="bar-value">{(revenue / 1000000000).toFixed(1)}B</span>
                    </div>
                    <div 
                      className="bar import-bar" 
                      style={{ height: `${(importCost / maxValue) * 100}%` }}
                      title={`Nhập xe: ${formatCurrency(importCost)}`}
                    >
                      <span className="bar-value">{(importCost / 1000000000).toFixed(1)}B</span>
                    </div>
                  </div>
                  <div className="bar-label">{dashboardData.charts.revenueImport.labels[index]}</div>
                </div>
              );
            })}
          </div>
          <div className="chart-legend">
            <div className="legend-item">
              <span className="legend-color revenue-3"></span>
              <span>Doanh thu</span>
            </div>
            <div className="legend-item">
              <span className="legend-color import"></span>
              <span>Nhập xe</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
