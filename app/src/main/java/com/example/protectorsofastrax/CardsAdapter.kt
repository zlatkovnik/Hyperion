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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


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

        val x: Bitmap

        val connection: HttpURLConnection =
            URL(img[position].toString()).openConnection() as HttpURLConnection
        connection.connect()
        val input: InputStream = connection.inputStream

        x = BitmapFactory.decodeStream(input)
        viewHolder.heroImage.setImageBitmap(x)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = img.size

}

