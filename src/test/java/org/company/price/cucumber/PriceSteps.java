package org.company.price.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceSteps {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    DatabaseClient databaseClient;

    @Autowired
    WebTestClient webTestClient;

    WebTestClient.ResponseSpec responseSpec;

    Integer productId;
    Integer brandId;
    String applicationDate;

    @Given("the PRICES table is reset with sample data")
    public void resetPricesTable() {

        databaseClient.sql("DELETE FROM PRICES")
                .fetch()
                .rowsUpdated()
                .block();

        insertPrice(1, "2020-06-14T00:00:00", "2020-12-31T23:59:59", 1, 35455, 0, 35.50)
                .block();
        insertPrice(1, "2020-06-14T15:00:00", "2020-06-14T18:30:00", 2, 35455, 1, 25.45)
                .block();
        insertPrice(1, "2020-06-15T00:00:00", "2020-06-15T11:00:00", 3, 35455, 1, 30.50)
                .block();
        insertPrice(1, "2020-06-15T16:00:00", "2020-12-31T23:59:59", 4, 35455, 1, 38.95)
                .block();
    }

    private Mono<Long> insertPrice(int brandId, String startDateStr, String endDateStr,
                                   int priceList, int productId, int priority, double price) {


        var startDate = LocalDateTime.parse(startDateStr, FORMATTER);
        var endDate = LocalDateTime.parse(endDateStr, FORMATTER);

        var sql = "INSERT INTO PRICES (brand_id, start_date, end_date, price_list, product_id, priority, price, curr) " +
                "VALUES (:brandId, :startDate, :endDate, :priceList, :productId, :priority, :price, 'EUR')";

        return databaseClient.sql(sql)
                .bind("brandId", brandId)
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .bind("priceList", priceList)
                .bind("productId", productId)
                .bind("priority", priority)
                .bind("price", price)
                .fetch()
                .rowsUpdated();
    }


    @Given("the product with id {int} and brand {int}")
    public void givenProductAndBrand(int prodId, int brId) {
        this.productId = prodId;
        this.brandId = brId;
    }

    @When("I request the price at {string}")
    public void requestPrice(String dateStr) {
        this.applicationDate = dateStr;

        responseSpec = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/prices")
                        .queryParam("productId", productId)
                        .queryParam("brandId", brandId)
                        .queryParam("applicationDate", applicationDate)
                        .build())
                .exchange();
    }

    @Then("the response contains price {double}")
    public void validateResponse(double expectedPrice) {
        responseSpec.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.brandId").isEqualTo(brandId)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.price").isEqualTo(expectedPrice)
                .jsonPath("$.currency").isEqualTo("EUR");
    }
}