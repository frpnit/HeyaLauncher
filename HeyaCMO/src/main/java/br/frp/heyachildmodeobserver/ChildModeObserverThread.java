package br.frp.heyachildmodeobserver;

/**
 * Created by Fabrício Ramos on 06/01/2016.
 */

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;


/**
 * Runs in the Background and observes to restrict access for Ouya Home, downloads and etc
 */
public class ChildModeObserverThread extends Thread
{

    /** Current context */
    private Context mcontext;

    /** Boolean indicates if background-service shall run */
    private Boolean mRun = true;

    /** Thread sleep interval */
    long threadInterval = 700;

    /** Handler for UI threads (Needed for Toasts) */
    private Handler h;

    /** Keep track of current App/Game running, if System Menu is opened */
    private String currentLaunchedApp;

    /** View for Overlay that covers you may like section */
    private View curGameView = null;

    /** View to cover launched games/apps from 'You may also like' section until observer reverts back to Heya Launcher */
    private View dimView = null;

    /** Boolean that indicates if observer will do its further actions to prevent launching games/apps from System Menu */
    private boolean SysOverlayWatch = false;

    /** keeps track of launched app from System Menu, because if default routine fails, this one is used to ensure force quit */
    private int launchedfromSysMenuCycle = 0;

    /** Integer to keep track of thread's cycles */
    private int threadCycle = 0;


    /**
     * Create new ChildModeObserverThread
     */
    public ChildModeObserverThread(Context context)
    {
        mcontext = context;

        // Set our priority to minimal
        this.setPriority(Thread.MIN_PRIORITY);
    }

    /** Try to stop thread */
    public void stopThread()
    {
        mRun = false;
    }

    /** Override run-method which is initiated on Thread-Start */
    @Override
    public void run()
    {

        h = new Handler(mcontext.getMainLooper());

        // Start endless-loop to observe running TopActivity
        while(mRun)
        {
            // Check if we shall run again:
            if(mRun)
            {

                try
                {
                    /** Actions to retrieve current Top App and its current Activity */
                    ActivityManager am = (ActivityManager)mcontext.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(6);
                    String currentTopApp=taskInfo.get(0).topActivity.getPackageName();

                    String currentTopAppActivity=taskInfo.get(0).topActivity.getClassName();

                    /** If currentTopApp = tv.ouya.console, that means we are or on System Menu, or any of Ouya Stock System activities */
                    if(currentTopApp.equals("tv.ouya.console")) SysOverlayWatch = true;

                    /** We will enclose observer further actions to prevent launching games/apps from System Menu inside below's IF block.
                     *  The deal is avoiding more processing at each loop,  because every loop is done at less than one second (threadInterval),
                     *  so intrusion while in game is minimal. **/
                    if(SysOverlayWatch){

                        if(currentTopApp.equals("tv.ouya.console")){
                            if(currentTopAppActivity.contains("GuideActivity")) {
                                ToggleCurGameInfoView(true);
                            }else if(!currentTopAppActivity.contains("PairControllersActivity")){
                                launchHomeApp();
                            }
                        }else if(currentTopApp.equals(currentLaunchedApp)||currentTopApp.equals("br.frp.heya")) {
                            ToggleCurGameInfoView(false);
                            ToggleDimMute(false);
                            SysOverlayWatch = false;
                        }else{
                            ToggleDimMute(true);
                            for (int i = 0; i < taskInfo.size(); i++) {
                                if (taskInfo.get(i).topActivity.getPackageName().equals(currentLaunchedApp)){
                                    launchedfromSysMenuCycle++;
                                    if(launchedfromSysMenuCycle<13)break;
                                }
                                if (i == taskInfo.size() - 1) {
                                    launchedfromSysMenuCycle=0;
                                    launchHomeApp();
                                    showToast(mcontext.getResources().getString(R.string.launch_not_allowed));
                                }
                            }

                        }

                    }

                    /** Check if there is some apk downloading and deletes it. Since it's less immediate than launching
                     *  apps/games blocker, we will do it at each 6th cycle. */

                    threadCycle++;

                    if(threadCycle==6) {
                        deleteAllApksfromDir("sdcard");
                        deleteAllApksfromDir("usbdrive");
                        threadCycle=0;
                    }
                    Thread.sleep(threadInterval);

                }

                catch (InterruptedException e){}

            }
        }
    }

    public void setCurrentLaunchedApp(String currentLaunchedApp){
        this.currentLaunchedApp = currentLaunchedApp;
    }

    private void launchHomeApp (){
        currentLaunchedApp = "br.frp.heya";
        PackageManager pm = mcontext.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(currentLaunchedApp);
        mcontext.startActivity(intent);
    }

