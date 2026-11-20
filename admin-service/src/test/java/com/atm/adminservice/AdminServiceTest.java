package com.atm.adminservice;

import com.atm.adminservice.client.AccountInterface;
import com.atm.adminservice.client.AtmInterface;
import com.atm.adminservice.client.CardInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AccountInterface accountInterface;

    @Mock
    private AtmInterface atmInterface;

    @Mock
    private CardInterface cardInterface;

    @InjectMocks
    private AdminService adminService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void authenticateAdmin_Success() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("1234"));

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        boolean result = adminService.authenticateAdmin("admin", "1234");

        assertTrue(result);
        verify(adminRepository, times(1)).findByUsername("admin");
    }

    @Test
    void authenticateAdmin_InvalidPassword() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("1234"));

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        boolean result = adminService.authenticateAdmin("admin", "wrongpassword");

        assertFalse(result);
    }

    @Test
    void authenticateAdmin_UserNotFound() {
        when(adminRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        boolean result = adminService.authenticateAdmin("unknown", "1234");

        assertFalse(result);
    }

    @Test
    void updateAdminProfile_Success() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("oldUser");
        admin.setEmail("old@atm.com");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        adminService.updateAdminProfile(1L, "newUser", "new@atm.com");

        assertEquals("newUser", admin.getUsername());
        assertEquals("new@atm.com", admin.getEmail());
        verify(adminRepository, times(1)).save(admin);
    }

    @Test
    void updateAdminProfile_NotFound() {
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                adminService.updateAdminProfile(99L, "newUser", "new@atm.com")
        );
        verify(adminRepository, never()).save(any());
    }

    @Test
    void updateAccountLimit_Success() {
        doReturn(null).when(accountInterface).updateAccountLimit(1L, BigDecimal.valueOf(1000));

        assertDoesNotThrow(() -> adminService.updateAccountLimit(1L, BigDecimal.valueOf(1000)));

        verify(accountInterface, times(1)).updateAccountLimit(1L, BigDecimal.valueOf(1000));
    }

    @Test
    void changeTerminalStatus_Success() {
        doReturn(null).when(atmInterface).changeTerminalStatus(1L, "OFFLINE");

        assertDoesNotThrow(() -> adminService.changeTerminalStatus(1L, "OFFLINE"));

        verify(atmInterface, times(1)).changeTerminalStatus(1L, "OFFLINE");
    }

    @Test
    void manualUnblockCard_Success() {
        doReturn(null).when(cardInterface).unblockCard(1L);

        assertDoesNotThrow(() -> adminService.manualUnblockCard(1L));

        verify(cardInterface, times(1)).unblockCard(1L);
    }

    @Test
    void manualBlockCard_Success() {
        doReturn(null).when(cardInterface).blockCard(1L);

        assertDoesNotThrow(() -> adminService.manualBlockCard(1L));

        verify(cardInterface, times(1)).blockCard(1L);
    }

    @Test
    void forceResetCardPin_Success() {
        doReturn(null).when(cardInterface).forceResetCardPin(1L, "newTempHash");

        assertDoesNotThrow(() -> adminService.forceResetCardPin(1L, "newTempHash"));

        verify(cardInterface, times(1)).forceResetCardPin(1L, "newTempHash");
    }
}