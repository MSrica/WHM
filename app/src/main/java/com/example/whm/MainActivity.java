package com.example.whm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

import static xdroid.toaster.Toaster.toast;

public class MainActivity extends AppCompatActivity {

// variables
// ---------------------------------------------------------------------------------------------------------------------------------------------------
    // constants
    protected final String out = "Out", in = "In", next = "Next", zero = "0";
    protected final int numberOfBreaths = 30, numberOfSeconds = 15, vibrationMilliseconds = 100;
    protected final int oneMinuteInMilliseconds = 60000, oneSecondInMilliseconds = 1000, minuteInSeconds = 60, hourInSeconds = 3600;

    // variables
    protected String clip = "first";
    protected int seconds = 0;
    protected boolean running = false;

    // views
    protected TextView breathCounterTextView, stopwatchTextView;
    protected Button nextButton, endButton;
    protected ConstraintLayout layout;
    protected ViewTreeObserver vto;

    // miscellaneous
    protected CountDownTimer fifteenSecondsCountdown = null;
    protected final Handler stopwatchHandler = new Handler();

    protected Vibrator vibrator;
    protected MediaPlayer mediaPlayer;

// functions
//---------------------------------------------------------------------------------------------------------------------------------------------------
    // setup
    protected void viewsInitialization(){
    breathCounterTextView = findViewById(R.id.breathCounter);
    stopwatchTextView = findViewById(R.id.stopwatch);
    nextButton = findViewById(R.id.nextButton);
    endButton = findViewById(R.id.endButton);

    layout = findViewById(R.id.mainLayout);
    vto = layout.getViewTreeObserver();

    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
}
    protected void mediaPlayerInitialization(Context c){
        mediaPlayer = MediaPlayer.create(c, R.raw.five_two_eight_hertz);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }
    protected void layoutInitialization(){
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private final ConstraintLayout layout = findViewById(R.id.mainLayout);

            @Override
            public void onGlobalLayout() {
                this.layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                final int layoutWidth  = layout.getMeasuredWidth();
                final int layoutHeight = layout.getMeasuredHeight();

                ViewGroup.LayoutParams nextButtonParams = nextButton.getLayoutParams();

                nextButtonParams.width = (int) (layoutWidth / 1.5);
                nextButtonParams.height = (int) (layoutHeight * 0.25);

                nextButton.setLayoutParams(nextButtonParams);

                breathCounterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (layoutHeight * 0.23));
                stopwatchTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (layoutHeight * 0.2));
            }
        });
    }

    // time
    protected void runStopwatch(){
        stopwatchHandler.post(new Runnable() {
            @Override
            public void run() {
                if (running){
                    final int mins = (seconds % hourInSeconds) / minuteInSeconds;
                    final int secs = seconds % minuteInSeconds;
                    final String time = String.format(Locale.getDefault(), "%d:%02d", mins, secs);
                    stopwatchTextView.setText(time);

                    seconds++;
                }
                stopwatchHandler.postDelayed(this, 1000);
            }
        });
    }
    protected void printTime(long millisecondsUntilFinished){
        final int minutes = (int) (millisecondsUntilFinished / oneMinuteInMilliseconds);
        final int seconds = (int) ((millisecondsUntilFinished / oneSecondInMilliseconds) - (minuteInSeconds * minutes));
        final String time = String.format(Locale.getDefault(), "%d:%02d",  minutes, seconds);
        stopwatchTextView.setText(time);
    }
    protected void fifteenSecondsCountdownInitialization(){
        fifteenSecondsCountdown = new CountDownTimer(numberOfSeconds * oneSecondInMilliseconds, oneSecondInMilliseconds) {
            public void onTick(long millisecondsUntilFinished) {
                nextButton.setEnabled(false);
                printTime(millisecondsUntilFinished);
            }
            public void onFinish() {
                breathCounterTextView.setText(zero);
                nextButton.setEnabled(true);
            }
        }.start();
    }

    // actions
    protected void addToClip(){
        if(clip.equals("first")) clip = stopwatchTextView.getText().toString() + ", ";
        else clip += stopwatchTextView.getText().toString() + ", ";
    }
    protected void breathing(){
        running = false;
        breathCounterTextView.setText(String.valueOf(Integer.parseInt(breathCounterTextView.getText().toString()) + 1));
        if(Integer.parseInt(breathCounterTextView.getText().toString()) == numberOfBreaths-1) nextButton.setText(out);
    }
    protected void noAirHold(){
        running = true;
        breathCounterTextView.setText(String.valueOf(Integer.parseInt(breathCounterTextView.getText().toString()) + 1));
        nextButton.setText(in);
    }
    protected void airHold(){
        running = false;
        nextButton.setText(next);
        fifteenSecondsCountdownInitialization();
    }

    // buttons
    protected void vibrate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(vibrationMilliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        else vibrator.vibrate(vibrationMilliseconds);
    }
    protected void buttonActions(){
        vibrate();

        if(Integer.parseInt(breathCounterTextView.getText().toString()) < numberOfBreaths-1) breathing();
        else{
            if(nextButton.getText().toString().equals(out)) noAirHold();
            else if(nextButton.getText().toString().equals(in)){
                addToClip();
                airHold();
            }
        }
    }
    protected void finishSession(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(!clip.equals("first")) {
            clip = clip.substring(0, clip.length()-2);
            ClipData clipData = ClipData.newPlainText("Times copied!", clip);
            clipboard.setPrimaryClip(clipData);

            toast("Times copied!");
        }else toast("No times available!");
    }

// lifecycle
//---------------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        viewsInitialization();
        layoutInitialization();

        mediaPlayerInitialization(this);
        runStopwatch();

        nextButton.setOnClickListener(new View.OnClickListener() {public void onClick(View v) { buttonActions(); }});
        endButton.setOnClickListener(new View.OnClickListener() {public void onClick(View v) { finishSession(); }});
    }

    @Override
    protected void onStop() {
        mediaPlayer.stop();
        super.onStop();
    }
}