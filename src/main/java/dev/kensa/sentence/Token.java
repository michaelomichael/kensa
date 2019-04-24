package dev.kensa.sentence;

public interface Token {
    Type type();
    String asString();

    enum Type {
        Acronym,
        Keyword,
        StringLiteral,
        Literal,
        NewLine,
        Identifier,
        Word
    }
}
