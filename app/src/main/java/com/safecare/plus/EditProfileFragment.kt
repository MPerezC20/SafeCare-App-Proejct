package com.safecare.plus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        val backArrow = view.findViewById<ImageView>(R.id.back_arrow)
        backArrow.setOnClickListener {
            (activity as? HomeActivity)?.navigateToProfile()
        }

        return view
    }
}
