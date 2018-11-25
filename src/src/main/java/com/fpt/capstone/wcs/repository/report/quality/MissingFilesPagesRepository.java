package com.fpt.capstone.wcs.repository.report.quality;

import com.fpt.capstone.wcs.model.entity.report.quality.MissingFileReport;
import com.fpt.capstone.wcs.model.entity.website.PageOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissingFilesPagesRepository extends JpaRepository<MissingFileReport,Long> {
    List<MissingFileReport> findAllByPageOption(PageOption pageOption);
    List<MissingFileReport> findAllByPageOptionAndPages(PageOption pageOption, String url);
    void removeAllByPageOption(PageOption pageOption);
}