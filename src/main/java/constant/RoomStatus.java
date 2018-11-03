package constant;

public enum RoomStatus {
    NOT_FULL(101, "not_full"), // 没满人
    WAITING(102, "waiting"),  // 满人了，没完全准备，在等待
    GAMING(103, "gaming");

    private String value;
    private int code;

    RoomStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getCode() {
        return code;
    }
}
