package br.frp.heya.gui;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.frp.heya.R;
import br.frp.heya.tools.Tools;

/**
 * Created by Fabrício Ramos on 29/08/2015.
 */

public class CustomPrefCategory extends PreferenceCategory {

    private Context mcontext;
    public CustomPrefCategory(Context context) {
        super(context);
    }

    public CustomPrefCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomPrefCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // It's just a TextView!
        TextView categoryTitle = (TextView) super.onCreateView(parent);
        categoryTitle.setPadding(Tools.getPixelFromDip(15),categoryTitle.getPaddingTop(), Tools.getPixelFromDip(56), categoryTitle.getPaddingBottom());
        CustomFonts.setCustomFont(1, getContext(), categoryTitle, false);
        return categoryTitle;
    }

    @Override
    protected boolean onPrepareAddPreference(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            throw new IllegalArgumentException(
                    "Cannot add a PreferenceCategory directly to a PreferenceCategory");
        }
        preference.setLayoutResource(R.layout.customprefitemlayout);
        return super.onPrepareAddPreference(preference);
    }
}
