package org.example.service.implementation;

import org.example.dto.DealerVariantDetailResponse;
import org.example.dto.VariantDetailResponse;
import org.example.entity.CarVariant;
import org.example.entity.Color;
import org.example.entity.UserAccount;
import org.example.repository.CarVariantRepository;
import org.example.repository.ColorRepository;
import org.example.repository.UserAccountRepository;
import org.example.service.CarVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CarVariantServiceImpl implements CarVariantService {

    @Autowired
    private CarVariantRepository carVariantRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private VariantDetailResponse convertToVariantDetailResponse(CarVariant carVariant, Integer dealerId) {
        // Lấy thông tin colorId, price, imagePath và quantity từ xe thuộc dealer hiện tại
        List<Object[]> colorIdsAndPrices = carVariantRepository.findColorIdsAndPricesByVariantIdAndDealerId(
            carVariant.getVariantId(), dealerId);

        // Tạo Map từ colorId -> (price, imagePath, quantity)
        Map<Integer, Object[]> colorDataMap = colorIdsAndPrices.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0],  // colorId
                    row -> new Object[]{row[1], row[2], row[3]}, // [price, imagePath, quantity]
                    (existing, replacement) -> existing // Nếu có duplicate, giữ giá trị đầu tiên
                ));

        // Lấy thông tin màu sắc từ ColorRepository
        List<Integer> colorIds = colorIdsAndPrices.stream()
                .map(row -> (Integer) row[0])
                .distinct()
                .toList();

        List<Color> colors = colorRepository.findByColorIds(colorIds);

        // Tạo danh sách ColorPrice
        List<VariantDetailResponse.ColorPrice> colorPrices = colors.stream()
                .filter(color -> colorDataMap.containsKey(color.getColorId()))
                .map(color -> {
                    Object[] data = colorDataMap.get(color.getColorId());
                    String imageName = (String) data[1];
                    // Xây dựng URL để truy cập ảnh qua HTTP
                    String imageUrl = null;
                    if (imageName != null && !imageName.isEmpty()) {
                        imageUrl = "/api/images/" + imageName;
                    }
                    return VariantDetailResponse.ColorPrice.builder()
                            .colorName(color.getColorName())
                            .price((Long) data[0])
                            .imagePath(imageUrl)
                            .quantity((Integer) data[2])
                            .build();
                })
                .sorted(Comparator.comparing(VariantDetailResponse.ColorPrice::getColorName))
                .toList();

        return VariantDetailResponse.builder()
                .variantId(carVariant.getVariantId())
                .modelName(carVariant.getCarModel().getModelName())
                .variantName(carVariant.getVariantName())
                .colorPrices(colorPrices)
                .build();
    }

    @Override
    public List<VariantDetailResponse> getVariantDetailsByCurrentDealer(String userEmail) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        // Lấy car variants theo dealer ID với configuration
        List<CarVariant> carVariants = carVariantRepository.findVariantsByDealerIdWithConfiguration(dealerId);

        return carVariants.stream()
                .map(cv -> convertToVariantDetailResponse(cv, dealerId))
                .toList();
    }

    @Override
    public List<VariantDetailResponse> searchVariantsByCurrentDealer(String userEmail, String searchTerm) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        // Xử lý search term linh hoạt để xử lý khoảng trắng và search không đầy đủ
        String processedSearchTerm = processFlexibleSearchTerm(searchTerm);

        // Lấy car variants theo dealer ID và search term với configuration
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndTerm(dealerId, processedSearchTerm);

        return carVariants.stream()
                .map(cv -> convertToVariantDetailResponse(cv, dealerId))
                .toList();
    }

    private String processFlexibleSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return "";
        }

        // Chuẩn hóa cơ bản: trim và loại bỏ khoảng trắng thừa
        String processed = searchTerm.trim().replaceAll("\\s+", " ").toLowerCase();

        // Xử lý các trường hợp đặc biệt
        processed = handleSpecialCases(processed);

        return processed;
    }

    private String handleSpecialCases(String term) {
        // Xử lý các pattern không đầy đủ

        // Case 1: Chỉ có số (ví dụ: "3" -> có thể tìm "vf3")
        if (term.matches("^\\d+$")) {
            // Để nguyên, query sẽ tìm trong cả modelName và variantName
            return term;
        }

        // Case 2: Có khoảng trắng nhưng thiếu từ (ví dụ: "vf eco", "3 plus")
        String[] parts = term.split("\\s+");
        if (parts.length == 2) {
            String part1 = parts[0];
            String part2 = parts[1];

            // Xử lý trường hợp thiếu "vf" ở đầu (ví dụ: "3 eco" -> "vf3 eco")
            if (part1.matches("^\\d+$")) {
                return "vf" + part1 + " " + part2;
            }

            // Xử lý trường hợp "vf eco" - tìm tất cả model VF có variant eco
            if (part1.equals("vf") && !part2.matches("^\\d+")) {
                // Để nguyên "vf eco" - query sẽ tìm trong CONCAT(modelName, ' ', variantName)
                return term;
            }

            // Xử lý trường hợp "vfe" viết tắt - có thể là "vf eco"
            if (part1.startsWith("vf") && part1.length() <= 4) {
                // Kiểm tra nếu là viết tắt của variant
                String possibleVariant = expandAbbreviation(part1.substring(2));
                if (!possibleVariant.isEmpty()) {
                    return "vf " + possibleVariant;
                }
            }
        }

        // Case 3: Không có khoảng trắng nhưng có thể là kết hợp (ví dụ: "vf3eco")
        if (!term.contains(" ")) {
            // Xử lý trường hợp "vfe" - viết tắt của "vf eco"
            if (term.matches("^vf[a-z]+$") && !term.matches("^vf\\d+")) {
                String abbreviation = term.substring(2);
                String expandedVariant = expandAbbreviation(abbreviation);
                if (!expandedVariant.isEmpty()) {
                    return "vf " + expandedVariant;
                }
            }

            // Tách model và variant nếu có thể
            String expandedTerm = expandCombinedTerm(term);
            if (!expandedTerm.equals(term)) {
                return expandedTerm;
            }
        }

        return term;
    }

    private String expandAbbreviation(String abbreviation) {
        // Map các viết tắt phổ biến
        return switch (abbreviation.toLowerCase()) {
            case "e", "ec" -> "eco";
            case "p", "pl" -> "plus";
            case "pr", "pro" -> "pro";
            case "prem" -> "premium";
            default -> abbreviation; // Nếu không phải viết tắt đặc biệt, để nguyên
        };
    }

    private String expandCombinedTerm(String term) {
        // Xử lý các pattern kết hợp phổ biến

        // Pattern: số + chữ (ví dụ: "3eco" -> "vf3 eco")
        if (term.matches("^\\d+[a-z]+$")) {
            String number = term.replaceAll("[a-z]+$", "");
            String variant = term.replaceAll("^\\d+", "");
            return "vf" + number + " " + variant;
        }

        // Pattern: vf + số + chữ (ví dụ: "vf3eco" -> "vf3 eco")
        if (term.matches("^vf\\d+[a-z]+$")) {
            String modelPart = term.replaceAll("[a-z]+$", "");
            String variantPart = term.replaceAll("^vf\\d+", "");
            return modelPart + " " + variantPart;
        }

        // Pattern: v + số + chữ (ví dụ: "v3eco" -> "vf3 eco")
        if (term.matches("^v\\d+[a-z]+$")) {
            String number = term.replaceAll("^v", "").replaceAll("[a-z]+$", "");
            String variant = term.replaceAll("^v\\d+", "");
            return "vf" + number + " " + variant;
        }

        return term;
    }

    // Methods for Admin/EVMStaff to view all variants in system
    @Override
    public List<VariantDetailResponse> getAllVariantDetailsInSystem() {
        // Lấy tất cả car variants trong hệ thống với configuration
        List<CarVariant> carVariants = carVariantRepository.findAllVariantsWithConfiguration();

        return carVariants.stream()
                .map(this::convertToVariantDetailResponseForSystemWithoutQuantity)
                .toList();
    }

    @Override
    public List<VariantDetailResponse> searchVariantsInSystem(String searchTerm) {
        // Xử lý search term linh hoạt
        String processedSearchTerm = processFlexibleSearchTerm(searchTerm);

        // Lấy tất cả car variants theo search term với configuration (toàn hệ thống)
        List<CarVariant> carVariants = carVariantRepository.searchAllVariantsWithConfiguration(processedSearchTerm);

        return carVariants.stream()
                .map(this::convertToVariantDetailResponseForSystemWithoutQuantity)
                .toList();
    }

    @Override
    public List<VariantDetailResponse> getVariantDetailsByDealerName(String dealerName) {
        // Lấy car variants theo dealer name với configuration
        List<CarVariant> carVariants = carVariantRepository.findVariantsByDealerNameWithConfiguration(dealerName);

        return carVariants.stream()
                .map(cv -> convertToVariantDetailResponseByDealerName(cv, dealerName))
                .toList();
    }

    private VariantDetailResponse convertToVariantDetailResponseByDealerName(CarVariant carVariant, String dealerName) {
        // Lấy thông tin colorId, price, imagePath và quantity từ xe thuộc dealer có tên này
        List<Object[]> colorIdsAndPrices = carVariantRepository.findColorIdsAndPricesByVariantIdAndDealerName(
            carVariant.getVariantId(), dealerName);

        // Tạo Map từ colorId -> (price, imagePath, quantity)
        Map<Integer, Object[]> colorDataMap = colorIdsAndPrices.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0],  // colorId
                    row -> new Object[]{row[1], row[2], row[3]}, // [price, imagePath, quantity]
                    (existing, replacement) -> existing // Nếu có duplicate, giữ giá trị đầu tiên
                ));

        // Lấy thông tin màu sắc từ ColorRepository
        List<Integer> colorIds = colorIdsAndPrices.stream()
                .map(row -> (Integer) row[0])
                .distinct()
                .toList();

        List<Color> colors = colorRepository.findByColorIds(colorIds);

        // Tạo danh sách ColorPrice
        List<VariantDetailResponse.ColorPrice> colorPrices = colors.stream()
                .filter(color -> colorDataMap.containsKey(color.getColorId()))
                .map(color -> {
                    Object[] data = colorDataMap.get(color.getColorId());
                    String imageName = (String) data[1];
                    // Xây dựng URL để truy cập ảnh qua HTTP
                    String imageUrl = null;
                    if (imageName != null && !imageName.isEmpty()) {
                        imageUrl = "/api/images/" + imageName;
                    }
                    return VariantDetailResponse.ColorPrice.builder()
                            .colorName(color.getColorName())
                            .price((Long) data[0])
                            .imagePath(imageUrl)
                            .quantity((Integer) data[2])
                            .build();
                })
                .sorted(Comparator.comparing(VariantDetailResponse.ColorPrice::getColorName))
                .toList();

        return VariantDetailResponse.builder()
                .variantId(carVariant.getVariantId())
                .modelName(carVariant.getCarModel().getModelName())
                .variantName(carVariant.getVariantName())
                .colorPrices(colorPrices)
                .build();
    }

    // Method for EVMStaff and Admin - no quantity information needed
    private VariantDetailResponse convertToVariantDetailResponseForSystemWithoutQuantity(CarVariant carVariant) {
        // Lấy thông tin colorId, price, imagePath từ tất cả xe trong hệ thống (không cần quantity)
        List<Object[]> colorIdsAndPrices = carVariantRepository.findColorIdsAndPricesWithoutQuantityByVariantId(carVariant.getVariantId());

        // Tạo Map từ colorId -> (price, imagePath)
        Map<Integer, Object[]> colorDataMap = colorIdsAndPrices.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0],  // colorId
                    row -> new Object[]{row[1], row[2]}, // [price, imagePath]
                    (existing, replacement) -> existing
                ));

        // Lấy thông tin màu sắc từ ColorRepository
        List<Integer> colorIds = colorIdsAndPrices.stream()
                .map(row -> (Integer) row[0])
                .distinct()
                .toList();

        List<Color> colors = colorRepository.findByColorIds(colorIds);

        // Tạo danh sách ColorPrice không có quantity
        List<VariantDetailResponse.ColorPrice> colorPrices = colors.stream()
                .filter(color -> colorDataMap.containsKey(color.getColorId()))
                .map(color -> {
                    Object[] data = colorDataMap.get(color.getColorId());
                    String imageName = (String) data[1];
                    // Xây dựng URL để truy cập ảnh qua HTTP
                    String imageUrl = null;
                    if (imageName != null && !imageName.isEmpty()) {
                        imageUrl = "/api/images/" + imageName;
                    }

                    return VariantDetailResponse.ColorPrice.builder()
                            .colorName(color.getColorName())
                            .price((Long) data[0])
                            .imagePath(imageUrl)
                            .quantity(null) // Không trả về quantity cho EVMStaff và Admin
                            .build();
                })
                .sorted(Comparator.comparing(VariantDetailResponse.ColorPrice::getColorName))
                .toList();

        return VariantDetailResponse.builder()
                .variantId(carVariant.getVariantId())
                .modelName(carVariant.getCarModel().getModelName())
                .variantName(carVariant.getVariantName())
                .colorPrices(colorPrices)
                .build();
    }

    // New methods for searching by variant name specifically
    @Override
    public List<VariantDetailResponse> searchVariantsByVariantNameInSystem(String variantName) {
        // Lấy tất cả car variants theo variant name với configuration (toàn hệ thống)
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByVariantNameInSystem(variantName);

        return carVariants.stream()
                .map(this::convertToVariantDetailResponseForSystemWithoutQuantity)
                .toList();
    }

    @Override
    public List<VariantDetailResponse> searchVariantsByVariantNameAndCurrentDealer(String userEmail, String variantName) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        // Lấy car variants theo dealer ID và variant name với configuration
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByVariantNameAndDealerId(dealerId, variantName);

        return carVariants.stream()
                .map(cv -> convertToVariantDetailResponse(cv, dealerId))
                .toList();
    }

    // New methods for searching by model name specifically
    @Override
    public List<VariantDetailResponse> searchVariantsByModelNameInSystem(String modelName) {
        // Lấy tất cả car variants theo model name với configuration (toàn hệ thống)
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByModelNameInSystem(modelName);

        return carVariants.stream()
                .map(this::convertToVariantDetailResponseForSystemWithoutQuantity)
                .toList();
    }

    @Override
    public List<VariantDetailResponse> searchVariantsByModelNameAndCurrentDealer(String userEmail, String modelName) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        // Lấy car variants theo dealer ID và model name với configuration
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByModelNameAndDealerId(dealerId, modelName);

        return carVariants.stream()
                .map(cv -> convertToVariantDetailResponse(cv, dealerId))
                .toList();
    }

    // New methods for searching by both model name and variant name
    @Override
    public List<VariantDetailResponse> searchVariantsByModelAndVariantNameInSystem(String modelName, String variantName) {
        // Lấy car variants theo cả model name và variant name trong toàn hệ thống
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByModelAndVariantNameInSystem(modelName, variantName);

        return carVariants.stream()
                .map(this::convertToVariantDetailResponseForSystemWithoutQuantity)
                .toList();
    }

    @Override
    public List<VariantDetailResponse> searchVariantsByModelAndVariantNameAndCurrentDealer(String userEmail, String modelName, String variantName) {
        // Tìm user theo email
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Kiểm tra user có thuộc dealer nào không
        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        // Lấy car variants theo dealer ID, model name và variant name với configuration
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByModelAndVariantNameAndDealerId(dealerId, modelName, variantName);

        return carVariants.stream()
                .map(cv -> convertToVariantDetailResponse(cv, dealerId))
                .toList();
    }

    @Override
    public List<String> getAllVariantNames() {
        return carVariantRepository.findAllVariantNames();
    }

    @Override
    public String getDescriptionByVariantName(String variantName) {
        return carVariantRepository.findDescriptionByVariantName(variantName)
                .orElse(null);
    }

    @Override
    public String getDescriptionByModelNameAndVariantName(String modelName, String variantName) {
        return carVariantRepository.findDescriptionByModelNameAndVariantName(modelName, variantName)
                .orElse(null);
    }

    @Override
    public List<String> getVariantNamesByModelName(String modelName) {
        return carVariantRepository.findVariantNamesByModelName(modelName);
    }

    @Override
    public List<DealerVariantDetailResponse> getDealerVariantDetailsForManager(Integer dealerId) {
        // Lấy tất cả xe của dealer (bao gồm cả pending và on sale) - cho dealer manager
        List<CarVariant> carVariants = carVariantRepository.findVariantsByDealerIdWithConfiguration(dealerId);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, false))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> getDealerVariantDetailsForStaff(Integer dealerId) {
        // Lấy chỉ xe có status "On Sale" - cho dealer staff
        List<CarVariant> carVariants = carVariantRepository.findVariantsByDealerIdWithConfiguration(dealerId);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, true))
                .toList();
    }

    private DealerVariantDetailResponse convertToDealerVariantDetailResponse(CarVariant carVariant, Integer dealerId, boolean onSaleOnly) {
        // Lấy thông tin colorId, manufacturerPrice, dealerPrice, imagePath, quantity và status từ xe thuộc dealer hiện tại
        List<Object[]> colorIdsAndData;
        if (onSaleOnly) {
            // Chỉ lấy xe có status "On Sale" cho dealer staff
            colorIdsAndData = carVariantRepository.findColorIdsAndDataByVariantIdAndDealerIdOnSale(
                carVariant.getVariantId(), dealerId);
        } else {
            // Lấy tất cả xe cho dealer manager
            colorIdsAndData = carVariantRepository.findColorIdsAndDataByVariantIdAndDealerId(
                carVariant.getVariantId(), dealerId);
        }

        // Tạo Map từ colorId -> (manufacturerPrice, dealerPrice, imagePath, quantity, status)
        Map<Integer, Object[]> colorDataMap = colorIdsAndData.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0],  // colorId
                    row -> new Object[]{row[1], row[2], row[3], row[4], row[5]}, // [manufacturerPrice, dealerPrice, imagePath, quantity, status]
                    (existing, replacement) -> existing // Nếu có duplicate, giữ giá trị đầu tiên
                ));

        // Lấy thông tin màu sắc từ ColorRepository
        List<Integer> colorIds = colorIdsAndData.stream()
                .map(row -> (Integer) row[0])
                .distinct()
                .toList();

        List<Color> colors = colorRepository.findByColorIds(colorIds);

        // Tạo danh sách DealerColorPrice
        List<DealerVariantDetailResponse.DealerColorPrice> colorPrices = colors.stream()
                .filter(color -> colorDataMap.containsKey(color.getColorId()))
                .map(color -> {
                    Object[] data = colorDataMap.get(color.getColorId());
                    String imageName = (String) data[2];
                    // Xây dựng URL để truy cập ảnh qua HTTP
                    String imageUrl = null;
                    if (imageName != null && !imageName.isEmpty()) {
                        imageUrl = "/api/images/" + imageName;
                    }

                    // Convert Long manufacturer price to BigDecimal
                    BigDecimal manufacturerPrice = null;
                    if (data[0] != null) {
                        if (data[0] instanceof Long) {
                            manufacturerPrice = BigDecimal.valueOf((Long) data[0]);
                        } else if (data[0] instanceof BigDecimal) {
                            manufacturerPrice = (BigDecimal) data[0];
                        }
                    }

                    BigDecimal dealerPrice = (BigDecimal) data[1];
                    Integer quantity = (Integer) data[3];
                    String status = (String) data[4];

                    // Phân biệt giữa Dealer Manager và Dealer Staff
                    if (onSaleOnly) {
                        // Dealer Staff - chỉ xem giá dealer, không xem giá niêm yết
                        return DealerVariantDetailResponse.DealerColorPrice.forDealerStaff(
                            color.getColorName(), dealerPrice, imageUrl, quantity, status);
                    } else {
                        // Dealer Manager - xem cả giá niêm yết và giá dealer
                        return DealerVariantDetailResponse.DealerColorPrice.forDealerManager(
                            color.getColorName(), manufacturerPrice, dealerPrice, imageUrl, quantity, status);
                    }
                })
                .sorted(Comparator.comparing(DealerVariantDetailResponse.DealerColorPrice::getColorName))
                .toList();

        return DealerVariantDetailResponse.builder()
                .variantId(carVariant.getVariantId())
                .modelName(carVariant.getCarModel().getModelName())
                .variantName(carVariant.getVariantName())
                .colorPrices(colorPrices)
                .build();
    }

    // Missing method implementations for dealer-specific searches
    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsForManager(Integer dealerId, String searchTerm) {
        String processedSearchTerm = processFlexibleSearchTerm(searchTerm);
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndTerm(dealerId, processedSearchTerm);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, false))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsForStaff(Integer dealerId, String searchTerm) {
        String processedSearchTerm = processFlexibleSearchTerm(searchTerm);
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndTerm(dealerId, processedSearchTerm);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, true))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsByVariantNameForManager(Integer dealerId, String variantName) {
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndVariantName(dealerId, variantName);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, false))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsByVariantNameForStaff(Integer dealerId, String variantName) {
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndVariantName(dealerId, variantName);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, true))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsByModelNameForManager(Integer dealerId, String modelName) {
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndModelName(dealerId, modelName);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, false))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsByModelNameForStaff(Integer dealerId, String modelName) {
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndModelName(dealerId, modelName);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, true))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsByModelAndVariantNameForManager(Integer dealerId, String modelName, String variantName) {
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndModelAndVariantName(dealerId, modelName, variantName);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, false))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsByModelAndVariantNameForStaff(Integer dealerId, String modelName, String variantName) {
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndModelAndVariantName(dealerId, modelName, variantName);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponse(cv, dealerId, true))
                .toList();
    }

    @Override
    public List<DealerVariantDetailResponse> searchDealerVariantsByStatusForManager(Integer dealerId, String status) {
        List<CarVariant> carVariants = carVariantRepository.searchVariantsByDealerIdAndStatus(dealerId, status);

        return carVariants.stream()
                .map(cv -> convertToDealerVariantDetailResponseByStatus(cv, dealerId, status))
                .toList();
    }

    // New method specifically for converting variants when searching by status
    private DealerVariantDetailResponse convertToDealerVariantDetailResponseByStatus(CarVariant carVariant, Integer dealerId, String status) {
        // Lấy thông tin màu chỉ với status cụ thể được yêu cầu
        List<Object[]> colorIdsAndData = carVariantRepository.findColorIdsAndDataByVariantIdAndDealerIdAndStatus(
                carVariant.getVariantId(), dealerId, status);

        // Tạo Map từ colorId -> (manufacturerPrice, dealerPrice, imagePath, quantity, status)
        Map<Integer, Object[]> colorDataMap = colorIdsAndData.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0],  // colorId
                    row -> new Object[]{row[1], row[2], row[3], row[4], row[5]}, // [manufacturerPrice, dealerPrice, imagePath, quantity, status]
                    (existing, replacement) -> existing // Nếu có duplicate, giữ giá trị đầu tiên
                ));

        // Lấy thông tin màu sắc từ ColorRepository
        List<Integer> colorIds = colorIdsAndData.stream()
                .map(row -> (Integer) row[0])
                .distinct()
                .toList();

        List<Color> colors = colorRepository.findByColorIds(colorIds);

        // Tạo danh sách DealerColorPrice chỉ với những màu có status được yêu cầu
        List<DealerVariantDetailResponse.DealerColorPrice> colorPrices = colors.stream()
                .filter(color -> colorDataMap.containsKey(color.getColorId()))
                .map(color -> {
                    Object[] data = colorDataMap.get(color.getColorId());
                    String imageName = (String) data[2];
                    // Xây dựng URL để truy cập ảnh qua HTTP
                    String imageUrl = null;
                    if (imageName != null && !imageName.isEmpty()) {
                        imageUrl = "/api/images/" + imageName;
                    }

                    // Convert Long manufacturer price to BigDecimal
                    BigDecimal manufacturerPrice = null;
                    if (data[0] != null) {
                        if (data[0] instanceof Long) {
                            manufacturerPrice = BigDecimal.valueOf((Long) data[0]);
                        } else if (data[0] instanceof BigDecimal) {
                            manufacturerPrice = (BigDecimal) data[0];
                        }
                    }

                    // Convert dealer price to BigDecimal with proper type checking
                    BigDecimal dealerPrice = null;
                    if (data[1] != null) {
                        if (data[1] instanceof Long) {
                            dealerPrice = BigDecimal.valueOf((Long) data[1]);
                        } else if (data[1] instanceof BigDecimal) {
                            dealerPrice = (BigDecimal) data[1];
                        }
                    }

                    Integer quantity = (Integer) data[3];
                    String colorStatus = (String) data[4];

                    // For search by status, always return dealer manager format (both prices)
                    return DealerVariantDetailResponse.DealerColorPrice.forDealerManager(
                        color.getColorName(), manufacturerPrice, dealerPrice, imageUrl, quantity, colorStatus);
                })
                .sorted(Comparator.comparing(DealerVariantDetailResponse.DealerColorPrice::getColorName))
                .toList();

        return DealerVariantDetailResponse.builder()
                .variantId(carVariant.getVariantId())
                .modelName(carVariant.getCarModel().getModelName())
                .variantName(carVariant.getVariantName())
                .colorPrices(colorPrices)
                .build();
    }

    @Override
    public boolean updateDealerCarPriceAndStatus(Integer dealerId, String modelName, String variantName,
                                               String colorName, java.math.BigDecimal dealerPrice, String status) {
        return carVariantRepository.updateDealerCarPriceAndStatus(dealerId, modelName, variantName,
                                                                 colorName, dealerPrice, status);
    }
}
