package spiridonov.no_to_garbage

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_description.*

class DescriptionFragment : Fragment() {

    private lateinit var msp: SharedPreferences
    private val TAG = "DocSnippets"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_description, container, false)
        var mainCategory = ""
        msp = this.requireActivity().getSharedPreferences("things", Context.MODE_PRIVATE)
        if (msp.contains("thing")) mainCategory = msp.getString("thing", "").toString()
        val db = Firebase.firestore
        db.collection(mainCategory).get().addOnSuccessListener { result ->
            for (document in result) {
                desc_head.text = "${document.getString("Head")}"
                desc_body.text = "${document.getString("Body")}"

            }
        }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        return root
    }
}