package br.frp.heya.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import br.frp.heya.R;

/**
 * Created by Fabrício Ramos on 29/08/2015.
 */

public final class CustomSeekbarPref extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "CustomListPref";

    //Interface vars
    private CharSequence mDialogTitle;

    // Slider parameters
    int mInitValue;
    int mCurrentValue;
    int mMinValue;
    int mMaxValue;
    int mStep;

    // View elements
    private SeekBar mSeekBar;
    private TextView mValueText;

    //Value slider on seekbar, uses increment step calculation
    private int mCurrent_on_Seek;

    public CustomSeekbarPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSliderValues(int Current, int Min, int Max, int Step)
    {
        this.mCurrentValue = this.mInitValue = Current;
        this.mMinValue = Min;
        this.mMaxValue = Max;
        this.mStep = Step;
        mCurrent_on_Seek = mCurrentValue / mStep;
    }

    @Override
    protected View onCreateDialogView() {
        // Get current value from preferences

        // Inflate layout
        //LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate custom layout with custom title & listview
        View view = View.inflate(getContext(), R.layout.customseekbarpref, null);

        mDialogTitle = getDialogTitle();
        if(mDialogTitle == null) mDialogTitle = getTitle();
        ((TextView) view.findViewById(R.id.dialog_title)).setText(mDialogTitle);

        // Setup minimum and maximum text labels
        ((TextView) view.findViewById(R.id.min_value)).setText(Integer.toString(mMinValue));
        ((TextView) view.findViewById(R.id.max_value)).setText(Integer.toString(mMaxValue));

        // Setup SeekBar
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mSeekBar.setMax((mMaxValue - mMinValue) / mStep);
        mSeekBar.setProgress(mCurrent_on_Seek - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        // Setup text label for current value
        mValueText = (TextView) view.findViewById(R.id.current_value);
        mValueText.setText(Integer.toString(mCurrentValue));

        Button button_ok = (Button)view.findViewById(R.id.button_ok);
        Button button_cancel = (Button)view.findViewById(R.id.button_cancel);

        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomSeekbarPref.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                getDialog().dismiss();
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomSeekbarPref.this.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
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
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // Return if change was cancelled
        if (!positiveResult) {
            mCurrentValue=mInitValue;
            mCurrent_on_Seek = mCurrentValue / mStep;
            return;
        }

        // Notify activity about changes (to update preference summary line)
        notifyChanged();
        mInitValue=mCurrentValue;
        callChangeListener(mCurrentValue);
    }

    @Override
    public CharSequence getTitle() {
        // Format title string with current value
        String title = super.getTitle().toString();
        int value = mCurrentValue;
        return String.format(title, value);
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        // Update current value
        mCurrent_on_Seek = value + mMinValue;
        mCurrentValue = mCurrent_on_Seek * mStep;
        // Update label with current value
        mValueText.setText(Integer.toString(mCurrentValue));
    }

    public void onStartTrackingTouch(SeekBar seek) {
        // Not used
    }

    public void onStopTrackingTouch(SeekBar seek) {
        // Not used
    }

}