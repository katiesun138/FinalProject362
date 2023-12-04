package com.example.fin362.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import com.example.fin362.R
import com.example.fin362.FirebaseDBManager
import com.example.fin362.ui.events.Job
import com.example.fin362.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val sp: SharedPreferences =
            requireActivity().getSharedPreferences("Info", AppCompatActivity.MODE_PRIVATE)
        val name=sp.getString("Name","John Doe")
        val email=sp.getString("Email","johndoe@gmail.com")
        val link=sp.getString("Link","https://ca.linkedin.com/")
        val git=sp.getString("Git","https://github.com/")
        val port=sp.getString("Port","https://www.portfoliobox.net/")
        val nameTextView: TextView = binding.tvUsername
        settingsViewModel.username.observe(viewLifecycleOwner) {
            nameTextView.text = name
        }
        val mailTextView: TextView = binding.tvEmail
        settingsViewModel.mail.observe(viewLifecycleOwner) {
            mailTextView.text = email
        }
        val appliedTextView: TextView = binding.tvApplied
        settingsViewModel.applied.observe(viewLifecycleOwner) {
            appliedTextView.text = it
        }
        val interviewingTextView: TextView = binding.tvInterviewing
        settingsViewModel.interviewing.observe(viewLifecycleOwner) {
            interviewingTextView.text = it
        }
        val rejectedTextView: TextView = binding.tvRejected
        settingsViewModel.rejected.observe(viewLifecycleOwner) {
            rejectedTextView.text = it
        }
        val offerTextView: TextView = binding.tvOffer
        settingsViewModel.offer.observe(viewLifecycleOwner) {
            offerTextView.text = it
        }
        val engineerTextView: TextView = binding.tvEngineer
        settingsViewModel.engineer.observe(viewLifecycleOwner) {
            engineerTextView.text = it
        }
        val spotifyTextView: TextView = binding.tvSpotify
        settingsViewModel.spotify.observe(viewLifecycleOwner) {
            spotifyTextView.text = it
        }
        val scienceTextView: TextView = binding.tvScience
        settingsViewModel.science.observe(viewLifecycleOwner) {
            scienceTextView.text = it
        }
        val simonTextView: TextView = binding.tvSimon
        settingsViewModel.simon.observe(viewLifecycleOwner) {
            simonTextView.text = it
        }
        val resumeTextView: TextView = binding.tvResume
        settingsViewModel.resume.observe(viewLifecycleOwner) {
            resumeTextView.text = it
        }

        val editResumeTextView: TextView = binding.tvEditResume
        editResumeTextView.setOnClickListener{
            val intent = Intent(this.context, EditResumeActivity().javaClass)
            startActivity(intent)
        }
        val editInfoTextView: TextView =binding.tvEditInfo
        editInfoTextView.setOnClickListener{
            val myDialog = InputDialog()
            val bundle = Bundle()
            bundle.putInt(InputDialog.DIALOG_KEY, InputDialog.TEST_DIALOG)
            myDialog.arguments = bundle
            myDialog.show(parentFragmentManager, "input dialog")
        }
        val linkedLnView:TextView=binding.tvLinkedln
        linkedLnView.setOnClickListener(){
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        }
        val gitHubView:TextView=binding.tvGithub
        gitHubView.setOnClickListener(){
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(git)))
        }
        val portView:TextView=binding.tvLinkedln
        portView.setOnClickListener(){
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(port)))
        }
        settingsViewModel.db.getStatusInformation {
            settingsViewModel.status=it.toMutableList()
            var applied=settingsViewModel.status[0]
            var interviewing=settingsViewModel.status[1]
            var rejected=settingsViewModel.status[2]
            var offer=settingsViewModel.status[3]
            appliedTextView.text=applied
            interviewingTextView.text=interviewing
            rejectedTextView.text=rejected
            offerTextView.text=offer
        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}