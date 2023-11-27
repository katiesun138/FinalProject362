package com.example.fin362.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fin362.R
import com.example.fin362.ui.events.Job
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class HomeGraphView(jobList: List<Job>) : Fragment() {
    private val internalJobList = jobList
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

            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                viewModel.graphType = position
            }
        }
    }

    private fun createBarChart(view: View) {
        val chartFrame = view.findViewById<FrameLayout>(R.id.history_graph_chart)
        val chart = com.github.mikephil.charting.charts.BarChart(requireContext())
        val entries: ArrayList<BarEntry> = ArrayList()
        val dataSet = BarDataSet(entries, "Application History")
        val legend = chart.legend

        val entryCounter = arrayOf(0, 0, 0, 0, 0)

        for (job in internalJobList) {
            when (job.appStatus) {
                "Applied" -> entryCounter[0]++
                "Interviewed" -> entryCounter[1]++
                "Rejected" -> entryCounter[2]++
                "Offer" -> entryCounter[3]++
                else -> entryCounter[4]++
            }
        }

        for (i in entryCounter.indices) {
            val entry = BarEntry(i.toFloat(), entryCounter[i].toFloat())
            if (entryCounter[i] > 0) {
                entries.add(entry)
            }
        }

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        dataSet.values = entries
        for (i in ColorTemplate.PASTEL_COLORS) {
            dataSet.colors.add(i)
        }

        val barData = BarData(dataSet)

        chart.data = barData
        chartFrame.addView(chart)
    }

    private fun createPieChart(view: View){
        val chartFrame = view.findViewById<FrameLayout>(R.id.history_graph_chart)
        val chart = com.github.mikephil.charting.charts.PieChart(requireContext())
        val entries: ArrayList<PieEntry> = ArrayList()
        val entryCounter = arrayOf(0,0,0,0,0)

        for(job in internalJobList){
            when(job.appStatus){
                "Applied" -> entryCounter[0]++
                "Interviewed" -> entryCounter[1]++
                "Rejected" -> entryCounter[2]++
                "Offer" -> entryCounter[3]++
                else -> entryCounter[4]++
            }
        }

        if(entryCounter[0] > 0) entries.add(PieEntry(entryCounter[0].toFloat(), "Applied"))
        if(entryCounter[1] > 0) entries.add(PieEntry(entryCounter[1].toFloat(), "Interviewed"))
        if(entryCounter[2] > 0) entries.add(PieEntry(entryCounter[2].toFloat(), "Rejected"))
        if(entryCounter[3] > 0) entries.add(PieEntry(entryCounter[3].toFloat(), "Offer"))
        if(entryCounter[4] > 0) entries.add(PieEntry(entryCounter[4].toFloat(), "Unknown"))

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

        view.findViewById<Button>(R.id.history_graph_mode_button).setOnClickListener{
            val chartFrame = view.findViewById<FrameLayout>(R.id.history_graph_chart) as ViewGroup
            chartFrame.removeView(chartFrame.getChildAt(0))

            when(viewModel.graphType){
                0 -> createPieChart(view)
                1 -> createBarChart(view)
            }
        }

        return view
    }
}