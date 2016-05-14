package br.frp.heya.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import br.frp.heya.tools.SettingsProvider;
import br.frp.heya.tools.Tools;

/**
 * Created by Fabrício Ramos on 02/11/2015.
 */

public class ChildModeUnlockOverlay {

    private Context mcontext;
    private Activity mActivity;

    private WindowManager wm;
    private Button buttons[] = new Button[4];

    private int unlockClicks=4;
    public Boolean inited=false;

    public ChildModeUnlockOverlay(Activity activity, Context context) {
        mcontext=context;
        mActivity=activity;
    }

    public void init(){

        wm = (WindowManager) mcontext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                50,50,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        for (int i=0;i<buttons.length;i++) {

            switch (i){
                case 0:
                    params.gravity= Gravity.TOP|Gravity.LEFT;
                    break;
                case 1:
                    params.gravity= Gravity.TOP|Gravity.RIGHT;
                    break;
                case 2:
                    params.gravity= Gravity.BOTTOM|Gravity.LEFT;
                    break;
                case 3:
                    params.gravity= Gravity.BOTTOM|Gravity.RIGHT;
                    break;
            }

            buttons[i] = new Button(mcontext);
            buttons[i].setBackgroundColor(Color.TRANSPARENT);
            wm.addView(buttons[i], params);

            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.GONE);
                    unlockClicks--;
                    if (unlockClicks == 0) {
                        finish();
                        SettingsProvider mSettings=SettingsProvider.getInstance(mActivity);
                        mSettings.setChildModeObserverEnabled(false);
                        Intent intent = new Intent("br.frp.heyachildmodeobserver.ChildModeService");
                        intent.setAction("br.frp.heyachildmodeobserver.action.stopforeground");
                        mActivity.startService(intent);

                        // Restart whole app
                        Tools.doRestart(mActivity);
                    }
                }
            });

        }
        inited=true;

    }

    public void finish() {
        for (int i=0;i<buttons.length;i++) {
            if (buttons[i] != null && wm != null) {
                wm.removeView(buttons[i]);
                buttons[i] = null;
            }
        }
        if(wm != null)wm=null;
        inited=false;
    }

}
