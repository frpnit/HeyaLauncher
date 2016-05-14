package br.frp.heya.gui;

import java.lang.reflect.Method;
import java.util.*;
import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.*;
import android.widget.TextView;

/**
 * Based on Felipe Micaroni Lalli’s post on Stack Overflow
 * (http://stackoverflow.com/a/13633767/1555605).
 *
 * @author Rubén Illodo Brea
 *
 * Altered by Fabrício Ramos Pereira on 11/09/2015
 */

public abstract class CustomFonts {
    private static Integer curStyle;
    private static Map<String, Typeface> typefaceCache = new HashMap<String, Typeface>();
    private static SparseArray<SparseArray<String>> FontFamily_Style_files = new SparseArray<SparseArray<String>>();

    private static Typeface getTypeface(Context context, Typeface originalTypeface, Integer fontfamily_index) {
        SparseArray<String> fontStyles_files = FontFamily_Style_files.get(fontfamily_index);
        String ttfFileToUse = fontStyles_files.get(Typeface.NORMAL);

        if (originalTypeface != null) {
            curStyle = originalTypeface.getStyle();

            String ttfFileToUseForStyle = fontStyles_files.get(curStyle);
            if (ttfFileToUseForStyle != null) {
                ttfFileToUse = ttfFileToUseForStyle;
                curStyle = 0;
            }
        }

        return getTypefaceUsingCache(context, ttfFileToUse);
    }

    private static Typeface getTypefaceUsingCache(Context context, String fontPath) {
        if (!typefaceCache.containsKey(fontPath))
            typefaceCache.put(fontPath, Typeface.createFromAsset(context.getAssets(), fontPath));

        return typefaceCache.get(fontPath);
    }

    /**
     * Sets the .ttf files (inside {@code assets}) you want to use for each
     * typeface style.
     * <p>
     * There’s no need to use this method if you place your .ttf files at the
     * default location and with the default names (see
     * {@linkplain CustomFonts #DEFAULT_FONT_NORMAL},
     * {@linkplain CustomFonts #DEFAULT_FONT_ITALIC},
     * {@linkplain CustomFonts #DEFAULT_FONT_BOLD},
     * {@linkplain CustomFonts #DEFAULT_FONT_BOLD_ITALIC}).
     *
     * @param forNormal
     *            .ttf file por {@linkplain Typeface#NORMAL}.
     * @param forItalic
     *            .ttf file por {@linkplain Typeface#ITALIC}.
     * @param forBold
     *            .ttf file por {@linkplain Typeface#BOLD}.
     * @param forBoldItalic
     *            .ttf file por {@linkplain Typeface#BOLD_ITALIC}.
     */
    public static void setFontsToUse(Integer fontfamily_index, String forNormal, String forBold, String forItalic, String forBoldItalic) {
        SparseArray<String> fontStyles_files = new SparseArray<String>();
        fontStyles_files.put(Typeface.NORMAL, forNormal);
        fontStyles_files.put(Typeface.BOLD, forBold);
        fontStyles_files.put(Typeface.ITALIC, forItalic);
        fontStyles_files.put(Typeface.BOLD_ITALIC, forBoldItalic);

        FontFamily_Style_files.put(fontfamily_index, fontStyles_files);
    }

    /**
     * Walks ViewGroups, finds TextViews and applies Typefaces taking styling
     * into consideration.
     *
     * @param context
     *            used to reach the .ttf files in {@code assets}.
     * @param view
     *            root view to apply typeface to.
     */
    public static void setCustomFont(Integer fontfamily_index, Context context, View view, boolean reflect) {

        if(view instanceof ViewGroup){
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
                setCustomFont(fontfamily_index, context, ((ViewGroup) view).getChildAt(i), reflect);
        }
        else if (view instanceof TextView) {
            Typeface currentTypeface = ((TextView) view).getTypeface();
            ((TextView) view).setTypeface(getTypeface(context, currentTypeface, fontfamily_index));
        }
        else if (reflect) {
            try {
                Object[] nullArgs = null;
                //Test wether setTypeface and getTypeface methods exists
                Method methodTypeFace = view.getClass().getMethod("setTypeface", new Class[]{Typeface.class, Integer.TYPE});
                //With getTypeface we'll get back the style (Bold, Italic...) set in XML
                Method methodGetTypeFace = view.getClass().getMethod("getTypeface", new Class[]{});
                Typeface currentTypeface = ((Typeface) methodGetTypeFace.invoke(view, nullArgs));
                Typeface newTypeface = getTypeface(context, currentTypeface, fontfamily_index);
                //Invoke the method and apply the new font with the defined style to the view if the method exists (textview,...)
                methodTypeFace.invoke(view, new Object[]{newTypeface, currentTypeface == null ? 0 : curStyle});
            }
            //Will catch the view with no such methods (listview...)
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private CustomFonts() {
    }

}
