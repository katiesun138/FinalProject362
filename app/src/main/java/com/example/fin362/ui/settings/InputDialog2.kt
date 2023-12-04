package com.example.fin362.ui.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.fin362.R

class InputDialog2: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var positionView: TextView
    private lateinit var organizationView: TextView
    private lateinit var durationView: TextView
    private lateinit var addressView: TextView


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
                R.layout.input_dialog2,
                null)
             positionView=view.findViewById(R.id.position)
             organizationView=view.findViewById(R.id.organization)
             durationView=view.findViewById(R.id.duration)
             addressView=view.findViewById(R.id.address)

            load()
            builder.setView(view)
            builder.setTitle("Experience")


            builder.setPositiveButton("save", this)
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        return ret
    }
    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            save()
        }
    }

    private fun save()
    {
        val sp: SharedPreferences =
            requireActivity().getSharedPreferences("Info", AppCompatActivity.MODE_PRIVATE)
        var editor=sp.edit()
        editor.putString("a1",positionView.text.toString())
        editor.putString("a2",organizationView.text.toString())
        editor.putString("a3",durationView.text.toString())
        editor.putString("a4",addressView.text.toString())

        editor.commit()
    }
    private fun load()
    { val sp: SharedPreferences =
        requireActivity().getSharedPreferences("Info", AppCompatActivity.MODE_PRIVATE)
        positionView.text=sp.getString("a1","Software Engineer")
        organizationView.text=sp.getString("a2","Spotify")
        durationView.text=sp.getString("a3","Dec 20 - Feb 21")
        addressView.text=sp.getString("a4","San Jose,US")

    }
}