package dev.kensa.sentence

enum class TokenType(private val css: String, val isWhitespace : Boolean = false) {
    Acronym("ac"),
    BlankLine("bl", true),
    BooleanLiteral("bo"),
    CharacterLiteral("cl"),
    Expandable("ex", true),
    FieldValue("fv"),
    Highlighted("hl"),
    HighlightedIdentifier("hlid"),
    Identifier("id"),
    Indent("in", true),
    Keyword("kw"),
    MethodValue("mv"),
    NewLine("nl", true),
    NullLiteral("null"),
    NumberLiteral("num"),
    Operator("op"),
    ParameterValue("pv"),
    ScenarioValue("sv"),
    StringLiteral("sl"),
    Word("wd");

    fun asCss(): String = "tk-$css"
}