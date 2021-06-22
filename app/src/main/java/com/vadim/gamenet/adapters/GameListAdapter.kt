package com.vadim.gamenet.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.models.Game

class GameListAdapter(
    context: Context,
    private val dataSet: ArrayList<Game>
) :
    RecyclerView.Adapter<GameListAdapter.ViewHolder>() {

    private var context: Context = context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        var gameImage: ImageView = v.findViewById(R.id.gameRow_IMG_gamePhoto)
        var gameName: TextView = v.findViewById(R.id.gameRow_LBL_gameName)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_game_list, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        viewHolder.gameName.text = temp.name
        Glide.with(context)
            .load(dataSet[position].imageUrl)
            .into(viewHolder.gameImage)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
