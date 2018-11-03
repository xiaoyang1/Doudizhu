package entity;

import com.alibaba.fastjson.JSONObject;
import config.SystemConfig;
import constant.MessageType;
import constant.RoomStatus;
import constant.StringConstant;
import constant.UserStatus;
import lombok.Data;
import message.Message;
import message.PokeMessage;
import message.TokenMessage;
import message.UserActionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CardUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  每个room是一个桌， 这个桌包含进入的 User， channelGroup。 roomid。
 *
 *  每个房间有一个记牌者，
 */
@Data
public class Room {

    private static Logger logger = LoggerFactory.getLogger(Room.class);
    private int roomId;
    private List<User> users;

    private RoomStatus status = RoomStatus.NOT_FULL;

    private Lock lock = new ReentrantLock();
    private AtomicInteger readyCount;
    private Dispatcher dispatcher;

    public Room() {
    }

    public Room(int roomId) {
        this.roomId = roomId;
        users = new ArrayList<>(SystemConfig.ONE_ROOM_SIZE);

        // 这个是没有游戏，不存在令牌, 也没有地主
        readyCount = new AtomicInteger(0);
    }

    public void comingOne(User user){
        try{
            lock.lock();
            if(status != RoomStatus.NOT_FULL){
                throw new RuntimeException("room is full！");
            }
            user.setRoomId(this.roomId);
            user.setStatus(UserStatus.WAITING);
            users.add(user);
            if(users.size() >= 3){
                this.status = RoomStatus.WAITING;
            }
            //  广播消息
            UserActionMessage message = new UserActionMessage(MessageType.COME_IN, user.getName(), String.format(StringConstant.COME_IN_ROOM, user.getName()));
            broadcast(message, null);

            logger.info("房间信息： " + this);
        } finally {
            lock.unlock();
        }
    }

    public void leaveOne(User user){
        try {
            lock.lock();
            users.remove(user);
            user.setStatus(UserStatus.FREE);
            this.status = RoomStatus.NOT_FULL;
            // 返回 玩家离开通知
            UserActionMessage message = new UserActionMessage(MessageType.LEAVE, user.getName(), String.format(StringConstant.LEAVE_ROOM, user.getName()));
            broadcast(message, null);
            logger.info("当前房间状态： " + this);
        } finally {
            lock.unlock();
        }
    }

    public void readyOne(User user){
        user.setStatus(UserStatus.READY);
        readyCount.getAndIncrement();
        UserActionMessage message = new UserActionMessage(MessageType.READY, user.getName(), String.format(StringConstant.READY, user.getName()));
        broadcast(message, null);

        showAllPeopleStatus();

        if(readyCount.get() >= SystemConfig.ONE_ROOM_SIZE){
            // 通知，并发牌。
            message.setBody(StringConstant.ALL_READY_AND_START);
            broadcast(message, null);
            // todo 开始一局游戏。
            System.out.println("游戏开始");
            startOneGame();
        }
    }

    private void startOneGame() {
        this.status = RoomStatus.GAMING;
        dispatcher = new Dispatcher();
    }

    // 用户取消准备
    public void cancelReadyOne(User user){
        user.setStatus(UserStatus.WAITING);
        readyCount.decrementAndGet();
        UserActionMessage message = new UserActionMessage(MessageType.CANCEL_READY, user.getName(), String.format(StringConstant.CancelReady, user.getName()));
        broadcast(message, null);

        showAllPeopleStatus();
        logger.info("当前房间状态： " + this);
    }

    // 结束游戏
    public void terminateGame(User user){
        // 发送中断信息
        Message terminateMessage = new UserActionMessage(MessageType.SERVER_INFO, user.getName(), String.format(StringConstant.ESCAPE, user.getRole()));

        // 强行结束
        dispatcher.cycleStatus = 3;
        // 展示所有人的牌
        dispatcher.showOtherCards(user);

        // 清理现场
        this.cleanup();
    }

    private void broadcast(Message message, String excluxiveName){
        for(User each : users){
            if(!each.getName().equals(excluxiveName)){
                each.getChannel().writeAndFlush(message );
            }
        }
    }


    // private void


