package cn.creditease.test.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test.None;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TestCase {
	public long timeout() default 0L;
	public Class<? extends Throwable> expected() default None.class;
	public String tag() default "";
	public String desc() default "";
}
