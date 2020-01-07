package com.lombardrisk.ignis.common.reservedWord;

import java.util.List;

public class FieldValidator extends ReservedWordValidator {

    private static final String FCR_SYS = "fcr_sys";

    public List<String> catchReservedWords(final String name) {
        List<String> capturedKeyWords = super.catchReservedWords(name);

        if (name.toLowerCase().startsWith(FCR_SYS)) {
            capturedKeyWords.add(FCR_SYS);
        }
        return capturedKeyWords;
    }
}
