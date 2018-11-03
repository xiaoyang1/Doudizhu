package message;

import constant.MessageType;

import java.io.Serializable;

public abstract class Message<T> implements Serializable {
    protected MessageType messageType;
    protected T body;

    public Message(MessageType messageType, T body){
        this.messageType = messageType;
        this.body = body;
    }

    protected Message() {
    }

    public abstract MessageType getMessageType();

    public abstract void setMessageType(MessageType messageType);

    public abstract T getBody();

    public abstract void setBody(T body);


}
