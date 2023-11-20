package com.example.fin362.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.fin362.R
import com.example.fin362.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val profileBtn = binding.imageProfile
        val filterBtn = binding.iconFilter
        val cardSelect = binding.cardView1


        profileBtn.setOnClickListener(){
            findNavController().navigate(R.id.nav_from_dash)

        }

        filterBtn.setOnClickListener(){
            val bottomSheetFragment = DashboardFilterPopup()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

        }

        cardSelect.setOnClickListener(){
            Log.d("katies", "clicked the card")
            val fragTransaction = requireActivity().supportFragmentManager.beginTransaction()

            val newFragment = DashboardDetailed()  // Replace with the actual fragment you want to navigate to
            fragTransaction.replace(R.id.container, newFragment)
            fragTransaction.addToBackStack(null)
            fragTransaction.commit()
        }



//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}