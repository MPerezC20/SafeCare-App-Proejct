package com.safecare.plus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Member(
    val name: String,
    val role: String,
    val age: String,
    val gender: String,
    val disability: String,
    val photoResId: Int
)

class MembersAdapter(private val members: List<Member>) :
    RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.memberPhoto.setImageResource(member.photoResId)
        holder.memberName.text = member.name
        holder.memberRole.text = member.role
        holder.memberAge.text = "Edad: ${member.age}"
        holder.memberGender.text = "Sexo: ${member.gender}"
        holder.memberDisability.text = "Discapacidad: ${member.disability}"
    }

    override fun getItemCount() = members.size

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberPhoto: ImageView = itemView.findViewById(R.id.member_image)
        val memberName: TextView = itemView.findViewById(R.id.member_name)
        val memberRole: TextView = itemView.findViewById(R.id.member_role)
        val memberAge: TextView = itemView.findViewById(R.id.member_age)
        val memberGender: TextView = itemView.findViewById(R.id.member_gender)
        val memberDisability: TextView = itemView.findViewById(R.id.member_disability)
    }
}