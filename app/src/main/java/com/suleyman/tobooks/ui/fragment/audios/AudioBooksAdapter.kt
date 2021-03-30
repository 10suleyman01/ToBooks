package com.suleyman.tobooks.ui.fragment.audios

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.suleyman.tobooks.databinding.FolderItemBinding
import com.suleyman.tobooks.model.BookModel
import com.suleyman.tobooks.ui.fragment.books.BooksAdapter

@SuppressLint("NotifyDataSetChanged")
class AudioBooksAdapter : RecyclerView.Adapter<AudioBooksAdapter.AudioBookHolder>() {

    private var audiosList = mutableListOf<BookModel>()
    private var listeners = mutableListOf<BooksAdapter.OnClickListener>()

    fun setAudios(booksResult: MutableList<BookModel>) {
        this.audiosList = booksResult
        notifyDataSetChanged()
    }

    fun clearWithNotify() {
        audiosList.clear()
        notifyDataSetChanged()
    }

    fun addListener(listener: BooksAdapter.OnClickListener) {
        this.listeners.add(listener)
    }

    inner class AudioBookHolder(private val itemViewBinding: FolderItemBinding) :
        RecyclerView.ViewHolder(
            itemViewBinding.root
        ) {
        fun bind(audio: BookModel) {
            with(itemViewBinding) {
                tvFolderTitle.text = audio.title
                categoryIcon.isVisible = false
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioBooksAdapter.AudioBookHolder {
        return AudioBookHolder(
            FolderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AudioBooksAdapter.AudioBookHolder, position: Int) {

        val audio = audiosList[position]

        holder.bind(audio)

        if (listeners.size > 0)
            listeners.forEach { listener ->
                holder.itemView.setOnClickListener {
                    listener.onClick(audio, position)
                }
            }
    }

    override fun getItemCount() = audiosList.size
}