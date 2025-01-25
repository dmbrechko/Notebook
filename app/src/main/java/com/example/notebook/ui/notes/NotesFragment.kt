package com.example.notebook.ui.notes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.notebook.Note
import com.example.notebook.NoteViewModel
import com.example.notebook.R
import com.example.notebook.databinding.FragmentNotesBinding
import com.example.notebook.databinding.ListItemBinding
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {
    private val viewModel by activityViewModels<NoteViewModel>()

    private var _binding: FragmentNotesBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            addBTN.setOnClickListener {
                if (noteET.text.isBlank()) {
                    Toast.makeText(requireActivity(), "Note can not be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.addNote(noteET.text.toString())
            }

            val actions = object : NotesAdapter.NoteActions {
                override fun edit(note: Note, position: Int) {
                    val noteET = layoutInflater.inflate(R.layout.note_edit_field, null).findViewById<EditText>(R.id.noteET)
                    noteET.setText(note.note)
                    val dialog = AlertDialog.Builder(requireActivity())
                        .setView(noteET)
                        .setTitle(R.string.edit_note)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            if (noteET.text.isBlank()) {
                                noteET.error = "Note can not be blank"
                            } else {
                                viewModel.editNote(note.copy(note = noteET.text.toString()), position)
                            }
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .create()
                    dialog.show()
                }

                override fun delete(position: Int) {
                    viewModel.deleteNote(position)
                }

                override fun onCheck(note: Note, position: Int) {
                    viewModel.editNote(note.copy(isDone = !note.isDone), position)
                }
            }
            listRV.layoutManager = LinearLayoutManager(requireActivity())
            adapter = NotesAdapter(actions)
            listRV.adapter = adapter
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.notesFlow.collect {
                        Log.d("VV", "collected, list: $it")
                        adapter.submitList(it)
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

class NotesAdapter(private val actions: NoteActions): ListAdapter<Note, NotesAdapter.NoteViewHolder>(Callback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding, actions)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        Log.d("VV", "note: ${getItem(position)}")
        holder.bind(getItem(position))
    }

    interface NoteActions {
        fun edit(note: Note, position: Int)
        fun delete(position: Int)
        fun onCheck(note: Note, position: Int)
    }

    class NoteViewHolder(private val binding: ListItemBinding, private val actions: NoteActions): ViewHolder(binding.root) {
        private lateinit var note: Note
        init {
            binding.apply {
                editIV.setOnClickListener {
                    actions.edit(note, adapterPosition)
                }
                deleteIV.setOnClickListener {
                    actions.delete(adapterPosition)
                }
                noteCB.setOnCheckedChangeListener { _, _ ->
                    actions.onCheck(note, adapterPosition)
                }
            }
        }
        fun bind(note: Note) {
            this.note = note
            binding.apply {
                noteCB.isChecked = note.isDone
                noteCB.text = note.note
            }
        }
    }

    class Callback: DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            Log.d("VV", "comparing items")
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.note == newItem.note && oldItem.isDone == newItem.isDone
        }

    }
}