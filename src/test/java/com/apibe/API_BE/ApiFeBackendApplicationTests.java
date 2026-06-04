package com.apibe.API_BE;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.apibe.API_BE.module.admin.repository.AdminActivityLogRepository;
import com.apibe.API_BE.module.admin.repository.AdminAiConversationRepository;
import com.apibe.API_BE.module.admin.repository.AdminApiTestHistoryRepository;
import com.apibe.API_BE.module.admin.repository.AdminAuditLogRepository;
import com.apibe.API_BE.module.admin.repository.AdminPaymentRepository;
import com.apibe.API_BE.module.admin.repository.AdminProjectRepository;
import com.apibe.API_BE.module.admin.repository.AdminUserRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ApiFeBackendApplicationTests {

    @MockitoBean
    private AdminUserRepository adminUserRepository;

    @MockitoBean
    private AdminProjectRepository adminProjectRepository;

    @MockitoBean
    private AdminPaymentRepository adminPaymentRepository;

    @MockitoBean
    private AdminApiTestHistoryRepository adminApiTestHistoryRepository;

    @MockitoBean
    private AdminAiConversationRepository adminAiConversationRepository;

    @MockitoBean
    private AdminActivityLogRepository adminActivityLogRepository;

    @MockitoBean
    private AdminAuditLogRepository adminAuditLogRepository;

    @Test
    void contextLoads() {
    }
}

