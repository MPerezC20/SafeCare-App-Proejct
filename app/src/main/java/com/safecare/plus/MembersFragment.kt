package com.safecare.plus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MembersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lógica para el botón de retroceso
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // Reemplazar el fragmento actual con HomeFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            // Actualizar el ítem seleccionado en la barra de navegación inferior
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        // Configuración del RecyclerView
        val membersRecyclerView: RecyclerView = view.findViewById(R.id.members_recycler_view)
        membersRecyclerView.layoutManager = LinearLayoutManager(context)

        val members = listOf(
            Member("Usuario 1", R.drawable.placeholder_member_1),
            Member("Usuario 2", R.drawable.placeholder_member_2)
        )

        val adapter = MembersAdapter(members)
        membersRecyclerView.adapter = adapter
    }
}
