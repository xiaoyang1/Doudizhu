package constant;

public enum  UserStatus {
    FREE("free"), // 空闲， 没进入房间
    WAITING("waiting"), // 进入房间，但是没有准备
    READY("ready"), // 进入房间，并且准备
    GAMING("gaming"); // 开始游戏

    private String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
