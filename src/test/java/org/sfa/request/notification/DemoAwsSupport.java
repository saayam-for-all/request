package org.sfa.request.notification;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Test-only helpers for driving and inspecting the LocalStack AWS emulation.
 */
@Component
public class DemoAwsSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final String localstackEndpoint;

    public DemoAwsSupport(SqsClient sqsClient,
                          @Value("${sqs.url}") String queueUrl,
                          @Value("${aws.endpoint-override}") String localstackEndpoint) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.localstackEndpoint = localstackEndpoint;
    }

    public void purgeQueue() {
        try {
            sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(queueUrl).build());
        } catch (RuntimeException e) {
            // PurgeQueueInProgress is only advisory for a fresh demo queue.
        }
    }

    /** Receives everything currently on the queue without deleting it. */
    public List<Message> receiveAll() {
        return sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .visibilityTimeout(0)
                .build()).messages();
    }

    public String peekSingleMessageBody() {
        List<Message> messages = receiveAll();
        if (messages.size() != 1) {
            throw new IllegalStateException("expected exactly 1 queued message, found " + messages.size());
        }
        return messages.get(0).body();
    }

    /** Drains the queue into the SQSEvent shape the Lambda consumer receives. */
    public SQSEvent drainIntoSqsEvent() {
        List<Message> messages = receiveAll();
        List<SQSEvent.SQSMessage> records = new ArrayList<>();

        for (Message message : messages) {
            SQSEvent.SQSMessage record = new SQSEvent.SQSMessage();
            record.setMessageId(message.messageId());
            record.setBody(message.body());
            record.setReceiptHandle(message.receiptHandle());
            records.add(record);

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }

        SQSEvent event = new SQSEvent();
        event.setRecords(records);
        return event;
    }

    public void clearSentEmails() {
        send(HttpRequest.newBuilder(URI.create(localstackEndpoint + "/_aws/ses")).DELETE().build());
    }

    /** Reads the messages LocalStack's SES emulator captured. */
    public List<String> sentEmailRecipients() {
        String payload = send(HttpRequest.newBuilder(URI.create(localstackEndpoint + "/_aws/ses")).GET().build());
        List<String> recipients = new ArrayList<>();
        try {
            JsonNode messages = MAPPER.readTree(payload).path("messages");
            for (JsonNode message : messages) {
                for (JsonNode address : message.path("Destination").path("ToAddresses")) {
                    recipients.add(address.asText());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read SES messages from LocalStack", e);
        }
        return recipients;
    }

    private String send(HttpRequest request) {
        try {
            return HTTP.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException e) {
            throw new IllegalStateException("LocalStack request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted calling LocalStack", e);
        }
    }
}
