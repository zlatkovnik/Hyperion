package com.example.protectorsofastrax



import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protectorsofastrax.data.Card
import com.google.firebase.storage.FirebaseStorage
import java.net.HttpURLConnection
import java.net.URL


class CardsAdapter( private val cards: ArrayList<Card>,private val select:Boolean) :
    RecyclerView.Adapter<CardsAdapter.ViewHolder>() {

    lateinit private var  context: Context
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val heroName: TextView
        val heroImage: ImageView
        val classImage: ImageView
        val heroPower: TextView
        val cardselected: Button

        init {
            // Define click listener for the ViewHolder's View.
            heroImage=view.findViewById(R.id.card_hero_img)
            heroName = view.findViewById(R.id.card_heroName_txt)
            classImage = view.findViewById(R.id.card_class_img)
            heroPower = view.findViewById(R.id.card_power_txt)
            cardselected=view.findViewById(R.id.card_select_btn)
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
//        val item = dataSet.get(holder.absoluteAdapterPosition)
        val item= cards[viewHolder.adapterPosition] as Int
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
        context=viewHolder.cardselected.context
        if(select)
        {
            viewHolder.cardselected.isEnabled=true
            viewHolder.cardselected.visibility=View.VISIBLE
            viewHolder.cardselected.setOnClickListener {
            val inte=Intent(context,MyCardsActivity::class.java)
                inte.putExtra("selected",cards[position].id)
            }
        }
    }



    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = cards.size


}

