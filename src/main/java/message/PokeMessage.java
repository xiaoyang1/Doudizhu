package message;

import constant.MessageType;
import entity.Card;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

/**
 *  这个主要用来封装有关牌的信息， 比如：发牌，弃牌，打出牌
 */
public class PokeMessage extends Message<Collection<Card>> implements Serializable {

    private String name;
    public PokeMessage(MessageType messageType, String name, Collection<Card> body) {
        super(messageType, body);
        this.name = name;
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
    public Collection<Card> getBody() {
        return this.body;
    }

    @Override
    public void setBody(Collection<Card> body) {
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PokeMessage{" +
                "name='" + name + '\'' +
                ", messageType=" + messageType +
                ", body=" + body +
                '}';
    }


}
