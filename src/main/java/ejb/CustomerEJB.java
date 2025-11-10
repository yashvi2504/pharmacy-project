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
import java.util.Date;
import java.util.List;

@Stateless
@PermitAll
public class CustomerEJB implements CustomerEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

    private Cart findOrCreateActiveCart(int userId) {
        Users user = em.find(Users.class, userId);

        TypedQuery<Cart> query = em.createQuery(
            "SELECT c FROM Cart c WHERE c.userId.userId = :userId AND c.status = :status", Cart.class);
        query.setParameter("userId", userId);
        query.setParameter("status", "Active");

        List<Cart> carts = query.getResultList();

        if (!carts.isEmpty()) {
            return carts.get(0);
        } else {
            Cart cart = new Cart();
            cart.setUserId(user);
            cart.setCreatedAt(new Date());
            cart.setStatus("Active");
            cart.setTotalAmount(0.0);
            em.persist(cart);
            return cart;
        }
    }

    @Override
    public List<Categories> getAllCategories() {
        return em.createNamedQuery("Categories.findAll", Categories.class).getResultList();
    }

    @Override
    public List<Medicines> getAllMedicines() {
        return em.createNamedQuery("Medicines.findAll", Medicines.class).getResultList();
    }

    @Override
    public Medicines getMedicineById(int medicineId) {
        return em.find(Medicines.class, medicineId);
    }

    @Override
    public List<Medicines> getMedicinesByCategory(int categoryId) {
        return em.createQuery(
            "SELECT m FROM Medicines m WHERE m.categoryId.categoryId = :catId", Medicines.class)
            .setParameter("catId", categoryId)
            .getResultList();
    }

    @Override
    public List<Medicines> searchMedicines(String query) {
        return em.createQuery(
            "SELECT m FROM Medicines m WHERE m.name LIKE :query OR m.description LIKE :query",
            Medicines.class)
            .setParameter("query", "%" + query + "%")
            .getResultList();
    }

    @Override
    public List<Offers> getActiveOffers() {
        return em.createQuery(
            "SELECT o FROM Offers o WHERE o.active = true AND o.endDate > :now", Offers.class)
            .setParameter("now", new Date())
            .getResultList();
    }

    @Override
    public String addOrUpdateCartItem(int userId, int medicineId, int quantity) {
        if (quantity <= 0) {
            return "Quantity must be greater than zero.";
        }

        Cart cart = findOrCreateActiveCart(userId);
        Medicines medicine = em.find(Medicines.class, medicineId);

        if (medicine == null) return "Medicine not found.";
        if (medicine.getStock() < quantity) return "Not enough stock available.";

        TypedQuery<CartItems> query = em.createQuery(
            "SELECT ci FROM CartItems ci WHERE ci.cartId.cartId = :cartId AND ci.medicineId.medicineId = :medId",
            CartItems.class);
        query.setParameter("cartId", cart.getCartId());
        query.setParameter("medId", medicineId);

        try {
            CartItems existingItem = query.getSingleResult();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            em.merge(existingItem);
        } catch (NoResultException e) {
            // ✅ FIXED: Added missing fields for Bean Validation
            
            CartItems newItem = new CartItems();
            newItem.setCartId(cart);
            newItem.setMedicineId(medicine);
            newItem.setQuantity(quantity);
            newItem.setAddedDate(new Date()); // Required
            newItem.setPricePerUnit(
                medicine.getPrice() != null ? medicine.getPrice().doubleValue() : 0.0
            ); 
            em.persist(newItem);
        }

        return "Item added to cart.";
    }

  @Override
public String removeCartItem(int userId, int cartItemId) {
    try {
        CartItems item = em.find(CartItems.class, cartItemId);
        if (item == null) {
            return "Cart item not found.";
        }

        Cart cart = item.getCartId();
        if (cart == null || cart.getUserId() == null || !cart.getUserId().getUserId().equals(userId)) {
            return "This cart item does not belong to the specified user.";
        }

        em.remove(item);
        return "Cart item removed successfully!";
    } catch (Exception e) {
        e.printStackTrace();
        return "Error removing cart item: " + e.getMessage();
    }
}


    @Override
    public Cart getActiveCart(int userId) {
        return findOrCreateActiveCart(userId);
    }

   @Override
