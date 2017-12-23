package networking;

import java.io.Serializable;


public class MessageCard implements Serializable {
    public String author;
    public String date;
    public String message;

    public MessageCard(String author, String date, String message) {
        this.author = author;
        this.date = date;
        this.message = message;
    }
}

