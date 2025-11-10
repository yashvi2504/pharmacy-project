package ejb;

import entity.Categories;
import entity.Manufacturers;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface AdminEJBLocal {

    // Category CRUD
    void addCategory(String name, String description);
    void updateCategory(Integer categoryId, String name, String description);
    void deleteCategory(Integer categoryId);
    List<Categories> getAllCategories();

    // Manufacturer CRUD
    void addManufacturer(String name, String contactInfo);
    void updateManufacturer(Integer manufacturerId, String name, String contactInfo);
    void deleteManufacturer(Integer manufacturerId);
    List<Manufacturers> getAllManufacturers();
}
