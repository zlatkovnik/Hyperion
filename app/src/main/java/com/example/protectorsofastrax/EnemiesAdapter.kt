package com.example.protectorsofastrax


import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.Uri.parse
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.protectorsofastrax.data.Card
import com.example.protectorsofastrax.data.Enemy
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class EnemiesAdapter(private val enemies: ArrayList<Enemy>, val clickListener: OnEnemyItemClickListner) :
    RecyclerView.Adapter<EnemiesAdapter.ViewHolder>() {

    lateinit private var context: Context
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val power: TextView
        val picture: ImageView
//        val select: Button

        init {
            name = view.findViewById(R.id.enemy_name_txt)
            power = view.findViewById(R.id.enemy_power_txt)
            picture = view.findViewById(R.id.enemy_avatar_img)
//            select = view.findViewById(R.id.enemy_select_btn)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.enemy, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        context = viewHolder.picture.context
        FirebaseStorage.getInstance().reference
            .child("enemies/" + enemies[position].picture)
            .downloadUrl
            .addOnSuccessListener { uri ->
//                val connection: HttpURLConnection =
//                    URL(uri.toString()).openConnection() as HttpURLConnection
//                connection.connect()
//                val x: Bitmap = BitmapFactory.decodeStream(connection.inputStream)
//                viewHolder.picture.setImageBitmap(x)
                Glide.with(context)
                    .load(uri)
                    .into(viewHolder.picture)
            }

        viewHolder.name.text = enemies[position].name
        viewHolder.power.text = enemies[position].power.toString()

        viewHolder.itemView.setOnClickListener {
            clickListener.onItemClick(enemies[position], position)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = enemies.size
}

interface OnEnemyItemClickListner{
    fun onItemClick(item: Enemy, position: Int)
}
