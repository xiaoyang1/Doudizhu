package client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import constant.MessageType;
import constant.PokerType;
import constant.StringConstant;
import constant.UserStatus;
import entity.Card;
import io.netty.channel.Channel;
import lombok.Data;
import message.Message;
import message.PokeMessage;
import message.TokenMessage;
import message.UserActionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CardUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

@Data
public class ClientProcessor {

    private static Logger logger = LoggerFactory.getLogger(ClientProcessor.class);

    private String name;

    private UserStatus status = UserStatus.FREE;  // 记录 当前用户状态，看是否是 游戏中， 等待中， 空闲中。
    private int roomId = -1;  // 房间号， 空闲时默认是 -1；
    private int seat = -1;   // 座位，开局的时候分配
    private Channel channel;
    private boolean isDiZhu = false;

    private boolean isLogin = false;

    private TreeSet<Card> pokes = new TreeSet<>();

    // 记录上一家出的牌，以及出牌者的索引, 以及出牌的令牌是否在自己手上
    private String lastOutCardUserName = null;
    private List<Card> lastOutCards = new LinkedList<Card>();
    private boolean token = false;

    public ClientProcessor(String name) {
        this.name = name;
    }



    public void process(Message message){
        switch (message.getMessageType()){
            case ACK:
            case NAK:
                System.out.println(message.getBody()); break;
            case RESPONSE__ROOM_INFO:   // 获得房间信息。
                System.out.println("最新房间状态信息为： " + message.getBody()); break;
            case UPDATE_USER_INFO:   // 更新用户状态
                dealWithUpdateInfo(message); break;

            case COME_IN:  // 通知有人 {进入、聊天、离开、 准备、取消准备} 这几种消息都是只需要对内容显示，不需要做逻辑修改
            case CHAT:    // 聊天
            case LEAVE:   // 离开房间
            case READY:   // 准备
            case CANCEL_READY:   // 取消准备
            case SERVER_INFO:   // 系统信息
            case NOTES_IN:     // 服务器问你是否叫地主
            case CONGRATULATION:  // 游戏结束的祝贺信息！
                System.out.println(message.getBody()); break;

            case GAME_START_CARDS:{
                this.pokes = (TreeSet<Card>) ((PokeMessage)message).getBody();
                // logger.info("我的手牌是： " + this.pokes);
                System.out.println("我的手牌是： " + this.pokes);
                break;
            }
            case GET_CARD:
                dealWithGetCard((PokeMessage)message); break;
            case CONFIRM_DIZHU:  // 判断自己是不是地主
                dealWithConfirmDiZhu((PokeMessage)message); break;
            case TOKEN_OUT:    // 处理出牌轮询的 token 令牌
                dealWithToken((TokenMessage) message); break;
            case CARD_OUT:   // 处理出牌
                dealWithCardOut((PokeMessage)message); break;
            case PASS:      // 处理过牌
                dealWithPass((PokeMessage)message); break;
            case GAME_OVER:
                dealWithGameOver((PokeMessage)message); break;
            case CLEAN_UP:
                cleanup(); break;
        }
    }

    private void dealWithGetCard(PokeMessage message) {
        synchronized (this) {
            this.pokes.addAll(message.getBody());
            this.pokes.notifyAll();
        }
    }

    private void cleanup() {
        this.status = UserStatus.WAITING;
        this.isDiZhu = false;
        this.seat = -1;
        this.pokes.clear();

        this.lastOutCardUserName = null;
        this.lastOutCards.clear();
        this.token = false;

        System.out.println("当前状态： " + this);
    }

    private void dealWithGameOver(PokeMessage message) {
        System.out.printf(" 玩家 %s 的剩余手牌为 :  ", message.getName());
        for(Card each : message.getBody()){
            System.out.printf("%8s", each);
        }
        System.out.println();
    }

    private void dealWithPass(PokeMessage message) {
        System.out.println(String.format(StringConstant.PASS, message.getName()));
    }

    private void dealWithCardOut(PokeMessage message) {
        this.lastOutCardUserName = message.getName();
        this.lastOutCards = (List<Card>) message.getBody();
    }

    private void dealWithToken(TokenMessage message) {
       this.token = this.name.equals(message.getCurrentPollName());
    }

    private void dealWithConfirmDiZhu(PokeMessage message) {
        System.out.println("底牌为：  " + message.getBody());
        if(this.name.equals(message.getName())){  // 是地主
            this.isDiZhu = true;
            this.pokes.addAll(message.getBody());
        }
    }

    private void dealWithUpdateInfo(Message message) {

        // this.roomId = Integer.parseInt(message.getBody().toString());
        JSONObject status = JSON.parseObject((String) message.getBody());
        if(status.get("roomId") != null){
            this.roomId = status.getInteger("roomId");
        }
        if(status.get("status") != null){
            this.status = UserStatus.valueOf(status.getString("status"));
        }
        if(status.get("seat") != null){
            this.seat = status.getInteger("seat");
        }
        if(status.get("isDiZhu") != null){
            this.isDiZhu = status.getBoolean("isDiZhu");
        }
        if(status.get("dropPoke") != null){
            // todo 丢弃所有牌， 并反馈回服务器显示。
            this.pokes.clear();
        }
        logger.info("当前用户信息修改为： " + this);
    }


    private void sendMessage(Message message){
        this.channel.writeAndFlush(message);
    }

    public void login(){
        Message message = new UserActionMessage(MessageType.LOGIN_IN, name, "no body");
        sendMessage(message);
        this.isLogin = true;
    }

