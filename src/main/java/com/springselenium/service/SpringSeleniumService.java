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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.awt.desktop.SystemSleepEvent;
import java.time.Duration;
import java.util.ArrayList;
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

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            driver.get("https://smartstore.naver.com/kwacoal/products/" + url);

            titleText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("_22kNQuEXmb"))).getText(); // 제목 엘리먼트

            wait.until(ExpectedConditions.elementToBeClickable(By.className("N=a:tab.review"))).click(); // 리뷰 탭 지정

            wait.until(ExpectedConditions.elementToBeClickable(By.className("N=a:rvs.srecent"))).click(); // 최신순 설정

            /* 리뷰 가져오기 */
            int reviewCount = Integer.parseInt(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("_9Fgp3X8HT7"))).getText());

            int pages = reviewCount % 20 == 0 ? reviewCount / 20 : reviewCount / 20 + 1;

            for (int i = 0; i < pages; i++) { // 페이지 돌기

                Thread.sleep(2000);

                List<WebElement> elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("BnwL_cs1av")));


                for (WebElement element : elements) { // 리뷰 획득

                    JSONObject reviewObject = new JSONObject();

                    reviewObject.put("title", titleText);
                    reviewObject.put("id", element.findElements(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9")).get(0).getText());
                    reviewObject.put("grade", element.findElement(By.className("_15NU42F3kT")).getText());
                    reviewObject.put("date", element.findElements(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9")).get(1).getText());
                    reviewObject.put("shoppingList", element.findElement(By.cssSelector("._3HKlxxt8Ii ._2FXNMst_ak")).getText().split("\n")[0]);

                    List<WebElement> bodyStates = element.findElements(By.cssSelector("._3F8sJXhFeW ._2L3vDiadT9"));
//                    reviewObject.put("id", wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9"))).get(0).getText());
//                    reviewObject.put("grade", wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("_15NU42F3kT"))).getText());
//                    reviewObject.put("date", wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9"))).get(1).getText());
//                    reviewObject.put("shoppingList", wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("._3HKlxxt8Ii ._2FXNMst_ak"))).getText().split("\n")[0]);
//
//                    List<WebElement> bodyStates = element.findElements(By.cssSelector("._3F8sJXhFeW ._2L3vDiadT9"));

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

                    reviewObject.put("text", element.findElement(By.className("_1kMfD5ErZ6")).getText());

                    reviewObject.put("imgResult", "false");

                    reviewArray.put(reviewObject);
                }

                WebElement next = driver.findElement(By.className("_2Ar8-aEUTq"));

                if (next.getAttribute("aria-hidden").equals("false")) next.click();
            }

            /* 이미지 가져오기 */
            int totalPhotoReview = Integer.parseInt(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("_3z_fNGjkmL"))).getText());

            wait.until(ExpectedConditions.elementToBeClickable(By.className("N=a:rvs.pimg"))).click(); // 전체 이미지 보기

            for (int i = 0; i < totalPhotoReview; i++) { // 포토 리뷰수만큼 이미지 주소 저장

                Thread.sleep(1000);

                JSONArray urlList = new JSONArray();
                JSONObject imgObject = new JSONObject();

                if (driver.findElements(By.className("_3UgWrDcnSs")).size() == 0) { // 동영상 아닐때만

                    imgObject.put("img00", wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("uAIeokuHC5"))).getAttribute("src"));

                    if (driver.findElements(By.className("_3ynDMIGV2Y")).size() != 0) { // 이미지가 여러장일때

                        List<WebElement> imgList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("_1DNLhGBSw9")));

                        // 각 이미지 클릭하며 저장
                        for (int j = 1; j < imgList.size(); j++) {
                            imgList.get(j).click();

                            Thread.sleep(100);

                            imgObject.put("img0" + j, wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("uAIeokuHC5"))).getAttribute("src"));
                        }
                    }
                }

                urlList.put(imgObject);

                String photoReviewText = driver.findElement(By.className("_1YWdqMv4aF")).getAttribute("textContent").toString();

                // 이미지 리뷰 내용과 저장했던 리뷰 내용이 동일하면 오브젝트에 이미지 리뷰 삽입
                for (int j = 0; j < reviewArray.length(); j++) {

                    if (reviewArray.getJSONObject(j).get("text").toString().equals(photoReviewText) &&
                            driver.findElements(By.className("_3UgWrDcnSs")).size() == 0) {

                        reviewArray.getJSONObject(j).put("img", urlList);
                        reviewArray.getJSONObject(j).put("imgResult", "true");

                        break;
                    }
                }

                // 다음 페이지 이동
                List<WebElement> next = driver.findElements(By.className("N=a:rvl.next"));

                if (next.size() != 0) next.get(0).click();
            }

//            resultObject.put("", );
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
}
