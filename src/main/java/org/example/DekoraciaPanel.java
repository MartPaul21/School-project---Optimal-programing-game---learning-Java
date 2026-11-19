package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class DekoraciaPanel extends JPanel {

    private static final BufferedImage OBRAZOK = nacitaj();
    private final boolean zrkadlit;

    public DekoraciaPanel(boolean zrkadlit) {
        this.zrkadlit = zrkadlit;

        setOpaque(false);
        setFocusable(false);

        setPreferredSize(new Dimension(76, 76));
        setMinimumSize(new Dimension(40, 40));
    }

    private static BufferedImage nacitaj() {
        String cesta = "/obrazky/dekoracia_sviecky.png";

        try (InputStream vstup =
                     DekoraciaPanel.class.getResourceAsStream(cesta)) {

            if (vstup == null) {
                throw new IOException(
                        "Súbor sa nenašiel: " + cesta
                );
            }

            BufferedImage obrazok = ImageIO.read(vstup);

            if (obrazok == null) {
                throw new IOException(
                        "Nepodporovaný obrázok: " + cesta
                );
            }

            return obrazok;

        } catch (IOException e) {
            System.err.println(
                    "Dekorácia sa nepodarila načítať: "
                            + e.getMessage()
            );

            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (OBRAZOK == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        Graphics2D grafika = (Graphics2D) g.create();

        try {
            grafika.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            double mierka = Math.min(
                    getWidth() / (double) OBRAZOK.getWidth(),
                    getHeight() / (double) OBRAZOK.getHeight()
            );

            int sirka = Math.max(
                    1,
                    (int) Math.round(OBRAZOK.getWidth() * mierka)
            );

            int vyska = Math.max(
                    1,
                    (int) Math.round(OBRAZOK.getHeight() * mierka)
            );

            int x = (getWidth() - sirka) / 2;
            int y = getHeight() - vyska;

            if (zrkadlit) {
                grafika.translate(x + sirka, y);
                grafika.scale(-1, 1);
            } else {
                grafika.translate(x, y);
            }

            grafika.drawImage(
                    OBRAZOK,
                    0,
                    0,
                    sirka,
                    vyska,
                    null
            );

        } finally {
            grafika.dispose();
        }
    }
}