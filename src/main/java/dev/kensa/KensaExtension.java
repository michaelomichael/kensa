package dev.kensa;

import dev.kensa.context.TestContainer;
import dev.kensa.context.TestContainerFactory;
import dev.kensa.context.TestContext;
import dev.kensa.output.ResultWriter;
import dev.kensa.parse.MethodParserFactory;
import dev.kensa.render.diagram.SequenceDiagramFactory;
import dev.kensa.sentence.Dictionary;
import dev.kensa.state.CapturedInteractions;
import dev.kensa.state.Givens;
import dev.kensa.state.TestInvocation;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Function;

import static dev.kensa.context.TestContextHolder.bindTestContextToThread;
import static dev.kensa.context.TestContextHolder.clearTestContextFromThread;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toList;

public class KensaExtension implements Extension, BeforeAllCallback, AfterTestExecutionCallback, BeforeTestExecutionCallback {

    private static final ExtensionContext.Namespace KENSA = ExtensionContext.Namespace.create(new Object());
    private static final String TEST_START_TIME_KEY = "StartTime";
    private static final String TEST_CONTAINER_KEY = "TestContainer";
    private static final String TEST_ARGUMENTS_KEY = "TestArguments";
    private static final String TEST_CONTEXT_KEY = "TestContext";
    private static final String TEST_PARSER_FACTORY_KEY = "TestParserFactory";
    private static final String TEST_GIVENS_KEY = "TestGivens";
    private static final String TEST_INTERACTIONS_KEY = "TestInteractions";
    private static final String KENSA_EXECUTION_CONTEXT_KEY = "KensaExecutionContext";

    private static final Function<String, KensaExecutionContext> EXECUTION_CONTEXT_FACTORY =
            key -> new KensaExecutionContext(new ResultWriter(Kensa.configuration()));

    private final TestContainerFactory testContainerFactory = new TestContainerFactory();
    private final SequenceDiagramFactory sequenceDiagramFactory = new SequenceDiagramFactory(Kensa.configuration().umlDirectives());

    @Override
    public void beforeAll(ExtensionContext context) {
        var executionContext = bindToRootContextOf(context);

        var store = context.getStore(KENSA);
        var container = testContainerFactory.createFor(context);

        store.put(TEST_CONTAINER_KEY, container);
        executionContext.register(container);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        var store = context.getStore(KENSA);
        store.put(TEST_START_TIME_KEY, System.currentTimeMillis());
        var givens = new Givens();
        var interactions = new CapturedInteractions();
        var testContext = new TestContext(givens, interactions);
        store.put(TEST_GIVENS_KEY, givens);
        store.put(TEST_INTERACTIONS_KEY, interactions);
        store.put(TEST_CONTEXT_KEY, testContext);
        store.put(TEST_PARSER_FACTORY_KEY, new MethodParserFactory(context.getRequiredTestMethod()));
        bindTestContextToThread(testContext);

        // Workaround for JUnit5 argument access
        var testDescriptor = getTestDescriptor(context);
        var arguments = getArguments(testDescriptor);
        processTestMethodArguments(context, arguments);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        try {
            var endTime = System.currentTimeMillis();

            var store = context.getStore(KENSA);
            var startTime = store.get(TEST_START_TIME_KEY, Long.class);

            var testContainer = store.get(TEST_CONTAINER_KEY, TestContainer.class);
            var methodParserFactory = store.get(TEST_PARSER_FACTORY_KEY, MethodParserFactory.class);
            var arguments = store.get(TEST_ARGUMENTS_KEY, Object[].class);
            var invocationData = testContainer.invocationDataFor(context.getRequiredTestMethod());
            var parser = methodParserFactory.createFor(arguments);
            var interactions = store.get(TEST_INTERACTIONS_KEY, CapturedInteractions.class);

            invocationData.add(
                    new TestInvocation(
                            Duration.of(endTime - startTime, MILLIS),
                            parser.sentences(),
                            parser.parameters(),
                            store.get(TEST_GIVENS_KEY, Givens.class),
                            interactions,
                            new ArrayList<>(),
                            Dictionary.acronyms().collect(toList()),
                            context.getExecutionException().orElse(null),
                            sequenceDiagramFactory.create(interactions)
                    )
            );
        } finally {
            clearTestContextFromThread();
        }
    }

    // TODO:: Need to watch this issue and remove reflection once extension point added in JUnit5
    // TODO:: https://github.com/junit-team/junit5/issues/1139
    @SuppressWarnings("WeakerAccess")
    public void processTestMethodArguments(ExtensionContext context, Object[] arguments) {
        var store = context.getStore(KENSA);
        store.put(TEST_ARGUMENTS_KEY, arguments);
    }

    private Object[] getArguments(TestMethodTestDescriptor testDescriptor) {
        try {
            TestTemplateInvocationContext invocationContext = fieldValue(testDescriptor, "invocationContext");
            return fieldValue(invocationContext, "arguments");
        } catch (Exception e) {
            return new Object[0];
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T fieldValue(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
        Field invocationContextField = target.getClass().getDeclaredField(name);
        invocationContextField.setAccessible(true);
        return (T) invocationContextField.get(target);
    }

    private TestMethodTestDescriptor getTestDescriptor(ExtensionContext context) {
        try {
            Method m = findMethod(context.getClass(), "getTestDescriptor");
            return (TestMethodTestDescriptor) m.invoke(context);
        } catch (Exception e) {
            throw new KensaException("Unable to find/call method [getTestDescriptor]", e);
        }
    }

    private Method findMethod(Class<?> target, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            Method method = target.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Class<?> superclass = target.getSuperclass();
            if (superclass != Object.class) {
                return findMethod(superclass, name, parameterTypes);
            }
            throw e;
        }
    }

    // Add the KensaExecutionContext to the store so we can hook up the close method to be executed when the
    // whole test run is complete
    private synchronized KensaExecutionContext bindToRootContextOf(ExtensionContext context) {
        var store = context.getRoot().getStore(KENSA);

        return store.getOrComputeIfAbsent(KENSA_EXECUTION_CONTEXT_KEY, EXECUTION_CONTEXT_FACTORY, KensaExecutionContext.class);
    }
}
