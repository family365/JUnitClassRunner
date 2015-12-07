package cn.creditease.test.framework.runner;

import java.util.Arrays;
import java.util.List;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class CustomizedRunnerBuilder extends AllDefaultPossibilitiesBuilder {

	public CustomizedRunnerBuilder(boolean canUseSuiteMethod) {
		super(canUseSuiteMethod);
	}
	

	@Override
	public Runner runnerForClass(Class<?> testClass) throws Throwable {
		List<RunnerBuilder> builders= Arrays.asList(
				ignoredBuilder(),
				suiteMethodBuilder(),
				junit3Builder(),
				selfRunnerBuilder());

		for (RunnerBuilder each : builders) {
			Runner runner= each.safeRunnerForClass(testClass);
			if (runner != null)
				return runner;
		}
		return null;
	}

	protected CEUnitRunnerBuild selfRunnerBuilder() {
		return new CEUnitRunnerBuild();
	}
}
