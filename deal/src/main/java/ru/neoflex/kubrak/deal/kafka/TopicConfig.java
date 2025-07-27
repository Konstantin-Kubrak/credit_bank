package ru.neoflex.kubrak.deal.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicConfig {

    @Value("${deal.kafka.topic.finish-registration}")
    private String topicFinishRegistration;
    @Value("${deal.kafka.topic.create-documents}")
    private String topicCreateDocuments;
    @Value("${deal.kafka.topic.send-documents}")
    private String topicSendDocuments;
    @Value("${deal.kafka.topic.send-ses}")
    private String topicSendSes;
    @Value("${deal.kafka.topic.credit-issued}")
    private String topicCreditIssued;
    @Value("${deal.kafka.topic.statement-denied}")
    private String topicStatementDenied;


    @Bean
    public NewTopic createTopicFinishRegistration(){
        return new NewTopic(topicFinishRegistration, 1, (short) 1);
    }

    @Bean
    public NewTopic createTopicCreateDocuments(){
        return new NewTopic(topicCreateDocuments, 1, (short) 1);
    }

    @Bean
    public NewTopic createTopicSendDocuments(){
        return new NewTopic(topicSendDocuments, 1, (short) 1);
    }

    @Bean
    public NewTopic createTopicSendSes(){
        return new NewTopic(topicSendSes, 1, (short) 1);
    }

    @Bean
    public NewTopic createTopicCreditIssued(){
        return new NewTopic(topicCreditIssued, 1, (short) 1);
    }

    @Bean
    public NewTopic createTopicStatementDenied(){
        return new NewTopic(topicStatementDenied, 1, (short) 1);
    }

}