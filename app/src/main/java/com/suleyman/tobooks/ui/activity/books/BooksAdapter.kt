package com.suleyman.tobooks.ui.activity.books

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.suleyman.tobooks.R
import com.suleyman.tobooks.databinding.BookItemBinding
import com.suleyman.tobooks.utils.Common
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
            BookItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        holder.bindBookView(bookList[position], position)
    }
    override fun getItemCount(): Int = bookList.size

    @SuppressLint("NonConstantResourceId")
    inner class BookHolder(private val itemViewBinding: BookItemBinding) : RecyclerView.ViewHolder(itemViewBinding.root) {

        fun bindBookView(book: BookModel, position: Int) {
            with(itemViewBinding) {
                tvBookTitle.text = book.title
                if (book.type == BookModel.Type.BOOK) {
                    btnDownloadBook.setImageResource(R.drawable.round_get_app_24)
                    btnDownloadBook.setOnClickListener {
                        Common.downloadBook(this@BooksAdapter.context, book)
                    }
                    val extIndex = book.title?.lastIndexOf(".")
                    when (book.title?.substring(extIndex!! + 1)) {
                        "pdf" -> {
                            bookImage.setImageResource(R.drawable.baseline_picture_as_pdf_grey_400_24dp)
                        }
                        else ->  bookImage.setImageResource(R.drawable.ic_baseline_library_books_24)
                    }
                    bookImage.isVisible = true
                } else {
                    bookImage.isVisible = false
                    btnDownloadBook.setImageResource(R.drawable.baseline_chevron_right_24)
                    btnDownloadBook.setOnClickListener(null)
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

    interface OnClickListener {
        fun onClick(book: BookModel, position: Int)
    }
}