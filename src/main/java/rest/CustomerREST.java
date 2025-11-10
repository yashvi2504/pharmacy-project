package rest;

import ejb.CustomerEJBLocal;
import entity.Addresses;
import entity.Cart;
import entity.DeliveryTracking;
import entity.OrderItems;
import entity.Orders;
import entity.Users;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("customer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerREST {

    @EJB
    private CustomerEJBLocal customerEJB;

    public static class AddToCartRequest {
        public Integer userId;   
        public int medicineId;
        public int quantity;
    }

    @POST
    @Path("cart/add")
    public Response addOrUpdateCartItem(AddToCartRequest request) {
        try {
           
            if (request.userId == null || request.userId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Please register or login before adding to cart.\"}")
                        .build();
            }

            System.out.println(" Received: " + request.userId + ", " + request.medicineId + ", " + request.quantity);

            String result = customerEJB.addOrUpdateCartItem(request.userId, request.medicineId, request.quantity);

            return Response.ok(result).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    @DELETE
    @Path("cart/remove/{userId}/{cartItemId}")
    public Response removeCartItem(@PathParam("userId") int userId, @PathParam("cartItemId") int cartItemId) {
        String result = customerEJB.removeCartItem(userId, cartItemId);
        return Response.ok(result).build();
    }
    
    
@GET
@Path("categories")
public Response getAllCategories() {
    try {
        return Response.ok(customerEJB.getAllCategories()).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
    }
}

@GET

@Path("medicines")
public Response getAllMedicines() {
    try {
        return Response.ok(customerEJB.getAllMedicines()).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
    }
}


@GET
@Path("medicines/{medicineId}")
public Response getMedicineById(@PathParam("medicineId") int medicineId) {
    try {
        var medicine = customerEJB.getMedicineById(medicineId);
        if (medicine == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Medicine not found.\"}")
                    .build();
        }
        return Response.ok(medicine).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
    }
}

@GET
@Path("medicines/category/{categoryId}")
public Response getMedicinesByCategory(@PathParam("categoryId") int categoryId) {
    try {
        var medicines = customerEJB.getMedicinesByCategory(categoryId);
        if (medicines == null || medicines.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"No medicines found for this category.\"}")
                    .build();
        }
        return Response.ok(medicines).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
    }
}
@GET
@Path("medicines/search")
public Response searchMedicines(@QueryParam("q") String query) {
    try {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Please provide a search keyword (q).\"}")
                    .build();
        }
        return Response.ok(customerEJB.searchMedicines(query)).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
    }
}

@GET
@Path("offers/active")
public Response getActiveOffers() {
    try {
        return Response.ok(customerEJB.getActiveOffers()).build();
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
    }
}


    @GET
    @Path("/cart/{userId}")
    public Cart getActiveCart(@PathParam("userId") int userId) {
        return customerEJB.getActiveCart(userId);
    }

@POST
@Path("/address/add")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response addAddress(AddAddressRequest request) {
    try {
        Addresses address = new Addresses();
        address.setAddressLine(request.addressLine);
        address.setCity(request.city);
        address.setState(request.state);
        address.setPincode(request.pincode);
//        address.setCountry(request.country);

        Users user = new Users();
        user.setUserId(request.userId);
        address.setUserId(user);

        String result = customerEJB.addAddress(address);
        return Response.ok("{\"message\":\"" + result + "\"}").build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{\"error\":\"" + e.getMessage() + "\"}")
                       .build();
    }
}

public static class AddAddressRequest {
    public String addressLine;
    public String city;
    public String state;
    public String pincode;
//    public String country;
    public int userId;
}


    @PUT
    @Path("/address/update")
    public String updateAddress(Addresses address) {
        return customerEJB.updateAddress(address);
    }

    @DELETE
    @Path("/address/delete/{addressId}/{userId}")
    public String deleteAddress(@PathParam("addressId") int addressId, @PathParam("userId") int userId) {
        return customerEJB.deleteAddress(addressId, userId);
    }

    @GET
    @Path("/address/list/{userId}")
    public List<Addresses> getAddressesByUserId(@PathParam("userId") int userId) {
        return customerEJB.getAddressesByUserId(userId);
    }

@POST
@Path("/order/place/{userId}/{addressId}/{paymentMethod}")
@Produces(MediaType.APPLICATION_JSON)
public Response placeOrderFromCart(
        @PathParam("userId") int userId,
        @PathParam("addressId") int addressId,
        @PathParam("paymentMethod") String paymentMethod) {

    try {
        Orders order = customerEJB.placeOrderFromCart(userId, addressId, paymentMethod);
        
        OrderResponse response = new OrderResponse();
        response.orderId = order.getOrderId();
        response.status = order.getStatus();
        response.paymentMethod = order.getPaymentMethod();
//        response.totalAmount = order.getTotalAmount().doubleValue();
response.totalAmount = order.getTotalAmount();

       response.orderDate = order.getOrderDate();

        for (OrderItems oi : order.getOrderItemsCollection()) {
            ItemDetails item = new ItemDetails();
            item.medicineId = oi.getMedicineId().getMedicineId();
            item.name = oi.getMedicineId().getName();
            item.quantity = oi.getQuantity();
            item.price = oi.getPrice();
            response.items.add(item);
        }

//        return Response.ok(response).build();
        return Response.ok(order).build();
    } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Error placing order: " + e.getMessage()).build();
    }
}



    @GET
    @Path("/orders/history/{userId}")
    public List<Orders> getOrderHistory(@PathParam("userId") int userId) {
        return customerEJB.getOrderHistory(userId);
    }

    @GET
    @Path("/orders/details/{orderId}/{userId}")
    public Orders getOrderDetails(@PathParam("orderId") int orderId, @PathParam("userId") int userId) {
        return customerEJB.getOrderDetails(orderId, userId);
    }

    @GET
    @Path("/orders/tracking/{orderId}/{userId}")
    public DeliveryTracking getOrderTracking(@PathParam("orderId") int orderId, @PathParam("userId") int userId) {
        return customerEJB.getOrderTracking(orderId, userId);
    }
    
public static class OrderRequest {
    public int userId;
    public int addressId;
    public String paymentMethod;

    public OrderRequest() {} 

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
    public class OrderResponse {
    public int orderId;
    public String status;
    public String paymentMethod;
    public BigDecimal totalAmount;
    public Date orderDate;
    public List<ItemDetails> items = new ArrayList<>();
}

public class ItemDetails {
    public int medicineId;
    public String name;
    public int quantity;
    public BigDecimal price;
}

}
