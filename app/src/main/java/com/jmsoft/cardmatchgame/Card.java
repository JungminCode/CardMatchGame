package com.jmsoft.cardmatchgame;

public class Card
{
    public static final int CARD_SHOW = 0;
    public static final int CARD_CLOSE = 1;
    public static final int CARD_PLAYEROPEN = 2;
    public static final int CARD_MATCH = 3;

    // 카드의 컬러 상수
    public static final int IMG_RED = 1;
    public static final int IMG_GREEN = 2;
    public static final int IMG_BLUE = 3;

    // 객체별로 다른 값을 가지는 변수들.
    public int state;
    public int color;

    // 카드 객체를 생성할 때, 컬러를 지정.
    Card(int color)
    {
        state = CARD_SHOW;
        this.color = color;
    }
}
