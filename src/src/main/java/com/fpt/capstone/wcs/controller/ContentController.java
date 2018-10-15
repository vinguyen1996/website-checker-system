package com.fpt.capstone.wcs.controller;

import com.fpt.capstone.wcs.model.LinkRedirection;
import com.fpt.capstone.wcs.model.Pages;
import com.fpt.capstone.wcs.model.SpeedTest;
import com.fpt.capstone.wcs.model.Url;
import com.fpt.capstone.wcs.service.ContentService;
import com.fpt.capstone.wcs.service.ExperienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ContentController {
//    @Autowired
//    ContentService contentService;

    @PostMapping("/api/pagestest")
    public List<Pages> getDataPagesTest(@RequestBody Url[] list) throws IOException {
        List<Pages> resultList = new ArrayList<>();
        ContentService con = new ContentService();
        for(int i = 0 ; i< list.length; i++)  {
            String title  = con.getTitle(list[i].getUrl());
            int  httpcode = con.getStatus(list[i].getUrl());
            String canoUrl = con.getCanonicalUrl(list[i].getUrl());
            Pages  item = new Pages(httpcode,list[i].getUrl(),title,canoUrl);
            resultList.add(item);
        }
        return resultList;
    }

    @PostMapping("/api/redirectiontest")
    public List<LinkRedirection> getDataRedirectTest(@RequestBody Url[] list) throws IOException {
        ContentService con  = new ContentService();
        List<LinkRedirection> resultList = new ArrayList<>();
        for(int i = 0 ; i< list.length; i++) {
            int  httpcode = con.getStatus(list[i].getUrl());

            if(httpcode== HttpURLConnection.HTTP_MOVED_TEMP || httpcode == HttpURLConnection.HTTP_MOVED_PERM){
                URL siteURL = new URL(list[i].getUrl());
                HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent","Mozilla/5.0 ");
                String message = connection.getResponseMessage();
                String newUrl = connection.getHeaderField("Location");
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                connection .addRequestProperty("User-Agent", "Mozilla");
                connection.addRequestProperty("Referer", "google.com");
                LinkRedirection link = new LinkRedirection(httpcode,list[i].getUrl(), message ,newUrl);
                System.out.println(link.getDriectToUrl());
                resultList.add(link);
            }
        }
        return resultList;
    }
}
