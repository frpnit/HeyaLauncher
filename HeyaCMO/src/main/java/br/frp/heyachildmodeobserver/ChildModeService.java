package br.frp.heyachildmodeobserver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class ChildModeService extends Service
{
    /** Start foreground action */
    public static final String FOREGROUNDSERVICE_START = "br.frp.heyachildmodeobserver.action.startforeground";

    /** Stop forground action */
    public static final String FOREGROUNDSERVICE_STOP = "br.frp.heyachildmodeobserver.action.stopforeground";

    /** ID of the foreground service */
    public static final int FOREGROUNDSERVICE_ID = 101;

    /** Request code of the notification */
    public static final int REQUEST_CODE = 1;

    /** Indicates if the service is currently running as foreground service */
    private Boolean mIsForeGroundRunning = false;

    /** Simple binder to interact with the service */
    private final IBinder mBinder = new TestRunnerLocalBinder();

    /** BackgroundObserver to observe Child Mode restrictions */
    private ChildModeObserverThread mChildModeObserverThread = null;

    private NotificationCompat.Builder builder;
     /**
     * @return Binder object
     */
    @Override
    public IBinder onBind(Intent arg0)
    {
        Log("onBind");
        return mBinder;
    }

    /**
     * Handle start commands (start-stop foreground)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log("onStartCommand: Received start id " + startId + ": " + intent);

        if (FOREGROUNDSERVICE_START.equals(intent.getAction()))
        {
            Bundle bundle = intent.getExtras();
            foregroundServiceStart(bundle);
        }
        else if (FOREGROUNDSERVICE_STOP.equals(intent.getAction()))
        {
            foregroundServiceStop();
        }

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        Log("onDestroy");
        super.onDestroy();
    }

    /** Start the foreground service */
    private void foregroundServiceStart(Bundle bundle)
    {

        setLocale(bundle.getString("language"));
        String currentLaunchedApp=bundle.getString("currentLaunchedApp");

        if(mIsForeGroundRunning) {
            Log("Foreground Service already running.");
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(FOREGROUNDSERVICE_ID, getCompatNotification());
            mChildModeObserverThread.setCurrentLaunchedApp(currentLaunchedApp);
            return;
        }

        Log("Start Foreground Service");
        startForeground(FOREGROUNDSERVICE_ID, getCompatNotification());

        // Now start the Thread
        runnerThreadStart(currentLaunchedApp);

        // Set the variable
        mIsForeGroundRunning = true;
    }

    /** Stop the foreground service */
    private void foregroundServiceStop()
    {
        if(!mIsForeGroundRunning)
        {
            Log("No Foreground Service running to stop.");
            return;
        }

        // First try to stop the runner thread
        runnerThreadStop();

        // Now stop the service
        Log("Stop Foreground Service");
        stopForeground(true);
        mIsForeGroundRunning = false;
    }

    /** Start the test runner */
    private void runnerThreadStart(String currentLaunchedApp)
    {
        runnerThreadStop();

        Log("Start background thread.");
        mChildModeObserverThread = new ChildModeObserverThread(this);
        mChildModeObserverThread.start();
        mChildModeObserverThread.setCurrentLaunchedApp(currentLaunchedApp);
    }

    /** Stops the test runner */
    private void runnerThreadStop()
    {
        if(mChildModeObserverThread != null && mChildModeObserverThread.isAlive())
        {
            Log("Shut down background thread.");
            mChildModeObserverThread.stopThread();
            try
            {
                mChildModeObserverThread.join(2000);
            }
            catch(Exception e)
            {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                String errorReason = errors.toString();
                Log("Failed to stop thread: \n" + errorReason);
            }

            if(mChildModeObserverThread != null && mChildModeObserverThread.isAlive())
            {
                Log("Force shut down background thread.");
                mChildModeObserverThread.interrupt();
                try
                {
                    mChildModeObserverThread.join();
                }
                catch (Exception e)
                {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    String errorReason = errors.toString();
                    Log("Failed to stop thread: \n" + errorReason);
                }
            }
        }
        else
        {
            Log("No background thread running..");
        }
        mChildModeObserverThread = null;
    }

    /**
     * @return Indicates if the foreground service is running
     */
    public Boolean getIsForeGroundRunning()
    {
        return mIsForeGroundRunning;
    }

    /**
     * @return Notification object which is shown while foreground service is running
     */
    private Notification getCompatNotification()
    {
        if(builder==null) {
            builder = new NotificationCompat.Builder(this);
            Intent startIntent = new Intent(getApplicationContext(), ChildModeActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, REQUEST_CODE, startIntent, 0);
            builder.setSmallIcon(R.mipmap.ic_launcher)
                   .setContentIntent(contentIntent)
                    .setWhen(System.currentTimeMillis());
        }

        Resources res = getResources();
        builder.setContentTitle(res.getString(R.string.notification_title, res.getString(R.string.app_name)))
                .setTicker(getString(R.string.notification_description));

        Notification notification = builder.build();

        return notification;
    }



    /**
     * Log messages to logcat
     * @param message message to log
     */
    private void Log(String message)
    {
        Log.d(ChildModeService.class.getName(), message);
    }

    /**
     * Simple binder class to get current TestRunnerService
     */
    public class TestRunnerLocalBinder extends Binder
    {
        public ChildModeService getService()
        {
            return ChildModeService.this;
        }
    }

    public void setLocale(String lang) {
        // Check language
        if(lang != null && !lang.equals(""))
        {
            try
            {
                Locale newLocale;

                // If lang has two parameters, it's language + region code, if is <= 3 chars, it's a language code, else, it's a constant locale code
                if(lang.split("_").length == 2)
                {
                    newLocale = new Locale(lang.split("_")[0],lang.split("_")[1]);
                }
                else if(lang.length() <= 3)
                {
                    newLocale = new Locale(lang);
                }
                else
                {
                    newLocale = (Locale) Locale.class.getField(lang).get(Locale.getDefault());
                }

                Locale.setDefault(newLocale);

                Configuration config = new Configuration(getBaseContext().getResources().getConfiguration());
                config.locale = newLocale;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            }
            catch (Exception e)
            {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                String errorReason = errors.toString();
                Log.d(ChildModeObserverThread.class.getName(), "Failed to load custom language setting: \n" + errorReason);
            }
        }
    }
}
