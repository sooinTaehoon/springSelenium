package com.springselenium.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.awt.desktop.SystemSleepEvent;
import java.util.List;

@Service
public class SpringSeleniumService {
    private WebDriver driver;

    public SpringSeleniumService() {

        System.setProperty("webdriver.chrome.driver", "/Users/admin/chromedriver"); // 크롬 드라이버 경로 설정
    }
    public String mainFunction(String url) throws JSONException {

        ChromeOptions chromeOptions = new ChromeOptions();

        chromeOptions.addArguments("--remote-allow-origins=*"); // 외장 톰캣 사용 시 드라이버 실행 가능하게 하는 옵션

        driver = new ChromeDriver(chromeOptions);

        String productInfo = getProductInfo(url);

        return productInfo;
    }

    public String getProductInfo(String url) throws JSONException {

        String titleText;
        WebElement reviewButtonElement;
        StringBuilder state = new StringBuilder();

        JSONObject resultObject = new JSONObject();
        JSONArray reviewArray = new JSONArray();


        try {
            driver.get("https://smartstore.naver.com/kwacoal/products/" + url);

            Thread.sleep(2000); // 페이지 로딩 대기 시간

            titleText = driver.findElement(By.className("_22kNQuEXmb")).getText(); // 제목 엘리먼트

            reviewButtonElement = driver.findElement(By.className("N=a:tab.review")); // 리뷰 탭 지정

            reviewButtonElement.click();

            Thread.sleep(2000);

            driver.findElement(By.className("N=a:rvs.srecent")).click(); // 최신순 설정

            List<WebElement> pages = driver.findElements(By.className("N=a:rvs.page"));

            for (int i = 0; i < pages.size(); i++) {

                Thread.sleep(1000);

                List<WebElement> elements = driver.findElements(By.className("BnwL_cs1av"));

                for (WebElement element : elements) {

                    JSONObject reviewObject = new JSONObject();

                    reviewObject.put("title", titleText);
                    reviewObject.put("id", element.findElements(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9")).get(0).getText());
                    reviewObject.put("grade", element.findElement(By.className("_15NU42F3kT")).getText());
                    reviewObject.put("date", element.findElements(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9")).get(1).getText());
                    reviewObject.put("shoppingList", element.findElement(By.cssSelector("._3HKlxxt8Ii ._2FXNMst_ak")).getText().split("\n")[0]);

                    List<WebElement> bodyStates = element.findElements(By.cssSelector("._3F8sJXhFeW ._2L3vDiadT9"));

                    state.append("체형: ");

                    if (bodyStates.size() != 0) {

                        for (int j = 0; j < bodyStates.size(); j++) {

                            state.append(bodyStates.get(j).getText() + ", ");
                        }
                    }

                    List<WebElement> evaluations = element.findElements(By.className("_1QLwBCINAr"));

                    for (int j = 0; j < evaluations.size(); j++) {

                        state.append(evaluations.get(j).findElement(By.className("CCKYhxjMDd")).getText() + ": ");
                        state.append(evaluations.get(j).findElement(By.className("_3y5bSL-H_P")).getText());

                        if (evaluations.size() - 1 != j) state.append(", ");
                    }

                    reviewObject.put("state", state.toString());

                    state.setLength(0);

                    reviewObject.put("text ", element.findElement(By.cssSelector("._1kMfD5ErZ6 ._2L3vDiadT9")).getText());

                    reviewArray.put(reviewObject);
                }
                if (i != pages.size() - 1) pages.get(i + 1).click();
            }

            resultObject.put("resultMessage", "성공");
            resultObject.put("resultCode", "true");
            resultObject.put("dataList", reviewArray);
        } catch (Exception e) {

            resultObject.put("resultMessage", e.getMessage());
            resultObject.put("resultCode", "false");
        } finally {

            driver.quit();
        }

        return resultObject.toString();
    }

//    private String getReviews(List<WebElement> elements) {
//
//    }
}
