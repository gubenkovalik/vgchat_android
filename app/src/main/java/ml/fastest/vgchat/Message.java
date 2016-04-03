package ml.fastest.vgchat;

/**
 * Created by valik on 15.03.16.
 */
public class Message {

    final private String avatar;
    final private String nickname;
    final private String message;
    final private String created_at;
    final private String user_id;
    private boolean online = false;
    private boolean active = true;

    public Message(String avatar, String nickname, String message, String created_at, String user_id) {
        this.avatar = avatar;

        this.nickname = nickname;
        this.message = message;
        this.created_at = created_at;
        this.user_id = user_id;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUser_id() {
        return user_id;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
