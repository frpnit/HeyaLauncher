package br.frp.heya.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import br.frp.heya.tools.AppStarter;
import br.frp.heya.tools.SettingsProvider;

/**
 * Receiver for Boot-Complete Broadcast
 */
public class StartUpBootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            Log.d(StartUpBootReceiver.class.getName(), "Received BOOT_COMPLETED intent.");

            // Get settings provider
            SettingsProvider settingsProvider = SettingsProvider.getInstance(context);

            // Starts background observer if Child Mode is enabled, and starts controller pairing to ensure user can connect a controller to Ouya,
            //so it will be possible to unlock Child Mode even if relying only on a single Ouya unpaired controller.
            if(settingsProvider.getChildModeObserverEnabled())
            {

                Intent svc_intent = new Intent("br.frp.heyachildmodeobserver.ChildModeService");
                String lang = settingsProvider.getLanguage();

                svc_intent.setAction("br.frp.heyachildmodeobserver.action.startforeground");
                svc_intent.putExtra("language", lang);
                svc_intent.putExtra("currentLaunchedApp", "br.frp.heya");
                context.startService(svc_intent);

            } else {
                // Start startup-activity
                String startPackage = settingsProvider.getStartupPackage();
                Log.d(StartUpBootReceiver.class.getName(), "Startup start package is: " + startPackage);

                if (startPackage != null && !startPackage.equals("")) {
                    AppStarter.startAppByPackageName(context, startPackage, true, true, true);
                }
            }
        }

    }

}
