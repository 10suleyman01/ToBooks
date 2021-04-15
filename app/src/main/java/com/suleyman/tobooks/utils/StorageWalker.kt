package com.suleyman.tobooks.utils

import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.suleyman.tobooks.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageWalker @Inject constructor(
    val storageReference: StorageReference,
    val utils: Utils
) {

    private var paths = mutableListOf<String>()
    private var isRoot = true

    fun goTo(bucket: String? = "", category: String, onSuccess: (ListResult) -> Unit) {
        paths.add(category)
        setIsRoot(false)
        storageReference.child(category).listAll().addOnSuccessListener(onSuccess)
    }

    fun backTo(bucket: String? = "", onSuccess: (ListResult) -> Unit) {
        val storage = bucket?.let {
            if (it.isEmpty()) storageReference
            else storageReference.child(bucket)
        }

        if (paths.isNotEmpty() && paths.size > 1) {
            paths.removeLast()
            val lastCategory = paths.last()
            storageReference.child(lastCategory)
                .listAll()
                .addOnSuccessListener(onSuccess)
        } else if (!isRoot && paths.size == 1) {
            paths.removeLast()
            setIsRoot(true)
            storage?.listAll()?.addOnSuccessListener(onSuccess)
        }
    }

    fun currentPath() = if (paths.isNotEmpty()) paths.last() else ""

    fun pathsIsNotEmpty() = paths.isNotEmpty()

    fun categoryTitle(): String {
        if (isRootPath()) return utils.getString(R.string.app_name)
        val lastIndex = currentPath().lastIndexOf("/")
        return currentPath().substring(lastIndex + 1)
    }

    fun backToCurrent(bucket: String? = "", onSuccess: (ListResult) -> Unit) {
        val storage = bucket?.let {
            if (it.isEmpty()) storageReference.listAll()
            else storageReference.child(bucket).listAll()
        }

        if (pathsIsNotEmpty()) {
            if (bucket?.isEmpty()!!) {
                storageReference.child(currentPath())
                    .listAll()
                    .addOnSuccessListener(onSuccess)
            } else {
                storageReference.child(bucket).child(currentPath())
                    .listAll()
                    .addOnSuccessListener(onSuccess)
            }
        } else {
            storage?.addOnSuccessListener(onSuccess)
        }
    }

    fun isRootPath() = isRoot

    fun setIsRoot(isRoot: Boolean) {
        this.isRoot = isRoot
    }

}