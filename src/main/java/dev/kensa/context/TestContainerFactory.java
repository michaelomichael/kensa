package dev.kensa.context;

import dev.kensa.Issue;
import dev.kensa.Notes;
import dev.kensa.state.TestInvocationData;
import dev.kensa.state.TestState;
import dev.kensa.util.Reflect;
import dev.kensa.util.Strings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.kensa.util.Reflect.getAnnotation;
import static java.util.Collections.emptyList;

public class TestContainerFactory {

    public TestContainer createFor(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();

        return new TestContainer(
                testClass,
                deriveDisplayNameFor(testClass, Strings.unCamel(testClass.getSimpleName())),
                invocationDataFor(testClass),
                notesFor(testClass),
                issuesFor(testClass)
        );
    }

    private Map<Method, TestInvocationData> invocationDataFor(Class<?> testClass) {
        return Reflect.testMethodsOf(testClass)
                      .map(this::createInvocationData)
                      .collect(
                              LinkedHashMap::new,
                              (m, i) -> m.put(i.testMethod(), i),
                              Map::putAll
                      );
    }

    private TestInvocationData createInvocationData(Method method) {
        return new TestInvocationData(
                method,
                deriveDisplayNameFor(method, Strings.unCamel(method.getName())),
                notesFor(method),
                issuesFor(method),
                initialStateFor(method)
        );
    }

    private TestState initialStateFor(Method element) {
        return getAnnotation(element, Disabled.class)
                .map(a -> TestState.Disabled)
                .orElse(TestState.NotExecuted);
    }

    private String deriveDisplayNameFor(AnnotatedElement element, String defaultName) {
        return getAnnotation(element, DisplayName.class)
                .map(DisplayName::value)
                .orElse(defaultName);
    }

    private String notesFor(AnnotatedElement element) {
        return getAnnotation(element, Notes.class)
                .map(Notes::value)
                .orElse(null);
    }

    private List<String> issuesFor(AnnotatedElement element) {
        return getAnnotation(element, Issue.class)
                .map(Issue::value)
                .map(Arrays::asList)
                .orElse(emptyList());
    }
}
