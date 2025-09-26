import React, { useState } from 'react';
import './Login.css';

const Login = ({ onLogin }) => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    role: 'dealer-staff',
    rememberMe: false
  });
  
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);

  const roles = [
    { value: 'dealer-staff', label: 'Nhân viên đại lý (Dealer Staff)' },
    { value: 'dealer-manager', label: 'Quản lý đại lý (Dealer Manager)' },
    { value: 'evm-staff', label: 'Nhân viên hãng xe (EVM Staff)' },
    { value: 'admin', label: 'Quản trị viên (Admin)' }
  ];

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    
    // Clear error when user starts typing
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
      newErrors.username = 'Tên đăng nhập không được để trống';
    } else if (formData.username.length < 3) {
      newErrors.username = 'Tên đăng nhập phải có ít nhất 3 ký tự';
    }
    
    if (!formData.password.trim()) {
      newErrors.password = 'Mật khẩu không được để trống';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsLoading(true);
    
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Mock user database - In real app, this would be API call
      const users = {
        // Dealer Staff accounts
        'staff1': { password: '123456', role: 'dealer-staff', name: 'Nguyễn Văn A' },
        'staff2': { password: '123456', role: 'dealer-staff', name: 'Trần Thị B' },
        'nhanvien1': { password: 'dealer123', role: 'dealer-staff', name: 'Lê Văn C' },
        
        // Dealer Manager accounts  
        'manager1': { password: '123456', role: 'dealer-manager', name: 'Nguyễn Quản Lý' },
        'quanly1': { password: 'manager123', role: 'dealer-manager', name: 'Trần Quản Lý' },
        'admin_dealer': { password: 'dealer2024', role: 'dealer-manager', name: 'Phạm Giám Đốc' },
        
        // EVM Staff accounts
        'evm1': { password: '123456', role: 'evm-staff', name: 'Nguyễn EVM' },
        'hangxe1': { password: 'evm2024', role: 'evm-staff', name: 'Lê Nhân Viên Hãng' },
        'evm_staff': { password: 'evstaff123', role: 'evm-staff', name: 'Trần Hãng Xe' },
        
        // Admin accounts
        'admin': { password: '123456', role: 'admin', name: 'Super Admin' },
        'admin1': { password: 'admin2024', role: 'admin', name: 'Quản Trị Viên' },
        'root': { password: 'root123', role: 'admin', name: 'Root Admin' }
      };
      
      // Check credentials
      const user = users[formData.username];
      const isValidUser = user && user.password === formData.password && user.role === formData.role;
      
      if (isValidUser) {
        const userData = {
          username: formData.username,
          role: formData.role,
          roleLabel: roles.find(r => r.value === formData.role)?.label,
          name: user.name
        };
        
        // Store user info in localStorage for persistence
        if (formData.rememberMe) {
          localStorage.setItem('user', JSON.stringify(userData));
          localStorage.setItem('rememberedUsername', formData.username);
        } else {
          localStorage.setItem('user', JSON.stringify(userData));
          localStorage.removeItem('rememberedUsername');
        }
        
        if (onLogin) {
          onLogin(userData);
        }
      } else {
        setErrors({ general: 'Tên đăng nhập, mật khẩu hoặc vai trò không đúng' });
      }
    } catch (error) {
      setErrors({ general: 'Có lỗi xảy ra. Vui lòng thử lại.' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleForgotPassword = () => {
    setShowForgotPassword(!showForgotPassword);
  };

  // Load remembered username on component mount
  React.useEffect(() => {
    const rememberedUsername = localStorage.getItem('rememberedUsername');
    if (rememberedUsername) {
      setFormData(prev => ({
        ...prev,
        username: rememberedUsername,
        rememberMe: true
      }));
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
              <span className="label-icon"></span>
              Tên đăng nhập
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              className={errors.username ? 'error' : ''}
              placeholder="Nhập tên đăng nhập"
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

          <div className="form-group">
            <label htmlFor="role">
              Vai trò
            </label>
            <select
              id="role"
              name="role"
              value={formData.role}
              onChange={handleInputChange}
              disabled={isLoading}
            >
              {roles.map(role => (
                <option key={role.value} value={role.value}>
                  {role.label}
                </option>
              ))}
            </select>
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
              <>
                Đăng nhập
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
