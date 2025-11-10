package rest;

import ejb.DeliveryEJBLocal;
import entity.DeliveryPartners;
import entity.Invoices;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("delivery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DeliveryREST {

    @EJB
    private DeliveryEJBLocal deliveryEJB;

    @POST
    @Path("register-partner")
    public Response registerPartner(DeliveryPartners partner) {
        String msg = deliveryEJB.registerDeliveryPartner(partner);
        return Response.ok(msg).build();
    }

  @GET
@Path("assigned/{partnerId}")
public Response getAssignedDeliveries(@PathParam("partnerId") int partnerId) {
    return Response.ok(deliveryEJB.getAssignedDeliveries(partnerId)).build();
}


@PUT
@Path("assign/{orderId}/{partnerId}")
public Response assignPartner(@PathParam("orderId") int orderId,
                              @PathParam("partnerId") int partnerId) {
    String msg = deliveryEJB.assignPartnerToOrder(orderId, partnerId);
    return Response.ok(msg).build();
}
    @PUT
    @Path("update-status/{deliveryId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStatus(@PathParam("deliveryId") int deliveryId, String status) {
        String msg = deliveryEJB.updateDeliveryStatus(deliveryId, status);
        return Response.ok(msg).build();
    }

    @GET
    @Path("tracking/{deliveryId}")
    public Response getTrackingHistory(@PathParam("deliveryId") int deliveryId) {
        return Response.ok(deliveryEJB.getTrackingHistory(deliveryId)).build();
    }
     
    @GET
    @Path("invoice/order/{orderId}")
    public Response getInvoicesByOrderId(@PathParam("orderId") int orderId) {
        List<Invoices> invoices = deliveryEJB.getInvoicesByOrderId(orderId);
        if (invoices == null || invoices.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("No invoices found for order ID: " + orderId)
                           .build();
        }
        return Response.ok(invoices).build();
    }
}