public String addAddress(Addresses address) {
    System.out.println("🧠 Inside addAddress()");
    if (address.getUserId() == null) {
        System.out.println("❌ address.getUserId() is null");
        return "Address must be linked to a user.";
    }

    if (address.getUserId().getUserId() == null) {
        System.out.println("❌ address.getUserId().getUserId() is null");
        return "Address must be linked to a user.";
    }

    Users user = em.find(Users.class, address.getUserId().getUserId());
    if (user == null) {
        System.out.println("❌ No user found in DB with ID: " + address.getUserId().getUserId());
        return "User not found.";
    }

    address.setUserId(user);
    em.persist(address);
    System.out.println("✅ Address saved successfully!");
    return "Address added successfully.";
}


    @Override
    public String updateAddress(Addresses address) {
        if (address.getAddressId() == null) {
            return "Address ID is required for update.";
        }
        em.merge(address);
        return "Address updated successfully.";
    }

    @Override
    public String deleteAddress(int addressId, int userId) {
        Addresses address = em.find(Addresses.class, addressId);
        if (address == null) return "Address not found.";
        if (address.getUserId().getUserId() != userId) {
            return "Authorization error: Cannot delete another user's address.";
        }
        em.remove(address);
        return "Address deleted.";
    }

    @Override
    public List<Addresses> getAddressesByUserId(int userId) {
        return em.createQuery(
            "SELECT a FROM Addresses a WHERE a.userId.userId = :userId", Addresses.class)
            .setParameter("userId", userId)
            .getResultList();
    }
    
public Orders placeOrderFromCart(int userId, int addressId, String paymentMethod) {
    try {
        // 1️⃣ Fetch the user's active cart
        Cart cart = em.createQuery(
                "SELECT c FROM Cart c WHERE c.userId.userId = :userId AND c.status = 'Active'", Cart.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getSingleResult();

        // 2️⃣ Fetch all items from the cart
        List<CartItems> cartItems = em.createQuery(
                "SELECT ci FROM CartItems ci WHERE ci.cartId = :cart", CartItems.class)
                .setParameter("cart", cart)
                .getResultList();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        // 3️⃣ Create a new order
        Orders order = new Orders();
        order.setOrderDate(new Date());
        order.setStatus("Pending");
        order.setPaymentMethod(paymentMethod);
        order.setUserId(cart.getUserId());
        order.setAddressId(em.find(Addresses.class, addressId));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItems> orderItemsList = new ArrayList<>();

        // 4️⃣ Convert each cart item into an order item
        for (CartItems cartItem : cartItems) {
            OrderItems orderItem = new OrderItems();
            orderItem.setOrderId(order);
            orderItem.setMedicineId(cartItem.getMedicineId());
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal lineTotal = BigDecimal.valueOf(cartItem.getPricePerUnit())
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setPrice(lineTotal);

            total = total.add(lineTotal);
            em.persist(orderItem);
            orderItemsList.add(orderItem);
        }

        // 5️⃣ Set totals & persist order
        order.setTotalAmount(total);
        order.setOrderItemsCollection(orderItemsList);
        em.persist(order);

        // 6️⃣ Mark cart as completed
        cart.setStatus("Completed");
        em.merge(cart);

        return order;

    } catch (NoResultException e) {
        throw new RuntimeException("No active cart found for this user.");
    } catch (Exception e) {
        throw new RuntimeException("Error placing order: " + e.getMessage(), e);
    }
}

@Override
    public List<Orders> getOrderHistory(int userId) {
        return em.createQuery(
            "SELECT o FROM Orders o WHERE o.userId.userId = :userId ORDER BY o.orderDate DESC", Orders.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Override
    public Orders getOrderDetails(int orderId, int userId) {
        Orders order = em.find(Orders.class, orderId);
        if (order != null && order.getUserId().getUserId() == userId) {
            return order;
        }
        return null;
    }

    @Override
    public DeliveryTracking getOrderTracking(int orderId, int userId) {
        TypedQuery<DeliveryTracking> q = em.createQuery(
            "SELECT dt FROM DeliveryTracking dt WHERE dt.deliveryId.orderId.orderId = :orderId AND dt.deliveryId.orderId.userId.userId = :userId",
            DeliveryTracking.class);
        q.setParameter("orderId", orderId);
        q.setParameter("userId", userId);

        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
