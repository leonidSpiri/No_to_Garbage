package spiridonov.no_to_garbage

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MapThingFragment : Fragment() {
    private lateinit var msp: SharedPreferences
    private val KEY_THING = "thing"
    private val callback = OnMapReadyCallback { googleMap -> mapCreate(googleMap) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mapview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun mapCreate(googleMap: GoogleMap) {
        msp = this.requireActivity().getSharedPreferences("things", Context.MODE_PRIVATE)
        var mainCategory = ""
        if (msp.contains(KEY_THING)) mainCategory = msp.getString(KEY_THING, "").toString()

        val db = Firebase.firestore
        db.collection(mainCategory).get().addOnSuccessListener { result ->
            for (document in result) {
                var geoPoint = document.getGeoPoint("Map")
                val point = LatLng(geoPoint!!.latitude, geoPoint.longitude)
                googleMap.addMarker(
                    MarkerOptions().position(point).title("${document.getString("Maptxt")}")
                )
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(point).zoom(13f).build()
                )
                googleMap.animateCamera(cameraUpdate)
                var mapp = 2
                geoPoint = document.getGeoPoint("Map$mapp")
                while (geoPoint != null) {
                    val point = LatLng(geoPoint.latitude, geoPoint.longitude)
                    googleMap.addMarker(
                        MarkerOptions().position(point)
                            .title("${document.getString("Maptxt$mapp")}")
                    )
                    geoPoint = document.getGeoPoint("Map$mapp")
                    mapp++
                }
            }
        }.addOnFailureListener { exception -> }
    }
}