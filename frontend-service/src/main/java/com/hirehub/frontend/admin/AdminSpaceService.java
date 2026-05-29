package com.hirehub.frontend.admin;

import com.hirehub.common.constants.EventType;
import com.hirehub.common.enums.UserRole;
import com.hirehub.frontend.auth.FrontendUserAccount;
import com.hirehub.frontend.auth.FrontendUserRepository;
import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.frontend.notification.FrontendEmailEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminSpaceService {

    private final FrontendUserRepository frontendUserRepository;
    private final FrontendEmailEventPublisher emailEventPublisher;

    public AdminSpaceService(
            FrontendUserRepository frontendUserRepository,
            FrontendEmailEventPublisher emailEventPublisher
    ) {
        this.frontendUserRepository = frontendUserRepository;
        this.emailEventPublisher = emailEventPublisher;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStats dashboardStats() {
        long totalUsers = frontendUserRepository.count();
        long candidats = frontendUserRepository.countByRole(UserRole.CANDIDAT);
        long recruteurs = frontendUserRepository.countByRole(UserRole.RECRUTEUR);
        long admins = frontendUserRepository.countByRole(UserRole.ADMIN);
        long recruiterPendingReview = frontendUserRepository.countByRoleAndRecruiterApprovedFalse(UserRole.RECRUTEUR);
        return new AdminDashboardStats(totalUsers, candidats, recruteurs, admins, recruiterPendingReview);
    }

    @Transactional(readOnly = true)
    public List<FrontendUserAccount> allUsers() {
        return frontendUserRepository.findAllByOrderByEmailAsc();
    }

    @Transactional(readOnly = true)
    public Optional<FrontendUserAccount> findUser(UUID userId) {
        return frontendUserRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public List<FrontendUserAccount> recruiters() {
        return frontendUserRepository.findAllByRoleOrderByEmailAsc(UserRole.RECRUTEUR);
    }

    @Transactional
    public void blockUser(UUID userId) {
        FrontendUserAccount account = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable"));
        if (account.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Blocage d'un admin interdit.");
        }
        account.setBlocked(true);
        frontendUserRepository.save(account);
        publishAdminAction(account, "BLOCKED", RabbitMQConstants.ROUTING_USER_BLOCKED);
    }

    @Transactional
    public void unblockUser(UUID userId) {
        FrontendUserAccount account = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable"));
        account.setBlocked(false);
        frontendUserRepository.save(account);
        publishAdminAction(account, "UNBLOCKED", RabbitMQConstants.ROUTING_USER_BLOCKED);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        FrontendUserAccount account = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Compte introuvable"));
        if (account.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Suppression d'un admin interdit.");
        }
        publishAdminAction(account, "DELETED", RabbitMQConstants.ROUTING_USER_DELETED);
        frontendUserRepository.delete(account);
    }

    private void publishAdminAction(FrontendUserAccount account, String action, String routingKey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", account.getId().toString());
        payload.put("action", action);
        payload.put("role", account.getRole().name());
        payload.put("source", "frontend-service-admin");

        try {
            emailEventPublisher.publish(
                    EventType.ADMIN_USER_ACTION,
                    account.getEmail(),
                    account.getFullName(),
                    routingKey,
                    payload
            );
        } catch (Exception ignored) {
            // Action locale déjà appliquée; la publication event est best-effort.
        }
    }
}
