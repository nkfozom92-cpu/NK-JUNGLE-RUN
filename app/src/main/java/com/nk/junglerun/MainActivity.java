
package com.nk.junglerun;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.*;
import android.view.MotionEvent;
import android.content.Context;
import java.util.Random;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        getWindow().setNavigationBarColor(Color.BLACK);
        setContentView(new GameView(this));
    }

    class GameView extends View {

        Paint p = new Paint(3);
        Random random = new Random();

        int score = 0;
        int lane = 1;
        int coins = 0;
        float speed = 12;
        float playerY = 0;
        boolean jumping = false;
        boolean gameOver = false;

        float[] obstacleX = new float[8];
        float[] obstacleY = new float[8];
        float[] coinX = new float[10];
        float[] coinY = new float[10];

        long lastTime = 0;

        GameView(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            for(int i=0;i<8;i++) {
                obstacleY[i] = -i*300;
                obstacleX[i] = random.nextInt(3);
            }

            for(int i=0;i<10;i++) {
                coinY[i] = -i*200-100;
                coinX[i] = random.nextInt(3);
            }
        }

        void color(int c) {
            p.setColor(c);
            p.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            float w=getWidth(), h=getHeight();
            long now=System.currentTimeMillis();

            if(lastTime==0) lastTime=now;
            float dt=Math.min((now-lastTime)/16f,3);
            lastTime=now;

            drawJungle(c,w,h);
            drawRoad(c,w,h);

            if(!gameOver) {
                score += (int)(dt);
                speed = Math.min(12+score/150f,22);
            }

            for(int i=0;i<8;i++) {
                if(!gameOver) obstacleY[i]+=speed*dt;

                float y=obstacleY[i];
                float x=w*(0.32f+obstacleX[i]*0.18f);

                if(y>h) {
                    obstacleY[i]=-random.nextInt(500)-200;
                    obstacleX[i]=random.nextInt(3);
                }

                if(!gameOver && Math.abs(y-h*0.78f)<35 &&
                   (int)obstacleX[i]==lane &&
                   playerY<35) {
                    gameOver=true;
                }

                drawObstacle(c,x,y,w,h);
            }

            for(int i=0;i<10;i++) {
                if(!gameOver) coinY[i]+=speed*dt;

                float y=coinY[i];
                float x=w*(0.32f+coinX[i]*0.18f);

                if(y>h) {
                    coinY[i]=-random.nextInt(500)-200;
                    coinX[i]=random.nextInt(3);
                }

                if(!gameOver && Math.abs(y-h*0.78f)<30 &&
                   (int)coinX[i]==lane) {
                    coins++;
                    coinY[i]=-random.nextInt(500)-200;
                }

                drawCoin(c,x,y,w);
            }

            if(jumping && !gameOver) {
                playerY += 18*dt;
                if(playerY>110) jumping=false;
            } else if(!jumping && playerY>0) {
                playerY=Math.max(0,playerY-18*dt);
            }

            drawPlayer(c,w,h);

            drawHUD(c,w,h);

            if(gameOver) drawGameOver(c,w,h);

            if(!gameOver) postInvalidateDelayed(16);
        }

        void drawJungle(Canvas c,float w,float h) {
            color(Color.rgb(8,35,24));
            c.drawRect(0,0,w,h,p);

            color(Color.rgb(14,65,39));
            for(int i=0;i<10;i++) {
                float x=(i%2==0)?i*w/5:w-i*w/5;
                c.drawCircle(x,h*0.2f,100,p);
                c.drawCircle(x+30,h*0.35f,90,p);
            }

            color(Color.rgb(35,100,52));
            c.drawRect(0,h*.45f,w,h,p);

            color(Color.rgb(190,150,80));
            p.setTextSize(35);
            p.setTypeface(Typeface.create("serif",Typeface.BOLD));
            c.drawText("ANCIENT TEMPLE",w*.22f,h*.18f,p);
        }

        void drawRoad(Canvas c,float w,float h) {
            Path road=new Path();
            road.moveTo(w*.47f,h*.30f);
            road.lineTo(w*.53f,h*.30f);
            road.lineTo(w*.94f,h);
            road.lineTo(w*.06f,h);
            road.close();

            color(Color.rgb(85,69,50));
            c.drawPath(road,p);

            p.setColor(Color.rgb(210,180,120));
            p.setStrokeWidth(4);

            c.drawLine(w*.47f,h*.30f,w*.06f,h,p);
            c.drawLine(w*.53f,h*.30f,w*.94f,h,p);

            for(int i=0;i<2;i++) {
                float top=w*(.5f+(i-0.5f)*.02f);
                float bottom=w*(.5f+(i-0.5f)*.55f);

                p.setColor(Color.argb(140,230,210,160));
                c.drawLine(top,h*.30f,bottom,h,p);
            }
        }

        void drawObstacle(Canvas c,float x,float y,float w,float h) {
            float size=20+y/h*40;

            color(Color.rgb(65,48,37));
            c.drawRoundRect(x-size,y-size,x+size,y+size,10,10,p);

            color(Color.rgb(145,82,40));
            c.drawRect(x-size,y-size,x+size,y-size/2,p);

            color(Color.rgb(230,130,35));
            c.drawCircle(x,y,size/3,p);
        }

        void drawCoin(Canvas c,float x,float y,float w) {
            float r=10+y/getHeight()*15;

            color(Color.rgb(255,190,0));
            c.drawCircle(x,y,r,p);

            color(Color.YELLOW);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            c.drawCircle(x,y,r*.65f,p);
            p.setStyle(Paint.Style.FILL);
        }

        void drawPlayer(Canvas c,float w,float h) {
            float x=w*(.32f+lane*.18f);
            float y=h*.78f-playerY;

            color(Color.rgb(35,35,45));
            c.drawOval(x-27,y-85,x+27,y+5,p);

            color(Color.rgb(20,110,220));
            c.drawRoundRect(x-23,y-70,x+23,y-15,12,12,p);

            color(Color.rgb(230,165,115));
            c.drawCircle(x,y-92,19,p);

            color(Color.rgb(30,30,30));
            c.drawCircle(x,y-110,19,p);

            color(Color.WHITE);
            c.drawCircle(x-6,y-95,3,p);
            c.drawCircle(x+6,y-95,3,p);
        }

        void drawHUD(Canvas c,float w,float h) {
            color(Color.WHITE);
            p.setTextSize(27);
            p.setTypeface(Typeface.DEFAULT_BOLD);

            c.drawText("SCORE: "+score,25,55,p);
            c.drawText("COINS: "+coins,25,95,p);

            p.setTextSize(17);
            c.drawText("NK JUNGLE RUN",w-190,55,p);
        }

        void drawGameOver(Canvas c,float w,float h) {
            color(Color.argb(190,0,0,0));
            c.drawRect(0,0,w,h,p);

            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(42);
            color(Color.YELLOW);
            c.drawText("GAME OVER",w/2,h*.43f,p);

            p.setTextSize(25);
            color(Color.WHITE);
            c.drawText("SCORE: "+score,w/2,h*.51f,p);
            c.drawText("TAP TO RESTART",w/2,h*.61f,p);
            p.setTextAlign(Paint.Align.LEFT);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if(e.getAction()==MotionEvent.ACTION_DOWN) {
                float x=e.getX();

                if(gameOver) {
                    score=0;
                    coins=0;
                    lane=1;
                    playerY=0;
                    jumping=false;
                    gameOver=false;

                    for(int i=0;i<8;i++) {
                        obstacleY[i]=-i*300;
                    }

                    invalidate();
                    return true;
                }

                if(x<getWidth()/3) lane=Math.max(0,lane-1);
                else if(x>getWidth()*2/3) lane=Math.min(2,lane+1);
                else jumping=true;

                invalidate();
            }
            return true;
        }
    }
}
