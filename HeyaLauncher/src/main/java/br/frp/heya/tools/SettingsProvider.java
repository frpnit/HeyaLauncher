package br.frp.heya.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import br.frp.heya.R;

/**
 * Settings Provider to store all Settings of the app
 */
public class SettingsProvider
{
    /** Map of possible languages */
    public static final Map<String, String> LANG = new LinkedHashMap<String, String>()
    {{
        // Key is the static field name of Locale (e.g. Locale.GERMAN or Locale.ENGLISH)
        // Value is the displayed value for the settings
        put("", "Auto");
        put("ENGLISH", "English");
        put("pt_BR", "Português Brasileiro");
        //put("GERMAN", "Deutsch");
        //put("ru", "Русский");
        //put("uk", "Українська");
    }};

    /** Private static singleton object */
    private static SettingsProvider _instance;

    /** Semaphore to limit readValues and writeValues access */
    private static Semaphore mSemaphore = new Semaphore(1);

    /** Synchronized singleton getter (Thread Safe) */
    public static synchronized SettingsProvider getInstance (Context context)
    {
        if(SettingsProvider._instance == null)
        {
            SettingsProvider._instance = new SettingsProvider(context);
        }
        return SettingsProvider._instance;
    }

    /** Stored context */
    Context mContext;

    /** Instance of app-shared preferences */
    SharedPreferences mPreferences;

    /** Indicator for package-order */
    final static String PACKAGEORDER = "packageorder_";

    /** Indicates if settings are already loaded */
    Boolean mIsLoaded = false;

    /** Package-Order List */
    List<String> mPackageOrder = new ArrayList<String>();

    /** Child Mode Background observer enabled */
    Boolean mChildModeObserverEnabled = false;

    /** Language string */
    String mLanguage = "";

    /** Startup package name */
    String mStartupPackage;

    /** List of hidden apps */
    Set<String> mHiddenAppsList = new HashSet<String>();

    /** Show system apps */
    Boolean mShowSystemApps = false;

    /** Laincher Name */
    String mLauncherName = "";

    /** Size of the app icons */
    Integer mAppIconSize = 0;

    /** Automatically select first icon when switching to app-drawer */
    Boolean mAutoSelectFirstIcon = true;

