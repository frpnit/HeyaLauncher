package br.frp.heya.gui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import br.frp.heya.R;
import br.frp.heya.observer.StartUpControllerObserver;
import br.frp.heya.tools.SettingsProvider;
import br.frp.heya.tools.Tools;


public class MainActivity extends Activity
{
    private LinearLayout mMainLayout;
    private ListView mListView;
    private Fragment mLastSetFragment;
    private SettingsProvider mSettings;

    private TextView mTextViewClock;
    private TextView mTextViewDate;

    private Timer mTimer = null;

    private boolean mOnResumeDirectlyAfterOnCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Get settings provider
        mSettings = SettingsProvider.getInstance(this);

        // Check language
        Log.d(MainActivity.class.getName(), "Set locale in onCreate");
        setLocale();

        // Set flag indicating we are in oncreate
        mOnResumeDirectlyAfterOnCreate = true;

        // Now go on
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);

        // Get base linear layout
        mMainLayout = (LinearLayout)findViewById(R.id.linearLayoutMain);

        //Retrieve Launcher name
        TextView textlogo = (TextView) findViewById(R.id.textLogo);
        textlogo.setText(mSettings.getLauncherName().toUpperCase());

        // Check if background image have to be set
        WallpaperSelectDialog selectDialog = new WallpaperSelectDialog(this);
        selectDialog.setDefaultWallpaper();
        selectDialog.setWallpaper(false);

        // Get clock and date
        mTextViewClock = (TextView)findViewById(R.id.textViewClock);
        mTextViewDate = (TextView)findViewById(R.id.textViewDate);

        // Get ListView
        mListView = (ListView)findViewById(R.id.listView);

        mListView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                for (int i = 0; i < mListView.getChildCount(); i++) {
                    if (mListView.getChildAt(i).isActivated()) {
                        if (hasFocus && mListView.getSelectedItemPosition() != i) {
                            mListView.getChildAt(i).setEnabled(false);
                        } else {
                            mListView.getChildAt(i).setEnabled(true);
                        }
                    }
                }
            }
        });

        // Handle item click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(MainActivity.class.getName(), "OnItemClickListener: clicked position " + position);
                view.setEnabled(true);
                handleLeftBarItemSelection(parent, view, position, id);
            }
        });

        // Handle item selected changes
        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(MainActivity.class.getName(), "OnItemSelectedListener: selected position " + position);
                for (int i = 0; i < mListView.getChildCount(); i++) {
                    mListView.getChildAt(i).setEnabled(false);
                }
                if (mListView.getChildAt(position).isActivated())
                    mListView.getChildAt(position).setEnabled(true);
                //handleLeftBarItemSelection(parent, view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set adapter
        final LeftBarItemsListAdapter actAdapter = new LeftBarItemsListAdapter(this);
        mListView.setAdapter(actAdapter);

        // Focus first item
        mListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    // Remove listener
                    mListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // Check if first icon have to be selected
                    selectFirstLeftBarItem();
                } catch (Exception e) {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    String errorReason = errors.toString();
                    Log.d(MainActivity.class.getName(), "Failed to focus first left bar list item: \n" + errorReason);
                }
            }
        });

        // Initialize app font
        setFonts();

        CustomFonts.setCustomFont(0, this, mMainLayout, false);
        CustomFonts.setCustomFont(2, this, (TextView) findViewById(R.id.textLogo), false);

        // Starts Controller Pairing Observer
        startControllerObserver();
    }

    @Override
    public void onResume()
    {
        // If not onResume directly after onCreate reset locale
        if(!mOnResumeDirectlyAfterOnCreate)
        {
            Log.d(MainActivity.class.getName(), "Set locale again in onResume");
            setLocale();
        }

        // Set date and time and start timer
        setDateAndTime();
        startTimer();

        // Check if Child Mode is installed and turned on, so observer service will be started (watches to restrict access for Ouya Home, downloads and etc).
        if(!Tools.isPackageInstalled(this, "br.frp.heyachildmodeobserver")) {
            mSettings.setChildModeObserverEnabled(false);
        }
        if(mSettings.getChildModeObserverEnabled()) {
            Intent intent = new Intent("br.frp.heyachildmodeobserver.ChildModeService");
            String lang = mSettings.getLanguage();
            intent.setAction("br.frp.heyachildmodeobserver.action.startforeground");
            intent.putExtra("language", lang);
            intent.putExtra("currentLaunchedApp", "br.frp.heya");

            startService(intent);
        }

        // Check if App Settings Overlay is disabled
        ImageView bt_icon  = (ImageView)findViewById(R.id.optionsButtonIcon);
        TextView bt_description = (TextView)findViewById(R.id.optionsButtonDescription);
        if(mSettings.getChildModeObserverEnabled()) {
            bt_icon.setVisibility(View.GONE);
            bt_description.setVisibility(View.GONE);
        }

        Log.d(MainActivity.class.getName(), "onResume");
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(MainActivity.class.getName(), "onPause");

        // Stop update timer
        stopTimer();

        // Set back onResume directly after onCreate
        mOnResumeDirectlyAfterOnCreate = false;
    }

    private void setActiveFragment(Fragment fragment)
    {
        try
        {
            mLastSetFragment = fragment;

            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.item_detail_container, fragment);
            fragmentTransaction.commit();
        }
        catch (Exception e)
        {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String errorReason = errors.toString();
            Log.d(MainActivity.class.getName(), "Set Active Fragment: Exception: \n" + errorReason);
        }
    }

    private void selectFirstLeftBarItem(){
        mListView.requestFocusFromTouch();
        mListView.setSelection(0);
        mListView.setItemChecked(0, true);
        handleLeftBarItemSelection(mListView, mListView, 0, 0);
    }

    /**
     * Handles selection or click of the left-bar items..
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    private void handleLeftBarItemSelection(AdapterView<?> parent, View view, int position, long id)
    {
        // Get instance of selected item and set as current fragment
        try {
            Log.d(MainActivity.class.getName(), "HandleLeftBarItemSelection: selected position " + position);
            Fragment fragment = (Fragment)Class.forName(((LeftBarItemsListAdapter)parent.getAdapter()).getItem(position).className).getConstructor().newInstance();
            ImageView bt_icon  = (ImageView)findViewById(R.id.optionsButtonIcon);
            TextView bt_description = (TextView)findViewById(R.id.optionsButtonDescription);
            if(position==0 && !mSettings.getChildModeObserverEnabled()){
                bt_icon.setVisibility(View.VISIBLE);
                bt_description.setVisibility(View.VISIBLE);
            }else{
                bt_icon.setVisibility(View.GONE);
                bt_description.setVisibility(View.GONE);
            }
            setActiveFragment(fragment);
        }
        catch (Exception e)
        {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String errorReason = errors.toString();
            Log.d(MainActivity.class.getName(), "HandleLeftBarItemSelection: Exception: \n" + errorReason);
        }

    }

    private void setLocale()
    {
        // Check language
        String lang = mSettings.getLanguage();
        if(lang != null && !lang.equals("") && SettingsProvider.LANG.containsKey(lang))
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

                Configuration config = new Configuration(getResources().getConfiguration());
                config.locale = newLocale;
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            }
            catch (Exception e)
            {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                String errorReason = errors.toString();
                Log.d(MainActivity.class.getName(), "Failed to load custom language setting: \n" + errorReason);
            }
        }
    }

    /**
     * Start the timer to update clock
     */
    private void startTimer() {
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                setDateAndTime();
            }
        };

        // Schedule task every full minute
        Integer everyXminute = 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Integer toBeAdded = everyXminute - (calendar.get(Calendar.MINUTE) % everyXminute);
        if(toBeAdded == 0)
        {
            toBeAdded = everyXminute;
        }
        calendar.add(Calendar.MINUTE, toBeAdded);

        mTimer.schedule(timerTask, calendar.getTime(), 1000*60*everyXminute);
        Log.d(MainActivity.class.getName(), "Update Time started");
    }

    /**
     * Stops the timer to update clock
     */
    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        Log.d(MainActivity.class.getName(), "Update Time stopped");
    }

    /**
     * Sets the current time and date
     */
    private void setDateAndTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Get current date time
                Date actDateTime = new Date();

                mTextViewClock.setText(DateUtils.formatDateTime(MainActivity.this, actDateTime.getTime(), DateUtils.FORMAT_SHOW_TIME));
                // Set date
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, getResources().getConfiguration().locale);
                mTextViewDate.setText(dateFormat.format(actDateTime));
            }
        });
    }

    private void setFonts () {
        CustomFonts.setFontsToUse(0, "fonts/Montserrat-Regular.otf", "fonts/Montserrat-SemiBold.otf", null, null); //general font
        CustomFonts.setFontsToUse(1, "fonts/Montserrat-Bold.otf", "fonts/Montserrat-Black.otf", null, null); //general bolder font - titles
        CustomFonts.setFontsToUse(2, "fonts/Monoglyceride.ttf", null, null, null);  //logo font
        CustomFonts.setFontsToUse(3, "fonts/LeagueSpartan-Bold.ttf", null, null, null); //lateral menu font
    }

    /**
     * @param drawable Set this drawable as background image
     */
    public void setBackgroundImage(Drawable drawable)
    {
        mMainLayout.setBackground(drawable);
    }

    public Integer getBackgroundWidth()
    {
        return mMainLayout.getWidth();
    }

    public Integer getBackgroundHeight()
    {
        return mMainLayout.getHeight();
    }

    /**
     * Check for window focus, useful for refreshing games list after usb was mounted
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(mListView.getCheckedItemPosition()==0) {
                selectFirstLeftBarItem();
            }
        }
    }

    public void startControllerObserver() {
        StartUpControllerObserver startUpControllerObserver=new StartUpControllerObserver();
        startUpControllerObserver.start(this);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e)
    {
        // Check if there is a receiver fragment
        if(mLastSetFragment != null && mLastSetFragment instanceof CustomFragment)
        {
            Boolean retVal = ((CustomFragment) mLastSetFragment).onKeyDown(keycode, e);
            if(retVal)
            {
                return true;
            }
        }

        // Overrides Menu key so activity won't "lost focus" anymore
        if(keycode == KeyEvent.KEYCODE_MENU) return true;

        return super.onKeyDown(keycode, e);
    }

    @Override
    public void onBackPressed()
    {
        boolean isHandled = false;

        // Check if there is a receiver fragment
        if(mLastSetFragment != null && mLastSetFragment instanceof CustomFragment)
        {
            isHandled = ((CustomFragment) mLastSetFragment).onBackPressed();
        }

        // Check if back-pressed is already handled
        if(!isHandled)
        {
            // If the fragment does not handle the back-button and Heya is not
            // on the main-app-view, open main-app-view
            if(!(mLastSetFragment instanceof AppActivity))
            {
                mListView.setSelection(0);
                mListView.setItemChecked(0, true);
                handleLeftBarItemSelection(mListView, mListView, 0, 0);
            }
        }

        // Prevent default by not calling super class..
    }

}
