package com.example.learningplatform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Course> courses = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag tag)) {
            return false;
        }
        return name != null && name.equalsIgnoreCase(tag.name);
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return "Tag{id=" + getId() + ", name='" + name + "'}";
    }
}
