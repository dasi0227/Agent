package com.dasi.sse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BochaWebSearchHttpResponse {

    @JsonProperty("_type")
    private String type;

    private QueryContext queryContext;

    private WebPages webPages;

    private Images images;

    private Videos videos;

    private RankingResponse rankingResponse;

    @JsonProperty("log_id")
    private String logId;

    private String code;

    private String message;

    private String msg;

    private BochaWebSearchHttpResponse data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QueryContext {
        private String originalQuery;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebPages {
        private String webSearchUrl;
        private Integer totalEstimatedMatches;
        private List<WebPageValue> value;
        private Boolean someResultsRemoved;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebPageValue {
        private String id;
        private String name;
        private String url;
        private String displayUrl;
        private String snippet;
        private String summary;
        private String siteName;
        private String siteIcon;
        private String datePublished;
        private String dateLastCrawled;
        private String cachedPageUrl;
        private String language;
        private Boolean isFamilyFriendly;
        private Boolean isNavigational;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Images {
        private String id;
        private String readLink;
        private String webSearchUrl;
        private Boolean isFamilyFriendly;
        private List<ImageValue> value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Videos {
        private String id;
        private String readLink;
        private String webSearchUrl;
        private Boolean isFamilyFriendly;
        private String scenario;
        private List<VideoValue> value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageValue {
        private String webSearchUrl;
        private String name;
        private String thumbnailUrl;
        private String datePublished;
        private String contentUrl;
        private String hostPageUrl;
        private String contentSize;
        private String encodingFormat;
        private String hostPageDisplayUrl;
        private Integer width;
        private Integer height;
        private Thumbnail thumbnail;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoValue {
        private String webSearchUrl;
        private String name;
        private String description;
        private String thumbnailUrl;
        private List<Publisher> publisher;
        private Creator creator;
        private String contentUrl;
        private String hostPageUrl;
        private String encodingFormat;
        private String hostPageDisplayUrl;
        private Integer width;
        private Integer height;
        private String duration;
        private String motionThumbnailUrl;
        private String embedHtml;
        private Boolean allowHttpsEmbed;
        private Integer viewCount;
        private Thumbnail thumbnail;
        private Boolean allowMobileEmbed;
        private Boolean isSuperfresh;
        private String datePublished;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Creator {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Publisher {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnail {
        private Integer height;
        private Integer width;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RankingResponse {
        private Mainline mainline;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mainline {
        private List<MainlineItem> items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainlineItem {
        private String answerType;
        private MainlineItemValue value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainlineItemValue {
        private String id;
    }

}
