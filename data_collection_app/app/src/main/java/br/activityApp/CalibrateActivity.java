package br.activityApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.File;

import br.activityApp.sensor.SensorService;
import br.activityApp.R;

public class CalibrateActivity extends Activity{

    //private ProgressBar progressBar;
    private TextView textCalibrationStatus;
    private static String targetName;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SensorService.CALIBRATION_STATUS)) {
                textCalibrationStatus.setText("Collecting data...");

                textCalibrationStatus.setVisibility(View.VISIBLE);

            }
            if(intent.getAction().equals(SensorService.CALIBRATION_FINISHED)) {
                String status = intent.getExtras().getString(SensorService.CALIBRATION_STATUS);
                textCalibrationStatus.setText(status);

                View buttonFinishCalibration = findViewById(R.id.buttonFinishCalibration);
                buttonFinishCalibration.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        IntentFilter intentFilterProgress = new IntentFilter(SensorService.CALIBRATION_PROGRESS);
        IntentFilter intentFilterStatus = new IntentFilter(SensorService.CALIBRATION_STATUS);
        IntentFilter intentFilterFinished = new IntentFilter(SensorService.CALIBRATION_FINISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilterProgress);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilterStatus);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilterFinished);

//        progressBar = (ProgressBar) findViewById(R.id.progressBarCalibration);
        textCalibrationStatus = (TextView) findViewById(R.id.textCalibrationStatus);
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent(this, SensorService.class);
        stopService(intent);
        super.onDestroy();
    }

    public void collectTargetName(View v) {
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_target_name_title);
        builder.setView(R.layout.dialog_target_name);
        builder.setNegativeButton(R.string.dialog_target_name_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) dialog.findViewById(R.id.editTargetName);
                String targetName = editText.getText().toString();
                if (targetName.trim().isEmpty()) {
                    editText.setError("Cannot be empty.");
                } else {
                    dialog.dismiss();
                    startCalibration(targetName);
                }
            }
        });
    }

    public void showGuidelines(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_guidelines_message).setTitle(R.string.dialog_guidelines_title);
        builder.setNegativeButton(R.string.dialog_guidelines_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void startCalibration(String targetName) {
        this.targetName = targetName;

        View buttonStartCalibration = findViewById(R.id.buttonStartCalibration);
        buttonStartCalibration.setVisibility(View.GONE);
        View buttonShowGuidelines = findViewById(R.id.buttonShowGuidelines);
        buttonShowGuidelines.setVisibility(View.GONE);

        textCalibrationStatus.setVisibility(View.VISIBLE);
        View buttonFinishCalibration = findViewById(R.id.buttonFinishCalibration);
        buttonFinishCalibration.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, SensorService.class);
        intent.putExtra(SensorService.SERVICE_TYPE, SensorService.TYPE_CALIBRATION);
        intent.putExtra(SensorService.TARGET_NAME, targetName);

        startService(intent);
    }

    public void finishCalibration(View v) {
        Intent intent = new Intent(this, SensorService.class);
        stopService(intent);
        super.onDestroy();
        finish();
    }

}


