package rest;

import jwtrest.TokenProvider;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jwtrest.TokenProvider;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthREST {

    @Inject
    private TokenProvider tokenProvider;

    // Simple login endpoint (replace with DB-based login later)
    @POST
    @Path("login")
    public Response login(UserLogin login) {

        // Hard-coded for now — replace with real DB validation
        if (!login.getUsername().equals("admin") || !login.getPassword().equals("admin123")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid credentials\"}")
                    .build();
        }

        String token = tokenProvider.generateToken("Admin");

        return Response.ok("{\"token\":\"" + token + "\"}").build();
    }

    // Helper class for JSON input
    public static class UserLogin {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
