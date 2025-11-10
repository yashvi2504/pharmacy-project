package ejb;

import entity.Delivery;
import entity.DeliveryPartners;
import entity.DeliveryTracking;
import entity.Invoices;
import entity.Orders;
import entity.Users;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
@PermitAll
public class DeliveryEJB implements DeliveryEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

   
    @Override
    public String registerDeliveryPartner(DeliveryPartners partner) {
        if (partner.getUserId() == null) {
            return "❌ User information is missing!";
        }

        Users user = em.find(Users.class, partner.getUserId().getUserId());
        if (user == null) {
            return "❌ User not found!";
        }

        em.persist(partner);
        return "✅ Delivery partner registered successfully!";
    }

  
 public List<Delivery> getAssignedDeliveries(int partnerId) {
    return em.createQuery(
        "SELECT d FROM Delivery d WHERE d.deliveryPartnerId.deliveryPartnerId = :pid", Delivery.class)
        .setParameter("pid", partnerId)
        .getResultList();
}
    @Override
//    @RolesAllowed("Delivery")
public String updateDeliveryStatus(int deliveryId, String status) {
    Delivery delivery = em.find(Delivery.class, deliveryId);
    if (delivery == null) {
        return "❌ Delivery not found!";
    }

    delivery.setStatus(status);
    em.merge(delivery);

    DeliveryTracking track = new DeliveryTracking();
    track.setDeliveryId(delivery);
    track.setStatus(status);
    track.setUpdateTime(new Date());
    em.persist(track);

   
    if ("Delivered".equalsIgnoreCase(status)) {
        Orders order = delivery.getOrderId();
        if (order != null) {
            order.setStatus("Delivered");
            em.merge(order);
        }
    }

    return "✅ Delivery status updated to " + status;
}
  
    @Override
//    @RolesAllowed({"Delivery", "Admin"})
    public List<DeliveryTracking> getTrackingHistory(int deliveryId) {
        return em.createQuery(
                "SELECT t FROM DeliveryTracking t WHERE t.deliveryId.deliveryId = :did ORDER BY t.updateTime DESC",
                DeliveryTracking.class)
                .setParameter("did", deliveryId)
                .getResultList();
    }
    @Override
public String assignPartnerToOrder(int orderId, int partnerId) {
    try {
      
        DeliveryPartners partner = em.find(DeliveryPartners.class, partnerId);
        if (partner == null) return "Partner not found!";

        Orders order = em.find(Orders.class, orderId);
        if (order == null) return " Order not found!";

        Delivery delivery = new Delivery();
        delivery.setOrderId(order);
        delivery.setDeliveryPartnerId(partner);
        delivery.setStatus("Assigned");
//        delivery.setAssignedDate(new Date()); 
        em.persist(delivery);

        return "✅ Partner assigned successfully!";

    } catch (Exception e) {
        e.printStackTrace();
        return "❌ Error assigning partner: " + e.getMessage();
    }
}
public List<Invoices> getInvoicesByOrderId(int orderId) {
    return em.createQuery(
        "SELECT i FROM Invoices i WHERE i.orderId.orderId = :orderId",
        Invoices.class
    )
    .setParameter("orderId", orderId)
    .getResultList();
}

}
