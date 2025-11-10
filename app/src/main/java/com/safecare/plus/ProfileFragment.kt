package com.safecare.plus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    private lateinit var profileName: TextView
    private lateinit var profileRole: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var profileImage: CircleImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileName = view.findViewById(R.id.profile_name)
        profileRole = view.findViewById(R.id.profile_role)
        emailText = view.findViewById(R.id.email_text)
        phoneText = view.findViewById(R.id.phone_text)
        profileImage = view.findViewById(R.id.profile_image)

        val backArrow = view.findViewById<ImageView>(R.id.back_arrow)
        backArrow.setOnClickListener {
            (activity as? HomeActivity)?.navigateToHome()
        }

        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage.setImageResource(R.drawable.icon5)

        viewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            if (userProfile != null) {
                profileName.text = userProfile.name
                profileRole.text = userProfile.role
                emailText.text = userProfile.email
                phoneText.text = userProfile.phone
            } else {
                // Handle case where user profile is not available
                profileName.text = "Sin Nombre"
                profileRole.text = ""
                emailText.text = ""
                phoneText.text = ""
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can show a progress bar here while data is loading
        }
    }
}
