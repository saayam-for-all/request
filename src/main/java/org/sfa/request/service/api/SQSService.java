package org.sfa.request.service.api;

/**
 * ClassName: SQSService
 * Package: org.sfa.request.service.api
 * Description:
 *
 * @author Fan Peng
 * Create 2024/8/15 23:43
 * @version 1.0
 */
public interface SQSService {
    void sendMessage(Object message);
}
