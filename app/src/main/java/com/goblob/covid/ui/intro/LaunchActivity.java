package com.goblob.covid.ui.intro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.goblob.covid.BuildConfig;
import com.goblob.covid.R;
import com.goblob.covid.utils.DbHelper;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_base);

        if (DbHelper.get().isFirstTime("introActivity")) {
            introActivity();
        } else {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob.covid.ui.Main2Activity");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void introActivity() {
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/intro"));
        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob.covid.ui.intro.IntroActivity");
        intent.setPackage(getBaseContext().getPackageName());
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
