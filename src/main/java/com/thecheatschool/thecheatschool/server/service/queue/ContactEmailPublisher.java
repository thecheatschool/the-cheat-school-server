package com.thecheatschool.thecheatschool.server.service.queue;

import com.thecheatschool.thecheatschool.server.config.RabbitMQConfig;
import com.thecheatschool.thecheatschool.server.model.queue.ContactEmailJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactEmailPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${queue.enabled:false}")
    private boolean queueEnabled;

    public void publishTcs(ContactEmailJob job) {
        publish(job, RabbitMQConfig.ROUTING_KEY_TCS);
    }

    public void publishEm(ContactEmailJob job) {
        publish(job, RabbitMQConfig.ROUTING_KEY_EM);
    }

    private void publish(ContactEmailJob job, String routingKey) {
        if (!queueEnabled) {
            log.debug("Queue disabled - skipping publish for type: {}, submissionId: {}, requestId: {}",
                    job.getType(), job.getSubmissionId(), job.getRequestId());
            return;
        }
        log.info("Publishing email job to queue, type: {}, submissionId: {}, requestId: {}",
                job.getType(), job.getSubmissionId(), job.getRequestId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.CONTACT_EXCHANGE, routingKey, job);
    }
}
