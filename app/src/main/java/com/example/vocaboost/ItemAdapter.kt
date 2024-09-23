package com.example.vocaboost

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.vocaboost.data.database.NoteDatabase
import com.example.vocaboost.data.model.Note

class ItemAdapter(
    private val context: Context,
    private var _itemList: MutableList<Note>,
    private val noteDatabase: NoteDatabase
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    var itemList: MutableList<Note>
        get() = _itemList
        private set(value) {
            _itemList = value
        }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemEnglish: TextView = itemView.findViewById(R.id.itemEnglish)
        val itemIndonesian: TextView = itemView.findViewById(R.id.itemIndonesian)
        val deleteIcon: ImageView = itemView.findViewById(R.id.iconDelete)
        val editIcon: ImageView = itemView.findViewById(R.id.iconEdit) // Tambahkan ini

        init {
            // Menangani klik untuk detail
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = itemList[position]
                    showDetailDialog(note)
                }
            }

            // Menangani klik untuk menghapus
            deleteIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = itemList[position]
                    (context as? DataActivity)?.deleteNoteFromDatabase(note, position)
                }
            }

            // Menangani klik untuk mengedit
            editIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = itemList[position]
                    (context as? DataActivity)?.showAddNoteDialog(note) // Panggil metode untuk dialog update
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = itemList[position]
        holder.itemEnglish.text = note.english
        holder.itemIndonesian.text = note.indonesian
    }

    override fun getItemCount(): Int = itemList.size

    private fun showDetailDialog(note: Note) {
        val dialog = DetailDialogFragment.newInstance(note)
        dialog.show((context as FragmentActivity).supportFragmentManager, "DetailDialog")
    }

    fun removeNoteAt(position: Int) {
        if (position >= 0 && position < itemList.size) {
            itemList.removeAt(position) // Remove the note from the list
            notifyItemRemoved(position) // Notify the adapter that the item has been removed
        }
    }


    fun updateNotes(newNotes: MutableList<Note>) {
        itemList = newNotes
        notifyDataSetChanged()
    }
}

