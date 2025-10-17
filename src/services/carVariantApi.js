const API_BASE_URL = 'http://localhost:8080/api';

const getAuthToken = () => {
  return localStorage.getItem('token');
};

export const getCurrentUser = () => {
  const userDataStr = localStorage.getItem('userData');
  if (!userDataStr) return null;
  
  try {
    return JSON.parse(userDataStr);
  } catch (error) {
    console.error('Error parsing user data:', error);
    return null;
  }
};

export const getCarVariantDetails = async () => {
  try {
    const token = getAuthToken();
    
    if (!token) {
      throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
    }
    
    const response = await fetch(`${API_BASE_URL}/car-variants/details`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();

  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};

export const searchCarVariants = async (query) => {
  try {
    const token = getAuthToken();
    
    if (!token) {
      throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
    }
    
    const response = await fetch(`${API_BASE_URL}/car-variants/search?q=${encodeURIComponent(query)}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();

  } catch (error) {
    console.error('Search Error:', error);
    throw error;
  }
};

export const getVariantConfiguration = async (variantId) => {
  try {
    const token = getAuthToken();
    
    if (!token) {
      throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
    }
    
    const response = await fetch(`${API_BASE_URL}/configurations/variant/${variantId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();

  } catch (error) {
    console.error('API Error:', error);
    return null;
  }
};

export const transformCarVariantData = (apiData) => {
  if (!Array.isArray(apiData)) {
    console.error('API data is not an array:', apiData);
    return [];
  }

  return apiData.map(variant => {
    const modelName = variant.modelName || 'Unknown';
    const variantName = variant.variantName || 'Unknown';
    const fullName = `VinFast ${modelName} ${variantName}`;

    const colors = variant.colorPrices?.map(cp => ({
      name: cp.colorName || 'Unknown',
      price: cp.price || 0,
      image: cp.imagePath ? `http://localhost:8080${cp.imagePath}` : null,
      quantity: cp.quantity || 0
    })) || [];

    const prices = colors.map(c => c.price).filter(p => p > 0);
    const quantities = colors.map(c => c.quantity);
    const totalStock = quantities.reduce((sum, q) => sum + q, 0);
    
    const minPrice = prices.length > 0 ? Math.min(...prices) : 0;
    const maxPrice = prices.length > 0 ? Math.max(...prices) : 0;
    
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

    return {
      id: variant.variantId,
      name: fullName,
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
  });
};

export const transformConfigurationData = (configData) => {
  if (!configData) return null;

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
// Search car variants by modelName (dòng xe)
export const searchCarVariantsByModelName = async (modelName) => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/car-variants/search/model-name?modelName=${encodeURIComponent(modelName)}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to search car variants by modelName');
  return response.json();
};

// Search car variants by variantName (phiên bản)
export const searchCarVariantsByVariantName = async (variantName) => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/car-variants/search/variant-name?variantName=${encodeURIComponent(variantName)}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to search car variants by variantName');
  return response.json();
};