    public void logout() {
        if(this.isLogin) {
            Message message = new UserActionMessage(MessageType.LOGIN_OUT, name, "");
            sendMessage(message);
            this.isLogin = false;
        }
    }

    public void chat(String content){
        if(this.isLogin) {
            Message message = new UserActionMessage(MessageType.CHAT, name, content);
            sendMessage(message);
        } else {
            logger.error("目前还未登录， 无法发消息聊天！");
        }
    }

    public void requestForRoomInfo() {
        if(this.isLogin) {
            Message request = new UserActionMessage(MessageType.REQUEST_ROOM_INFO, name, "");
            sendMessage(request);
        }else {
            logger.error("目前还未登录， 无法获取房间信息！");
        }
    }

    // 选择进入的房间
    public void comeInRoom(int roomId){
        if(this.isLogin) {
            Message message = new UserActionMessage(MessageType.COME_IN, this.name, String.valueOf(roomId));
            sendMessage(message);
        }
    }

    public void leaveRoom(){
        if(this.isLogin) {
            Message message = new UserActionMessage(MessageType.LEAVE, this.name, "");
            sendMessage(message);
        }
    }

    public void ready(){
        if(this.isLogin) {
            Message message = new UserActionMessage(MessageType.READY, this.name, "");
            sendMessage(message);
        }
    }

    public void cancelReady(){
        if(this.isLogin) {
            Message message = new UserActionMessage(MessageType.CANCEL_READY, this.name, "");
            sendMessage(message);
        }
    }

    @Override
    public String toString() {
        return "ClientProcessor{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", roomId=" + roomId +
                ", seat=" + seat +
                ", isDiZhu=" + isDiZhu +
                ", isLogin=" + isLogin +
                ", token=" + token +
                '}';
    }

    public void dealWithCallDizhu(char reply) {
        if(reply == 'y'){
           sendMessage(new UserActionMessage(MessageType.REPLY_FOR_NOTES_IN, name, "yes"));
        }
        if(reply == 'n'){
            sendMessage(new UserActionMessage(MessageType.REPLY_FOR_NOTES_IN, name, "no"));
        }
    }

    public void showPoker() {
        System.out.printf("手牌序号 ： ");
        for(int i = 0; i < this.pokes.size(); i++){
            System.out.printf("%8d", i);
        }
        System.out.println();
        System.out.printf("我的手牌 ： " );
        for(Card each : this.pokes){
            System.out.printf("%8s", each);
        }
    }

    public void outCard(String cardIndex) {
        String[] outIndex = cardIndex.split(",");
        Object[] cards = this.pokes.toArray();

        List<Card> outCardList = new LinkedList<>();
        try{
            for(String eachIndex : outIndex) {
                outCardList.add((Card) cards[Integer.parseInt(eachIndex.trim())]);
            }
            if(CardUtils.getPokerType(outCardList) == PokerType.ILLEGAL){
                System.out.println("选择的牌为 " + outCardList + ", 牌型不符合要求");
                return;
            }

            if(this.token){
                // 情况 1： 刚叫玩地主， 地主出牌 或者 轮到自己，最后一次出牌的人也是自己。
                if((this.isDiZhu && this.lastOutCardUserName == null) || this.name.equals(this.lastOutCardUserName)){
                    outCard(outCardList);
                    return;
                }
                // 情况 2： 轮到自己,上一次出牌的不是自己.
                boolean isIllegal = CardUtils.checkPoke(this.lastOutCards, outCardList);
                if(isIllegal){
                    if(CardUtils.isBiggerThan(this.lastOutCards, outCardList)){
                        outCard(outCardList);
                    } else {
                        System.out.println(" 所选择的牌 "+ outCardList + " 大不过人家");
                    }
                } else {
                    System.out.println("所选择的牌 " + outCardList + " 不符合规则！ ");
                }
            } else {
                System.out.println("对不起， 还没到你出牌 !");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("请重新出牌 ~ ");
        }
    }


    public void pass() {
        if(!token){
            System.out.println(" 没到出牌阶段 ！ ");
            return;
        }else {
            // 情况 1： 刚叫玩地主， 地主出牌 或者 轮到自己，最后一次出牌的人也是自己。 这时必须出牌，不能pass
            if ((this.isDiZhu && this.lastOutCardUserName == null) || this.name.equals(this.lastOutCardUserName)) {
                System.out.println("该回合你必须出牌， 不能 pass 。");
                return;
            }
            //  发送 pass 请求。
            Message passMessage = new PokeMessage(MessageType.PASS, this.name, null);
            sendMessage(passMessage);
        }
    }

    public void showLastOutCard() {
        if(this.lastOutCardUserName == null || this.lastOutCards == null || this.lastOutCards.size() == 0){
            System.out.println("还没开始出牌！");
        } else {
            System.out.println(this.lastOutCardUserName + "  打出 :  " + this.lastOutCards + "  ; 牌型为： " + CardUtils.getPokerType(this.lastOutCards));
        }
    }

    private void outCard(Collection<Card> outCards){
        // 删去出的牌
        this.pokes.removeAll(outCards);
        Message outCardMessage = null;
        // 牌打完了就结束，否则正常出牌
        outCardMessage = this.pokes.isEmpty() ? new PokeMessage(MessageType.CARD_OVER, this.name, outCards) :
                                        new PokeMessage(MessageType.CARD_OUT, this.name, outCards);
        sendMessage(outCardMessage);
    }

    public void getAndShowCards() {
        synchronized (this) {
            if (this.pokes.isEmpty()) {
                Message getCardMessage = new UserActionMessage(MessageType.GET_CARD, this.name, "");
                sendMessage(getCardMessage);
                try {
                    this.pokes.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.showPoker();
    }
}
