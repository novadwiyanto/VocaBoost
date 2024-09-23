package com.example.vocaboost

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class AddNoteDialogFragment : DialogFragment() {

    // Listener untuk menyimpan data note
    interface OnNoteAddedListener {
        fun onNoteAdded(english: String, indonesian: String, description: String)
    }

    var listener: OnNoteAddedListener? = null

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
        // Inflate layout untuk dialog
        return inflater.inflate(R.layout.fragment_add_note_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referensi ke elemen layout
        val englishEditText = view.findViewById<EditText>(R.id.et_english)
        val indonesianEditText = view.findViewById<EditText>(R.id.et_indonesian)
        val descriptionEditText = view.findViewById<EditText>(R.id.et_description)
        val saveButton = view.findViewById<Button>(R.id.btn_save)
        val closeButton = view.findViewById<Button>(R.id.btn_close)

        // Action untuk tombol save
        saveButton.setOnClickListener {
            val english = englishEditText.text.toString()
            val indonesia = indonesianEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (english.isNotEmpty()) {
                listener?.onNoteAdded(english, indonesia, description)
                dismiss()  // Tutup dialog setelah note ditambahkan
            } else {
                Toast.makeText(requireContext(), "Please fill english fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Action untuk tombol close
        closeButton.setOnClickListener {
            dismiss()  // Tutup dialog
        }
    }

    override fun onStart() {
        super.onStart()
        // Mengatur ukuran dialog agar full screen
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialog  // Menggunakan tema full screen
    }
}