    public void chat(User user, String content) {
        UserActionMessage message = new UserActionMessage(MessageType.CHAT, user.getName(), user.getName()+ "  say:  " + content);
        broadcast(message, null);
    }

    // 发牌，更新座位信息，第一个地主叫号

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Room{")
                .append("roomId: " + roomId )
                .append(", userNum: " + users.size())
                .append(", status: " + status);

        if(this.status == RoomStatus.GAMING){
            sb.append(", dizhuIndex: " + dispatcher.dizhuIndex);
        } else {
            sb.append(", readyCount: " + readyCount);
        }

        sb.append("}");

        return sb.toString();
    }

    private void showAllPeopleStatus(){
        System.out.println("*******************************************************");
        for(User each : users){
            System.out.println(each);
        }
        System.out.println("*******************************************************");
    }

    public void replyForNotesIn(String userName, boolean isCall) {
        dispatcher.replyForNotesIn(userName, isCall);
    }

    public void outCard(PokeMessage message, User user) {
        dispatcher.realOutCard(message, user, false);
    }

    public void pass(PokeMessage message, User user) {
        dispatcher.realPass(message, user);
    }

    public void gameOver(PokeMessage message, User user) {
        // 最后出牌结束, 先把最后的牌输出
        dispatcher.realOutCard(message, user, true);
        // 下发获得游戏结束命令，显示所有玩家手牌。
        dispatcher.showOtherCards(user);
        // 发送 祝贺消息
        Message congratulation = new UserActionMessage(MessageType.CONGRATULATION, user.getName(), String.format(StringConstant.CONGRATULATION, user.getRole()));
        broadcast(congratulation, null);
        logger.info(String.format(StringConstant.CONGRATULATION, user.getRole()));

        this.cleanup();
    }

    private void cleanup(){
        dispatcher.cleanStatus();
        // 清空房间状态
        this.status = RoomStatus.WAITING;
        this.readyCount.set(0);
        logger.info("当前房间状态： " + this);

        // 清空 user 信息。
        for(User each : users){
            each.cleanup();
            logger.info("当前用户状态： " + each);
        }
    }

    private class Dispatcher{
        Poker poker = Poker.getPoker();

        private int dizhuIndex = -1;
        private int cycleStatus = 0;  // 一局的状态转移， 0 是发牌阶段， 1是叫地主-抢地主阶段， 2是出牌阶段， 3是结束。

        // 记录每一局的底牌
        TreeSet<Card> bottomPoker = null;

        // 产生地主摇号信息, 从这个开始。
        int from = 0;
        // 叫地主抢地主规则. 1. 无人叫，重新发牌， 2， 只有一个人叫，直接给这个， 3. 一人叫，一人抢，重新询问第一个是否抢， 4. 一人叫，两人抢。
        LinkedList<Integer> jiaoDiZhuList = new LinkedList<>();
        int round = 0;    //最少三次， 最多四次，第三次就会询问第一个叫地主的人

        // 上一个出牌者，和上一个出牌者的牌
        private String lastOutCardUserName;
        private List<Card> lastOutCards = new LinkedList<Card>();
        // 上一个轮询的index
        private int lastPollIndex;

        public Dispatcher() {
            start();
        }


        private void start(){
            dealPoker();
            // 确定位置， 修改所有人的状态，包括座位， 游戏状态， 发牌的牌型。
            for(int index = 0; index < users.size(); index ++){
                User user = users.get(index);
                user.setSeat(index);
                user.setStatus(UserStatus.GAMING);
                JSONObject status = new JSONObject();
                status.put("status", UserStatus.GAMING);
                status.put("seat", index);
                user.responseToClient(new UserActionMessage(MessageType.UPDATE_USER_INFO, user.getName(), status.toJSONString()));
                // 发手牌
                logger.info("玩家 " + user.getName() + " : 手牌为 ： " + user.getPokes());
                user.responseToClient(new PokeMessage(MessageType.GAME_START_CARDS, user.getName(), user.getPokes()));
            }

            this.cycleStatus = 1;
            from = Math.abs(new Random().nextInt() % 3);
            round = 0;
            logger.info(" 当前的from 是 ： " + from);
            callOneToJiaoDiZhu(from, true);
        }

