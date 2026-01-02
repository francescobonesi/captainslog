package com.frabon.captainslog.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.frabon.captainslog.CaptainsLogApplication
import com.frabon.captainslog.R
import com.frabon.captainslog.databinding.FragmentMainBinding
import com.frabon.captainslog.viewmodels.MainViewModel
import com.frabon.captainslog.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(
            (requireActivity().application as CaptainsLogApplication).repository,
            requireContext().applicationContext
        )
    }

    private val exportLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            uri?.let { onExportUriReceived(it) }
        }

    private val importLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { onImportUriReceived(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()

        val adapter = EventAdapter { event ->
            val action = MainFragmentDirections.actionMainFragmentToAddEditEventFragment(event.id)
            findNavController().navigate(action)
        }
        binding.recyclerView.adapter = adapter

        // Observe the filtered list
        mainViewModel.groupedEvents.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        mainViewModel.monthStats.observe(viewLifecycleOwner) { (poopCount, _) ->
            binding.statDefecation.text = "💩 $poopCount"

            binding.statsContainer.visibility = if (poopCount > 0) View.VISIBLE else View.GONE
            binding.statDefecation.visibility = View.VISIBLE
        }

        setupMonthNavigation()

        // Observe Current Month Name (e.g. "September 2024")
        mainViewModel.currentMonthName.observe(viewLifecycleOwner) { name ->
            binding.textCurrentMonth.text = name
        }

        binding.fab.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddEditEventFragment(-1)
            findNavController().navigate(action)
        }
    }

    private fun setupMonthNavigation() {
        binding.buttonNextMonth.setOnClickListener { mainViewModel.nextMonth() }
        binding.buttonPrevMonth.setOnClickListener { mainViewModel.previousMonth() }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        mainViewModel.setSearchQuery(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_export -> {
                        exportLauncher.launch(getString(R.string.default_export_filename))
                        true
                    }

                    R.id.action_import -> {
                        importLauncher.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "text/plain"
                            )
                        )
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun onExportUriReceived(uri: Uri) {
        lifecycleScope.launch {
            try {
                requireContext().contentResolver.openOutputStream(uri)?.let {
                    mainViewModel.exportEventsToCsv(it)
                    Toast.makeText(
                        context,
                        getString(R.string.export_successful),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.export_failed), Toast.LENGTH_SHORT)
                    .show()
                e.printStackTrace()
            }
        }
    }

    private fun onImportUriReceived(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.let {
                mainViewModel.importEventsFromCsv(it)
                Toast.makeText(context, getString(R.string.import_successful), Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}