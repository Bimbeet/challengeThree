package com.interapt.challengethree

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.interapt.challengethree.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val apiKey = ""
    private lateinit var placesClient: PlacesClient
    private lateinit var requestQueue: RequestQueue
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        val network = BasicNetwork(HurlStack())
        requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        Places.initialize(this, apiKey)
        placesClient = Places.createClient(this)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
        if (requestCode == 99) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> binding.cLayoutMain.isClickable = true
                PackageManager.PERMISSION_DENIED -> Log.e("debugy", "failed permission grant")
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitude = location.latitude
            longitude = location.longitude
            Log.i("debugy", "Latitute: $latitude ; Longitute: $longitude")
            preformPlaceRequest()
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    private var proxRadius = 809
    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()
    fun onSearchTouch(v: View?) {
        if (binding.radiusInput.text.toString().isNotEmpty()) {
            proxRadius = if (1609 * binding.radiusInput.text.toString().toInt() < 46000) {
                1609 * binding.radiusInput.text.toString().toInt()
            } else {
                40000
            }
            Log.d("debugy", "PROXIMITY_RADIUS in meters = $proxRadius")
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("debugy", "failed location permission check!")
            return
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                50000L,
                0f,
                locationListener
            )
        } catch (ex: SecurityException) {
            Log.e("debugy", "Security Exception, no location available")
        }
    }

    var placeIndex = 0
    var placeReqResults: JSONObject? = null
    private fun preformPlaceRequest() {
        val googlePlacesUrl =
            StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude)
        googlePlacesUrl.append("&radius=").append(proxRadius)
        googlePlacesUrl.append("&types=restaurant&keyword=restaurant&sensor=true")
        googlePlacesUrl.append("&key=$apiKey")
        Log.d("debugy", "Request = $googlePlacesUrl")

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,
            googlePlacesUrl.toString(),
            null,
            { response ->
//                Log.d("debugy", "Response: %s".format(response.toString()))
                placeReqResults = response
                parseLocationResult(response)
            },
            { error ->
                Log.e("debugy", "onErrorResponse: Error = $error")
                Log.e("debugy", "onErrorResponse: Error = " + error.message)
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    var finalAddress: String? = null
    private var currentPlacesArray = JSONArray()
    private var doOnce = false
    private fun parseLocationResult(result: JSONObject) {
        var name: String? = null
        var address: String? = null
        var placeID: String? = null
        try {
            val placesResultsArray = result.getJSONArray("results")
            Log.i("debugy", "placesResultsArray : $placesResultsArray")
            if (!doOnce) {
                for (resultsIndex in 0 until placesResultsArray.length()) {
//                    Log.d("debugy", "placesResultsArray.getJSONObject(i) - " + placesResultsArray.getJSONObject(resultsIndex).toString())
                    if (!placesResultsArray.getJSONObject(resultsIndex).getString("name")
                            .contains("test")
                    ) {
                        currentPlacesArray.put(placesResultsArray.getJSONObject(resultsIndex))
                    }

//                    for (currentIndex in 0 until currentPlacesArray.length()) {
//                        for (nextIndex in 1 until currentPlacesArray.length()) {
//                            if (currentPlacesArray.getJSONObject(currentIndex).getString("name").equals(currentPlacesArray.getJSONObject(nextIndex).getString("name"))) {
//                                currentPlacesArray.remove(nextIndex)
//                            }
//                        }
//                    }
                }
                doOnce = true
//                Log.d("debugy", "prevPlacesArray : $currentPlacesArray")
            }
            try {
                name = currentPlacesArray.getJSONObject(placeIndex).getString("name")
                Log.i("debugy", "JSON object: name - $name")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            try {
                placeID = currentPlacesArray.getJSONObject(placeIndex).getString("place_id")
                Log.i("debugy", "JSON object: place_id - $placeID")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            try {
                address = currentPlacesArray.getJSONObject(placeIndex).getString("vicinity")
                Log.i("debugy", "JSON object: address - $address")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if (placeID != null) {
            val placeRequest = FetchPlaceRequest.newInstance(
                placeID,
                Collections.singletonList(Place.Field.PHOTO_METADATAS)
            )
            placesClient.fetchPlace(placeRequest)
                .addOnSuccessListener(OnSuccessListener { response ->
                    val place = response.place
                    val metadata = place.photoMetadatas
                    if (metadata == null || metadata.isEmpty()) {
                        Log.w("debugy", "No photo metadata.")
                        binding.placePhoto.visibility = View.VISIBLE
                        Toast.makeText(this, "No Photo Found", Toast.LENGTH_LONG).show()
                        return@OnSuccessListener
                    }
                    Log.i("debugy", "current place photo Metadata : $metadata")
                    val photoMetadata = metadata[0]
                    val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()
                    placesClient.fetchPhoto(photoRequest)
                        .addOnSuccessListener { fetchPhotoResponse ->
                            val bitmap = fetchPhotoResponse.bitmap
                            binding.placePhoto.setImageBitmap(bitmap)
                            binding.placePhoto.visibility = View.VISIBLE
                        }.addOnFailureListener { exception ->
                            Log.e("debugy", "Place not found: " + exception.message)
                        }

                })
            if (name != null) {
                binding.searchButton.visibility = View.INVISIBLE
                binding.radiusInput.visibility = View.INVISIBLE
                binding.textShow.text = name

                try {
                    if (currentPlacesArray.getJSONObject(placeIndex++) != null) {
                        binding.nextPlaceButton.visibility = View.VISIBLE
                    } else {
                        binding.nextPlaceButton.visibility = View.INVISIBLE
                    }
//                    if (placeIndex - 1 < 0) {
//                        binding.prevPlaceButton.setVisibility(View.INVISIBLE)
//                    } else {
//                        binding.prevPlaceButton.setVisibility(View.VISIBLE)
//                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if (address != null) {
                    finalAddress = address
                    binding.addressDisplay.text = finalAddress
                    binding.addressDisplay.visibility = View.VISIBLE
//                    binding.mapButton.setVisibility(View.INVISIBLE)
                } else {
                    binding.addressDisplay.visibility = View.INVISIBLE
//                    binding.mapButton.setVisibility(View.VISIBLE)
                }
            }
        }
    }

    fun nextPlace(view: View?) {
        placeIndex++
        parseLocationResult(placeReqResults!!)
    }
}