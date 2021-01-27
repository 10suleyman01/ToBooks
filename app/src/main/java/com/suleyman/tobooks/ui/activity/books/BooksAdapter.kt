package com.suleyman.tobooks.ui.activity.books

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.suleyman.tobooks.R
import com.suleyman.tobooks.model.BookModel

class BooksAdapter(): RecyclerView.Adapter<BooksAdapter.BookHolder>() {

    private var bookList = mutableListOf<BookModel>()
    private var listeners = mutableListOf<OnClickListener>()

    fun setBooks(bookList: MutableList<BookModel>) {
        this.bookList = bookList
        notifyDataSetChanged()
    }

    fun clearAll() {
        bookList.clear()
        notifyDataSetChanged()
    }

    fun addListener(listener: OnClickListener) {
        this.listeners.add(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        return BookHolder(LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false))
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        holder.bindBookView(bookList[position])
    }

    override fun getItemCount(): Int = bookList.size

    @SuppressLint("NonConstantResourceId")
    inner class BookHolder(private val itemView: View): RecyclerView.ViewHolder(itemView) {

        init {
            ButterKnife.bind(this, itemView)
        }

        @BindView(R.id.tvBookTitle)
        lateinit var tvBookTitle: TextView

        @BindView(R.id.btnDownloadBook)
        lateinit var btnBookDownload: ImageView

        @BindView(R.id.bookImage)
        lateinit var bookImageView: ImageView

        fun bindBookView(book: BookModel) {
            tvBookTitle.text = book.title

            if (book.type == BookModel.Type.BOOK) {
                btnBookDownload.setImageResource(R.drawable.baseline_get_app_24)
                bookImageView.setImageResource(R.drawable.baseline_insert_drive_file_24)
            } else {
                bookImageView.setImageResource(R.drawable.baseline_folder_24)
                btnBookDownload.setImageResource(R.drawable.baseline_chevron_right_24)
            }

            if (listeners.size > 0)
                listeners.forEach { listener ->
                    itemView.setOnClickListener { _ ->
                        listener.onClick(book)
                    }
                }
        }
    }

    interface OnClickListener {
        fun onClick(book: BookModel)
    }
}