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
    private int currentStepNo;
    private PathMeasure pathMeasure;
    private Matrix matrix;
    private ArrayList<Coin> coins;
    private ArrayList<Spike> spikes;
    private int coinsCollected;
    private float pathStartX, pathStartY;
    private float pathEndX, pathEndY;

    private float[] matrixValues;
    private float[] carPosition;

    private GameViewInterface gameViewInterface;

    private boolean carMoving;
    private boolean finishedDrawingPath;
    private boolean initialDraw;
    private boolean currentDraw;
    private boolean coinsAndSpikeObjectsCreated;

    public GameView(Context context, GameViewInterface listener) {
        super(context);
        this.gameViewInterface = listener;
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
        //coins.add(new Coin(400, 400));
        spikes = new ArrayList<>();
        //spikes.add(new Spike(600, 400));

        matrixValues = new float[9];

        carPosition = new float[2];

        coinsCollected = 0;

        currentStepNo = 0;

        carMoving = false;
        initialDraw = true;
        currentDraw = false;
        finishedDrawingPath = false;
        coinsAndSpikeObjectsCreated = false;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundResource(R.drawable.road);
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        Log.d(TAG, "onDraw: entered");
        Log.d(TAG, "onDraw: finishedDrawingPath: " + finishedDrawingPath);
        if(finishedDrawingPath) {

            if (currentStepNo < 120 && !currentDraw) {

                Log.d(TAG, "onDraw: finished path and step < 120");
                
                matrix = new Matrix();
                pathMeasure = new PathMeasure(drawnPath, false);
                float segmentLength = pathMeasure.getLength() / 120;

                carMoving = true;
                Log.d(TAG, "onDraw: step: " + currentStepNo);

                pathMeasure.getMatrix(segmentLength * currentStepNo, matrix, pathMeasure.POSITION_MATRIX_FLAG + pathMeasure.TANGENT_MATRIX_FLAG);
                canvas.drawBitmap(parkSpot, 60, 60, null);
                canvas.drawPath(drawnPath, drawPaint);
                canvas.drawBitmap(playerCarMoving, matrix, null);
                matrix.getValues(matrixValues);

                checkAndDrawSpikesCoins(canvas);
                checkFinish();

                currentStepNo++;

                invalidate();

                if(currentStepNo == 120) {
                    currentDraw = true;
                }

            }
            else if(currentDraw) {
                currentSetUp(canvas);
                Log.d(TAG, "onDraw: finished path and step !< 120");
                currentStepNo = 0;
                drawnPath.reset();
                finishedDrawingPath = false;
                carMoving = false;
            }
        }

        else if(initialDraw) {
            initialSetUp(canvas);
            Log.d(TAG, "onDraw: initialdraw");
        }

        else {
            currentSetUp(canvas);
        }

    }

    public void initialSetUp(Canvas canvas) {

        if(!coinsAndSpikeObjectsCreated) {
            coins.add(new Coin((float) (canvasWidth * 0.1), (float) (canvasHeight / 2)));
            coins.add(new Coin((float) (canvasWidth / 2), (float) (canvasHeight * 0.85)));
            coins.add(new Coin((float) (canvasWidth * 0.75), (float) (canvasHeight * 0.2)));

            spikes.add(new Spike((float) (canvasWidth * 0.35), (float) (canvasHeight * 0.7)));
            spikes.add(new Spike((float) (canvasWidth * 0.55), (float) (canvasHeight * 0.3)));
            spikes.add(new Spike((float) (canvasWidth * 0.65), (float) (canvasHeight * 0.45)));

            coinsAndSpikeObjectsCreated = true;
        }

        int carHeight = playerCar.getHeight();
        int carWidth = playerCar.getWidth();

        float carStartPosLeft = (float) (canvasWidth - carWidth - 60);
        float carStartPosTop = (float) (canvasHeight - carHeight - 60);

        canvas.drawBitmap(parkSpot, 60, 60, null);
        canvas.drawPath(drawnPath, drawPaint);
        canvas.drawBitmap(playerCar, carStartPosLeft, carStartPosTop, null);

        for(Coin c : coins) {
            canvas.drawBitmap(coinBitmap, c.getLeft(), c.getTop(), null);
        }
        for(Spike s : spikes) {
            canvas.drawBitmap(spikeBitmap, s.getLeft(), s.getTop(), null);
        }

        carPosition[0] = carStartPosLeft;
        carPosition[1] = carStartPosTop;

        gameViewInterface.getCoins(coinsCollected);

    }

    public void currentSetUp(Canvas canvas) {
        matrix.getValues(matrixValues);
        //Log.d(TAG, "onDraw: mat x: " + matrixValues[2] + " maty: " + matrixValues[5]);
        canvas.drawBitmap(parkSpot, 60, 60, null);
        canvas.drawPath(drawnPath, drawPaint);
        canvas.drawBitmap(playerCarMoving, matrix, null);
        checkAndDrawSpikesCoins(canvas);

        carPosition[0] = matrixValues[2];
        carPosition[1] = matrixValues[5];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();

        //pathStartX;

        Log.d(TAG, "onTouchEvent: carmoving: " + carMoving);
        Log.d(TAG, "onTouchEvent: stepcount: "+ currentStepNo );

        if(!carMoving) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawnPath.moveTo(pointX, pointY);
                    pathStartX = pointX;
                    pathStartY = pointY;
                    //Log.d(TAG, "onTouchEvent: action down");
                    return true;
                case MotionEvent.ACTION_MOVE:
                    drawnPath.lineTo(pointX, pointY);
                    //Log.d(TAG, "onTouchEvent: actionmove");
                    break;
                case MotionEvent.ACTION_UP:
                    pathEndX = pointX;
                    pathEndY = pointY;
                    Log.d(TAG, "onTouchEvent: endx: " + pathEndX + " endy: " + pathEndY);
                    checkPathStartAndEnd();
                    //Log.d(TAG, "onTouchEvent: actionup");
                    break;
                default:
                    return false;
            }
        }

        postInvalidate();
        return false;
    }

    public void checkPathStartAndEnd() {
        if(!(pathStartX >= carPosition[0] - playerCarMoving.getWidth() && pathStartX <= carPosition[0] + playerCarMoving.getWidth() && pathStartY >= carPosition[1] - playerCarMoving.getWidth() && pathStartY <= carPosition[1] + playerCarMoving.getWidth() && pathEndX >= 60 && pathEndX <= (60 + parkSpot.getWidth()) && pathEndY >= 60 && pathEndY <= (60 + parkSpot.getHeight()))) {
                drawnPath.reset();
                Log.d(TAG, "checkPathStart: path reset");
                currentDraw = true;
        }
        else {
            Log.d(TAG, "checkPathStart: else cond");
            finishedDrawingPath = true;
            initialDraw = false;
            currentDraw = false;
        }
    }

    public class Coin {
        private float left;
        private float top;

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
        float carLeft = matrixValues[2];
        float carTop = matrixValues[5];

        for (Spike s : spikes) {
            if (carLeft >= s.getLeft() && carLeft <= s.getLeft() + spikeBitmap.getWidth() && carTop >= s.getTop() && carTop <= s.getTop() + spikeBitmap.getHeight()) {
                gameViewInterface.gameLost();
                currentDraw = true;
            }
            canvas.drawBitmap(spikeBitmap, s.getLeft(), s.getTop(), null);
        }

        for (Coin c : coins) {
            boolean collected = false;
                Log.d(TAG, "checkAndDrawSpikesCoins: coin collected is false");
                if (carLeft >= c.getLeft() && carLeft <= c.getLeft() + coinBitmap.getWidth() && carTop >= c.getTop() && carTop <= c.getTop() + coinBitmap.getHeight()) {
                    coins.remove(c);
                    collected = true;
                    Log.d(TAG, "checkAndDrawSpikesCoins: coin had been collected");
                    coinsCollected++;
                    gameViewInterface.getCoins(coinsCollected);
                    break;
                }
        }
        Log.d(TAG, "checkAndDrawSpikesCoins: size: " + coins.size());
        for(Coin c : coins) {
                canvas.drawBitmap(coinBitmap, c.getLeft(), c.getTop(), null);
                Log.d(TAG, "checkAndDrawSpikesCoins: coin drawn");
        }
    }

    public void checkFinish() {
        float carLeft = matrixValues[2];
        float carTop = matrixValues[5];

        if(carLeft >= 60 && carLeft + playerCarMoving.getWidth()/2 <= 60 + parkSpot.getWidth() && carTop >= 60 && carTop + playerCarMoving.getHeight()/2 <= 60 + parkSpot.getHeight()) {
            currentDraw = true;
            gameViewInterface.gameWon();
        }
    }

    public interface GameViewInterface {
        void getCoins(int c);
        void gameWon();
        void gameLost();
    }

    public void resetGame() {
        initializations();
        postInvalidate();
        gameViewInterface.getCoins(coinsCollected);
    }

}
