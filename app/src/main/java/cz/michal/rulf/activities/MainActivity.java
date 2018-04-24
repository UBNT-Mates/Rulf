package cz.michal.rulf.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import cz.michal.rulf.BuildConfig;
import cz.michal.rulf.R;
import cz.michal.rulf.api.RetrofitClient;
import cz.michal.rulf.model.Server;
import cz.michal.rulf.model.Token;
import cz.michal.rulf.utils.SpeedTestTask;
import cz.michal.rulf.utils.UIHelper;
import cz.michal.rulf.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private FusedLocationProviderClient fusedLocationClient;
    private View container;

    private SpeedTestTask speedTest;
    private RetrofitClient retrofitClient;
    private OkHttpClient.Builder httpClient;

    private TextView serverText;
    private TextView pingText;
    private TextView downloadText;
    private Button processButton;

    private List<Server> serverList;
    private Location location;
    private Server finalServer;
    private String apiToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.main_activity_container);

        httpClient = new OkHttpClient.Builder();
        retrofitClient = new RetrofitClient(httpClient.build());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        serverText = findViewById(R.id.server_text);
        pingText = findViewById(R.id.ping_text);
        downloadText = findViewById(R.id.download_text);
        processButton = findViewById(R.id.process_button);
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                measureDownloadSpeed();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLocation();
        }
    }

    public void measureDownloadSpeed() {
        retrofitClient.api.getToken().enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                apiToken = response.body().getToken();
                retrofitClient.endpoint.getDownloadSpeed(apiToken).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                        if (processButton.getText().equals(getString(R.string.process_button_start))) {
                            speedTest = new SpeedTestTask(MainActivity.this);
                            speedTest.execute(response.body());
                            processButton.setText(R.string.process_button_stop);
                        } else {
                            speedTest.cancel(true);
                            processButton.setText(R.string.process_button_start);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {

            }
        });
    }

    public void getServers() {
        retrofitClient.api.getToken().enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                apiToken = response.body().getToken();
                retrofitClient.api.getServers(apiToken).enqueue(new Callback<List<Server>>() {
                    @Override
                    public void onResponse(Call<List<Server>> call, Response<List<Server>> response) {
                        serverList = Utils.getServerDistance(location, response.body());
                        finalServer = Utils.getServerLatency(serverList);
                        serverText.setText(finalServer.getCity() + "/" + finalServer.getCountry() + " (" + finalServer.getProvider() + ")");
                        pingText.setText((int) finalServer.getLatency() + getString(R.string.ping_text_unit));
                        retrofitClient = new RetrofitClient(httpClient.build(), finalServer.getUrl());
                        processButton.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<List<Server>> call, Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {

            }
        });
    }

    @SuppressWarnings("MissingPermission")
    private void getLocation() {
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            location = task.getResult();
                            resetUI();
                            getServers();
                        } else {
                            Log.w(TAG, "getLocation:exception", task.getException());
                            UIHelper.showSnackbar(container, getString(R.string.no_location_detected));
                        }
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            UIHelper.showSnackbar(findViewById(android.R.id.content), getString(R.string.permission_rationale), getString(android.R.string.ok),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                UIHelper.showSnackbar(findViewById(android.R.id.content), getString(R.string.permission_denied_explanation), getString(R.string.settings),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void resetUI(){
        serverText.setText(R.string.server_text_getting_data);
        pingText.setText(R.string.ping_text_default);
        downloadText.setText(R.string.download_text_default);
        processButton.setEnabled(false);
    }

    public void updateDownloadSpeed(double speed) {
        downloadText.setText(String.format("%.2f", speed / 1000) + " Mbps");
    }

    public void updateButtonText() {
        processButton.setText(R.string.process_button_start);
    }
}