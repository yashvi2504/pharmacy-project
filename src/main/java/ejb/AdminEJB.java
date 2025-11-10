package ejb;

import entity.Categories;
import entity.Manufacturers;
import entity.Medicines;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;

@Stateless
public class AdminEJB implements AdminEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

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

        // Remove parent reference from medicines
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
}
