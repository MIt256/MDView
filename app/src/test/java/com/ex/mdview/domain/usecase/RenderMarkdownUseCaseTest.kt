package com.ex.mdview.domain.usecase

import com.ex.mdview.domain.model.MarkdownElement
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class RenderMarkdownUseCaseTest {

    private lateinit var renderMarkdownUseCase: RenderMarkdownUseCase

    @Before
    fun setup() {
        renderMarkdownUseCase = RenderMarkdownUseCase()
    }

    @Test
    fun `renderMarkdown_emptyString_returnsOneEmptyLine`() {
        val markdown = ""
        val result = renderMarkdownUseCase(markdown)
        assertTrue("Result list should be empty for empty markdown", result.size == 1)
    }

    @Test
    fun `renderMarkdown_simpleParagraph_returnsParagraphElement`() {
        val markdown = "This is a simple paragraph."
        val result = renderMarkdownUseCase(markdown)
        assertEquals("Result list should have 1 element", 1, result.size)
        assertTrue("Element should be a Paragraph", result[0] is MarkdownElement.Paragraph)
        val paragraph = result[0] as MarkdownElement.Paragraph
        assertEquals("Paragraph text should match", "This is a simple paragraph.", paragraph.text)
    }

    @Test
    fun `renderMarkdown_multipleParagraphs_returnsMultipleParagraphElements`() {
        val markdown = "Paragraph 1.\n\nParagraph 2."
        val result = renderMarkdownUseCase(markdown)
        assertEquals("Result list should have 2 elements", 3, result.size)

        assertTrue("First element should be a Paragraph", result[0] is MarkdownElement.Paragraph)
        assertEquals(
            "First paragraph text should match",
            "Paragraph 1.",
            (result[0] as MarkdownElement.Paragraph).text
        )

        assertTrue("First element should be a Paragraph", result[1] is MarkdownElement.EmptyLine)

        assertTrue("Second element should be a Paragraph", result[2] is MarkdownElement.Paragraph)
        assertEquals(
            "Second paragraph text should match",
            "Paragraph 2.",
            (result[2] as MarkdownElement.Paragraph).text
        )
    }

    @Test
    fun `renderMarkdown_h1Heading_returnsHeadingElementLevel1`() {
        val markdown = "# Main Heading"
        val result = renderMarkdownUseCase(markdown)
        assertEquals("Result list should have 1 element", 1, result.size)
        assertTrue("Element should be a Heading", result[0] is MarkdownElement.Heading)
        val heading = result[0] as MarkdownElement.Heading
        assertEquals("Heading text should match", "Main Heading", heading.text)
        assertEquals("Heading level should be 1", 1, heading.level)
    }

    @Test
    fun `renderMarkdown_h3Heading_returnsHeadingElementLevel3`() {
        val markdown = "### Sub Heading"
        val result = renderMarkdownUseCase(markdown)
        assertEquals("Result list should have 1 element", 1, result.size)
        assertTrue("Element should be a Heading", result[0] is MarkdownElement.Heading)
        val heading = result[0] as MarkdownElement.Heading
        assertEquals("Heading text should match", "Sub Heading", heading.text)
        assertEquals("Heading level should be 3", 3, heading.level)
    }

    @Test
    fun `renderMarkdown_image_returnsImageElement`() {
        val markdown = "![Alt text for image](http://example.com/image.png)"
        val result = renderMarkdownUseCase(markdown)
        assertEquals("Result list should have 1 element", 1, result.size)
        assertTrue("Element should be an Image", result[0] is MarkdownElement.Image)
        val image = result[0] as MarkdownElement.Image
        assertEquals("Image alt text should match", "Alt text for image", image.altText)
        assertEquals("Image URL should match", "http://example.com/image.png", image.url)
    }

    @Test
    fun `renderMarkdown_table_returnsTableElement`() {
        val markdown = """
            | Header 1 | Header 2 | Header 3 |
            |----------|:--------:|---------:|
            | Row 1 Col 1 | Row 1 Col 2 | Row 1 Col 3 |
            | Row 2 Col 1 | Row 2 Col 2 | Row 2 Col 3 |
        """.trimIndent()
        val result = renderMarkdownUseCase(markdown)
        assertEquals("Result list should have 1 element", 1, result.size)
        assertTrue("Element should be a Table", result[0] is MarkdownElement.Table)
        val table = result[0] as MarkdownElement.Table

        assertEquals("Table headers count should match", 3, table.headers.size)
        assertEquals("Header 1 should match", "Header 1", table.headers[0])
        assertEquals("Header 2 should match", "Header 2", table.headers[1])
        assertEquals("Header 3 should match", "Header 3", table.headers[2])

        assertEquals("Table rows count should match", 2, table.rows.size)

        assertEquals("Row 1, Col 1 should match", "Row 1 Col 1", table.rows[0][0])
        assertEquals("Row 1, Col 2 should match", "Row 1 Col 2", table.rows[0][1])
        assertEquals("Row 1, Col 3 should match", "Row 1 Col 3", table.rows[0][2])

        assertEquals("Row 2, Col 1 should match", "Row 2 Col 1", table.rows[1][0])
        assertEquals("Row 2, Col 2 should match", "Row 2 Col 2", table.rows[1][1])
        assertEquals("Row 2, Col 3 should match", "Row 2 Col 3", table.rows[1][2])
    }

    @Test
    fun `renderMarkdown_complexDocument_returnsCorrectElementsInOrder`() {
        val markdown = """
            # Welcome

            This is a paragraph.

            ## Features

            - Feature 1
            - Feature 2

            ![Sample Image](http://example.com/sample.jpg)

            | Name  | Age | City    |
            |-------|-----|---------|
            | Alice | 30  | New York|
            | Bob   | 24  | London  |
        """.trimIndent()

        val result = renderMarkdownUseCase(markdown)
        assertEquals(
            "Result list should have 12 elements",
            12,
            result.size
        )

        // Element 0: Heading 1
        assertTrue("Element 0 should be a Heading", result[0] is MarkdownElement.Heading)
        assertEquals(
            "Heading 1 text should match",
            "Welcome",
            (result[0] as MarkdownElement.Heading).text
        )
        assertEquals("Heading 1 level should be 1", 1, (result[0] as MarkdownElement.Heading).level)

        // Element 1: EmptyLine
        assertTrue("Element 1 should be EmptyLine", result[1] is MarkdownElement.EmptyLine)

        // Element 2: Paragraph
        assertTrue("Element 2 should be a Paragraph", result[2] is MarkdownElement.Paragraph)
        assertEquals(
            "Paragraph text should match",
            "This is a paragraph.",
            (result[2] as MarkdownElement.Paragraph).text
        )

        // Element 3: EmptyLine
        assertTrue("Element 3 should be EmptyLine", result[3] is MarkdownElement.EmptyLine)

        // Element 4: Heading 2
        assertTrue("Element 4 should be a Heading", result[4] is MarkdownElement.Heading)
        assertEquals(
            "Heading 2 text should match",
            "Features",
            (result[4] as MarkdownElement.Heading).text
        )
        assertEquals("Heading 2 level should be 2", 2, (result[4] as MarkdownElement.Heading).level)

        // Element 5: EmptyLine
        assertTrue("Element 5 should be EmptyLine", result[5] is MarkdownElement.EmptyLine)

        // Element 6: Paragraph (for "- Feature 1")
        assertTrue("Element 6 should be a Paragraph", result[6] is MarkdownElement.Paragraph)
        assertEquals(
            "First list item should match",
            "- Feature 1",
            (result[6] as MarkdownElement.Paragraph).text
        )

        // Element 7: Paragraph (for "- Feature 2")
        assertTrue("Element 7 should be a Paragraph", result[7] is MarkdownElement.Paragraph)
        assertEquals(
            "Second list item should match",
            "- Feature 2",
            (result[7] as MarkdownElement.Paragraph).text
        )

        // Element 8: EmptyLine
        assertTrue("Element 8 should be EmptyLine", result[8] is MarkdownElement.EmptyLine)

        // Element 9: Image
        assertTrue("Element 9 should be an Image", result[9] is MarkdownElement.Image)
        assertEquals(
            "Image alt text should match",
            "Sample Image",
            (result[9] as MarkdownElement.Image).altText
        )

        // Element 10: EmptyLine
        assertTrue("Element 10 should be EmptyLine", result[10] is MarkdownElement.EmptyLine)

        // Element 11: Table
        assertTrue("Element 11 should be a Table", result[11] is MarkdownElement.Table)
        val table = result[11] as MarkdownElement.Table
        assertEquals("Table headers count should match", 3, table.headers.size)
        assertEquals("Table row count should match", 2, table.rows.size)
    }

    @Test
    fun `renderMarkdown_markdownWithLeadingTrailingSpaces_trimsContent`() {
        val markdown =
            " #  Title with spaces \n\n   Paragraph with spaces.   \n\n![  Alt  ](  http://url.com/img.png  )"
        val result = renderMarkdownUseCase(markdown)

        assertEquals("Result list should have 5 elements", 5, result.size)

        // Heading
        assertTrue("Element 0 should be a Heading", result[0] is MarkdownElement.Heading)
        assertEquals(
            "Heading text should be trimmed",
            "Title with spaces",
            (result[0] as MarkdownElement.Heading).text
        )

        // EmptyLine
        assertTrue("Element 1 should be EmptyLine", result[1] is MarkdownElement.EmptyLine)

        // Paragraph
        assertTrue("Element 2 should be a Paragraph", result[2] is MarkdownElement.Paragraph)
        assertEquals(
            "Paragraph text should be trimmed",
            "Paragraph with spaces.",
            (result[2] as MarkdownElement.Paragraph).text
        )

        // EmptyLine
        assertTrue("Element 3 should be EmptyLine", result[3] is MarkdownElement.EmptyLine)

        // Image
        assertTrue("Element 4 should be an Image", result[4] is MarkdownElement.Image)
        assertEquals(
            "Image alt text should be trimmed",
            "Alt",
            (result[4] as MarkdownElement.Image).altText
        )
        assertEquals(
            "Image URL should be trimmed",
            "http://url.com/img.png",
            (result[4] as MarkdownElement.Image).url
        )
    }

    @Test
    fun `renderMarkdown_tableWithNoContentRows_returnsTableWithHeadersAndEmptyRows`() {
        val markdown = """
            | Header A | Header B |
            |----------|----------|
        """.trimIndent()
        val result = renderMarkdownUseCase(markdown)
        assertEquals(1, result.size)
        assertTrue(result[0] is MarkdownElement.Table)
        val table = result[0] as MarkdownElement.Table
        assertEquals(listOf("Header A", "Header B"), table.headers)
        assertTrue(table.rows.isEmpty())
    }

    @Test
    fun `renderMarkdown_tableWithOnlyHeadersAndSeparator_returnsTableWithNoRows`() {
        val markdown = """
            | Col1 | Col2 |
            |------|------|
        """.trimIndent()
        val result = renderMarkdownUseCase(markdown)
        assertEquals(1, result.size)
        assertTrue(result[0] is MarkdownElement.Table)
        val table = result[0] as MarkdownElement.Table
        assertEquals(listOf("Col1", "Col2"), table.headers)
        assertTrue(table.rows.isEmpty())
    }

    @Test
    fun `renderMarkdown_emptyLinesBetweenElements_areNotIgnored`() {
        val markdown = """
            # Heading 1


            Paragraph 1 


            ## Heading 2
        """.trimIndent()
        val result = renderMarkdownUseCase(markdown)
        assertEquals(7, result.size)
        assertTrue(result[0] is MarkdownElement.Heading)
        assertTrue(result[1] is MarkdownElement.EmptyLine)
        assertTrue(result[2] is MarkdownElement.EmptyLine)
        assertTrue(result[3] is MarkdownElement.Paragraph)
        assertTrue(result[4] is MarkdownElement.EmptyLine)
        assertTrue(result[5] is MarkdownElement.EmptyLine)
        assertTrue(result[6] is MarkdownElement.Heading)
    }

    @Test
    fun `renderMarkdown_mixedElementsWithDifferentSeparators`() {
        val markdown = """
            # Title
            Some text.
            ![image](url)
            | A | B |
            |---|---|
            | 1 | 2 |
        """.trimIndent()
        val result = renderMarkdownUseCase(markdown)
        assertEquals(4, result.size)
        assertTrue(result[0] is MarkdownElement.Heading)
        assertTrue(result[1] is MarkdownElement.Paragraph)
        assertTrue(result[2] is MarkdownElement.Image)
        assertTrue(result[3] is MarkdownElement.Table)
    }

    @Test
    fun `renderMarkdown_textFollowedByHeadingWithoutNewline_behavesAsParagraph`() {
        val markdown = "This is a paragraph.#NotAHeading"
        val result = renderMarkdownUseCase(markdown)
        assertEquals(1, result.size)
        assertTrue(result[0] is MarkdownElement.Paragraph)
        assertEquals(
            "This is a paragraph.#NotAHeading",
            (result[0] as MarkdownElement.Paragraph).text
        )
    }

    @Test
    fun `renderMarkdown_tableAndHeadingWithInlineFormatting_preservesMarkdownSyntax`() {
        val markdown = """
            # *Italic Heading*

            | Name      | Status         |
            |-----------|---------------|
            | **Alice** | *Active*       |
            | ~~Bob~~   | **_Inactive_** |
        """.trimIndent()
        val result = renderMarkdownUseCase(markdown)
        // Ожидаем: Heading, EmptyLine, Table
        assertEquals(3, result.size)
        // Heading с markdown-разметкой
        assertTrue(result[0] is MarkdownElement.Heading)
        assertEquals("*Italic Heading*", (result[0] as MarkdownElement.Heading).text)
        // EmptyLine
        assertTrue(result[1] is MarkdownElement.EmptyLine)
        // Таблица с markdown-разметкой в ячейках
        assertTrue(result[2] is MarkdownElement.Table)
        val table = result[2] as MarkdownElement.Table
        assertEquals(listOf("Name", "Status"), table.headers)
        assertEquals(2, table.rows.size)
        assertEquals("**Alice**", table.rows[0][0])
        assertEquals("*Active*", table.rows[0][1])
        assertEquals("~~Bob~~", table.rows[1][0])
        assertEquals("**_Inactive_**", table.rows[1][1])
    }

}