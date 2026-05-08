package com.timetracker.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;

public class HoursRecord {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty user = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> comeIn = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> comeOut = new SimpleObjectProperty<>();
    private final DoubleProperty hour = new SimpleDoubleProperty();

    public HoursRecord() {}

    public HoursRecord(int id, String user, LocalDateTime comeIn, LocalDateTime comeOut, double hour) {
        this.id.set(id);
        this.user.set(user);
        this.comeIn.set(comeIn);
        this.comeOut.set(comeOut);
        this.hour.set(hour);
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public IntegerProperty idProperty() { return id; }

    public String getUser() { return user.get(); }
    public void setUser(String v) { user.set(v); }
    public StringProperty userProperty() { return user; }

    public LocalDateTime getComeIn() { return comeIn.get(); }
    public void setComeIn(LocalDateTime v) { comeIn.set(v); }
    public ObjectProperty<LocalDateTime> comeInProperty() { return comeIn; }

    public LocalDateTime getComeOut() { return comeOut.get(); }
    public void setComeOut(LocalDateTime v) { comeOut.set(v); }
    public ObjectProperty<LocalDateTime> comeOutProperty() { return comeOut; }

    public double getHour() { return hour.get(); }
    public void setHour(double v) { hour.set(v); }
    public DoubleProperty hourProperty() { return hour; }
}
