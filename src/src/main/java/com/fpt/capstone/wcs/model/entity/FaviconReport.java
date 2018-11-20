package com.fpt.capstone.wcs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Getter
@Setter
@Entity
@Table(name="FaviconReport")
public class FaviconReport {

    @Id
    @GeneratedValue
    private Long id;
    private String faviconUrl;
    private String url;
    private String sizeFavicon;


    public  FaviconReport(String faviconUrl, String url, String sizeFavicon){
        this.faviconUrl=faviconUrl;
        this.url = url;
        this.sizeFavicon  = sizeFavicon;
    }

    @ManyToOne()
    @JoinColumn(name="page_option_id")
    @JsonIgnore
    private PageOption pageOption;

}
