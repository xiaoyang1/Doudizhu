package utils;

import constant.PokerType;
import entity.Card;
import entity.Poker;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
@Data
public class CardUtils {
    // 判断是否是 单张
    public static boolean isSingle(List<Card> cards){
        return cards.size() == 1;
    }

    // 判断是否是 对子  ok
    public static boolean isCouble(List<Card> cards){
        // 因为放大了 100 倍，所以4张3的插值的绝对值 小于4
        return cards.size() == 2 && cards.get(0).substract(cards.get(1)) < 4;

    }

    // 判断是否是 三张  ok
    public static boolean isThree(List<Card> cards){
        if(cards.size() == 3){
            boolean flag = cards.get(0).substract(cards.get(1)) < 4;
            boolean flag1 = cards.get(0).substract(cards.get(2)) < 4;
            return flag && flag1;
        }
        return false;
    }

    // 判断是否是 三带一  ok
    public static boolean isThreeOne(List<Card> cards){
        if(cards.size() != 4){
            return false;
        }
        Collections.sort(cards);
        // 判断另外的一张在哪, subList 不包含 最后一个位置
        return isThree(cards.subList(0,3)) || isThree(cards.subList(1,4));
    }

    // 判断是否是 三带二, 排序之后，中间那张肯定是三条的，检验即可  ok
    public static boolean isThreeTwo(List<Card> cards){
        if(cards.size() == 5){
            Collections.sort(cards);
            // 判断三条是否在前面
            if(isThree(cards.subList(0, 3)) && isCouble(cards.subList(3, 5))){
                return true;
            }
            return isCouble(cards.subList(0, 2)) && isThree(cards.subList(2, 5));
        }
        return false;
    }

