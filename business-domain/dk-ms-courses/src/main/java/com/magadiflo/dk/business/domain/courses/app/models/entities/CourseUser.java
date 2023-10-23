package com.magadiflo.dk.business.domain.courses.app.models.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "courses_users", uniqueConstraints = {@UniqueConstraint(columnNames = {"course_id", "user_id"})})
public class CourseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseUser that = (CourseUser) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CourseUser{");
        sb.append("id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append('}');
        return sb.toString();
    }
}
