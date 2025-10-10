const API_BASE_URL = 'http://localhost:8080/api';

// Lấy token từ localStorage
const getAuthToken = () => {
  return localStorage.getItem('token');
};

// Lấy thông tin user hiện tại
export const getCurrentUser = () => {
  const userDataStr = localStorage.getItem('userData');
  if (!userDataStr) {
    console.warn('⚠️ No user data in localStorage');
    return null;
  }
  
  try {
    const userData = JSON.parse(userDataStr);
    console.log('👤 Current user:', userData);
    return userData;
  } catch (error) {
    console.error('❌ Error parsing user data:', error);
    return null;
  }
};

// 📡 Gọi API lấy danh sách xe theo đại lý
export const getCarVariantDetails = async () => {
  try {
    const token = getAuthToken();
    console.log('🔐 Token exists:', !!token);
    
    if (!token) {
      throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
    }

    console.log('📡 Calling API: GET /car-variants/details');
    
    const response = await fetch(`${API_BASE_URL}/car-variants/details`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    console.log('📊 Response status:', response.status);

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log('✅ API Response:', data);
    return data;

  } catch (error) {
    console.error('❌ API Error:', error);
    throw error;
  }
};

// ✅ Search xe theo query
export const searchCarVariants = async (query) => {
  try {
    const token = getAuthToken();
    
    if (!token) {
      throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
    }

    console.log(`🔍 Calling API: GET /car-variants/search?q=${query}`);
    
    const response = await fetch(`${API_BASE_URL}/car-variants/search?q=${encodeURIComponent(query)}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    console.log('📊 Response status:', response.status);

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log('✅ Search results:', data);
    return data;

  } catch (error) {
    console.error('❌ Search Error:', error);
    throw error;
  }
};

// ✅ Lấy thông tin cấu hình chi tiết của từng variant
export const getVariantConfiguration = async (variantId) => {
  try {
    const token = getAuthToken();
    
    if (!token) {
      throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
    }

    console.log(`📡 Calling API: GET /configurations/variant/${variantId}`);
    
    const response = await fetch(`${API_BASE_URL}/configurations/variant/${variantId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    console.log('📊 Response status:', response.status);

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log('✅ Configuration data:', data);
    return data;

  } catch (error) {
    console.error('❌ API Error:', error);
    return null;
  }
};

// 🔄 Transform data từ API sang format hiển thị
export const transformCarVariantData = (apiData) => {
  if (!Array.isArray(apiData)) {
    console.error('❌ API data is not an array:', apiData);
    return [];
  }

  console.log('🔄 Transforming data for', apiData.length, 'variants');

  return apiData.map(variant => {
    const modelName = variant.modelName || 'Unknown';
    const variantName = variant.variantName || 'Unknown';
    const fullName = `VinFast ${modelName} ${variantName}`;

    // ✅ Transform colors từ API
    const colors = variant.colorPrices?.map(cp => ({
      name: cp.colorName || 'Unknown',
      price: cp.price || 0,
      image: cp.imagePath ? `http://localhost:8080${cp.imagePath}` : null,
      quantity: cp.quantity || 0
    })) || [];

    // Tính giá và tồn kho
    const prices = colors.map(c => c.price).filter(p => p > 0);
    const quantities = colors.map(c => c.quantity);
    const totalStock = quantities.reduce((sum, q) => sum + q, 0);
    
    const minPrice = prices.length > 0 ? Math.min(...prices) : 0;
    const maxPrice = prices.length > 0 ? Math.max(...prices) : 0;
    
    // Format price range
    let priceRange = '';
    if (minPrice === maxPrice) {
      priceRange = `${(minPrice / 1000000).toFixed(0)} triệu`;
    } else {
      const minFormatted = minPrice >= 1000000000 
        ? `${(minPrice / 1000000000).toFixed(2)} tỷ`
        : `${(minPrice / 1000000).toFixed(0)} triệu`;
      const maxFormatted = maxPrice >= 1000000000
        ? `${(maxPrice / 1000000000).toFixed(2)} tỷ`
        : `${(maxPrice / 1000000).toFixed(0)} triệu`;
      priceRange = `${minFormatted} - ${maxFormatted}`;
    }

    // Determine status based on stock
    let status = 'available';
    if (totalStock === 0) {
      status = 'out-of-stock';
    } else if (totalStock < 10) {
      status = 'low-stock';
    }

    const defaultImage = colors[0]?.image || '/images/default-car.png';
    
    const images = {};
    const colorPrices = {};
    colors.forEach(color => {
      images[color.name] = color.image || defaultImage;
      colorPrices[color.name] = color.price;
    });

    const result = {
      id: variant.variantId,
      name: fullName,
      // ✅ XÓA brand (mock data)
      // brand: 'VinFast',
      modelName: modelName,
      variantName: variantName,
      price: minPrice,
      priceRange: priceRange,
      colors: colors.map(c => c.name),
      colorPrices: colorPrices,
      images: images,
      defaultImage: defaultImage,
      stock: totalStock,
      colorQuantities: colors.reduce((obj, c) => {
        obj[c.name] = c.quantity;
        return obj;
      }, {}),
      status: status,
      configLoaded: false,
      specs: null
    };

    console.log('✅ Transformed vehicle:', result);
    return result;
  });
};

// ✅ Transform configuration data từ API
export const transformConfigurationData = (configData) => {
  if (!configData) {
    console.warn('⚠️ No configuration data');
    return null;
  }

  console.log('🔄 Transforming configuration:', configData);

  return {
    battery: `${configData.batteryCapacity} kWh (${configData.batteryType})`,
    range: configData.rangeKm,
    charging: `${configData.fullChargeTime} phút (AC)`,
    power: configData.power,
    seats: configData.seats,
    torque: `${configData.torque} Nm`,
    dimensions: `${configData.lengthMm}x${configData.widthMm}x${configData.heightMm} mm`,
    wheelbase: `${configData.wheelbaseMm} mm`,
    weight: `${configData.weightKg} kg`,
    batteryType: configData.batteryType
  };
};