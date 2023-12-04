package com.example.fin362.ui.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.fin362.R

class InputDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var nameView: TextView
    private lateinit var emailView: TextView
    private lateinit var linkView: TextView
    private lateinit var gitView: TextView
    private lateinit var portView: TextView

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
            nameView=view.findViewById(R.id.profile_name)
            emailView=view.findViewById(R.id.profile_email)
            linkView=view.findViewById(R.id.profile_linkedln)
            gitView=view.findViewById(R.id.profile_Github)
            portView=view.findViewById(R.id.profile_Portfolio)
            load()
            builder.setView(view)
            builder.setTitle("Information")

            if(savedInstanceState !=null){
                nameView.text=savedInstanceState.getString("Name","")
                emailView.text=savedInstanceState.getString("Email","")
                linkView.text=savedInstanceState.getString("Link","https://ca.linkedin.com/")
                gitView.text=savedInstanceState.getString("Git","https://github.com/")
                portView.text=savedInstanceState.getString("Port","https://www.portfoliobox.net/")
            }
            builder.setPositiveButton("save", this)
            builder.setNegativeButton("cancel", this)
            ret = builder.create()
        }
        return ret
    }
    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            save()
            Toast.makeText(activity, "Profile Saved", Toast.LENGTH_LONG).show()

        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("Name",nameView.text.toString())
        outState.putString("Email",emailView.text.toString())
        outState.putString("Link",linkView.text.toString())
        outState.putString("Git",gitView.text.toString())
        outState.putString("Port",portView.text.toString())
    }
    private fun save()
    {
        val sp: SharedPreferences =
            requireActivity().getSharedPreferences("Info", AppCompatActivity.MODE_PRIVATE)
        var editor=sp.edit()
        editor.putString("Name",nameView.text.toString())
        editor.putString("Email",emailView.text.toString())
        editor.putString("Link",linkView.text.toString())
        editor.putString("Git",gitView.text.toString())
        editor.putString("Port",portView.text.toString())
        editor.commit()
    }
    private fun load()
    { val sp: SharedPreferences =
        requireActivity().getSharedPreferences("Info", AppCompatActivity.MODE_PRIVATE)
        nameView.text=sp.getString("Name","")
        emailView.text=sp.getString("Email","")
        linkView.text=sp.getString("Link","https://ca.linkedin.com/")
        gitView.text=sp.getString("Git","https://github.com/")
        portView.text=sp.getString("Port","https://www.portfoliobox.net/")
    }
}