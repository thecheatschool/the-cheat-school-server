package com.thecheatschool.thecheatschool.server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "queue.enabled", havingValue = "true")
public class RabbitMQConfig {

    public static final String CONTACT_EXCHANGE = "contact.exchange";
    public static final String CONTACT_EMAIL_QUEUE = "contact.email.queue";
    public static final String ROUTING_KEY_TCS = "tcs.contact.email";
    public static final String ROUTING_KEY_EM = "em.contact.email";

    @Bean
    public DirectExchange contactExchange() {
        return new DirectExchange(CONTACT_EXCHANGE, true, false);
    }

    @Bean
    public Queue contactEmailQueue() {
        return new Queue(CONTACT_EMAIL_QUEUE, true);
    }

    @Bean
    public Binding tcsContactEmailBinding(Queue contactEmailQueue, DirectExchange contactExchange) {
        return BindingBuilder.bind(contactEmailQueue).to(contactExchange).with(ROUTING_KEY_TCS);
    }

    @Bean
    public Binding emContactEmailBinding(Queue contactEmailQueue, DirectExchange contactExchange) {
        return BindingBuilder.bind(contactEmailQueue).to(contactExchange).with(ROUTING_KEY_EM);
    }
}
