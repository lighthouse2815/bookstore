package com.bookstore.bookstore.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.nullValue;

import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.UserWebMapper;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, UserWebMapper.class})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @MockitoBean
    private AdminAuditSupport adminAuditSupport;

    @Test
    void create_whenAdminAuthenticated_returnsCreatedUser() throws Exception {
        given(userService.createByAdmin(any())).willReturn(buildUser("STAFF"));

        mockMvc.perform(post("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "staff",
                                  "password": "secret123",
                                  "phoneNumber": "0123456789",
                                  "email": "staff@gmail.com",
                                  "firstName": "First",
                                  "lastName": "Last",
                                  "avatarUrl": null,
                                  "gender": "MALE",
                                  "dateOfBirth": "2000-01-01",
                                  "roleName": "STAFF"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("staff"))
                .andExpect(jsonPath("$.data.roles[0]").value("STAFF"));
    }

    @Test
    void create_whenPhoneNumberIsNull_returnsCreatedUser() throws Exception {
        given(userService.createByAdmin(any())).willReturn(buildUserWithPhone(null, "STAFF"));

        mockMvc.perform(post("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "staff",
                                  "password": "secret123",
                                  "phoneNumber": null,
                                  "email": "staff@gmail.com",
                                  "firstName": "First",
                                  "lastName": "Last",
                                  "avatarUrl": null,
                                  "gender": "MALE",
                                  "dateOfBirth": "2000-01-01",
                                  "roleName": "STAFF"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phoneNumber").value(nullValue()));

        verify(userService).createByAdmin(any());
    }

    @Test
    void updateStaff_whenAdminAuthenticated_returnsUpdatedStaff() throws Exception {
        User updatedStaff = buildUser("STAFF", "ADMIN");
        updatedStaff.updateManagedInfo(
                "newstaff@gmail.com",
                "0987654321",
                new LinkedHashSet<>(List.of(buildRole("STAFF"), buildRole("ADMIN")))
        );
        given(userService.getByIdIncludingDeleted(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .willReturn(buildUser("STAFF"));
        given(userService.updateStaffByAdmin(any())).willReturn(updatedStaff);

        mockMvc.perform(put("/api/admin/users/staff/00000000-0000-0000-0000-000000000001")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "0987654321",
                                  "email": "newstaff@gmail.com",
                                  "roleNames": ["STAFF", "ADMIN"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("staff"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0987654321"))
                .andExpect(jsonPath("$.data.email").value("newstaff@gmail.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.locked").value(false))
                .andExpect(jsonPath("$.data.roles[0]").value("STAFF"))
                .andExpect(jsonPath("$.data.roles[1]").value("ADMIN"));
    }

    @Test
    void updateStaff_whenPhoneNumberIsNull_returnsUpdatedStaff() throws Exception {
        User updatedStaff = buildUserWithPhone(null, "STAFF", "ADMIN");
        given(userService.getByIdIncludingDeleted(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .willReturn(buildUser("STAFF"));
        given(userService.updateStaffByAdmin(any())).willReturn(updatedStaff);

        mockMvc.perform(put("/api/admin/users/staff/00000000-0000-0000-0000-000000000001")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": null,
                                  "email": "staff@gmail.com",
                                  "roleNames": ["STAFF", "ADMIN"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phoneNumber").value(nullValue()));

        verify(userService).updateStaffByAdmin(any());
    }

    @Test
    void lockUser_whenAdminAuthenticated_returnsLockedUser() throws Exception {
        User lockedUser = buildUser("USER");
        lockedUser.updateLockStatus(true);
        given(userService.getByIdIncludingDeleted(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .willReturn(buildUser("USER"));
        given(userService.updateLockByAdmin(any())).willReturn(lockedUser);

        mockMvc.perform(put("/api/admin/users/00000000-0000-0000-0000-000000000001/lock")
                        .with(jwt().jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000999"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("user"))
                .andExpect(jsonPath("$.data.locked").value(true));
    }

    @Test
    void unlockUser_whenAdminAuthenticated_returnsUnlockedUser() throws Exception {
        User unlockedUser = buildUser("USER");
        given(userService.getByIdIncludingDeleted(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .willReturn(buildUser("USER"));
        given(userService.updateLockByAdmin(any())).willReturn(unlockedUser);

        mockMvc.perform(put("/api/admin/users/00000000-0000-0000-0000-000000000001/unlock")
                        .with(jwt().jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000999"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("user"))
                .andExpect(jsonPath("$.data.locked").value(false));
    }

    @Test
    void getCustomers_whenAdminAuthenticated_returnsCustomerList() throws Exception {
        given(userService.getCustomers()).willReturn(List.of(buildUser("USER")));

        mockMvc.perform(get("/api/admin/users/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("user"))
                .andExpect(jsonPath("$.data[0].email").value("user@gmail.com"))
                .andExpect(jsonPath("$.data[0].roles[0]").value("USER"));
    }

    @Test
    void getStaffs_whenAdminAuthenticated_returnsStaffList() throws Exception {
        given(userService.getStaffs()).willReturn(List.of(buildUser("STAFF")));

        mockMvc.perform(get("/api/admin/users/staff")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("staff"))
                .andExpect(jsonPath("$.data[0].roles[0]").value("STAFF"));
    }

    @Test
    void getAdmins_whenAdminAuthenticated_returnsAdminList() throws Exception {
        given(userService.getAdmins()).willReturn(List.of(buildUser("ADMIN")));

        mockMvc.perform(get("/api/admin/users/admins")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[0].roles[0]").value("ADMIN"));
    }

    @Test
    void getCustomers_whenNotAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteByAdmin_whenAdminAuthenticated_returnsSuccess() throws Exception {
        given(userService.getByIdIncludingDeleted(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .willReturn(buildUser("USER"));
        willDoNothing().given(userService).deleteByAdmin(any());

        mockMvc.perform(delete("/api/admin/users/00000000-0000-0000-0000-000000000001")
                        .with(jwt().jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000999"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Deleted"));
    }

    @Test
    void updateMe_whenPhoneNumberIsNull_returnsUpdatedUser() throws Exception {
        given(userService.update(any())).willReturn(buildUserWithPhone(null, "USER"));

        mockMvc.perform(put("/api/users/me")
                        .with(jwt().jwt(jwt -> jwt.subject("00000000-0000-0000-0000-000000000001"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "phoneNumber": null,
                                  "email": "user@gmail.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phoneNumber").value(nullValue()));

        verify(userService).update(any());
    }

    private User buildUser(String... roleNames) {
        return buildUserWithPhone("0123456789", roleNames);
    }

    private User buildUserWithPhone(String phoneNumber, String... roleNames) {
        Set<Role> roles = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            roles.add(buildRole(roleName));
        }

        return new User(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                roleNames[0].toLowerCase(),
                "password_hash",
                phoneNumber,
                roleNames[0].toLowerCase() + "@gmail.com",
                UserStatus.ACTIVE,
                false,
                roles,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private Role buildRole(String roleName) {
        return new Role(
                UUID.fromString("00000000-0000-0000-0000-000000000010"),
                roleName,
                "Default role",
                Set.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }
}
