package br.frp.heya.gui;

import android.app.Fragment;
import android.view.KeyEvent;

/**
 * Fragment that has additional features
 */
public class CustomFragment extends Fragment
{
    /**
     * Custom on backpressed method
     */
    public boolean onBackPressed()
    {
        return false;
    }

    public boolean onKeyDown(int keycode, KeyEvent e)
    {
        return false;
    }
}
