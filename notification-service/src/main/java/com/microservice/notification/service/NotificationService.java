package com.microservice.notification.service;

import com.microservices.order.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @KafkaListener(topics = "order_placed_topic", groupId = "consumer-group")
    public void getOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received OrderPlacedEvent from Kafka {}", orderPlacedEvent);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(orderPlacedEvent.getEmail());
            messageHelper.setSubject(String.format(
                    "Your Order with Order Number %s has been placed successfully",
                    orderPlacedEvent.getOrderNumber()));
            messageHelper.setText(String.format("""
                    Hi %s %s,

                    Your Order with Order Number %s has been placed successfully.

                    Thank you for shopping with us!

                    Best Regards,
                    Spring Shop Team
                    """,
                    orderPlacedEvent.getFirstName(),
                    orderPlacedEvent.getLastName(),
                    orderPlacedEvent.getOrderNumber()));
        };

        try {
            mailSender.send(messagePreparator);
            log.info("Notification email sent successfully for order number: {}", orderPlacedEvent.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send notification email for order number: {}. Error: {}",
                    orderPlacedEvent.getOrderNumber(), e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to send notification email for order number: " + orderPlacedEvent.getOrderNumber(), e);
        }
    }
}

