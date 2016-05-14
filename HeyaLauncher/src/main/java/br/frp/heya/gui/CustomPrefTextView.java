package br.frp.heya.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Fabrício Ramos on 13/09/2015.
 */
public class CustomPrefTextView extends TextView {

    public CustomPrefTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CustomPrefTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomPrefTextView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode()) {
            CustomFonts.setCustomFont(0, context, this, false);
        }
    }
}
