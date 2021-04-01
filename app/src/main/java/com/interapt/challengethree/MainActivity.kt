package com.interapt.challengethree

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.interapt.challengethree.databinding.ActivityMainBinding
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val apiKey = ""
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Places.initialize(applicationContext, apiKey)
        placesClient = Places.createClient(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext!!)

        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            binding.cLayoutMain.isClickable = true
        } else {
            singleLocationRequest(null)
        }
    }

    fun singleLocationRequest(@Nullable view: View?) {
        ActivityCompat.requestPermissions(
            this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            99
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            99 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    binding.cLayoutMain.isClickable = true
                }
//                else {
//                    showInContextUI()
//                }
                return
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var PROXIMITY_RADIUS = 809
    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()
    fun onSearchTouch(v: View?) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    Log.d("debugy", "location : $location")
                    // get latitude , longitude and other info from this
                }

            }


//        if (!binding.radiusInput.getText().toString().isEmpty()) {
//            if (1609 * binding.radiusInput.getText().toString().toInt() < 50000) {
//                PROXIMITY_RADIUS = 1609 * binding.radiusInput.getText().toString().toInt()
//            } else {
//                PROXIMITY_RADIUS = 46000
//            }
//            Log.d("debugy", "PROXIMITY_RADIUS in meters = $PROXIMITY_RADIUS")
//        }
//        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        try {
//            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//        } catch (ex: Exception) {
//            Log.d("debugy", "location_services Exception throw : $ex")
//        }
//
//        //        try {
////            lm.requestSingleUpdate( LocationManager.NETWORK_PROVIDER, new LocationListener(), null );
////        } catch ( SecurityException e ) { e.printStackTrace(); }
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50000, 0, LocationListener {  })
//        val currlocation: Location? = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//
//        Log.d("debugy", "Lat / long = " + currlocation.getLatitude().toString() + " / " + currlocation.getLongitude())
//        latitude = currlocation.getLatitude()
//        longitude = currlocation.getLongitude()
//        //        longitude = -85.773934499646;
////        latitude = 38.200575703571;
//        preformPlaceRequest()
    }
}