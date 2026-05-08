package com.timetracker.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private final StringProperty user = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final BooleanProperty superUser = new SimpleBooleanProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty mobile = new SimpleStringProperty();

    public User() {}

    public User(String user, String password, boolean superUser, String email, String mobile) {
        this.user.set(user);
        this.password.set(password);
        this.superUser.set(superUser);
        this.email.set(email != null ? email : "");
        this.mobile.set(mobile != null ? mobile : "");
    }

    public String getUser() { return user.get(); }
    public void setUser(String v) { user.set(v); }
    public StringProperty userProperty() { return user; }

    public String getPassword() { return password.get(); }
    public void setPassword(String v) { password.set(v); }
    public StringProperty passwordProperty() { return password; }

    public boolean isSuperUser() { return superUser.get(); }
    public void setSuperUser(boolean v) { superUser.set(v); }
    public BooleanProperty superUserProperty() { return superUser; }

    public String getEmail() { return email.get(); }
    public void setEmail(String v) { email.set(v); }
    public StringProperty emailProperty() { return email; }

    public String getMobile() { return mobile.get(); }
    public void setMobile(String v) { mobile.set(v); }
    public StringProperty mobileProperty() { return mobile; }
}
