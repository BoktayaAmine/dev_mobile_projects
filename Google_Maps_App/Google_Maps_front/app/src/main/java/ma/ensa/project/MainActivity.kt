package ma.ensa.project

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val PHONE_PERMISSION_REQUEST_CODE = 2
        private const val INSERT_URL = "http://192.168.0.138:8090/api/positions"
    }

    private var latitude = 0.0
    private var longitude = 0.0
    private var altitude = 0.0
    private var accuracy = 0f
    private lateinit var requestQueue: RequestQueue
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)
        initializeLocationServices()
        initializeButtonListener()
    }

    private fun initializeLocationServices() {
        if (hasLocationPermissions()) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    private fun initializeButtonListener() {
        findViewById<View>(R.id.button_show_map).setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                updateLocationData(location)
            }

            override fun onProviderEnabled(provider: String) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.provider_enabled, provider),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onProviderDisabled(provider: String) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.provider_disabled, provider),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000L, // Intervalle de 60 secondes
                150f,   // 150 mètres distance minimum
                locationListener
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationData(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        altitude = location.altitude
        accuracy = location.accuracy

        @SuppressLint("StringFormatMatches") val msg = getString(R.string.new_location, latitude, longitude, altitude, accuracy)
        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
        sendLocationToServer(latitude, longitude)
    }

    private fun sendLocationToServer(lat: Double, lon: Double) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("latitude", lat)
            jsonObject.put("longitude", lon)
            jsonObject.put("date", getCurrentTimestamp())
            jsonObject.put("imei", getDeviceIdentifier())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            INSERT_URL,
            jsonObject,
            { Toast.makeText(this, "Position saved successfully", Toast.LENGTH_SHORT).show() },
            { error ->
                Log.e("MainActivity", "Error: $error")
                Toast.makeText(this, "Error saving position: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceIdentifier(): String? {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                telephonyManager.imei
            else
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            else -> if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                telephonyManager.deviceId
            else
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ), LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Date())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> if (allPermissionsGranted(grantResults)) {
                startLocationUpdates()
            }
            PHONE_PERMISSION_REQUEST_CODE -> if (allPermissionsGranted(grantResults)) {
                getDeviceIdentifier()
            }
            else -> {
                Toast.makeText(this, "Required permissions were denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted(grantResults: IntArray): Boolean {
        return grantResults.all { it == PackageManager.PERMISSION_GRANTED }
    }
}
