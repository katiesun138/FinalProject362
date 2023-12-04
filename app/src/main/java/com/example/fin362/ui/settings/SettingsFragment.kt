package com.example.fin362.ui.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.fin362.databinding.FragmentSettingsBinding
import java.io.File
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var imgUri: Uri
    private lateinit var myViewModel: MyViewModel
    private var line:String?="........"
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val imgName="xdImg.jpg"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val imageView:ImageView=binding.profileImage
        imageView.setOnClickListener(){
            Util.checkPermissions(requireActivity())
            val dialogOptions = arrayOf( "Select from Gallery")
            val builder = AlertDialog.Builder(requireActivity())
            var intent: Intent
            builder.setTitle("change")
            builder.setItems(dialogOptions)
            {
                    _, index ->
                when(dialogOptions[index])
                {
                    "Select from Gallery" -> {
                        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryResult.launch(intent)
                    }
                }
            }
            builder.show()
        }
        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap=Util.getBitmap(requireActivity(),result.data?.data!!)
                myViewModel.userImage.value =bitmap
                line=imgUri.path.toString()

            }
        }
        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.userImage.observe(viewLifecycleOwner){
                bitMap: Bitmap ->
            imageView.setImageBitmap(bitMap)

        }
        val imgFile =File(requireContext().getExternalFilesDir(null),imgName)
        imgUri=FileProvider.getUriForFile(requireActivity(),"com.example.fin362",imgFile)

        val sp: SharedPreferences =
            requireActivity().getSharedPreferences("Info", AppCompatActivity.MODE_PRIVATE)
        val name=sp.getString("Name","John Doe")
        val email=sp.getString("Email","johndoe@gmail.com")
        val link=sp.getString("Link","https://ca.linkedin.com/")
        val git=sp.getString("Git","https://github.com/")
        val port=sp.getString("Port","https://www.portfoliobox.net/")
        val a1=sp.getString("a1","Software Engineer")
        val a2=sp.getString("a2","Spotify")
        val a3=sp.getString("a3","Dec 20 - Feb 21")
        val a4=sp.getString("a4","San Jose,US")
        val b1=sp.getString("b1","BSc Computer Science")
        val b2=sp.getString("b2","Simon Fraser University")
        val b3=sp.getString("b3","2024")
        val b4=sp.getString("b4","Grad")

        val experienceImage: ImageView = binding.expIcon
        val imageUrlForExp =  "https://logo.clearbit.com/$a2.com"
        Picasso.get().load(imageUrlForExp).into(experienceImage)
        val educationImage: ImageView = binding.educationIcon
        val imageUrlForEdu = "https://www.$b2/favicon.ico"
        Picasso.get().load(imageUrlForEdu).into(educationImage)


        val majorTextView:TextView=binding.tvMajor
        settingsViewModel.major.observe(viewLifecycleOwner){
            majorTextView.text=b1
        }
        val schoolTextView:TextView=binding.tvSchool
        settingsViewModel.school.observe(viewLifecycleOwner){
            schoolTextView.text=b2
        }
        val jobTextView: TextView = binding.tvJob
        settingsViewModel.job.observe(viewLifecycleOwner) {
            jobTextView.text = a1
        }
        val companyTextView: TextView = binding.tvCompany
        settingsViewModel.company.observe(viewLifecycleOwner) {
            companyTextView.text = a2
        }
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
            engineerTextView.text = a4
        }
        val spotifyTextView: TextView = binding.tvSpotify
        settingsViewModel.spotify.observe(viewLifecycleOwner) {
            spotifyTextView.text = a3
        }
        val scienceTextView: TextView = binding.tvScience
        settingsViewModel.science.observe(viewLifecycleOwner) {
            scienceTextView.text = b4
        }
        val simonTextView: TextView = binding.tvSimon
        settingsViewModel.simon.observe(viewLifecycleOwner) {
            simonTextView.text = b3
        }
        val resumeTextView: TextView = binding.tvResume
        settingsViewModel.resume.observe(viewLifecycleOwner) {
            resumeTextView.text = it
        }
        val editExperienceTextView:TextView=binding.tvEdit
        editExperienceTextView.setOnClickListener(){
            val myDialog = InputDialog2()
            val bundle = Bundle()
            bundle.putInt(InputDialog2.DIALOG_KEY, InputDialog2.TEST_DIALOG)
            myDialog.arguments = bundle
            myDialog.show(parentFragmentManager, "input dialog")

        }
        val editEducationTextView:TextView=binding.tvEditEdu
        editEducationTextView.setOnClickListener(){
            val myDialog = InputDialog3()
            val bundle = Bundle()
            bundle.putInt(InputDialog3.DIALOG_KEY, InputDialog3.TEST_DIALOG)
            myDialog.arguments = bundle
            myDialog.show(parentFragmentManager, "input dialog")

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
        val portView:TextView=binding.tvPortfolio
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