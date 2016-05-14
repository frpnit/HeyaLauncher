package br.frp.heya.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import br.frp.heya.R;

/**
 * Created by Fabrício Ramos on 16/09/2015.
 */
public class CustomListPref extends ListPreference implements AdapterView.OnItemClickListener {

    public static final String TAG = "CustomListPref";

    private Integer dialogTopmargin_dp=40;
    private Integer dialogBottommargin_dp=40;

    private int mClickedDialogEntryIndex;

    private CharSequence mDialogTitle;

    public CustomListPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListPref(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        // inflate custom layout with custom title & listview
        View view = View.inflate(getContext(), R.layout.customlistpref, null);

        mDialogTitle = getDialogTitle();
        if(mDialogTitle == null) mDialogTitle = getTitle();
        ((TextView) view.findViewById(R.id.dialog_title)).setText(mDialogTitle);

        ListView list = (ListView) view.findViewById(android.R.id.list);
        // note the layout we're providing for the ListView entries
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getContext(), R.layout.customdiaglistitemradio,
                getEntries()) {
                    @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View lview = super.getView(position,convertView, parent);
                            CustomFonts.setCustomFont(0, getContext(), lview, false);
                            return lview;
                    }
        };

        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setItemChecked(findIndexOfValue(getValue()), true);
        list.setOnItemClickListener(this);
        list.setSelection(0);
        list.requestFocus();

        Button button_cancel = (Button)view.findViewById(R.id.button_cancel);

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomListPref.this.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
                getDialog().dismiss();
            }
        });

        CustomFonts.setCustomFont(0, getContext(), view, false);
        CustomFonts.setCustomFont(1, getContext(), view.findViewById(R.id.dialog_title), false);

        return view;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // adapted from ListPreference
        if (getEntries() == null || getEntryValues() == null) {
            // throws exception
            super.onPrepareDialogBuilder(builder);
            return;
        }

        mClickedDialogEntryIndex = findIndexOfValue(getValue());

        // .setTitle(null), .setPositiveButton(null,null), .setNegativeButton(null,null) to prevent default (blue)
        // title+divider and buttons from showing up
        builder.setTitle(null);
        builder.setPositiveButton(null,null);
        builder.setNegativeButton(null,null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mClickedDialogEntryIndex = position;
        CustomListPref.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
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
    protected void onDialogClosed(boolean positiveResult) {
        // adapted from ListPreference
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String value = getEntryValues()[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }
}
