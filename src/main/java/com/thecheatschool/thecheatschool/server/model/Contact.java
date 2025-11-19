package com.thecheatschool.thecheatschool.server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String college;

    @Column(nullable = false)
    private String yearOfStudy;

    @Column(nullable = false)
    private String branch;

    @Column(nullable = false)
    private String hearAboutUs;

    @Column
    private String hearAboutUsOther;

    @Column(nullable = false)
    private String status; // "EMAIL_SENT" or "EMAIL_FAILED"

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime expiresAt; // 30 days from submission
}