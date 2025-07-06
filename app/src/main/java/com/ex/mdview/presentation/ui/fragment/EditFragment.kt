package com.ex.mdview.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        when (status) {
                            is OperationStatus.Idle -> {
                                binding.saveButton.isEnabled = true
                            }

                            is OperationStatus.Loading -> {
                                binding.saveButton.isEnabled = false
                            }

                            is OperationStatus.Success -> {
                                binding.saveButton.isEnabled = true
                            }

                            is OperationStatus.Error -> {
                                binding.saveButton.isEnabled = true
                            }
                        }
                    }
                }
                launch {
                    sharedViewModel.oneTimeMessage.collectLatest { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

        binding.saveButton.setOnClickListener {
            val editedContent = binding.editTextContent.text.toString()
            sharedViewModel.setContent(editedContent)
            sharedViewModel.saveCurrentDocument()
            findNavController().popBackStack()
        }
    }
}