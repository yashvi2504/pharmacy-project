package ejb;

import entity.Categories;
import entity.Manufacturers;
import entity.Medicines;
import jakarta.ejb.Local;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Local
public interface AdminEJBLocal {

    void addCategory(String name, String description);
    void updateCategory(Integer categoryId, String name, String description);
    void deleteCategory(Integer categoryId);
    List<Categories> getAllCategories();

    void addManufacturer(String name, String contactInfo);
    void updateManufacturer(Integer manufacturerId, String name, String contactInfo);
    void deleteManufacturer(Integer manufacturerId);
    List<Manufacturers> getAllManufacturers();
    
    
    
 void addMedicine(String name, String brand, BigDecimal price, int stock,
                     LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                     Integer packOf, String description, String picture);

void updateMedicine(Integer medicineId, String name, String brand, BigDecimal price, int stock,
                        LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                        Integer packOf, String description, String picture);

void deleteMedicine(Integer medicineId);

Medicines getMedicineById(Integer medicineId);
Collection<Medicines> getAllMedicines();

  Collection<Medicines> getMedicineByName(String name);
Collection<Medicines> getMedicinesByCategory(Integer categoryId);
Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId);
void updateMedicineStock(Integer medicineId, int newStock);
Collection<Medicines> getLowStockMedicines(int threshold);//all medicines whose stock < n


}
