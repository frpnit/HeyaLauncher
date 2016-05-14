package br.frp.heya.gui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.frp.heya.R;
import br.frp.heya.tools.AppInfo;
import br.frp.heya.tools.SettingsProvider;
import br.frp.heya.tools.Tools;

/**
 * Preferences activity
 */
public class PreferenceActivity extends PreferenceFragment
{
    SettingsProvider mSettings = SettingsProvider.getInstance(this.getActivity());

    public PreferenceActivity()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencesactivity);

        InstalledAppsAdapter actAppsAdapter = new InstalledAppsAdapter(getActivity(), false);
        List<AppInfo> actApps = actAppsAdapter.getAppList();

        CharSequence[] entries = new CharSequence[actApps.size() + 1];
        CharSequence[] entryValues = new CharSequence[actApps.size() + 1];

        entries[0] = " - No Action - ";
        entryValues[0] = "";

        for (Integer i = 1; i < actApps.size() + 1; i++)
        {
            AppInfo actApp = actApps.get(i - 1);
            entries[i] = actApp.getDisplayName();
            entryValues[i] = actApp.packageName;
        }

        ListPreference startUpPackage = (ListPreference) findPreference("prefStartupPackage");
        startUpPackage.setEntries(entries);
        startUpPackage.setEntryValues(entryValues);
        startUpPackage.setDefaultValue(mSettings.getStartupPackage());
        startUpPackage.setValue(mSettings.getStartupPackage());


        CharSequence[] langEntries = new CharSequence[SettingsProvider.LANG.size()];
        CharSequence[] langValues = new CharSequence[SettingsProvider.LANG.size()];
        Integer counter = 0;
        for (Map.Entry<String, String> entry : SettingsProvider.LANG.entrySet())
        {
            langEntries[counter] = entry.getValue();
            langValues[counter] = entry.getKey();
            counter++;
        }
        ListPreference languagePreference = (ListPreference) findPreference("prefLanguage");
        languagePreference.setEntries(langEntries);
        languagePreference.setEntryValues(langValues);
        languagePreference.setDefaultValue(mSettings.getLanguage());
        languagePreference.setValue(mSettings.getLanguage());
        languagePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                // Check if value has really changed:
                if (!mSettings.getLanguage().equals(newValue.toString()))
                {
                    // Force reload settings
                    mSettings.setLanguage(newValue.toString());

                    // Restart whole app
                    Tools.doRestart(PreferenceActivity.this.getActivity());
                }
                return true;
            }
        });


        InstalledAppsAdapter actHiddenAppsAdapter = new InstalledAppsAdapter(getActivity(), true);
        List<AppInfo> actHiddenApps = actHiddenAppsAdapter.getAppList();
        PackageManager pm = getActivity().getPackageManager();

        List<String> hiddenEntriesList = new ArrayList<String>();
        List<String> hiddenEntryValuesList = new ArrayList<String>();

        for (Integer i = 0; i < actHiddenApps.size(); i++)
        {
            AppInfo actApp = actHiddenApps.get(i);
            try {
                ApplicationInfo ai = pm.getApplicationInfo(actApp.packageName, PackageManager.GET_META_DATA);
                if(actApp.packageName.contains("com.android.vending")||((actApp.flags & ai.FLAG_UPDATED_SYSTEM_APP) != 1 && (actApp.flags & ai.FLAG_SYSTEM) != 1)){
                    hiddenEntriesList.add(actApp.getDisplayName());
                    hiddenEntryValuesList.add(actApp.packageName);

                }
            }catch(Exception e){
                hiddenEntriesList.add(actApp.getDisplayName());
                hiddenEntryValuesList.add(actApp.packageName);
            }
        }
        final CharSequence[] hiddenEntries = hiddenEntriesList.toArray(new CharSequence[hiddenEntriesList.size()]);
        final CharSequence[] hiddenEntryValues = hiddenEntryValuesList.toArray(new CharSequence[hiddenEntriesList.size()]);

        MultiSelectListPreference hiddenAppsList = (MultiSelectListPreference) findPreference("prefHiddenApps");
        hiddenAppsList.setEntries(hiddenEntries);
        hiddenAppsList.setEntryValues(hiddenEntryValues);
        hiddenAppsList.setDefaultValue(mSettings.getHiddenApps());

        EditTextPreference prefLauncherName = (EditTextPreference) findPreference("prefLauncherName");
        prefLauncherName.setDefaultValue(mSettings.getLauncherName());
        prefLauncherName.setText(mSettings.getLauncherName());
        prefLauncherName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Check if value has really changed:
                if (!mSettings.getLauncherName().equals(newValue.toString())) {

                    // Force reload settings
                    mSettings.setLauncherName(newValue.toString());

                    // Restart whole app
                    Tools.doRestart(PreferenceActivity.this.getActivity());
                }
                return true;
            }
        });

        CustomSeekbarPref appIconSize = (CustomSeekbarPref) findPreference("prefAppIconSize");
        appIconSize.setDefaultValue(mSettings.getAppIconSize().toString());
        appIconSize.setSliderValues(mSettings.getAppIconSize(), 0, 200, 1);
        appIconSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });
        appIconSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mSettings.setAppIconSize(Integer.valueOf(newValue.toString()));
                return true;
            }
        });


        Preference prefWallpaper = (Preference) findPreference("prefWallpaper");
        prefWallpaper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WallpaperSelectDialog wallpaperSelector = new WallpaperSelectDialog((MainActivity) PreferenceActivity.this.getActivity());
                wallpaperSelector.show();
                return false;
            }
        });

        Preference prefAbout = (Preference) findPreference("prefAbout");
        prefAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AboutDialog about = new AboutDialog((MainActivity) PreferenceActivity.this.getActivity());
                about.show();
                return false;
            }
        });


        ChildModeDialogPref prefChildMode = (ChildModeDialogPref) findPreference("prefChildModeObservationEnabled");
        prefChildMode.getPrefValue(mSettings.getChildModeObserverEnabled());
        prefChildMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    final Boolean boolVal = (Boolean) newValue;
                    mSettings.setChildModeObserverEnabled(boolVal);
                }

                if (mSettings.getChildModeObserverEnabled()) {
                    //Restart whole app
                    Tools.doRestart(PreferenceActivity.this.getActivity());
                }
                return false;
            }
        });


        Preference prefExportCurrentSettings = (Preference) findPreference("prefExportCurrentSettings");
        prefExportCurrentSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Toast.makeText(getActivity(), Tools.settingsExport(getActivity()), Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        Preference prefImportCurrentSettings = (Preference) findPreference("prefImportCurrentSettings");
        prefImportCurrentSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                String retVal = Tools.settingsImport(getActivity());
                if(retVal == null)
                {
                    Toast.makeText(getActivity(), getString(R.string.setting_export_failed), Toast.LENGTH_SHORT).show();
                    Thread restarter = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Thread.sleep(2000);
                            }
                            catch (Exception ignore){ }
                            Tools.doRestart(getActivity());
                        }
                    });
                    restarter.start();
                }
                else
                {
                    Toast.makeText(getActivity(), retVal, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


        //Hide options if Child Mode is enabled
        if(mSettings.getChildModeObserverEnabled()) {
            PreferenceScreen settingsPrefs = (PreferenceScreen) findPreference("settingsPrefs");
            PreferenceCategory AppsGameslistingSettings = (PreferenceCategory) findPreference("AppsGameslistingSettings");
            PreferenceCategory parentalControls = (PreferenceCategory) findPreference("parentalControls");
            PreferenceCategory backupSettings = (PreferenceCategory) findPreference("backupSettings");
            settingsPrefs.removePreference(AppsGameslistingSettings);
            settingsPrefs.removePreference(parentalControls);
            settingsPrefs.removePreference(backupSettings);
        }

    }

    @Override
    public void onPause()
    {
        super.onPause();

        // Force read-settings
        mSettings.readValues(true);

        // Check if background observer is active
        if (mSettings.getChildModeObserverEnabled())
        {
            // Start foreground service
            Intent intent = new Intent("br.frp.heyachildmodeobserver.ChildModeService");
            String lang = mSettings.getLanguage();

            intent.setAction("br.frp.heyachildmodeobserver.action.startforeground");
            intent.putExtra("language", lang);
            intent.putExtra("currentLaunchedApp", "br.frp.heya");

            this.getActivity().startService(intent);

        } else {
            // Stop foreground service
            Intent intent = new Intent("br.frp.heyachildmodeobserver.ChildModeService");
            intent.setAction("br.frp.heyachildmodeobserver.action.stopforeground");

            this.getActivity().startService(intent);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setVerticalScrollBarEnabled(false);
            lv.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)lv.getLayoutParams();
            params.topMargin = Tools.getPixelFromDip(-8);
            lv.requestLayout();
            lv.setPadding(0, 0, 0, 0);
        }
        return v;
    }

}
