package de.digisocken.ReoTwe;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(
                "Welcome !",
                "bold",
                "This is ReoTwé, a unofficial very reduced twitter client.",
                "normal",
                R.drawable.ic_logo,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));
        addSlide(AppIntroFragment.newInstance(
                "Maximal Privacy, but ...",
                "... if you want to take a picture, the App needs cam access (but not file access!)",
                android.R.drawable.ic_menu_camera,
                ContextCompat.getColor(this, R.color.primColor)
        ));

        addSlide(AppIntroFragment.newInstance(
                "To Do !",
                "bold",
                "translate the slides and give hints to API Key, usage und (anti)features",
                "normal",
                android.R.drawable.ic_menu_help,
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));

        setFadeAnimation();
        askForPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        setBarColor(ContextCompat.getColor(this, R.color.primColor));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
