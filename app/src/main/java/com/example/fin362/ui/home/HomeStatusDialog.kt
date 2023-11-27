package com.example.fin362.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.fin362.R

class HomeStatusDialog : DialogFragment() {

    fun Response(status: String){
        val bundle = Bundle()
        bundle.putString("status", status)
        setFragmentResult("status", bundle)
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_status_dialog, container, false)

        view.findViewById<Button>(R.id.history_status_dialog_cancel).setOnClickListener{
            dismiss()
        }

        view.findViewById<RadioButton>(R.id.history_status_dialog_applied).setOnClickListener{
            Response("Applied")
        }

        view.findViewById<RadioButton>(R.id.history_status_dialog_interviewing).setOnClickListener{
            Response("Interviewing")
        }

        view.findViewById<RadioButton>(R.id.history_status_dialog_offer).setOnClickListener{
            Response("Offer")
        }

        view.findViewById<RadioButton>(R.id.history_status_dialog_rejected).setOnClickListener{
            Response("Rejected")
        }

        return view
    }
}