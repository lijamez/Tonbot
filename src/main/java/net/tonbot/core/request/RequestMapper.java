package net.tonbot.core.request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import lombok.Data;
import net.tonbot.core.request.parsing.LineParser;
import net.tonbot.core.request.parsing.ParseException;

@SuppressWarnings("rawtypes")
public class RequestMapper {

	private static final Logger LOG = LoggerFactory.getLogger(RequestMapper.class);

	private final Map<Class<?>, List<ParamInfo>> cachedParamInfosMap = new ConcurrentHashMap<>();
	private final LineParser lineParser;

	@Inject
	public RequestMapper(LineParser lineParser) {
		this.lineParser = Preconditions.checkNotNull(lineParser, "lineParser must be non-null.");
	}

	/**
	 * Maps a line of arguments to a Java object.
	 * 
	 * @param args
	 *            The line of arguments. Non-null.
	 * @param target
	 *            The target Java object class. Must have a zero-argument
	 *            constructor. Non-null.
	 * @param context
	 *            {@link Context}. Non-null.
	 * @return An instance of <T> with all fields mapped.
	 * @throws RequestMappingException
	 *             if there was an mapping error caused by the input.
	 * @throws ParseException
	 *             if the arguments couldn't be parsed according to the target's
	 *             params.
	 */
	@SuppressWarnings("unchecked")
	public <T> T map(String args, Class<T> target, Context context) {
		Preconditions.checkNotNull(args, "args must be non-null.");
		Preconditions.checkNotNull(target, "target must be non-null.");
		Preconditions.checkNotNull(context, "context must be non-null.");

		List<ParamInfo> paramInfos = getAndCacheParamInfos(target);

		try {
			T targetObj = target.newInstance();

			if (paramInfos.isEmpty()) {
				return targetObj;
			}

			ParamInfo lastParamInfo = paramInfos.get(paramInfos.size() - 1);

			List<Class<?>> argTypes = paramInfos.stream()
					.map(pi -> pi.getType())
					.collect(Collectors.toList());

			List<Object> parsedValues = lineParser.parse(
					args,
					argTypes,
					lastParamInfo.getParam().captureRemaining(),
					context);

			for (int i = 0; i < parsedValues.size(); i++) {
				ParamInfo pi = paramInfos.get(i);
				Object parsedValue = parsedValues.get(i);

				if (parsedValue == null && !pi.isNullable()) {
					throw new RequestMappingException("Parameter '" + pi.getParam().name() + "' is required.");
				}

				try {
					pi.getSetter().apply(targetObj, parsedValue);
				} catch (IllegalArgumentException e) {
					throw new RequestMappingException(e.getMessage(), e);
				}
			}

			return targetObj;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not instantiate target class " + target
					+ ". Make sure it is concrete and has a public zero-argument constructor.", e);
		} catch (ParseException e) {
			throw new RequestMappingException("Incorrect usage.", e);
		}
	}

	private List<ParamInfo> getAndCacheParamInfos(Class<?> target) {
		return cachedParamInfosMap.computeIfAbsent(target, t -> {
			List<ParamInfo> paramInfos = extractParamInfos(t);
			paramInfos = validate(paramInfos);

			return paramInfos;
		});
	}

	/**
	 * Checks that ParamInfos go contiguously from 0 to paramInfos.size() - 1
	 * 
	 * Param where captureRemaining == true is only on the last param and that Param
	 * must be on a {@link CharSequence}.
	 * 
	 * @param paramInfos
	 *            The list of {@link ParamInfos} to validate.
	 * @return A sorted list of ParamInfos with their ordinal matching the array
	 *         index.
	 */
	private List<ParamInfo> validate(List<ParamInfo> paramInfos) {
		List<ParamInfo> sortedParamInfos = new ArrayList<>(Collections.nCopies(paramInfos.size(), null));

		for (ParamInfo paramInfo : paramInfos) {
			Param param = paramInfo.getParam();

			if (param.ordinal() < 0) {
				throw new IllegalArgumentException("Param ordinal must be non-negative.");
			} else if (param.ordinal() >= sortedParamInfos.size()) {
				throw new IllegalArgumentException("Param ordinals are non-contiguous.");
			}

			if (param.ordinal() != sortedParamInfos.size() - 1 && param.captureRemaining()
					&& !CharSequence.class.isAssignableFrom(paramInfo.getType())) {
				throw new IllegalArgumentException(
						"captureRemaining must only be true on the @Param with the highest ordinal and must be on a String type");
			}

			ParamInfo prevParamInfo = sortedParamInfos.set(param.ordinal(), paramInfo);
			if (prevParamInfo != null) {
				throw new IllegalArgumentException("Param indices duplication detected.");
			}
		}

		return sortedParamInfos;
	}

	private List<ParamInfo> extractParamInfos(Class<?> clazz) {
		List<ParamInfo> paramInfos = new ArrayList<>();

		paramInfos.addAll(extractParamInfosFromFields(clazz));
		paramInfos.addAll(extractParamInfosFromMethods(clazz));

		return paramInfos;
	}

	private List<ParamInfo> extractParamInfosFromFields(Class<?> clazz) {

		List<ParamInfo> paramInfos = FieldUtils.getFieldsListWithAnnotation(clazz, Param.class)
				.stream()
				.map(field -> {
					Param paramAnnotation = field.getAnnotation(Param.class);
					boolean nullable = field.getAnnotation(Nonnull.class) == null;

					field.setAccessible(true);

					return new ParamInfo<>(paramAnnotation, field.getType(), (obj, val) -> {
						try {
							field.set(obj, val);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new IllegalStateException("Failed to set field " + field + " with data.", e);
						}
						return null;
					}, nullable);
				})
				.collect(Collectors.toList());

		return paramInfos;
	}

	private List<ParamInfo> extractParamInfosFromMethods(Class<?> clazz) {

		List<ParamInfo> paramInfos = Arrays.asList(MethodUtils.getMethodsWithAnnotation(clazz, Param.class))
				.stream()
				.filter(method -> {
					if (method.getParameterTypes().length != 1) {
						LOG.warn(
								"Incorrect @Param annotation placement on method {}. The method must have exactly one parameter.",
								method);
						return false;
					} else if (!method.getReturnType().equals(Void.TYPE)) {
						LOG.warn(
								"Incorrect @Param annotation placement on method {}. The method must return void.",
								method);
						return false;
					}

					return true;
				})
				.map(method -> {
					Param paramAnnotation = method.getAnnotation(Param.class);

					Parameter methodParameter = method.getParameters()[0];
					boolean nullable = methodParameter.getAnnotation(Nonnull.class) == null;

					return new ParamInfo<>(paramAnnotation, methodParameter.getType(), (obj, val) -> {
						try {
							method.invoke(obj, val);
						} catch (IllegalAccessException | IllegalArgumentException e) {
							throw new IllegalStateException("Failed to call method " + method + " to set data.", e);
						} catch (InvocationTargetException e) {
							// Setter threw an exception.
							// The messages in any IllegalArgumentException and NullPointerExceptions thrown
							// by these setter methods should be sent back to the user.
							if (e.getTargetException() instanceof IllegalArgumentException) {
								throw (IllegalArgumentException) e.getTargetException();
							} else {
								throw new IllegalStateException("Failed to call method " + method + " to set data.", e);
							}
						}
						return null;
					}, nullable);

				})
				.collect(Collectors.toList());

		return paramInfos;
	}

	@Data
	private static class ParamInfo<OT, FT> {
		private final Param param;
		private final Class<FT> type;
		private final BiFunction<OT, FT, Void> setter;
		private final boolean isNullable;
	}
}
