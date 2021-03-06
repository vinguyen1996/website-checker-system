package com.fpt.capstone.wcs.service.report.experience;

import com.fpt.capstone.wcs.model.entity.report.quality.BrokenLinkReport;
import com.fpt.capstone.wcs.model.entity.user.Website;
import com.fpt.capstone.wcs.model.entity.report.experience.MobileLayoutReport;
import com.fpt.capstone.wcs.model.entity.report.experience.SpeedTestReport;
import com.fpt.capstone.wcs.model.entity.website.Page;
import com.fpt.capstone.wcs.model.entity.website.PageOption;
import com.fpt.capstone.wcs.model.pojo.RequestCommonPOJO;
import com.fpt.capstone.wcs.model.pojo.RequestReportPOJO;
import com.fpt.capstone.wcs.model.pojo.WebsiteUserPOJO;
import com.fpt.capstone.wcs.repository.report.experience.MobileLayoutRepository;
import com.fpt.capstone.wcs.repository.website.PageOptionRepository;
import com.fpt.capstone.wcs.repository.report.experience.SpeedtestRepository;
import com.fpt.capstone.wcs.service.system.authenticate.AuthenticateService;
import com.fpt.capstone.wcs.utils.CheckHTTPResponse;
import com.fpt.capstone.wcs.utils.Constant;
import com.fpt.capstone.wcs.utils.MathUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.cloudinary.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class ExperienceImpl implements ExperienceService {

    final
    AuthenticateService authenticate;
    final
    PageOptionRepository pageOptionRepository;
    final
    SpeedtestRepository speedtestRepository;
    final
    MobileLayoutRepository mobileLayoutRepository;

    @Autowired
    public ExperienceImpl(AuthenticateService authenticate,
                          PageOptionRepository pageOptionRepository,
                          SpeedtestRepository speedtestRepository,
                          MobileLayoutRepository mobileLayoutRepository) {
        this.authenticate = authenticate;
        this.pageOptionRepository = pageOptionRepository;
        this.speedtestRepository = speedtestRepository;
        this.mobileLayoutRepository = mobileLayoutRepository;
    }

    @Override
    public Map<String, Object> doSpeedTest(RequestCommonPOJO request) throws InterruptedException {
        Map<String, Object> res = new HashMap<>();
        WebsiteUserPOJO userWebsite = authenticate.isAuthGetUserAndWebsite(request);
        if (userWebsite != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(), userWebsite.getWebsite(), false);
            if (pageOption == null) {
                request.setPageOptionId((long) -1);
            }
            if (request.getPageOptionId() != -1) { //page option list is null


                List<Page> pages = pageOption.getPages();
                if (pages.size() == 0) {
                    Page page = new Page();
                    page.setUrl(userWebsite.getWebsite().getUrl());
                    page.setType(1);
                    pages.add(page);
                }
                List<SpeedTestReport> resultList = speedTestService(pages, pageOption);
                speedtestRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("speedtestReport", resultList);
                return res;
            } else {
                List<Page> pages = new ArrayList<>();
                Page page = new Page();
                page.setUrl(userWebsite.getWebsite().getUrl());
                page.setType(1);
                pages.add(page);
                List<SpeedTestReport> resultList = speedTestService(pages, null);
                resultList = speedtestRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("speedtestReport", resultList);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> saveSpeedTestReport(RequestReportPOJO request) {
        Map<String, Object> res = new HashMap<>();
        RequestCommonPOJO requestCommon = new RequestCommonPOJO();
        requestCommon.setPageOptionId(request.getPageOptionId());
        requestCommon.setUserId(request.getUserId());
        requestCommon.setWebsiteId(request.getWebsiteId());
        requestCommon.setUserToken(request.getUserToken());
        WebsiteUserPOJO userWebsite = authenticate.isAuthGetUserAndWebsite(requestCommon);
        if (userWebsite != null) {
            List<SpeedTestReport> listReport = new ArrayList<>();
            for (int i = 0; i < request.getListReportId().size(); i++) {
                Optional<SpeedTestReport> optionalReport = speedtestRepository.findById(request.getListReportId().get(i));
                if (optionalReport.isPresent()) {
                    SpeedTestReport report = optionalReport.get();
                    report.setDelFlag(false);
                    listReport.add(report);
                }
            }
            List<SpeedTestReport> results = speedtestRepository.saveAll(listReport);
            if (results.size() != 0) {
                res.put("action", Constant.SUCCESS);
                res.put("speedtestReport", results);
                return res;
            } else {
                res.put("action", Constant.INCORRECT);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> getHistorySpeedTestReport(RequestCommonPOJO request) {
        Map<String, Object> res = new HashMap<>();
        Date time = new Date(request.getReportTime());
        List<SpeedTestReport> speedList = speedtestRepository.findALlByCreatedTime(time);
        res.put("action",Constant.SUCCESS);
        res.put("data",speedList);
        return res;
    }

    @Override
    public Map<String, Object> getHistorySpeedTestList(RequestCommonPOJO request) {
        List<SpeedTestReport> list = speedtestRepository.findAllGroupByCreatedTimeAndPageOption(request.getUserId(), request.getPageOptionId());
        Map<String, Object> res = new HashMap<>();
        List<Long> dates = new ArrayList<>();
        for(int i = 0 ; i< list.size();i++)
        {
            dates.add(list.get(i).getCreatedTime().getTime());
        }
        res.put("action",Constant.SUCCESS);
        res.put("data",dates);
        return res;
    }

    @Override
    public Map<String, Object> getLastestSpeedTest(RequestCommonPOJO request) {
        Map<String, Object> res = new HashMap<>();
        Website website = authenticate.isAuthGetSingleSite(request);
        if (website != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(), website, false);
            if (pageOption == null) {
                request.setPageOptionId((long) -1);
            }
            if (request.getPageOptionId() != -1) {
                SpeedTestReport speedTestReport = speedtestRepository.findFirstByPageOptionAndDelFlagEqualsOrderByCreatedTimeDesc(pageOption, false);
                if (speedTestReport != null) {
                    Date lastedCreatedTime = speedTestReport.getCreatedTime();
                    List<SpeedTestReport> resultList = speedtestRepository.findAllByPageOptionAndCreatedTime(pageOption, lastedCreatedTime);
                    res.put("speedtestReport", resultList);
                    res.put("action", Constant.SUCCESS);
                    return res;
                } else {
                    res.put("speedtestReport", new ArrayList<>());
                    res.put("action", Constant.SUCCESS);
                    return res;
                }
            } else {
                List<SpeedTestReport> resultList = speedtestRepository.findAllByPageOptionAndUrl(null, website.getUrl());
                res.put("speedtestReport", resultList);
                res.put("action", Constant.SUCCESS);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }


    public List<SpeedTestReport> speedTestService(List<Page> list, PageOption option) throws InterruptedException {
        Date createdTime = new Date();
        System.setProperty("webdriver.chrome.driver", Constant.CHROME_DRIVER);
        //Asign list speed info
        List<SpeedTestReport> resultList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Constant.MAX_THREAD);
        for (Page p : list) {
            executor.submit(new Runnable() {
                @Override
                public void run()
                {

                    System.out.println("start testing url= " + p.getUrl());
                    //DesiredCapabilities
                    DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                    LoggingPreferences logPrefs = new LoggingPreferences();
                    logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--headless");
                    chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
                    WebDriver driver = new ChromeDriver(chromeOptions);//chay an

                    driver.get(p.getUrl());

                    List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
                    List<Double> entrySize = new ArrayList<>();
                    for (LogEntry entry : entries) {
                        Matcher dataLengthMatcher = Pattern.compile("encodedDataLength\":(.*?),").matcher(entry.getMessage());
                        if (dataLengthMatcher.find()) {
                            entrySize.add(Double.parseDouble(dataLengthMatcher.group().split(":")[1].split(",")[0]));
                        }
                    }

                    double totalByte = new MathUtil().calculateSumDoubleList(entrySize);
                    //page load page interact
                    WebDriverWait myWait = new WebDriverWait(driver, 60);
                    ExpectedCondition<Boolean> conditionCheck = new ExpectedCondition<Boolean>() {
                        public Boolean apply(WebDriver input) {
                            return ((((JavascriptExecutor) input).executeScript("return document.readyState").equals("complete")));
                        }
                    };
                    myWait.until(conditionCheck);

                    Long loadPage = (Long) ((JavascriptExecutor) driver).executeScript(
                            "return performance.timing.loadEventEnd  - performance.timing.navigationStart;");
                    double loadTime = loadPage;
                    double loadTime1 = Math.floor(loadTime / 1000 * 10) / 10;
                    Long interact = (Long) ((JavascriptExecutor) driver).executeScript(
                            "return performance.timing.domContentLoadedEventEnd  - performance.timing.navigationStart;");
                    double interactTime = interact;
                    double interactTime1 = Math.floor(interactTime / 1000 * 10) / 10;
                    double sizeTransferred1 = Math.floor(totalByte / 1000000 * 10) / 10;
                    SpeedTestReport speedTestReport = new SpeedTestReport(p.getUrl(), interactTime1 + "", loadTime1 + "", sizeTransferred1 + "");
                    speedTestReport.setPageOption(option);
                    speedTestReport.setCreatedTime(createdTime);
                    resultList.add(speedTestReport);
                    driver.quit();

                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE,TimeUnit.MILLISECONDS);

        return resultList;
    }

    @Override
    public Map<String, Object> getDataMobileLayout(RequestCommonPOJO request) throws InterruptedException {
        Map<String, Object> res = new HashMap<>();
        WebsiteUserPOJO userWebsite = authenticate.isAuthGetUserAndWebsite(request);
        if (userWebsite != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(),  userWebsite.getWebsite(), false);
            if (pageOption == null) {
                request.setPageOptionId((long) -1);
            }
            if (request.getPageOptionId() != -1) { //page option list is null
                List<Page> pages = pageOption.getPages();
                if (pages.size() == 0) {
                    Page page = new Page();
                    page.setUrl(userWebsite.getWebsite().getUrl());
                    page.setType(1);
                    pages.add(page);
                }
                List<MobileLayoutReport> resultList = mobileLayoutTestService(pages, pageOption);
                mobileLayoutRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("mobileLayoutTestReport", resultList);
                return res;
            } else {
                List<Page> pages = new ArrayList<>();
                Page page = new Page();
                page.setUrl(userWebsite.getWebsite().getUrl());
                page.setType(1);
                pages.add(page);
                List<MobileLayoutReport> resultList = mobileLayoutTestService(pages, null);
                mobileLayoutRepository.saveAll(resultList);
                res.put("action", Constant.SUCCESS);
                res.put("mobileLayoutTestReport", resultList);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> getLastestDataMobileLayout(RequestCommonPOJO request) {
        Map<String, Object> res = new HashMap<>();
        Website website = authenticate.isAuthGetSingleSite(request);
        if (website != null) {
            PageOption pageOption = pageOptionRepository.findOneByIdAndWebsiteAndDelFlagEquals(request.getPageOptionId(), website, false);
            if (pageOption == null) {
                request.setPageOptionId((long) -1);
            }
            if (request.getPageOptionId() != -1) {
                MobileLayoutReport mobileLayoutReport = mobileLayoutRepository.findFirstByPageOptionAndDelFlagEqualsOrderByCreatedTimeDesc(pageOption, false);
               if(mobileLayoutReport != null){
                   Date lastedCreatedTime = mobileLayoutReport.getCreatedTime();
                   List<MobileLayoutReport> resultList = mobileLayoutRepository.findAllByPageOptionAndCreatedTime(pageOption, lastedCreatedTime);

                   res.put("mobileLayoutTestReport", resultList);
                   res.put("action", Constant.SUCCESS);
                   return res;
               }else {
                   List<MobileLayoutReport> resultList = mobileLayoutRepository.findAllByPageOptionAndUrl(null, website.getUrl());
                   res.put("mobileLayoutTestReport", resultList);
                   res.put("action", Constant.SUCCESS);
                   return res;
               }

            } else {
                List<MobileLayoutReport> resultList = mobileLayoutRepository.findAllByPageOptionAndUrl(null, website.getUrl());
                res.put("mobileLayoutTestReport", resultList);
                res.put("action", Constant.SUCCESS);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }

    }

    @Override
    public Map<String, Object> saveMobileReport(RequestReportPOJO request) {
        Map<String, Object> res = new HashMap<>();
        RequestCommonPOJO requestCommon = new RequestCommonPOJO();
        requestCommon.setPageOptionId(request.getPageOptionId());
        requestCommon.setUserId(request.getUserId());
        requestCommon.setWebsiteId(request.getWebsiteId());
        requestCommon.setUserToken(request.getUserToken());
        WebsiteUserPOJO userWebsite = authenticate.isAuthGetUserAndWebsite(requestCommon);
        if (userWebsite != null) {
            List<MobileLayoutReport> listReport = new ArrayList<>();
            for (int i = 0; i < request.getListReportId().size(); i++) {
                Optional<MobileLayoutReport> optionalReport = mobileLayoutRepository.findById(request.getListReportId().get(i));
                if (optionalReport.isPresent()) {
                    MobileLayoutReport report = optionalReport.get();
                    report.setDelFlag(false);
                    listReport.add(report);
                }
            }
            List<MobileLayoutReport> results = mobileLayoutRepository.saveAll(listReport);
            if (results.size() != 0) {
                res.put("action", Constant.SUCCESS);
                res.put("mobileLayoutTestReport", results);
                return res;
            } else {
                res.put("action", Constant.INCORRECT);
                return res;
            }
        } else {
            res.put("action", Constant.INCORRECT);
            return res;
        }
    }

    @Override
    public Map<String, Object> getHistoryMobileLayoutTestReport(RequestCommonPOJO request) {
        Map<String, Object> res = new HashMap<>();
        Date time = new Date(request.getReportTime());
        List<MobileLayoutReport> mobileLayoutReports = mobileLayoutRepository.findALlByCreatedTime(time);
        res.put("action",Constant.SUCCESS);
        res.put("data",mobileLayoutReports);
        return res;
    }

    @Override
    public Map<String, Object> getHistoryMobileLayoutTestList(RequestCommonPOJO request) {
        List<MobileLayoutReport> list = mobileLayoutRepository.findAllGroupByCreatedTimeAndPageOption(request.getUserId(), request.getPageOptionId());
        Map<String, Object> res = new HashMap<>();
        List<Long> dates = new ArrayList<>();
        for(int i = 0 ; i< list.size();i++)
        {
            dates.add(list.get(i).getCreatedTime().getTime());
        }
        res.put("action",Constant.SUCCESS);
        res.put("data",dates);
        return res;
    }




    public List<MobileLayoutReport> mobileLayoutTestService(List<Page> list, PageOption option) throws InterruptedException {
        final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", Constant.CLOUDARY_NAME, "api_key", Constant.CLOUDARY_API_KEY, "api_secret", Constant.CLOUDARY_API_SECRET));
        Date createdTime = new Date();
        System.setProperty("webdriver.chrome.driver", Constant.CHROME_DRIVER);
        List<MobileLayoutReport> resultList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Constant.MAX_THREAD);
        for (Page p : list) {
            executor.submit(new Runnable() {
                @Override
                    public void run() {
                    try {
                        String url = p.getUrl();
                        int codeRespone = CheckHTTPResponse.verifyHttpMessage(url);
                        if(codeRespone<400) {

                            Map<String, String> mobileEmulation = new HashMap<>();
                            mobileEmulation.put("deviceName", "Nexus 5");
                            ChromeOptions chromeOptions = new ChromeOptions();
                            chromeOptions.addArguments("--headless");
                            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
                            WebDriver driver = new ChromeDriver(chromeOptions);

                            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                            StopWatch stopWatch = new StopWatch();
                            stopWatch.start();
                            String mainDomain = null;
                            if (p.getUrl().contains("http://www.") || p.getUrl().contains("https://www.")) {
                                mainDomain = p.getUrl().split("www.")[1];
                            } else if (p.getUrl().contains("http://")) {
                                mainDomain = p.getUrl().split("http://")[1];
                            } else if (p.getUrl().contains("https://")) {
                                mainDomain = p.getUrl().split("https://")[1];

                            }
                            driver.get(p.getUrl());
                            String title = driver.getTitle();
                            boolean isSupport = false;
                            if (driver.getCurrentUrl().contains("m." + mainDomain)) {
                                isSupport = true;
                            }
                            //TakesScreenshot
                            TakesScreenshot screenshot = (TakesScreenshot) driver;
                            File source = screenshot.getScreenshotAs(OutputType.FILE);
                            FileUtils.copyFile(source, new File("./" + list.indexOf(p) + ".png"));
                            File file = new File("./" + list.indexOf(p) + ".png");
                            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
                            String screenShot = uploadResult.get("url").toString();
                            screenShot = screenShot.split("/upload/")[0] + "/upload/w_350/" + screenShot.split("/upload/")[1];
                            //user.out.println(uploadResult.get("url"));
                            file.delete();

                            if (isSupport == false) {
                                java.util.List<WebElement> manifest = driver.findElements(By.cssSelector("link[rel='manifest'][href*='.json']"));
                                System.out.println("size manifest : " + manifest.size());
                                String valueDisplay = null;
                                if (manifest.size() != 0) {
//                                user.out.println("manifest" + manifest.size());
//                                user.out.println("manifest" + manifest.get(0).getAttribute("href"));
                                    JSONObject json = new JSONObject(IOUtils.toString(new URL("" + manifest.get(0).getAttribute("href") + ""), Charset.forName("UTF-8")));

                                    if (json.keySet().contains("display") == true) {
                                        valueDisplay = json.getString("display");
                                    }
                                    if (valueDisplay == "standalone") {
                                        isSupport = true;
                                    }

                                }
                            }


                            if (isSupport == false) {
                                java.util.List<WebElement> media = driver.findElements(By.cssSelector("link[rel='stylesheet']"));
                                List<String> listFramework = Arrays.asList("/bootstrap.min.css", "/pure-min.css", "/w3.css", "/semantic.min.css");
                                if (media.size() != 0) {
                                    for (int i = 0; i < media.size(); i++) {
                                        for (int j = 0; j < listFramework.size(); j++) {
                                            if (media.get(i).getAttribute("href").contains(listFramework.get(j))) {
                                                isSupport = true;
                                            }
                                        }

                                    }
                                }

                            }


                            java.util.List<WebElement> links1 = driver.findElements(By.cssSelector("meta[name='viewport'][content*='width=device-width'][content*='initial-scale=1']"));
                            System.out.println("size viewport " + links1.size());


                            //<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                            //Get view port css content

                            String content = null;
                            if (links1.size() != 0) {
                                isSupport = true;
                                content = links1.get(0).getAttribute("content");
                                System.out.println("content : " + content);
                            }

                            //Get overflow css of html tag
                            WebElement htmlElememt = driver.findElement(By.tagName("html"));
                            String htmlOverflowXValue = htmlElememt.getCssValue("overflow-x");
                            String htmlOverflowValue = htmlElememt.getCssValue("overflow");
                            System.out.println("htmlx : " + htmlOverflowXValue);
                            System.out.println("html : " + htmlOverflowValue);

                            //Get overflow css of body tag
                            WebElement bodyElement = driver.findElement(By.tagName("body"));
                            String bodyOverflowXValue = bodyElement.getCssValue("overflow-x");
                            String bodyOverflowValue = bodyElement.getCssValue("overflow");
                            System.out.println("bodyx : " + bodyOverflowXValue);
                            System.out.println("body : " + bodyOverflowValue);


                            //Check support mobile
                            String issue = "";
                            if (isSupport == false) {
                                //user.out.println("That page is not Optimize for Mobile!");
                                issue = "Not Optimize for Mobile!,";
                                MobileLayoutReport mobileLayoutReport = new MobileLayoutReport(p.getUrl(), title, screenShot, issue);
                                mobileLayoutReport.setPageOption(option);
                                mobileLayoutReport.setCreatedTime(createdTime);
                                resultList.add(mobileLayoutReport);

                            } else {


                                //Check pinch to zoom
                                if (content != null) {
                                    if (content.contains("user-scalable=no") || content.contains("user-scalable=0")
                                            || (content.contains("minimum-scale=1") && content.contains("maximum-scale=1"))) {
                                        // user.out.println("That page not support pinch to zoom!");
                                        issue = issue + "Not support pinch to zoom,";
                                    }
                                }


                                //Check The page does not scroll horizontally
                                if (htmlOverflowXValue.matches("scroll|hidden") || htmlOverflowValue.equals("scroll|hidden")) {
                                    if (bodyOverflowXValue.equals("hidden") || bodyOverflowValue.equals("hidden")) {
                                        //user.out.println("That page not support scroll horizontally!");
                                        issue = issue + "Not support scroll horizontally,";
                                    }

                                }

                                //Test big enough to press with a finger
                                java.util.List<WebElement> listLink = driver.findElements(By.tagName("a"));
                                int link = 0;
                                int linkBigEnough = 0;
                                for (int i = 0; i < listLink.size() && listLink.size() != 0; i++) {
                                    if (listLink.get(i).getSize().getHeight() != 0 && listLink.get(i).getSize().getHeight() != 0) {
                                        link = link + 1;
                                        if (listLink.get(i).getSize().getWidth() >= 44 && listLink.get(i).getSize().getHeight() >= 44) {
                                            linkBigEnough = linkBigEnough + 1;
                                        }
                                    }
                                }
                                System.out.println("link size : " + link);
                                System.out.println("link enough size : " + linkBigEnough);
                                if (link != 0) {
                                    float a = (float) linkBigEnough / link;
                                    if (a < 0.9) {
                                        //user.out.println("Links too small!");
                                        issue = issue + "Links too small,";
                                    }
                                }


                                java.util.List<WebElement> listButton = driver.findElements(By.tagName("button"));
                                int button = 0;
                                int buttonBigEnough = 0;
                                for (int i = 0; i < listButton.size() && listButton.size() != 0; i++) {
                                    if (listButton.get(i).getSize().getHeight() != 0 && listButton.get(i).getSize().getHeight() != 0) {
                                        button = button + 1;
                                        if (listButton.get(i).getSize().getWidth() >= 44 && listButton.get(i).getSize().getHeight() >= 44) {
                                            buttonBigEnough = buttonBigEnough + 1;
                                        }
                                    }
                                }
                                System.out.println("button size : " + button);
                                System.out.println("button enough size : " + buttonBigEnough);
                                if (button != 0) {
                                    float a = (float) buttonBigEnough / button;
                                    if (a < 0.9) {
                                        // user.out.println("Buttons too small!");
                                        issue = issue + "Buttons too small,";
                                    }
                                }

                                int bodyTextLength = driver.findElement(By.tagName("body")).getText().replaceAll("\\s+", " ").length();

                                //user.out.println("p length : " + listP.size());
                                boolean checkSize = false;
                                int lengthTotal = 0;
                                List<String> listTag = Arrays.asList("p", "span", "h4", "div", "h5", "h6", "td", "li");
                                for (int i = 0; i < listTag.size(); i++) {
                                    if (checkSize == false) {
                                        java.util.List<WebElement> elementListByTag = driver.findElements(By.tagName(listTag.get(i)));
                                        if (elementListByTag.size() != 0) {
                                            lengthTotal = 0;
                                            for (int j = 0; j < elementListByTag.size(); j++) {
                                                if (elementListByTag.get(j).getText().length() != 0) {
                                                    float length = Float.parseFloat(elementListByTag.get(j).getCssValue("font-size").split("px")[0]);
                                                    if (length < 16)
                                                        lengthTotal += elementListByTag.get(j).getText().length();
                                                }

                                            }
                                            float a = (float) lengthTotal / bodyTextLength;
                                            if (a > 0.1) {
                                                checkSize = true;
                                                issue = issue + "Text too small,";
                                            }
                                        }

                                    }


                                }

                                if (issue == null) {
                                    issue = "The Page is optimized for the phone,";
                                }
                                MobileLayoutReport mobileLayoutReport = new MobileLayoutReport(p.getUrl(), title, screenShot, issue);
                                mobileLayoutReport.setPageOption(option);
                                mobileLayoutReport.setCreatedTime(createdTime);
                                resultList.add(mobileLayoutReport);


                            }


                            driver.quit();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                });
            }


        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE,TimeUnit.MILLISECONDS);
        return resultList;
    }

}
