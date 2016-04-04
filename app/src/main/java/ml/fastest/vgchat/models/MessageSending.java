package ml.fastest.vgchat.models;

/**
 * Created by valik on 15.03.16.
 */
public class MessageSending {

    private String message;

    private String access_token;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getMessage() {
        return message;
    }

    public String getAccess_token() {
        return access_token;
    }
}
