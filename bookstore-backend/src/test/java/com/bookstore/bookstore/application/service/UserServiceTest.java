package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CreateUserCommand;
import com.bookstore.bookstore.application.command.UpdateStaffUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserLockCommand;
import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.enums.Gender;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IProfileRepository profileRepository;

    @Mock
    private IProfileService profileService;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getCustomers_returnsOnlyActiveUsersWithUserRole() {
        when(userRepository.findAllActive()).thenReturn(List.of(
                buildUser("USER"),
                buildUser("STAFF"),
                buildUser("ADMIN")
        ));

        List<User> result = userService.getCustomers();

        assertEquals(1, result.size());
        assertEquals("USER", result.getFirst().getRoles().iterator().next().getName());
        verify(userRepository).findAllActive();
        verify(userRepository, never()).findAllIncludingDeleted();
    }

    @Test
    void getStaffs_returnsOnlyActiveUsersWithStaffRole() {
        when(userRepository.findAllActive()).thenReturn(List.of(
                buildUser("USER"),
                buildUser("STAFF"),
                buildUser("ADMIN")
        ));

        List<User> result = userService.getStaffs();

        assertEquals(1, result.size());
        assertEquals("STAFF", result.getFirst().getRoles().iterator().next().getName());
    }

    @Test
    void getAdmins_returnsOnlyActiveUsersWithAdminRole() {
        when(userRepository.findAllActive()).thenReturn(List.of(
                buildUser("USER"),
                buildUser("STAFF"),
                buildUser("ADMIN"),
                buildUser("SHIPPER")
        ));

        List<User> result = userService.getAdmins();

        assertEquals(1, result.size());
        assertEquals("ADMIN", result.getFirst().getRoles().iterator().next().getName());
    }

    @Test
    void getShippers_returnsOnlyActiveUsersWithShipperRole() {
        when(userRepository.findAllActive()).thenReturn(List.of(
                buildUser("USER"),
                buildUser("STAFF"),
                buildUser("ADMIN"),
                buildUser("SHIPPER")
        ));

        List<User> result = userService.getShippers();

        assertEquals(1, result.size());
        assertEquals("SHIPPER", result.getFirst().getRoles().iterator().next().getName());
    }

    @Test
    void createByAdmin_createsActivePrivilegedUserAndProfile() {
        CreateUserCommand command = new CreateUserCommand(
                "staff",
                "secret123",
                "0123456789",
                "staff@gmail.com",
                "First",
                "Last",
                null,
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1),
                "staff"
        );
        Role staffRole = buildRole("STAFF");

        when(roleRepository.findByNameActive("STAFF")).thenReturn(Optional.of(staffRole));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(userRepository.existsByUsernameIncludingDeleted("staff")).thenReturn(false);
        when(userRepository.existsByPhoneNumberIncludingDeleted("0123456789")).thenReturn(false);
        when(userRepository.existsByEmailIncludingDeleted("staff@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileService.create(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createByAdmin(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(userRepository).save(userCaptor.capture());
        verify(profileService).create(profileCaptor.capture());

        assertEquals(UserStatus.ACTIVE, userCaptor.getValue().getStatus());
        assertEquals("hashed-secret", userCaptor.getValue().getPasswordHash());
        assertEquals("STAFF", userCaptor.getValue().getRoles().iterator().next().getName());
        assertEquals(userCaptor.getValue().getId(), profileCaptor.getValue().getUserId());
        assertEquals("staff", result.getUsername());
    }

    @Test
    void createByAdmin_allowsNullPhoneNumber() {
        CreateUserCommand command = new CreateUserCommand(
                "staff",
                "secret123",
                null,
                "staff@gmail.com",
                "First",
                "Last",
                null,
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1),
                "staff"
        );
        Role staffRole = buildRole("STAFF");

        when(roleRepository.findByNameActive("STAFF")).thenReturn(Optional.of(staffRole));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(userRepository.existsByUsernameIncludingDeleted("staff")).thenReturn(false);
        when(userRepository.existsByEmailIncludingDeleted("staff@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileService.create(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createByAdmin(command);

        assertNull(result.getPhoneNumber());
        verify(userRepository, never()).existsByPhoneNumberIncludingDeleted(any());
    }

    @Test
    void createByAdmin_rejectsNonPrivilegedRole() {
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> userService.createByAdmin(new CreateUserCommand(
                        "user",
                        "secret123",
                        "0123456789",
                        "user@gmail.com",
                        "First",
                        "Last",
                        null,
                        Gender.MALE,
                        java.time.LocalDate.of(2000, 1, 1),
                        "USER"
                ))
        );

        assertEquals(ApplicationErrorCode.USER_ROLE_NOT_ALLOWED, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
        verify(profileService, never()).create(any(Profile.class));
    }

    @Test
    void createByAdmin_allowsShipperRole() {
        CreateUserCommand command = new CreateUserCommand(
                "shipper",
                "secret123",
                "0123456789",
                "shipper@gmail.com",
                "First",
                "Last",
                null,
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1),
                "shipper"
        );
        Role shipperRole = buildRole("SHIPPER");

        when(roleRepository.findByNameActive("SHIPPER")).thenReturn(Optional.of(shipperRole));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(userRepository.existsByUsernameIncludingDeleted("shipper")).thenReturn(false);
        when(userRepository.existsByPhoneNumberIncludingDeleted("0123456789")).thenReturn(false);
        when(userRepository.existsByEmailIncludingDeleted("shipper@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileService.create(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createByAdmin(command);

        assertEquals("SHIPPER", result.getRoles().iterator().next().getName());
    }

    @Test
    void update_allowsClearingPhoneNumber() {
        User currentUser = buildUser("USER");

        when(userRepository.findByIdActive(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(new UpdateUserCommand(
                currentUser.getId(),
                currentUser.getUsername(),
                null,
                currentUser.getEmail()
        ));

        assertNull(result.getPhoneNumber());
        verify(userRepository, never()).existsByPhoneNumberIncludingDeleted(any());
    }

    @Test
    void updateStaffByAdmin_updatesOnlyUserFields() {
        User currentStaff = buildUser("STAFF");
        Role staffRole = buildRole("STAFF");
        Role adminRole = buildRole("ADMIN");

        when(userRepository.findByIdIncludingDeleted(currentStaff.getId())).thenReturn(Optional.of(currentStaff));
        when(userRepository.existsByPhoneNumberIncludingDeleted("0987654321")).thenReturn(false);
        when(userRepository.existsByEmailIncludingDeleted("newstaff@gmail.com")).thenReturn(false);
        when(roleRepository.findByNameActive("STAFF")).thenReturn(Optional.of(staffRole));
        when(roleRepository.findByNameActive("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateStaffByAdmin(new UpdateStaffUserCommand(
                currentStaff.getId(),
                "0987654321",
                "newstaff@gmail.com",
                new LinkedHashSet<>(List.of("STAFF", "ADMIN"))
        ));

        assertEquals("staff", result.getUsername());
        assertEquals("0987654321", result.getPhoneNumber());
        assertEquals("newstaff@gmail.com", result.getEmail());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertEquals(false, result.isLocked());
        assertEquals(Set.of("STAFF", "ADMIN"), result.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()));
        verify(profileService, never()).create(any(Profile.class));
    }

    @Test
    void updateStaffByAdmin_allowsClearingPhoneNumber() {
        User currentStaff = buildUser("STAFF");
        Role staffRole = buildRole("STAFF");

        when(userRepository.findByIdIncludingDeleted(currentStaff.getId())).thenReturn(Optional.of(currentStaff));
        when(roleRepository.findByNameActive("STAFF")).thenReturn(Optional.of(staffRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateStaffByAdmin(new UpdateStaffUserCommand(
                currentStaff.getId(),
                null,
                currentStaff.getEmail(),
                Set.of("STAFF")
        ));

        assertNull(result.getPhoneNumber());
        verify(userRepository, never()).existsByPhoneNumberIncludingDeleted(any());
    }

    @Test
    void updateStaffByAdmin_allowsManagingAdminUser() {
        User currentAdmin = buildUser("ADMIN");
        Role adminRole = buildRole("ADMIN");

        when(userRepository.findByIdIncludingDeleted(currentAdmin.getId())).thenReturn(Optional.of(currentAdmin));
        when(userRepository.existsByPhoneNumberIncludingDeleted("0987654321")).thenReturn(false);
        when(roleRepository.findByNameActive("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateStaffByAdmin(new UpdateStaffUserCommand(
                currentAdmin.getId(),
                "0987654321",
                "admin@gmail.com",
                Set.of("ADMIN")
        ));

        assertEquals("0987654321", result.getPhoneNumber());
        assertEquals("admin@gmail.com", result.getEmail());
        assertEquals(Set.of("ADMIN"), result.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void updateStaffByAdmin_rejectsRegularUser() {
        User regularUser = buildUser("USER");
        when(userRepository.findByIdIncludingDeleted(regularUser.getId())).thenReturn(Optional.of(regularUser));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> userService.updateStaffByAdmin(new UpdateStaffUserCommand(
                        regularUser.getId(),
                        "0987654321",
                        "user@gmail.com",
                        Set.of("STAFF")
                ))
        );

        assertEquals(ApplicationErrorCode.STAFF_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateStaffByAdmin_allowsManagingShipperUser() {
        User currentShipper = buildUser("SHIPPER");
        Role shipperRole = buildRole("SHIPPER");

        when(userRepository.findByIdIncludingDeleted(currentShipper.getId())).thenReturn(Optional.of(currentShipper));
        when(roleRepository.findByNameActive("SHIPPER")).thenReturn(Optional.of(shipperRole));
        when(userRepository.existsByPhoneNumberIncludingDeleted("0987654321")).thenReturn(false);
        when(userRepository.existsByEmailIncludingDeleted("newshipper@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateStaffByAdmin(new UpdateStaffUserCommand(
                currentShipper.getId(),
                "0987654321",
                "newshipper@gmail.com",
                Set.of("SHIPPER")
        ));

        assertEquals("0987654321", result.getPhoneNumber());
        assertEquals("newshipper@gmail.com", result.getEmail());
        assertEquals(Set.of("SHIPPER"), result.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void updateStaffByAdmin_rejectsUnchangedManagedInfo() {
        User currentStaff = buildUser("STAFF", "ADMIN");
        Role staffRole = buildRole("STAFF");
        Role adminRole = buildRole("ADMIN");

        when(userRepository.findByIdIncludingDeleted(currentStaff.getId())).thenReturn(Optional.of(currentStaff));
        when(roleRepository.findByNameActive("STAFF")).thenReturn(Optional.of(staffRole));
        when(roleRepository.findByNameActive("ADMIN")).thenReturn(Optional.of(adminRole));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> userService.updateStaffByAdmin(new UpdateStaffUserCommand(
                        currentStaff.getId(),
                        currentStaff.getPhoneNumber(),
                        currentStaff.getEmail(),
                        new LinkedHashSet<>(List.of("STAFF", "ADMIN"))
                ))
        );

        assertEquals(com.bookstore.bookstore.domain.exception.DomainErrorCode.USER_MANAGED_INFO_NOT_CHANGED, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateLockByAdmin_updatesLockStatusForOtherUser() {
        User currentUser = buildUser("USER");
        UUID adminId = UUID.randomUUID();

        when(userRepository.findByIdIncludingDeleted(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateLockByAdmin(new UpdateUserLockCommand(
                currentUser.getId(),
                adminId,
                true
        ));

        assertEquals(true, result.isLocked());
        verify(userRepository).save(currentUser);
    }

    @Test
    void updateLockByAdmin_rejectsUnchangedLockState() {
        User currentUser = buildLockedUser("USER");
        UUID adminId = UUID.randomUUID();

        when(userRepository.findByIdIncludingDeleted(currentUser.getId())).thenReturn(Optional.of(currentUser));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> userService.updateLockByAdmin(new UpdateUserLockCommand(
                        currentUser.getId(),
                        adminId,
                        true
                ))
        );

        assertEquals(com.bookstore.bookstore.domain.exception.DomainErrorCode.USER_LOCK_STATUS_NOT_CHANGED, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateLockByAdmin_rejectsManagingOwnAccount() {
        UUID userId = UUID.randomUUID();

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> userService.updateLockByAdmin(new UpdateUserLockCommand(userId, userId, true))
        );

        assertEquals(ApplicationErrorCode.USER_SELF_MANAGEMENT_NOT_ALLOWED, exception.getErrorCode());
        verify(userRepository, never()).findByIdActive(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteByAdmin_softDeletesOtherUser() {
        User adminUser = buildUser("ADMIN");
        User currentUser = buildUser("USER");

        when(userRepository.findByIdActive(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findByIdIncludingDeleted(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileRepository.findByUserIdActive(currentUser.getId())).thenReturn(Optional.empty());

        userService.deleteByAdmin(new DeleteUserCommand(currentUser.getId(), adminUser.getId()));

        assertEquals(true, currentUser.getDeletedAt() != null);
        verify(userRepository).save(currentUser);
    }

    @Test
    void deleteByAdmin_rejectsAlreadyDeletedUser() {
        User adminUser = buildUser("ADMIN");
        User deletedUser = buildDeletedUser("USER");

        when(userRepository.findByIdActive(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(userRepository.findByIdIncludingDeleted(deletedUser.getId())).thenReturn(Optional.of(deletedUser));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> userService.deleteByAdmin(new DeleteUserCommand(deletedUser.getId(), adminUser.getId()))
        );

        assertEquals(com.bookstore.bookstore.domain.exception.DomainErrorCode.USER_ALREADY_DELETED, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteByAdmin_rejectsDeletingOwnAccount() {
        UUID userId = UUID.randomUUID();

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> userService.deleteByAdmin(new DeleteUserCommand(userId, userId))
        );

        assertEquals(ApplicationErrorCode.USER_SELF_MANAGEMENT_NOT_ALLOWED, exception.getErrorCode());
        verify(userRepository, never()).findByIdActive(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    private static User buildUser(String... roleNames) {
        return buildUser(false, null, roleNames);
    }

    private static User buildLockedUser(String... roleNames) {
        return buildUser(true, null, roleNames);
    }

    private static User buildDeletedUser(String... roleNames) {
        return buildUser(false, Instant.EPOCH, roleNames);
    }

    private static User buildUser(boolean locked, Instant deletedAt, String... roleNames) {
        Set<Role> roles = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            roles.add(buildRole(roleName));
        }

        return new User(
                UUID.randomUUID(),
                roleNames[0].toLowerCase(),
                "password_hash",
                "0123456789",
                roleNames[0].toLowerCase() + "@gmail.com",
                UserStatus.ACTIVE,
                locked,
                roles,
                deletedAt == null ? Instant.parse("2024-01-01T00:00:00Z") : deletedAt,
                deletedAt == null ? Instant.parse("2024-01-01T00:00:00Z") : deletedAt,
                deletedAt
        );
    }

    private static Role buildRole(String roleName) {
        return new Role(
                UUID.randomUUID(),
                roleName,
                "Default role",
                Set.of(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }
}
