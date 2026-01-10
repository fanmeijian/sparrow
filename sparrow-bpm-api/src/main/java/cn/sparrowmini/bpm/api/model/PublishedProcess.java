package cn.sparrowmini.bpm.api.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "spr_published_process")
public class PublishedProcess extends Process {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }
}
