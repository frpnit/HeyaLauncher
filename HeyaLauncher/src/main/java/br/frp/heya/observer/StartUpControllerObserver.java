package br.frp.heya.observer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.InputDevice;
import android.widget.Toast;

import java.util.Locale;

import br.frp.heya.R;


/**
 * Created by Fabrício RP on 30/04/2016.
 */
public class StartUpControllerObserver {

    private Integer pairingCurrentTime = 0;
    private Integer pairingCheckInterval = 5000;
    private Integer pairingLaunchTime = 60000;
    private Handler pairingTimerHandler=null;
    private Runnable pairingRunnable=null;

    /**
     * Check if has any controller paired/plugged, if not checks every 5 seconds for controller (pairingCheckInterval),
     * if none after 1 minute (pairingLaunchTime), brings pairing screen up
     */
    public void start(final Context context) {
        pairingCurrentTime = 0;
        pairingTimerHandler = new Handler(context.getMainLooper());
        pairingRunnable=new Runnable() {
            @Override
            public void run() {
                if (!hasConnectedController()) {
                    if (pairingCurrentTime >= pairingLaunchTime) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("ouya://launcher/manage/controllers/pairing"));
                            context.startActivity(intent);
                        }catch (Exception e) {
                            Toast.makeText(context, R.string.no_pairing_activity, Toast.LENGTH_SHORT).show();
                        }
                        stop();
                    } else {
                        pairingCurrentTime += pairingCheckInterval;
                        pairingTimerHandler.postDelayed(pairingRunnable, pairingCheckInterval);
                    }
                } else {
                    stop();
                }
            }
        };

        pairingTimerHandler.post(pairingRunnable);
    }

    private void stop(){
        pairingTimerHandler.removeCallbacks(pairingRunnable);
        pairingTimerHandler = null;
    }

    /**
     * Function that returns if any controller is paired/plugged/turned on
     */
    private boolean hasConnectedController(){

        /*Get a list of valid input ids and iterate through them*/
        for (int deviceId : InputDevice.getDeviceIds()) {

            InputDevice currentDevice = InputDevice.getDevice(deviceId);

            /*Device ID of the controller*/
            String controllerName = currentDevice.getName().toLowerCase(Locale.ENGLISH);

            /*Check to see if a known controller is connected*/
            if (controllerName.contains("controller") ||
                    controllerName.contains("conteroller") ||
                    controllerName.contains("contoroller") ||
                    controllerName.contains("pad") ||
                    controllerName.contains("joystick") ||
                    controllerName.contains("nintendo")) {
                //|| (this.hidDevicesOK && controllerName.equals("hid_device"))){
                return true;
            }
        }
        return false;
    }

}
