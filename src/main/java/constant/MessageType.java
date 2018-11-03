package constant;

public enum MessageType {

    ACK(0, "成功"),

    NAK(-1, "失败"),

    LOGIN_IN(9999, "登录游戏"),

    LOGIN_OUT(9998, "退出登录"),

    COME_IN(1000, "进入房间"),

    LEAVE(1001, "离开房间"),

    UPDATE_USER_INFO(1002, "进入或退出房间后修改用户房间id"),

    READY(1020, "准备"),

    CANCEL_READY(1021, "取消准备"),

    GAME_START_CARDS(1030, "一局开始，发牌"),

    NOTES_IN(1040, "叫地主"),

    REPLY_FOR_NOTES_IN(1041, "回复是否叫地主"),

    CONFIRM_DIZHU(1042, "地主确认信息"),

    GET_CARD(1048, "获得自己的手牌"),

    TOKEN_OUT(1050, "出牌Token轮询"),

    CARD_OUT(1051, "出牌"),

    PASS(1060, "过"),

    CARD_OVER(1080, "牌打完了"),

    GAME_OVER(1090, "游戏结束"),

    CLEAN_UP(2000, "清理状态"),

    CONGRATULATION(6666, "祝贺获胜"),

    CHAT(8888, "聊天"),

    REQUEST_ROOM_INFO(8800, "获取房间信息"),

    RESPONSE__ROOM_INFO(8801, "返回房间信息"),

    SERVER_INFO(7777, "等待"),
    ;


    private MessageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private final int code;

    private final String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
