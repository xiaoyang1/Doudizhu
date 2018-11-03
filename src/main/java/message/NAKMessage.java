package message;

import constant.MessageType;

public class NAKMessage extends Message<String>{

    public NAKMessage( String body) {
        super(MessageType.NAK, body);
    }

    @Override
    public MessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public void setMessageType(MessageType messageType) {

    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "NAKMessage{" +
                "messageType=" + messageType +
                ", body=" + body +
                '}';
    }
}
