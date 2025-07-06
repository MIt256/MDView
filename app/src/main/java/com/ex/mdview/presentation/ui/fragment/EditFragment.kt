package com.ex.mdview.presentation.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ex.mdview.R
import com.ex.mdview.databinding.FragmentEditBinding
import com.ex.mdview.presentation.ui.util.viewBinding
import com.ex.mdview.presentation.viewmodel.OperationStatus
import com.ex.mdview.presentation.viewmodel.SharedViewModel
import com.ex.mdview.presentation.viewmodel.factory.SharedViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Фрагмент для загрузки Markdown-документов.
 * Предоставляет UI для выбора локального файла или ввода URL для загрузки Markdown.
 * Взаимодействует с [SharedViewModel] для доступа к Markdown-документу и сохранению
 */
class EditFragment : Fragment() {

    private val binding by viewBinding(FragmentEditBinding::bind)
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = SharedViewModelFactory(requireActivity().application)
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupListeners()
        setupToolbar()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.documentContent.collectLatest { content ->
                    if (binding.editTextContent.text.toString() != content) {
                        binding.editTextContent.setText(content)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.operationStatus.collectLatest { status ->
                        binding.saveButton.isEnabled = status !is OperationStatus.Loading
                    }
                }
                launch {
                    sharedViewModel.oneTimeMessage.collectLatest { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            val editedContent = binding.editTextContent.text.toString()
            sharedViewModel.setContent(editedContent)
            if (sharedViewModel.isCurrentDocumentLocal()) {
                sharedViewModel.saveCurrentDocument()
                findNavController().popBackStack()
            } else {
                createFileLauncher.launch(getString(R.string.default_markdown_filename))
            }
        }
    }

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri: Uri? ->
            uri?.let {
                val editedContent = binding.editTextContent.text.toString()
                sharedViewModel.setContent(editedContent)
                sharedViewModel.saveCurrentDocument(it)
                findNavController().popBackStack()
            }
        }

    private fun setupToolbar() {
        binding.btnBold.setOnClickListener {
            formatSelectedText("**", "**")
        }
        binding.btnItalic.setOnClickListener {
            formatSelectedText("*", "*")
        }
        binding.btnStrike.setOnClickListener {
            formatSelectedText("~~", "~~")
        }
        binding.btnHeader.setOnClickListener {
            formatHeader()
        }
    }

    private fun formatSelectedText(prefix: String, suffix: String) {
        val editText = binding.editTextContent
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < 0 || end < 0) return
        val text = editText.text
        val selected = text.substring(start, end)
        val newText = prefix + selected + suffix
        text.replace(start, end, newText)
        editText.setSelection(start + newText.length)
    }

    private fun formatHeader() {
        val editText = binding.editTextContent
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < 0 || end < 0) return
        val text = editText.text
        val selected = text.substring(start, end)
        val lines = selected.lines()
        val newText = lines.joinToString("\n") { line ->
            if (line.startsWith("# ")) line else "# $line"
        }
        text.replace(start, end, newText)
        editText.setSelection(start + newText.length)
    }
}