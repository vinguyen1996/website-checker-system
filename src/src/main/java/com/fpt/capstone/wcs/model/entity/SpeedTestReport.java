package com.fpt.capstone.wcs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Getter
@Setter
@Table(name = "SpeedTestReport")
public class SpeedTestReport {
    @Id
    @GeneratedValue
    private Long id;
    private String url;
    private String interactiveTime;
    private String pageLoadTime;
    private String size;

    public SpeedTestReport(String url, String interactiveTime, String pageLoadTime, String size) {
        this.url = url;
        this.interactiveTime = interactiveTime;
        this.pageLoadTime = pageLoadTime;
        this.size = size;
    }

    @ManyToOne()
    @JoinColumn(name="page_option_id")
    @JsonIgnore
    private PageOption pageOption;

}
