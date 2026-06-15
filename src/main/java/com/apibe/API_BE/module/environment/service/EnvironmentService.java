package com.apibe.API_BE.module.environment.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.environment.dto.request.*;
import com.apibe.API_BE.module.environment.dto.response.*;
import com.apibe.API_BE.module.environment.entity.*;
import com.apibe.API_BE.module.environment.mapper.EnvironmentMapper;
import com.apibe.API_BE.module.environment.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentVariableRepository environmentVariableRepository;
    private final ActiveEnvironmentRepository activeEnvironmentRepository;
    private final EnvironmentMapper environmentMapper;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+)\\s*\\}\\}");

    public List<EnvironmentResponse> getEnvironments(UUID projectId) {
        List<Environment> environments = environmentRepository.findByProjectId(projectId);
        if (environments.isEmpty()) {
            // Tự động khởi tạo môi trường mặc định
            Environment defaultEnv = Environment.builder()
                    .projectId(projectId)
                    .name("Local")
                    .description("Default local environment")
                    .build();
            defaultEnv = environmentRepository.save(defaultEnv);

            EnvironmentVariable baseUrlVar = EnvironmentVariable.builder()
                    .environmentId(defaultEnv.getId())
                    .variableKey("baseUrl")
                    .initialValue("http://localhost:8080/api")
                    .currentValue("http://localhost:8080/api")
                    .type("text")
                    .isEnabled(true)
                    .isSecret(false)
                    .build();

            EnvironmentVariable tokenVar = EnvironmentVariable.builder()
                    .environmentId(defaultEnv.getId())
                    .variableKey("token")
                    .initialValue("mock-token")
                    .currentValue("mock-token")
                    .type("secret")
                    .isEnabled(true)
                    .isSecret(true)
                    .build();

            environmentVariableRepository.saveAll(Arrays.asList(baseUrlVar, tokenVar));

            ActiveEnvironment activeEnv = ActiveEnvironment.builder()
                    .projectId(projectId)
                    .environmentId(defaultEnv.getId())
                    .build();
            activeEnvironmentRepository.save(activeEnv);

            environments = Collections.singletonList(defaultEnv);
        }

        return environments.stream()
                .map(env -> {
                    List<EnvironmentVariable> vars = environmentVariableRepository.findByEnvironmentId(env.getId());
                    return environmentMapper.toEnvironmentResponse(env, vars);
                })
                .collect(Collectors.toList());
    }

    public EnvironmentResponse createEnvironment(UUID projectId, CreateEnvironmentRequest request) {
        Environment env = Environment.builder()
                .projectId(projectId)
                .name(request.getName())
                .description(request.getDescription())
                .build();
        env = environmentRepository.save(env);

        // Nếu là môi trường đầu tiên của project, tự động set làm active
        Optional<ActiveEnvironment> activeOpt = activeEnvironmentRepository.findByProjectId(projectId);
        if (activeOpt.isEmpty() || activeOpt.get().getEnvironmentId() == null) {
            ActiveEnvironment activeEnv = activeOpt.orElseGet(() -> ActiveEnvironment.builder().projectId(projectId).build());
            activeEnv.setEnvironmentId(env.getId());
            activeEnvironmentRepository.save(activeEnv);
        }

        return environmentMapper.toEnvironmentResponse(env, Collections.emptyList());
    }

    public EnvironmentResponse updateEnvironment(UUID environmentId, CreateEnvironmentRequest request) {
        Environment env = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (request.getName() != null) {
            env.setName(request.getName());
        }
        env.setDescription(request.getDescription());
        env = environmentRepository.save(env);

        List<EnvironmentVariable> vars = environmentVariableRepository.findByEnvironmentId(environmentId);
        return environmentMapper.toEnvironmentResponse(env, vars);
    }

    public void deleteEnvironment(UUID environmentId) {
        Environment env = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        environmentVariableRepository.deleteByEnvironmentId(environmentId);
        environmentRepository.delete(env);

        // Xóa hoặc cập nhật ActiveEnvironment nếu môi trường bị xóa đang là active
        activeEnvironmentRepository.findByProjectId(env.getProjectId()).ifPresent(active -> {
            if (environmentId.equals(active.getEnvironmentId())) {
                List<Environment> remainingEnvs = environmentRepository.findByProjectId(env.getProjectId());
                if (!remainingEnvs.isEmpty()) {
                    active.setEnvironmentId(remainingEnvs.get(0).getId());
                    activeEnvironmentRepository.save(active);
                } else {
                    active.setEnvironmentId(null);
                    activeEnvironmentRepository.save(active);
                }
            }
        });
    }

    public ActiveEnvironmentResponse setActiveEnvironment(UUID projectId, UUID environmentId) {
        Environment env = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!env.getProjectId().equals(projectId)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        ActiveEnvironment active = activeEnvironmentRepository.findByProjectId(projectId)
                .orElseGet(() -> ActiveEnvironment.builder().projectId(projectId).build());

        active.setEnvironmentId(environmentId);
        active = activeEnvironmentRepository.save(active);

        return environmentMapper.toActiveEnvironmentResponse(active);
    }

    public EnvironmentResponse getActiveEnvironment(UUID projectId) {
        Optional<ActiveEnvironment> activeOpt = activeEnvironmentRepository.findByProjectId(projectId);
        UUID activeId = null;

        if (activeOpt.isPresent()) {
            activeId = activeOpt.get().getEnvironmentId();
        }

        if (activeId == null) {
            List<EnvironmentResponse> allEnvs = getEnvironments(projectId);
            if (!allEnvs.isEmpty()) {
                return allEnvs.get(0);
            }
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        Environment env = environmentRepository.findById(activeId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        List<EnvironmentVariable> vars = environmentVariableRepository.findByEnvironmentId(activeId);
        return environmentMapper.toEnvironmentResponse(env, vars);
    }

    public EnvironmentVariableResponse addVariable(UUID environmentId, CreateVariableRequest request) {
        if (!environmentRepository.existsById(environmentId)) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        EnvironmentVariable variable = EnvironmentVariable.builder()
                .environmentId(environmentId)
                .variableKey(request.getKey() != null ? request.getKey() : "newVar")
                .initialValue(request.getInitialValue() != null ? request.getInitialValue() : "")
                .currentValue(request.getCurrentValue() != null ? request.getCurrentValue() : "")
                .type(request.getType() != null ? request.getType() : "text")
                .isEnabled(request.getIsEnabled() == null || request.getIsEnabled())
                .isSecret(request.getIsSecret() != null && request.getIsSecret())
                .build();

        variable = environmentVariableRepository.save(variable);
        return environmentMapper.toVariableResponse(variable);
    }

    public EnvironmentVariableResponse updateVariable(UUID variableId, CreateVariableRequest request) {
        EnvironmentVariable variable = environmentVariableRepository.findById(variableId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (request.getKey() != null) {
            variable.setVariableKey(request.getKey());
        }
        if (request.getInitialValue() != null) {
            variable.setInitialValue(request.getInitialValue());
        }
        if (request.getCurrentValue() != null) {
            variable.setCurrentValue(request.getCurrentValue());
        }
        if (request.getType() != null) {
            variable.setType(request.getType());
        }
        if (request.getIsEnabled() != null) {
            variable.setEnabled(request.getIsEnabled());
        }
        if (request.getIsSecret() != null) {
            variable.setSecret(request.getIsSecret());
        }

        variable = environmentVariableRepository.save(variable);
        return environmentMapper.toVariableResponse(variable);
    }

    public void deleteVariable(UUID variableId) {
        if (!environmentVariableRepository.existsById(variableId)) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        environmentVariableRepository.deleteById(variableId);
    }

    public ResolveResponse resolveVariables(UUID projectId, ResolveRequest request) {
        String text = request.getText();
        if (text == null || text.isEmpty()) {
            return ResolveResponse.builder().resolvedText(text).build();
        }

        Optional<ActiveEnvironment> activeOpt = activeEnvironmentRepository.findByProjectId(projectId);
        if (activeOpt.isEmpty() || activeOpt.get().getEnvironmentId() == null) {
            return ResolveResponse.builder().resolvedText(text).build();
        }

        UUID activeId = activeOpt.get().getEnvironmentId();
        List<EnvironmentVariable> variables = environmentVariableRepository.findByEnvironmentId(activeId);

        Map<String, String> varMap = variables.stream()
                .filter(EnvironmentVariable::isEnabled)
                .collect(Collectors.toMap(
                        EnvironmentVariable::getVariableKey,
                        v -> {
                            if (v.getCurrentValue() != null && !v.getCurrentValue().isEmpty()) {
                                return v.getCurrentValue();
                            }
                            return v.getInitialValue() != null ? v.getInitialValue() : "";
                        },
                        (v1, v2) -> v1
                ));

        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            if (varMap.containsKey(key)) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(varMap.get(key)));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);

        return ResolveResponse.builder().resolvedText(sb.toString()).build();
    }
}
