package com.kamina.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class AvatarAdapter(
    private val avatars: List<String>, // Use List<String> for avatar paths
    private val onClick: (String) -> Unit // Use String to pass the avatar URL on click
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    class AvatarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatarImage: ImageView = view.findViewById(R.id.avatarImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.avatar_item, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        val avatarUrl = avatars[position]
        holder.avatarImage.load(avatarUrl) // Load avatar URL using Coil

        // Handle avatar click
        holder.itemView.setOnClickListener {
            onClick(avatarUrl) // Pass the avatar URL to the onClick handler
        }
    }

    override fun getItemCount(): Int {
        return avatars.size
    }
}
