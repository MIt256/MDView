package com.ex.mdview.presentation.ui.fragment

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ex.mdview.R
import com.ex.mdview.databinding.FragmentViewBinding
import com.ex.mdview.domain.model.MarkdownElement
import com.ex.mdview.presentation.ui.util.viewBinding
import com.ex.mdview.presentation.util.MarkdownTextFormatter
import com.ex.mdview.presentation.viewmodel.SharedViewModel
import com.ex.mdview.presentation.viewmodel.factory.SharedViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Фрагмент для просмотра Markdown-документа.
 * Динамически отображает элементы Markdown, разобранные [RenderMarkdownUseCase].
 */
class ViewFragment : Fragment() {

    private val binding by viewBinding(FragmentViewBinding::bind)
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var markdownTextFormatter: MarkdownTextFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = SharedViewModelFactory(requireActivity().application)
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]
        markdownTextFormatter = MarkdownTextFormatter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.renderedMarkdownElements.collectLatest { elements ->
                        renderMarkdownContent(elements)
                    }
                }
                launch {
                    sharedViewModel.oneTimeMessage.collectLatest { message ->
                        showToastMessage(message)
                    }
                }
            }
        }
    }

    /**
     * Основной метод рендеринга Markdown-контента
     * @param elements список элементов Markdown для отображения
     */
    private fun renderMarkdownContent(elements: List<MarkdownElement>) {
        binding.markdownContentContainer.removeAllViews()

        if (shouldShowPlaceholder(elements)) {
            binding.placeholderText.visibility = View.VISIBLE
            return
        }
        binding.placeholderText.visibility = View.GONE

        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Heading -> renderHeading(element)
                is MarkdownElement.Paragraph -> renderParagraph(element)
                is MarkdownElement.Image -> renderImage(element)
                is MarkdownElement.Table -> renderTable(element)
            }
        }
    }

    private fun shouldShowPlaceholder(elements: List<MarkdownElement>): Boolean {
        return elements.isEmpty() || (elements.size == 1 &&
                elements[0] is MarkdownElement.Paragraph &&
                (elements[0] as MarkdownElement.Paragraph).text.isEmpty())
    }

    private fun renderHeading(heading: MarkdownElement.Heading) {
        TextView(context).apply {
            text = heading.text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, (24 - (heading.level - 1) * 2).toFloat())
            setTypeface(null, Typeface.BOLD)
            setLayoutParams(
                topMargin = if (heading.level == 1) 16 else 8,
                bottomMargin = 4
            )
            binding.markdownContentContainer.addView(this)
        }
    }

    private fun renderParagraph(paragraph: MarkdownElement.Paragraph) {
        TextView(context).apply {
            text = markdownTextFormatter.formatInlineText(paragraph.text)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setLayoutParams(topMargin = 4, bottomMargin = 4)
            binding.markdownContentContainer.addView(this)
        }
    }

    private fun renderImage(image: MarkdownElement.Image) {
        ImageView(context).apply {
            contentDescription = image.altText
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setLayoutParams(topMargin = 8, bottomMargin = 8)
            setImageResource(R.drawable.img_ph)
            binding.markdownContentContainer.addView(this)
            loadRemoteImage(image.url, this)
        }
    }

    private fun renderTable(table: MarkdownElement.Table) {
        HorizontalScrollView(context).apply {
            setLayoutParams(topMargin = 8, bottomMargin = 8)

            addView(TableLayout(context).apply {
                setBackgroundResource(R.drawable.table_border)

                addView(createTableRow(table.headers, Typeface.DEFAULT_BOLD, Gravity.CENTER))

                table.rows.forEach { row ->
                    addView(createTableRow(row, null, Gravity.START))
                }
            })
            binding.markdownContentContainer.addView(this)
        }
    }

    private fun createTableRow(
        cells: List<String>,
        typeface: Typeface?,
        gravity: Int,
    ): TableRow {
        return TableRow(context).apply {
            cells.forEach { cell ->
                addView(TextView(context).apply {
                    text = cell
                    typeface?.let { setTypeface(it) }
                    setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                    this.gravity = gravity
                    setBackgroundResource(R.drawable.cell_border)
                    minWidth = dpToPx(50)
                })
            }
        }
    }

    private fun loadRemoteImage(imageUrl: String, imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeStream(
                    URL(imageUrl).openConnection().getInputStream()
                )
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.img_ph)
                }
            }
        }
    }

    private fun View.setLayoutParams(
        leftMargin: Int = 0,
        topMargin: Int = 0,
        rightMargin: Int = 0,
        bottomMargin: Int = 0,
    ) {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                dpToPx(leftMargin),
                dpToPx(topMargin),
                dpToPx(rightMargin),
                dpToPx(bottomMargin)
            )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}