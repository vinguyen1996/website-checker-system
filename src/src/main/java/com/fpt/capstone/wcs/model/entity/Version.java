package com.fpt.capstone.wcs.model.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Getter
@Setter
@Table(name = "version")
public class Version {
    @Id
    @GeneratedValue
    private long id;
    @NotNull
    int version;
    @NotNull
    Date time;

    @OneToMany(targetEntity = Page.class, mappedBy = "version", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Page> pages= new ArrayList<>();
}