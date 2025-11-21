package org.example.service;

public interface CarDeletionService {
    /**
     * Xóa xe theo model name, variant name, color name
     * Nếu xóa hết xe của variant đó thì tự xóa variant và configuration của variant đó
     * Nếu xóa hết các variant thì tự xóa model
     */
    String deleteCars(String modelName, String variantName, String colorName);
}
