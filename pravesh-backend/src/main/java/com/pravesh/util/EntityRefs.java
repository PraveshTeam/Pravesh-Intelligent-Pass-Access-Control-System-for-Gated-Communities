package com.pravesh.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

// Builds a lazy reference to an entity from just its id, so a service holding
// only an id can set a relationship without an extra SELECT. Null id -> null.
@Component
public class EntityRefs {

    @PersistenceContext
    private EntityManager em;

    public <T> T ref(Class<T> type, Object id) {
        return id == null ? null : em.getReference(type, id);
    }
}
