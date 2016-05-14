package br.frp.heya.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import br.frp.heya.R;
import br.frp.heya.tools.Tools;

/**
 * Created by luki on 28.06.15.
 */
public class WallpaperSelectDialog
{
    /** Current MainActivity */
    MainActivity mMainActivity;

    /** Current Wallpaper Path */
    File mWallpaperPath;
    File mWallpaperDefaultPath;
    public WallpaperSelectDialog(MainActivity mainActivity)
    {
        mMainActivity = mainActivity;
        mWallpaperPath = new File(mMainActivity.getApplicationInfo().dataDir, "wallpaper.png");
        mWallpaperDefaultPath = new File(mMainActivity.getApplicationInfo().dataDir, "wallpaper_default.png");
    }

    public void setDefaultWallpaper() {
        InputStream in = null;
        OutputStream out = null;
        if(!mWallpaperDefaultPath.exists()) {
            try {
                in = mMainActivity.getAssets().open("wallpaper.png");
                out = new FileOutputStream(mWallpaperDefaultPath);
                copyFile(in, out);
            } catch (IOException e) {
                Log.d("tag", "Failed to copy asset file: wallpaper_default.png");
            } finally {
                if (in != null) {
                    try { in.close(); } catch (IOException e) { }
                }
                if (out != null) {
                    try { out.close(); } catch (IOException e) { }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void setWallpaper(Boolean forceError)
    {
        try
        {
            if(!forceError)
            {
                if(!mWallpaperPath.exists())
                {
                    mWallpaperPath=mWallpaperDefaultPath;
                }
            }
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(mWallpaperPath.getAbsolutePath(), bmOptions);

            BitmapDrawable drawable = new BitmapDrawable(mMainActivity.getResources(), bitmap);
            mMainActivity.setBackgroundImage(drawable);
        }
        catch(Exception e)
        {
            Toast.makeText(mMainActivity, "Could not set wallpaper", Toast.LENGTH_SHORT).show();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String errorReason = errors.toString();
            Log.d(WallpaperSelectDialog.class.getName(), "Failed to set background: \n" + errorReason);
        }
    }

    public void show()
    {
        // Create Layout inflater
        LayoutInflater inflater = mMainActivity.getLayoutInflater();
                //(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate custom layout with custom title
        View view = inflater.inflate(R.layout.wallpaperdialog, null);

        List<String> items = new ArrayList<String>();
        if(mWallpaperPath.exists())
        {
            items.add(mMainActivity.getResources().getString(R.string.remove_wallpaper));
        }
        items.add(mMainActivity.getResources().getString(R.string.select_wallpaper));

        final String[] itemList = items.toArray(new String[items.size()]);

        ListView list = (ListView) view.findViewById(android.R.id.list);

        // note the layout we're providing for the ListView entries
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mMainActivity,R.layout.customdiaglistitem,items){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View lview = super.getView(position,convertView, parent);
                CustomFonts.setCustomFont(0, getContext(), lview, false);
                return lview;
            }
        };

        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String itemChosen = parent.getItemAtPosition(position).toString();

                if (itemChosen == mMainActivity.getResources().getString(R.string.remove_wallpaper)) {
                    deleteWallpaper();
                } else if (itemChosen == mMainActivity.getResources().getString(R.string.select_wallpaper)) {
                    showFileSelectorDialog();
                }
            }
        });

        list.setFocusable(true);
        list.setFocusableInTouchMode(true);
        list.setSelection(0);
        list.requestFocus();

        CustomFonts.setCustomFont(0, mMainActivity, view, false);
        CustomFonts.setCustomFont(1, mMainActivity, view.findViewById(R.id.dialog_title), false);

        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);

        /*
        builder.setTitle("JPG/PNG - 1920x1080px");

        List<String> items = new ArrayList<String>();
        if(mWallpaperPath.exists())
        {
            items.add(mMainActivity.getResources().getString(R.string.remove_wallpaper));
        }
        items.add(mMainActivity.getResources().getString(R.string.select_wallpaper));

        final String[] itemList = items.toArray(new String[items.size()]);

        builder.setItems(itemList, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
                dialog.dismiss();

                String itemChosen = itemList[which];
                if (itemChosen == mMainActivity.getResources().getString(R.string.remove_wallpaper))
                {
                    deleteWallpaper();
                }
                else if(itemChosen == mMainActivity.getResources().getString(R.string.select_wallpaper))
                {
                    showFileSelectorDialog();
                }
            }
        });
        */

        builder.setView(view);
        builder.setTitle(null);
        Dialog dialog = builder.show();
        dialog.show();
    }
/*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mClickedDialogEntryIndex = position;
        CustomListPref.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
    }
*/
    private void deleteWallpaper()
    {
        if(mWallpaperPath.exists())
        {
            mWallpaperPath.delete();
            Tools.doRestart(mMainActivity);
        }
    }

    private void showFileSelectorDialog()
    {
        File mPath = Environment.getExternalStorageDirectory();
        FileDialog fileDialog = new FileDialog(mMainActivity, mPath);
        fileDialog.setFileEndsWith(new String[]{".jpg", ".jpeg", ".png"});
        fileDialog.addFileListener(new FileDialog.FileSelectedListener()
        {
            public void fileSelected(File file)
            {
                WallpaperSelectDialog.this.handleFileSelected(file);
            }
        });
        //fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
        //  public void directorySelected(File directory) {
        //      Log.d(getClass().getName(), "selected dir " + directory.toString());
        //  }
        //});
        //fileDialog.setSelectDirectoryOption(false);
        fileDialog.showDialog();
    }

    private void handleFileSelected(File file)
    {
        // Now we try to resize and set this file
        try
        {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);

            // Resize image
            bitmap = Tools.resizeBitmapToFit(bitmap, mMainActivity.getBackgroundWidth(), mMainActivity.getBackgroundHeight());

            File dir = mWallpaperPath.getParentFile();
            if(!dir.exists())
            {
                dir.mkdirs();
            }
            if(mWallpaperPath.exists())
            {
                mWallpaperPath.delete();
            }

            FileOutputStream fOut = new FileOutputStream(mWallpaperPath);

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            fOut.flush();
            fOut.close();

            // Now try to load this one
            setWallpaper(true);
        }
        catch(Exception e)
        {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String errorReason = errors.toString();
            Log.d(MainActivity.class.getName(), "Failed to set background: \n" + errorReason);
        }
    }
}
