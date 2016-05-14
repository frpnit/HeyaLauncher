package br.frp.heya.gui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.frp.heya.R;
import br.frp.heya.tools.AppInfo;

/**
 * App Settings Overlay for additional settings per app
 */
public class AppSettingsOverlayDialog extends DialogFragment
{
    public enum ActionEnum { NOTHING, SORT, DETAILS, SETTINGS }

    public static AppSettingsOverlayDialog newInstance(AppInfo appInfo)
    {
        // Create dialog and set custom style
        AppSettingsOverlayDialog dialog = new AppSettingsOverlayDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.TransparentDialog);

        // Add needed stuff
        dialog.setAppInfo(appInfo);

        return dialog;
    }

    /** AppInfo of the current App */
    private AppInfo mAppInfo;

    /** Action handler */
    private OnActionClickedHandler onActionClickedHandler;

    /**
     * Default constructor required for DialogFragment
     */
    public AppSettingsOverlayDialog()
    {
    }

    /**
     * Set the action click listener
     * @param listener
     */
    public void setOnActionClickedHandler(OnActionClickedHandler listener)
    {
        onActionClickedHandler = listener;
    }

    /**
     * Sets the current AppInfo
     * @param appInfo
     */
    public void setAppInfo(AppInfo appInfo)
    {
        mAppInfo = appInfo;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {
                startRemoving();
            }
        };
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Create view
        View view = inflater.inflate(R.layout.appsettingsoverlaydialog, container);

        // Set text, icons and click-handler
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(mAppInfo.getDisplayName().toUpperCase());

        ImageView appIcon = (ImageView) view.findViewById(R.id.appIcon);
        appIcon.setImageDrawable(mAppInfo.getDisplayIcon());

        LinearLayout appSort = (LinearLayout) view.findViewById(R.id.appSort);
        appSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireActionClicked(ActionEnum.SORT);
            }
        });

        LinearLayout appDetails = (LinearLayout) view.findViewById(R.id.appDetails);
        appDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireActionClicked(ActionEnum.DETAILS);
            }
        });

        LinearLayout appSettings = (LinearLayout) view.findViewById(R.id.appSettings);
        appSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fireActionClicked(ActionEnum.SETTINGS);
            }
        });

        CustomFonts.setCustomFont(0, getActivity(), view, false);
        CustomFonts.setCustomFont(1, getActivity(), title, false);

        LinearLayout appsettingsContainer = (LinearLayout) view.findViewById(R.id.container);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.appsettings_container_in);
        appsettingsContainer.startAnimation(animation);

        return view;
    }

    private void fireActionClicked(ActionEnum action)
    {
        if(onActionClickedHandler != null)
        {
            onActionClickedHandler.onActionClicked(action);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        startRemoving();
    }

    public void startRemoving() {
        LinearLayout appsettingsContainer = (LinearLayout) this.getDialog().findViewById(R.id.container);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.appsettings_container_out);
        appsettingsContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                dismiss();
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
    }

    /**
     * Interface for a service error
     */
    public interface OnActionClickedHandler
    {
        public void onActionClicked(ActionEnum action);
    }
}