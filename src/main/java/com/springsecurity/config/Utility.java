package com.springsecurity.config;

import jakarta.servlet.http.HttpServletRequest;

public class Utility {
    public static String getSiteURL(HttpServletRequest httpServletRequest) {
        String siteURL = httpServletRequest.getRequestURL().toString();

        siteURL = siteURL.replace(httpServletRequest.getServletPath(), "");
        return siteURL;
    }
}
