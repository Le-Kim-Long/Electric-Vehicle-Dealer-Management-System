import React, { useState, useEffect } from 'react';
import './Login.css';

const Login = ({ onLogin }) => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    rememberMe: false
  });
  
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);

  const API_BASE_URL = 'http://localhost:8080';
  
  const roles = [
    { value: 'DealerStaff', label: 'Nhân viên đại lý (Dealer Staff)' },
    { value: 'DealerManager', label: 'Quản lý đại lý (Dealer Manager)' },
    { value: 'EVMStaff', label: 'Nhân viên hãng xe (EVM Staff)' },
    { value: 'Admin', label: 'Quản trị viên (Admin)' }
  ];

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.username.trim()) {
      newErrors.username = 'Email không được để trống';
    } else if (!formData.username.includes('@')) {
      newErrors.username = 'Vui lòng nhập email hợp lệ';
    }
    
    if (!formData.password.trim()) {
      newErrors.password = 'Mật khẩu không được để trống';
    }
    
    // Loại bỏ validation dealerName vì server tự xác định
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const loginAPI = async (credentials) => {
    try {
      console.log('Making API call to:', `${API_BASE_URL}/api/auth/login`); // DEBUG
      
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: credentials.username,
          password: credentials.password
        })
      });

      console.log('Response status:', response.status); // DEBUG

      if (!response.ok) {
        const errorText = await response.text();
        console.error('API Error:', errorText); // DEBUG
        throw new Error(`Login failed: ${response.status} - ${errorText}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('API call failed:', error);
      throw error;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setIsLoading(true);
    
    try {
      const response = await loginAPI(formData);
      
      if (response && response.token) {
        const userData = {
          token: response.token,
          role: response.role,
          username: response.username,
          status: response.status,
          dealerName: response.dealerName
        };
        
        // LƯU CẢ userData VÀ token riêng
        localStorage.setItem('userData', JSON.stringify(userData));
        localStorage.setItem('token', response.token); // ← THÊM DÒNG NÀY
        
        if (formData.rememberMe) {
          localStorage.setItem('rememberedUser', JSON.stringify({
            username: formData.username
          }));
        }
        
        onLogin(userData);
      } else {
        setErrors({ general: 'Đăng nhập thất bại - không nhận được token' });
      }
    } catch (error) {
      console.error('Login error:', error);
      setErrors({ general: `Đăng nhập thất bại: ${error.message}` });
    } finally {
      setIsLoading(false);
    }
  };

  const handleForgotPassword = () => {
    setShowForgotPassword(!showForgotPassword);
  };

  useEffect(() => {
    // Sửa key từ 'rememberedUsername' thành 'rememberedUser'
    const rememberedUser = localStorage.getItem('rememberedUser');
    if (rememberedUser) {
      try {
        const userData = JSON.parse(rememberedUser);
        setFormData(prev => ({
          ...prev,
          username: userData.username,
          rememberMe: true
        }));
      } catch (error) {
        console.error('Error parsing remembered user:', error);
      }
    }
  }, []);

  return (
    <div className="login-container">      
      <div className="login-form-container">
        <div className="login-header">
          <h1>EV Dealer Management</h1>
          <p>Hệ thống quản lý bán xe điện thông qua kênh đại lý</p>
        </div>
        
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">
              Email đăng nhập
            </label>
            <input
              type="email"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              className={errors.username ? 'error' : ''}
              placeholder="Nhập email đăng nhập"
              disabled={isLoading}
            />
            {errors.username && <span className="error-message">{errors.username}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="password">
              Mật khẩu
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              className={errors.password ? 'error' : ''}
              placeholder="Nhập mật khẩu"
              disabled={isLoading}
            />
            {errors.password && <span className="error-message">{errors.password}</span>}
          </div>

          <div className="form-options">
            <div className="remember-me">
              <input
                type="checkbox"
                id="rememberMe"
                name="rememberMe"
                checked={formData.rememberMe}
                onChange={handleInputChange}
                disabled={isLoading}
              />
              <label htmlFor="rememberMe" className="checkbox-label">
                Ghi nhớ đăng nhập
              </label>
            </div>
            
            <button
              type="button"
              className="forgot-password-btn"
              onClick={handleForgotPassword}
              disabled={isLoading}
            >
              Quên mật khẩu?
            </button>
          </div>

          {showForgotPassword && (
            <div className="forgot-password-section">
              <p className="forgot-password-text">
                Vui lòng liên hệ quản trị viên hệ thống để đặt lại mật khẩu.
              </p>
              <p className="contact-info">
                Email: admin@evdealer.com | Hotline: 1900-123-456
              </p>
            </div>
          )}

          {errors.general && (
            <div className="error-message general-error">
              {errors.general}
            </div>
          )}

          <button 
            type="submit" 
            className={`login-button ${isLoading ? 'loading' : ''}`}
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <span className="spinner"></span>
                Đang đăng nhập...
              </>
            ) : (
              'Đăng nhập'
            )}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
