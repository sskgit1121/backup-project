package com.ssk.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ssk.auth.model.UserEntity;

import java.util.List;

@Repository
public interface UserAuthorizationRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = """
        SELECT DISTINCT p.permission_name 
        FROM public.t_permissions p
        JOIN public.t_role_permissions rp ON p.id = rp.permission_id
        JOIN public.t_user_roles ur ON rp.role_id = ur.role_id
        JOIN public.t_users u ON ur.user_id = u.id
        WHERE LOWER(u.email) = LOWER(:username) AND LOWER(u.tenant_id) = LOWER(:tenantId)
    """, nativeQuery = true)
    List<String> findPermissionsByUsernameAndTenant(
            @Param("username") String username, 
            @Param("tenantId") String tenantId
    );
    
    @Query(value = """
        SELECT r.role_name 
        FROM public.t_roles r
        JOIN public.t_user_roles ur ON r.id = ur.role_id
        JOIN public.t_users u ON ur.user_id = u.id
        WHERE LOWER(u.email) = LOWER(:username) AND LOWER(u.tenant_id) = LOWER(:tenantId)
    """, nativeQuery = true)
    List<String> findRolesByUsernameAndTenant(
            @Param("username") String username, 
            @Param("tenantId") String tenantId
    );
}
