package com.lolcompanion.bg2ez.riot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "riot")
public record RiotProperties(String apiKey, String region) {

}