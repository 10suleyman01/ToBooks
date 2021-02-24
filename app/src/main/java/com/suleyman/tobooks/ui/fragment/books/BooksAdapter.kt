package com.suleyman.tobooks.ui.fragment.books

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.suleyman.tobooks.R
import com.suleyman.tobooks.databinding.BookItemBinding
import com.suleyman.tobooks.databinding.FolderItemBinding
import com.suleyman.tobooks.utils.Common
import com.suleyman.tobooks.model.BookModel
import com.suleyman.tobooks.utils.Common.GENRES
import com.suleyman.tobooks.utils.FirestoreConfig
import com.suleyman.tobooks.utils.Utils
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("NotifyDataSetChanged")
@Singleton
class BooksAdapter @Inject constructor(
    val context: Context,
    val utils: Utils,
    val firestore: FirebaseFirestore,
    val databaseReference: DatabaseReference
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        const val BOOK_TYPE = 101
        const val FOLDER_TYPE = 102
    }

    private var bookList = mutableListOf<BookModel>()
    private var listeners = mutableListOf<OnClickListener>()


    fun setBooks(newBookList: MutableList<BookModel>) {
        this.bookList = newBookList
        notifyDataSetChanged()
    }

    fun clearWithNotify() {
        bookList.clear()
        notifyDataSetChanged()
    }

    fun addListener(listener: OnClickListener) {
        this.listeners.add(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BOOK_TYPE -> BookHolder(
                BookItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            FOLDER_TYPE -> FolderHolder(
                FolderItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> BookHolder(
                BookItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            BOOK_TYPE -> {
                (holder as BookHolder).bindBookView(bookList[position])
            }

            FOLDER_TYPE -> {
                (holder as FolderHolder).bindFolder(bookList[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (bookList[position].type == BookModel.Type.BOOK) BOOK_TYPE
        else FOLDER_TYPE
    }

    override fun getItemCount(): Int = bookList.size

    private inner class BookHolder(private val itemViewBinding: BookItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {

        fun bindBookView(book: BookModel) {
            with(itemViewBinding) {

                tvBookTitle.text = "..."
                tvBookAuthor.text = "..."
                bookImage.setImageBitmap(null)

                firestore.collection(FirestoreConfig.COLLECTION)
                    .document(book.title!!)
                    .get()
                    .addOnSuccessListener { document ->
                        tvBookTitle.text = document[FirestoreConfig.NAME].toString()
                        tvBookAuthor.text = document[FirestoreConfig.AUTHOR].toString()

                        val bytes = Base64.decode(
                            document[FirestoreConfig.PHOTO].toString(),
                            Base64.DEFAULT
                        )

                        val bmp = utils.toBitmap(bytes)
                        Glide.with(context)
                            .load(bmp)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(bookImage)
                    }

                btnDownloadBook.setImageResource(R.drawable.round_get_app_24)
                btnDownloadBook.setOnClickListener {
                    Common.downloadBook(this@BooksAdapter.context, book)
                }
            }
            if (listeners.size > 0)
                listeners.forEach { listener ->
                    itemView.setOnClickListener {
                        listener.onClick(book, position)
                    }
                }
        }
    }

    private inner class FolderHolder(private val itemViewBinding: FolderItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bindFolder(book: BookModel) {

            with(itemViewBinding) {
                tvFolderTitle.text = book.title
                book.title?.let {
//                    val icon = getIconOfCategory(it)
                    categoryIcon.isVisible = false
//                    else {
//                        categoryIcon.isVisible = true
//                        categoryIcon.setBackgroundResource(icon)
//                    }
                }
            }


            if (listeners.size > 0)
                listeners.forEach { listener ->
                    itemView.setOnClickListener {
                        listener.onClick(book, position)
                    }
                }
        }
    }

    private fun getIconOfCategory(title: String): Int {
        when (title) {
            GENRES[0] -> R.drawable.business
            GENRES[25] -> R.drawable.sports
            GENRES[GENRES.lastIndex] -> R.drawable.humor
            else -> 0
        }
        return 0
    }

    interface OnClickListener {
        fun onClick(book: BookModel, position: Int)
    }
}