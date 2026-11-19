package org.example;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public final class HernePismo {

    // Súbor načítame iba raz.
    private static final Font ZAKLAD = nacitajPismo();

    private HernePismo() {
    }

    private static Font nacitajPismo() {
        try (InputStream vstup = HernePismo.class
                .getResourceAsStream("/fonty/Cinzel.ttf")) {

            if (vstup == null) {
                throw new IOException(
                        "Chýba súbor /fonty/Cinzel.ttf"
                );
            }

            Font pismo = Font.createFont(
                    Font.TRUETYPE_FONT,
                    vstup
            );

            String kontrola = "ÁÄČĎÉÍĹĽŇÓÔŔŠŤÚÝŽáäčďéíĺľňóôŕšťúýž";

            if (pismo.canDisplayUpTo(kontrola) != -1) {
                throw new IOException(
                        "Písmo nepodporuje všetky kontrolované "
                                + "slovenské znaky."
                );
            }

            return pismo;

        } catch (IOException | FontFormatException e) {
            System.err.println(
                    "Herné písmo sa nepodarilo načítať: "
                            + e.getMessage()
                            + ". Používam náhradné písmo Serif."
            );

            return new Font("Serif", Font.PLAIN, 20);
        }
    }

    public static Font nadpis(float velkost) {
        return ZAKLAD.deriveFont(Font.BOLD, velkost);
    }
}