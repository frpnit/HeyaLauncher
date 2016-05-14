package br.frp.heya.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.frp.heya.R;
import br.frp.heya.tools.Tools;

/**
 * Created by Fabrício Ramos on 29/08/2015.
 */

public final class ChildModeDialogPref extends DialogPreference {

    public static final String TAG = "ChildModeDialogPref";
    private static Context mcontext;

    //Current preference value
    private boolean mValue;
    public ChildModeDialogPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        mcontext=context;
    }

    public void getPrefValue(Boolean value)
    {
        this.mValue = value;
    }

    @Override
    protected View onCreateDialogView() {

        // inflate custom layout with custom title & listview
        View view = View.inflate(getContext(), R.layout.childmodedialogpref, null);

        ((TextView) view.findViewById(R.id.dialog_title)).setText(getDialogTitle());

        Button button_ok = (Button)view.findViewById(R.id.button_ok);
        Button button_cancel = (Button)view.findViewById(R.id.button_cancel);

        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChildModeDialogPref.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                getDialog().dismiss();
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChildModeDialogPref.this.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
                getDialog().dismiss();
            }
        });

        CustomFonts.setCustomFont(0, getContext(), view, false);
        CustomFonts.setCustomFont(1, getContext(), view.findViewById(R.id.dialog_title), false);

        return view;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

        // .setTitle(null), .setPositiveButton(null,null), .setNegativeButton(null,null) to prevent default (blue)
        // title+divider and buttons from showing up
        builder.setTitle(null);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);

    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final View view=getDialog().findViewById(R.id.dialog_container);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                //now we can retrieve the width and height
                int width = Resources.getSystem().getDisplayMetrics().widthPixels * 9 / 10;
                int height = view.getHeight();

                getDialog().getWindow().setLayout(width, height);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // Return if change was cancelled
        if (!positiveResult) {
            return;
        }

        // Notify activity about changes (to update preference summary line)
        notifyChanged();
        if (mValue) {
            callChangeListener(false);
        }else{
            if(Tools.isPackageInstalled(mcontext, "br.frp.heyachildmodeobserver")) {
                callChangeListener(true);
            }else{
                Toast.makeText(mcontext, R.string.child_observer_not_found, Toast.LENGTH_LONG).show();
                callChangeListener(false);
            }
        }
    }

}