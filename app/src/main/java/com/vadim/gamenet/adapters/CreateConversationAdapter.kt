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
import com.google.android.material.card.MaterialCardView
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.interfaces.SelectedFriendsCallback
import com.vadim.gamenet.models.AppUser


class CreateConversationAdapter(
    context: Context,
    private val dataSet: ArrayList<AppUser>,
    srcUser: AppUser,
    selectedListener: SelectedFriendsCallback
) :
    RecyclerView.Adapter<CreateConversationAdapter.ViewHolder>() {

    private var context: Context = context
    private val sourceUser = srcUser
    private val listener = selectedListener
    private val selectedItems = BooleanArray(srcUser.friends_list.size)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        var friendImage: ImageView = v.findViewById(R.id.createConversation_IMG_friendPhoto)
        var friendUsername: TextView = v.findViewById(R.id.createConversation_LBL_friendUsername)
        var rowCard: MaterialCardView = v.findViewById(R.id.createConversation_LAY_mainCard)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_create_conversation_item, viewGroup, false)
        Log.d(MyAppClass.Constants.TAG, "onCreateViewHolder: $dataSet ${sourceUser.email}")
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(MyAppClass.Constants.TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position]
        viewHolder.friendUsername.text = temp.username
        Glide.with(context)
            .load(temp.photo_url)
            .into(viewHolder.friendImage)
        viewHolder.rowCard.setBackgroundColor(context.resources.getColor(R.color.white))
        viewHolder.rowCard.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: Clicked on: ${temp.username}")
            if (selectedItems[position]) {
                deselectRow(viewHolder.rowCard, position)
            } else {
                selectRow(viewHolder.rowCard, position)
            }
            listener.getSelectedItems(selectedItems)

        }
    }

    private fun selectRow(rowCard: MaterialCardView, position: Int) {
        Log.d(TAG, "selectRow: ")
        selectedItems[position] = true
        rowCard.setBackgroundColor(context.resources.getColor(R.color.colorSecondary))
    }

    private fun deselectRow(rowCard: MaterialCardView, position: Int) {
        Log.d(TAG, "deselectRow: ")
        selectedItems[position] = false
        rowCard.setBackgroundColor(context.resources.getColor(R.color.white))
    }

    override fun getItemCount() = dataSet.size

}
