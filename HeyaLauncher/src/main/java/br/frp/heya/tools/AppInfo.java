package br.frp.heya.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Container to hold app informations
 */
public class AppInfo extends ApplicationInfo
{
    /** The current context */
    Context mContext;

    /** The current app */
    ApplicationInfo appInfo;

    /**
     * @param app App to be hold
     */
    public AppInfo(Context context, ApplicationInfo app)
    {
        super(app);
        appInfo = app;
        mContext = context;
    }

    /**
     * @return Name to be displayed
     */
    public String getDisplayName()
    {
        PackageManager pm = mContext.getPackageManager();
        String retVal = this.loadLabel(pm).toString();
        if(retVal == null || retVal.equals("")) {
            retVal = packageName;
        } else if (retVal.equals(packageName)){
            try {
                Intent intent = new Intent();
                intent.setPackage(packageName);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ResolveInfo result = pm.resolveActivity(intent, 0);
                String forcedLabel = result.activityInfo.loadLabel(pm).toString();
                if(forcedLabel!=null && !forcedLabel.equals("")) retVal = forcedLabel;
            } catch (Exception e) { }
        }
        return retVal;
    }

    /**
     * @return Icon to be displayed
     */
    public Drawable getDisplayIcon()
    {
        Resources resources;
        Drawable IconImage = null;

        try {
            resources = mContext.getPackageManager().getResourcesForApplication(appInfo);
            int identifier = resources.getIdentifier(packageName + ":drawable/ouya_icon", "", "");

            if (identifier == 0) {
                IconImage = this.loadIcon(mContext.getPackageManager());
            } else {
                IconImage = mContext.getPackageManager().getResourcesForApplication(appInfo).getDrawable(identifier);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return IconImage;
        //return this.loadIcon(mContext.getPackageManager());
    }
}
