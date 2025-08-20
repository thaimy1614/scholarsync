package com.datn.school_service.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "homer_room_teacher_id", length = 50)
    private String homeroomTeacherId; //GVCN

    @Column(name = "school_year_id")
    private Long schoolYearId;

    @Column(name = "class_monitor_id", length = 50)
    private String classMonitorId;

    @Enumerated(EnumType.STRING)
    @Column(name ="main_session")
    private MainSession mainSession;

    @Column(name = "class_active")

    private boolean classActive;

    @OneToMany(mappedBy = "clazz")
    private Set<EvaluationSession> evaluationSessions = new HashSet<>();

    @ElementCollection
    @Nullable
    public List<String> studentId;

    @JoinColumn(name = "room_id")
    @ManyToOne
    private Room room;

    public enum MainSession {
        MORNING,
        AFTERNOON
    }

    @JoinColumn(name = "grade_id")
    @ManyToOne
    private Grade grade;

}