package cn.sparrowmini.common.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 用于做排序的关联表
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortedFile implements Serializable {
    private int seq;
    private String fileId;
    private String name;
    private long size;
}
