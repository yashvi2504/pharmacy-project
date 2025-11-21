package beans;

import ejb.AdminEJBLocal;
import entity.Categories;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("categoriesBean")
@ViewScoped
public class CategoriesBean implements Serializable {

    @Inject
    private AdminEJBLocal adminEJB;

    private List<Categories> categoriesList;

    private Categories selectedCategory = new Categories(); // For add/edit dialog

    private boolean editMode = false; // false = add, true = update

    @PostConstruct
    public void init() {
        categoriesList = adminEJB.getAllCategories();
    }

    public void openNew() {
        selectedCategory = new Categories();
        editMode = false;
    }

    public void openEdit(Categories c) {
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

            init(); // Reload table
        } catch (Exception e) {
            showMessage("Error: " + e.getMessage());
        }
    }

    public void deleteCategory(Categories c) {
        try {
            adminEJB.deleteCategory(c.getCategoryId());
            showMessage("Category deleted successfully!");
            init(); // Reload
        } catch (Exception e) {
            showMessage("Cannot delete category: " + e.getMessage());
        }
    }

    private void showMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    // Getters
    public List<Categories> getCategoriesList() {
        return categoriesList;
    }

    public Categories getSelectedCategory() {
        return selectedCategory;
    }

    public boolean isEditMode() {
        return editMode;
    }
}
