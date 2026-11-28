package com.example.quanlydaotao.n1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.quanlydaotao.n1.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            select u from User u
            where u.deletedAt is null
              and u.isActive = true
              and (
                :keyword = ''
                or lower(u.fullName) like lower(concat('%', :keyword, '%'))
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or lower(u.username) like lower(concat('%', :keyword, '%'))
                or exists (
                  select s.id
                  from Student s
                  where u.email is not null
                    and lower(s.email) = lower(u.email)
                    and lower(s.studentCode) like lower(concat('%', :keyword, '%'))
                )
                or exists (
                  select ur.id
                  from UserRole ur, Role r
                  where ur.userId = u.id
                    and ur.roleId = r.id
                    and ur.isActive = true
                    and ur.deletedAt is null
                    and r.isActive = true
                    and r.deletedAt is null
                    and (
                      lower(r.name) like lower(concat('%', :keyword, '%'))
                      or lower(r.code) like lower(concat('%', :keyword, '%'))
                    )
                )
              )
            order by u.fullName
            """)
    List<User> searchActiveUsers(@Param("keyword") String keyword);
}
