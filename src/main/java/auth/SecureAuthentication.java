package auth;

import beans.LoginBean;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static jwtrest.Constants.AUTHORIZATION_HEADER;
import static jwtrest.Constants.BEARER;
import jwtrest.JWTCredential;
import jwtrest.TokenProvider;
import record.KeepRecord;

@Named
@RequestScoped
public class SecureAuthentication implements HttpAuthenticationMechanism, Serializable {

    @Inject
    IdentityStoreHandler handler;

    @Inject
    TokenProvider tokenProvider;

    @Inject
    LoginBean lbean;

    @Inject
    KeepRecord keepRecord;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext ctx) throws AuthenticationException {
        // handle logout
        try {
            if (request.getRequestURI().contains("Logout")) {
                try { request.logout(); } catch (Exception ex) { /* ignore */ }
                keepRecord.reset();
                response.sendRedirect("Login.jsf");
                return ctx.doNothing();
            }
        } catch (Exception e) { e.printStackTrace(); }

        // if request contains username/password parameters => form login attempt
        String token = extractToken(ctx);
        try {
            if (token == null && request.getParameter("username") != null) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");

                Credential credential = new UsernamePasswordCredential(username, new Password(password));
                CredentialValidationResult result = handler.validate(credential);

                if (result.getStatus() == jakarta.security.enterprise.identitystore.CredentialValidationResult.Status.VALID) {
                    AuthenticationStatus status = createToken(result, ctx);

                    // notify container (so programmatic security works)
                    ctx.notifyContainerAboutLogin(result);

                    keepRecord.setPrincipal(result.getCallerPrincipal());
                    keepRecord.setRoles(result.getCallerGroups());
                    keepRecord.setCredential(credential);

                    if (result.getCallerGroups().contains("Admin")) {
                        request.getRequestDispatcher("admin/Admin.jsf").forward(request, response);
                    } else {
                        request.getRequestDispatcher("index.xhtml").forward(request, response);
                    }
                    return status;
                } else {
                    keepRecord.setErrorStatus("Either Username or Password is wrong !");
                    response.sendRedirect("Login.jsf");
                    return ctx.doNothing();
                }
            }

            if (keepRecord.getToken() != null) {
                // If we already have a token in session, set container identity
                ctx.notifyContainerAboutLogin(keepRecord.getPrincipal(), keepRecord.getRoles());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // if there is a bearer token, validate it:
        if (token != null) {
            return validateToken(token, ctx);
        } else if (ctx.isProtected()) {
            // protected resource requested but no credentials, deny
            return ctx.responseUnauthorized();
        }

        return ctx.doNothing();
    }

    private AuthenticationStatus validateToken(String token, HttpMessageContext context) {
        try {
            if (tokenProvider.validateToken(token)) {
                JWTCredential credential = tokenProvider.getCredential(token);
                return context.notifyContainerAboutLogin(credential.getPrincipal(), credential.getAuthorities());
            }
            return context.responseUnauthorized();
        } catch (ExpiredJwtException eje) {
            return context.responseUnauthorized();
        }
    }

    private AuthenticationStatus createToken(CredentialValidationResult result, HttpMessageContext context) {
        String jwt = tokenProvider.createToken(result.getCallerPrincipal().getName(), result.getCallerGroups(), false);
        keepRecord.setToken(jwt);
        context.getResponse().addHeader(AUTHORIZATION_HEADER, BEARER + jwt);
        return context.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
    }

    private String extractToken(HttpMessageContext context) {
        String authorizationHeader = context.getRequest().getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            String token = authorizationHeader.substring(BEARER.length());
            keepRecord.setToken(token);
            return token;
        }
        return null;
    }
}
