package com.atminterface.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atm")
public record AppProperties(int withdrawMultiple) {
}
