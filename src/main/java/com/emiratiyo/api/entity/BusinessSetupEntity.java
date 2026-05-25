package com.emiratiyo.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_setup_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSetupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String countryOfResidence;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime expiresAt;
}
