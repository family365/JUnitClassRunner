package cn.creditease.test.framework.runner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import cn.creditease.pay.common.log.Logger;
import cn.creditease.pay.common.log.LoggerFactory;
import cn.creditease.test.framework.GlobalProperty;
import cn.creditease.test.framework.annotation.RunOption;
import cn.creditease.test.framework.annotation.TestCase;
import cn.creditease.test.framework.db.DatabaseFactory;
import cn.creditease.test.framework.serviceClient.ClientEngineFactory;

public class CEUnit extends Suite  {
	private static final Logger logger = LoggerFactory.getLogger(CEUnit.class);
	
	public static class TagFilter extends Filter {
		private List<String> targetTags;
		public TagFilter(List<String> tags) {
			this.targetTags = tags;
		}
		
		@Override
		public boolean shouldRun(Description description) {
			boolean matched = haveMatchedTag(description);
			if (matched) {
				return true;
			}
			
			for (Description each : description.getChildren()) {
				matched = shouldRun(each);
				if (matched) {
					return true;
				}
			}
			return false;
		}

		private boolean haveMatchedTag(Description description) {
			List<String> givenTags = getTag(description);
			if (targetTags == null || targetTags.size() == 0) {
				return true;
			}
			
			for (String tag : targetTags) {
				if (givenTags.contains(tag)) {
					return true;
				}
			}

			return false;
		}

		private List<String> getTag(Description description) {
			List<String> givenTags = new ArrayList<String>();
			String directTag = getDirectTags(description);
			if (directTag != null) {
				givenTags.add(directTag);
			}
			
			Description parentDescription = getParentDescription(description);
			String parentTag = getDirectTags(parentDescription);
			if (parentTag != null) {
				givenTags.add(parentTag);
			}
			
			return givenTags;
		}

		private String getDirectTags(Description desc) {
			TestCase testCase = desc.getAnnotation(TestCase.class);
			if (testCase == null) {
				return null;
			}
			
			String tag = testCase.tag();
			return tag;
		}
		
		private Description getParentDescription(Description description) {
			Class<?> testClass = description.getTestClass();
			Description parentDesc = Description.createSuiteDescription(testClass);
			return parentDesc;
		}

		@Override
		public String describe() {
			return "tag " + targetTags.toString();
		}
	}
	
	private List<Runner> children = null;
	public CEUnit(Class<?> kickoffClass, RunnerBuilder builder) throws InitializationError {
		super(kickoffClass, Collections.<Runner>emptyList());
		buildChildRunner(kickoffClass);
		logger.info("Complete to init runner");
		
		try {
			List<String> includeTags = getIncludedTags(kickoffClass);
			TagFilter tagFilter = new TagFilter(includeTags);
			filter(tagFilter);
			logger.info("Complete to filte test case");
		} catch (NoTestsRemainException e) {
			logger.info(e.getMessage());
			throw new InitializationError(e);
		}

		initGlobalProperty(kickoffClass);
		initService(kickoffClass);
		initDatabase();		
	}

	private void buildChildRunner(Class<?> kickoffClass) throws InitializationError {
		Set<Class<?>> testSuite = new HashSet<Class<?>>();
		
		RunOption runOption = kickoffClass.getAnnotation(RunOption.class);
		String[] testFiles = runOption.testFiles();
		if (testFiles != null && testFiles.length > 0) {
			Set<Class<?>> testListFromFile = loadTestClasses(testFiles);
			testSuite.addAll(testListFromFile);
		}
		
		Class<?>[] testList = runOption.testSuite();
		if (testList.length > 0) {
			testSuite.addAll(Arrays.asList(testList));
		}
		
		CustomizedRunnerBuilder selfRunnerBuild = new CustomizedRunnerBuilder(true);
		children = selfRunnerBuild.runners(kickoffClass, testSuite.toArray(new Class<?>[0]));
	}

