package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
@Tag(name = "Image Management", description = "APIs để quản lý và upload ảnh")
public class ImageController {

    @Value("${car.image.base-path}")
    private String imageBasePath;

    @Value("${app.file.max-image-size}")
    private long maxImageSize;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(imageBasePath).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file ảnh", description = "Chọn file ảnh từ máy tính để upload")
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file) {
        return processFileUpload(file);
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<?> deleteImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(imageBasePath).resolve(filename).normalize();

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Xóa ảnh thành công");
                response.put("filename", filename);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Lỗi khi xóa file: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> processFileUpload(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File không được để trống"));
            }

            if (file.getSize() > maxImageSize) {
                return ResponseEntity.badRequest().body(createErrorResponse("Kích thước file không được vượt quá " + (maxImageSize / 1024 / 1024) + "MB"));
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Tên file không hợp lệ"));
            }

            if (!isValidImageFile(originalFilename.toLowerCase())) {
                return ResponseEntity.badRequest().body(createErrorResponse("Chỉ chấp nhận các định dạng ảnh: JPG, JPEG, PNG, GIF, BMP, WEBP"));
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + fileExtension;

            Path uploadPath = Paths.get(imageBasePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload ảnh thành công");
            response.put("filename", newFilename);
            response.put("originalFilename", originalFilename);
            response.put("size", file.getSize());
            response.put("imagePath", "/api/images/" + newFilename);
            response.put("fullPath", filePath.toString());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Lỗi khi lưu file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Lỗi không xác định: " + e.getMessage()));
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "webp" -> "image/webp";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            default -> "application/octet-stream";
        };
    }

    private boolean isValidImageFile(String filename) {
        return filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
               filename.endsWith(".png") || filename.endsWith(".gif") ||
               filename.endsWith(".bmp") || filename.endsWith(".webp");
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}
