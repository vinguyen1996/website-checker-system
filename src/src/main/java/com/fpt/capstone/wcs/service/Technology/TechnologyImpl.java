package com.fpt.capstone.wcs.service.Technology;

import com.fpt.capstone.wcs.model.entity.*;
import com.fpt.capstone.wcs.model.pojo.RequestCommonPOJO;
import com.fpt.capstone.wcs.repository.CookieRepository;
import com.fpt.capstone.wcs.repository.FaviconRepository;
import com.fpt.capstone.wcs.repository.JSCheckRepository;
import com.fpt.capstone.wcs.repository.PageOptionRepository;
import com.fpt.capstone.wcs.utils.Authenticate;
import com.fpt.capstone.wcs.utils.Constant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.stereotype.Service;
import sun.misc.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
public class TechnologyImpl implements TechnologyService {
    final PageOptionRepository pageOptionRepository;

    final Authenticate authenticate;

    final FaviconRepository faviconRepository;

    final JSCheckRepository jsCheckRepository;

    final CookieRepository cookieRepository;

    public TechnologyImpl(PageOptionRepository pageOptionRepository, Authenticate authenticate, FaviconRepository faviconRepository, JSCheckRepository jsCheckRepository, CookieRepository cookieRepository) {
        this.pageOptionRepository = pageOptionRepository;
        this.authenticate = authenticate;
        this.faviconRepository = faviconRepository;
        this.jsCheckRepository = jsCheckRepository;
        this.cookieRepository = cookieRepository;
    }

