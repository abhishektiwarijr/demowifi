package com.example.cqs.demowifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CustomWifiListActivity extends AppCompatActivity implements OnItemClickListener, View.OnClickListener {
    private TextView mainText;
    private RecyclerView wifiDeviceList;
    private WifiManager mainWifi;
    private CustomWifiListActivity.WifiReceiver receiverWifi;
    private List<ScanResult> wifiList;
    private StringBuilder sb;
    private String ssid_selected = null;
    private Context context = null;

    private static final int MY_PERMISSIONS_REQUEST = 1;

    Button connect_btn;
    private List<String> mDeviceList;
    private WifiListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        setTitle("Choose Switchbox");
        connect_btn = (Button) findViewById(R.id.btnScan);
        connect_btn.setOnClickListener(this);
        wifiDeviceList = (RecyclerView) findViewById(R.id.rvWifiList);
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mainWifi.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            mainWifi.setWifiEnabled(true);
        }

        mDeviceList = new ArrayList<String>();
        wifiDeviceList.setLayoutManager(new LinearLayoutManager(this));
        wifiDeviceList.setHasFixedSize(true);
        wifiDeviceList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        mAdapter = new WifiListAdapter(mDeviceList, CustomWifiListActivity.this);
        wifiDeviceList.setAdapter(mAdapter);
    }


    public void connectToHost(Context context, String host, String password) {
        mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wc = new WifiConfiguration();

        wc.SSID = host;
        wc.preSharedKey = password;

        int netId = mainWifi.addNetwork(wc);

        try {
            mainWifi.enableNetwork(netId, true);
            mainWifi.setWifiEnabled(true);

            System.out.println("enabled network");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    public void connectToHost2(Context context, String host, String password) {
        mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wc = new WifiConfiguration();

        wc.SSID = host;
        wc.preSharedKey = password;
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);


        int netId = mainWifi.addNetwork(wc);


        try {
            mainWifi.enableNetwork(netId, true);
            mainWifi.setWifiEnabled(true);

            System.out.println("enabled network");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onItemClick(int position) {
        final String ssid = '"' + wifiList.get(position).SSID + '"';
        final String mac = wifiList.get(position).BSSID;
        String pass = "";
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomWifiListActivity.this);
        alertDialogBuilder.setView(promptView);
        final EditText ssid_et = (EditText) promptView.findViewById(R.id.etSSID);
        final EditText pass_et = (EditText) promptView.findViewById(R.id.etPassword);
        ssid_et.setText(wifiList.get(position).SSID);
        ssid_et.setSelection(wifiList.get(position).SSID.length());
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String ssid = '"' + wifiList.get(position).SSID + '"';
                        String password = '"' + pass_et.getText().toString() + '"';

                        System.out.println("ssid: " + ssid);
                        System.out.println("password: " + password);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            connectToHost(getApplicationContext(), ssid, password);
                        } else {
                            connectToHost2(getApplicationContext(), ssid, password);
                        }

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
    }

    @Override
    public void onClick(View view) {
        if (mainWifi != null)
            mainWifi.startScan();
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                sb = new StringBuilder();
                wifiList = mainWifi.getScanResults();
                sb.append("\n Number Of Wifi connections :" + wifiList.size() + "\n\n");

                List<String> deviceList = new ArrayList<String>();
                for (int i = 0; i < wifiList.size(); i++) {
                    sb.append(new Integer(i + 1).toString() + ". ");
                    sb.append((wifiList.get(i)).toString());
                    sb.append("\n\n");
                    deviceList.add(wifiList.get(i).SSID);
                }

                mDeviceList.clear();
                mDeviceList.addAll(deviceList);
                mAdapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new CustomWifiListActivity.WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }

    private void getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(CustomWifiListActivity.this, "version>=marshmallow", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(CustomWifiListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CustomWifiListActivity.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(CustomWifiListActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST);
            } else {
                Toast.makeText(CustomWifiListActivity.this, "location turned on", Toast.LENGTH_SHORT).show();
                mainWifi.startScan();
            }
        } else {
            Toast.makeText(CustomWifiListActivity.this, "scanning", Toast.LENGTH_SHORT).show();
            mainWifi.startScan();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CustomWifiListActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                mainWifi.startScan();
            } else {
                Toast.makeText(CustomWifiListActivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}