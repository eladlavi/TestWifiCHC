package il.co.expertigo.testwifiapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.os.Handler;
import android.os.PatternMatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 123;
    public static final String SSID = "GNSS-3377323";
    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
    };
    private WifiManager wifiManager;
    private final Handler handler = new Handler();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private TextView lblMessage;
    private Button btnSearchForGNSS, btnGetBattery;
    private Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean hasPermission = true;
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }
        btnSearchForGNSS = findViewById(R.id.btnSearchForGNSS);
        btnGetBattery = findViewById(R.id.btnGetBattery);
        if (!hasPermission) {
            requestPermissions(permissions, REQUEST_CODE);
        } else {
            //startSearchingForGnssWifi();
            btnSearchForGNSS.setEnabled(true);
        }
        lblMessage = findViewById(R.id.lblMessage);
    }

    private void startSearchingForGnssWifi() {
        /*
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }

        */
        connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkSpecifier networkSpecifier = new WifiNetworkSpecifier.Builder().setSsidPattern(new PatternMatcher("GNSS-", PatternMatcher.PATTERN_PREFIX)).build();


        NetworkRequest networkRequest =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        //.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)

                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(networkSpecifier)
                        .build();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                MainActivity.this.network = network;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lblMessage.setText("available");
                        btnSearchForGNSS.setEnabled(false);
                    }
                });

                Toast.makeText(MainActivity.this, "it worked!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                MainActivity.this.network = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lblMessage.setText("unavailable");
                        btnSearchForGNSS.setEnabled(true);
                    }
                });
                Toast.makeText(MainActivity.this, "failed...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                super.onBlockedStatusChanged(network, blocked);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                MainActivity.this.network = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lblMessage.setText("losing wifi");
                    }
                });

            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                MainActivity.this.network = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lblMessage.setText("wifi lost");
                        btnSearchForGNSS.setEnabled(true);
                    }
                });
            }

        };

        connectivityManager.requestNetwork(networkRequest, networkCallback, 30000);



    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        network = null;
    }


    private void scanSuccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();
        ScanResult found = null;
        for (ScanResult result : results) {
            if (result.SSID.equals(SSID)) {
                found = result;
                break;
            }
        }
        if (found != null) {


        }

    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermission = true;
        if (requestCode == REQUEST_CODE) {
            for (int i=0; i<permissions.length; i++) {
                int permission = grantResults[i];
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    hasPermission = false;
                    Toast.makeText(this, "no permissions: " + permissions[i], Toast.LENGTH_SHORT).show();

                }
            }
        }
        if (hasPermission){
            //startSearchingForGnssWifi();
            btnSearchForGNSS.setEnabled(true);
        }else{
            lblMessage.setText("no permissions..");
        }

    }

    public void btnSearchForGNSS(View view) {
        btnSearchForGNSS.setEnabled(false);
        lblMessage.setText("wait..");
        startSearchingForGnssWifi();
    }

    public void btnGetBatteryClicked(View view) {
        if(network == null) {
            lblMessage.setText("no GNSS network");
            btnSearchForGNSS.setEnabled(true);
            btnGetBattery.setEnabled(false);
            return;
        }
        lblMessage.setText("getting battery..");
        btnGetBattery.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) network.openConnection(new URL("http://192.168.1.1/power_status_get.cmd?urlStringId=admin&_=1732177460973"));
                    Http.sendHttpRequest(httpURLConnection, "application/json", "GET", new Http.HttpWork() {
                        @Override
                        public void work(HttpURLConnection connection) throws IOException {
                            int code;
                            if((code = connection.getResponseCode()) == 200){
                                byte[] buffer = new byte[1024];
                                int actuallyRead;
                                int pos = 0;
                                while ((actuallyRead = connection.getInputStream().read(buffer, pos, buffer.length - pos)) > 0){
                                    pos += actuallyRead;
                                }
                                final String response = new String(buffer, 0, pos);
                                Log.d("ELAD", response);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            JSONObject jsonBattery = new JSONObject(response);
                                            String volt_bat1 = jsonBattery.getString("volt_bat1");
                                            String volt_bat2 = jsonBattery.getString("volt_bat2");
                                            lblMessage.setText("bat1: " + volt_bat1 + ", bat2: " + volt_bat2);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            lblMessage.setText("invalid json");
                                        }
                                        btnGetBattery.setEnabled(true);
                                    }
                                });
                            }else{
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lblMessage.setText("failed getting battery.. code" + code);
                                        btnGetBattery.setEnabled(true);
                                    }
                                });
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            lblMessage.setText("failed getting battery..");
                            btnGetBattery.setEnabled(true);
                        }
                    });
                }finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        }).start();

    }
}