package com.thecheatschool.thecheatschool.server.model.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactEmailJob {

    public enum Type {
        TCS_CONTACT,
        EM_CONTACT,
        TCS_NOTIFY_ME
    }

    private Type type;
    private Long submissionId;
    private String requestId;
    private Instant createdAt;
}
