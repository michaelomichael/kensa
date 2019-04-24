package dev.kensa.parse;

import dev.kensa.render.Renderers;
import dev.kensa.sentence.Dictionary;
import dev.kensa.sentence.Sentence;
import dev.kensa.sentence.Sentences;
import dev.kensa.util.NameValuePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.javaparser.JavaParser.parse;
import static dev.kensa.sentence.SentenceTokens.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MethodParserTest {

    @AfterEach
    void tearDown() {
        Dictionary.clearAcronyms();
    }

    @Test
    void canParseMethodWithAcronymsInStatement() {
        Dictionary.putAcronym("KCI");

        String code = "class T {\n" +
                "   void testMethod() {\n" +
                "      given(aKCIOf(\"On\"));\n" +
                "   }\n" +
                "}\n";


        List<Sentence> sentences = parseToSentences(code);

        assertThat(sentences).hasSize(1);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("a"),
                anAcronymOf("KCI"),
                aWordOf("of"),
                aStringLiteralOf("On")
        );
    }

    @Test
    void canParseSimpleSingleLineStatementsWithStringLiterals() {
        String code = "class T {\n" +
                "   void testMethod() {\n" +
                "      given(aLightIs(\"On\"));\n" +
                "      when(theLightIsSwitchedOff());\n" +
                "      then(theLightIs(\"Off\"));\n" +
                "   }\n" +
                "}\n";


        List<Sentence> sentences = parseToSentences(code);

        assertThat(sentences).hasSize(3);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("a"),
                aWordOf("light"),
                aWordOf("is"),
                aStringLiteralOf("On")
        );
        assertThat(sentences.get(1).stream()).containsExactly(
                aKeywordOf("When"),
                aWordOf("the"),
                aWordOf("light"),
                aWordOf("is"),
                aWordOf("switched"),
                aWordOf("off")
        );
        assertThat(sentences.get(2).stream()).containsExactly(
                aKeywordOf("Then"),
                aWordOf("the"),
                aWordOf("light"),
                aWordOf("is"),
                aStringLiteralOf("Off")
        );
    }

    @Test
    void canParseAMultilineStatementAndPreserveLineBreaks() {
        String code = "class T {\n" +
                "   void testMethod() {\n" +
                "      then(theLightIs(\"Off\"))\n" +
                "         .and(itIsDark())\n" +
                "         .and(monstersComeOut());\n" +
                "   }\n" +
                "}\n";

        List<Sentence> sentences = parseToSentences(code);

        assertThat(sentences).hasSize(1);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Then"),
                aWordOf("the"),
                aWordOf("light"),
                aWordOf("is"),
                aStringLiteralOf("Off"),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("it"),
                aWordOf("is"),
                aWordOf("dark"),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("monsters"),
                aWordOf("come"),
                aWordOf("out")
        );
    }

    @Test
    void canParseStatementAndResolveScenarioMethodValues() {
        String code = "class T {\n" +
                "   private Object myScenario;" +
                "   void testMethod() {\n" +
                "      then(theExpectedValue(), is(myScenario.aMethod()));\n" +
                "   }\n" +
                "}\n";

        String expectedScenarioValue = "someScenarioValue";

        CachingScenarioMethodAccessor scenarioAccessor = mock(CachingScenarioMethodAccessor.class);
        when(scenarioAccessor.valueOf("myScenario", "aMethod")).thenReturn(Optional.of(expectedScenarioValue));
        List<Sentence> sentences = parseToSentences(code, scenarioAccessor);

        assertThat(sentences).hasSize(1);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Then"),
                aWordOf("the"),
                aWordOf("expected"),
                aWordOf("value"),
                aWordOf("is"),
                anIdentifierOf(expectedScenarioValue)
        );
    }

    @Test
    void canParseStatementAndResolveParameterAndFieldValues() {
        String code = "class T {\n" +
                "   private String f1;" +
                "   private Integer f2;" +
                "   void testMethod(String p1, String p2, Integer p3) {\n" +
                "      given(theFirstFieldHasAValueOf(f1));" +
                "      given(theSecondFieldHasAValueOf(f2));" +
                "      then(theFirstParameterIs(p1))\n" +
                "         .and(theSecondParameterIs(p2))\n" +
                "         .and(theThirdParameterIs(p3));\n" +
                "   }\n" +
                "}\n";

        String fieldValue = "fieldValue";
        Integer renderedField = 555;

        Map<String, ? extends Serializable> fieldMap = Map.of("f1", fieldValue,
                                                              "f2", renderedField
        );

        String parameterValue1 = "parameterValue1";
        String parameterValue2 = "parameterValue2";
        Integer renderedParameter = 666;

        CachingFieldAccessor fieldAccessor = mock(CachingFieldAccessor.class);
        when(fieldAccessor.valueOf(any(String.class))).then(invocation -> Optional.ofNullable(fieldMap.get(invocation.<String>getArgument(0))));

        ParameterAccessor parameterAccessor = new ParameterAccessor(Set.of(
                new NameValuePair("p1", parameterValue1),
                new NameValuePair("p2", parameterValue2),
                new NameValuePair("p3", renderedParameter)
        ));

        List<Sentence> sentences = parseToSentences(code, fieldAccessor, parameterAccessor);

        assertThat(sentences).hasSize(3);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("the"),
                aWordOf("first"),
                aWordOf("field"),
                aWordOf("has"),
                aWordOf("a"),
                aWordOf("value"),
                aWordOf("of"),
                anIdentifierOf(fieldValue)
        );
        assertThat(sentences.get(1).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("the"),
                aWordOf("second"),
                aWordOf("field"),
                aWordOf("has"),
                aWordOf("a"),
                aWordOf("value"),
                aWordOf("of"),
                anIdentifierOf("<<555>>")
        );
        assertThat(sentences.get(2).stream()).containsExactly(
                aKeywordOf("Then"),
                aWordOf("the"),
                aWordOf("first"),
                aWordOf("parameter"),
                aWordOf("is"),
                anIdentifierOf(parameterValue1),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("the"),
                aWordOf("second"),
                aWordOf("parameter"),
                aWordOf("is"),
                anIdentifierOf(parameterValue2),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("the"),
                aWordOf("third"),
                aWordOf("parameter"),
                aWordOf("is"),
                anIdentifierOf("<<666>>")
        );
    }

    private List<Sentence> parseToSentences(String code) {
        return parseToSentences(code, null, null, null);
    }

    private List<Sentence> parseToSentences(String code, CachingScenarioMethodAccessor scenarioAccessor) {
        return parseToSentences(code, scenarioAccessor, null, null);
    }

    private List<Sentence> parseToSentences(String code, CachingFieldAccessor fieldAccessor, ParameterAccessor parameterAccessor) {
        return parseToSentences(code, null, fieldAccessor, parameterAccessor);
    }

    private List<Sentence> parseToSentences(String code, CachingScenarioMethodAccessor scenarioAccessor, CachingFieldAccessor fieldAccessor, ParameterAccessor parameterAccessor) {
        Renderers renderers = new Renderers();
        renderers.add(Integer.class, value -> String.format("<<%d>>", value));

        ValueAccessors valueAccessors = new ValueAccessors(renderers, scenarioAccessor, fieldAccessor, parameterAccessor);

        return parse(code).getClassByName("T")
                          .map(cd -> cd.getMethodsByName("testMethod").get(0))
                          .map(md -> new MethodParser(md, valueAccessors))
                          .map(MethodParser::sentences)
                          .map(Sentences::stream)
                          .map(s -> s.collect(toList()))
                          .orElseThrow(() -> new RuntimeException("Unable to parse given code"));
    }
}