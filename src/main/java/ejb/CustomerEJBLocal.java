package ejb;

import entity.Addresses;
import entity.Cart;
import entity.Categories;
import entity.Medicines;
import entity.Offers;
import entity.Orders;
import entity.DeliveryTracking; 
import entity.Manufacturers;
import jakarta.ejb.Local;
import java.util.Collection;
import java.util.List;

@Local
public interface CustomerEJBLocal {

    List<Categories> getAllCategories();//done
    Collection<Medicines> getAllMedicines();//done
    List<Manufacturers> getAllManufacturers();//done


    // ===== Medicine retrieval/search =====
//    Medicines getMedicineById(Integer medicineId);
    Collection<Medicines> getMedicinesByCategory(Integer categoryId);
    Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId);

    Collection<Medicines> getMedicineByName(String name);
//    Collection<Medicines> searchMedicines(String name, Integer categoryId, Integer manufacturerId);

    // ===== Offers =====
    Collection<Offers> getActiveOffers();

    // ===== Cart operations =====
    Cart getActiveCart(Integer userId);

    String addOrUpdateCartItem(Integer userId, Integer medicineId, int quantity);

    String removeCartItem(Integer userId, Integer cartItemId);
//    Cart getActiveCart(Integer userId);

    // ===== Address management =====
   
    // Add an address using individual fields
    void addAddressToCustomer(String street, String city, String state, String zip, Integer userId);

    // Update an address by fields
    void updateAddress(Integer addressId, String street, String city, String state, String zip, Integer userId);

    // Delete an address by ID and userId
    void deleteAddress(Integer addressId, Integer userId);

    // Get all addresses for a specific user
    List<Addresses> getAddressesByUserId(Integer userId);
    // ===== Orders =====
    Orders placeOrderFromCart(Integer userId, Integer shippingAddressId, String paymentMethod);
    List<Orders> getOrderHistory(Integer userId);
    Orders getOrderDetails(Integer orderId, Integer userId);

    // ===== Delivery tracking =====
//    DeliveryTracking getOrderTracking(Integer orderId, Integer userId);
}