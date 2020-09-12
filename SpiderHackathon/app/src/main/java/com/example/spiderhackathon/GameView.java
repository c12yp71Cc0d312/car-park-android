package com.example.spiderhackathon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class GameView extends View {
    private static final String TAG = "GameView";

    private Bitmap playerCar;
    private Bitmap playerCarMoving;
    private Bitmap parkSpot;
    private Bitmap spikeBitmap;
    private Bitmap coinBitmap;
    private double canvasWidth;
    private double canvasHeight;
    private Paint drawPaint;
    private Path drawnPath;
    private float carStartPosLeft;
    private float carStartPosTop;
    private int currentStepNo;
    private PathMeasure pathMeasure;
    private Matrix matrix;
    private boolean pathFinished;
    private static boolean initialDraw = true;
    private ArrayList<Coin> coins;
    private ArrayList<Spike> spikes;
    private int coinsCollected;

    private float[] matrixValues;

    public GameView(Context context) {
        super(context);
        initializations();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializations();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializations();
    }

    public void initializations() {
        playerCar = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        playerCarMoving = BitmapFactory.decodeResource(getResources(), R.drawable.player_rot);
        parkSpot = BitmapFactory.decodeResource(getResources(), R.drawable.parkingspot);
        coinBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.coin);
        spikeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.spike);

        drawPaint = new Paint();
        drawPaint.setColor(Color.YELLOW);
        drawPaint.setStrokeWidth(15);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setAntiAlias(true);

        drawnPath = new Path();

        coins = new ArrayList<>();
        coins.add(new Coin(400, 400));

        spikes = new ArrayList<>();
        spikes.add(new Spike(600, 400));

        matrixValues = new float[9];

        coinsCollected = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundResource(R.drawable.road);
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        int carHeight = playerCar.getHeight();
        int carWidth = playerCar.getWidth();

        carStartPosLeft = (float) (canvasWidth - carWidth - 60);
        carStartPosTop = (float) (canvasHeight - carHeight - 60);

        if(pathFinished) {
            matrix = new Matrix();
            pathMeasure = new PathMeasure(drawnPath, false);
            float segmentLength = pathMeasure.getLength() / 30;

            if (currentStepNo <= 120) {
                pathMeasure.getMatrix(segmentLength * currentStepNo, matrix, pathMeasure.POSITION_MATRIX_FLAG + pathMeasure.TANGENT_MATRIX_FLAG);
                canvas.drawBitmap(parkSpot, 60, 60, null);
                canvas.drawPath(drawnPath, drawPaint);
                canvas.drawBitmap(playerCarMoving, matrix, null);
                matrix.getValues(matrixValues);
                //Log.d(TAG, "onDraw:\nmatrix[0]: " + matrixValues[0] + "\nmatrix[1]: " + matrixValues[1] + "\nmatrix[2]: " + matrixValues[2] + "\nmatrix[3]: " + matrixValues[3] + "\nmatrix[4]: " + matrixValues[4] + "\nmatrix[5]: " + matrixValues[5] + "\nmatrix[6]: " + matrixValues[6] + "\nmatrix[7]: " + matrixValues[7] + "\nmatrix[8]: " + matrixValues[8]);
                checkAndDrawSpikesCoins(canvas);
                Log.d(TAG, "onDraw: carX: " + matrixValues[2] + " carY: " + matrixValues[5]);
                Log.d(TAG, "onDraw: coinX: " + coins.get(0).getLeft());
                currentStepNo++;
                Log.d(TAG, "onDraw: step: " + currentStepNo);
                if(currentStepNo < 120) {
                    invalidate();
                }
                //Log.d(TAG, "onDraw: collected: " + coins.get(0).isCollected());
            } else {
                currentStepNo = 0;
                Log.d(TAG, "onDraw: step: " + currentStepNo);
                drawnPath.reset();
            }
        }

        else if(initialDraw) {
            canvas.drawBitmap(parkSpot, 60, 60, null);
            canvas.drawPath(drawnPath, drawPaint);
            canvas.drawBitmap(playerCar, carStartPosLeft, carStartPosTop, null);
            for(Coin c : coins) {
                canvas.drawBitmap(coinBitmap, c.getLeft(), c.getTop(), null);
            }
            for(Spike s : spikes) {
                canvas.drawBitmap(spikeBitmap, s.getLeft(), s.getTop(), null);
            }
        }
        //Log.d(TAG, "onDraw: initialDraw: " + initialDraw);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                drawnPath.moveTo(pointX, pointY);
                return true;
            case MotionEvent.ACTION_MOVE:
                drawnPath.lineTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                pathFinished = true;
                initialDraw = false;
                break;
            default:
                return false;
        }

        postInvalidate();
        return false;
    }

    public class Coin {
        private float left;
        private float top;
        private boolean collected = false;

        public Coin(float left, float top) {
            this.left = left;
            this.top = top;
        }

        public float getLeft() {
            return left;
        }

        public float getTop() {
            return top;
        }

        public boolean isCollected() {
            return collected;
        }

        public void setCollected(boolean collected) {
            this.collected = collected;
        }
    }

    public class Spike {
        private float left;
        private float top;

        public Spike(float left, float top) {
            this.left = left;
            this.top = top;
        }

        public float getLeft() {
            return left;
        }

        public float getTop() {
            return top;
        }
    }

    public void checkAndDrawSpikesCoins(Canvas canvas) {
        for (Spike s : spikes) {
            if (matrixValues[2] >= s.getLeft() && matrixValues[2] <= s.getLeft() + spikeBitmap.getWidth() && matrixValues[5] >= s.getTop() && matrixValues[5] <= s.getTop() + spikeBitmap.getHeight()) {
                currentStepNo = 120;
            }
            canvas.drawBitmap(spikeBitmap, s.getLeft(), s.getTop(), null);
        }

        for(Coin c : coins) {
            if (matrixValues[2] >= c.getLeft() && matrixValues[2] <= c.getLeft() + coinBitmap.getWidth() && matrixValues[5] >= c.getTop() && matrixValues[5] <= c.getTop() + coinBitmap.getHeight()) {
                c.setCollected(true);
                coinsCollected++;
            }
            if (!c.isCollected()) {
                canvas.drawBitmap(coinBitmap, c.getLeft(), c.getTop(), null);
            }
        }
    }

}
