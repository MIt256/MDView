package com.ex.mdview.presentation.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ex.mdview.R
import com.ex.mdview.databinding.FragmentUploadBinding
import com.ex.mdview.presentation.ui.util.viewBinding
import com.ex.mdview.presentation.viewmodel.OperationStatus
import com.ex.mdview.presentation.viewmodel.SharedViewModel
import com.ex.mdview.presentation.viewmodel.factory.SharedViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Фрагмент для загрузки Markdown-документов.
 * Предоставляет UI для выбора локального файла или ввода URL для загрузки Markdown.
 * Взаимодействует с [SharedViewModel] для выполнения операций загрузки и отслеживания статуса.
 */
class UploadFragment : Fragment(R.layout.fragment_upload) {

    private val binding by viewBinding(FragmentUploadBinding::bind)
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = SharedViewModelFactory(requireActivity().application)
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        with(binding) {
            localButton.setOnClickListener { openFilePicker() }
            webButton.setOnClickListener { loadFromUrl() }
            viewFragmentButton.setOnClickListener {
                findNavController().navigate(
                    UploadFragmentDirections.actionUploadFragmentToViewFragment()
                )
            }
            editFragmentButton.setOnClickListener {
                findNavController().navigate(
                    UploadFragmentDirections.actionUploadFragmentToEditFragment()
                )
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "text/markdown", "text/x-markdown",
                    "application/octet-stream", "text/plain"
                )
            )
        }
        filePickerLauncher.launch(intent)
    }

    private val filePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                if (isMarkdownFile(uri)) {
                    sharedViewModel.loadLocalFile(uri)
                } else {
                    showMessage(getString(R.string.select_md_file_error))
                }
            }
        }

    private fun isMarkdownFile(uri: Uri): Boolean {
        val mimeType = context?.contentResolver?.getType(uri)
        return mimeType == "text/markdown" || mimeType == "text/x-markdown" ||
                uri.path?.endsWith(".md", ignoreCase = true) == true ||
                uri.path?.endsWith(".markdown", ignoreCase = true) == true
    }

    private fun loadFromUrl() {
        val url = binding.url.text.toString()
        if (url.isNotBlank()) {
            sharedViewModel.loadFromUrl(url)
        } else {
            binding.url.error = getString(R.string.enter_url_error)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.operationStatus.collectLatest { status ->
                        updateUiByStatus(status)
                    }
                }
                launch {
                    sharedViewModel.oneTimeMessage.collectLatest { message ->
                        showMessage(message)
                    }
                }
            }
        }
    }

    private fun updateUiByStatus(status: OperationStatus) {
        with(binding) {
            when (status) {
                OperationStatus.Idle, OperationStatus.Loading -> {
                    editFragmentButton.visibility = View.INVISIBLE
                    viewFragmentButton.visibility = View.INVISIBLE
                    imageView.visibility = View.INVISIBLE
                }
                OperationStatus.Success -> {
                    editFragmentButton.visibility = View.VISIBLE
                    viewFragmentButton.visibility = View.VISIBLE
                    imageView.visibility = View.VISIBLE
                    imageView.setImageResource(R.drawable.good_status)
                }
                is OperationStatus.Error -> {
                    editFragmentButton.visibility = View.INVISIBLE
                    viewFragmentButton.visibility = View.INVISIBLE
                    imageView.visibility = View.VISIBLE
                    imageView.setImageResource(R.drawable.error_status)
                }
            }
        }
    }

    private fun showMessage(async: String) {
        Toast.makeText(context, async, Toast.LENGTH_SHORT).show()
    }
}