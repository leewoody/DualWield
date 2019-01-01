package com.odbol.dualwield;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.burns.android.ancssample.ANCSGattCallback;
import com.burns.android.ancssample.BLEservice;
import com.burns.android.ancssample.R;
import com.odbol.dualwield.events.ConnectionStatusEvent;
import com.odbol.dualwield.events.ConnectionStatusEventBus;
import com.odbol.dualwield.onboarding.DeviceRepo;
import com.odbol.dualwield.onboarding.OnboardingActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.netbeans.modules.vcscore.util.WeakList;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ONBOARDING = 234;

    private TextView statusText;
    private Switch connectionSwitch;
    private Button rePairButton;

    private final CompositeDisposable subscriptions = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        statusText = findViewById(R.id.status);
        connectionSwitch = findViewById(R.id.connectionSwitch);
        rePairButton = findViewById(R.id.repairButton);

        connectionSwitch.setOnCheckedChangeListener((CompoundButton view, boolean isChecked) -> {
            if (isChecked) {
                connect();
            } else {
                disconnect();
            }
        });

        rePairButton.setOnClickListener((v) -> {
            disconnect();
            startActivity(new Intent(this, OnboardingActivity.class));
        });

        subscriptions.add(ConnectionStatusEventBus.getInstance().subscribe().subscribe(this::onConnectionStatus));
    }

    @Override
    protected void onStart() {
        super.onStart();


        if (new DeviceRepo(this).getPairedDevice() == null) {
            startActivityForResult(new Intent(this, OnboardingActivity.class), REQUEST_CODE_ONBOARDING);
        }
        else {
            subscriptions.add(new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH)
                    .subscribe(
                            (isGranted) -> {
                                // we're done, just show the activity.
                            },
                            (error) -> {
                                Toast.makeText(this, R.string.error_permissions, Toast.LENGTH_LONG).show();
                                finish();
                            }));
        }
    }

    @Override
    protected void onDestroy() {
        subscriptions.dispose();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_ONBOARDING) {
            if (resultCode == RESULT_OK) {
                connect();
            } else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connect() {
        startService(new Intent(this, BLEservice.class));
    }

    private void disconnect() {
        stopService(new Intent(this, BLEservice.class));
    }

    private void onConnectionStatus(ConnectionStatusEvent event) {
        int state;
        state = getStateMessage(event.status);

        statusText.setText(state);

        connectionSwitch.setChecked(!event.isServiceStarted);

        // TODO(tyler): always show this???
        rePairButton.setVisibility(event.status != ANCSGattCallback.BleAncsConnected ? View.VISIBLE : View.VISIBLE);
    }

    public static int getStateMessage(int status) {
        int state;
        switch (status) {
            case ANCSGattCallback.BleBuildStart:
            case ANCSGattCallback.BleBuildConnectedGatt:
            case ANCSGattCallback.BleBuildDiscoverService:
            case ANCSGattCallback.BleBuildDiscoverOver:
            case ANCSGattCallback.BleBuildSetingANCS:
            case ANCSGattCallback.BleBuildNotify:
                state = R.string.ongoing_notification_message_connecting;
                break;
            case ANCSGattCallback.BleAncsConnected: // 10
                state = R.string.ongoing_notification_message_connected;
                break;
            case ANCSGattCallback.BleDisconnect: // 0
            default:
                state = R.string.ongoing_notification_message_disconnected;
                break;
        }
        return state;
    }
}
