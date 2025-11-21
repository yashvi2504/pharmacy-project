package rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jwtrest.TokenProvider;
import ejb.AdminEJBLocal;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthREST {

    @Inject
    private AdminEJBLocal adminEJB;

    @Inject
    private TokenProvider tokenProvider;

  @POST
@Path("login")
   @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
public Response login(UserLogin login) {

    boolean isValid = adminEJB.login(login.getEmail(), login.getPassword());

    if (!isValid) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\":\"Invalid email or password\"}")
                .build();
    }

    String token = tokenProvider.generateToken("Admin");

    return Response.ok("{\"token\":\"" + token + "\"}").build();
}

 public static class UserLogin {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

}
