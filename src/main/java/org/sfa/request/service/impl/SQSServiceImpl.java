//package org.sfa.request.service.impl;
//
//import io.awspring.cloud.sqs.operations.SqsTemplate;
//import org.sfa.request.service.api.SQSService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.sfa.request.config.ObjectMapperConfig;
//import com.fasterxml.jackson.core.JsonProcessingException;
//
///**
// * ClassName: SQSServiceImpl
// * Package: org.sfa.request.service.impl
// * Description:
// *
// * @author Fan Peng
// * Create 2024/8/15 23:45
// * @version 1.0
// */
//@Service
//public class SQSServiceImpl implements SQSService {
//
//    private final SqsTemplate sqsTemplate;
//    private final String sqsUrl;
//
//    public SQSServiceImpl(SqsTemplate sqsTemplate, @Value("${sqs.url}") String sqsUrl) {
//        this.sqsTemplate = sqsTemplate;
//        this.sqsUrl = sqsUrl;
//    }
//
//    @Override
//    public void sendMessage(Object message) {
//        try {
//            String jsonMessage = ObjectMapperConfig.getObjectMapper().writeValueAsString(message);
//            sqsTemplate.send(sqsUrl, jsonMessage);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Error converting message to JSON", e);
//        }
//    }
//}