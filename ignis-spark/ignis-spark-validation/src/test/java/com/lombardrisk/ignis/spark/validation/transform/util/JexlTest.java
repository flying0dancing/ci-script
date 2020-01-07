package com.lombardrisk.ignis.spark.validation.transform.util;

import com.lombardrisk.ignis.common.jexl.JexlEngineFactory;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class JexlTest {

    private static final JexlEngine JEXL_ENGINE = JexlEngineFactory.jexlEngine().create();

    @Test
    public void isInRangeWithThreshold() {
        String exp = "var cashMarketSec = BPWHCP14 ?: 0; "
                + "var accountsRecCurr = BPWHCP48 ?: 0; "
                + "var inventoryCurr = BPWHCP49 ?: 0; "
                + "var assets = cashMarketSec + accountsRecCurr + inventoryCurr; "
                + "var assetsCurr = BPWHCP62 ?: 0; "
                + "  ( (assets - 1000)..(assets + 1000) ).contains( assetsCurr )";
        JexlScript jexlScript = JEXL_ENGINE.createScript(exp);

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("BPWHCP62", 1000),
                                Pair.of("BPWHCP14", 10),
                                Pair.of("BPWHCP48", 2),
                                Pair.of("BPWHCP49", 2))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("BPWHCP62", 1020),
                                Pair.of("BPWHCP14", 17),
                                Pair.of("BPWHCP48", 1),
                                Pair.of("BPWHCP49", 1))))
                .isFalse();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("BPWHCP62", null),
                                Pair.of("BPWHCP14", 17),
                                Pair.of("BPWHCP48", 1),
                                Pair.of("BPWHCP49", 101))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("BPWHCP62", 1000),
                                Pair.of("BPWHCP14", 17),
                                Pair.of("BPWHCP48", null),
                                Pair.of("BPWHCP49", null))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("BPWHCP62", 1000),
                                Pair.of("BPWHCP14", null),
                                Pair.of("BPWHCP48", 1),
                                Pair.of("BPWHCP49", 2))))
                .isTrue();
    }

    @Test
    public void isInRange() {
        String expression = "empty( item ) ? true : ( 0..6 ).contains( item )";
        JexlScript jexlScript = JEXL_ENGINE.createScript(expression);

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(Pair.of("item", null))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(Pair.of("item", 2))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(Pair.of("item", -32))))
                .isFalse();
    }

    @Test
    public void isAsciiWithoutCommaExpression() {
        String expression = "empty( ID ) ? true : not string:contains( ID, ',' ) && string:isAsciiPrintable( ID )";
        JexlScript jexlScript = JEXL_ENGINE.createScript(expression);

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(Pair.of(
                                "ID",
                                " !\"#$%&'()*+-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}"))))
                .isTrue();
        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(Pair.of("ID", "£"))))
                .isFalse();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(Pair.of("ID", "A,"))))
                .isFalse();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(Pair.of("ID", null))))
                .isTrue();
    }

    @Test
    public void isFirstDateGreaterThanSecondDate() {
        String expression = "empty( date1 ) || empty( date2 ) ? true : date1.after( date2 )";
        JexlScript jexlScript = JEXL_ENGINE.createScript(expression);

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("date1", toDateTime("2000-01-01T00:00:01")),
                                Pair.of("date2", toDateTime("2000-01-01T00:00:02")))))
                .isFalse();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("date1", toDateTime("2000-01-01T00:00:01")),
                                Pair.of("date2", toDateTime("2000-01-01T00:00:00")))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("date2", null),
                                Pair.of("date1", toDateTime("2000-01-01T00:00:00")))))
                .isTrue();
    }

    @Test
    public void isUsZipCode() {
        String expression = "country != 'US' || empty( zipCode ) ? true : zipCode =~ '^[0-9]{5}(?:-[0-9]{4})?$'";
        JexlScript jexlScript = JEXL_ENGINE.createScript(expression);

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("country", "US"),
                                Pair.of("zipCode", "99501"))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("country", "US"),
                                Pair.of("zipCode", "BT1 1TT"))))
                .isFalse();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("zipCode", "BT1 1TT"),
                                Pair.of("country", null))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("zipCode", null),
                                Pair.of("country", "US"))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("zipCode", "SS11SS"),
                                Pair.of("country", "DE"))))
                .isTrue();
    }

    @Test
    public void isBigDecimalWithPrecision() {
        String expression = "var bg; "
                + "if ( empty( index ) || empty( rate ) || index =~ [1, 2, 3])\n"
                + " true \n"
                + "else\n"
                + " number:isCreatable( rate )\n"
                + " && (bg = number:createBigDecimal( rate )).precision() == 5\n"
                + " && bg.scale() == 4";
        JexlScript jexlScript = JEXL_ENGINE.createScript(expression);

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("index", 11),
                                Pair.of("rate", "1.2345"))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("index", 2),
                                Pair.of("rate", "1.2345"))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("index", 4),
                                Pair.of("rate", "12.345"))))
                .isFalse();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("index", null),
                                Pair.of("rate", "5.6789"))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("index", 21),
                                Pair.of("rate", null))))
                .isTrue();

        assertThat(
                (Boolean) jexlScript.execute(
                        mapContext(
                                Pair.of("index", 5),
                                Pair.of("rate", "3.o122"))))
                .isFalse();
    }

    @SafeVarargs
    private static MapContext mapContext(final Pair<String, Object>... entries) {
        Map<String, Object> context = new HashMap<>();

        for (Pair<String, Object> entry : entries) {
            context.put(entry.getKey(), entry.getValue());
        }
        return new MapContext(context);
    }
}