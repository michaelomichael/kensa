package dev.kensa.state;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestInvocationData {

    private final Method testMethod;
    private final String displayName;
    private final TestState initialState;
    private final List<TestInvocation> invocations = new ArrayList<>();

    public TestInvocationData(Method testMethod, String displayName, TestState initialState) {
        this.testMethod = testMethod;
        this.displayName = displayName;
        this.initialState = initialState;
    }

    public Method testMethod() {
        return testMethod;
    }

    public String displayName() {
        return displayName;
    }

    public void add(TestInvocation invocation) {
        invocations.add(invocation);
    }

    public TestState state() {
        TestState state = initialState;

        for (TestInvocation invocation : invocations) {
            state = state.overallStateFrom(invocation.state());
        }

        return state;
    }

    public Stream<TestInvocation> invocations() {
        return invocations.stream();
    }
}