    private void deleteAllApksfromDir(String directory) {

        File f = new File(directory);
        String filenames="";

        if(f.exists()) {
            File[] files = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".apk"));
                }
            });

            if (files!=null) {
                for (File file : files) {
                    if (!filenames.equals("")) {
                        filenames += "\n";
                    }
                    filenames += file.getName() + ": " + mcontext.getResources().getString(R.string.download_not_allowed);
                    boolean deleted = file.delete();
                    showToast(String.valueOf(deleted));
                }
            }

            if(!filenames.equals("")) showToast(filenames);
        }

    }

    private void showToast (String message){
        final String msg=message;
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mcontext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ToggleCurGameInfoView(Boolean toggleOn){

        final WindowManager wm = (WindowManager) mcontext.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        Boolean createView=null;

        if(toggleOn && curGameView==null) {
            curGameView = View.inflate(mcontext, R.layout.curgameinfolayout, null);
            createView = true;
        }else if(!toggleOn && curGameView!=null) {
            createView = false;
        }

        if(createView!=null) {
            final View thread_curGameView=curGameView;
            final Boolean thread_createView = createView;

            h.post(new Runnable() {
                @Override
                public void run() {
                    if (thread_createView) {
                        wm.addView(thread_curGameView, params);
                        createAppInfoOverlay(thread_curGameView);
                        AnimateAppInfoOverlay(thread_curGameView, R.anim.curgameinfo_in, false);
                    }else{
                        if(thread_curGameView.isShown()) {
                            AnimateAppInfoOverlay(thread_curGameView, R.anim.curgameinfo_out, true);
                        }
                    }
                }
            });
        }

    }

    private void ToggleDimMute(Boolean toggleOn){

        final WindowManager wm = (WindowManager) mcontext.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
        );
        Boolean createView=null;

        if(toggleOn && dimView==null){
            dimView = new View(mcontext);
            createView=true;
        }else if(!toggleOn && dimView!=null){
            createView=false;
        }

        if(createView!=null) {
            final View thread_dimView = dimView;
            final AudioManager audio = (AudioManager) mcontext.getSystemService(Context.AUDIO_SERVICE);
            final Boolean thread_createView = createView;

            h.post(new Runnable() {
                @Override
                public void run() {
                    if (thread_createView) {
                        wm.addView(thread_dimView, params);
                        audio.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    } else {
                        if (thread_dimView.isShown()) wm.removeView(thread_dimView);
                        audio.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    }
                }
            });

            if(!createView) dimView=null;
        }
    }

    private void createAppInfoOverlay(View v){

        final PackageManager pm = mcontext.getPackageManager();
        ApplicationInfo appInfo;

        try {
            appInfo = mcontext.getPackageManager().getApplicationInfo(currentLaunchedApp, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            appInfo = null;
        }

        LinearLayout curAppInfoOverlay = (LinearLayout) v.findViewById(R.id.curgameinfocontent);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) curAppInfoOverlay.getLayoutParams();

        params.topMargin = getPixelFromDip(getBaseDipFromPixel(546));
        params.leftMargin = getPixelFromDip(getBaseDipFromPixel(506));
        params.height = getPixelFromDip(getBaseDipFromPixel(402));

        curAppInfoOverlay.setLayoutParams(params);

        LinearLayout curAppInfoText = (LinearLayout) v.findViewById(R.id.curgameinfoText);
        curAppInfoText.getLayoutParams().height = getPixelFromDip(getBaseDipFromPixel(197));

        Typeface tf = Typeface.createFromAsset(mcontext.getAssets(), "fonts/Montserrat-SemiBold.otf");
        TextView curgameTit = (TextView) curAppInfoOverlay.findViewById(R.id.curgameTit);
        TextView curgameName = (TextView) curAppInfoOverlay.findViewById(R.id.curgameName);
        TextView dgRestriction = (TextView) curAppInfoOverlay.findViewById(R.id.dgRestriction);
        curgameTit.setTypeface(tf);
        curgameName.setTypeface(tf);
        dgRestriction.setTypeface(tf);

        curgameName.setText(getDisplayName(pm, appInfo).toUpperCase());

        ImageView appIcon = (ImageView) curAppInfoOverlay.findViewById(R.id.appIcon);
        appIcon.setImageDrawable(getDisplayIcon(pm, appInfo));
        appIcon.getLayoutParams().width = getPixelFromDip(getBaseDipFromPixel(350));
        appIcon.getLayoutParams().height = getPixelFromDip(getBaseDipFromPixel(197));
    }

    final private void AnimateAppInfoOverlay(View v, int AnimXML, Boolean setListener){

        final WindowManager wm = (WindowManager) mcontext.getSystemService(Context.WINDOW_SERVICE);
        LinearLayout curAppInfoOverlay = (LinearLayout) v.findViewById(R.id.curgameinfocontent);

        Animation animation = AnimationUtils.loadAnimation(mcontext, AnimXML);
        curAppInfoOverlay.startAnimation(animation);

        if(setListener) {
            final View thread_curGameView = v;
            animation.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            wm.removeView(thread_curGameView);
                            curGameView = null;
                        }
                    });
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationStart(Animation animation) {
                }
            });
        }

    }

    private static int getBaseDipFromPixel(int px)
    {
        return (int)(px / 2.0f);
    }

    private static int getPixelFromDip(int dip)
    {
        return (int)(dip * Resources.getSystem().getDisplayMetrics().density);
    }

    private String getDisplayName(PackageManager pm, ApplicationInfo appInfo)
    {
        String retVal = appInfo.loadLabel(pm).toString();
        if(retVal == null || retVal.equals(""))
        {
            retVal = currentLaunchedApp;
        }
        return retVal;
    }

    private Drawable getDisplayIcon(PackageManager pm, ApplicationInfo appInfo)
    {
        Resources resources;
        Drawable IconImage = null;

        try {
            resources = pm.getResourcesForApplication(appInfo);
            int identifier = resources.getIdentifier(currentLaunchedApp + ":drawable/ouya_icon", "", "");

            if (identifier == 0) {
                IconImage = appInfo.loadIcon(pm);
            } else {
                IconImage = pm.getResourcesForApplication(appInfo).getDrawable(identifier);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return IconImage;
    }

}



