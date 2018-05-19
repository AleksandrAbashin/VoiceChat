package src; /**
 * Created by Aleksandr on 28.03.2018.
 */
import java.io.Serializable;

public class Message implements Serializable {

    private final MessageType type;
    private final String data;
    private byte [] arrayByte;

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
        this.arrayByte = null;
    }

    public Message(MessageType type, byte [] arrayByte) {
        this.type = type;
        this.arrayByte = arrayByte;
        this.data = null;
    }

    public Message(MessageType type) {
        this.type = type;
        this.data = null;
        this.arrayByte = null;
    }

    public byte [] getArrayByte() {
        return arrayByte;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}