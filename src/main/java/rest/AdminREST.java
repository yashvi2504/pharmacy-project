package rest;

import ejb.AdminEJBLocal;
import entity.Categories;
import entity.Manufacturers;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/admin")
public class AdminREST {

    @EJB
    private AdminEJBLocal adminEJB;

    // --------- Category Endpoints ---------
    @POST
    @Path("category/add/{name}/{description}")
    public void addCategory(@PathParam("name") String name,
                            @PathParam("description") String description) {
        adminEJB.addCategory(name, description);
    }

    @PUT
    @Path("category/update/{id}/{name}/{description}")
    public void updateCategory(@PathParam("id") Integer id,
                               @PathParam("name") String name,
                               @PathParam("description") String description) {
        adminEJB.updateCategory(id, name, description);
    }

   @DELETE
@Path("deletecategory/{id}")
public Response deleteCategory(@PathParam("id") Integer categoryId) {
    try {
        adminEJB.deleteCategory(categoryId);
        return Response.ok().build(); // 200 OK
    } catch (RuntimeException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{\"error\":\"" + e.getMessage() + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}

    @GET
    @Path("category/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Categories> getAllCategories() {
        return adminEJB.getAllCategories();
    }

    // --------- Manufacturer Endpoints ---------
    @POST
    @Path("manufacturer/add/{name}/{contactInfo}")
    public void addManufacturer(@PathParam("name") String name,
                                @PathParam("contactInfo") String contactInfo) {
        adminEJB.addManufacturer(name, contactInfo);
    }

    @PUT
    @Path("manufacturer/update/{id}/{name}/{contactInfo}")
    public void updateManufacturer(@PathParam("id") Integer id,
                                   @PathParam("name") String name,
                                   @PathParam("contactInfo") String contactInfo) {
        adminEJB.updateManufacturer(id, name, contactInfo);
    }

    @DELETE
    @Path("manufacturer/delete/{id}")
    public void deleteManufacturer(@PathParam("id") Integer id) {
        adminEJB.deleteManufacturer(id);
    }

    @GET
    @Path("manufacturer/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Manufacturers> getAllManufacturers() {
        return adminEJB.getAllManufacturers();
    }
}
