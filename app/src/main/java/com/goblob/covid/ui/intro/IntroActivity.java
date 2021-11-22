package com.goblob.covid.ui.intro;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.model.SliderPage;
import com.goblob.covid.BuildConfig;
import com.goblob.covid.R;


public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle("Bienvenidos!");
        sliderPage1.setDescription("Hemos creado esta app para protegernos contra el covid19.");
        sliderPage1.setImageDrawable(R.drawable.app_logo);
        sliderPage1.setBgDrawable(R.drawable.back_slide1);
        addSlide(AppIntroFragment.newInstance(sliderPage1));

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle("Comencemos!");
        sliderPage2.setDescription("Necesitamos acceder a tu localizacion para saber si has estado en contacto con alguien infectado con covid19.");
        sliderPage2.setImageDrawable(R.drawable.app_logo);
        sliderPage2.setBgDrawable(R.drawable.back_slide2);
        addSlide(AppIntroFragment.newInstance(sliderPage2));

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle("Tu Informacion");
        sliderPage3.setDescription("Tu informacion esta protegida pues no necesitamos numero de telefono, e-mail, facebook.");
        // sliderPage1.setTitleTypefaceFontRes(R.font.lato);
        // sliderPage1.setDescTypefaceFontRes(R.font.lato);
        sliderPage3.setImageDrawable(R.drawable.app_logo);
        sliderPage3.setBgDrawable(R.drawable.back_slide3);
        addSlide(AppIntroFragment.newInstance(sliderPage3));

        SliderPage sliderPage4 = new SliderPage();
        sliderPage4.setTitle("Te Mantenemos Actualizado");
        sliderPage4.setDescription("A traves de la app reciviras informacion importante y geolocalizada. Noticias del lugar donde resides.");
        // sliderPage4.setTitleTypefaceFontRes(R.font.opensans_regular);
        // sliderPage4.setDescTypefaceFontRes(R.font.opensans_regular);
        sliderPage4.setImageDrawable(R.drawable.app_logo);
        sliderPage4.setBgDrawable(R.drawable.back_slide4);
        addSlide(AppIntroFragment.newInstance(sliderPage4));

        SliderPage sliderPage5 = new SliderPage();
        sliderPage5.setTitle("Adios Covid19");
        sliderPage5.setDescription("Pongamos todos nuestro granito de arena y superemos este virus, NOSOTROS PODEMOS MAS!");
        sliderPage3.setTitleTypeface("OpenSans-Light.ttf");
        sliderPage3.setDescTypeface("OpenSans-Light.ttf");
        sliderPage5.setImageDrawable(R.drawable.app_logo);
        sliderPage5.setBgDrawable(R.drawable.back_slide5);
        addSlide(AppIntroFragment.newInstance(sliderPage5));

        // Here we ask for camera permission on slide 2
        askForPermissions(new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 2, true); // OR

        // OPTIONAL METHODS
        // Override bar/separator color.
        // setBarColor(Color.parseColor("#3F51B5"));
        // setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        // showSkipButton(true);
        // setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        // setVibrate(true);
        // setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/map"));
        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob.covid.ui.Main2Activity");
        intent.setPackage(getBaseContext().getPackageName());
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Init", true);
        finish();
        startActivity(intent);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/map"));
        Intent intent = new Intent();
        intent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob.covid.ui.Main2Activity");
        intent.setPackage(getBaseContext().getPackageName());
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Init", true);
        finish();
        startActivity(intent);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

}
