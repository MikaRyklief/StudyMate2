package com.example.studymate2.ui.dashboard

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate2.R
import com.example.studymate2.databinding.FragmentDashboardBinding
import com.example.studymate2.viewmodel.StudyTaskViewModel
import com.example.studymate2.viewmodel.StudyTaskViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardAdapter = DashboardTaskAdapter()
    private val taskViewModel: StudyTaskViewModel by activityViewModels {
        StudyTaskViewModelFactory(requireActivity().application)
    }

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftMillis: Long = DEFAULT_FOCUS_DURATION

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dashboardTasksRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.dashboardTasksRecycler.adapter = dashboardAdapter

        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName?.takeIf { it.isNotBlank() }
            ?: getString(R.string.settings_account_name_placeholder)
        binding.greetingText.text = getString(R.string.dashboard_greeting_with_name, name)

        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val completed = tasks.count { it.completed }
            val total = tasks.size
            val progress = if (total == 0) 0 else (completed * 100) / total
            binding.progressBar.progress = progress
            binding.progressValue.text = if (total == 0) {
                getString(R.string.dashboard_progress_placeholder)
            } else {
                getString(R.string.dashboard_progress_value, completed, total)
            }

            val upcoming = tasks.filter { !it.completed }.take(3)
            dashboardAdapter.submitList(upcoming)
            binding.dashboardEmptyState.visibility = if (upcoming.isEmpty()) View.VISIBLE else View.GONE

            updateAnalytics(tasks)
        }

        binding.startTimerButton.setOnClickListener { toggleTimer() }
        binding.resetTimerButton.setOnClickListener { resetTimer() }

        updateTimerText()
        configureCharts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }

    private fun toggleTimer() {
        if (isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timeLeftMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                timeLeftMillis = DEFAULT_FOCUS_DURATION
                isTimerRunning = false
                updateTimerText()
            }
        }.start()
        isTimerRunning = true
        binding.startTimerButton.text = getString(R.string.dashboard_pause)
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        binding.startTimerButton.text = getString(R.string.dashboard_start)
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeftMillis = DEFAULT_FOCUS_DURATION
        isTimerRunning = false
        updateTimerText()
        binding.startTimerButton.text = getString(R.string.dashboard_start)
    }

    private fun updateTimerText() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMillis) % 60
        binding.timerCountdown.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.startTimerButton.text = if (isTimerRunning) {
            getString(R.string.dashboard_pause)
        } else {
            getString(R.string.dashboard_start)
        }
    }

    private fun configureCharts() {
        binding.subjectPieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false
            setNoDataText(getString(R.string.analytics_no_data))
            setEntryLabelColor(Color.WHITE)
        }

        binding.weeklyTrendChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setNoDataText(getString(R.string.analytics_no_data))
            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                textColor = ContextCompat.getColor(requireContext(), R.color.black)
                setDrawGridLines(false)
                position = YAxis.YAxisLabelPosition.OUTSIDE_CHART
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), R.color.black)
                setDrawGridLines(false)
            }
        }
    }

    private fun updateAnalytics(tasks: List<com.example.studymate2.data.StudyTask>) {
        val subjectDurations = tasks.groupBy { it.subject }
            .mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
            .filterValues { it > 0 }

        if (subjectDurations.isEmpty()) {
            binding.subjectPieChart.clear()
            binding.weeklyTrendChart.clear()
            binding.analyticsInsight.text = getString(R.string.analytics_insight_placeholder)
            return
        }

        val totalMinutes = subjectDurations.values.sum().coerceAtLeast(1)
        val pieEntries = subjectDurations.map { (subject, minutes) ->
            PieEntry(minutes.toFloat(), subject)
        }
        val pieColors = subjectDurations.keys.map {
            com.example.studymate2.util.SubjectColorProvider.colorForSubject(requireContext(), it)
        }
        val pieDataSet = PieDataSet(pieEntries, getString(R.string.analytics_title)).apply {
            colors = pieColors
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            sliceSpace = 2f
        }
        binding.subjectPieChart.data = PieData(pieDataSet).apply {
            setValueFormatter(PercentFormatter(binding.subjectPieChart))
            setValueTextColor(Color.WHITE)
        }
        binding.subjectPieChart.invalidate()

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val dayLabels = mutableListOf<String>()
        val lineEntries = mutableListOf<Entry>()
        val dayFormat = java.text.SimpleDateFormat("EEE", Locale.getDefault())
        val dayMillis = TimeUnit.DAYS.toMillis(1)

        for (index in 6 downTo 0) {
            val dayStart = calendar.timeInMillis - index * dayMillis
            val dayEnd = dayStart + dayMillis - 1
            val minutes = tasks.filter { it.dueDate in dayStart..dayEnd }
                .sumOf { it.durationMinutes }
            dayLabels.add(dayFormat.format(java.util.Date(dayStart)))
            lineEntries.add(Entry((6 - index).toFloat(), minutes / 60f))
        }

        val lineDataSet = LineDataSet(lineEntries, getString(R.string.analytics_weekly_hours)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.analytics_line)
            setCircleColor(color)
            lineWidth = 3f
            circleRadius = 5f
            setDrawFilled(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        binding.weeklyTrendChart.xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels)
        binding.weeklyTrendChart.data = LineData(lineDataSet)
        binding.weeklyTrendChart.invalidate()

        val dominantSubject = subjectDurations.maxByOrNull { it.value }
        if (dominantSubject != null) {
            val percent = (dominantSubject.value * 100f / totalMinutes).roundToInt()
            binding.analyticsInsight.text = getString(
                R.string.analytics_insight,
                percent,
                dominantSubject.key
            )
        } else {
            binding.analyticsInsight.text = getString(R.string.analytics_insight_placeholder)
        }
    }

    companion object {
        private const val DEFAULT_FOCUS_DURATION = 25 * 60 * 1000L
    }
}
