package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ObrazkovyPanel extends JPanel {

    private final BufferedImage obrazok;

    public ObrazkovyPanel(String cesta) {
        setBackground(new Color(20, 18, 17));
        setPreferredSize(new Dimension(240, 360));
        setMinimumSize(new Dimension(100, 100));

        obrazok = nacitaj(cesta);
    }

    private BufferedImage nacitaj(String cesta) {
        try (InputStream vstup =
                     ObrazkovyPanel.class.getResourceAsStream(cesta)) {

            if (vstup == null) {
                throw new IOException(
                        "Súbor sa nenašiel: " + cesta
                );
            }

            BufferedImage nacitany = ImageIO.read(vstup);

            if (nacitany == null) {
                throw new IOException(
                        "Nepodporovaný obrázok: " + cesta
                );
            }

            return nacitany;

        } catch (IOException e) {
            System.err.println(
                    "Portrét sa nepodarilo načítať: "
                            + e.getMessage()
            );

            setToolTipText(e.getMessage());
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int sirka = getWidth();
        int vyska = getHeight();

        if (sirka <= 0 || vyska <= 0) {
            return;
        }

        Graphics2D grafika = (Graphics2D) g.create();

        try {
            if (obrazok == null) {
                grafika.setColor(new Color(230, 218, 192));
                grafika.drawString(
                        "Obrázok sa nenačítal.",
                        10,
                        25
                );
                return;
            }

            grafika.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            // Vyplnenie celého panela bez deformácie.
            double mierka = Math.max(
                    sirka / (double) obrazok.getWidth(),
                    vyska / (double) obrazok.getHeight()
            );

            int novaSirka = (int) Math.ceil(
                    obrazok.getWidth() * mierka
            );

            int novaVyska = (int) Math.ceil(
                    obrazok.getHeight() * mierka
            );

            // Vodorovne zarovnáme na stred.
            int x = (sirka - novaSirka) / 2;

            // Zvisle zachováme hornú časť portrétu.
            int y = 0;

            grafika.drawImage(
                    obrazok,
                    x,
                    y,
                    novaSirka,
                    novaVyska,
                    null
            );

            // Jemné stmavenie spodného okraja.
            int vyskaPrechodu = Math.min(65, vyska);

            grafika.setPaint(
                    new GradientPaint(
                            0, vyska - vyskaPrechodu,
                            new Color(20, 18, 17, 0),
                            0, vyska,
                            new Color(20, 18, 17, 160)
                    )
            );

            grafika.fillRect(
                    0,
                    vyska - vyskaPrechodu,
                    sirka,
                    vyskaPrechodu
            );

            // Tenký vnútorný rám portrétu.
            grafika.setColor(new Color(95, 70, 43));
            grafika.drawRect(
                    0, 0,
                    sirka - 1, vyska - 1
            );

        } finally {
            grafika.dispose();
        }
    }
}