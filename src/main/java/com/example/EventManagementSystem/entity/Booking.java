package com.suman.eventmanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Booking {
    @Id
    @GeneratedValue
    private Long id;
    private Long eventId;
    private String user;
    private String pass;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", user='" + user + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }

}

