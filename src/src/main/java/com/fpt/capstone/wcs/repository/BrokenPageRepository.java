package com.fpt.capstone.wcs.repository;

import com.fpt.capstone.wcs.model.entity.BrokenPageReport;
import com.fpt.capstone.wcs.model.entity.PageOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrokenPageRepository extends JpaRepository<BrokenPageReport,Long> {
    List<BrokenPageReport> findAllByPageOption(PageOption pageOption);
    List<BrokenPageReport> findAllByPageOptionAndUrlPage(PageOption pageOption, String url);
    void removeAllByPageOption(PageOption pageOption);
}
