package com.chasion.event;

import com.alibaba.fastjson.JSONObject;
import com.chasion.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        System.out.println("event topic:" + event.getTopic());
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
