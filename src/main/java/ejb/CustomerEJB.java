package ejb;

import entity.*;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Stateless

public class CustomerEJB implements CustomerEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

   @Override
public List<Categories> getAllCategories() {
    List<Categories> categories = em.createNamedQuery("Categories.findAll", Categories.class)
                                    .getResultList();
    return categories;
}
@Override
public Collection<Medicines> getAllMedicines() {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findAll", Medicines.class)
                                        .getResultList();
    return medicines;
}

@Override
public List<Manufacturers> getAllManufacturers() {
    List<Manufacturers> manufacturers = em.createNamedQuery("Manufacturers.findAll", Manufacturers.class)
                                          .getResultList();
    return manufacturers;
}

   @Override
public Collection<Medicines> getMedicinesByCategory(Integer categoryId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByCategory", Medicines.class)
                                        .setParameter("categoryId", categoryId)
                                        .getResultList();
    return medicines;
}

    @Override
public Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByManufacturer", Medicines.class)
                                        .setParameter("manufacturerId", manufacturerId)
                                        .getResultList();
    return medicines;
}
@Override
public Collection<Medicines> getMedicineByName(String name) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByName", Medicines.class)
                                        .setParameter("name", name)
                                        .getResultList();
    return medicines;
}
@Override
public Collection<Offers> getActiveOffers() {
    return em.createNamedQuery("Offers.findActive", Offers.class)
             .getResultList();
}
@Override
public Cart getActiveCart(Integer userId) {
    Users user = em.find(Users.class, userId);
    return em.createNamedQuery("Cart.findActiveByUser", Cart.class)
             .setParameter("user", user)
             .getResultStream()
             .findFirst()
             .orElse(null); // Returns null if no active cart
}
//@Override
//public String addOrUpdateCartItem(Integer userId, Integer medicineId, int quantity) {
//    // 1️⃣ Find the user
//    Users user = em.find(Users.class, userId);
////    if (user == null) {
////        return "Error: User not found";
////    }
//
//    // 2️⃣ Find the medicine
//    Medicines med = em.find(Medicines.class, medicineId);
////    if (med == null) {
////        return "Error: Medicine not found";
////    }
//
//    // 3️⃣ Get or create active cart
//    TypedQuery<Cart> q = em.createNamedQuery("Cart.findActiveByUser", Cart.class);
//    q.setParameter("user", user);
//    Cart cart = q.getResultStream().findFirst().orElse(null);
//
//    if (cart == null) {
//        cart = new Cart();
//        cart.setUserId(user);
//        cart.setCreatedAt(new Date());
//        cart.setStatus("ACTIVE");
//        cart.setTotalAmount(0.0);
//        cart.setCartItemsCollection(new ArrayList<>());
//        em.persist(cart);
//    }
//
//    // 4️⃣ Check if item already exists
//    CartItems existingItem = null;
//    for (CartItems item : cart.getCartItemsCollection()) {
//        if (item.getMedicineId().getMedicineId().equals(medicineId)) {
//            existingItem = item;
//            break;
//        }
//    }
//
//    // 5️⃣ Add or update the cart item
//    if (existingItem != null) {
//        existingItem.setQuantity(existingItem.getQuantity() + quantity);
//        em.merge(existingItem);
//    } else {
//        CartItems newItem = new CartItems();
//        newItem.setCartId(cart);
//        newItem.setMedicineId(med);
//        newItem.setQuantity(quantity);
//        newItem.setPricePerUnit(med.getPrice().doubleValue());
//        newItem.setAddedDate(new Date());
//        em.persist(newItem);
//
//        cart.getCartItemsCollection().add(newItem);
//        em.merge(cart);
//    }
//
//    return "Cart updated successfully";
//}
@Override
public String addOrUpdateCartItem(Integer userId, Integer medicineId, int quantity) {
    Users user = em.find(Users.class, userId);
    
    // Get active cart
    TypedQuery<Cart> q = em.createNamedQuery("Cart.findActiveByUser", Cart.class);
    q.setParameter("user", user);
    Cart cart = q.getResultStream().findFirst().orElse(null);
    
    if (cart == null) {
        // Create new cart if none exists
        cart = new Cart();
        cart.setUserId(user);
        cart.setCreatedAt(new Date());
        cart.setStatus("ACTIVE");
        cart.setTotalAmount(0.0);
        cart.setCartItemsCollection(new ArrayList<>()); // initialize collection
        em.persist(cart);
    }
    
    // Find existing cart item
    CartItems existingItem = null;
    for (CartItems item : cart.getCartItemsCollection()) {
        if (item.getMedicineId().getMedicineId().equals(medicineId)) {
            existingItem = item;
            break;
        }
    }
    
    Medicines med = em.find(Medicines.class, medicineId);
    if (med == null) return "Medicine not found!";
    
    if (existingItem != null) {
        existingItem.setQuantity(existingItem.getQuantity() + quantity);
        em.merge(existingItem);
    } else {
        CartItems newItem = new CartItems();
        newItem.setCartId(cart);
        newItem.setMedicineId(med);
        newItem.setQuantity(quantity);
        newItem.setPricePerUnit(med.getPrice().doubleValue());
        newItem.setAddedDate(new Date());
        em.persist(newItem);

        cart.getCartItemsCollection().add(newItem);
    }
    
    // Recalculate total amount
    double total = 0.0;
    for (CartItems item : cart.getCartItemsCollection()) {
        total += item.getPricePerUnit() * item.getQuantity();
    }
    cart.setTotalAmount(total);
    em.merge(cart);

    return "Cart updated successfully. Total: " + total;
}

