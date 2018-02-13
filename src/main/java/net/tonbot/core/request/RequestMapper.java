package net.tonbot.core.request;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import lombok.Data;

@SuppressWarnings("rawtypes")
public class RequestMapper {

	private final Map<Class<?>, List<ParamInfo>> cachedParamInfosMap = new ConcurrentHashMap<>();
	private final LineParser lineParser = new LineParser();

	/**
	 * Maps a line of arguments to a Java object.
	 * 
	 * @param args
	 *            The line of arguments. Non-null.
	 * @param target
	 *            The target Java object class. Must have a zero-argument
	 *            constructor. Non-null.
	 * @return An instance of <T> with all fields mapped.
	 * @throws RequestMappingException if there was an internal error.
	 * @throws ParseException if the arguments couldn't be parsed according to the target's params.
	 */
	@SuppressWarnings("unchecked")
	public <T> T map(String args, Class<T> target) {
		Preconditions.checkNotNull(args, "args must be non-null.");
		Preconditions.checkNotNull(target, "target must be non-null.");

		List<ParamInfo> paramInfos = getAndCacheParamInfos(target);

		List<Class<?>> types = paramInfos.stream()
				.map(pi -> pi.getType())
				.collect(Collectors.toList());

		try {
			List<Object> parsedValues = lineParser.parse(args, types);

			T targetObj = target.newInstance();

			for (int i = 0; i < parsedValues.size(); i++) {
				ParamInfo pi = paramInfos.get(i);
				Object parsedValue = parsedValues.get(i);

				pi.getSetter().apply(targetObj, parsedValue);
			}

			return targetObj;

		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not instantiate target class " + target
					+ ". Make sure it is concrete and has a public zero-argument constructor.", e);
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
	 * @param paramInfos
	 *            The list of {@link ParamInfos} to validate.
	 * @return A sorted list of ParamInfos with their index matching the array
	 *         index.
	 */
	private List<ParamInfo> validate(List<ParamInfo> paramInfos) {
		List<ParamInfo> sortedParamInfos = new ArrayList<>(Collections.nCopies(paramInfos.size(), null));

		for (ParamInfo paramInfo : paramInfos) {
			if (paramInfo.getIndex() < 0) {
				throw new IllegalArgumentException("Param index must be non-negative.");
			} else if (paramInfo.getIndex() >= sortedParamInfos.size()) {
				throw new IllegalArgumentException("Param indices are non-contiguous.");
			}

			ParamInfo prevParamInfo = sortedParamInfos.set(paramInfo.getIndex(), paramInfo);
			if (prevParamInfo != null) {
				throw new IllegalArgumentException("Param indices duplication detected.");
			}
		}

		return sortedParamInfos;
	}

	private List<ParamInfo> extractParamInfos(Class<?> clazz) {

		List<ParamInfo> paramInfos = new ArrayList<>();

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Param paramAnnotation = field.getAnnotation(Param.class);

			if (paramAnnotation != null) {

				int index = paramAnnotation.index();

				paramInfos.add(new ParamInfo<>(index, field.getType(), (obj, val) -> {
					try {
						field.setAccessible(true);
						field.set(obj, val);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RequestMappingException("Failed to set field " + field + " with data.", e);
					}
					return null;
				}));
			}
		}

		return paramInfos;
	}

	@Data
	private static class ParamInfo<OT, FT> {
		private final int index;
		private final Class<FT> type;
		private final BiFunction<OT, FT, Void> setter;
	}
}
