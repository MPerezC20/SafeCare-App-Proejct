package com.safecare.plus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val phone: String = "",
    val profileImageUrl: String? = null
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        if (_userProfile.value != null) return // Data already loaded

        _isLoading.value = true
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "Sin Nombre"
                        val email = firebaseUser.email ?: ""
                        val role = document.getString("profile") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val profileImageUrl = document.getString("profileImageUrl")
                        _userProfile.value = UserProfile(name, email, role, phone, profileImageUrl)
                    } else {
                        _userProfile.value = null // User not found in Firestore
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    _userProfile.value = null // Error loading
                    _isLoading.value = false
                }
        } else {
            _userProfile.value = null // No user logged in
            _isLoading.value = false
        }
    }
}
