package br.frp.heya.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import br.frp.heya.R;

/**
 * Created by Fabrício Ramos on 16/09/2015.
 */
public class CustomEditTextPref extends EditTextPreference {

    public static final String TAG = "CustomEditTextPref";

    private Integer dialogTopmargin_dp=40;
    private Integer dialogBottommargin_dp=40;

    private CharSequence mDialogTitle;
    private CharSequence mDialogMessage;

    private EditText mEditField;
    private String mText;

    public CustomEditTextPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditTextPref(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        // inflate custom layout with custom title & listview
        View view = View.inflate(getContext(), R.layout.customedittextpref, null);

        mDialogTitle = getDialogTitle();
        mDialogMessage = getDialogMessage();

        if(mDialogTitle == null) mDialogTitle = getTitle();
        ((TextView) view.findViewById(R.id.dialog_title)).setText(mDialogTitle);

        if(mDialogMessage!=null){
            ((TextView) view.findViewById(R.id.dialog_message)).setText(mDialogMessage);
        }

        mEditField = (EditText)view.findViewById(R.id.editfield);
        mEditField.setText(mText);

        Button button_ok = (Button)view.findViewById(R.id.button_ok);
        Button button_cancel = (Button)view.findViewById(R.id.button_cancel);

        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomEditTextPref.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                getDialog().dismiss();
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomEditTextPref.this.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
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
                int width = view.getWidth();
                int height = view.getHeight();

                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                float target_height = displayMetrics.heightPixels - (dialogTopmargin_dp + dialogBottommargin_dp) * displayMetrics.density;

                if (height > (int) target_height) {
                    height = (int) target_height;
                    getDialog().getWindow().setLayout(width, height);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    public void setText(String text) {
        mText = text;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = mEditField.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

}
