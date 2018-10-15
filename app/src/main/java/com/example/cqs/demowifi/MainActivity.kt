package com.example.cqs.demowifi

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionScanResultsListener
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.thanosfisherman.wifiutils.wifiScan.ScanResultsListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ScanResultsListener, View.OnClickListener, OnItemClickListener, ConnectionSuccessListener, ConnectionScanResultsListener {

    override fun onConnectWithScanResult(scanResults: MutableList<ScanResult>): ScanResult? {
        return scanResults[mPos]
    }

    override fun isSuccessful(isSuccess: Boolean) {
        if (isSuccess) Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
        else Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show()
    }

    private var mPos = -1

    override fun onItemClick(position: Int) {
        mPos = position
        openDialog()
    }

    private fun openDialog() {
        val li = LayoutInflater.from(this)
        val promptsView = li.inflate(R.layout.custom_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView)
        val userInput = promptsView.findViewById(R.id.etPassword) as EditText
        val userWiFiName = promptsView.findViewById(R.id.etSSID) as EditText
        userWiFiName.setText(mWifiList[mPos].SSID)
        userWiFiName.setSelection(mWifiList[mPos].SSID.length)

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Connect",
                        DialogInterface.OnClickListener { dialog, id ->
                            // get user input and set it to result
                            // edit text
                            WifiUtils.withContext(applicationContext)
                                    .connectWithScanResult(userInput.text.toString().trim(), this)
                                    .setTimeout(30000)
                                    .onConnectionResult(MainActivity@ this)
                                    .start()
                        })
                .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

        // create alert dialog
        val alertDialog = alertDialogBuilder.create()
        // show it
        alertDialog.show()
    }

    override fun onClick(p0: View?) {
        WifiUtils.withContext(applicationContext).scanWifi(this).start()
    }

    override fun onScanResults(scanResults: MutableList<ScanResult>) {
        progress.visibility = View.GONE

        mWifiList.clear()
        mWifiList.addAll(scanResults)
//        Toast.makeText(this, scanResults[0].SSID, Toast.LENGTH_SHORT).show()
        mAdapter.notifyDataSetChanged()
    }

    private lateinit var wifi: WifiManager

    private lateinit var mWifiList: MutableList<ScanResult>

    private val MY_PERMISSIONS_REQUEST_CODE: Int = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifi.isWifiEnabled) {
            Toast.makeText(applicationContext, "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show()
            wifi.isWifiEnabled = true
        }
        mWifiList = ArrayList<ScanResult>()
        cookDataForRecycler()

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_CODE)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            progress.visibility = View.VISIBLE
            // Permission has already been granted
            WifiUtils.withContext(applicationContext).scanWifi(this).start()
            wifi()
        }
        btnScan.setOnClickListener(this)
    }

    private fun wifi() {
        val connection_manager: ConnectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val request: NetworkRequest.Builder = NetworkRequest.Builder()
            request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            connection_manager.registerNetworkCallback(request.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    super.onAvailable(network)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        connection_manager.bindProcessToNetwork(network)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ConnectivityManager.setProcessDefaultNetwork(network)
                        }
                    }
                }
            })
        }
    }

    private lateinit var mAdapter: WifiListAdapter

    private fun cookDataForRecycler() {
        rvWifiList.layoutManager = LinearLayoutManager(this)
        rvWifiList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvWifiList.setHasFixedSize(true)
        mAdapter = WifiListAdapter(mWifiList, this)
        rvWifiList.adapter = mAdapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    progress.visibility = View.VISIBLE
                    WifiUtils.withContext(applicationContext).scanWifi(this).start()
                    wifi()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_CODE)
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}

/*NetworkRequest.Builder builder;
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    builder = new NetworkRequest.Builder();
    //set the transport type do WIFI
    builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

    connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.RELEASE.equalsIgnoreCase("6.0")) {
                    if (!Settings.System.canWrite(mActivity)) {
                        Intent goToSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        goToSettings.setData(Uri.parse("package:" + mActivity.getPackageName()));
                        mActivity.startActivity(goToSettings);
                    }
                }
                connectivityManager.bindProcessToNetwork(null);
                if (mSsid.contains("my_iot_device-xxxxxxxxx")) {
                    connectivityManager.bindProcessToNetwork(network);
                } else {

                }

            } else {
                //This method was deprecated in API level 23
                ConnectivityManager.setProcessDefaultNetwork(null);
                if (mSsid.contains("my_iot_device-xxxxxxxxx")) {
                    ConnectivityManager.setProcessDefaultNetwork(network);
                } else {

                }

            }
            try {
                //do a callback or something else to alert your code that it's ok to send the message through socket now
            } catch (Exception e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
            connectivityManager.unregisterNetworkCallback(this);
        }
    });
}*/
