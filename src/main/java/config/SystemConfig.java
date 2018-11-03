package config;

public class SystemConfig {
    public static final String SYSTEM_PROPERTY_THREADPOOL_QUEUE_NAME_ATTR = "LinkedBlockingQueue";
    public static final Integer SYSTEM_PROPERTY_PARALLEL = Math.max(2, Runtime.getRuntime().availableProcessors());
    public static final Integer MAX_ROOM = 5;
    public static final Integer ONE_ROOM_SIZE = 3; // 一个房间三个人
}
