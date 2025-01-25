package com.example.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class NoteViewModel: ViewModel() {
    private val notes = mutableListOf<Note>()
    val notesFlow = MutableSharedFlow<List<Note>>(replay = 1)
    private val _userFlow = MutableStateFlow(User(firstName = "Unknown", lastName = "Unknown"))
    val userFlow: Flow<User> = _userFlow
    val user get() = _userFlow.value

    fun setUserFirstName(name: String) {
        _userFlow.value = _userFlow.value.copy(firstName = name)
    }

    fun setUserLastName(name: String) {
        _userFlow.value = _userFlow.value.copy(lastName = name)
    }

    fun addNote(note: String) {
        notes.add(Note(note = note))
        emitNotes()
    }

    fun editNote(note: Note, position: Int) {
        notes[position] = note
        emitNotes()
    }

    fun deleteNote(position: Int) {
        notes.removeAt(position)
        emitNotes()
    }

    fun emitNotes(){
        viewModelScope.launch {
            val new = mutableListOf<Note>()
            new.addAll(notes)
            notesFlow.emit(new)
        }

    }
}

data class Note(val id: UUID = UUID.randomUUID(), val note: String, val isDone: Boolean = false)
data class User(val firstName: String, val lastName: String)