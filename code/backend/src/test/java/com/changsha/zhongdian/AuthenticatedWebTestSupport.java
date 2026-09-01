package com.changsha.zhongdian;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;

import com.changsha.zhongdian.dictionary.repository.SystemDictItemRepository;
import com.changsha.zhongdian.dictionary.repository.SystemDictTypeRepository;
import com.changsha.zhongdian.pile.repository.PileRepository;
import com.changsha.zhongdian.pile.repository.PileStatusLogRepository;
import com.changsha.zhongdian.station.repository.StationRepository;
import com.changsha.zhongdian.station.repository.StationStatusLogRepository;
import com.changsha.zhongdian.user.entity.SystemUser;
import com.changsha.zhongdian.user.entity.UserRole;
import com.changsha.zhongdian.user.entity.UserStatus;
import com.changsha.zhongdian.user.repository.SystemUserRepository;
import com.changsha.zhongdian.user.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 无数据库的 Web 层集成测试基类。
 * 为什么：这批测试的设计意图是验证鉴权、参数校验与响应结构，不应依赖真实数据库；
 * 但登录链路已统一走数据库认证，且字典/用户/站点等组件强依赖 JPA Repository，
 * 因此统一以 @MockBean 提供 Repository，并 stub 数据库用户让 /api/auth/login 可走通。
 * 后续里程碑引入 Testcontainers 或嵌入式 DB 后，可移除 JPA 排除项恢复真实启动链路测试。
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
public abstract class AuthenticatedWebTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PasswordHasher passwordHasher;

    @MockBean
    protected SystemUserRepository systemUserRepository;

    @MockBean
    protected SystemDictTypeRepository systemDictTypeRepository;

    @MockBean
    protected SystemDictItemRepository systemDictItemRepository;

    @MockBean
    protected StationRepository stationRepository;

    @MockBean
    protected StationStatusLogRepository stationStatusLogRepository;

    @MockBean
    protected PileRepository pileRepository;

    @MockBean
    protected PileStatusLogRepository pileStatusLogRepository;

    protected String loginAsBusinessAdmin() throws Exception {
        return loginAs("13800000001", "dev-biz-password", UserRole.BUSINESS_ADMIN, "业务管理员");
    }

    protected String loginAsSystemAdmin() throws Exception {
        return loginAs("admin", "dev-admin-password", UserRole.SYSTEM_ADMIN, "系统管理员");
    }

    protected String loginAs(String account, String rawPassword, UserRole role, String name) throws Exception {
        stubDatabaseUser(account, rawPassword, role, name);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "account": "%s",
                                  "password": "%s"
                                }
                                """.formatted(account, rawPassword)))
                .andExpect(status().isOk())
                .andReturn();
        return extractToken(loginResult);
    }

    protected SystemUser stubDatabaseUser(String account, String rawPassword, UserRole role, String name) {
        SystemUser user = new SystemUser();
        user.setAccount(account);
        user.setPhone(account);
        user.setName(name);
        user.setPasswordHash(passwordHasher.hash(rawPassword));
        user.setRoleCode(role);
        user.setUserStatus(UserStatus.ENABLED);

        when(systemUserRepository.findByAccount(account)).thenReturn(Optional.of(user));
        // 为什么：登录成功后会回写最后登录时间，mock 的 save 需返回同一实例并补齐时间，
        // 否则登录响应中的 lastLoginAt 与 /api/auth/me 的回查都会拿到空值
        when(systemUserRepository.save(any(SystemUser.class))).thenAnswer(invocation -> {
            SystemUser saving = invocation.getArgument(0);
            if (saving.getLastLoginAt() == null) {
                saving.setLastLoginAt(LocalDateTime.now());
            }
            return saving;
        });
        return user;
    }

    protected String extractToken(MvcResult loginResult) throws UnsupportedEncodingException {
        String responseBody = loginResult.getResponse().getContentAsString();
        return responseBody.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
