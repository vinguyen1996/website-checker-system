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
@Table(name = "BrokenPageReport")

public class BrokenPageReport {
    @Id
    @GeneratedValue
    private Long id;
    private String urlPage;
    private String stt;
    private int httpCode;
    private String httpMessage;

    public BrokenPageReport(String urlPage, String stt, int httpCode, String httpMessage) {
        this.urlPage = urlPage;
        this.stt = stt;
        this.httpCode = httpCode;
        this.httpMessage = httpMessage;
    }


    @ManyToOne()
    @JoinColumn(name="page_option_id")
    @JsonIgnore
    private PageOption pageOption;


}
