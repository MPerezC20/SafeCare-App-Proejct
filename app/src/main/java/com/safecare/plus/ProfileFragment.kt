package com.safecare.plus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val backArrow = view.findViewById<ImageView>(R.id.back_arrow)
        backArrow.setOnClickListener {
            (activity as? HomeActivity)?.navigateToHome()
        }

        val editButton = view.findViewById<Button>(R.id.edit_button)
        editButton.setOnClickListener {
            (activity as? HomeActivity)?.navigateToEditProfile()
        }

        return view
    }
}
