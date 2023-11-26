package com.example.fin362.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fin362.R
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class HomeGraphView : Fragment() {
    private lateinit var viewModel: HomeGraphViewModel
    private fun createSpinner(view: View) {
        val graphTypeSpinner = view.findViewById<Spinner>(R.id.history_graph_mode_spinner)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.history_graph_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            graphTypeSpinner.adapter = adapter
        }

        graphTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Intentionally does nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.graphType = position

                when(position){
                    0 -> createPieChart(view!!)
                    1 -> createBarChart(view!!)
                }
            }
        }
    }

    private fun createBarChart(view: View){
        val chartFrame = view.findViewById<FrameLayout>(R.id.history_graph_chart)
        val chart = com.github.mikephil.charting.charts.PieChart(requireContext())
        val entries: ArrayList<BarEntry> = ArrayList()

        val dataSet = BarDataSet(entries, "Application History")

        //TODO: Finish this.
    }

    private fun createPieChart(view: View){
        val chartFrame = view.findViewById<FrameLayout>(R.id.history_graph_chart)
        val chart = com.github.mikephil.charting.charts.PieChart(requireContext())
        val entries: ArrayList<PieEntry> = ArrayList()

        entries.add(PieEntry(127f, "Applied"))
        entries.add(PieEntry(17f, "Interviewed"))
        entries.add(PieEntry(27f, "Rejected"))
        entries.add(PieEntry(2f, "Offer"))

        val dataSet = PieDataSet(entries, "Application History")
        dataSet.sliceSpace = 4f

        for(i in ColorTemplate.PASTEL_COLORS) {
            dataSet.colors.add(i)
        }

        dataSet.setDrawIcons(true)
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 20f

        chart.data = PieData(dataSet)
        chart.setEntryLabelColor(R.color.black)
        chart.setEntryLabelTextSize(15f)
        chart.holeRadius = 50f
        chart.setUsePercentValues(false)

        chartFrame.addView(chart)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[HomeGraphViewModel::class.java]

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_graph_view, container, false)

        createSpinner(view)
        createPieChart(view)

        return view
    }
}