    /** Create a the instace of SettingsProvider */
    private SettingsProvider(Context context)
    {
        mContext = context;
        mLauncherName = mContext.getResources().getString(R.string.AppName);
        mStartupPackage = "";
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Set order of packages for app-drawer
     * @param packageOrder List of packagenames in correct order
     */
    public void setPackageOrder(List<String> packageOrder)
    {
        mPackageOrder = packageOrder;
        storeValues();
    }

    /**
     * @return List of packagenames in last saved order
     */
    public List<String> getPackageOrder()
    {
        readValues();
        return mPackageOrder;
    }

    public void setChildModeObserverEnabled(Boolean value)
    {
        mChildModeObserverEnabled = value;
        storeValues();
    }
    public Boolean getChildModeObserverEnabled()
    {
        readValues();
        return mChildModeObserverEnabled;
    }

    public void setAutoSelectFirstIcon(Boolean value)
    {
        mAutoSelectFirstIcon = value;
        storeValues();
    }
    public Boolean getAutoSelectFirstIcon()
    {
        readValues();
        return mAutoSelectFirstIcon;
    }

    public void setLanguage(String language)
    {
        mLanguage = language;
        storeValues();
    }
    public String getLanguage()
    {
        readValues();
        return mLanguage;
    }

    public void setStartupPackage(String startupPackage)
    {
        mStartupPackage = startupPackage;
        storeValues();
    }
    public String getStartupPackage()
    {
        readValues();
        return mStartupPackage;
    }

    public void setHiddenApps(Set<String> hiddenApps)
    {
        mHiddenAppsList = hiddenApps;
        storeValues();
    }
    public Set<String> getHiddenApps()
    {
        readValues();
        return mHiddenAppsList;
    }

    public void setShowSystemApps(Boolean value)
    {
        mShowSystemApps = value;
        storeValues();
    }
    public Boolean getShowSystemApps()
    {
        readValues();
        return mShowSystemApps;
    }

    public void setLauncherName(String launcherName)
    {
        mLauncherName = checkNameString(launcherName);
        storeValues();
    }

    public String getLauncherName()
    {
        readValues();
        return mLauncherName;
    }

    private String checkNameString(String launcherName){
        if(launcherName.isEmpty()){
            launcherName=mContext.getResources().getString(R.string.AppName);
        }else if(launcherName.length()>32) {
            launcherName=launcherName.substring(0,32);
        }
        return launcherName;
    }

    public void setAppIconSize(Integer appIconSize)
    {
        mAppIconSize = appIconSize;
        storeValues();
    }
    public Integer getAppIconSize()
    {
        readValues();
        return mAppIconSize;
    }

    /**
     * Read values from settings
     */
    public void readValues()
    {
        readValues(false);
    }

    /**
     * @param forceRead Force reading values from preferences
     */
    public void readValues(Boolean forceRead)
    {
        try
        {
            // ATTENTION: NEVER CALL ONE OF THE GETTERS OR SETTERS IN HERE!
            // Aquire semaphore
            mSemaphore.acquire();

            // Load only once (hold in singleton)
            if (mIsLoaded && !forceRead)
            {
                mSemaphore.release();
                return;
            }

            // PackageList
            List<String> packageList = new ArrayList<String>();
            Integer size = mPreferences.getInt(PACKAGEORDER + "size", 0);
            for (Integer i = 0; i < size; i++)
            {
                String actKey = PACKAGEORDER + i.toString();
                packageList.add(mPreferences.getString(actKey, null));
            }
            mPackageOrder = packageList;

            // BackgroundObserverEnabled
            mChildModeObserverEnabled = mPreferences.getBoolean("prefChildModeObservationEnabled", mChildModeObserverEnabled);

            // Auto select first icon
            mAutoSelectFirstIcon = mPreferences.getBoolean("prefAutoSelectFirstIcon", mAutoSelectFirstIcon);

            // Startup-package
            mStartupPackage = mPreferences.getString("prefStartupPackage", mStartupPackage);

            // HiddenApps-List
            mHiddenAppsList = mPreferences.getStringSet("prefHiddenApps", mHiddenAppsList);

            // Show sys apps
            mShowSystemApps = mPreferences.getBoolean("prefShowSysApps", mShowSystemApps);

            // lang
            mLanguage = mPreferences.getString("prefLanguage", mLanguage);

            // Launcher name
            mLauncherName = checkNameString(mPreferences.getString("prefLauncherName", mLauncherName));

            // App icon size
            mAppIconSize = Integer.valueOf(mPreferences.getString("prefAppIconSize", mAppIconSize.toString()));

            // Set is loaded flag
            mIsLoaded = true;
        }
        catch(Exception e)
        {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String errorReason = errors.toString();
            Log.d(SettingsProvider.class.getName(), "Exception while reading settings: \n" + errorReason);
        }

        mSemaphore.release();
    }

    /**
     * Store values to settings
     */
    public void storeValues()
    {
        try
        {
            // ATTENTION: NEVER CALL ONE OF THE GETTERS OR SETTERS IN HERE!
            // Aquire semaphore
            mSemaphore.acquire();

            // Get editor, clear editor and save new values
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.clear();


            // PackageList
            editor.putInt(PACKAGEORDER + "size", mPackageOrder.size());
            for (Integer i = 0; i < mPackageOrder.size(); i++)
            {
                String actKey = PACKAGEORDER + i.toString();
                editor.remove(actKey);
                editor.putString(actKey, mPackageOrder.get(i));
            }

            // BackgroundObserverEnabled
            editor.putBoolean("prefChildModeObservationEnabled", mChildModeObserverEnabled);

            // Auto select first icon
            editor.putBoolean("prefAutoSelectFirstIcon", mAutoSelectFirstIcon);

            // Startup package
            editor.putString("prefStartupPackage", mStartupPackage);

            // Hidden apps list
            editor.putStringSet("prefHiddenApps", mHiddenAppsList);

            // Show sys apps
            editor.putBoolean("prefShowSysApps", mShowSystemApps);

            // Lang
            editor.putString("prefLanguage", mLanguage);

            // Launcher Name
            editor.putString("prefLauncherName", mLauncherName);

            // App icon size
            editor.putString("prefAppIconSize", mAppIconSize.toString());

            editor.commit();
        }
        catch(Exception e)
        {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String errorReason = errors.toString();
            Log.d(SettingsProvider.class.getName(), "Exception while reading settings: \n" + errorReason);
        }

        mSemaphore.release();
    }
}
