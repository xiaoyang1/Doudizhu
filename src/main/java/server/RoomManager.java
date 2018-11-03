package server;

import com.alibaba.fastjson.JSONObject;
import config.SystemConfig;
import entity.Room;
import entity.User;

import java.util.*;

/**
 *  这个类是用来做服务器端的管理员， 管理者用户和room的从属信息，
 *   <roomId, room>
 */
public class RoomManager {
    private static Map<Integer, Room> roomManager = new HashMap<>();

    public static Room getRoom(Integer roomId){
        return roomManager.get(roomId);
    }

    static {
        // 初始化的时候就创建好房间， 也可以用createAndGetRoomId，自己该逻辑，自定义创建，不过这种方式客户端进入房间要注意。
        for(int i = 1; i <= SystemConfig.MAX_ROOM; i++){
            roomManager.put(i, new Room(i));
        }
    }

    private static int createAndGetRoomId(){
        synchronized (roomManager){
            int size = roomManager.size();

            if(size >= SystemConfig.MAX_ROOM){
                throw new RuntimeException("房间数已满， 不能再创建房间了！");
            }
            Room room = new Room();
            roomManager.put(size+1, room);
            return size+1;
        }
    }

    public static JSONObject getAllRoomStatus(){
        JSONObject jsonObject = new JSONObject();
        List gaming = new ArrayList();
        List waiting = new ArrayList();
        List notFull = new ArrayList();
        synchronized (roomManager){
            for(Map.Entry<Integer, Room> entry: roomManager.entrySet()){
                switch (entry.getValue().getStatus()){
                    case GAMING:
                        gaming.add(entry.getKey());
                        break;
                    case WAITING:
                        waiting.add(entry.getKey());
                        break;
                    case NOT_FULL:
                        notFull.add(entry.getKey());
                        break;
                }
            }
        }
        jsonObject.put("gaming", gaming);  // 正在游戏的
        jsonObject.put("waiting", waiting); // 满人了正在等待的。
        jsonObject.put("notFull", notFull); // 没满人的
        jsonObject.put("free", SystemConfig.MAX_ROOM - roomManager.size()); // 还可以创建的剩余房间数。
        return jsonObject;
    }

    public static void main(String[] args) {
        // ArrayList<User> users = new ArrayList<>();
        // User one = new User();
        // one.setName("one");
        // users.add(one);
        // User two = new User();
        // two.setName("two");
        // users.add(two);
        //
        // one.setName("gaile");
        // users.remove(one);
        // for(User each : users){
        //     System.out.println(each);
        // }
    }
}
