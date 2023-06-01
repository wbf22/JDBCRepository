package org.jdbc.repository.classscan;

public interface Filter {

    boolean isWanted(Class<?> type);

}
