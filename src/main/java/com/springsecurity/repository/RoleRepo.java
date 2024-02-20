package com.springsecurity.repository;

import com.springsecurity.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<Role, Integer> {

    @Query("select r from Role r where r.name = :name")
    public Role findByName(String name);
}
