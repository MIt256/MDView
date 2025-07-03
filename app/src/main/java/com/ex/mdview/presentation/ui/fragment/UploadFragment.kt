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
import com.ex.mdview.presentation.viewmodel.LoadStatus
import com.ex.mdview.R
import com.ex.mdview.presentation.viewmodel.SharedViewModel
import com.ex.mdview.presentation.viewmodel.factory.SharedViewModelFactory
import com.ex.mdview.databinding.FragmentUploadBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: SharedViewModel

    private val filePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                if (isMarkdownFile(uri)) {
                    sharedViewModel.loadLocalFile(uri)
                } else {
                    showStatus("Выберите файл с расширением .md")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = SharedViewModelFactory(requireActivity().application)
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        observeViewModel()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "text/markdown",
                    "text/x-markdown",
                    "application/octet-stream",
                    "text/plain"
                )
            )

        }
        filePickerLauncher.launch(intent)
    }

    private fun isMarkdownFile(uri: Uri): Boolean {
        val mimeType = context?.contentResolver?.getType(uri)
        if (mimeType == "text/markdown" || mimeType == "text/x-markdown") {
            return true
        }
        return uri.path?.endsWith(".md", ignoreCase = true) ?: false
    }

    private fun loadFromUrl() {
        val url = binding.url.text.toString()
        if (url.isNotBlank()) {
            sharedViewModel.loadFromUrl(url)
        } else {
            binding.url.error = "Введите URL"
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.loadStatus.collectLatest { status ->
                    when (status) {
                        LoadStatus.Idle -> {
                            binding.editFragmentButton.visibility = View.INVISIBLE
                            binding.viewFragmentButton.visibility = View.INVISIBLE
                            binding.imageView.visibility = View.INVISIBLE
                        }

                        LoadStatus.Loading -> {
                            binding.editFragmentButton.visibility = View.INVISIBLE
                            binding.viewFragmentButton.visibility = View.INVISIBLE
                            binding.imageView.visibility = View.INVISIBLE
                        }

                        LoadStatus.Success -> {
                            binding.editFragmentButton.visibility = View.VISIBLE
                            binding.viewFragmentButton.visibility = View.VISIBLE
                            binding.imageView.visibility = View.VISIBLE
                            binding.imageView.setImageResource(R.drawable.good_status)
                            showStatus("Документ успешно загружен!")
                        }

                        is LoadStatus.Error -> {
                            binding.editFragmentButton.visibility = View.VISIBLE
                            binding.viewFragmentButton.visibility = View.VISIBLE
                            binding.imageView.visibility = View.VISIBLE
                            binding.imageView.setImageResource(R.drawable.error_status)
                            showStatus(status.message)
                        }
                    }
                }
            }
        }
    }

    private fun showStatus(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}