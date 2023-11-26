package com.example.fin362.ui.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.fin362.R

class InputDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var commentView: TextView
    companion object{
        const val DIALOG_KEY = "dialog"
        const val TEST_DIALOG = 1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val bundle = arguments
        val dialogId = bundle?.getInt(DIALOG_KEY)
        if (dialogId == TEST_DIALOG) {
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(
                R.layout.input_dialog,
                null)
            builder.setView(view)
            builder.setTitle("Information")
//            commentView=view.findViewById(R.id.comment)
//            load()
            if(savedInstanceState !=null){
                commentView.text=savedInstanceState.getString("Comment","")
            }
            builder.setPositiveButton("save", this)
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        return ret
    }
    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
//            save()
            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()


        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
        }
    }
}