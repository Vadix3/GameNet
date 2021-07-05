package com.vadim.gamenet.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.vadim.gamenet.MyAppClass
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.dialogs.DialogUserDetails
import com.vadim.gamenet.models.AppUser
import com.vadim.gamenet.models.Game

class FriendsListAdapter(
    context: Context,
    private val dataSet: ArrayList<AppUser>, srcUser: AppUser
) :
    RecyclerView.Adapter<FriendsListAdapter.ViewHolder>() {

    private var context: Context = context
    private val sourceUser = srcUser

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var v: View = view
        var friendImage: ImageView = v.findViewById(R.id.friendsRow_IMG_friendPhoto)
        var friendUsername: TextView = v.findViewById(R.id.friendsRow_LBL_friendUsername)
        var rowCard: MaterialCardView = v.findViewById(R.id.friendsRow_LAY_mainCard)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_friend_list_item, viewGroup, false)
        Log.d(TAG, "onCreateViewHolder: $dataSet")
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
        viewHolder.rowCard.setOnClickListener {
            Log.d(TAG, "onBindViewHolder: Clicked on: ${temp.username}")
            openUserDetailsDialog(temp)
        }
    }

    private fun openUserDetailsDialog(temp: AppUser) {
        Log.d(TAG, "openUserDetailsDialog: $temp")
        val dialog = DialogUserDetails(context, temp,sourceUser)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels * 0.8).toInt()
        dialog.window!!.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setDimAmount(0.9f)
    }

    override fun getItemCount() = dataSet.size

}
