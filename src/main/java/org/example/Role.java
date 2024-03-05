package org.example;

public class Role {
    private String role;
    private Boolean canUpdate;
    private Boolean canDelete;
    private Boolean canView;
    private Boolean canCreate;
    private Boolean newRole;

    public Role(String role, Boolean canUpdate, Boolean canDelete, Boolean canView, Boolean canCreate, Boolean newRole) {
        this.role = role;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
        this.canView = canView;
        this.canCreate = canCreate;
        this.newRole = newRole;
    }

    public Boolean getNewRole() {
        return newRole;
    }

    public void setNewRole(Boolean newRole) {
        this.newRole = newRole;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(Boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    public Boolean getCanView() {
        return canView;
    }

    public void setCanView(Boolean canView) {
        this.canView = canView;
    }

    public Boolean getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(Boolean canCreate) {
        this.canCreate = canCreate;
    }
}
