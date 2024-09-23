package com.example.vocaboost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.vocaboost.data.model.Note // Pastikan Anda mengimpor Note dari lokasi yang benar

class DetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_NOTE = "note"

        fun newInstance(note: Note): DetailDialogFragment {
            val fragment = DetailDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_NOTE, note) // Pastikan Note mengimplementasikan Parcelable
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val note = arguments?.getParcelable<Note>(ARG_NOTE)

        // Set data ke view di sini
        view.findViewById<TextView>(R.id.detailEnglish).text = note?.english
        view.findViewById<TextView>(R.id.detailIndonesian).text = note?.indonesian
        view.findViewById<TextView>(R.id.detailDescription).text = note?.description
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout((0.9 * resources.displayMetrics.widthPixels).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
