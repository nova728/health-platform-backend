package com.health.healthplatform.DTO.DietRecord;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodSearchResponseDTO {
    @JsonProperty("foods_search")
    private FoodsSearch foodsSearch;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FoodsSearch {
        @JsonProperty("max_results")
        private String maxResults;

        @JsonProperty("total_results")
        private String totalResults;

        @JsonProperty("page_number")
        private String pageNumber;

        private Results results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Results {
        private List<Food> food;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Food {
        @JsonProperty("food_id")
        private String foodId;

        @JsonProperty("food_name")
        private String foodName;

        @JsonProperty("food_type")
        private String foodType;

        @JsonProperty("brand_name")
        private String brandName;

        private Servings servings;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Servings {
        private List<Serving> serving;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serving {
        @JsonProperty("serving_id")
        private String servingId;

        @JsonProperty("serving_description")
        private String servingDescription;

        @JsonProperty("metric_serving_amount")
        private Double metricServingAmount;

        @JsonProperty("metric_serving_unit")
        private String metricServingUnit;

        @JsonProperty("number_of_units")
        private Double numberOfUnits;

        @JsonProperty("measurement_description")
        private String measurementDescription;

        @JsonProperty("calories")
        private String calories;

        @JsonProperty("carbohydrate")
        private String carbohydrate;

        @JsonProperty("protein")
        private String protein;

        @JsonProperty("fat")
        private String fat;
    }
}