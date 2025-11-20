
package ejb;

import entity.Categories;
import entity.Manufacturers;
import entity.Medicines;
import entity.Users;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Stateless
//@DeclareRoles({"Admin", "Customer", "Delivery"})
@RolesAllowed("Admin")   // ⭐ ALL methods require ADMIN
public class AdminEJB implements AdminEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

    private final String IMAGE_UPLOAD_DIR = "/path/to/upload/medicines/"; // set your path


    // --------- Category CRUD ---------
    @Override
    public void addCategory(String name, String description) {
        Categories category = new Categories();
        category.setName(name);
        category.setDescription(description);
        em.persist(category);
    }

    @Override
    public void updateCategory(Integer categoryId, String name, String description) {
        Categories category = em.find(Categories.class, categoryId);
        if (category != null) {
            category.setName(name);
            category.setDescription(description);
            em.merge(category);
        }
    }

   @Override
public void deleteCategory(Integer categoryId) {
    Categories category = em.find(Categories.class, categoryId);
    if (category == null) {
        return;
    }

    // Check if medicines exist
    if (category.getMedicinesCollection() != null && !category.getMedicinesCollection().isEmpty()) {
        throw new RuntimeException("Cannot delete category: medicines are still assigned!");
    }

    em.remove(category);
}

    @Override
    public List<Categories> getAllCategories() {
        return em.createQuery("SELECT c FROM Categories c", Categories.class).getResultList();
    }

    // --------- Manufacturer CRUD ---------
    @Override
    public void addManufacturer(String name, String contactInfo) {
        Manufacturers manufacturer = new Manufacturers();
        manufacturer.setName(name);
        manufacturer.setContactInfo(contactInfo);
        em.persist(manufacturer);
    }

    @Override
    public void updateManufacturer(Integer manufacturerId, String name, String contactInfo) {
        Manufacturers manufacturer = em.find(Manufacturers.class, manufacturerId);
        if (manufacturer != null) {
            manufacturer.setName(name);
            manufacturer.setContactInfo(contactInfo);
            em.merge(manufacturer);
        }
    }

    @Override
    public void deleteManufacturer(Integer manufacturerId) {
        Manufacturers manufacturer = em.find(Manufacturers.class, manufacturerId);
        if (manufacturer == null) return;
        Collection<Medicines> medicines = manufacturer.getMedicinesCollection();
        if (medicines != null) {
            for (Medicines m : medicines) {
                m.setManufacturerId(null); // Remove the foreign key
                em.merge(m);
            }
        }

        em.remove(manufacturer);
    }

    @Override
    public List<Manufacturers> getAllManufacturers() {
        return em.createNamedQuery("Manufacturers.findAll", Manufacturers.class)
                 .getResultList();
    }
    
    @Override
public void addMedicine(String name, String brand, BigDecimal price, int stock, 
                        LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                        Integer packOf, String description, String picture) {

    
    Categories category = em.find(Categories.class, categoryId);
    Manufacturers manufacturer = em.find(Manufacturers.class, manufacturerId);

    Medicines m = new Medicines();
    m.setName(name);
    m.setBrand(brand);
    m.setPrice(price);          
    m.setStock(stock);
    m.setExpiryDate(expiryDate); 
    m.setPackOf(packOf);       
    m.setDescription(description);
    m.setPicture(picture);

    m.setCategoryId(category);
    m.setManufacturerId(manufacturer);

    Collection<Medicines> medicines = category.getMedicinesCollection();
    medicines.add(m);
    category.setMedicinesCollection(medicines);
    
Collection<Medicines> manufacturerMedicines = manufacturer.getMedicinesCollection();
manufacturerMedicines.add(m);
manufacturer.setMedicinesCollection(manufacturerMedicines);


    em.persist(m);
    em.merge(manufacturer);
    em.merge(category);
}

    @Override
public void updateMedicine(Integer medicineId, String name, String brand, BigDecimal price, int stock,
                           LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                           Integer packOf, String description, String picture) {

    Medicines m = em.find(Medicines.class, medicineId);
    if (m == null) {
        throw new RuntimeException("Medicine not found with ID: " + medicineId);
    }

    m.setName(name);
    m.setBrand(brand);
    m.setPrice(price);
    m.setStock(stock);
    m.setExpiryDate(expiryDate);
    m.setPackOf(packOf);
    m.setDescription(description);
    
    if (picture != null && !picture.isEmpty()) {
        m.setPicture(picture);
    }

    if (categoryId != null) {
        Categories c = em.find(Categories.class, categoryId);
        m.setCategoryId(c);
    } else {
        m.setCategoryId(null);
    }

    if (manufacturerId != null) {
        Manufacturers manu = em.find(Manufacturers.class, manufacturerId);
        m.setManufacturerId(manu);
    } else {
        m.setManufacturerId(null);
    }

    em.merge(m);
}
    
    @Override
    public void deleteMedicine(Integer medicineId) {
        Medicines m = em.find(Medicines.class, medicineId);
        
        em.remove(m);
    }
  @Override
public Collection<Medicines> getAllMedicines() {
    return em.createNamedQuery("Medicines.findAll").getResultList();
}

@Override
public Medicines getMedicineById(Integer medicineId) {
    Medicines medicine = em.find(Medicines.class, medicineId);
    return medicine;
}

@Override
public Collection<Medicines> getMedicineByName(String name) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByName")
                                        .setParameter("name", name)
                                        .getResultList();
    return medicines;
}
@Override
public Collection<Medicines> getMedicinesByCategory(Integer categoryId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByCategory")
                                        .setParameter("categoryId", categoryId)
                                        .getResultList();
    return medicines;
}
@Override
public Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByManufacturer")
                                        .setParameter("manufacturerId", manufacturerId)
                                        .getResultList();
    return medicines;
}

@Override
public void updateMedicineStock(Integer medicineId, int newStock) {
    Medicines medicine = em.find(Medicines.class, medicineId);
        medicine.setStock(newStock);
        em.merge(medicine);
}

@Override
public Collection<Medicines> getLowStockMedicines(int threshold) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findLowStock", Medicines.class)
                                        .setParameter("threshold", threshold)
                                        .getResultList();
    return medicines;
}@Override
public boolean login(String email, String password) {
    try {
        Users user = em.createQuery(
            "SELECT u FROM Users u WHERE u.email = :e AND u.password = :p",
            Users.class
        )
        .setParameter("e", email)
        .setParameter("p", password)
        .getSingleResult();

        System.out.println("LOGIN SUCCESS: " + user.getEmail());
        System.out.println("ROLE = " + user.getRoleId().getRoleName());

        return true;
    } catch (Exception ex) {
        System.out.println("LOGIN FAILED: " + ex.getMessage());
        return false;
    }
}

    }
