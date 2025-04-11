package vn.edu.tlu.cse.amourswip.model.data;

public class Message {
    private String messageId; // Thêm thuộc tính messageId
    private String senderId;
    private String message;
    private long timestamp;
    private String senderImage;

    // Constructor mặc định (yêu cầu bởi Firebase)
    public Message() {}

    // Constructor đầy đủ, bao gồm messageId
    public Message(String messageId, String senderId, String message, long timestamp, String senderImage) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.senderImage = senderImage;
    }

    // Getters và Setters cho messageId
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // Getters và Setters cho senderId
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    // Getters và Setters cho message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getters và Setters cho timestamp
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getters và Setters cho senderImage
    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }
}