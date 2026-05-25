package com.emiratiyo.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime expiresAt;
}
