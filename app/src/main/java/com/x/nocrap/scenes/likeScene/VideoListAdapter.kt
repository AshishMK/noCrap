package com.x.nocrap.scenes.likeScene

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.x.nocrap.R
import com.x.nocrap.databinding.VideoListItemBinding
import com.x.nocrap.di.module.ApiModule
import com.x.nocrap.scenes.homeScene.VideoEntity
import com.x.nocrap.utils.Utils
import java.util.*

class VideoListAdapter(val activity: Activity, val itemListener: ItemListener) :
    RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {
    var contents: ArrayList<VideoEntity> = ArrayList<VideoEntity>()

    interface ItemListener {
        fun onItemClickListener(entity: VideoEntity, position: Int)
        fun onItemDeleteClickListener(entity: VideoEntity, position: Int)
    }

    /*public void setStatus(Status status) {
        this.status = status;
        notifyItemChanged(contents.size());
    }*/
    fun setItems(contents: ArrayList<VideoEntity>) {
        this.contents.addAll(contents)
        //notifyItemRangeInserted(itemCount, contents.size)
        notifyDataSetChanged();
    }

    fun removeItem(position: Int) {
        this.contents.removeAt(position)
        //notifyItemRangeInserted(itemCount, contents.size)
        notifyItemRemoved(position);
    }

    inner class ViewHolder(private val listItemBinding: VideoListItemBinding) :
        RecyclerView.ViewHolder(listItemBinding.root) {

        fun bindTo(viewHolder: ViewHolder, content: VideoEntity) {
            listItemBinding.contentViewHolder = viewHolder
            listItemBinding.content = content
            listItemBinding.myViewModelStatic = Utils.Companion
            listItemBinding.url =
                ApiModule.base_url_download + content.thumb + (if (content.category.toInt() == 2) "" else ".png")
            listItemBinding.position = adapterPosition
        }

        fun onItemClick(position: Int) {
            itemListener.onItemClickListener(contents[position], position)
        }

        fun onItemDeleteClick(position: Int) {
            itemListener.onItemDeleteClickListener(contents[position], position)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.video_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(holder, contents[position])
    }
}