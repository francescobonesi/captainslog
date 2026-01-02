package com.frabon.captainslog.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.frabon.captainslog.R
import com.frabon.captainslog.CaptainsLogApplication
import com.frabon.captainslog.data.Event
import com.frabon.captainslog.data.EventType
import com.frabon.captainslog.databinding.FragmentAddEditEventBinding
import com.frabon.captainslog.viewmodels.AddEditViewModel
import com.frabon.captainslog.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEditEventFragment : Fragment() {

    private var _binding: FragmentAddEditEventBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditEventFragmentArgs by navArgs()
    private var currentEvent: Event? = null

    // We use a Calendar object to temporarily hold the user's selection
    private val selectedDate: Calendar = Calendar.getInstance()

    private val addEditViewModel: AddEditViewModel by viewModels {
        ViewModelFactory(
            (requireActivity().application as CaptainsLogApplication).repository,
            requireContext().applicationContext
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.eventId != -1) {
            setupMenu()
            lifecycleScope.launch {
                currentEvent = addEditViewModel.getEventById(args.eventId).first()
                currentEvent?.let { populateUi(it) }
            }
        } else {
            // New Event: Default to NOW
            updateDateLabel()
            updateTimeLabel()
        }

        binding.dateEditText.setOnClickListener { showDatePickerDialog() }
        binding.timeEditText.setOnClickListener { showTimePickerDialog() }
        binding.saveButton.setOnClickListener { saveEvent() }
    }

    private fun populateUi(event: Event) {
        binding.notesEditText.setText(event.notes)

        // Update Calendar object from Event
        selectedDate.set(Calendar.YEAR, event.year)
        selectedDate.set(Calendar.MONTH, event.month - 1) // Calendar months are 0-indexed
        selectedDate.set(Calendar.DAY_OF_MONTH, event.day)
        selectedDate.set(Calendar.HOUR_OF_DAY, event.hour)
        selectedDate.set(Calendar.MINUTE, event.minute)

        updateDateLabel()
        updateTimeLabel()

    }

    private fun updateDateLabel() {
        val day = selectedDate.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val month = (selectedDate.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val year = selectedDate.get(Calendar.YEAR)
        binding.dateEditText.setText("$day/$month/$year")
    }

    private fun updateTimeLabel() {
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = selectedDate.get(Calendar.MINUTE).toString().padStart(2, '0')
        binding.timeEditText.setText("$hour:$minute")
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateLabel()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                updateTimeLabel()
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true // 24-hour format
        ).show()
    }

    private fun saveEvent() {
        val notes = binding.notesEditText.text.toString().trim()

        val eventType = EventType.DEFECATION

        // Create the object
        val eventToSave = Event(
            id = currentEvent?.id ?: 0, // Keep ID if updating, 0 if new
            notes = notes.ifBlank { null },
            day = selectedDate.get(Calendar.DAY_OF_MONTH),
            month = selectedDate.get(Calendar.MONTH) + 1, // Store as 1-12
            year = selectedDate.get(Calendar.YEAR),
            hour = selectedDate.get(Calendar.HOUR_OF_DAY),
            minute = selectedDate.get(Calendar.MINUTE),
            type = eventType
        )

        lifecycleScope.launch {
            if (currentEvent == null) {
                addEditViewModel.insertEvent(eventToSave).join()
            } else {
                addEditViewModel.updateEvent(eventToSave).join()
            }
            findNavController().navigateUp()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation_message))
            .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                currentEvent?.let {
                    lifecycleScope.launch {
                        addEditViewModel.deleteEvent(it).join()
                        findNavController().navigateUp()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel_button), null)
            .show()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}