package com.duyphuc.olympics.model;

import java.util.Objects;

public class OlympicEvent {
    private int id;
    private String eventName;
    private int year;
    private String eventType; // "Summer" or "Winter"
    private String tableNameInDb;

    // Constructors
    public OlympicEvent() {
    }

    public OlympicEvent(int id, String eventName, int year, String eventType, String tableNameInDb) {
        this.id = id;
        this.eventName = eventName;
        this.year = year;
        this.eventType = eventType;
        this.tableNameInDb = tableNameInDb;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTableNameInDb() {
        return tableNameInDb;
    }

    public void setTableNameInDb(String tableNameInDb) {
        this.tableNameInDb = tableNameInDb;
    }

    // Quan trọng: Override toString() để ComboBox hiển thị tên sự kiện thay vì object reference
    @Override
    public String toString() {
        return eventName + " (" + year + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OlympicEvent that = (OlympicEvent) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}