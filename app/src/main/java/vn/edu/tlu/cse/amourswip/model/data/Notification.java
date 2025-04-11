package vn.edu.tlu.cse.amourswip.model.data;

public class Notification {
    private String userId; // ID của người dùng gửi thông báo
    private String userName; // Tên của người dùng
    private String status; // Trạng thái thông báo (ví dụ: "đã match với bạn")
    private String time; // Thời gian thông báo
    private boolean isUnread; // Trạng thái chưa xem
    private boolean isOnline; // Trạng thái online/offline

    public Notification() {
    }

    public Notification(String userId, String userName, String status, String time, boolean isUnread, boolean isOnline) {
        this.userId = userId;
        this.userName = userName;
        this.status = status;
        this.time = time;
        this.isUnread = isUnread;
        this.isOnline = isOnline;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}