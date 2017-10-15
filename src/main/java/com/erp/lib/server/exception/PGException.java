package com.erp.lib.server.exception;

import java.sql.BatchUpdateException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PGException {

    private final Map<String, String> errors = new HashMap();
    private final Set<ConstraintError> constraintErrors = new HashSet();
    private final Set<FieldError> fieldErrors = new HashSet();
    private final Logger logger = LoggerFactory.getLogger(PGException.class);

    public PGException() {
        initialize();
    }

    protected void initialize() {
    }

    protected void addError(String context, String message) {
        errors.put(context, message);
    }

    protected void addConstraintError(String constraint, String field, String message) {
        constraintErrors.add(new ConstraintError(constraint, field, message));
    }

    protected void addFieldError(String error, String field, String message) {
        fieldErrors.add(new FieldError(error, field, message));
    }

    public void throwJsonException(Exception exception, String context) {
        JsonException jsonException = new JsonException();

        if (exception instanceof BatchUpdateException) {
            exception = ((BatchUpdateException) exception).getNextException();
        }

        if (exception instanceof PSQLException) {
            PSQLException psqlException = (PSQLException) exception;

            if (psqlException.getSQLState().startsWith("23") || psqlException.getSQLState().startsWith("P0")) {
                constraintErrors.forEach((constraintError) -> {
                    if (psqlException.getMessage().contains(constraintError.constraint)) {
                        jsonException.addField(constraintError.field, constraintError.message);
                    }
                });

            }

            fieldErrors.forEach((fieldError) -> {
                if (psqlException.getMessage().contains(fieldError.error)) {
                    jsonException.addField(fieldError.field, fieldError.message);
                }
            });
        }

        if (errors.containsKey(context)) {
            jsonException.setMessage(errors.get(context));
        }

        logger.error(exception.getMessage());
        throw jsonException;

    }

    private class ConstraintError {

        private String constraint;
        private String field;
        private String message;

        ConstraintError(String constraint, String field, String message) {
            this.constraint = constraint;
            this.field = field;
            this.message = message;
        }
    }

    private class FieldError {

        private String error;
        private String field;
        private String message;

        FieldError(String error, String field, String message) {
            this.error = error;
            this.field = field;
            this.message = message;
        }
    }
}
