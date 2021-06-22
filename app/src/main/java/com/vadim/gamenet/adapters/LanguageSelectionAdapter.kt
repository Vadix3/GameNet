package com.vadim.gamenet.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R

class LanguageSelectionAdapter(
    context: Context,
    private val dataSet: ArrayList<String>
) :
    RecyclerView.Adapter<LanguageSelectionAdapter.ViewHolder>() {

    private var context: Context = context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        val square: MaterialTextView = v.findViewById(R.id.languageSquare_LBL_name)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_language_square, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        viewHolder.square.text = temp.capitalize()

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
