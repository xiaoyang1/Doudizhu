package entity;

import lombok.Data;

/**
 *  这个是 牌 的 Bean
 */

@Data
public class Card implements Comparable<Card>{

    private char number;// 号码
    private char flower;// 花色
    private int grade;// 等级


    public Card(char number, char flower, int grade) {
        super();
        this.number = number;
        this.flower = flower;
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "[" + flower + number + "]";
    }

    public char getNumber() {
        return number;
    }

    public void setNumber(char number) {
        this.number = number;
    }

    public char getFlower() {
        return flower;
    }

    public void setFlower(char flower) {
        this.flower = flower;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int substract(Card that){
        return Math.abs(this.grade - that.grade);
    }

    @Override
    public int compareTo(Card o) {
        return o.grade - this.grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (number != card.number) return false;
        if (flower != card.flower) return false;
        return grade == card.grade;
    }

    @Override
    public int hashCode() {
        int result = (int) number;
        result = 31 * result + (int) flower;
        result = 31 * result + grade;
        return result;
    }
}
