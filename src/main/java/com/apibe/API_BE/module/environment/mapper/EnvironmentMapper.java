package com.apibe.API_BE.module.environment.mapper;

import com.apibe.API_BE.module.environment.dto.response.*;
import com.apibe.API_BE.module.environment.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EnvironmentMapper {

    public EnvironmentVariableResponse toVariableResponse(EnvironmentVariable variable) {
        if (variable == null) {
            return null;
        }
        return EnvironmentVariableResponse.builder()
                .id(variable.getId())
                .environmentId(variable.getEnvironmentId())
                .key(variable.getVariableKey())
                .initialValue(variable.getInitialValue())
                .currentValue(variable.getCurrentValue())
                .type(variable.getType())
                .isEnabled(variable.isEnabled())
                .isSecret(variable.isSecret())
                .createdAt(variable.getCreatedAt())
                .updatedAt(variable.getUpdatedAt())
                .build();
    }

    public List<EnvironmentVariableResponse> toVariableResponseList(List<EnvironmentVariable> variables) {
        if (variables == null) {
            return Collections.emptyList();
        }
        return variables.stream()
                .map(this::toVariableResponse)
                .collect(Collectors.toList());
    }

    public EnvironmentResponse toEnvironmentResponse(Environment environment, List<EnvironmentVariable> variables) {
        if (environment == null) {
            return null;
        }
        return EnvironmentResponse.builder()
                .id(environment.getId())
                .projectId(environment.getProjectId())
                .name(environment.getName())
                .description(environment.getDescription())
                .variables(toVariableResponseList(variables))
                .createdAt(environment.getCreatedAt())
                .updatedAt(environment.getUpdatedAt())
                .build();
    }

    public ActiveEnvironmentResponse toActiveEnvironmentResponse(ActiveEnvironment activeEnvironment) {
        if (activeEnvironment == null) {
            return null;
        }
        return ActiveEnvironmentResponse.builder()
                .id(activeEnvironment.getId())
                .projectId(activeEnvironment.getProjectId())
                .environmentId(activeEnvironment.getEnvironmentId())
                .build();
    }
}
