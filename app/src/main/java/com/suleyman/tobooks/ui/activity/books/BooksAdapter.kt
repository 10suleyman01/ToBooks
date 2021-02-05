package com.suleyman.tobooks.ui.activity.books

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.suleyman.tobooks.R
import com.suleyman.tobooks.app.Common
import com.suleyman.tobooks.databinding.BookItemBinding
import com.suleyman.tobooks.model.BookModel
import java.util.*

class BooksAdapter(val context: Context) : RecyclerView.Adapter<BooksAdapter.BookHolder>() {

    private var bookList = mutableListOf<BookModel>()
    private var listeners = mutableListOf<OnClickListener>()

    fun setBooks(newBookList: MutableList<BookModel>, withClear: Boolean = false) {
        if (withClear) this.bookList.clear()
        this.bookList = newBookList
        notifyDataSetChanged()
    }


    fun clear() {
        bookList.clear()
        notifyDataSetChanged()
    }

    fun addListener(listener: OnClickListener) {
        this.listeners.add(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        return BookHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        holder.bindBookView(bookList[position])
    }

    override fun getItemCount(): Int = bookList.size

    @SuppressLint("NonConstantResourceId")
    inner class BookHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {

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
                btnBookDownload.setImageResource(R.drawable.round_get_app_24)
                btnBookDownload.setOnClickListener {
                    Common.downloadBook(this@BooksAdapter.context, book)
                }

                val extIndex = book.title?.lastIndexOf(".")
                when (book.title?.substring(extIndex!! + 1)) {
                    "pdf" -> {
                        bookImageView.setImageResource(R.drawable.baseline_picture_as_pdf_grey_400_24dp)
                    }
                    else ->  bookImageView.setImageResource(R.drawable.baseline_insert_drive_file_24dp)
                }
                bookImageView.visibility = View.VISIBLE
            } else {
                bookImageView.visibility = View.GONE
                btnBookDownload.setImageResource(R.drawable.baseline_chevron_right_24)
                btnBookDownload.setOnClickListener(null)
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