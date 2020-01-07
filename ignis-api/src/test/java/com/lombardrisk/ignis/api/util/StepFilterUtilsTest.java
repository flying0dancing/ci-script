package com.lombardrisk.ignis.api.util;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

public class StepFilterUtilsTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void getFilterWithQuotes_returnsFilterWithQuotes() {
        HashSet<String> fields = newHashSet("NamE", "NetWorth", "JobTitle", "Description", "Networth_in_Billions");

        String filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes("NamE = 'test'", fields);
        soft.assertThat(filterWithQuotes).isEqualTo("\"NamE\" = 'test'");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes("Name = 'test'", fields);
        soft.assertThat(filterWithQuotes).isEqualTo("Name = 'test'");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes("NetWorth/100==900", fields);
        soft.assertThat(filterWithQuotes).isEqualTo("\"NetWorth\"/100==900");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes("concat(NamE, JobTitle)", fields);
        soft.assertThat(filterWithQuotes).isEqualTo("concat(\"NamE\", \"JobTitle\")");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes("concat(NamE, 'has a JobTitle', JobTitle)", fields);
        soft.assertThat(filterWithQuotes).isEqualTo("concat(\"NamE\", 'has a JobTitle', \"JobTitle\")");

        filterWithQuotes =
                StepFilterUtils.getStepFilterWithQuotes("NamE = 'JEFF BEZOS' AND Networth_in_Billions > 10", fields);
        soft.assertThat(filterWithQuotes).isEqualTo("\"NamE\" = 'JEFF BEZOS' AND \"Networth_in_Billions\" > 10");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes(
                "PriceOfAssetInMillions / PriceOfAsset",
                newHashSet("PriceOfAssetInMillions"));
        soft.assertThat(filterWithQuotes).isEqualTo("\"PriceOfAssetInMillions\" / PriceOfAsset");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes(
                "PriceOfAssetInMillions / PriceOfAsset",
                newHashSet("PriceOfAsset"));
        soft.assertThat(filterWithQuotes).isEqualTo("PriceOfAssetInMillions / \"PriceOfAsset\"");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes(
                "PriceOfAsset / PriceOfAssetInMillions",
                newHashSet("PriceOfAsset"));
        soft.assertThat(filterWithQuotes).isEqualTo("\"PriceOfAsset\" / PriceOfAssetInMillions");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes(
                "PriceOfAssetInBillions = PriceOfAsset / PriceOfAssetInMillions",
                newHashSet("PriceOfAsset"));
        soft.assertThat(filterWithQuotes)
                .isEqualTo("PriceOfAssetInBillions = \"PriceOfAsset\" / PriceOfAssetInMillions");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes(
                "if(concat(Name, Description, concat_ws(some_array)) == 'Matt smells', 1, 0) == 1",
                newHashSet("Name", "Description"));
        soft.assertThat(filterWithQuotes)
                .isEqualTo("if(concat(\"Name\", \"Description\", concat_ws(some_array)) == 'Matt smells', 1, 0) == 1");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes(
                "1=1 AND Name = 'Matt' AND 1=1 AND Name = 'Matt'",
                newHashSet("Name"));
        soft.assertThat(filterWithQuotes)
                .isEqualTo("1=1 AND \"Name\" = 'Matt' AND 1=1 AND \"Name\" = 'Matt'");

        filterWithQuotes = StepFilterUtils.getStepFilterWithQuotes(
                "1=1 AND Name = 'Matt' AND 1=1 AND Name = 'Matt' AND LastName = ' My Name is Anthony Gonsalves ' AND LastName = 'Matt'",
                newHashSet("Name"));
        soft.assertThat(filterWithQuotes)
                .isEqualTo("1=1 AND \"Name\" = 'Matt' AND 1=1 AND \"Name\" = 'Matt' AND LastName = ' My Name is Anthony Gonsalves ' AND LastName = 'Matt'");
    }

    @Test
    public void replaceDoubleEqualsByEquals_doubleEquals_repalcedByOneEquals() {
        List<String> filters = asList(
                "SomeText =='anyText1'",
                "SomeText==SomeText",
                "SomeText ==SomeText",
                "SomeText == 'anyText1'",
                "SomeText==Some'==Text"
        );

        List<String> results = filters.stream()
                .map(StepFilterUtils::replaceDoubleEqualsByEquals)
                .collect(Collectors.toList());

        soft.assertThat(results.get(0)).isEqualTo("SomeText ='anyText1'");
        soft.assertThat(results.get(1)).isEqualTo("SomeText=SomeText");
        soft.assertThat(results.get(2)).isEqualTo("SomeText =SomeText");
        soft.assertThat(results.get(3)).isEqualTo("SomeText = 'anyText1'");
        soft.assertThat(results.get(4)).isEqualTo("SomeText=Some'=Text");
    }

    @Test
    public void replaceDoubleEqualsByEquals_sequenceOfEqualsMoreThanDoubleEquals_stillUnchanged() {
        List<String> filters = asList(
                "SomeText === anyText",
                "SomeText===anyText",
                "SomeText=== anyText",
                "SomeText ===anyText",
                "===SomeText anyText"
        );

        List<String> results = filters.stream()
                .map(StepFilterUtils::replaceDoubleEqualsByEquals)
                .collect(Collectors.toList());

        soft.assertThat(results.get(0)).isEqualTo("SomeText === anyText");
        soft.assertThat(results.get(1)).isEqualTo("SomeText===anyText");
        soft.assertThat(results.get(2)).isEqualTo("SomeText=== anyText");
        soft.assertThat(results.get(3)).isEqualTo("SomeText ===anyText");
        soft.assertThat(results.get(4)).isEqualTo("===SomeText anyText");
    }

    @Test
    public void replaceDoubleEqualsByEquals_sequenceOfEqualsBetweenQuotes_stillUnchanged() {
        List<String> filters = asList(
                "'anyText=='",
                "'==anyText'",
                "'any==Text'"
        );

        List<String> results = filters.stream()
                .map(StepFilterUtils::replaceDoubleEqualsByEquals)
                .collect(Collectors.toList());

        soft.assertThat(results.get(0)).isEqualTo("'anyText=='");
        soft.assertThat(results.get(1)).isEqualTo("'==anyText'");
        soft.assertThat(results.get(2)).isEqualTo("'any==Text'");
    }

    @Test
    public void replaceDoubleEqualsByEquals_filterWithDoubleEqualsAndAnyCondition_returnsCheckedFilter() {
        final String filter = "field1 >== 2 and field2 <== 3 and "
                + "field3 =='azerty===' and "
                + "field4== '==querty' and field5 !== 'myText'";

        final String result = StepFilterUtils.replaceDoubleEqualsByEquals(filter);

        soft.assertThat(result).isEqualTo("field1 >= 2 and field2 <= 3 and "
                + "field3 ='azerty===' and "
                + "field4= '==querty' and field5 != 'myText'");
    }
}
