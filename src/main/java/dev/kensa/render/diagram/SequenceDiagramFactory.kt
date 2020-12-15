package dev.kensa.render.diagram

import dev.kensa.Kensa.configuration
import dev.kensa.render.diagram.directive.UmlDirective
import dev.kensa.state.CapturedInteractions
import dev.kensa.util.KensaMap
import net.sourceforge.plantuml.FileFormat.SVG
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream

private val keyPattern = "(.*) from (\\w+) to (\\w+)".toRegex()
private val valuePatterns = listOf(
    "^\\.\\.\\..*\\.\\.\\.$".toRegex(),
    "^==.*==$".toRegex()
)

object SequenceDiagramFactory {
    private val participants: List<String>
        get() = configuration.umlDirectives.flatMap { uml: UmlDirective -> uml.asUml() }

    fun create(interactions: CapturedInteractions): SequenceDiagram? =
        eventsFrom(interactions).takeUnless { it.isEmpty() }?.let { events ->
            createSvg(
                (participants + events)
                    .joinToString("\n", "@startuml\n", "\n@enduml")
            )
        }

    private fun eventsFrom(interactions: CapturedInteractions): List<String> {
        return ArrayList<String>().also { events ->
            interactions.entrySet()
                .filter(IsSvgCompatible)
                .map(ToGroupedSvg)
                .groupByTo(LinkedHashMap(), { it.first }, { it.second })
                .forEach { (group, lines) ->
                    group?.takeIf { it.isNotEmpty() }?.let { g ->
                        events.add("group $g")
                        events.addAll(lines)
                        events.add("end")
                    } ?: events.addAll(lines)
                }
        }
    }

    private fun createSvg(plantUmlMarkup: String): SequenceDiagram =
        ByteArrayOutputStream().use { os ->
            SourceStringReader(plantUmlMarkup).generateImage(os, FileFormatOption(SVG))
            SequenceDiagram(os.toString())
        }
}

object IsSvgCompatible : (KensaMap.Entry) -> Boolean {
    override fun invoke(entry: KensaMap.Entry) =
        (keyPattern.matches(entry.key.toLowerCase()) || valuePatterns.any { p -> p.matches(entry.value.toString()) })

}

object ToGroupedSvg : (KensaMap.Entry) -> Pair<String?, String> {
    override fun invoke(entry: KensaMap.Entry): Pair<String?, String> =
        keyPattern.matchEntire(entry.key)?.let { result ->
            val interactionId = entry.key.hashCode().toString()
            val interactionName = result.groupValues[1].trim()
            val from = result.groupValues[2].trim()
            val to = result.groupValues[3].trim()

            Pair(
                entry.attributes.group,
                """$from ${entry.attributes.arrowStyle.value} ${to}:<text class=sequence_diagram_clickable sequence_diagram_interaction_id="$interactionId">${interactionName}</text>"""
            )
        } ?: Pair(entry.attributes.group, entry.value.toString().trim())
}