    @Override
    public Map<String, Object> faviconTest(RequestCommonPOJO request) {
        Map<String,Object> res = new HashMap<>();
        Website website =authenticate.isAuthGetSingleSite(request);
        if (website != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(), website, false);
            if(pageOption==null){
                request.setPageOptionId((long)-1);
            }
            if(request.getPageOptionId()!=-1) { //page option list is null
                List<Page> pages = pageOption.getPages();
                String urlRoot="";
                for(int i =0; i< pages.size();i++ ){
                    Pattern pattern = Pattern.compile("(http\\:|https\\:)//([\\w\\-?\\.?]+)?\\.([a-zA-Z]{2,3})?",Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(pages.get(i).getUrl());
                    while (matcher.find()){
                        urlRoot = matcher.group();
                    }
                }
                com.fpt.capstone.wcs.service.TechnologyService exp = new com.fpt.capstone.wcs.service.TechnologyService();
                List<FaviconReport> resultList = exp.checkFavicon(pages, pageOption, urlRoot);
                faviconRepository.removeAllByPageOption(pageOption);
                faviconRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("faviconReport", resultList);
                return res;
            }
            else {
                List<Page> pages = new ArrayList<>();
                Page page = new Page();
                page.setUrl(website.getUrl());
                page.setType(1);
                pages.add(page);
                String urlRoot="";
                for(int i =0; i< pages.size();i++ ){
                    Pattern pattern = Pattern.compile("(http\\:|https\\:)//([\\w\\-?\\.?]+)?\\.([a-zA-Z]{2,3})?",Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(pages.get(i).getUrl());
                    while (matcher.find()){
                        urlRoot = matcher.group();
                    }
                }

                List<FaviconReport> resultList = checkFavicon(pages, null,urlRoot);
                faviconRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("faviconReport", resultList);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> getLastestFaviconTest(RequestCommonPOJO request) {
        Map<String, Object> res = new HashMap<>();
        Website website = authenticate.isAuthGetSingleSite(request);
        if (website != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(), website, false);
            if(pageOption==null){
                request.setPageOptionId((long)-1);
            }
            if(request.getPageOptionId()!=-1) {
                List<FaviconReport> resultList = faviconRepository.findAllByPageOption(pageOption);
                res.put("faviconReport", resultList);
                res.put("action", Constant.SUCCESS);
                return res;
            } else {
                List<FaviconReport> resultList = faviconRepository.findAllByPageOptionAndUrl(null, website.getUrl());
                res.put("faviconReport", resultList);
                res.put("action", Constant.SUCCESS);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> getJavaErrrorTest(RequestCommonPOJO request) {

        Map<String, Object> res = new HashMap<>();
        Website website = authenticate.isAuthGetSingleSite(request);
        if (website != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(), website, false);
            if(pageOption==null){
                request.setPageOptionId((long)-1);
            }



            if(request.getPageOptionId()!=-1) { //page option list is null
                List<Page> pages = pageOption.getPages();
                List<JavascriptReport> resultList = jsTestService(pages, pageOption);
                jsCheckRepository.removeAllByPageOption(pageOption);
                jsCheckRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("jsErrorReport", resultList);
                return res;
            }
            else {
                List<Page> pages = new ArrayList<>();
                Page page = new Page();
                page.setUrl(website.getUrl());
                page.setType(1);
                pages.add(page);

                List<JavascriptReport> resultList =jsTestService(pages, null);
                jsCheckRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("jsErrorReport", resultList);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> getLastestSpeedTest(RequestCommonPOJO request) {
        Map<String, Object> res = new HashMap<>();
        Website website = authenticate.isAuthGetSingleSite(request);
        if (website != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(), website, false);
            if(pageOption==null){
                request.setPageOptionId((long)-1);
            }

            if(request.getPageOptionId()!=-1) {

                List<JavascriptReport> resultList = jsCheckRepository.findAllByPageOption(pageOption);
                res.put("jsErrorReport", resultList);
                res.put("action", Constant.SUCCESS);
                return res;
            } else {
                List<JavascriptReport> resultList = jsCheckRepository.findAllByPageOption(null);
                res.put("jsErrorReport", resultList);
                res.put("action", Constant.SUCCESS);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> getCookies(RequestCommonPOJO request) {
        return null;
    }

    @Override
    public Map<String, Object> getLastestCookies(RequestCommonPOJO request) {
        return null;
    }

    public List<JavascriptReport> jsTestService(List<Page> list, PageOption option) {
        System.setProperty("webdriver.chrome.driver", Constant.CHROME_DRIVER);
        //Asign list JS info
        List<JavascriptReport> resultList = new ArrayList<>();
        final CyclicBarrier gate = new CyclicBarrier(list.size());
        List<Thread> listThread = new ArrayList<>();
        for (Page u : list) {
            listThread.add(new Thread() {
                public void run() {
                    try {
                        gate.await();
                        System.out.println("start testing url= " + u.getUrl());
                        //DesiredCapabilities
                        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                        LoggingPreferences loggingprefs = new LoggingPreferences();
                        loggingprefs.enable(LogType.BROWSER, Level.ALL);
                        capabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingprefs);
                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.addArguments("--headless");
                        WebDriver driver = new ChromeDriver(chromeOptions);

                        driver.get(u.getUrl());

                        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
                        for (LogEntry entry : logEntries) {
                            String pattern = "(http(.*?)\\s)";
                            Pattern pt = Pattern.compile(pattern);
                            Matcher matcher = pt.matcher(entry.getMessage().toString());
                            String messages = "";
                            if (matcher.find()) {
                                messages = entry.getMessage().toString().replace(matcher.group(0), "");
                            }
                            JavascriptReport report = new JavascriptReport(messages, entry.getLevel().toString(), u.getUrl());
                            report.setPageOption(option);
                            resultList.add(report);
                        }
                        driver.quit();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        Logger.getLogger(TechnologyService.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            });
        }
        for (Thread t : listThread) {
            System.out.println("Threed start");
            t.start();
        }

        for (Thread t : listThread) {
            System.out.println("Threed join");
            try {
                t.join();
            } catch (InterruptedException e) {
                Logger.getLogger(TechnologyService.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        return resultList;

    }



    public  List<FaviconReport> checkFavicon(List<Page> list, PageOption option, String urlRoot) {
        List<FaviconReport> fav = new ArrayList();
        boolean flagMethod1 = false;
        String urlFaviconMethod1 = urlRoot + "/favicon.ico";
        int httpMessage = verifyHttpMessage(urlFaviconMethod1);
        if (httpMessage == 200) {
            byte[] capacity = getBytes(urlFaviconMethod1);
            if (capacity.length != 0) {
                System.out.println("Favicon URL: " + urlFaviconMethod1 + " Message: " + httpMessage + " Capacity: " + capacity.length);
                flagMethod1 = true;
            }
        }
        for ( Page urlNew : list) {
            if (flagMethod1 == true) {
                System.out.println(urlNew.getUrl().startsWith(urlRoot));
                if(urlNew.getUrl().startsWith(urlRoot)){
                    FaviconReport faviconMethod1 = new FaviconReport(urlFaviconMethod1, urlNew.getUrl(), "16x16");
                    faviconMethod1.setPageOption(option);
                    fav.add(faviconMethod1);
                }
                else{
                    FaviconReport faviconMethod1 = new FaviconReport("External Link", urlNew.getUrl(), "");
                    faviconMethod1.setPageOption(option);
                    fav.add(faviconMethod1);
                }
            } else {
                try {
                    Document doc = Jsoup.connect(urlNew.getUrl()).ignoreContentType(true).get();

                    Elements elem = doc.head().select("link[rel~=(shortcut icon|icon|apple-touch-icon-precomposed|nokia-touch-icon)]");
                    System.out.println(elem.size());
                    if (elem.size() == 0) {
                        FaviconReport favicon = new FaviconReport("Missing Favicon", urlNew.getUrl(), "undefine");
                        favicon.setPageOption(option);
                        fav.add(favicon);
                    }
                    for (Element element : elem) {
                        String size = element.attr("sizes");

                        if (size.equals("")) {
                            size = "undefine";
                        }
                        String href = elem.attr("href");
                        int code = verifyHttpMessage(href);
                        if (code == 200) {
                            FaviconReport favicon = new FaviconReport(href, urlNew.getUrl(), size);
                            favicon.setPageOption(option);
                            fav.add(favicon);
                            System.out.println("Favicon: " + href + " - Web Address: " + urlNew + " - Size: " + size + " http code: " + code);
                        }
                        if (code != 200) {
                            String urlFavAgain = urlRoot + href;
                            int checkFaviconResponeAgain = verifyHttpMessage(urlFavAgain);
                            if (checkFaviconResponeAgain == 200) {
                                FaviconReport favicon = new FaviconReport(urlFavAgain, urlNew.getUrl(), size);
                                favicon.setPageOption(option);
                                fav.add(favicon);
                                System.out.println("Favicon: " + urlFavAgain + " - Web Address: " + urlNew + " - Size: " + size + " http code: " + checkFaviconResponeAgain);
                            }
                            if (checkFaviconResponeAgain != 200) {
                                String urlFavLast = "https:" + href;
                                int checkFaviconResponeLast = verifyHttpMessage(urlFavLast);
                                if (checkFaviconResponeLast == 200) {
                                    FaviconReport favicon = new FaviconReport(urlFavLast, urlNew.getUrl(), size);
                                    favicon.setPageOption(option);
                                    fav.add(favicon);
                                    System.out.println("Favicon: " + urlFavLast + " - Web Address: " + urlNew + " - Size: " + size + " http code: " + checkFaviconResponeLast);
                                }
                            }
                        }

                    }
                } catch (IOException ex) {
                    Logger.getLogger( TechnologyImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return fav;
    }

    private byte[] getBytes(String url) {
        byte[] b = new byte[0];
        try {
            URL urlTesst = new URL(url);
            URLConnection uc = urlTesst.openConnection();
            int len = uc.getContentLength();
            InputStream in = new BufferedInputStream(uc.getInputStream());

            try {
                b = IOUtils.readFully(in, len, true);
            } finally {
                in.close();
            }
        } catch (IOException ex) {

        }

        return b;
    }

    private int verifyHttpMessage(String url) {
        int message;
        try {
            URL urlTesst = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlTesst.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 ");
            message = connection.getResponseCode();
        } catch (Exception e) {
            message = 404;
        }
        return message;
    }
}