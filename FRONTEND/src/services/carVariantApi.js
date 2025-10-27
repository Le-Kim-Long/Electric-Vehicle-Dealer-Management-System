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
// Search car variants by both modelName and variantName
export const searchCarVariantsByModelAndVariant = async (modelName, variantName) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const url = `${API_BASE_URL}/car-variants/search/model-and-variant?modelName=${encodeURIComponent(modelName)}&variantName=${encodeURIComponent(variantName)}`;
  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
};
// Lấy danh sách xe theo tên đại lý
export const getCarVariantsByDealerName = async (dealerName) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const url = `${API_BASE_URL}/dealers/car-variants/${encodeURIComponent(dealerName)}`;
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
};
// Lấy danh sách tên đại lý
export const fetchDealerNames = async () => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/dealers/names`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to fetch dealer names');
  return response.json();
};
// Thêm xe mới (complete car)
export const addCompleteCar = async (carData) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const response = await fetch(`${API_BASE_URL}/cars/add-complete-car`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify(carData)
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
};
// Lấy tất cả model-names
export const fetchAllModelNames = async () => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/car-models/model-names`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to fetch all model names');
  return response.json();
};
// Lấy cấu hình theo modelName và variantName
export const fetchConfigurationByModelAndVariant = async (modelName, variantName) => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/configurations/variant-name?modelName=${encodeURIComponent(modelName)}&variantName=${encodeURIComponent(variantName)}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to fetch configuration by modelName and variantName');
  return response.json();
};
// Lấy description theo modelName và variantName
export const fetchDescriptionByModelAndVariant = async (modelName, variantName) => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/car-variants/description?modelName=${encodeURIComponent(modelName)}&variantName=${encodeURIComponent(variantName)}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to fetch description by modelName and variantName');
  return response.text();
};
// Lấy segment theo modelName
export const fetchSegmentByModelName = async (modelName) => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/car-models/segment?modelName=${encodeURIComponent(modelName)}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to fetch segment by modelName');
  return response.text();
};
// Lấy danh sách variantName theo modelName
export const fetchVariantNamesByModel = async (modelName) => {
  const token = getAuthToken();
  const response = await fetch(`${API_BASE_URL}/car-variants/variant-names/by-model?modelName=${encodeURIComponent(modelName)}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) throw new Error('Failed to fetch variant names by model');
  return response.json();
};
// Thêm xe vào đại lý
export const addCarToDealer = async ({ variantId, colorName, dealerName, quantity }) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const response = await fetch(`${API_BASE_URL}/cars/add-to-dealer`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify({ variantId, colorName, dealerName, quantity })
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.text();
};
// Cập nhật cấu hình theo modelName và variantName
export const updateConfigurationByModelAndVariant = async (modelName, variantName, configData) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const url = `${API_BASE_URL}/configurations/update?modelName=${encodeURIComponent(modelName)}&variantName=${encodeURIComponent(variantName)}`;
  const response = await fetch(url, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify(configData)
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
};
// Cập nhật giá xe theo modelName, variantName, colorName
export const updateManufacturerPriceByModelVariantColor = async (modelName, variantName, colorName, newPrice) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const url = `${API_BASE_URL}/cars/update-manufacturer-price?modelName=${encodeURIComponent(modelName)}&variantName=${encodeURIComponent(variantName)}&colorName=${encodeURIComponent(colorName)}`;
  const response = await fetch(url, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify({ price: newPrice })
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
};
// Lấy danh sách màu theo modelName và variantName
export const fetchColorsByModelAndVariant = async (modelName, variantName) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const url = `${API_BASE_URL}/colors/by-model-variant?modelName=${encodeURIComponent(modelName)}&variantName=${encodeURIComponent(variantName)}`;
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
};
// Lấy giá tiền theo modelName, variantName, colorName
export const fetchManufacturerPriceByModelVariantColor = async (modelName, variantName, colorName) => {
  const token = getAuthToken();
  if (!token) throw new Error('Không tìm thấy token. Vui lòng đăng nhập lại.');
  const url = `${API_BASE_URL}/cars/manufacturer-price?modelName=${encodeURIComponent(modelName)}&variantName=${encodeURIComponent(variantName)}&colorName=${encodeURIComponent(colorName)}`;
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json'
    }
  });
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
    }
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json();
};