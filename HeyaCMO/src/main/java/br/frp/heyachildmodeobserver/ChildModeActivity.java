package br.frp.heyachildmodeobserver;

import android.app.Activity;
import android.os.Bundle;

public class ChildModeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }

}