package com.example.fin362.ui.home

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.drawToBitmap
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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

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
        val legend = chart.legend
        val dataSets: ArrayList<IBarDataSet> = arrayListOf()
        var numBars = 0
        var graphMax = 0

        val entryCounter = arrayOf(0, 0, 0, 0, 0)

        for (job in internalJobList) {
            when (job.appStatus) {
                "Applied" -> entryCounter[0]++
                "Interviewing" -> entryCounter[1]++
                "Rejected" -> entryCounter[2]++
                "Offer" -> entryCounter[3]++
                else -> entryCounter[4]++
            }
        }

        for (i in entryCounter.indices) {
            if (entryCounter[i] > 0) {
                val entry = BarEntry(numBars.toFloat(), entryCounter[i].toFloat())
                numBars++
                val entryHolder: ArrayList<BarEntry> = ArrayList()

                val dataSet = when(i){
                    0 -> BarDataSet(entryHolder, "Applied")
                    1 -> BarDataSet(entryHolder, "Interviewing")
                    2 -> BarDataSet(entryHolder, "Rejected")
                    3 -> BarDataSet(entryHolder, "Offer")
                    else -> BarDataSet(entryHolder, "Unknown")
                }

                entryHolder.add(entry)
                dataSet.color = ColorTemplate.PASTEL_COLORS[numBars]
                dataSets.add(dataSet)

                if(entryCounter[i] > graphMax) { graphMax = entryCounter[i] }
            }
        }

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = numBars.toFloat() - 0.5f
        xAxis.labelCount = numBars
        xAxis.textSize = 0f

        val yAxis = chart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = graphMax.toFloat()

        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        val barData = BarData(dataSets)
        barData.barWidth = 0.9f

        Log.d("entry",dataSets.toString())
        Log.d("entry",barData.toString())

        chart.data = barData
        chart.description.isEnabled = false

        Log.d("entry",chart.data.toString())
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
                "Interviewing" -> entryCounter[1]++
                "Rejected" -> entryCounter[2]++
                "Offer" -> entryCounter[3]++
                else -> entryCounter[4]++
            }
        }

        if(entryCounter[0] > 0) entries.add(PieEntry(entryCounter[0].toFloat(), "Applied"))
        if(entryCounter[1] > 0) entries.add(PieEntry(entryCounter[1].toFloat(), "Interviewing"))
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
        chart.description.isEnabled = false

        chartFrame.addView(chart)
    }

    private fun exportImage(view: View){
        val chartFrame = view.findViewById<FrameLayout>(R.id.history_graph_chart)
        val image = chartFrame.drawToBitmap()

        val filePathAsString = requireActivity().getExternalFilesDir(null).toString() + "/" + getString(R.string.app_name)
        val filePath = File(filePathAsString)
        if(!filePath.exists()){ filePath.mkdir() }

        val imageName = "JobChart_" + Calendar.getInstance().timeInMillis.toString() + ".png"
        val file = File(filePath, imageName)
        val outputStream = FileOutputStream(file)

        image.compress(Bitmap.CompressFormat.PNG, 85, outputStream)

        outputStream.flush()
        outputStream.close()

        Toast.makeText(context, "Chart exported as $imageName!", Toast.LENGTH_LONG).show()
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

        view.findViewById<Button>(R.id.history_export_button).setOnClickListener{ exportImage(view) }

        return view
    }
}