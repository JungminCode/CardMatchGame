package com.jmsoft.cardmatchgame;

public class CardGameThread extends Thread
{
    // CardGameView의 checkMatch 함수 접근을 위해, 뷰의 주소를 얻어온다.
    CardGameView view;

    // 생성자로, 뷰를 얻어온다.
    CardGameThread(CardGameView view)
    {
        this.view = view;
    }

    @Override
    public void run()
    {
        while(true)
        {
            view.checkMatch();
        }
    }
}
