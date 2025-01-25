package com.example.notebook.ui.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.notebook.NoteViewModel
import com.example.notebook.R
import com.example.notebook.databinding.FragmentPersonBinding
import kotlinx.coroutines.launch

class PersonFragment : Fragment() {

    private val viewModel by activityViewModels<NoteViewModel>()
    private var _binding: FragmentPersonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPersonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            firstName.setOnClickListener {
                val edit = layoutInflater.inflate(R.layout.note_edit_field, null).findViewById<EditText>(R.id.noteET)
                edit.setText(viewModel.user.firstName)
                val dialog = AlertDialog.Builder(requireActivity())
                    .setView(edit)
                    .setTitle("Edit first name:")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        if (edit.text.isBlank()) {
                            edit.error = "First name can't be blank"
                        } else {
                            viewModel.setUserFirstName(edit.text.toString())
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
            }
            lastName.setOnClickListener {
                val edit = layoutInflater.inflate(R.layout.note_edit_field, null).findViewById<EditText>(R.id.noteET)
                edit.setText(viewModel.user.lastName)
                val dialog = AlertDialog.Builder(requireActivity())
                    .setView(edit)
                    .setTitle("Edit last name:")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        if (edit.text.isBlank()) {
                            edit.error = "Last name can't be blank"
                        } else {
                            viewModel.setUserLastName(edit.text.toString())
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
            }
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.userFlow.collect {
                        val context = requireContext()
                        val first = String.format(context.getString(R.string.first_name), it.firstName)
                        val last = String.format(context.getString(R.string.last_name), it.lastName)
                        firstName.text = first
                        lastName.text = last
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}