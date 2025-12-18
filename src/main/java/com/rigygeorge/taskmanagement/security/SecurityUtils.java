package com.rigygeorge.taskmanagement.security;

import com.rigygeorge.taskmanagement.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("securityUtils")
public class SecurityUtils {
    
    public boolean isAdmin() {
        CustomUserDetails user = getCurrentUser();
        return user.getRole() == User.Role.ADMIN;
    }
    
    public boolean isManagerOrAdmin() {
        CustomUserDetails user = getCurrentUser();
        return user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.MANAGER;
    }
    
    public boolean canEditTask(UUID taskCreatorId) {
        CustomUserDetails user = getCurrentUser();
        return user.getRole() == User.Role.ADMIN || user.getId().equals(taskCreatorId);
    }
    
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}