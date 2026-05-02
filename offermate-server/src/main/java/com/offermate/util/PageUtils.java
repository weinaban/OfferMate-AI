package com.offermate.util;

public class PageUtils {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private PageUtils() {
    }

    public static int page(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    public static int pageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public static String jobSearchSort(String sort) {
        if ("latest".equals(sort) || "salary".equals(sort) || "relevance".equals(sort)) {
            return sort;
        }
        return "relevance";
    }
}
