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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


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
        if (msp.contains(KEY_THING)) {
            val mainCategory = msp.getString(KEY_THING, "").toString()
            val firebaseDate = FirebaseDatabase.getInstance()
            val rootReference = firebaseDate.reference
            val garbageReference = rootReference.child("GarbageInformation").child(mainCategory)


            garbageReference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trueMapNumber: Long = (snapshot.childrenCount - 2) / 2

                    var mapNumber = 0
                    while (mapNumber < trueMapNumber) {
                        var hint = ""
                        val geopointReference = garbageReference.child("map$mapNumber")
                        val hintReference = garbageReference.child("mapHint$mapNumber")

                        hintReference.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}

                            override fun onDataChange(snapshot: DataSnapshot) {
                                hint = snapshot.getValue(String::class.java)!!
                            }
                        })

                        geopointReference.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val value: List<String> =
                                    snapshot.getValue(String::class.java)!!.split(",")

                                createMarker(
                                    googleMap,
                                    LatLng(value[0].toDouble(), value[1].toDouble()),
                                    hint
                                )

                            }
                        })

                        mapNumber++
                    }
                }
            })

        }
    }

    private fun createMarker(googleMap: GoogleMap, point: LatLng, hint: String) {
        googleMap.addMarker(MarkerOptions().position(point).title(hint))
        googleMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(point).zoom(12f).build()
            )
        )
    }


}