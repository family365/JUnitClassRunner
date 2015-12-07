package cn.creditease.test.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RunOption {
	public Class<?>[] testSuite();
	public String[] testFiles() default {};
	public String[] tags() default {};
	public String[] service() default {};
	public String propertyFile() default "";
}
