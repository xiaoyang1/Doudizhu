package entity;

import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class Poker {
    private LinkedList<Card> pokers = new LinkedList<>();
    private char[] flowers = { '♦', '♣', '♥', '♠' };
    private char[] numbers = { '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K', 'A', '2' };

    private Poker() {
        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < flowers.length; j++) {
                // 直接变成 300，301,302,303， 这样好判断牌型
                pokers.add(new Card(numbers[i], flowers[j], j + i * 100));
            }
        }

        pokers.add(new Card('王', '小', 1400));
        pokers.add(new Card('王', '大', 1450));
    }


    public LinkedList<Card> getPokers() {
        return pokers;
    }

    // 初始化牌
    public static Poker getPoker() {
        return new Poker();
    }

    // 打印牌
    public void showPoker() {
        for (Card c : pokers) {
            System.out.println(c.toString());
        }
    }

    // 洗牌和发牌方法
    public TreeSet<Card> dealPoker(Collection<Card> play1, Collection<Card> play2, Collection<Card> play3) {
        Collections.shuffle(pokers);// 洗牌
        while (pokers.size() > 3) {
            play1.add(pokers.removeFirst());
            play2.add(pokers.removeFirst());
            play3.add(pokers.removeFirst());
        }
        return new TreeSet<>(pokers);// 返回3张底牌
    }

    public static void main(String[] args) {

        Poker poker = new Poker();

        poker.getPoker();// 得到牌
        // 每个玩家存储牌的容器
        TreeSet<Card> p1 = new TreeSet<>();
        TreeSet<Card> p2 = new TreeSet<>();
        TreeSet<Card> p3 = new TreeSet<>();
        // 洗牌和发牌操作 返回的是底牌
        TreeSet<Card> dealPoker = poker.dealPoker(p1, p2, p3);

        // // 删除牌
        // System.err.println("玩家一：" + p1);
        // System.err.println("玩家二：" + p2);
        // System.err.println("玩家三：" + p3);
        // System.out.println("底牌    ：" + dealPoker);
        //
        // List<Card> list = new LinkedList<Card>(p1);
        // System.out.println(list);
        System.out.printf("手牌序号 ： ");
        for(int i = 0; i < p1.size(); i++){
            System.out.printf("%8d", i);
        }
        System.out.println();
        System.out.printf("我的手牌 ： " );
        for(Card each : p1){
            System.out.printf("%8s", each);
        }
    }
}
