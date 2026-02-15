package com.thecheatschool.thecheatschool.server.model.tcs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tcs_notify_me_signups", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tcs_notify_me_email", columnNames = {"email"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TCSNotifyMeSignup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime updatedAt;
}
