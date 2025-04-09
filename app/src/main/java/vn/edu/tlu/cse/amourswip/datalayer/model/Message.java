package vn.edu.tlu.cse.amourswip.datalayer.model;

public class Message {
    private String senderId;
    private String message;
    private long timestamp;
    private String senderImage;

    // Constructor mặc định (yêu cầu bởi Firebase)
    public Message() {}

    public Message(String senderId, String message, long timestamp, String senderImage) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.senderImage = senderImage;
    }

    // Getters và Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }
}
