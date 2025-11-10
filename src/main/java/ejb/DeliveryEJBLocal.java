package ejb;

import entity.Delivery;
import entity.DeliveryPartners;
import entity.DeliveryTracking;
import entity.Invoices;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface DeliveryEJBLocal {
    String registerDeliveryPartner(DeliveryPartners partner);
    List<Delivery> getAssignedDeliveries(int partnerId);
    String updateDeliveryStatus(int deliveryId, String status);
    List<DeliveryTracking> getTrackingHistory(int deliveryId);
    String assignPartnerToOrder(int orderId, int partnerId);
List<Invoices> getInvoicesByOrderId(int orderId);

}
