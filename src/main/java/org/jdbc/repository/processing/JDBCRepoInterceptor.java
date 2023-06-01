package org.jdbc.repository.processing;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jdbc.repository.exception.JDBCRepositoryException;
import org.jdbc.repository.Query;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JDBCRepoInterceptor implements MethodInterceptor {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public JDBCRepoInterceptor(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    @Override
    public Object invoke(MethodInvocation invocation) {
        Method method = invocation.getMethod();

        if (!method.isAnnotationPresent(Query.class)) {
            throw new JDBCRepositoryException("Methods in JDBCRepositories must be annotated with the" +
                    " org.jdbc.repository.Query annotation to specify the sql to be executed.");
        }

        Query query = method.getAnnotation(Query.class);
        String sqlQuery = query.value();

        Map<String, Object> parameters = new HashMap<>();

        Object[] args = invocation.getArguments();
        List<String> names = Arrays.stream(method.getParameters()).sequential().map(Parameter::getName).toList();
        for (int i = 0; i < names.size(); i++) {
            parameters.put(names.get(i), args[i]);
        }

        Class<?> userRepoType = method.getDeclaringClass();
        Class<?> resultType = (Class<?>) ((ParameterizedType) userRepoType.getGenericInterfaces()[0]).getActualTypeArguments()[0];

        return namedParameterJdbcTemplate.query(sqlQuery, parameters, new Mapper<>(resultType));
    }
}