        // 下达叫地主信息， 抢地主信息， boolean type true 是叫地主，false 抢地主
        private void callOneToJiaoDiZhu(int index, boolean type){
            String contene1 = type ? StringConstant.IFCALLDIZHU : StringConstant.IFQIANGDIZHU;
            String content2 = type ? StringConstant.WAITCALLDIZHU : StringConstant.WAITQIANGDIZHU;
            // for(int i = 0; i < users.size(); i++){
            //     User user = users.get(i);
            //     if(i == index){
            //         user.responseToClient(new UserActionMessage(MessageType.NOTES_IN, user.getName(), contene1));
            //     }else {
            //         user.responseToClient(new UserActionMessage(MessageType.SERVER_INFO, user.getName(), String.format(content2, users.get(index).getName())));
            //     }
            // }
            User user = users.get(index);
            broadcast(new UserActionMessage(MessageType.SERVER_INFO, user.getName(), String.format(content2, user.getName())), user.getName());
            user.responseToClient(new UserActionMessage(MessageType.NOTES_IN, user.getName(), contene1));
        }

        // 发牌 , 本想每个房间一个线程， 但是想想，不可能每个房间都起一个线程，那显然服务器时撑不起的。
        private void dealPoker(){
            TreeSet<Card> userPokes_1 = users.get(0).getPokes();
            TreeSet<Card> userPokes_2 = users.get(1).getPokes();
            TreeSet<Card> userPokes_3 = users.get(2).getPokes();

            bottomPoker = poker.dealPoker(userPokes_1, userPokes_2, userPokes_3);

            // logger.info("玩家 " + users.get(0).getName() + " : 手牌为 ： " + userPokes_1);
            // logger.info("玩家 " + users.get(1).getName() + " : 手牌为 ： " + userPokes_2);
            // logger.info("玩家 " + users.get(2).getName() + " : 手牌为 ： " + userPokes_3);
            logger.info("底牌为 ： " + bottomPoker);
        }

        private void replyForNotesIn(String userName, boolean isCall) {
            String replyContent = isCall ? (jiaoDiZhuList.isEmpty() ? " 叫地主！" : "抢地主")
                    :  (jiaoDiZhuList.isEmpty() ? " 不叫！" : "不抢");
            // 叫地主, 采用等待信息返回叫地主和不叫的消息
            broadcast(new UserActionMessage(MessageType.SERVER_INFO, userName, userName + replyContent), userName);

            round ++;
            if(round < 3){
                if(isCall){
                    jiaoDiZhuList.addLast((from+round - 1)%3);
                }
                callOneToJiaoDiZhu((from+round)%3, jiaoDiZhuList.isEmpty());
            } else {
                if(round == 3 && isCall){
                    jiaoDiZhuList.addLast((from+round - 1)%3);
                }

                if(round == 4){
                    confirmDiZhu(isCall ? jiaoDiZhuList.removeFirst() : jiaoDiZhuList.removeLast());
                    return;
                }
                if (jiaoDiZhuList.isEmpty()) {
                    // 没人叫地主，重新发牌
                    start();
                    return;
                }
                if (jiaoDiZhuList.size() == 1) {
                    confirmDiZhu(jiaoDiZhuList.removeFirst());
                } else {
                    // 询问第一个叫地主的人是否抢地主
                    callOneToJiaoDiZhu(jiaoDiZhuList.getFirst(), false);
                }
            }
        }

        // 确认地主信息， 并将底牌发给地主， 然后通知地主出牌
        private void confirmDiZhu(int dizhuIndex){
            this.dizhuIndex = dizhuIndex;
            User dizhu = users.get(dizhuIndex);
            dizhu.setDiZhu(true);
            logger.info("本局游戏的地主为： " + dizhu.getName());
            // 下发农民的通知信息， 状态信息, 下发地主的信息， 状态信息， 广播， 用户通过判断自己是否是指定的名字来判断自己是否是地主。
            Message messageToAll = new PokeMessage(MessageType.CONFIRM_DIZHU, dizhu.getName(), bottomPoker);
            broadcast(messageToAll, null);

            // 通知等待地主出牌信息
            broadcast(new UserActionMessage(MessageType.SERVER_INFO, dizhu.getName(), String.format(StringConstant.WAITFORONETOOUTCARD, dizhu.getRole())), dizhu.getName());
            // 通知地主出牌, 并下发token， 记录当前的出牌者的名字

            this.lastPollIndex = dizhuIndex;
            downSendToken(this.lastPollIndex);
            dizhu.responseToClient(new UserActionMessage(MessageType.SERVER_INFO, dizhu.getName(), "地主请出牌， 通过命令行用 -o 1,3,4,5,6,8 出牌"));

            // 清空所有选地主的状态
            this.jiaoDiZhuList.clear();
            this.round = 0;
            this.cycleStatus = 2;
        }


