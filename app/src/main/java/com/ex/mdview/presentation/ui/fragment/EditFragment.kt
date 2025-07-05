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
import com.ex.mdview.databinding.FragmentEditBinding
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

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
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
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
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
                sharedViewModel.operationStatus.collectLatest { status ->
                    when (status) {
                        OperationStatus.Idle -> {
                        }

                        OperationStatus.Loading -> {
                            Toast.makeText(context, "Сохранение...", Toast.LENGTH_SHORT).show()
                        }

                        OperationStatus.Success -> {
                            Toast.makeText(context, "Сохранено успешно!", Toast.LENGTH_SHORT).show()

                        }

                        is OperationStatus.Error -> {
                            Toast.makeText(
                                context,
                                "Ошибка сохранения: ${status.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}