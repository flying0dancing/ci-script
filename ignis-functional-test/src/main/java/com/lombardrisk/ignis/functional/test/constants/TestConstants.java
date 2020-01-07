package com.lombardrisk.ignis.functional.test.constants;

import lombok.experimental.UtilityClass;

@SuppressWarnings({ "squid:S2386" })
@UtilityClass
public class TestConstants {

    // CIL
    public static final String CIL_PRODUCT_CONFIG = "src/main/resources/products/CIL";

    public static final String WHOLESALE_CORPORATE = "DEWHL001";
    static final String WHOLESALE_CORPORATE_SCHEMA = "WHOLESALE_CORPORATE";
    static final String WHOLESALE_CORPORATE_CSV = "FUN_TEST_DEWHL001.csv";

    public static final String REG_ADJUSTMENTS = "DEADJ001";
    static final String REG_ADJUSTMENTS_SCHEMA = "REG_ADJUSTMENTS";
    static final String REG_ADJUSTMENTS_CSV = "FUN_TEST_DEADJ001.csv";

    // ACCOUNTS
    public static final String ACCOUNTS_PRODUCT_CONFIG = "src/main/resources/products/accounts";
    public static final String ACCOUNTS = "ACC_T1";
    public static final String ACCOUNTS_SCHEMA = "ACCOUNTS_TEST_V1";
    public static final String INVALID_ACCOUNTS_CSV = "accounts_invalid.csv";

    // TRADES
    public static final String TRADES_PRODUCT_CONFIG = "src/main/resources/products/trades";
    public static final String TRADES = "TRD_DATA_S";
    public static final String TRADES_SCHEMA = "TRADES_DATA_SCHEDULES";

    // TRADES-PIPELINE
    public static final String TRADES_PIPELINE_PRODUCT_CONFIG = "src/main/resources/products/trades-pipeline";
    public static final String TRADES_PIPELINE_JOINED_DISPLAY_NAME = "Trades Joined from Quotes and Stocks Tables";
    public static final String TRADES_PIPELINE_OUT_DISPLAY = "TRADES OUTPUT";
    public static final String TRADES_PIPELINE_IN_PHYSICAL = "TRADES_JOINED";
    public static final String TRADES_PIPELINE_OUT_PHYSICAL = "TRADES_OUT";

    public static final String STOCKS_NAMES_DISPLAY = "Stock Names";
    public static final String STOCKS_NAMES_PHYSICAL = "STOCKS";

    public static final String QUOTES_NAMES_DISPLAY = "Stock Quotes";
    public static final String QUOTES_NAMES_PHYSICAL = "QUOTES";

    public static final String TRADES_RAW_INTERNAL_DISPLAY = "Raw Trades Data Internally Sourced";
    public static final String TRADES_RAW_INTERNAL_PHYSICAL = "TRADES_INTERNAL";

    public static final String TRADES_RAW_EXTERNAL_DISPLAY = "Raw Trades Data from another source";
    public static final String TRADES_RAW_EXTERNAL_PHYSICAL = "TRADES_EXTERNAL";

    public static final String TRADES_UNION_RAW_DISPLAY = "Raw Trades Data with ids and dates";
    public static final String TRADES_UNION_RAW_PHYSICAL = "TRADES_RAW";

    public static final String TRADES_JOINED_DISPLAY = "Trades Joined from Quotes and Stocks Tables";
    public static final String TRADES_JOINED_PHYSICAL = "TRADES_JOINED";

    public static final String TRADES_PIPELINE_AGGREGATED_DISPLAY = "TRADES AGGREGATED";
    public static final String TRADES_PIPELINE_AGGREGATED_PHYSICAL = "TRADES_AGGREGATED";

    public static final String TRADES_PIPELINE_WINDOWED_DISPLAY = "TRADES WINDOWED";
    public static final String TRADES_PIPELINE_WINDOWED_PHYSICAL = "TRADES_WINDOWED";

    public static final String TRADERS_DISPLAY = "Traders and trade dates";
    public static final String TRADERS_PHYSICAL = "TRADERS";

    public static final String TRADES_BY_TRADERS_DISPLAY = "Total trades by trader and stock";
    public static final String TRADES_BY_TRADERS_PHYSICAL = "TRADES_BY_TRADER";
}
