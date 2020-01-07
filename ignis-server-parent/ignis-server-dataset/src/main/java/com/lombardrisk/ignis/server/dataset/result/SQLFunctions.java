package com.lombardrisk.ignis.server.dataset.result;

import java.io.Serializable;

public interface SQLFunctions extends Serializable {

    String toDate(String value, String format);

    String toTimestamp(String value, String format);
}
