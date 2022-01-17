package com.cym.sqlhelper.reflection;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtil {

	private static Map<SerializableFunction<?, ?>, Field> cache = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<>(256);
	private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

	public static <T, R> String getFieldName(SerializableFunction<T, R> function) {
		Field field = ReflectionUtil.getField(function);
		return field.getName();
	}

	public static Field getField(SerializableFunction<?, ?> function) {
		return cache.computeIfAbsent(function, ReflectionUtil::findField);
	}

	public static Field findField(SerializableFunction<?, ?> function) {
		Field field = null;
		String fieldName = null;
		try {
			// 第1步 获取SerializedLambda
			Method method = function.getClass().getDeclaredMethod("writeReplace");
			method.setAccessible(Boolean.TRUE);
			SerializedLambda serializedLambda = (SerializedLambda) method.invoke(function);
			// 第2步 implMethodName 即为Field对应的Getter方法名
			String implMethodName = serializedLambda.getImplMethodName();
			if (implMethodName.startsWith("get") && implMethodName.length() > 3) {
				fieldName = Introspector.decapitalize(implMethodName.substring(3));

			} else if (implMethodName.startsWith("is") && implMethodName.length() > 2) {
				fieldName = Introspector.decapitalize(implMethodName.substring(2));
			} else if (implMethodName.startsWith("lambda$")) {
				throw new IllegalArgumentException("SerializableFunction不能传递lambda表达式,只能使用方法引用");

			} else {
				throw new IllegalArgumentException(implMethodName + "不是Getter方法引用");
			}
			// 第3步 获取的Class是字符串，并且包名是“/”分割，需要替换成“.”，才能获取到对应的Class对象
			String declaredClass = serializedLambda.getImplClass().replace("/", ".");
			Class<?> aClass = Class.forName(declaredClass, false, getDefaultClassLoader());

			// 第4步 Spring 中的反射工具类获取Class中定义的Field
			field = findField(aClass, fieldName, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 第5步 如果没有找到对应的字段应该抛出异常
		if (field != null) {
			return field;
		}
		throw new NoSuchFieldError(fieldName);
	}

	public static Field findField(Class<?> clazz, String name, Class<?> type) {
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			Field[] fields = getDeclaredFields(searchType);
			for (Field field : fields) {
				if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	private static Field[] getDeclaredFields(Class<?> clazz) {
		Field[] result = declaredFieldsCache.get(clazz);
		if (result == null) {
			try {
				result = clazz.getDeclaredFields();
				declaredFieldsCache.put(clazz, (result.length == 0 ? EMPTY_FIELD_ARRAY : result));
			} catch (Throwable ex) {
				throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() + "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
			}
		}
		return result;
	}

	
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ReflectionUtil.class.getClassLoader();
			if (cl == null) {
				// getClassLoader() returning null indicates the bootstrap ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				}
				catch (Throwable ex) {
					// Cannot access system ClassLoader - oh well, maybe the caller can live with null...
				}
			}
		}
		return cl;
	}
}