package com.fpt.capstone.wcs.model;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SiteLink {
    private String srcUrl;
    private String desUrl;
    private int desType;
}