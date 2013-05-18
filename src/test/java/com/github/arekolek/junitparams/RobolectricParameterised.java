package com.github.arekolek.junitparams;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import junitparams.internal.ParameterisedTestClassRunner;
import junitparams.internal.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.List;


public class RobolectricParameterised extends BlockJUnit4ClassRunner {

    private ParameterisedTestClassRunner parameterisedRunner;
    private Description description;

    public RobolectricParameterised(Class<?> type) throws Exception {
        // TODO Constructor is called twice when extending RobolectricTestRunner
        // TODO super(robolectricClass(type)); almost works except annotations don't work because of different class loaders
        super(robolectricClass(type));
        //super(type);
        parameterisedRunner = new ParameterisedTestClassRunner(getTestClass());
    }

    private static Class<?> robolectricClass(Class<?> originalClass) throws Exception {
        return getRobolectricLoader().loadClass(originalClass.getName());
    }

    private static RobolectricClassLoader getRobolectricLoader() throws Exception {
        Method getDefaultLoader = RobolectricTestRunner.class.getDeclaredMethod("getDefaultLoader");
        getDefaultLoader.setAccessible(true);
        return (RobolectricClassLoader) getDefaultLoader.invoke(null);
    }

    protected void collectInitializationErrors(List<Throwable> errors) {
        for (Throwable throwable : errors)
            throwable.printStackTrace();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (handleIgnored(method, notifier))
            return;

        TestMethod testMethod = parameterisedRunner.testMethodFor(method);
        if (parameterisedRunner.shouldRun(testMethod))
            parameterisedRunner.runParameterisedTest(testMethod, methodBlock(method), notifier);
        else
            super.runChild(method, notifier);
    }

    private boolean handleIgnored(FrameworkMethod method, RunNotifier notifier) {
        TestMethod testMethod = parameterisedRunner.testMethodFor(method);
        if (testMethod.isIgnored())
            notifier.fireTestIgnored(describeMethod(method));

        return testMethod.isIgnored();
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return parameterisedRunner.computeFrameworkMethods();
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement methodInvoker = parameterisedRunner.parameterisedMethodInvoker(method, test);
        if (methodInvoker == null)
            methodInvoker = super.methodInvoker(method, test);

        return methodInvoker;
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), getTestClass().getAnnotations());
            List<FrameworkMethod> resultMethods = parameterisedRunner.returnListOfMethods();

            for (FrameworkMethod method : resultMethods)
                description.addChild(describeMethod(method));
        }

        return description;
    }

    protected Description describeMethod(FrameworkMethod method) {
        Description child = parameterisedRunner.describeParameterisedMethod(method);

        if (child == null)
            child = describeChild(method);

        return child;
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                statement.evaluate();
            }
        };
    }
}

