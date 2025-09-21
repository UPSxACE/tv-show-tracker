package com.upsxace.tv_show_tracker.common.exceptions;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import org.springframework.graphql.execution.ErrorType;

import java.util.List;

public class NotFoundException extends RuntimeException implements GraphQLError {
    private String message = "Not found.";

    public NotFoundException(){}

    public NotFoundException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return List.of();
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.NOT_FOUND;
    }
}
