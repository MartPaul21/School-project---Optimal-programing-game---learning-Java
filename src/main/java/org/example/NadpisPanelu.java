package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class NadpisPanelu extends JLabel {

    public enum Styl {
        CERVENY,
        PERGAMEN,
        TMAVY
    }

    // Každý obrázok sa načíta iba raz.
    private static final BufferedImage PERGAMEN =
            nacitaj("/obrazky/nadpis_pergamen.png");

    private static final BufferedImage CERVENY =
            nacitaj("/obrazky/nadpis_cerveny.png");

    private final BufferedImage obrazok;
    private final Color vrch;
    private final Color spodok;
    private final Color ram;

    public NadpisPanelu(String text, Styl styl) {
        super(text);

        switch (styl) {
            case CERVENY -> {
                obrazok = CERVENY;
                vrch = new Color(110, 34, 29);
                spodok = new Color(55, 19, 18);
                ram = new Color(141, 104, 62);

                setForeground(new Color(245, 229, 193));
            }

            case PERGAMEN -> {
                obrazok = PERGAMEN;
                vrch = new Color(198, 175, 132);
                spodok = new Color(151, 126, 89);
                ram = new Color(92, 66, 40);

                setForeground(new Color(27, 21, 16));
            }

            case TMAVY -> {
                obrazok = null;
                vrch = new Color(48, 39, 30);
                spodok = new Color(25, 21, 18);
                ram = new Color(119, 90, 55);

                setForeground(new Color(220, 175, 95));
            }

            default -> throw new IllegalArgumentException(
                    "Neznámy štýl nadpisu."
            );
        }

        setFont(HernePismo.nadpis(19f));
        setHorizontalAlignment(SwingConstants.CENTER);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        setOpaque(false);
        setBorder(new EmptyBorder(10, 16, 10, 16));
    }

    private static BufferedImage nacitaj(String cesta) {
        try (InputStream vstup =
                     NadpisPanelu.class.getResourceAsStream(cesta)) {

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
                    "Podklad nadpisu sa nepodarilo načítať: "
                            + e.getMessage()
            );

            return null;
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(
                Integer.MAX_VALUE,
                getPreferredSize().height
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        int sirka = getWidth();
        int vyska = getHeight();

        if (sirka <= 0 || vyska <= 0) {
            return;
        }

        Graphics2D grafika = (Graphics2D) g.create();

        try {
            if (obrazok != null) {
                grafika.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR
                );

                vykresliPas(grafika, sirka, vyska);

            } else {
                // Tmavý štýl a náhradné pozadie,
                // ak sa obrázok nepodarilo načítať.
                grafika.setPaint(
                        new GradientPaint(
                                0, 0, vrch,
                                0, vyska, spodok
                        )
                );

                grafika.fillRect(0, 0, sirka, vyska);

                grafika.setColor(ram);
                grafika.drawRect(
                        0, 0,
                        sirka - 1, vyska - 1
                );
            }

        } finally {
            grafika.dispose();
        }

        // Text zostáva ostrý a nezávislý od obrázka.
        super.paintComponent(g);
    }

    private void vykresliPas(
            Graphics2D g,
            int sirka,
            int vyska
    ) {
        int zdrojSirka = obrazok.getWidth();
        int zdrojVyska = obrazok.getHeight();

        // Zachováme samostatné konce pásu.
        // Na šírku sa roztiahne iba jeho stredná časť.
        int zdrojOkraj = Math.max(1, zdrojSirka / 10);
        int cielOkraj = Math.min(24, sirka / 3);

        // Ľavý koniec.
        g.drawImage(
                obrazok,
                0, 0, cielOkraj, vyska,
                0, 0, zdrojOkraj, zdrojVyska,
                null
        );

        // Stred.
        g.drawImage(
                obrazok,
                cielOkraj, 0,
                sirka - cielOkraj, vyska,
                zdrojOkraj, 0,
                zdrojSirka - zdrojOkraj, zdrojVyska,
                null
        );

        // Pravý koniec.
        g.drawImage(
                obrazok,
                sirka - cielOkraj, 0,
                sirka, vyska,
                zdrojSirka - zdrojOkraj, 0,
                zdrojSirka, zdrojVyska,
                null
        );
    }
}