	private Set<Class<?>> loadTestClasses(String[] fileList) {
		Set<Class<?>> testClassSet = new HashSet<Class<?>>();
		
		if (fileList == null || fileList.length < 1) {
			return testClassSet;
		}

		Set<String> searchedFiles = new HashSet<String>();
		for(String file : fileList) {
			if (searchedFiles.contains(file)) {
				continue;
			}
			
			searchedFiles.add(file);
			if (file.contains("*")) {
				List<Class<?>> testClasses = loadClassFromDir(file);
				testClassSet.addAll(testClasses);
			} else {
				Class<?> testClass = loadClass(file);
				testClassSet.add(testClass);
			}
		}
		
		return testClassSet;
	}

	private List<Class<?>> loadClassFromDir(String testFile) {
		List<Class<?>> testClassList = new ArrayList<Class<?>>();
		String packageName = testFile.substring(0, testFile.lastIndexOf('.'));
		
		String testFilePath = testFile.replace('.', File.separatorChar);
		String dirPackagePath = testFilePath.substring(0, testFilePath.lastIndexOf(File.separatorChar));
		String currentPath = getClass().getClassLoader().getResource("").getFile();
		String fullDirectoryPath = currentPath + File.separatorChar + dirPackagePath;
		File dir = new File(fullDirectoryPath);
		if (!dir.exists()) {
			return testClassList; 
		}
		
		String filePattern = testFilePath.substring(testFilePath.lastIndexOf(File.separatorChar) + 1);
		filePattern = filePattern.replace("*", ".*");
		final Pattern pattern = Pattern.compile(filePattern, Pattern.CASE_INSENSITIVE);
		String[] matchedFiles = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				Matcher matcher = pattern.matcher(name);
				if (matcher.find()) {
					return true;
				}
				
				return false;
			}
		});
		
		if (matchedFiles == null || matchedFiles.length < 1) {
			logger.warn(String.format("NOT found any test class for [%s]", testFile));
			return testClassList;
		}
		
		for(String file : matchedFiles) {
			String className = file.substring(0, file.length() -6);
			className = packageName + "." + className;
			Class<?> testClass = loadClass(className);
			testClassList.add(testClass);
			logger.info(String.format("Test class [%S] was add into suite", className));
		}

		return testClassList;
	}

	private Class<?> loadClass(String className) {
		try {
			return Class.forName(className, true, getClass().getClassLoader());
		} catch(ClassNotFoundException ex) {
			String errMsg = String.format("Class [%s] not found, initialize failed", className);
			logger.error(errMsg);
			throw new RuntimeException(errMsg);
		}
	}

	private List<String> getIncludedTags(Class<?> kickoffClass) {
		RunOption option = kickoffClass.getAnnotation(RunOption.class);
		if (option == null) {
			return null;
		}
		
		if (option.tags() == null || option.tags().length < 1) {
			return null;
		}
		
		return Arrays.asList(option.tags());
	}


	@Override
	protected List<Runner> getChildren() {
		return children;
	}
	
	@Override
	protected Description describeChild(Runner child) {
		return child.getDescription();
	}

	@Override
	protected void runChild(Runner runner, final RunNotifier notifier) {
		runner.run(notifier);
	}
	
	private void initService(Class<?> kickoffClass) {
		RunOption runOption = kickoffClass.getAnnotation(RunOption.class);
		String[] services = runOption.service();
		if (services == null || services.length < 1) {
			return;
		}
		
		ClientEngineFactory clientEngineFactory = ClientEngineFactory.getInstance();
		clientEngineFactory.init(Arrays.asList(services));
	}
	
	private void initGlobalProperty(Class<?> kickoffClass) {
		RunOption runOption = kickoffClass.getAnnotation(RunOption.class);
		String propertyConfig = runOption.propertyFile();
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.init(propertyConfig);
	}
	
	private void initDatabase() {
		DatabaseFactory factory = DatabaseFactory.getInstance();
		factory.init();
	}
}
