package com.fpt.capstone.wcs.controller.test;

import com.fpt.capstone.wcs.model.pojo.RequestCommonPOJO;

import com.fpt.capstone.wcs.model.pojo.RequestReportPOJO;
import com.fpt.capstone.wcs.service.report.content.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    ContentService contentService;

    @CrossOrigin
    @Transactional
    @PostMapping("/api/pagestest")
    public Map<String, Object> getDataPagesTest(@RequestBody RequestCommonPOJO request) throws InterruptedException {
       return contentService.getDataPagesTest(request);
    }

    @CrossOrigin
    @PostMapping("/api/pagestest/lastest")
    public Map<String, Object> getLastestPageTest(@RequestBody RequestCommonPOJO request)
    {
       return  contentService.getLastestPageTest(request);
    }

    @PostMapping("/api/pageTest/saveReport")
    public  Map<String, Object> savePageReport(@RequestBody RequestReportPOJO report){
        return  contentService.savePageReport(report);
    }

    @CrossOrigin
    @Transactional
    @PostMapping("/api/redirectiontest")
    public Map<String, Object> getDataRedirectTest(@RequestBody RequestCommonPOJO request) throws InterruptedException, IOException {
      return  contentService.getDataRedirectTest(request);
    }

    @CrossOrigin
    @PostMapping("/api/redirectiontest/lastest")
    public Map<String, Object> getLastestLinkRedirection(@RequestBody RequestCommonPOJO request)
    {
       return contentService.getLastestLinkRedirection(request);
    }

    @PostMapping("/api/redirectiontest/saveReport")
    public Map<String, Object> saveRedirectionTest(@RequestBody RequestReportPOJO report){
        return contentService.saveRedirectionReport(report);
    }

    @CrossOrigin
    @Transactional
    @PostMapping("/api/contactDetail")
    public Map<String, Object> getDataContactDetail(@RequestBody RequestCommonPOJO request) throws InterruptedException, IOException {
        return contentService.getDataContactDetail(request);
    }

    @CrossOrigin
    @PostMapping("/api/contactDetail/lastest")
    public Map<String, Object> getLastestContactDetail(@RequestBody RequestCommonPOJO request)
    {
       return  contentService.getLastestContactDetail(request);
    }
    @PostMapping("/api/contactDetail/saveReport")
    public Map<String, Object> saveContactDetailReport(@RequestBody RequestReportPOJO report){
        return contentService.saveContactDetailReport(report);
    }
}
