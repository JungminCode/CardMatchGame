package com.jmsoft.cardmatchgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CardGameView extends View
{
    // 게임의 상태값 상수.
    public static final int STATE_READY = 0;
    public static final int STATE_GAME = 1;
    public static final int STATE_END= 2;

    // 게임 상태를 저장하는 변수
    private int viewState = STATE_READY;    // 처음은 준비상태.

    Bitmap backGroundImage;     // 배경 이미지 저장 변수
    Bitmap cardBackSideImage;  // 카드 뒷면 저장 변수

    // 각 색깔 별 카드 이미지 저장 변수
    Bitmap cardRed;
    Bitmap cardGreen;
    Bitmap cardBlue;

    // 카드 6장 저장 변수 배열.
    Card shffleCard[][];        // 2줄로 배치할 것이기 때문에, 2차원 배열을 사용.

    // 클릭한 카드 저장 변수
    Card selectCard1 = null;
    Card selectCard2 = null;

    int count = 0;

    public CardGameView(Context context)
    {
        super(context);

        // 배경 이미지를 가져오기.
        backGroundImage = BitmapFactory.decodeResource(getResources(),R.drawable.background);

        // 카드 뒷면 이미지 가져오기.
        cardBackSideImage = BitmapFactory.decodeResource(getResources(),R.drawable.backside);

        // 컬러별 카드 이미지 가져오기
        cardRed = BitmapFactory.decodeResource(getResources(),R.drawable.front_red);
        cardGreen = BitmapFactory.decodeResource(getResources(),R.drawable.front_green);
        cardBlue = BitmapFactory.decodeResource(getResources(),R.drawable.front_blue);

        // 2차원 배열 초기화.
        shffleCard = new Card[3][2];

        // 카드 위치 지정하는 부분
        setCardShuffle();

        // 스레드 실행 부분, CardGameThread 클래스 정의 후 사용.
        CardGameThread cardGameThread = new CardGameThread(this);
        cardGameThread.start();
    }

    // 화면을 그려주는 함수.
    @Override
    protected void onDraw(Canvas canvas)
    {
        // 배경 그리기
        canvas.drawBitmap(backGroundImage,0,0,null);

        // 카드 6장 그리기
        for (int y = 0; y < 2; y++)
        {
            for (int x = 0; x < 3; x++)
            {
                // 카드함에서 해당 위치(x,y)의 카드를 꺼내오기.
                Card tempCard = shffleCard[x][y];

                // 카드의 이미지는 보여야 하는 상태에 따라 처리, 카드의 앞면이 보이는 상태.
                /*
                * 1. 게임이 시작되었을 때,
                * 2. 사용자 카드를 클릭했을 때,
                * 3. 매치가 된 카드 일 경우만 보여준다.
                */
                if(tempCard.state == Card.CARD_SHOW || tempCard.state == Card.CARD_MATCH || tempCard.state == Card.CARD_PLAYEROPEN)
                {
                    // 카드의 색상에 따라, 카드의 앞면 이미지를 그려준다.
                    if(tempCard.color == Card.IMG_RED)      // 빨간색 이미지 일 경우,
                    {
                        canvas.drawBitmap(cardRed, (35 + (x * 150)), (300 + (y * 180)), null);
                    }
                    else if(tempCard.color == Card.IMG_GREEN)       // 초록색 이미지 일 경우
                    {
                        canvas.drawBitmap(cardGreen,(35 + (x * 150)), (300 + (y * 180)), null);
                    }
                    else            // 파란색 이미지 일 경우
                    {
                        canvas.drawBitmap(cardBlue,(35 + (x * 150)), (300 + (y * 180)), null);
                    }
                }
                else        // 상태가 매치가 되지도, 시작된 지 좀 됐고, 사용자가 클릭되지 않았을 경우, 뒷면의 그림을 그린다.
                {
                    canvas.drawBitmap(cardBackSideImage,(35 + (x * 150)), (300 + (y * 180)), null);
                }
            }
        }
    }


    // 화면 터치 이벤트 처리
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(viewState == STATE_READY)            // 1. 어플이 실행되서 첫 터치를 하면, 전부 카드의 상태를 CARD_CLOSE로 변경한다.
        {
            // 게임 시작 시 처리 함수
            startGame();
            viewState = STATE_GAME;
        }
        else if(viewState == STATE_GAME)        // 2. 게임이 진행중 터치 발생시,
        {
            if(selectCard1 != null && selectCard2 != null)      // 선택 된 카드들이 null이 아니면, 잘못 누른 경우가 되버리기 때문에, true값 반환
            {
                return true;
            }

            // 터치된 위치 정보를 얻어온다.
            int posX = (int)event.getX();
            int posY = (int)event.getY();

            // 얻어온 위치정보가 어느 카드의 위에 있는지를 체크한다.
            // 6장의 카드의 위치에다가 판별 영역(Colider)을 잡아준다.
            for(int y= 0; y<2; y++)
            {
                for(int x=0; x<3; x++)
                {
                    // 사각형 영역을 만든다.
                    Rect cardBoxColider = new Rect((35 + (x * 150)), (300 + (y * 180)), (35 + (x * 150) + 120), (300 + (y * 180) + 175));
                    if(cardBoxColider.contains(posX,posY))
                    {
                        if(shffleCard[x][y].state != Card.CARD_MATCH)       // 터치한 카드가 매치상태로 열려 있을 경우, 처리할 필요가 없기 때문에, 매치상태가 아닌 경우를 비교하여 작업
                        {
                            if(selectCard1 == null)     // 첫 번째 선택한 카드가 null일 경우,
                            {
                                selectCard1 = shffleCard[x][y];         // selectCard1 객체에, 해당 카드를 넣어준다.
                                selectCard1.state = Card.CARD_PLAYEROPEN;
                            }
                            else
                            {
                                // 첫번째 선택한 카드가 존재하는 상황 일 경우, 처선째 선택한 카드가 아닌 다른 카드를 터치했을 때 처리
                                if(selectCard1 != shffleCard[x][y])
                                {
                                    selectCard2 = shffleCard[x][y];     // 두 번째 선택한 카드로 지정.
                                    selectCard2.state = Card.CARD_PLAYEROPEN;
                                }
                            }
                        }
                    }
                }
            }
        }
        else if(viewState == STATE_END)
        {
            viewState = STATE_READY;        // 게임이 끝난 후 터치를 한번 더 해주게 되면, 다시 처음 상태로 돌아간다.
            try
            {
                Thread.sleep(1000);
                Toast.makeText(getContext(),"게임을 다시 시작합니다.",Toast.LENGTH_SHORT).show();
                setCardShuffle();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        invalidate();       // 화면을 다시 그려준다.

        return super.onTouchEvent(event);
    }

    // 원래는 섞어야 하지만,임의로 지정을 해줌.
    public void setCardShuffle()
    {
        int red = 0, green = 0, blue = 0;
        Random random = new Random();
        for(int i=0; i<3; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                int rand = random.nextInt(3);
                if(rand == 0 && red < 2)
                {
                    shffleCard[i][j] = new Card(Card.IMG_RED);
                    red++;
                }
                else if(rand == 1 && green < 2)
                {
                    shffleCard[i][j] = new Card(Card.IMG_GREEN);
                    green++;
                }
                else if(rand == 2 && blue < 2)
                {
                    shffleCard[i][j] = new Card(Card.IMG_BLUE);
                    blue++;
                }
                else
                {
                    j--;
                }
            }
        }
       /* shffleCard[0][0] = new Card(Card.IMG_RED);
        shffleCard[1][0] = new Card(Card.IMG_GREEN);
        shffleCard[2][0] = new Card(Card.IMG_BLUE);
        shffleCard[0][1] = new Card(Card.IMG_BLUE);
        shffleCard[1][1] = new Card(Card.IMG_GREEN);
        shffleCard[2][1] = new Card(Card.IMG_RED);*/
    }

    // 시작 시 모든 카드의 상태를 뒷면을 보이게 하는 상태로 바꾸어 주는 함수.
    public void startGame()
    {
        shffleCard[0][0].state = Card.CARD_CLOSE;
        shffleCard[1][0].state = Card.CARD_CLOSE;
        shffleCard[2][0].state = Card.CARD_CLOSE;
        shffleCard[0][1].state = Card.CARD_CLOSE;
        shffleCard[1][1].state = Card.CARD_CLOSE;
        shffleCard[2][1].state = Card.CARD_CLOSE;
    }

    // 매치 함수는, 스레드에서 호출하는 함수이기 때문에, invalidate가 아닌, postInvalidate 함수를 사용한다.
    public void checkMatch()
    {
        // 뒤집힌 두장의 카드가 같은지 아닌지 판단 처리
        // 두장의 카드가 선택되어 있지 않다면
        if(selectCard1 == null || selectCard2 == null)
        {
            return;
        }
        if(selectCard1.color == selectCard2.color)      // 두 카드가 맞았을 경우
        {
            //  두 카드의 상태를 매치되었다는 상태로 바꿔준 뒤, 선택된 카드의 객체 저장공간을 null값으로 초기화.
            selectCard1.state = Card.CARD_MATCH;
            selectCard2.state = Card.CARD_MATCH;
            selectCard1 = null;
            selectCard2 = null;
            count += 2;
        }
        else            // 두 카드가 매치가 안되었을 경우
        {
            try
            {
                Thread.sleep(500);      // 0.5초간 지연 처리를 해준다.
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            // 그리고, 두 카드의 상태를 닫은 상태로 바꿔준 뒤, 선택된 카드의 객체 저장공간을 null값으로 초기화.
            selectCard1.state = Card.CARD_CLOSE;
            selectCard2.state = Card.CARD_CLOSE;
            selectCard1 = null;
            selectCard2 = null;
        }
        if(count >= 6)
        {
            viewState = STATE_END;
            count = 0;
            try
            {
                Thread.sleep(1000);      // 1초간 지연 처리를 해준다.
                startGame();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        postInvalidate();
    }
}
