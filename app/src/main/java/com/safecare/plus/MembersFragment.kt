package com.safecare.plus

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MembersFragment : Fragment() {

    private lateinit var adapter: MembersAdapter
    private val membersList = mutableListOf<Member>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón de retroceso
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        // Configuración del RecyclerView
        val membersRecyclerView: RecyclerView = view.findViewById(R.id.members_recycler_view)
        membersRecyclerView.layoutManager = LinearLayoutManager(context)

        // Datos iniciales
        if (membersList.isEmpty()) {
            membersList.add(Member("Juan Pérez", "Familiar", "65", "Masculino", "Ninguna", R.drawable.placeholder_member_1))
            membersList.add(Member("María García", "Cuidadora", "40", "Femenino", "Ninguna", R.drawable.placeholder_member_2))
        }

        adapter = MembersAdapter(membersList)
        membersRecyclerView.adapter = adapter

        // Lógica para añadir miembro
        val addMemberButton: Button = view.findViewById(R.id.add_member_button)
        addMemberButton.setOnClickListener {
            showAddMemberDialog()
        }
    }

    private fun showAddMemberDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_member, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val inputName = dialogView.findViewById<EditText>(R.id.input_member_name)
        val inputRole = dialogView.findViewById<EditText>(R.id.input_member_role)
        val inputAge = dialogView.findViewById<EditText>(R.id.input_member_age)
        val inputGender = dialogView.findViewById<EditText>(R.id.input_member_gender)
        val inputDisability = dialogView.findViewById<EditText>(R.id.input_member_disability)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_member)

        btnSave.setOnClickListener {
            val name = inputName.text.toString()
            val role = inputRole.text.toString()
            val age = inputAge.text.toString()
            val gender = inputGender.text.toString()
            val disability = inputDisability.text.toString()

            if (name.isNotEmpty()) {
                val newMember = Member(
                    name,
                    role.ifEmpty { "Sin rol" },
                    age.ifEmpty { "0" },
                    gender.ifEmpty { "No especificado" },
                    disability.ifEmpty { "Ninguna" },
                    R.drawable.no_profile
                )
                membersList.add(newMember)
                adapter.notifyItemInserted(membersList.size - 1)
                dialog.dismiss()
            } else {
                inputName.error = "El nombre es obligatorio"
            }
        }

        dialog.show()
    }
}