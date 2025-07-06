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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.renderedMarkdownElements.collectLatest { elements ->
                    displayMarkdownElements(elements)
                }
            }
        }
    }

    /**
     * Динамически создает и добавляет View для каждого [MarkdownElement].
     * @param elements набор [MarkdownElement] для отображения.
     */
    private fun displayMarkdownElements(elements: List<MarkdownElement>) {
        binding.markdownContentContainer.removeAllViews()

        if (elements.isEmpty() || (elements.size == 1 && elements[0] is MarkdownElement.Paragraph && (elements[0] as MarkdownElement.Paragraph).text.isEmpty())) {
            binding.placeholderText.visibility = View.VISIBLE
            return
        } else {
            binding.placeholderText.visibility = View.GONE
        }

        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Heading -> {
                    val textView = TextView(context).apply {
                        text = element.text
                        setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            (24 - (element.level - 1) * 2).toFloat()
                        )
                        setTypeface(null, Typeface.BOLD)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(
                                0,
                                if (element.level == 1) dpToPx(16) else dpToPx(8),
                                0,
                                dpToPx(4)
                            )
                        }
                    }
                    binding.markdownContentContainer.addView(textView)
                }

                is MarkdownElement.Paragraph -> {
                    val textView = TextView(context).apply {
                        text = markdownTextFormatter.formatInlineText(element.text)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, dpToPx(4), 0, dpToPx(4))
                        }
                    }
                    binding.markdownContentContainer.addView(textView)
                }

                is MarkdownElement.Image -> {
                    val imageView = ImageView(context).apply {
                        contentDescription = element.altText
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        adjustViewBounds = true
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, dpToPx(8), 0, dpToPx(8))
                        }
                        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_ph))
                    }
                    binding.markdownContentContainer.addView(imageView)

                    loadImage(element.url, imageView)
                }

                is MarkdownElement.Table -> {
                    val tableLayout = TableLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, dpToPx(8), 0, dpToPx(8))
                        }
                        setBackgroundResource(R.drawable.table_border)
                    }

                    //Заголовки таблицы
                    val headerRow = TableRow(context).apply {
                        layoutParams = TableLayout.LayoutParams(
                            TableLayout.LayoutParams.WRAP_CONTENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    element.headers.forEach { header ->
                        val headerTextView = TextView(context).apply {
                            text = header
                            setTypeface(null, Typeface.BOLD)
                            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                            gravity = Gravity.CENTER // Центрируем текст
                            setBackgroundResource(R.drawable.cell_border)
                            minWidth = dpToPx(50)
                        }
                        headerRow.addView(headerTextView)
                    }
                    tableLayout.addView(headerRow)

                    //Cтроки таблицы
                    element.rows.forEach { rowData ->
                        val dataRow = TableRow(context).apply {
                            layoutParams = TableLayout.LayoutParams(
                                TableLayout.LayoutParams.WRAP_CONTENT,
                                TableLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                        rowData.forEach { cellData ->
                            val cellTextView = TextView(context).apply {
                                text = cellData
                                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                                gravity = Gravity.START
                                setBackgroundResource(R.drawable.cell_border)
                                minWidth = dpToPx(50)
                            }
                            dataRow.addView(cellTextView)
                        }
                        tableLayout.addView(dataRow)
                    }

                    val horizontalScrollView = HorizontalScrollView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, dpToPx(8), 0, dpToPx(8))
                        }
                        addView(tableLayout)
                    }
                    binding.markdownContentContainer.addView(horizontalScrollView)
                }
            }
        }
    }

    /** Асинхронноая загрузка изображений из URL. */
    private fun loadImage(imageUrl: String, imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap =
                    BitmapFactory.decodeStream(URL(imageUrl).openConnection().getInputStream())
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

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}