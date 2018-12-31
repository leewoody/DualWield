package com.burns.android.ancssample;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.odbol.dualwield.onboarding.OnboardingActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class CheckPermissionsActivity extends FragmentActivity {

    private static final String ONBOARDING_PREFS = "ONBOARDING_PREFS";

    /**
     * Change the version number to re-show onboarding to everyone!
     */
    private static final String PREF_ONBOARDING_VISITED = "PREF_ONBOARDING_VISITED_v1";

    @Override
    protected void onStart() {
        super.onStart();

        if (!launchOnboardingIfNeeded()) {
            new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH)
                    .subscribe(
                            (isGranted) -> {
                                // we're done, just show the activity.
                            },
                            (error) -> {
                                Toast.makeText(this, R.string.error_permissions, Toast.LENGTH_LONG);
                                finish();
                            });
        }
    }

    private boolean launchOnboardingIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(ONBOARDING_PREFS, MODE_PRIVATE);

        if (!prefs.getBoolean(PREF_ONBOARDING_VISITED, false)) {
            prefs.edit()
                    .putBoolean(PREF_ONBOARDING_VISITED, true)
                    .apply();

            startActivity(new Intent(this, OnboardingActivity.class));
            return true;
        } else {
            return false;
        }
    }
}
