package com.lombardrisk.ignis.common.reservedWord;

import java.util.ArrayList;
import java.util.List;

public class ReservedWordValidator {

    public List<String> catchReservedWords(final String name) {
        List<String> capturedKeyWords = new ArrayList<>();
        String[] values = name.trim().toLowerCase().split(" ");
        for (ReservedWord reservedWord : ReservedWord.values()) {
            for (String value : values) {
                if (value.equals(reservedWord.getValue())) {
                    capturedKeyWords.add(reservedWord.getValue());
                }
            }
        }
        return capturedKeyWords;
    }
}
