package com.example.EventManagementSystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String location;

    private LocalDateTime eventDate;

    private int availableSeats;
}
