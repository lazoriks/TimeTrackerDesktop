package com.timetracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User model")
class UserTest {

    @Test
    @DisplayName("full constructor sets every field")
    void fullConstructor_setsAllFields() {
        User u = new User("john.doe", "secret", true, "john@example.com", "+1234567890");

        assertEquals("john.doe",       u.getUser());
        assertEquals("secret",         u.getPassword());
        assertTrue(u.isSuperUser());
        assertEquals("john@example.com", u.getEmail());
        assertEquals("+1234567890",    u.getMobile());
    }

    @Test
    @DisplayName("default constructor produces non-null properties")
    void defaultConstructor_propertiesExist() {
        User u = new User();
        assertNotNull(u.userProperty());
        assertNotNull(u.passwordProperty());
        assertNotNull(u.superUserProperty());
        assertNotNull(u.emailProperty());
        assertNotNull(u.mobileProperty());
    }

    @Test
    @DisplayName("SuperUser defaults to false in default constructor")
    void defaultConstructor_superUserIsFalse() {
        assertFalse(new User().isSuperUser());
    }

    @Test
    @DisplayName("setters update the values returned by getters")
    void setters_updateGetters() {
        User u = new User();
        u.setUser("alice");
        u.setPassword("p@ss");
        u.setSuperUser(true);
        u.setEmail("alice@test.com");
        u.setMobile("555-1234");

        assertEquals("alice",          u.getUser());
        assertEquals("p@ss",           u.getPassword());
        assertTrue(u.isSuperUser());
        assertEquals("alice@test.com", u.getEmail());
        assertEquals("555-1234",       u.getMobile());
    }

    @Test
    @DisplayName("null email is stored as empty string")
    void nullEmail_storedAsEmpty() {
        User u = new User("bob", "pw", false, null, null);
        assertEquals("", u.getEmail());
        assertEquals("", u.getMobile());
    }

    @Test
    @DisplayName("regular user has SuperUser = false")
    void regularUser_superUserFalse() {
        User u = new User("regular", "pw", false, "", "");
        assertFalse(u.isSuperUser());
    }

    @Test
    @DisplayName("JavaFX property objects match the value")
    void properties_reflectCurrentValue() {
        User u = new User("test", "123", true, "a@b.com", "999");

        assertEquals(u.getUser(),      u.userProperty().get());
        assertEquals(u.getPassword(),  u.passwordProperty().get());
        assertEquals(u.isSuperUser(),  u.superUserProperty().get());
        assertEquals(u.getEmail(),     u.emailProperty().get());
        assertEquals(u.getMobile(),    u.mobileProperty().get());
    }
}
