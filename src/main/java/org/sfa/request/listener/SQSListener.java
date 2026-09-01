//package org.sfa.request.listener;
//
//import io.awspring.cloud.sqs.annotation.SqsListener;
//import org.springframework.stereotype.Component;
//
///**
// * ClassName: SQSListener
// * Package: org.sfa.request.listener
// * Description:
// *
// * @author Fan Peng
// * Create 2024/8/14 23:52
// * @version 1.0
// */
//@Component
//public class SQSListener {
//
//    @SqsListener("${sqs.url}")
//    public void receiveMessage(String message) {
//        System.out.println("Received message: " + message);
//    }
//}
