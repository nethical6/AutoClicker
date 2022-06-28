package in.nethical.clikit.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

public class TapAccessibilityService extends AccessibilityService {

    private Handler handler;
    private int x;
    private int y;
    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("tap-handler");
        handlerThread.start();
        handler= new Handler(handlerThread.getLooper());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            String action = intent.getStringExtra("action");
            if (action.equals("run")){
                x = intent.getIntExtra("x", 0);
                y = intent.getIntExtra("y",0);
                if(myRunnable == null){
                    myRunnable = new myRunnable();
                }
                handler.post(myRunnable);
            }else{
                handler.removeCallbacksAndMessages(null);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {

    }
    private void tap(int x, int y){
        Path swipePath = new Path();
        swipePath.moveTo(x,y);
        swipePath.lineTo(x,y);
        GestureDescription.Builder gBuilder = new GestureDescription.Builder();
        gBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath,0,1));
        dispatchGesture(gBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                handler.post(myRunnable);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }
    private myRunnable myRunnable;
    private class myRunnable implements Runnable{

        @Override
        public void run() {
            tap(x,y);
        }
    }
}
