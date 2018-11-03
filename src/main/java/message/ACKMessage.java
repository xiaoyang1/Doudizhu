package message;

import constant.MessageType;

public class ACKMessage extends Message<String>{

    public ACKMessage(String body) {
        super(MessageType.ACK, body);
    }

    @Override
    public MessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public void setMessageType(MessageType messageType) {
        // 不允许重写
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
        return "ACKMessage{" +
                "messageType=" + messageType +
                ", body=" + body +
                '}';
    }
}
