package com.example.studymate2.ui.dashboard

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate2.R
import com.example.studymate2.databinding.FragmentDashboardBinding
import com.example.studymate2.viewmodel.StudyTaskViewModel
import com.example.studymate2.viewmodel.StudyTaskViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import java.util.concurrent.TimeUnit

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
        }

        binding.startTimerButton.setOnClickListener { toggleTimer() }
        binding.resetTimerButton.setOnClickListener { resetTimer() }

        updateTimerText()
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

    companion object {
        private const val DEFAULT_FOCUS_DURATION = 25 * 60 * 1000L
    }
}
