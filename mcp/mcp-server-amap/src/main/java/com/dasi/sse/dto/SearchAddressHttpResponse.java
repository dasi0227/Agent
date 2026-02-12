package com.dasi.sse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchAddressHttpResponse {

    private String status;
    private String info;
    private String infocode;
    private String count;
    private List<Geocode> geocodes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geocode {
        private String formatted_address;
        private String country;
        private String province;
        private String city;
        private String adcode;
        private String location;
    }
}
