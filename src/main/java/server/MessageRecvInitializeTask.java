package server;

import com.alibaba.fastjson.JSONObject;
import constant.MessageType;
import constant.UserStatus;
import entity.Room;
import entity.User;
import io.netty.channel.ChannelHandlerContext;
import message.*;

import java.util.concurrent.Callable;

public class MessageRecvInitializeTask implements Callable<Boolean> {
    private Message message;
    private ChannelHandlerContext ctx;
    public MessageRecvInitializeTask(Message message, ChannelHandlerContext ctx) {
        this.message = message;
        this.ctx = ctx;
    }

    @Override
    public Boolean call() throws Exception {
        switch (message.getMessageType()){
            case LOGIN_IN:  // 登录
                dealWithLogin((UserActionMessage) message); break;
            case LOGIN_OUT:  // 退出登录
                dealWithLogOut((UserActionMessage) message); break;
            case CHAT:  // 聊天
                dealWithChat((UserActionMessage) message); break;
            case REQUEST_ROOM_INFO:  // 请求获得房间信息
                dealWithGetRoomInfo((UserActionMessage) message); break;
            case COME_IN:  // 进入房间，房间号是body
                dealWithComeInRoom((UserActionMessage) message); break;
            case LEAVE:
                dealWithLeaveRoom((UserActionMessage) message); break;
            case READY:
                dealWithReady((UserActionMessage) message); break;
            case CANCEL_READY:
                dealWithCancelReady((UserActionMessage) message); break;
            case REPLY_FOR_NOTES_IN:  // 处理叫地主抢地主的回复。
                dealWithReplyForNotesIn((UserActionMessage) message); break;
            case GET_CARD:
                dealWithGetCard((UserActionMessage) message); break;
            case CARD_OUT:
                dealWithCardOut((PokeMessage) message); break;
            case PASS:
                dealWithPass((PokeMessage) message); break;
            case CARD_OVER:
                dealWithCardOver((PokeMessage) message); break;
        }

        return Boolean.TRUE;
    }

    private void dealWithGetCard(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        Message response = new PokeMessage(MessageType.GET_CARD, user.getName(), user.getStatus() == UserStatus.GAMING ? user.getPokes() : null);
        user.responseToClient(response);
    }

    private void dealWithCardOver(PokeMessage message) {
        User user = UserManager.getUser(message.getName());
        user.gameOver(message);
    }

    private void dealWithPass(PokeMessage message) {
        User user = UserManager.getUser(message.getName());
        user.pass(message);
    }

    private void dealWithCardOut(PokeMessage message) {
        User user = UserManager.getUser(message.getName());
        user.outCard(message);
    }

    private void dealWithReplyForNotesIn(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        Room room = RoomManager.getRoom(user.getRoomId());
        if(room != null){
            room.replyForNotesIn(message.getUserName(), "yes".equals(message.getBody()));
        }
    }

    private void dealWithCancelReady(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        if(user != null){
            user.cancelReady();
        }
    }

    private void dealWithReady(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        if(user != null){
            user.ready();
        }
    }

    private void dealWithLeaveRoom(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        if(user != null){
            user.leaveRoom();
        }
    }

    private void dealWithComeInRoom(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        if(user != null) {
            Message response = null;
            try {
                int roomId = Integer.parseInt(message.getBody());
                user.comeInRoom(roomId);
                response = new ACKMessage("");
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response = new NAKMessage("进入房间失败： " + e.getMessage());
            }
            user.responseToClient(response);
        }
    }

    private void dealWithGetRoomInfo(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        if(user != null) {
            JSONObject responseJson = RoomManager.getAllRoomStatus();
            Message response = new UserActionMessage(MessageType.RESPONSE__ROOM_INFO, "server", responseJson.toJSONString());
            user.responseToClient(response);
        }
    }

    private void dealWithChat(UserActionMessage message) {
        User user = UserManager.getUser(message.getUserName());
        if(user != null){
            user.chat(message.getBody());
        }
    }

    private void dealWithLogOut(UserActionMessage message) {
        // 退出登录
        User user = UserManager.getUser(message.getUserName());
        Message response = null;
        if(user != null){
            Room room = RoomManager.getRoom(user.getRoomId());
            if(user.getStatus() == UserStatus.GAMING){
                // 游戏中， 此时退出直接停止游戏, 回到所有的为准备状态
                room.terminateGame(user);
            }
            user.cancelReady();
            user.leaveRoom();
            UserManager.remove(user.getName());
            response = new ACKMessage("恭喜你，成功退出了！");
        }else {
            response = new NAKMessage("退出失败！");
        }
        user.responseToClient(response);
    }

    private void dealWithLogin(UserActionMessage message) {
        // 处理登录， 创建一个User， 并赋值name， channel. 避免断线重连
        User user = UserManager.containsKey(message.getUserName()) ? UserManager.getUser(message.getUserName()) : new User(message.getUserName());
        user.setChannel(ctx.channel());
        UserManager.addUser(user);
        Message response = new ACKMessage("恭喜你，成功登录了！");
        user.responseToClient(response);
    }
}
