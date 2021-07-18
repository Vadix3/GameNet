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
import com.google.android.material.button.MaterialButton
import com.vadim.gamenet.MyAppClass.Constants.TAG
import com.vadim.gamenet.R
import com.vadim.gamenet.models.FeedPost
import kotlin.collections.ArrayList

class MainFeedAdapter(
    context: Context,
    private val dataSet: ArrayList<FeedPost>
) :
    RecyclerView.Adapter<MainFeedAdapter.ViewHolder>() {

    private var context: Context = context


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var v: View = view
        val profilePicture: ImageView = v.findViewById(R.id.feedPost_IMG_profilePicture)
        val profileName: TextView = v.findViewById(R.id.feedPost_LBL_profileName)
        var content: TextView = v.findViewById(R.id.feedPost_LBL_postMessage)
        val likeBtn: MaterialButton = v.findViewById(R.id.feedPost_LAY_buttonLike)
        val commentBtn: MaterialButton = v.findViewById(R.id.feedPost_LAY_buttonComment)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_feed_post, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Position:$position")
        val temp = dataSet[position] // my temp post
        viewHolder.likeBtn.setOnClickListener {
            addLikeToPost(temp.id)
        }
        viewHolder.commentBtn.setOnClickListener {
            addCommentToPost(temp.id)
        }

//        val comments: ArrayList<String> = arrayListOf(),
//        val postTime: Long = System.currentTimeMillis()

        val content = temp.content
        val likeCount = temp.like_count
        val profilePictureUri = temp.profile_picture_uri
        val posterName = temp.username
        //TODO: Fetch profile picture uri from user in server
        Glide.with(context).load(profilePictureUri)
            .placeholder(context.getDrawable(R.drawable.ic_baseline_person_24_color))
            .into(viewHolder.profilePicture) // load image to view
        viewHolder.content.text = content
        viewHolder.profileName.text = posterName
        viewHolder.likeBtn.text = likeCount.toString()
    }

    private fun addCommentToPost(id: String) {
        Log.d(TAG, "addCommentToPost: $id")
    }

    private fun addLikeToPost(id: String) {
        Log.d(TAG, "addLikeToPost: $id")
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
