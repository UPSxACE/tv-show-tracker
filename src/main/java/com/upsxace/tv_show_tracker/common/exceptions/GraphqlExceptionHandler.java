package com.upsxace.tv_show_tracker.common.exceptions;

import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphqlExceptionHandler extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, @NotNull DataFetchingEnvironment env){
        return switch (ex) {
            case ConstraintViolationException violationEx -> buildViolationError(violationEx, env);
            case GraphQLError gql -> wrapCustomGqlError(gql,env); // custom throwable graphql error
            default -> null;
        };
    }

    private GraphQLError wrapCustomGqlError(GraphQLError ex, DataFetchingEnvironment env){
        return GraphqlErrorBuilder
                .newError()
                .errorType(ex.getErrorType())
                .message(ex.getMessage())
                .extensions(ex.getExtensions())
                .path(env.getExecutionStepInfo().getPath())
                .locations(ex.getLocations())
                .build();
    }

    private GraphQLError buildViolationError(ConstraintViolationException ex, DataFetchingEnvironment env){
        return GraphqlErrorBuilder
                .newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message("Validation failed.")
                .extensions(Map.of(
                        "validationErrors",
                        ex.getConstraintViolations().stream()
                                .map(v -> Map.of(
                                        "field", v.getPropertyPath().toString(),
                                        "message", v.getMessage()
                                ))
                                .toList()
                ))
                .path(env.getExecutionStepInfo().getPath())
                .build();
    }
}
