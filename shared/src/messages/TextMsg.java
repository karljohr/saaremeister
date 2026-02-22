package messages;

public class TextMsg {
    public String text;
    boolean isSentByServer = false;
    public int senderId;

    public TextMsg(String text, int id) {
        this.text = text;
        this.senderId = id;
    }

    public TextMsg() {}

    public String getText() {
        return text;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSentByServer() {
        this.isSentByServer = true;
    }

    public boolean isSentByServer() {
        return isSentByServer;
    }
}
