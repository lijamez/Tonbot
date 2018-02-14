package net.tonbot.core.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the field or method as a parameter.
 * 
 * If this annotation is put on a method, then that method should throw
 * {@link IllegalArgumentException} if the param is invalid.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD })
public @interface Param {

	/**
	 * The parameter's name.
	 * 
	 * @return The parameter's name.
	 */
	String name();

	/**
	 * An ordinal. Must be non-negative.
	 * 
	 * @return The ordinal.
	 */
	int ordinal();

	/**
	 * Indicates whether if the remainder of the command should be put into this
	 * param. Should only be set to true on the last param (highest ordinal) and the
	 * parameter type is a String.
	 * 
	 * @return Indicator to capture the remaining part of the input.
	 */
	boolean captureRemaining() default false;
}
