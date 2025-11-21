

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.Set;
import record.KeepRecord;


/**
 *
 * @author root
 */
@Named(value = "loginBean")
@RequestScoped
public class LoginBean {
    @Inject KeepRecord keepRecord;
    
    private String errorstatus; 
    
    public String getErrorStatus() {
        return keepRecord.getErrorStatus();
    }

    public void setErrorStatus(String status) {
        
        this.errorstatus = status;
    }

 
    public LoginBean() {
        
       // errorstatus="";
    }
    
   
    
    // ---- NEW: verifyAdminAccess called from pages
    public void verifyAdminAccess() throws IOException {
        Set<String> roles = keepRecord.getRoles();
        if (roles == null || !roles.contains("Admin")) {
            // redirect to AccessDenied.xhtml (relative path from root of Web Pages)
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .redirect("AccessDenied.xhtml");
        }
    }

    // ---- NEW: helper to show/hide links in menu
    public boolean isAdmin() {
        Set<String> roles = keepRecord.getRoles();
        return roles != null && roles.contains("Admin");
    }
  
}
