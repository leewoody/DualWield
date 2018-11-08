package com.burns.android.ancssample;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

public class CheckPermissionsActivity extends FragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();

        new RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH)
                .subscribe(
                        (isGranted) -> {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        },
                        (error) -> {
                            Toast.makeText(this, R.string.error_permissions, Toast.LENGTH_LONG);
                            finish();
                        });
    }
}
