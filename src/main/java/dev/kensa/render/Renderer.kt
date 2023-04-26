package dev.kensa.render

import dev.kensa.render.Language.PlainText
import dev.kensa.util.NamedValue

enum class Language(val value: String) {
    Json("json"),
    PlainText("plainText"),
    Xml("xml");
}

@Deprecated("Deprecated", replaceWith = ReplaceWith("ValueRenderer<T>", "dev.kensa.render"))
fun interface Renderer<T> : ValueRenderer<T>

fun interface ValueRenderer<T> {
    fun render(value: T) : String
}

interface InteractionRenderer<T> {
    fun render(value: T) : List<RenderedInteraction>
    fun renderAttributes(value: T): List<RenderedAttributes>
}

data class RenderedInteraction(val name: String, val value: String, val showOnSequenceDiagram: Boolean = true, val language: Language = PlainText)
data class RenderedAttributes(val name: String, val attributes: Set<NamedValue>, val showOnSequenceDiagram: Boolean = true)
