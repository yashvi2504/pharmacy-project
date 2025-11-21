package beans;

import ejb.AdminEJBLocal;
import entity.Categories;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    @Inject
    private AdminEJBLocal adminEJB;

    // ---------------- Users ----------------
    private List<Users> usersList;

    // ---------------- Categories ----------------
    private List<Categories> categoriesList;
    private Categories selectedCategory = new Categories();
    private boolean editMode = false;

    // ---------------- Init ----------------
    @PostConstruct
    public void init() {
        usersList = adminEJB.getAllUsers();
        categoriesList = adminEJB.getAllCategories();
    }

    // ---------------- Users ----------------
    public List<Users> getUsersList() {
        return usersList;
    }

    // ---------------- Categories ----------------
    public List<Categories> getCategoriesList() {
        return categoriesList;
    }

    public Categories getSelectedCategory() {
        return selectedCategory;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void openNewCategory() {
        selectedCategory = new Categories();
        editMode = false;
    }

    public void openEditCategory(Categories c) {
        selectedCategory = c;
        editMode = true;
    }

    public void saveCategory() {
        try {
            if (editMode) {
                adminEJB.updateCategory(
                        selectedCategory.getCategoryId(),
                        selectedCategory.getName(),
                        selectedCategory.getDescription()
                );
                showMessage("Category updated successfully!");
            } else {
                adminEJB.addCategory(
                        selectedCategory.getName(),
                        selectedCategory.getDescription()
                );
                showMessage("Category added successfully!");
            }

            categoriesList = adminEJB.getAllCategories(); // Reload table
        } catch (Exception e) {
            showMessage("Error: " + e.getMessage());
        }
    }

    public void deleteCategory(Categories c) {
        try {
            adminEJB.deleteCategory(c.getCategoryId());
            showMessage("Category deleted successfully!");
            categoriesList = adminEJB.getAllCategories(); // Reload
        } catch (Exception e) {
            showMessage("Cannot delete category: " + e.getMessage());
        }
    }

    private void showMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }
}
