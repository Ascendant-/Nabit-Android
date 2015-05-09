package io.nabit.nabit;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by robbyrao on 2015-05-08.
 */
public class CameraHandlerThread extends HandlerThread {

    private static CameraHandlerThread cInstance;
    private Handler mHandler;

    static {
        cInstance = new CameraHandlerThread();
    }

    private CameraHandlerThread() {
        super("CameraHandlerThread");
        start();

        //Handler is associated with Looper of new thread.
        //by sending Handler runnable, it is put in queue of
        //new thread to run
        mHandler = new Handler(getLooper());

        Log.d("CHT3", getLooper().getThread().getName());
        Log.d("CHT3_ID", String.valueOf(getLooper().getThread().getId()));
    }

    public static CameraHandlerThread getInstance() {
        return cInstance;
    }
}
