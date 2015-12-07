package cn.creditease.test.framework.runner;

import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class CEUnitRunnerBuild extends RunnerBuilder {
	@Override
	public Runner runnerForClass(Class<?> testClass) throws Throwable {
		return new CEUnitClassRunner(testClass);
	}
}