@Override
public String removeCartItem(Integer userId, Integer cartItemId) {
    // Get the active cart for the given user
    Cart cart = getActiveCart(userId);

    // If no active cart exists, return a message
    if (cart == null) {
        return "No active cart found for user.";
    }

    // Initialize a variable to hold the item to remove
    CartItems itemToRemove = null;

    // Loop through the items in the cart to find the one to remove
    for (CartItems item : cart.getCartItemsCollection()) {
        // Check if the current item's ID matches the cartItemId
        if (item.getCartItemId().equals(cartItemId)) {
            // Found the item, assign it to itemToRemove
            itemToRemove = item;
            // Exit the loop since item is found
            break;
        }
    }

    // If the item was not found in the cart, return a message
    if (itemToRemove == null) {
        return "Cart item not found.";
    }

    // Remove the item from the cart's collection
    cart.getCartItemsCollection().remove(itemToRemove);

    // Remove the item from the database
    em.remove(em.contains(itemToRemove) ? itemToRemove : em.merge(itemToRemove));

    // Recalculate the total amount of the cart after removal
    double total = 0.0;
    for (CartItems item : cart.getCartItemsCollection()) {
        total += item.getPricePerUnit() * item.getQuantity(); // Sum price * quantity
    }

    // Update the cart's total amount
    cart.setTotalAmount(total);

    // Merge the updated cart back into the database
    em.merge(cart);

    // Return a success message with the updated total
    return "Cart item removed successfully. Total: " + total;
}
 
  
    @Override
    public void addAddressToCustomer(String street, String city, String state, String zip, Integer userId) {
        // Find the user
        Users user = em.find(Users.class, userId);

        // Create new address
        Addresses a = new Addresses();
        a.setAddressLine(street);
        a.setCity(city);
        a.setState(state);
        a.setPincode(zip);
        a.setUserId(user);

        // Add to user's address collection
        Collection<Addresses> addresses = user.getAddressesCollection(); // make sure Users has this collection
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        addresses.add(a);
        user.setAddressesCollection(addresses);

        // Persist address and merge user
        em.persist(a);
        em.merge(user);
    }

    @Override
    public void updateAddress(Integer addressId, String street, String city, String state, String zip, Integer userId) {
        Addresses a = em.find(Addresses.class, addressId);
        if (a != null && a.getUserId().getUserId().equals(userId)) {
            a.setAddressLine(street);
            a.setCity(city);
            a.setState(state);
            a.setPincode(zip);
            em.merge(a);
        }
    }

    @Override
    public void deleteAddress(Integer addressId, Integer userId) {
        Addresses a = em.find(Addresses.class, addressId);
        if (a != null && a.getUserId().getUserId().equals(userId)) {
            em.remove(a);
        }
    }

    @Override
    public List<Addresses> getAddressesByUserId(Integer userId) {
        TypedQuery<Addresses> q = em.createNamedQuery("Addresses.findAll", Addresses.class);
        List<Addresses> all = q.getResultList();
        all.removeIf(addr -> !addr.getUserId().getUserId().equals(userId));
        return all;
    }
// ===================== ORDER MANAGEMENT =====================

@Override
public Orders placeOrderFromCart(Integer userId, Integer shippingAddressId, String paymentMethod) {
    Users user = em.find(Users.class, userId);

    // 1️⃣ Get active cart
    TypedQuery<Cart> q = em.createNamedQuery("Cart.findActiveByUser", Cart.class);
    q.setParameter("user", user);
    Cart cart = q.getResultStream().findFirst().orElse(null);

    if (cart == null || cart.getCartItemsCollection().isEmpty()) {
        throw new RuntimeException("Cart is empty or not found for user ID: " + userId);
    }

    // 2️⃣ Create new order
    Orders order = new Orders();
    order.setUserId(user);
    order.setOrderDate(new Date());
    order.setStatus("Pending");
    order.setPaymentMethod(paymentMethod);
    order.setDeliveryAddressId(em.find(Addresses.class, shippingAddressId));

    BigDecimal total = BigDecimal.ZERO;
    List<OrderItems> orderItemsList = new ArrayList<>();

    // 3️⃣ Convert cart items to order items
    for (CartItems cartItem : cart.getCartItemsCollection()) {
        OrderItems orderItem = new OrderItems();
        orderItem.setOrderId(order);
        orderItem.setMedicineId(cartItem.getMedicineId());
        orderItem.setQuantity(cartItem.getQuantity());
        BigDecimal price = BigDecimal.valueOf(cartItem.getPricePerUnit() * cartItem.getQuantity());
        orderItem.setPrice(price);
        total = total.add(price);
        em.persist(orderItem);
        orderItemsList.add(orderItem);
    }

    order.setTotalAmount(total);
    order.setOrderItemsCollection(orderItemsList);
    em.persist(order);

    // 4️⃣ Mark cart as completed
    cart.setStatus("Completed");
    em.merge(cart);

    return order;
}

@Override
public List<Orders> getOrderHistory(Integer userId) {
    TypedQuery<Orders> q = em.createQuery(
        "SELECT o FROM Orders o WHERE o.userId.userId = :userId ORDER BY o.orderDate DESC", Orders.class);
    q.setParameter("userId", userId);
    return q.getResultList();
}

@Override
public Orders getOrderDetails(Integer orderId, Integer userId) {
    TypedQuery<Orders> q = em.createQuery(
        "SELECT o FROM Orders o WHERE o.orderId = :orderId AND o.userId.userId = :userId", Orders.class);
    q.setParameter("orderId", orderId);
    q.setParameter("userId", userId);
    return q.getResultStream().findFirst().orElse(null);
}

}
