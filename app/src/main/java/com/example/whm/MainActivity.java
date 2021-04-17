/*
Calming music
Quiet notification when pressing the button
Quiet notifications 3 breaths before 30

Save times automatically(show at the end to copy and paste)
*/

package com.example.whm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final String out = "Out", in = "In", next = "Next", zero = "0";
    final int numberOfBreaths = 30, numberOfSeconds = 15;
    final int oneMinuteInMilliseconds = 60000, oneSecondInMilliseconds = 1000, minuteInSeconds = 60;
    int seconds = 0;
    boolean running = false;

    TextView breathCounterTextView, stopwatchTextView;
    Button nextButton;

    CountDownTimer fifteenSecondsCountdown = null;
    final Handler stopwatchHandler = new Handler();

    protected void printTime(long millisecondsUntilFinished){
        int minutes = (int) (millisecondsUntilFinished / oneMinuteInMilliseconds);
        int seconds = (int) ((millisecondsUntilFinished / oneSecondInMilliseconds) - (minuteInSeconds * minutes));
        String time = String.format(Locale.getDefault(), "%d:%02d",  minutes, seconds);
        stopwatchTextView.setText(time);
    }

    protected void viewsInitialization(){
        breathCounterTextView = findViewById(R.id.breathCounter);
        stopwatchTextView = findViewById(R.id.stopwatch);
        nextButton = findViewById(R.id.nextButton);
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

    protected void buttonActions(){
        if(Integer.parseInt(breathCounterTextView.getText().toString()) < numberOfBreaths-1) {
            breathCounterTextView.setText(String.valueOf(Integer.parseInt(breathCounterTextView.getText().toString()) + 1));
            if(Integer.parseInt(breathCounterTextView.getText().toString()) == numberOfBreaths-1) {
                nextButton.setText(out);
            }
            running = false;
        }else{
            if(nextButton.getText().toString().equals(out)) {
                running = true;
                breathCounterTextView.setText(String.valueOf(Integer.parseInt(breathCounterTextView.getText().toString()) + 1));
                nextButton.setText(in);
            }else if(nextButton.getText().toString().equals(in)){
                running = false;
                nextButton.setText(next);
                fifteenSecondsCountdownInitialization();
            }
        }
    }

    protected void runStopwatch(){
        stopwatchHandler.post(new Runnable() {
            @Override
            public void run() {
                if (running){
                    int mins = (seconds % 3600) / 60;
                    int secs = seconds % 60;

                    String time = String.format(Locale.getDefault(), "%d:%02d", mins, secs);
                    stopwatchTextView.setText(time);

                    seconds++;
                }
                stopwatchHandler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewsInitialization();

        nextButton.setOnClickListener(new View.OnClickListener() {public void onClick(View v) { buttonActions(); }});

        runStopwatch();
    }


}