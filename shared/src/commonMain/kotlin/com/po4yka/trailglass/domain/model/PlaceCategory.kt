package com.po4yka.trailglass.domain.model

/**
 * Categories for place visits.
 */
enum class PlaceCategory {
    /**
     * Home location.
     */
    HOME,

    /**
     * Work/office location.
     */
    WORK,

    /**
     * Restaurant, cafe, or food-related place.
     */
    FOOD,

    /**
     * Shop, mall, or retail location.
     */
    SHOPPING,

    /**
     * Gym, park, sports facility.
     */
    FITNESS,

    /**
     * Entertainment venue (cinema, theater, etc.).
     */
    ENTERTAINMENT,

    /**
     * Travel-related (hotel, airport, train station).
     */
    TRAVEL,

    /**
     * Healthcare facility.
     */
    HEALTHCARE,

    /**
     * Educational institution.
     */
    EDUCATION,

    /**
     * Religious place.
     */
    RELIGIOUS,

    /**
     * Friend or family home.
     */
    SOCIAL,

    /**
     * Outdoor location (park, nature, etc.).
     */
    OUTDOOR,

    /**
     * Service (bank, post office, etc.).
     */
    SERVICE,

    /**
     * Uncategorized or unknown.
     */
    OTHER
}

/**
 * Confidence level for place categorization.
 */
enum class CategoryConfidence {
    /** High confidence (>80%) - based on user labeling or strong POI data */
    HIGH,

    /** Medium confidence (50-80%) - based on visit patterns or weak POI data */
    MEDIUM,

    /** Low confidence (<50%) - inferred from limited data */
    LOW
}

/**
 * Significance of a place based on visit frequency and duration.
 */
enum class PlaceSignificance {
    /** Very frequent visits (e.g., home, work) */
    PRIMARY,

    /** Regular visits (weekly or more) */
    FREQUENT,

    /** Occasional visits (monthly) */
    OCCASIONAL,

    /** Rare visit or one-time */
    RARE
}
