package com.example.learningplatform.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, unique = true)
    private Module module;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSubmission> submissions = new ArrayList<>();

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
        if (module != null && module.getQuiz() != this) {
            module.setQuiz(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<QuizSubmission> getSubmissions() {
        return submissions;
    }

    @Override
    public String toString() {
        return "Quiz{id=" + getId() + ", title='" + title + "'}";
    }
}