        private void downSendToken(int currentSeat){
            downSendToken(users.get(currentSeat).getName());
        }
        private void downSendToken(String currentUserName){
            Message tokenMessage = new TokenMessage(currentUserName);
            broadcast(tokenMessage, null);
        }

        public void realOutCard(PokeMessage message, User user, boolean isOver) {
            if(this.cycleStatus == 2) {
                this.lastOutCardUserName = user.getName();
                this.lastOutCards = (List<Card>) message.getBody();

                // 服务器同步，情况所有打出的手牌
                user.getPokes().removeAll(message.getBody());
                // 向下通知 出牌 信息，
                Message outCardMessage = new PokeMessage(MessageType.CARD_OUT, this.lastOutCardUserName, this.lastOutCards);
                Message outCardNoticeMessage = new UserActionMessage(MessageType.SERVER_INFO, this.lastOutCardUserName,
                        String.format(StringConstant.OUT_CARD_INFO, user.getRole()) + this.lastOutCards + "  ; 牌型为：" + CardUtils.getPokerType(this.lastOutCards));
                broadcast(outCardMessage, null);
                broadcast(outCardNoticeMessage, null);

                // 如果没有结束轮询下一个
                if(!isOver) {
                    this.lastPollIndex = (this.lastPollIndex + 1) % 3;
                    pollForNext(this.lastPollIndex);
                }else {
                    this.cycleStatus = 3;
                }
            }
        }

        public void realPass(PokeMessage message, User user) {
            if(this.cycleStatus == 2){
                // 向下通知 pass信息，
                Message passMessage = new PokeMessage(MessageType.PASS, user.getRole(), null);
                broadcast(passMessage, null);

                // 轮询下一个
                this.lastPollIndex = (this.lastPollIndex + 1) % 3;
                pollForNext(this.lastPollIndex);
            }
        }

        private void pollForNext(int nextIndex){
            downSendToken(nextIndex);

            // 下发出牌通知信息
            User nextOne = users.get(nextIndex);
            broadcast(new UserActionMessage(MessageType.SERVER_INFO, nextOne.getName(),
                    String.format(StringConstant.WAITFORONETOOUTCARD, nextOne.getRole())), nextOne.getName());
            users.get(nextIndex).responseToClient(new UserActionMessage(MessageType.SERVER_INFO, nextOne.getName(),
                    String.format(StringConstant.SHOULDYOUOUTCARD, nextOne.getRole())));
        }


        private void showOtherCards(User user) {
            // 下达展示玩家手牌的通知。 玩家获得消息后，展示手牌，并做状态清理工作。
            if(this.cycleStatus == 3) {
                for (User each : users) {
                    if (!each.getPokes().isEmpty()) {
                        Message message = new PokeMessage(MessageType.GAME_OVER, each.getRole(), each.getPokes());
                        broadcast(message, null);
                        logger.info("玩家 " + each.getRole() + " 的剩余手牌为 ： " + each.getPokes());
                    }
                }
            }
        }

        private void cleanStatus(){
            if(this.cycleStatus == 3) {
                // 先下发清理消息
                Message cleanMessage = new UserActionMessage(MessageType.CLEAN_UP, "server", "game over, cleanup !");
                broadcast(cleanMessage, null);
                // 处理服务器状态，
                // 先解决processor 的状态。
                this.lastOutCardUserName = null;
                this.lastPollIndex = 0;
                this.lastOutCards.clear();
                this.jiaoDiZhuList.clear();
                this.bottomPoker.clear();
                this.dizhuIndex = -1;
            }
        }
    }
}
