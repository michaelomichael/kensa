package dev.kensa.sentence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.kensa.sentence.SentenceTokens.*;
import static org.assertj.core.api.Assertions.assertThat;

class SentenceBuilderTest {

    private SentenceBuilder builder;

    @BeforeEach
    void setUp() {
        Dictionary dictionary = new Dictionary();
        dictionary.putAcronyms(Acronym.of("FOO", ""), Acronym.of("BAR", ""));

        builder = new SentenceBuilder(1, Set.of("highlighted"), dictionary.keywordPattern(), dictionary.acronymPattern());
    }

    @Test
    void canConstructASentenceFromVariousValueTypes() {
        builder.append("givenFOOMooBar")
               .appendLiteral("literal1")
               .markLineNumber(2)
               .appendStringLiteral("stringLiteral1")
               .appendIdentifier("parameter1")
               .appendIdentifier("highlighted");

        assertThat(builder.build().stream())
                .containsExactly(
                        aKeywordOf("Given"),
                        anAcronymOf("FOO"),
                        aWordOf("moo"),
                        anAcronymOf("BAR"),
                        aLiteralOf("literal1"),
                        aNewline(),
                        aStringLiteralOf("stringLiteral1"),
                        anIdentifierOf("parameter1"),
                        aHighlightedIdentifierOf("highlighted")
                );
    }
}