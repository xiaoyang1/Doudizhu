package message;

import constant.MessageType;

public class TokenMessage extends Message<String>{

    private String currentPollName;

    public TokenMessage(String currentPollName) {
        super(MessageType.TOKEN_OUT, null);
        this.currentPollName = currentPollName;
    }

    @Override
    public MessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public void setMessageType(MessageType messageType) {
        this.messageType = MessageType.TOKEN_OUT;
    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public void setBody(String body) {

    }

    public String getCurrentPollName() {
        return currentPollName;
    }

    public void setCurrentPollName(String currentPollName) {
        this.currentPollName = currentPollName;
    }
}
