package com.example.application.data;

import jakarta.persistence.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditTrailListener {
    private static Log log = LogFactory.getLog(AuditTrailListener.class);

    @PostPersist
    private void afterInsert(Object object) {
        log.info("[JPA AUDIT] user='" + currentUser() + "', add complete for object: " + object);
    }

    @PostUpdate
    private void afterUpdate(Object object) {
        log.info("[JPA AUDIT] user='" + currentUser() + "', update complete for object: " + object);
    }

    @PostRemove
    private void afterDelete(Object object) {
        log.info("[JPA AUDIT] user='" + currentUser() + "', delete complete for object: " + object);
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "no user";
    }

}
