package com.timetracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HoursRecord model")
class HoursRecordTest {

    private static final LocalDateTime CHECK_IN  = LocalDateTime.of(2026, 5, 9, 9, 0, 0);
    private static final LocalDateTime CHECK_OUT = LocalDateTime.of(2026, 5, 9, 17, 30, 0);

    @Test
    @DisplayName("full constructor sets every field")
    void fullConstructor_setsAllFields() {
        HoursRecord r = new HoursRecord(42, "alice", CHECK_IN, CHECK_OUT, 8.50);

        assertEquals(42,        r.getId());
        assertEquals("alice",   r.getUser());
        assertEquals(CHECK_IN,  r.getComeIn());
        assertEquals(CHECK_OUT, r.getComeOut());
        assertEquals(8.50,      r.getHour(), 0.001);
    }

    @Test
    @DisplayName("default constructor produces non-null properties")
    void defaultConstructor_propertiesExist() {
        HoursRecord r = new HoursRecord();
        assertNotNull(r.idProperty());
        assertNotNull(r.userProperty());
        assertNotNull(r.comeInProperty());
        assertNotNull(r.comeOutProperty());
        assertNotNull(r.hourProperty());
    }

    @Test
    @DisplayName("active session has null ComeOut")
    void activeSession_comeOutIsNull() {
        HoursRecord r = new HoursRecord(1, "bob", CHECK_IN, null, 0);
        assertNull(r.getComeOut());
    }

    @Test
    @DisplayName("setters update getters correctly")
    void setters_updateGetters() {
        HoursRecord r = new HoursRecord();
        r.setId(10);
        r.setUser("charlie");
        r.setComeIn(CHECK_IN);
        r.setComeOut(CHECK_OUT);
        r.setHour(8.5);

        assertEquals(10,        r.getId());
        assertEquals("charlie", r.getUser());
        assertEquals(CHECK_IN,  r.getComeIn());
        assertEquals(CHECK_OUT, r.getComeOut());
        assertEquals(8.5,       r.getHour(), 0.001);
    }

    @Test
    @DisplayName("ComeOut can be set to null (active session transition)")
    void comeOut_canBeSetToNull() {
        HoursRecord r = new HoursRecord(1, "dave", CHECK_IN, CHECK_OUT, 8.5);
        r.setComeOut(null);
        assertNull(r.getComeOut());
    }

    @Test
    @DisplayName("JavaFX properties reflect current values")
    void properties_reflectCurrentValue() {
        HoursRecord r = new HoursRecord(5, "eve", CHECK_IN, CHECK_OUT, 3.25);

        assertEquals(r.getId(),      r.idProperty().get());
        assertEquals(r.getUser(),    r.userProperty().get());
        assertEquals(r.getComeIn(),  r.comeInProperty().get());
        assertEquals(r.getComeOut(), r.comeOutProperty().get());
        assertEquals(r.getHour(),    r.hourProperty().get(), 0.001);
    }
}
