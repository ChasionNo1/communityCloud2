package com.chasion;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CloudMessageApplication.class)
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;
    @Test
    public void test1(){
        kafkaProducer.sendMessage("test", "hello");
        kafkaProducer.sendMessage("test", "how are you");
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
@Component
class KafkaProducer{
    @Autowired
    private KafkaTemplate kafkaTemplate;
    public void sendMessage(String topic, String content){
        kafkaTemplate.send(topic, content);
    }
}
@Component
class KafkaConsumer{
    @KafkaListener(topics = {"test"})
    public void handle(ConsumerRecord record){
        System.out.println(record.value());
    }
}
