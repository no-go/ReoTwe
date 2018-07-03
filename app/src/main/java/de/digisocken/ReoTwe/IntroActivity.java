package de.digisocken.ReoTwe;

import android.Manifest;
import android.graphics.Color;
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
                getString(R.string.welcome),
                "bold",
                getString(R.string.welcomeHint),
                "normal",
                R.drawable.ic_logo,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));

        addSlide(AppIntroFragment.newInstance(
                "I am sorry",
                "bold",
                "since june twitter needs callback url, but in official twitterkit this is not supported :-O Thus login features are removed :-(",
                "normal",
                android.R.drawable.ic_dialog_info,
                ContextCompat.getColor(this, android.R.color.holo_red_light),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slideDetox),
                "bold",
                getString(R.string.slideDetoxHint),
                "normal",
                R.drawable.wtfemoji,
                Color.BLACK,
                Color.WHITE,
                Color.WHITE
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slidePriv),
                "bold",
                getString(R.string.slidePrivHint),
                "normal",
                android.R.drawable.ic_menu_camera,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));




        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slideApi1),
                "bold",
                getString(R.string.slideApiHint1),
                "normal",
                android.R.drawable.ic_menu_info_details,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slideApi2),
                "bold",
                getString(R.string.slideApiHint2),
                "normal",
                android.R.drawable.ic_lock_lock,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slideApi3),
                "bold",
                getString(R.string.slideApiHint3),
                "normal",
                android.R.drawable.ic_menu_add,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slideApi4),
                "bold",
                getString(R.string.slideApiHint4),
                "normal",
                android.R.drawable.ic_menu_today,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));


/*
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slideAlpha),
                "bold",
                getString(R.string.slideAlphaHint),
                "normal",
                android.R.drawable.ic_menu_help,
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));
*/

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.slideHomepage),
                "bold",
                getString(R.string.slideHomepageHint, App.PROJECT_LINK),
                "normal",
                R.drawable.homehint,
                ContextCompat.getColor(this, R.color.textBack),
                ContextCompat.getColor(this, R.color.textFront),
                ContextCompat.getColor(this, R.color.textFront)
        ));

        setFadeAnimation();
        askForPermissions(new String[]{Manifest.permission.CAMERA}, 3);
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
