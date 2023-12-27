package com.springselenium.service;

import com.springselenium.config.dao.PostgresDBMapper;
import com.springselenium.domain.ReviewVO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service
public class SpringSeleniumService {
    private WebDriver driver;
    @Autowired
    PostgresDBMapper postgresDBMapper;

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

        String product_name;
        StringBuilder state = new StringBuilder();

        List<ReviewVO> reviewVOList = new ArrayList<>();

        JSONObject resultObject = new JSONObject();
        JSONArray reviewArray = new JSONArray();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        try {

            driver.get("https://smartstore.naver.com/kwacoal/products/" + url);

            try {

                Thread.sleep(500);

                if (driver.findElements(By.className("_22kNQuEXmb")).size() > 0) { // 제목 없을 시 상품 없음으로 취급

                    product_name = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("_22kNQuEXmb"))).getText(); // 제목 엘리먼트

                    resultObject.put("product_name", product_name);

                    wait.until(ExpectedConditions.elementToBeClickable(By.className("N=a:tab.review"))).click(); // 리뷰 탭 지정

                    /* 리뷰 가져오기 */
                    Long reviewCount = Long.parseLong(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("_3HJHJjSrNK"))).getText());

                    resultObject.put("count", reviewCount);

                    if (reviewCount != 0) {

                        wait.until(ExpectedConditions.elementToBeClickable(By.className("N=a:rvs.srecent"))).click(); // 최신순 설정

                        Thread.sleep(500);

                        ReviewVO recentReview = postgresDBMapper.getRecentReview(url);
                        int recentReviewDate = Integer.parseInt(recentReview == null ? "0" : recentReview.getDate());

                        boolean quitFlag = false;

                        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                        int today = Integer.parseInt(sdf.format(new Date()));

                        int imgCount = 0;

                        Long pages = reviewCount % 20 == 0 ? reviewCount / 20 : reviewCount / 20 + 1;

                        for (int i = 0; i < pages; i++) { // 페이지 돌기

                            Thread.sleep(500);

                            List<WebElement> elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("BnwL_cs1av"))); // 리뷰 리스트 설정

                            for (WebElement element : elements) { // 리뷰 획득

                                int elementDate = Integer.parseInt(element.findElements(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9")).get(1).getText().toString().replaceAll("[^0-9]", ""));

                                if (elementDate == today) continue; // 전날 까지의 리뷰만 획득

                                if (elementDate <= recentReviewDate) { // elementDate를 recentReviewDate와 비교해 같거나 작으면 break

                                    quitFlag = true;
                                    break;
                                }

                                ReviewVO reviewVO = new ReviewVO();

                                JSONObject reviewObject = new JSONObject();
                                reviewObject.put("user_id", element.findElements(By.cssSelector(".iWGqB6S4Lq ._2L3vDiadT9")).get(0).getText());
                                reviewObject.put("grade", element.findElement(By.className("_15NU42F3kT")).getText());
                                reviewObject.put("date", elementDate);
                                reviewObject.put("shopping_list", element.findElement(By.cssSelector("._3HKlxxt8Ii ._2FXNMst_ak")).getText().split("\n")[0]);

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

                                reviewObject.put("text", element.findElement(By.className("_1kMfD5ErZ6")).getText());

                                reviewObject.put("imgResult", "false");

                                reviewArray.put(reviewObject);

                                /* DB에 담을 reviewVO 설정 */
                                reviewVO.setProduct_name(product_name);
                                reviewVO.setUser_id(reviewObject.get("user_id").toString());
                                reviewVO.setGrade(Integer.parseInt(reviewObject.get("grade").toString()));
                                reviewVO.setDate(reviewObject.get("date").toString().replaceAll("[^0-9]", ""));
                                reviewVO.setShopping_list(reviewObject.get("shopping_list").toString());
                                reviewVO.setState(reviewObject.get("state").toString());
                                reviewVO.setText(reviewObject.get("text").toString());
                                reviewVO.setSite("naver");
                                reviewVO.setProduct_id(url);

                                reviewVOList.add(reviewVO);

                                // 이미지 개수 세기
                                if (element.findElements(By.className("_3Bbv1ae9fg")).size() > 0) imgCount++;
                            }

                            if (quitFlag) break; // 종료 플래그가 트루면 break

                            WebElement next = driver.findElement(By.className("_2Ar8-aEUTq"));

                            if (next.getAttribute("aria-hidden").equals("false")) next.click();
                        }

                        /* 이미지 가져오기 */
                        if (driver.findElements(By.className("_3z_fNGjkmL")).size() != 0) { // 이미지 리뷰가 있다면

                            Thread.sleep(1000);

                            wait.until(ExpectedConditions.elementToBeClickable(By.className("N=a:rvs.pimg"))).click(); // 전체 이미지 보기

                            for (int i = 0; i < imgCount; i++) { // 포토 리뷰수만큼 이미지 주소 저장

                                Thread.sleep(1000);

                                JSONArray urlList = new JSONArray();

                                if (driver.findElements(By.className("_3UgWrDcnSs")).size() == 0) { // 동영상 아닐때만

                                    urlList.put(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("uAIeokuHC5"))).getAttribute("src"));

                                    if (driver.findElements(By.className("_3ynDMIGV2Y")).size() != 0) { // 이미지가 여러장일때

                                        List<WebElement> imgList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("_1DNLhGBSw9")));

                                        // 각 이미지 클릭하며 저장
                                        for (int j = 1; j < imgList.size(); j++) {

                                            imgList.get(j).click();

                                            urlList.put(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("uAIeokuHC5"))).getAttribute("src"));
                                        }
                                    }
                                }

                                String photoReviewText = driver.findElement(By.className("_1YWdqMv4aF")).getAttribute("textContent").toString();

                                // 이미지 리뷰 내용과 저장했던 리뷰 내용이 동일하면 오브젝트에 이미지 리뷰 삽입
                                for (int j = 0; j < reviewArray.length(); j++) {

                                    if (reviewArray.getJSONObject(j).get("text").toString().equals(photoReviewText) &&
                                            driver.findElements(By.className("_3UgWrDcnSs")).size() == 0) {

                                        reviewArray.getJSONObject(j).put("img", urlList);
                                        reviewArray.getJSONObject(j).put("img_count", urlList.length());
                                        reviewArray.getJSONObject(j).put("imgResult", "true");

                                        reviewVOList.get(j).setImg(urlList.toString());
                                        reviewVOList.get(j).setImg_count(urlList.length());

                                        break;
                                    }
                                }

                                // 다음 페이지 이동
                                List<WebElement> next = driver.findElements(By.className("N=a:rvl.next"));

                                if (next.size() != 0) next.get(0).click();
                            }
                        }
                    }

                    resultObject.put("resultMessage", "성공");
                    resultObject.put("resultCode", "true");
                    resultObject.put("dataList", reviewArray);

                    if (reviewVOList.size() > 0) {

                        try {

                            Collections.reverse(reviewVOList);

                            postgresDBMapper.setReviewList(reviewVOList);
                        } catch (Exception e) {

                            System.out.println("DB 저장 실패\n" + e.getMessage());
                        }
                    }
                } else {

                    resultObject.put("resultMessage", "상품이 존재하지 않습니다");
                    resultObject.put("resultCode", "false");
                }
            } catch (NoSuchElementException | NoSuchWindowException | NoSuchFrameException | NoAlertPresentException e) {

                resultObject.put("resultMessage", "요소 없음, " + e.getMessage());
                resultObject.put("resultCode", "false");
            } catch (TimeoutException e) {

                resultObject.put("resultMessage", "시간 초과");
                resultObject.put("resultCode", "false");
            } catch (Exception e) {

                resultObject.put("resultMessage", e.getMessage());
                resultObject.put("resultCode", "false");
            }
        } catch (Exception e) {

            resultObject.put("resultMessage", "페이지 접속 에러");
            resultObject.put("resultCode", "false");
        } finally {

            driver.quit();
        }

        return resultObject.toString();
    }
}
