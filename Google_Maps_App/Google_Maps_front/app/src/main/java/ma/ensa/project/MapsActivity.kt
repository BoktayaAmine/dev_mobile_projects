package ma.ensa.project

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var mapSearchView: SearchView
    private lateinit var myLocation: ImageButton
    private lateinit var requestQueue: RequestQueue
    private val url = "http://192.168.0.138:8090/api/positions" // Votre point de terminaison API

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapSearchView = findViewById(R.id.mapSearch)
        myLocation = findViewById(R.id.myLocationButton)
        requestQueue = Volley.newRequestQueue(applicationContext)

        // Initialiser la carte
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        myLocation.setOnClickListener {
            val latitude = 33.230873333333335
            val longitude = -8.527043333333333
            mMap?.apply {
                addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .title("My Position")
                )
                moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 20f))
            }
        }

        mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val location = mapSearchView.query.toString()
                if (location.isNotEmpty()) {
                    val geocoder = Geocoder(this@MapsActivity)
                    try {
                        val addressList = geocoder.getFromLocationName(location, 1)
                        if (!addressList.isNullOrEmpty()) {
                            val address = addressList[0]
                            val latLng = LatLng(address.latitude, address.longitude)
                            mMap?.apply {
                                addMarker(MarkerOptions().position(latLng).title(location))
                                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@MapsActivity, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()
    }

    private fun setUpMap() {
        // Récupérer des positions depuis le backend
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    for (i in 0 until response.length()) {
                        val position = response.getJSONObject(i)
                        val latitude = position.getDouble("latitude")
                        val longitude = position.getDouble("longitude")

                        // Ajouter un marqueur pour chaque position
                        mMap?.addMarker(
                            MarkerOptions()
                                .position(LatLng(latitude, longitude))
                                .title("Position ${i + 1}")
                        )
                    }
                    if (response.length() > 0) {
                        val firstPosition = response.getJSONObject(response.length() - 1)
                        val latitude = firstPosition.getDouble("latitude")
                        val longitude = firstPosition.getDouble("longitude")
                        mMap?.apply {
                            addMarker(
                                MarkerOptions()
                                    .position(LatLng(latitude, longitude))
                                    .title("My Position")
                            )
                            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 20f))
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this@MapsActivity, "Error parsing positions", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("MapsActivity", "Error fetching positions: $error")
                Toast.makeText(this@MapsActivity, "Error fetching positions", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the request queue
        requestQueue.add(jsonArrayRequest)
    }
}
