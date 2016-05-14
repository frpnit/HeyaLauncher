package br.frp.heya.gui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;

import java.text.DateFormat;
import java.util.Date;

import br.frp.heya.R;
import br.frp.heya.tools.SettingsProvider;
import br.frp.heya.tools.Tools;

/**
 * Info / System view
 */
public class InfosPrefActivity extends PreferenceFragment
{
    SettingsProvider mSettings = SettingsProvider.getInstance(this.getActivity());

    public InfosPrefActivity()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.infoprefactivity);

        Preference prefDeviceDetails = (Preference) findPreference("prefVirtualDeviceDetails");
        prefDeviceDetails.setSummary(Tools.getDeviceDetails());

        Preference prefHostname = (Preference) findPreference("prefVirtualHostname");
        prefHostname.setSummary(Tools.getHostName(getActivity().getResources().getString(R.string.notfound)));

        Preference prefWifiName = (Preference) findPreference("prefVirtualWifiName");
        prefWifiName.setSummary(Tools.getWifiSsid(this.getActivity(), getActivity().getResources().getString(R.string.notfound)));

        Preference prefIpAddress = (Preference) findPreference("prefVirtualIpAddress");
        prefIpAddress.setSummary(Tools.getActiveIpAddress(getActivity(), getActivity().getResources().getString(R.string.notfound)));

        Preference prefDeviceUpTime = (Preference) findPreference("prefVirtualDeviceUpTime");
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM, getResources().getConfiguration().locale);
        String upTime = String.format(getActivity().getResources().getString(R.string.uptimedesc), Tools.formatInterval(SystemClock.elapsedRealtime()), dateFormat.format(new Date(System.currentTimeMillis() - SystemClock.elapsedRealtime())));
        prefDeviceUpTime.setSummary(upTime);

        if(mSettings.getChildModeObserverEnabled()) {
            PreferenceScreen infoPrefs = (PreferenceScreen) findPreference("infoPrefs");
            PreferenceCategory systemScreens = (PreferenceCategory) findPreference("ouyaScreens");
            infoPrefs.removePreference(systemScreens);

        }else {
            Preference prefDiscover = (Preference) findPreference("prefOuyaDiscover");
            prefDiscover.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("ouya://launcher/discover")));
                    return false;
                }
            });

            Preference prefOuyaPairing = (Preference) findPreference("prefOuyaPairing");
            prefOuyaPairing.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("ouya://launcher/manage/controllers/pairing")));
                    return false;
                }
            });

            Preference prefOuyaNetwork = (Preference) findPreference("prefOuyaNetwork");
            prefOuyaNetwork.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("ouya://launcher/manage/network")));
                    return false;
                }
            });

            Preference prefOuyaManage = (Preference) findPreference("prefOuyaManage");
            prefOuyaManage.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("ouya://launcher/manage")));
                    return false;
                }
            });

        }
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
