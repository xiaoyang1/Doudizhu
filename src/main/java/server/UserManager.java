package server;

import constant.StringConstant;
import entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static Logger logger = LoggerFactory.getLogger(UserManager.class);

    private static Map<String, User> userManager = new HashMap<>();

    public static User getUser(String name){
        return userManager.get(name);
    }

    synchronized public static void addUser(User user){
        userManager.put(user.getName(), user);
        logger.info(String.format(StringConstant.LOGIN_IN, user.getName()));
        logger.info("there is total " + userManager.size() + " users at present!");
    }

    synchronized public static void remove(String name){
        User user = userManager.remove(name);
        logger.info(user + "  用户已经下线离开!");
        logger.info("there is total " + userManager.size() + " users at present!");
    }

    synchronized public static boolean containsKey(String name){
        return userManager.containsKey(name);
    }
}
