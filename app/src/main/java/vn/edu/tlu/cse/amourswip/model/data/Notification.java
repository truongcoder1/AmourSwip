package vn.edu.tlu.cse.amourswip.model.data;

public class Notification {
    private String userId; // ID của người dùng gửi thông báo
    private String userName; // Tên của người dùng
    private String userImage; // URL hình ảnh của người dùng
    private String lastMessage; // Tin nhắn cuối cùng
    private String time; // Thời gian thông báo
    private boolean isUnread; // Trạng thái chưa xem

    public Notification() {
    }

    public Notification(String userId, String userName, String userImage, String lastMessage, String time, boolean isUnread) {
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.lastMessage = lastMessage;
        this.time = time;
        this.isUnread = isUnread;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }
}