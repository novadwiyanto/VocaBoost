package com.example.vocaboost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class DetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_ITEM = "item"

        fun newInstance(item: DataActivity.Item): DetailDialogFragment {
            val fragment = DetailDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_ITEM, item) // Pastikan DataActivity.Item mengimplementasikan Parcelable
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
        val item = arguments?.getParcelable<DataActivity.Item>(ARG_ITEM)

        // Set data ke view di sini
        view.findViewById<TextView>(R.id.detailEnglish).text = item?.english
        view.findViewById<TextView>(R.id.detailIndonesian).text = item?.indonesian
        view.findViewById<TextView>(R.id.detailDescription).text = item?.indonesian
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout((0.9 * resources.displayMetrics.widthPixels).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
