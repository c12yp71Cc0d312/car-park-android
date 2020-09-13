package com.example.spiderhackathon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements GameView.GameViewInterface {
    private static final String TAG = "MainActivity";

    private GameView gameView;
    private TextView score;
    private FrameLayout game_view_frame;
    private ConstraintLayout gameWonLayout, gameLostLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = findViewById(R.id.textView_coinsCollected);
        game_view_frame = findViewById(R.id.game_view_frame);
        gameView = new GameView(this, this);
        game_view_frame.addView(gameView);

        gameWonLayout = findViewById(R.id.game_won_layout);
        gameLostLayout = findViewById(R.id.game_lost_layout);

    }

    @Override
    public void getCoins(int c) {
        score.setText("Coins Collected: " + c);
    }

    @Override
    public void gameWon() {
        gameWonLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void gameLost() {
        gameLostLayout.setVisibility(View.VISIBLE);
    }

    public void refreshActivity(View v) {
        gameWonLayout.setVisibility(View.GONE);
        gameLostLayout.setVisibility(View.GONE);
        gameView.resetGame();
    }

}
