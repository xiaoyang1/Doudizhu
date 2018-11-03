package entity;

import com.alibaba.fastjson.JSONObject;
import constant.MessageType;
import constant.RoomStatus;
import constant.UserStatus;
import io.netty.channel.Channel;
import lombok.Data;
import message.Message;
import message.PokeMessage;
import message.UserActionMessage;
import server.RoomManager;

import java.util.TreeSet;

@Data
public class User {
    private String name;
    private UserStatus status = UserStatus.FREE;  // 记录 当前用户状态，看是否是 游戏中， 等待中， 空闲中。
    private int roomId;  // 房间号， 空闲时默认是 -1；
    private int seat;   // 座位，开局的时候分配
    private Channel channel;
    private boolean isDiZhu;

    private TreeSet<Card> pokes = new TreeSet<>();

    public User(String name) {
        this.name = name;
        roomId = -1;
        seat = -1;
    }

    public void comeInRoom(int roomId){
        Room room = RoomManager.getRoom(roomId);
        if(room == null || room.getStatus() != RoomStatus.NOT_FULL){
            throw new RuntimeException("room doesn't exist or room is full !");
        }
        if(this.status != UserStatus.FREE){
            this.leaveRoom();
        }

        room.comingOne(this);
        JSONObject body = new JSONObject();
        body.put("roomId", roomId);
        body.put("status", this.status);
        Message response = new UserActionMessage(MessageType.UPDATE_USER_INFO, name, body.toJSONString());
        responseToClient(response);
    }

    public void leaveRoom(){
        if(this.status == UserStatus.FREE){
            return;
        }
        Room room = RoomManager.getRoom(roomId);
        if(room.getStatus() == RoomStatus.GAMING){
            room.terminateGame(this);
        }
        if(this.status == UserStatus.READY){
            cancelReady();
        }
        room.leaveOne(this);

        JSONObject body = new JSONObject();
        body.put("roomId", -1);
        body.put("status", this.status);
        Message response = new UserActionMessage(MessageType.UPDATE_USER_INFO, name, body.toJSONString());
        responseToClient(response);
    }

    public void ready(){
        if(this.status == UserStatus.WAITING){
            JSONObject body = new JSONObject();
            body.put("status", UserStatus.READY);
            Message response = new UserActionMessage(MessageType.UPDATE_USER_INFO, name, body.toJSONString());
            responseToClient(response);
            // 先更新状态再执行，防止所有人准备直接到发牌状态。
            Room room = RoomManager.getRoom(roomId);
            room.readyOne(this);
        }
    }

    public void cancelReady(){
        if(this.status != UserStatus.READY){
            return;
        }
        Room room = RoomManager.getRoom(roomId);
        room.cancelReadyOne(this);

        JSONObject body = new JSONObject();
        body.put("status", UserStatus.WAITING);
        Message response = new UserActionMessage(MessageType.UPDATE_USER_INFO, name, body.toJSONString());
        responseToClient(response);
    }

    public void chat(String content){
        if(this.status != UserStatus.FREE){
            Room room = RoomManager.getRoom(roomId);
            room.chat(this, content);
        }
    }

    public String getRole(){  // 获得用户角色， 返回 name (role) ，
        return this.name + (isDiZhu ? "（地主）" : "（农民）");
    }

    public void responseToClient(Message message){
        this.channel.writeAndFlush(message);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", roomId=" + roomId +
                ", seat=" + seat +
                ", isDiZhu=" + isDiZhu +
                '}';
    }

    public void outCard(PokeMessage message) {
        if(this.status == UserStatus.GAMING){
            Room room = RoomManager.getRoom(roomId);
            room.outCard(message, this);
        }
    }

    public void pass(PokeMessage message) {
        if (this.status == UserStatus.GAMING) {
            Room room = RoomManager.getRoom(roomId);
            room.pass(message, this);
        }
    }

    public void gameOver(PokeMessage message) {
        if (this.status == UserStatus.GAMING) {
            Room room = RoomManager.getRoom(roomId);
            room.gameOver(message, this);
        }
    }

    public void cleanup(){
        this.status = UserStatus.WAITING;
        this.isDiZhu = false;
        this.seat = -1;
        this.pokes.clear();
    }
}
