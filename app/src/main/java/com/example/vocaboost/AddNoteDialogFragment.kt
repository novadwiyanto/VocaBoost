package com.example.vocaboost

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.vocaboost.data.model.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddNoteDialogFragment : DialogFragment() {

    interface OnNoteAddedListener {
        fun onNoteAdded(note: Note)
    }

    var listener: OnNoteAddedListener? = null
    private var existingNote: Note? = null

    // Fungsi untuk mengatur catatan yang ada
    fun setNote(note: Note) {
        existingNote = note
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_note_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val englishEditText = view.findViewById<EditText>(R.id.et_english)
        val indonesianEditText = view.findViewById<EditText>(R.id.et_indonesian)
        val descriptionEditText = view.findViewById<EditText>(R.id.et_description)
        val saveButton = view.findViewById<FloatingActionButton>(R.id.btn_save)
        val closeButton = view.findViewById<ImageButton>(R.id.btn_close)

        // Jika ada catatan yang ada, isi EditText dengan data
        existingNote?.let {
            englishEditText.setText(it.english)
            indonesianEditText.setText(it.indonesian)
            descriptionEditText.setText(it.description)
        }

        saveButton.setOnClickListener {
            val english = englishEditText.text.toString()
            val indonesian = indonesianEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (english.isNotEmpty()) {
                val note = existingNote?.copy(
                    english = english,
                    indonesian = indonesian,
                    description = description
                ) ?: Note(english = english, indonesian = indonesian, description = description)

                listener?.onNoteAdded(note)
            } else {
                Toast.makeText(requireContext(), "Please fill in the English field", Toast.LENGTH_SHORT).show()
            }
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }
}
