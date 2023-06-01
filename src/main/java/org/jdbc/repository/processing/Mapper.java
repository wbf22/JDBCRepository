package org.jdbc.repository.processing;

import org.jdbc.repository.exception.JDBCRepositoryException;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Mapper <T> implements RowMapper<T> {

    private Class<?> type;

    public Mapper(Class<?> type) {
        this.type = type;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) {
        try {
            return (T) getValue(rs, type, null);
        } catch (ReflectiveOperationException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getValue(ResultSet rs, Class<?> type, String name) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (type == String.class) {
            return rs.getString(name);
        }
        else if (type == Long.class || type == long.class) {
            return rs.getLong(name);
        }
        else if (type == Boolean.class || type == boolean.class) {
            return rs.getBoolean(name);
        }
        else if (type == Float.class || type == float.class) {
            return rs.getFloat(name);
        }
        else if (type == BigDecimal.class) {
            return rs.getBigDecimal(name);
        }
        else if (type == Integer.class || type == int.class) {
            return rs.getInt(name);
        }
        else if (type == Double.class || type == double.class) {
            return rs.getDouble(name);
        }
        else if (type.isArray()) {
            return rs.getArray(name);
        }
        else if (Date.class.isAssignableFrom(type)) {
            return rs.getDate(name);
        }
        else if (isJavaType(type)) {
            throw new JDBCRepositoryException("Unsupported Java type for JDBCRepository: " + type.getName());
        }
        else if (type.isEnum()) {
            int enumValue = rs.getInt(name);
            return type.getEnumConstants()[enumValue];
        }
        else {
            // some user object
            Field[] fields = type.getDeclaredFields();
            List<Object> values = new ArrayList<>();
            for (Field field : fields) {
                values.add(
                        getValue(rs, field.getType(), field.getName())
                );
            }
            return type.getDeclaredConstructors()[0].newInstance(values.toArray());
        }
    }

    private static final String JAVA = "java.";
    public static boolean isJavaType(Class<?> type) {
        return type.getName().startsWith(JAVA) || isNumericClass(type);
    }

    public static boolean isNumericClass(Class<?> type) {
        return Number.class.isAssignableFrom(type) || isPrimitiveNumericClass(type);
    }

    public static boolean isPrimitiveNumericClass(Class<?> type) {
        return type == int.class || type == long.class || type == double.class
                || type == float.class || type == short.class || type == byte.class;
    }
}