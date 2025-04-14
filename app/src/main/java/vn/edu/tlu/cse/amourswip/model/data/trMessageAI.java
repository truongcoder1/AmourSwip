package vn.edu.tlu.cse.amourswip.model.data;

public class trMessageAI {
    private String text;
    private boolean isUserMessage;
    private long timestamp;

    public trMessageAI() {
        // Constructor mặc định cho Firebase (nếu cần)
    }

    public trMessageAI(String text, boolean isUserMessage, long timestamp) {
        this.text = text;
        this.isUserMessage = isUserMessage;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public void setUserMessage(boolean userMessage) {
        isUserMessage = userMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}