package com.example.fin362.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fin362.R
import com.example.fin362.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    private fun spinnerSetup(view: View) {
        val layoutSpinner = view.findViewById<Spinner>(R.id.history_view_spinner)
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.history_layout_types))

        layoutSpinner.adapter = adapter

        layoutSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Intentionally does nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> viewModel.compactView = false
                    1 -> viewModel.compactView = true
                    else -> viewModel.compactView = false // Shouldn't be possible
                }
                //TODO: Make this actually change anything on the display
            }
        }
    }
    private fun historyDetailSetup(view: View){
        val jobList = view.findViewById<LinearLayout>(R.id.history_job_list)
        for(cardIndex in 0..<jobList.childCount){
            val card = jobList.getChildAt(cardIndex)

            card.setOnClickListener{
                //TODO: This doesn't pass any info, it just changes fragments.
                //Not actually useful in its current state!
                val fragTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragTransaction.replace(R.id.history_container,  HomeDetail())
                fragTransaction.addToBackStack(null)
                fragTransaction.commit()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root

        spinnerSetup(view)
        historyDetailSetup(view)

        view.findViewById<Button>(R.id.history_graph_goto).setOnClickListener{
            val fragTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragTransaction.replace(R.id.history_container, HomeGraphView())
            fragTransaction.addToBackStack(null)
            fragTransaction.commit()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}