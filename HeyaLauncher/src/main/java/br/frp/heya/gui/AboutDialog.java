package br.frp.heya.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import br.frp.heya.R;
import br.frp.heya.tools.SettingsProvider;
import br.frp.heya.tools.Tools;

public class AboutDialog
{
    /** Current MainActivity */
    MainActivity mMainActivity;


    private Integer dialogTopmargin_dp=40;
    private Integer dialogBottommargin_dp=40;

    private SettingsProvider mSettings;

    //Child Mode Unlocker object
    private ChildModeUnlockOverlay childModeUnlockOverlay;

    public AboutDialog(MainActivity mainActivity)
    {
        mMainActivity = mainActivity;
    }

    public void show()
    {
        // Create Layout inflater
        LayoutInflater inflater = mMainActivity.getLayoutInflater();

        // inflate custom layout with custom title
        View view = inflater.inflate(R.layout.aboutdialog, null);

        Button button = (Button)view.findViewById(R.id.button_ok);
        ScrollView scrollview = (ScrollView)view.findViewById(R.id.scroll);

        scrollview.setFocusable(true);
        scrollview.setFocusableInTouchMode(true);
        scrollview.requestFocus();

        CustomFonts.setCustomFont(0, mMainActivity, view, false);
        CustomFonts.setCustomFont(1, mMainActivity, view.findViewById(R.id.dialog_title), false);

        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);

        builder.setView(view);
        builder.setTitle(null);
        final Dialog dialog = builder.show();
        dialog.show();

        //Watch for dialog dismiss action to remove Child Mode Unlocker Overlay, if it exists
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(final DialogInterface dialog) {
                if (childModeUnlockOverlay != null) {
                    childModeUnlockOverlay.finish();
                    childModeUnlockOverlay = null;
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dialog.dismiss(); }
        });

        final View v=dialog.findViewById(R.id.dialog_container);
        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                //now we can retrieve the width and height
                int width = Resources.getSystem().getDisplayMetrics().widthPixels * 7 / 10;
                int height = v.getHeight();

                DisplayMetrics displayMetrics = mMainActivity.getResources().getDisplayMetrics();
                float target_height = displayMetrics.heightPixels - (dialogTopmargin_dp + dialogBottommargin_dp) * displayMetrics.density;

                if (height > (int) target_height) {
                    height = (int) target_height;
                    dialog.getWindow().setLayout(width, height);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        // Create Child Mode Unlocker Overlay, IF we are on Child Mode
        mSettings = SettingsProvider.getInstance(mMainActivity);
        if (mSettings.getChildModeObserverEnabled()) {
            childModeUnlockOverlay = new ChildModeUnlockOverlay(mMainActivity, mMainActivity.getApplicationContext());
            childModeUnlockOverlay.init();
        }

    }

}
