package com.example.protectorsofastrax



import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.Uri.parse
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.protectorsofastrax.data.Card
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class CardsAdapter( private val cards: ArrayList<Card>) :
    RecyclerView.Adapter<CardsAdapter.ViewHolder>() {


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heroName: TextView
        val heroImage: ImageView
        val classImage: ImageView
        val heroPower: TextView


        init {
            // Define click listener for the ViewHolder's View.
            heroImage=view.findViewById(R.id.card_hero_img)
            heroName = view.findViewById(R.id.card_heroName_txt)
            classImage = view.findViewById(R.id.card_class_img)
            heroPower = view.findViewById(R.id.card_power_txt)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        viewHolder.textView.text = dataSet[position]

        FirebaseStorage.getInstance().reference
            .child("cards/" + cards[position].picture)
            .downloadUrl
            .addOnSuccessListener { uri ->
                val connection: HttpURLConnection =
                    URL(uri.toString()).openConnection() as HttpURLConnection
                connection.connect()
                val x: Bitmap = BitmapFactory.decodeStream(connection.inputStream)
                viewHolder.heroImage.setImageBitmap(x)
            }

        FirebaseStorage.getInstance().reference
            .child("classes/" + cards[position].clas.toLowerCase() + ".png")
            .downloadUrl
            .addOnSuccessListener { uri ->
                val connection: HttpURLConnection =
                    URL(uri.toString()).openConnection() as HttpURLConnection
                connection.connect()
                val x: Bitmap = BitmapFactory.decodeStream(connection.inputStream)
                viewHolder.classImage.setImageBitmap(x)
            }

        viewHolder.heroName.text = cards[position].name
        viewHolder.heroPower.text = cards[position].power.toString()

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = cards.size

}

