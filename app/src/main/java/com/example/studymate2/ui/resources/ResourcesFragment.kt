package com.example.studymate2.ui.resources

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate2.databinding.FragmentResourcesBinding
import com.example.studymate2.viewmodel.StudyResourcesViewModel
import com.example.studymate2.viewmodel.StudyResourcesViewModelFactory
import com.google.android.material.snackbar.Snackbar

class ResourcesFragment : Fragment() {

    private var _binding: FragmentResourcesBinding? = null
    private val binding get() = _binding!!

    private val adapter = StudyResourceAdapter()

    private val viewModel: StudyResourcesViewModel by viewModels {
        StudyResourcesViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resourcesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.resourcesRecycler.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadResources()
        }

        viewModel.resources.observe(viewLifecycleOwner) { resources ->
            val items = resources.orEmpty()
            adapter.submitList(items)
            binding.resourcesEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
            binding.loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }

        if (viewModel.resources.value.isNullOrEmpty()) {
            viewModel.loadResources()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
