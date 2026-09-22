package com.microservice.notification.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class ObservationConfig {

    @Bean
    public BeanPostProcessor kafkaObservationEnabler() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof ConcurrentKafkaListenerContainerFactory<?, ?> factory) {
                    factory.getContainerProperties().setObservationEnabled(true);
                }
                if (bean instanceof KafkaTemplate<?, ?> kafkaTemplate) {
                    kafkaTemplate.setObservationEnabled(true);
                }
                return bean;
            }
        };
    }

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
