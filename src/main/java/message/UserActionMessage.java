package message;

import constant.MessageType;

import java.io.Serializable;

/**
 *  这个主要用来 封装
 *  登录游戏，
 *  退出游戏，
 *  用户进入房间，
 *  离开房间，
 *  准备
 *  取消准备
 *  聊天
 */
public class UserActionMessage extends Message<String> implements Serializable {

    private String userName;

    public UserActionMessage(MessageType messageType, String userName, String body) {
        super(messageType, body);
        this.userName = userName;
    }

    @Override
    public MessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "UserActionMessage{" +
                "userName='" + userName + '\'' +
                ", messageType=" + messageType +
                ", body=" + body +
                '}';
    }
}
