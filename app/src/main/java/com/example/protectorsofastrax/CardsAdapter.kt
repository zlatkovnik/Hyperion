package com.example.protectorsofastrax

import android.net.Uri.parse
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import android.net.Uri
import android.net.Uri.EMPTY


class CardsAdapter( private val img: Array<String>) :
    RecyclerView.Adapter<CardsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val textView: TextView
        var heroImage: ImageView

        init {
            // Define click listener for the ViewHolder's View.
            heroImage=view.findViewById(R.id.card_hero_img)
//            textView = view.findViewById(R.id.card_heroName_txt)
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
        viewHolder.heroImage.setImageURI(parse(img[position]))
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = img.size

}

