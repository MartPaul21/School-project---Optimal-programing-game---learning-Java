package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PozadiePanel extends JPanel {

    private final BufferedImage pozadie;

    public PozadiePanel(LayoutManager rozlozenie) {
        super(rozlozenie);

        setOpaque(true);
        setBackground(new Color(24, 22, 20));

        pozadie = nacitajPozadie();
    }

    private BufferedImage nacitajPozadie() {
        String cesta = "/obrazky/zalar.png";

        try (InputStream vstup =
                     PozadiePanel.class.getResourceAsStream(cesta)) {

            if (vstup == null) {
                throw new IOException(
                        "Súbor sa nenašiel: " + cesta
                );
            }

            BufferedImage obrazok = ImageIO.read(vstup);

            if (obrazok == null) {
                throw new IOException(
                        "Súbor nie je podporovaný obrázok: " + cesta
                );
            }

            return obrazok;

        } catch (IOException e) {
            System.err.println(
                    "Pozadie sa nepodarilo načítať: "
                            + e.getMessage()
            );

            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (pozadie == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        Graphics2D grafika = (Graphics2D) g.create();

        try {
            grafika.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            // Vyplníme celé okno bez deformácie obrázka.
            // Pri inom pomere strán sa časť okrajov oreže.
            double mierka = Math.max(
                    getWidth() / (double) pozadie.getWidth(),
                    getHeight() / (double) pozadie.getHeight()
            );

            int sirka = (int) Math.ceil(
                    pozadie.getWidth() * mierka
            );

            int vyska = (int) Math.ceil(
                    pozadie.getHeight() * mierka
            );

            int x = (getWidth() - sirka) / 2;
            int y = (getHeight() - vyska) / 2;

            grafika.drawImage(
                    pozadie,
                    x,
                    y,
                    sirka,
                    vyska,
                    null
            );

        } finally {
            grafika.dispose();
        }
    }
}