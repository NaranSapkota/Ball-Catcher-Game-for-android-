package com.real.ballcatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //declaration for frame layout
    private FrameLayout gameFrame;
    private int frameHeight, frameWidth, initialFrameWidth;
    private LinearLayout startLayout;

    //declaring imageView
    private ImageView box, black, orange, pink;
    private Drawable imageBoxRight, imageBoxLeft;

    //declaring box size
    int boxSize;

    //postion for all image view
    private float boxX, boxY;
    private float blackX, blackY;
    private float orangeX, orangeY;
    private float pinkX, pinkY;

    //setting score
    private TextView scoreLabel, highScoreLabel;
    private int score, highScore, timeCount;

    //setting timer
    private Timer timer;
    private Handler handler = new Handler();

    private boolean start_flg = false;
    private boolean action_flg = false;
    private boolean pink_flg = false;

    private SharedPreferences settings;

    private SoundPlayer soundPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        soundPlayer = new SoundPlayer(this);
        gameFrame = findViewById(R.id.gameFrame);
        startLayout = findViewById(R.id.startLayout);
        box = findViewById(R.id.box);
        black = findViewById(R.id.black);
        orange = findViewById(R.id.orange);
        pink = findViewById(R.id.pink);
        scoreLabel = findViewById(R.id.scoreLabel);
        highScoreLabel = findViewById(R.id.highScoreLabel);

        imageBoxLeft = getResources().getDrawable(R.drawable.box_left);
        imageBoxRight = getResources().getDrawable(R.drawable.box_right);

        //setting high score
        settings = getSharedPreferences("Game Data", Context.MODE_PRIVATE);
        highScore = settings.getInt("High Score", 0);
        highScoreLabel.setText("High Score :" + highScore);


    }

    public void startGame(View view) {

        start_flg = true;
        startLayout.setVisibility(View.INVISIBLE);
        if (frameHeight == 0) {

            frameHeight = gameFrame.getHeight();
            frameWidth = gameFrame.getWidth();
            initialFrameWidth = frameWidth;

            boxSize = box.getHeight();
            boxX = box.getX();
            boxY = box.getY();
        }
        frameWidth = initialFrameWidth;

        box.setX(0.0f);  //setting box at lowest position
        black.setY(3000.0f);
        orange.setY(3000.0f);
        pink.setY(3000.0f);

        blackY = black.getY();
        orangeY = orange.getY();
        pinkY = pink.getY();

        box.setVisibility(View.VISIBLE);
        black.setVisibility(View.VISIBLE);
        orange.setVisibility(View.VISIBLE);
        pink.setVisibility(View.VISIBLE);

        timeCount = 0;
        score = 0;
        scoreLabel.setText("Score : 0");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (start_flg) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePostion();
                        }
                    });
                }
            }
        }, 0, 20);

    }


    public Boolean hitCheck(float x, float y) {

        if (boxX <= x && x <= boxX + boxSize && boxY <= y && y <= frameHeight) {

            return true;
        }
        return false;
    }

    public void changePostion() {

        //Add time count
        timeCount += 20;

        //Orange Ball
        orangeY += 12;
        float orangeCenterX = orangeX + orange.getWidth() / 2;
        float orangeCenterY = orangeY + orange.getHeight() / 2;

        if (hitCheck(orangeCenterX, orangeCenterY)) {

            orangeY = frameHeight + 100;
            score += 10;

            soundPlayer.playHitOrangeSound();
        }

        if (orangeY > frameHeight) {

            orangeY = -100;
            orangeX = (float) Math.floor(Math.random() * (frameWidth - orange.getWidth()));
        }
        orange.setX(orangeX);
        orange.setY(orangeY);


        //Pink
        if (!pink_flg && timeCount % 10000 == 0) {

            pink_flg = true;
            pinkY = -20;
            pinkX = (float) Math.floor(Math.random() * (frameWidth - pink.getWidth()));

        }

        if (pink_flg) {

            pinkY += 20;

            float pinkCenterX = pinkX + pink.getWidth() / 2;
            float pinkCenterY = pinkY + pink.getHeight() / 2;

            if (hitCheck(pinkCenterX, pinkCenterY)) {

                pinkY = frameHeight + 30;
                score += 30;
                soundPlayer.playHitPinkSound();

                if (initialFrameWidth > frameWidth * 110 / 100) {

                    frameWidth = frameWidth * 110 / 100;
                    changeFrameWidth(frameWidth);
                }
            }

            if (pinkY > frameHeight) pink_flg = false;
            pink.setX(pinkX);
            pink.setY(pinkY);

        }


        //Black
        blackY += 18;

        float blackCenterX = blackX + black.getWidth() / 2;
        float blackCenterY = blackY + black.getHeight() / 2;
        if (hitCheck(blackCenterX, blackCenterY)) {

            blackY = frameHeight + 100;
            soundPlayer.playHitBlackSound();

            //change frame width
            frameWidth = frameWidth * 80 / 100;
            changeFrameWidth(frameWidth);
            if (frameWidth <= boxSize) {

                gameOver();
            }
        }


        if (blackY > frameHeight) {

            blackY = -100;
            blackX = (float) Math.floor(Math.random() * (frameWidth - black.getWidth()));
        }
        black.setX(blackX);
        black.setY(blackY);

        //Moving the box in x axis only
        if (action_flg) {

            //TAKE touch Action
            boxX += 14;
            box.setImageDrawable(imageBoxRight);
        } else {

            boxX -= 14;
            box.setImageDrawable(imageBoxLeft);
        }

        //checking the position of box
        if (boxX < 0) {

            boxX = 0;
            box.setImageDrawable(imageBoxRight);
        }

        //checking if box is outside frame layout or not
        if (frameWidth - boxSize < boxX) {

            boxX = frameWidth - boxSize;
            box.setImageDrawable(imageBoxLeft);

        }
        box.setX(boxX);
        scoreLabel.setText("Score :" + score);
    }

    public void gameOver() {

        timer.cancel();
        timer = null;
        start_flg = false;

        //set timer for 1 sec  before showing start layout
        try {
            TimeUnit.SECONDS.sleep(1);

        } catch (Exception e) {
            e.printStackTrace();
        }


        changeFrameWidth(initialFrameWidth);
        startLayout.setVisibility(View.VISIBLE);
        box.setVisibility(View.INVISIBLE);
        orange.setVisibility(View.INVISIBLE);
        pink.setVisibility(View.INVISIBLE);
        black.setVisibility(View.INVISIBLE);

        //update  score
        if (score > highScore) {

            highScore = score;
            highScoreLabel.setText("Score :" + highScore);

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("High Score", highScore);
            editor.commit();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (start_flg) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                action_flg = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                action_flg = false;

            }

        }
        return true;
    }

    public void changeFrameWidth(int frameWidth) {

        ViewGroup.LayoutParams params = gameFrame.getLayoutParams();
        params.width = frameWidth;
        gameFrame.setLayoutParams(params);
    }

    public void quitGame(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();

        }else {
            finish();
        }
    }

}
