package com.upsxace.tv_show_tracker.common.exceptions;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import org.springframework.graphql.execution.ErrorType;

import java.util.List;

public class BadRequestException extends RuntimeException implements GraphQLError {
    private String message = "Bad request.";

    public BadRequestException(){}

    public BadRequestException(String message){
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
        return ErrorType.BAD_REQUEST;
    }
}
