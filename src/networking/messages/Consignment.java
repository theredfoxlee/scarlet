package networking.messages;

public class Consignment extends Message {
    private final String author;
    private final String date;
    private final String message;

    public Consignment(String author, String date, String message) {
        this.author = author;
        this.date = date;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }
}

