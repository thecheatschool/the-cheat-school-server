package com.thecheatschool.thecheatschool.server.service.queue;

import com.thecheatschool.thecheatschool.server.config.RabbitMQConfig;
import com.thecheatschool.thecheatschool.server.model.em.EMContact;
import com.thecheatschool.thecheatschool.server.model.em.EMContactRequest;
import com.thecheatschool.thecheatschool.server.model.queue.ContactEmailJob;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContact;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContactRequest;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeRequest;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeSignup;
import com.thecheatschool.thecheatschool.server.repository.EMContactRepository;
import com.thecheatschool.thecheatschool.server.repository.TCSContactRepository;
import com.thecheatschool.thecheatschool.server.repository.TCSNotifyMeRepository;
import com.thecheatschool.thecheatschool.server.service.em.EMEmailService;
import com.thecheatschool.thecheatschool.server.service.tcs.TCSEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "queue.enabled", havingValue = "true")
public class ContactEmailConsumer {

    private final TCSContactRepository tcsContactRepository;
    private final EMContactRepository emContactRepository;
    private final TCSNotifyMeRepository tcsNotifyMeRepository;

    private final TCSEmailService tcsEmailService;
    private final EMEmailService emEmailService;

    @RabbitListener(queues = RabbitMQConfig.CONTACT_EMAIL_QUEUE)
    public void handle(ContactEmailJob job) {
        if (job == null || job.getType() == null || job.getSubmissionId() == null) {
            log.warn("Received invalid email job payload: {}", job);
            return;
        }

        try {
            if (job.getType() == ContactEmailJob.Type.TCS_CONTACT) {
                processTcsContact(job);
                return;
            }
            if (job.getType() == ContactEmailJob.Type.EM_CONTACT) {
                processEmContact(job);
                return;
            }
            if (job.getType() == ContactEmailJob.Type.TCS_NOTIFY_ME) {
                processTcsNotifyMe(job);
                return;
            }

            log.warn("Unknown job type: {}", job.getType());
        } catch (Exception e) {
            log.error("Email consumer failed for job type: {}, submissionId: {}", job.getType(), job.getSubmissionId(), e);
            throw e;
        }
    }

    private void processTcsContact(ContactEmailJob job) {
        TCSContact submission = tcsContactRepository.findById(job.getSubmissionId())
                .orElseThrow(() -> new IllegalArgumentException("TCS submission not found: " + job.getSubmissionId()));

        TCSContactRequest request = new TCSContactRequest(
                submission.getFullName(),
                submission.getEmail(),
                submission.getPhoneNumber(),
                submission.getCollege(),
                submission.getYearOfStudy(),
                submission.getBranch(),
                submission.getHearAboutUs(),
                submission.getHearAboutUsOther()
        );

        tcsEmailService.sendContactEmail(request);
        submission.setStatus("SENT");
        tcsContactRepository.save(submission);
        log.info("TCS contact email sent successfully for submissionId: {}", job.getSubmissionId());
    }

    private void processEmContact(ContactEmailJob job) {
        EMContact submission = emContactRepository.findById(job.getSubmissionId())
                .orElseThrow(() -> new IllegalArgumentException("EM submission not found: " + job.getSubmissionId()));

        EMContactRequest request = new EMContactRequest(
                submission.getName(),
                submission.getCompany(),
                submission.getPhone(),
                submission.getEmail(),
                submission.getSubject(),
                submission.getMessage()
        );

        emEmailService.sendContactEmail(request);
        submission.setStatus("SENT");
        emContactRepository.save(submission);
        log.info("EM contact email sent successfully for submissionId: {}", job.getSubmissionId());
    }

    private void processTcsNotifyMe(ContactEmailJob job) {
        TCSNotifyMeSignup signup = tcsNotifyMeRepository.findById(job.getSubmissionId())
                .orElseThrow(() -> new IllegalArgumentException("TCS notify-me signup not found: " + job.getSubmissionId()));

        TCSNotifyMeRequest request = new TCSNotifyMeRequest(
                signup.getName(),
                signup.getEmail(),
                signup.getPhoneNumber()
        );

        tcsEmailService.sendNotifyMeEmail(request);
        signup.setStatus("SENT");
        tcsNotifyMeRepository.save(signup);
        log.info("TCS notify-me email sent successfully for submissionId: {}", job.getSubmissionId());
    }
}
