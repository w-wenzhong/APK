package andhook.test;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


/**
 * ActivityThread의 'mH: Handle`핸들의 mCallback을 후크한다.
 * http://androidxref.com/5.1.1_r6/xref/frameworks/base/core/java/android/os/Handler.java
 * 생성자에서 원래의 콜백을 전달한다.
 */
public class HookCallback implements Handler.Callback {
    private static final String TAG = HookInit.TAG;
    private static final int RESUME_ACTIVITY                = 107;
    private static final int PAUSE_ACTIVITY                 = 101;
    public static final int STOP_ACTIVITY_HIDE              = 104;
    public static final int SHOW_WINDOW                     = 105;
    public static final int DESTROY_ACTIVITY                = 109;
    private static final int BIND_APPLICATION               = 110;
    public static final int RECEIVER                        = 113;
    public static final int CREATE_SERVICE                  = 114;
    public static final int SERVICE_ARGS                    = 115;
    public static final int STOP_SERVICE                    = 116;
    public static final int CLEAN_UP_CONTEXT                = 119;
    public static final int BIND_SERVICE                    = 121;
    private static final int SET_CORE_SETTINGS              = 138;
    public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
    private static final int ENTER_ANIMATION_COMPLETE       = 149;

    private Handler.Callback mParentCallback;
    HookCallback(Handler.Callback parentCallback){
        mParentCallback = parentCallback;
    }
    static boolean load_class = false;
    public static Thread autoThread;
    private boolean isAutoEngineStart;
    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case BIND_APPLICATION:
                Log.d(TAG, "ActivityThread msg is BIND_APPLICATION");
                isAutoEngineStart = false;
                // java hooking....
                break;
            case CREATE_SERVICE:
                Log.d(TAG, "ActivityThread msg is CREATE_SERVICE");
                if(!load_class) {
                    Class<?> clazz = ClasspathScanner.FindClassForName("androidx.slidingpanelayout.widget.SlidingPaneLayout");
                    clazz = ClasspathScanner.FindClassForName("j.a.f0.w0");

                    load_class = true;
                }

                break;
            case RESUME_ACTIVITY:
                Log.d(TAG, "ActivityThread msg is RESUME_ACTIVITY");
                synchronized (AutoEngine.class) {
                    AutoEngine.mPause = false;
                    TimeRange.setPauseTime(false);
                }
                Activity topActivity = GetActivity.getTopActivity();
                Log.d(TAG, "Top Activity is " + topActivity.getClass().getName());
                break;
            case PAUSE_ACTIVITY:
                Log.d(TAG, "ActivityThread msg is PAUSE_ACTIVITY");
                topActivity = GetActivity.getTopActivity();
                //ViewGroup viewGroup = topActivity.getWindow().getDecorView();
                synchronized (AutoEngine.class) {
                    AutoEngine.mPause = true;
                    TimeRange.setPauseTime(true);
                }
                Log.d(TAG, "Top Activity is " + topActivity.getClass().getName());

                break;
            case ENTER_ANIMATION_COMPLETE:
                Log.d(TAG, "ActivityThread msg is ENTER_ANIMATION_COMPLETE");
                topActivity = GetActivity.getRunningActivity();
                Log.d(TAG, "Top Activity is " + topActivity.getClass().getName());
                if(!isAutoEngineStart) {
                    autoThread = new Thread(new AutoEngine());
                    autoThread.start();
                    isAutoEngineStart = true;
                }

                ViewGroup vs =(ViewGroup)topActivity.getWindow().getDecorView();
                View v = AppHooking.findViewContainID("menu_layout_container");
                if(v != null)
                {
                    int[] loc;
                    loc = EventAction.getViewLocation(v);
                    for(int n : loc) {
                        Log.d(TAG,"menu : " + n);
                    }

                }
                break;
            case DESTROY_ACTIVITY:
                Log.d(TAG, "ActivityThread msg is DESTROY_ACTIVITY");
                autoThread.interrupt();
                break;
            default:
                Log.d(TAG, "ActivityThread msg is " + msg.what);
                break;

        }

        if (mParentCallback != null){
            return mParentCallback.handleMessage(msg);
        }else{
            return false;
        }
    }
}
