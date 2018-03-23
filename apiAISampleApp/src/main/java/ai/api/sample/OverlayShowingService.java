package ai.api.sample;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class OverlayShowingService extends Service implements OnTouchListener {

    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private WindowManager wm;
    private GestureDetector gestureDetector;
    private View customView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        customView = View.inflate(this, R.layout.hike_ai, null);

        GifImageView gifImageView = (GifImageView) customView.findViewById(R.id.gif);
        customView.setOnTouchListener(this);
        try {
            final GifDrawable gifDrawable = new GifDrawable(getAssets(), "hello.gif");
            gifImageView.setBackground(gifDrawable);
            gifImageView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gifDrawable.stop();
                }
            }, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        wm.addView(customView, params);


        gestureDetector = new GestureDetector(this, new MyGestureListener());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (customView != null) {
            wm.removeView(customView);
            customView = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            customView.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];

            System.out.println("topLeftY=" + topLeftLocationOnScreen[1]);
            System.out.println("originalY=" + originalYPos);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (LayoutParams) customView.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 10 && Math.abs(newY - originalYPos) < 10 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            wm.updateViewLayout(customView, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void startAnimationTimer(final View floatingUnitLayout) {

        final WindowManager.LayoutParams myParams = (LayoutParams) customView.getLayoutParams();

        ValueAnimator animator = ValueAnimator.ofInt(myParams.y, 0);
        final float startX = myParams.x;

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                myParams.y = (Integer) animation.getAnimatedValue();
                myParams.x = (int) (startX - (animation.getAnimatedFraction() *
                        startX));
                wm.updateViewLayout(floatingUnitLayout, myParams);
            }
        });
        animator.start();
    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            AIDialogueUtils.openVoiceInputDialog(OverlayShowingService.this);
            startAnimationTimer(customView);
            return true;
        }
    }

}