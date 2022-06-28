package in.nethical.clikit.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import in.nethical.clikit.R;

public class FloatingViewService extends Service {
    private WindowManager mWindowManager;
    private View myFloatingView;

    private WindowManager targetWindowManager;
    private View targetView;
    int[] location = new int[2];
    ImageView TargetImage;
    Boolean isTargetSelected = false;
    Boolean isRunning = false;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();

        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);
        int layout_params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            layout_params = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        }
        else {
            layout_params = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_params,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);

        createDraggableWindow(R.id.floating_root,mWindowManager,params,myFloatingView);

        Button create_target = myFloatingView.findViewById(R.id.floating_new_target);
        create_target.setOnClickListener(view -> {
            if(isTargetSelected){  //target has been been selected
                create_target.setText(R.string.string_change);
                TargetImage.getLocationOnScreen(location);
                if (targetView != null) targetWindowManager.removeView(targetView);
                Toast.makeText(FloatingViewService.this, "Target Set", Toast.LENGTH_SHORT).show();
                isTargetSelected = false;
            }else{
                targetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.floating_target, null);
                //getting windows services and adding the floating view to it
                targetWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                targetWindowManager.addView(targetView, params);
                TargetImage = targetView.findViewById(R.id.targetimg);
                TargetImage.setImageResource(R.drawable.ic_baseline_my_location_24);
                createDraggableWindow(R.id.target_root,targetWindowManager, params,targetView);
                isTargetSelected =true;
                create_target.setText(R.string.string_done);
            }

        });


        Button run = myFloatingView.findViewById(R.id.floating_run);
        run.setOnClickListener(view -> {
            if (location!=null&&TargetImage!=null){
                Intent service = new Intent(FloatingViewService.this,TapAccessibilityService.class);
                if (isRunning){
                    service.putExtra("action","stop");
                    isRunning = false;
                    run.setText(R.string.string_start);
                } else {
                    service.putExtra("action","run");

                    //obtain the centre of the floating view
                    int midTarget = (int) (25 * getResources().getDisplayMetrics().density); //as the image is 50dp , convert 25dp into pixels
                    //add 25dp to both x and y
                    service.putExtra("x",location[0]+midTarget);
                    service.putExtra("y",location[1]+midTarget);

                    isRunning = true;
                    run.setText(R.string.string_stop);
                }
                startService(service);
            }else{
                Toast.makeText(FloatingViewService.this, "Please Add a Target First.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myFloatingView != null) mWindowManager.removeView(myFloatingView);
    }
    
    private void createDraggableWindow(int id,WindowManager uWindowManager,WindowManager.LayoutParams params,View viewG){
        viewG.findViewById(id).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        uWindowManager.updateViewLayout(viewG, params);
                        return true;
                }
                return false;
            }
        });

    }

}
