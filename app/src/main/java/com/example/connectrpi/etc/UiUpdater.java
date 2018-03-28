package com.example.connectrpi.etc;

import android.os.Looper;
import android.os.Handler;

/**
 * A class used to perform periodical updates,
 * specified inside a runnable object. An update interval
 * may be specified (otherwise, the class will perform the
 * update every 2 seconds).
 *
 * @author Carlos Simões
 */
public class UiUpdater {
    // Create a Handler that uses the Main Looper to run in
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable mStatusChecker;
    private final int UPDATE_INTERVAL_IN_SECONDS = 5;

    /**
     * Creates an UiUpdater object, that can be used to
     * perform UiUpdates on a specified time interval.
     *
     * @param runnable A runnable containing the update routine.
     */
    public UiUpdater(final Runnable runnable) {
        mStatusChecker = new Runnable() {
            @Override
            public void run() {
                // Run the passed runnable
                runnable.run();
                // Re-run it after the update interval
                mHandler.postDelayed(this, UPDATE_INTERVAL_IN_SECONDS * 1000);
            }
        };
    }

    /**
     * The same as the default constructor, but specifying the
     * intended update interval.
     *
     * @param uiUpdater A runnable containing the update routine.
     * @param interval  The interval over which the routine
     *                  should run (milliseconds).
     */
    public UiUpdater(Runnable uiUpdater, int interval){
        //UPDATE_INTERVAL_IN_SECONDS = interval;
        this(uiUpdater);
    }

    /**
     * Starts the periodical update routine (mStatusChecker
     * adds the callback to the handler).
     */
    public synchronized void startUpdates(){
        mStatusChecker.run();
    }

    /**
     * Stops the periodical update routine from running,
     * by removing the callback.
     */
    public synchronized void stopUpdates(){
        mHandler.removeCallbacks(mStatusChecker);
    }
}