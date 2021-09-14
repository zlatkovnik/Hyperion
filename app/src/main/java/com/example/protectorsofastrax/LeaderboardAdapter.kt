package com.example.protectorsofastrax

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.protectorsofastrax.data.LeaderboardUser
import com.example.protectorsofastrax.data.User
import kotlin.collections.ArrayList


class LeaderboardAdapter(private val users: ArrayList<LeaderboardUser>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView
        val name: TextView
        val battlesWon: TextView

        init {
            rank = view.findViewById(R.id.leaderboard_rank_txt)
            name = view.findViewById(R.id.leaderboard_name_txt)
            battlesWon = view.findViewById(R.id.leaderboard_battles_txt)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.leaderboard_user, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.rank.text = (position + 1).toString() + "."
        viewHolder.name.text = users[position].name
        viewHolder.battlesWon.text = users[position].battlesWon.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = users.size
}