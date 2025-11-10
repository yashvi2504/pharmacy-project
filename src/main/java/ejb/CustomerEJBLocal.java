package ejb;

import entity.Addresses;
import entity.Cart;
import entity.Categories;
import entity.Medicines;
import entity.Offers;
import entity.Orders;
import entity.DeliveryTracking; 
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CustomerEJBLocal {

    List<Categories> getAllCategories();//done

    
    List<Medicines> getAllMedicines();//dome

    Medicines getMedicineById(int medicineId);//done

    List<Medicines> getMedicinesByCategory(int categoryId);//done

    List<Medicines> searchMedicines(String query);//done

    List<Offers> getActiveOffers();//done

    String addOrUpdateCartItem(int userId, int medicineId, int quantity);//done

    String removeCartItem(int userId, int cartItemId); //done

    Cart getActiveCart(int userId);//done

    String addAddress(Addresses address);//done

    String updateAddress(Addresses address);//done

    String deleteAddress(int addressId, int userId);//done 

    List<Addresses> getAddressesByUserId(int userId);//done

    Orders placeOrderFromCart(int userId, int shippingAddressId, String paymentMethod);//dppne

    List<Orders> getOrderHistory(int userId);//donr

    Orders getOrderDetails(int orderId, int userId);//done 
    
    DeliveryTracking getOrderTracking(int orderId, int userId);//done
}