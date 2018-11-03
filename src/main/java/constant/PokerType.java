package constant;

public enum PokerType {
    ILLEGAL(0, -1, "illegal"),  // 不合法的牌型
    SINGLE(1, 1, "single"),    // 单张
    COUPLE(2, 1, "couple"),    // 对子
    THREE(3, 1, "three"),      // 三张
    THREE_ONE(4, 1, "three_one"),      // 三带一，
    THREE_TWO(5, 1, "three_two"),   // 三带二
    SHUNZI(6, 1, "shunzi"),       // 顺子
    LIANDUI(7, 1, "liandui"),     // 连队
    FEIJI(8, 1, "feiji"),       // 飞机
    FEIJI_ONE(9, 1, "feiji_one"), // 飞机带单
    FEI_JI_TWO(10, 1, "feiji_two"),  // 飞机带对
    BOOM(11, 2, "bomb"),        // 炸弹
    BOOM_ONE(12, 1, "boom_one"),  // 炸弹带单
    BOOM_TWO(13, 1, "boom_two"),  // 炸弹带对
    BOOM_TWO_DOUBLE(14, 1, "boom_two_double"), // 炸弹带两对
    ROCKET(15, 3, "rocket"),    // 火箭
    ;
    private int code;
    private int priority;
    private String value;

    PokerType(int code, int priority, String value) {
        this.code = code;
        this.priority = priority;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