    // 判断是否是顺子  ok
    public static boolean isShunZi(List<Card> cards){
        // 至少5张以上, 小于13张， 即 3 到A
        if(cards.size() > 4 && cards.size() < 13){
            Collections.sort(cards); // 顺序排序, 从小到大
            for(int i = 0; i < cards.size() - 1; i++){
                // 不包含2以上的。
                if(cards.get(i).getGrade() >= 1200){
                    return false;
                }
                if(!isSequence(cards.get(i), cards.get(i+1))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // 判断是够是连队
    public static boolean isLianDui(List<Card> cards){
         if((cards.size() >= 6) && (cards.size() % 2 == 0)){
             Collections.sort(cards);
             for(int i = 0; i < cards.size(); i = i + 2){
                 // 不能有2和王， 2的grade 最小是1300
                 if(cards.get(i).getGrade() >= 1200){
                     return false;
                 }
                 // 说明这两张不是一对
                 if(cards.get(i).substract(cards.get(i+1)) >= 4){
                     return false;
                 }
                 // 看看是否连续
                 if(i+2 < cards.size()){
                     if(!isSequence(cards.get(i+1),cards.get(i+2))){
                         return false;
                     }
                 }
             }
             return true;
         }
         return false;
    }

    // 判断是否是飞机, 不带的。
    public static boolean isFeiJi(List<Card> cards){
        // todo 找三个连续的数的位置
        if(cards.size() >= 6 && cards.size() % 3 == 0){
            Collections.sort(cards);
            for(int i = 0; i < cards.size(); i = i + 3){
                // 不能带有2和王
                if(cards.get(i).getGrade() >= 1200){
                    return false;
                }
                // 不是三张
                if(!isThree(cards.subList(i, i+3))) {
                    return false;
                }
                if(i+3 < cards.size()){
                    if(! isSequence(cards.get(i), cards.get(i+3))){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    // 判断是否是飞机带单
    public static boolean isFeiJiOne(List<Card> cards){
        if(cards.size() >= 8 && cards.size() % 4 == 0){
            Collections.sort(cards);
            int firstThreeIndex = findIndexOfFirstThree(cards);
            int lengthOfThree = cards.size() / 4;
            return isFeiJi(cards.subList(firstThreeIndex, firstThreeIndex + lengthOfThree * 3));
        }
        return false;
    }

    public static boolean isFeijiTwo(List<Card> cards){
        if(cards.size() >= 10 && cards.size() % 5 == 0){
            Collections.sort(cards);
            int firstThreeIndex = findIndexOfFirstThree(cards);
            int lengthOfThree = cards.size() / 5;
            if(isFeiJi(cards.subList(firstThreeIndex, firstThreeIndex + lengthOfThree * 3))) {
                for (int i = 0; i < firstThreeIndex; i = i + 2) {
                    // 判断是否是对子
                    if(! (cards.get(i).substract(cards.get(i+1)) < 4)){
                        return false;
                    }
                }
                for (int i = firstThreeIndex + lengthOfThree * 3; i < cards.size(); i = i + 2) {
                    // 判断是否是对子
                    if(! (cards.get(i).substract(cards.get(i+1)) < 4)){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 判断是否是炸弹
    public static boolean isBoom(List<Card> cards){
        if(cards.size() == 4){
            for(int i = 0; i < cards.size()-1; i++){
                if(cards.get(i).substract(cards.get(i+1)) >= 4){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // 判断是否是四带一
    public static boolean isBoomOne(List<Card> cards){
        if(cards.size() == 5){
            Collections.sort(cards);
            return isBoom(cards.subList(0, 4)) || isBoom(cards.subList(1, 5));
        }
        return false;
    }

    // 判断是否是四带二
    public static boolean isBoomTwo(List<Card> cards){
        if(cards.size() == 6){
            Collections.sort(cards);
            // 判断四个的位置在哪？
            return isBoom(cards.subList(0,4)) || isBoom(cards.subList(1,5)) || isBoom(cards.subList(2,6));
        }
        return false;
    }

    // 判断是否是四带两对
    public static boolean isBoomTwoDouble(List<Card> cards){
        if(cards.size() == 8){
            Collections.sort(cards);
            if(isBoom(cards.subList(0, 4))){
                return isCouble(cards.subList(4, 6)) && isCouble(cards.subList(6, 8));
            }
            if(isBoom(cards.subList(2, 6))){
                return isCouble(cards.subList(0, 2)) && isCouble(cards.subList(6, 8));
            }
            if(isBoom(cards.subList(4, 8))){
                return isCouble(cards.subList(0, 2)) && isCouble(cards.subList(2, 4));
            }
        }
        return false;
    }

    // 判断是否是火箭
    public static boolean isRocket(List<Card> cards){
        if(cards.size() == 2){
            int one = cards.get(0).getGrade();
            int two = cards.get(1).getGrade();
            return (one+two) == (1400+1450);
        }
        return false;
    }

    // 获得牌型
    public static PokerType getPokerType(List<Card> cards){
        if(cards == null || cards.size() == 0){
            return PokerType.ILLEGAL;
        }
        switch (cards.size()){
            case 1:
                return PokerType.SINGLE;
            case 2:{
                if(isRocket(cards)) {return PokerType.ROCKET;}
                if(isCouble(cards)) {return PokerType.COUPLE;}
                break;
            }
            case 3:{
                if(isThree(cards)) {return PokerType.THREE;}
                break;
            }
            case 4:{
                if(isBoom(cards)) {return PokerType.BOOM;}
                if(isThreeOne(cards)) {return PokerType.THREE_ONE;}
                break;
            }
            case 5:{
                if(isThreeTwo(cards)) {return PokerType.THREE_TWO;}
                if(isShunZi(cards)) {return PokerType.SHUNZI;}
                if(isBoomOne(cards)) {return PokerType.BOOM_ONE;}
                break;
            }
            default:{  // 大于5张牌的只有顺子，连队，四带二，飞机
                if(isShunZi(cards)) {return PokerType.SHUNZI;}
                if(isLianDui(cards)) {return PokerType.LIANDUI;}
                if(isFeiJiOne(cards)) {return PokerType.FEIJI_ONE;}
                if(isFeijiTwo(cards)) {return  PokerType.FEI_JI_TWO;}
                if(isFeiJi(cards)) {return PokerType.FEIJI;}
                if(isBoomTwo(cards)) {return PokerType.BOOM_TWO;}
                if(isBoomTwoDouble(cards)) {return PokerType.BOOM_TWO_DOUBLE;}
                break;
            }
        }
        return PokerType.ILLEGAL;
    }

    // 未确定牌型的比较
    public static boolean isBiggerThan(List<Card> others, List<Card> yours){
        PokerType othersType = getPokerType(others);
        PokerType yoursType = getPokerType(yours);

        if(yoursType.getPriority() > othersType.getPriority()){
            return true;
        }
        return isBiggerThan(others, yours, yoursType);
    }

    // 只有同一个牌型，同已优先级的才能比较，才会调用这个函数
    public static boolean isBiggerThan(List<Card> others, List<Card> yours, PokerType type){
        if(others == null || yours == null || others.size() == 0 || yours.size() == 0){
            return false;
        }
        Collections.sort(others);
        Collections.sort(yours);

        switch (type){
            // 单张， 一对， 三条 ， 连队， 顺子， 飞机不带， 炸弹
            case SINGLE:
            case COUPLE:
            case THREE:
            case LIANDUI:
            case SHUNZI:
            case FEIJI:
            case BOOM:
                return yours.get(0).getGrade() - others.get(0).getGrade() > 4;
            // 三带一， 三带二， 四带一，四带二，只需要比较最中间的那个数，谁大谁小就行
            case THREE_ONE:
            case THREE_TWO:
            case BOOM_ONE:
            case BOOM_TWO:{
                int middle = yours.size()/2;
                return  yours.get(middle).getGrade() - others.get(middle).getGrade() > 4;
            }
            // 飞机带单，带对，和四带二都是可以通过找到第一个三条的位置，比较大小就行。
            case FEIJI_ONE:
            case FEI_JI_TWO:
            case BOOM_TWO_DOUBLE:{
                int firstThreeIndexOfOthers = findIndexOfFirstThree(others);
                int firstThreeIndexOfYours = findIndexOfFirstThree(yours);
                return yours.get(firstThreeIndexOfYours).getGrade() - others.get(firstThreeIndexOfOthers).getGrade() > 4;
            }

        }
        return false;
    }

    // 检查排序，看看是否牌合法，
    public static boolean checkPoke(List<Card> others, List<Card> yours){
        PokerType othersType = getPokerType(others);
        PokerType yoursType = getPokerType(yours);

        if(yoursType.getPriority() > othersType.getPriority()){
            return true;
        }
        return yoursType == othersType && yours.size() == others.size();
    }

    // 判断是否连续
    private static boolean isSequence(Card left, Card right){
        // 这里获得的数字是在Poker中，生成时number数组的index
        int previousNumber = left.getGrade()/100;
        int nextNumber = right.getGrade()/100;
        if(Math.abs(nextNumber - previousNumber) != 1){
            return false;
        }
        return true;
    }

    // 找到第一个三条的位置
    private static int findIndexOfFirstThree(List<Card> cards){
        int i = 0;
        while(i < cards.size() - 3){
            if(isThree(cards.subList(i, i+3))){
                return i;
            }
            i++;
        }
        return -1;
    }

    public static void main(String[] args) {
        // 测试
        Poker poker = Poker.getPoker();
        LinkedList<Card> pokers = poker.getPokers();

        // 判断连续
        // System.out.println(isSequence(pokers.get(4), pokers.get(11)));

        // // 测试一对
        // List doub = getList(pokers, 0,1);
        // List doub_1 = getList(pokers, 7,8);
        // System.out.println(doub + "  : is " + isDouble(doub));
        // System.out.println(doub_1 + "  : is " + isDouble(doub_1));


        // 测试三张
        // List three = getList(pokers, 0,1,2);
        // List three_1 = getList(pokers, 7,8,9);
        // List three_2 = getList(pokers, 51, 52, 53);
        // System.out.println(three + "  : is " + isThree(three));
        // System.out.println(three_1 + "  : is " + isThree(three_1));
        // System.out.println(three_2 + "  : is " + isThree(three_2));

        // 测试三带一
        // List threeOne = getList(pokers, 0,1,2,3);
        // List threeOne_1 = getList(pokers, 7,8,9,10);
        // List threeOne_2 = getList(pokers, 4,51, 52, 53);
        // List threeOne_3 = getList(pokers, 6,7,8,9);
        //
        // System.out.println(threeOne + "  : is " + isThreeOne(threeOne));
        // System.out.println(threeOne_1 + "  : is " + isThreeOne(threeOne_1));
        // System.out.println(threeOne_2 + "  : is " + isThreeOne(threeOne_2));
        // System.out.println(threeOne_3 + "  : is " + isThreeOne(threeOne_3));

        // 测试三带二
        // List threeOne = getList(pokers, 0,1,2,3, 9);
        // List threeOne_1 = getList(pokers, 4,7,8,9,10);
        // List threeOne_2 = getList(pokers, 4,20, 51, 52, 53);
        // List threeOne_3 = getList(pokers, 6,7,8,9, 15);
        //
        // System.out.println(threeOne + "  : is " + isThreeTwo(threeOne));
        // System.out.println(threeOne_1 + "  : is " + isThreeTwo(threeOne_1));
        // System.out.println(threeOne_2 + "  : is " + isThreeTwo(threeOne_2));
        // System.out.println(threeOne_3 + "  : is " + isThreeTwo(threeOne_3));

        // 测试顺子
        // List shunzi = getList(pokers, 0,1,2,3, 7);
        // List shunzi_1 = getList(pokers, 4,7,8,9,10);
        // List shunzi_2 = getList(pokers, 4,20,51, 52, 53);
        // List shunzi_3 = getList(pokers, 3,4,11,13,19,23);
        // List shunzi_4 = getList(pokers, 3,4,11,13,19,20,23);
        // List shunzi_5 = getList(pokers, 3,4,11,13,19,22,26,31,34, 36,40,46);
        // List shunzi_6 = getList(pokers, 32,37, 41, 46, 50);
        //
        // System.out.println(shunzi + "  : is " + isShunZi(shunzi));
        // System.out.println(shunzi_1 + "  : is " + isShunZi(shunzi_1));
        // System.out.println(shunzi_2 + "  : is " + isShunZi(shunzi_2));
        // System.out.println(shunzi_3 + "  : is " + isShunZi(shunzi_3));
        // System.out.println(shunzi_4 + "  : is " + isShunZi(shunzi_4));
        // System.out.println(shunzi_5 + "  : is " + isShunZi(shunzi_5));
        // System.out.println(shunzi_6 + "  : is " + isShunZi(shunzi_6));

        // 测试连对
        // List liandui = getList(pokers, 3,0,4,5, 10,8);
        // List liandui_1 = getList(pokers, 3,0,4,5, 10,8, 20);
        // List liandui_2 = getList(pokers, 3,0,4,5, 20,8);
        // List liandui_3 = getList(pokers, 3,0,1,5, 10,8);
        // List liandui_4 = getList(pokers, 3,0,4,5, 10,8,52);
        // List liandui_5 = getList(pokers, 40,41,46,47, 48, 50);
        // List liandui_6 = getList(pokers, 44,47,40,43, 37,38,34,35,28,31,25,27,20,23,16,17);
        // System.out.println(liandui + "  :   " + isLianDui(liandui));
        // System.out.println(liandui_1 + "  :   " + isLianDui(liandui_1));
        // System.out.println(liandui_2 + "  :   " + isLianDui(liandui_2));
        // System.out.println(liandui_3 + "  :   " + isLianDui(liandui_3));
        // System.out.println(liandui_4 + "  :   " + isLianDui(liandui_4));
        // System.out.println(liandui_5 + "  :   " + isLianDui(liandui_5));
        // System.out.println(liandui_6 + "  :   " + isLianDui(liandui_6));


        // 测试返回牌型是否正确；
        List type_1 = getList(pokers, 0);       // 单张
        List type_2 = getList(pokers, 0, 3);    // 一对
        List type_3 = getList(pokers, 0, 4);    // 不合法
        List type_4 = getList(pokers, 0, 1, 2);  // 三张
        List type_5 = getList(pokers, 0,2,3,4);   // 三带一
        List type_6 = getList(pokers, 0,2,3,4,7); // 三带二
        List type_7 = getList(pokers, 28, 33,38, 40, 47);   // 顺子
        List type_8 = getList(pokers, 3,4,11,13,19,22,26,31,34, 36,40,46);   // 最长顺子
        List type_9 = getList(pokers, 44,47,40,43, 37,38,34,35,28,31,25,27,20,23,16,17);   //  连队
        List type_10 = getList(pokers, 3,0,4,5, 10,8);   //  连队

        System.out.println(type_1 + "   :  " + getPokerType(type_1));
        System.out.println(type_2 + "   :  " + getPokerType(type_2));
        System.out.println(type_3 + "   :  " + getPokerType(type_3));
        System.out.println(type_4 + "   :  " + getPokerType(type_4));
        System.out.println(type_5 + "   :  " + getPokerType(type_5));
        System.out.println(type_6 + "   :  " + getPokerType(type_6));
        System.out.println(type_7 + "   :  " + getPokerType(type_7));
        System.out.println(type_8 + "   :  " + getPokerType(type_8));
        System.out.println(type_9 + "   :  " + getPokerType(type_9));
        System.out.println(type_10 + "   :  " + getPokerType(type_10));


        List type_11 = getList(pokers, 52, 53);  // 王炸
        List type_12 = getList(pokers, 0,1,2,3);  // 炸
        List type_13 = getList(pokers, 0,1,2,3, 53);  // 四带一
        List type_14 = getList(pokers, 0,1,2,3, 52, 53);  // 四带二
        List type_15 = getList(pokers, 0,1,2,3, 4,6);  // 四带二
        System.out.println(type_11 + "   :  " + getPokerType(type_11));
        System.out.println(type_12 + "   :  " + getPokerType(type_12));
        System.out.println(type_13 + "   :  " + getPokerType(type_13));
        System.out.println(type_14 + "   :  " + getPokerType(type_14));
        System.out.println(type_15 + "   :  " + getPokerType(type_15));

        List feiji_1 = getList(pokers,0,1,3,4,5,6);    // 飞机不带
        List feiji_2 = getList(pokers,0,1,3,4,5,6, 52);  // 不合法
        List feiji_3 = getList(pokers,0,1,3,4,5,6, 52,53);  // 飞机带单
        List feiji_4 = getList(pokers,0,1,3,4,5,6, 16,19, 36,38);  // 飞机带对

        System.out.println(feiji_1 + "   :   " + getPokerType(feiji_1));
        System.out.println(feiji_2 + "   :   " + getPokerType(feiji_2));
        System.out.println(feiji_3 + "   :   " + getPokerType(feiji_3));
        System.out.println(feiji_4 + "   :   " + getPokerType(feiji_4));

        List<Card> temp = getList(pokers, 8,9,10,11,24,25);
        System.out.println(isBiggerThan(type_15, temp));
    }

    public static List<Card> getList(List<Card> pokes, int... indexs){
        List<Card> cards = new ArrayList<>();
        for(int index: indexs){
            cards.add(pokes.get(index));
        }
        return cards;
    }
}
