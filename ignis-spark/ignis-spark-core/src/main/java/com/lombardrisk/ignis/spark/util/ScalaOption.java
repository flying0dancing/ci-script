package com.lombardrisk.ignis.spark.util;

import lombok.experimental.UtilityClass;
import scala.Option;

@UtilityClass
public class ScalaOption {

    public static final Option<String> NONE = scala.Option.apply(null);
}