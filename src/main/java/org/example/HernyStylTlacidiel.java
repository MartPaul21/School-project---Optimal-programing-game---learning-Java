package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class HernyStylTlacidiel {

    private static final BufferedImage CERVENY_PODKLAD =
            nacitajPodklad();

    private HernyStylTlacidiel() {
    }

    private static BufferedImage nacitajPodklad() {
        String cesta = "/obrazky/nadpis_cerveny.png";

        try (InputStream vstup =
                     HernyStylTlacidiel.class.getResourceAsStream(cesta)) {

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
                    "Podklad tlačidla sa nepodarilo načítať: "
                            + e.getMessage()
            );

            return null;
        }
    }

    public static void uprav(JButton tlacidlo, boolean hlavne) {
        tlacidlo.setUI(new BasicButtonUI() {

            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton button = (AbstractButton) c;
                ButtonModel model = button.getModel();

                int sirka = c.getWidth();
                int vyska = c.getHeight();

                if (sirka <= 0 || vyska <= 0) {
                    return;
                }

                boolean stlacene =
                        model.isPressed() && model.isArmed();

                Color vrch;
                Color spodok;
                Color ram;

                if (!button.isEnabled()) {
                    vrch = new Color(44, 40, 36);
                    spodok = new Color(31, 28, 25);
                    ram = new Color(83, 72, 57);

                } else if (stlacene) {
                    vrch = hlavne
                            ? new Color(72, 19, 17)
                            : new Color(48, 34, 25);

                    spodok = hlavne
                            ? new Color(108, 29, 24)
                            : new Color(76, 53, 35);

                    ram = new Color(220, 175, 95);

                } else if (model.isRollover()) {
                    vrch = hlavne
                            ? new Color(155, 48, 37)
                            : new Color(104, 75, 46);

                    spodok = hlavne
                            ? new Color(100, 26, 22)
                            : new Color(62, 43, 29);

                    ram = new Color(220, 175, 95);

                } else {
                    vrch = hlavne
                            ? new Color(125, 35, 29)
                            : new Color(73, 52, 35);

                    spodok = hlavne
                            ? new Color(75, 20, 18)
                            : new Color(39, 30, 24);

                    ram = new Color(141, 104, 62);
                }

                Graphics2D grafika = (Graphics2D) g.create();

                try {
                    grafika.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR
                    );

                    if (hlavne && CERVENY_PODKLAD != null) {
                        vykresliPodklad(
                                grafika,
                                sirka,
                                vyska
                        );

                        // Prekrytie mení iba podklad.
                        // Text sa vykreslí až následne.
                        if (!button.isEnabled()) {
                            grafika.setColor(
                                    new Color(30, 28, 26, 195)
                            );
                            grafika.fillRect(0, 0, sirka, vyska);

                        } else if (stlacene) {
                            grafika.setColor(
                                    new Color(0, 0, 0, 85)
                            );
                            grafika.fillRect(0, 0, sirka, vyska);

                        } else if (model.isRollover()) {
                            grafika.setColor(
                                    new Color(240, 154, 76, 35)
                            );
                            grafika.fillRect(0, 0, sirka, vyska);
                        }

                    } else {
                        // Ostatné tlačidlá používajú pôvodný vzhľad.
                        grafika.setPaint(
                                new GradientPaint(
                                        0, 0, vrch,
                                        0, vyska, spodok
                                )
                        );

                        grafika.fillRect(0, 0, sirka, vyska);
                    }

                    grafika.setColor(ram);
                    grafika.drawRect(
                            0, 0,
                            sirka - 1, vyska - 1
                    );

                    // Jemná vnútorná linka pri prejdení myšou.
                    if (button.isEnabled()
                            && (model.isRollover() || stlacene)) {

                        grafika.setColor(
                                new Color(220, 175, 95, 110)
                        );

                        grafika.drawRect(
                                2, 2,
                                sirka - 5, vyska - 5
                        );
                    }

                    // Označenie pri ovládaní klávesnicou.
                    if (button.hasFocus() && button.isEnabled()) {
                        grafika.setColor(
                                new Color(220, 175, 95)
                        );

                        grafika.drawRect(
                                4, 4,
                                sirka - 9, vyska - 9
                        );
                    }

                } finally {
                    grafika.dispose();
                }

                // Text a ikony vykreslí Swing nad podkladom.
                super.paint(g, c);
            }

            @Override
            protected void paintText(
                    Graphics g,
                    AbstractButton button,
                    Rectangle textRect,
                    String text
            ) {
                if (button.isEnabled()) {
                    super.paintText(g, button, textRect, text);
                    return;
                }

                g.setColor(new Color(145, 133, 114));

                FontMetrics metrika = g.getFontMetrics();

                g.drawString(
                        text,
                        textRect.x,
                        textRect.y + metrika.getAscent()
                );
            }
        });

        tlacidlo.setForeground(new Color(239, 222, 184));
        tlacidlo.setContentAreaFilled(false);
        tlacidlo.setOpaque(false);
        tlacidlo.setBorderPainted(false);
        tlacidlo.setFocusPainted(false);
        tlacidlo.setRolloverEnabled(true);

        tlacidlo.setBorder(
                BorderFactory.createEmptyBorder(
                        hlavne ? 12 : 8,
                        16,
                        hlavne ? 12 : 8,
                        16
                )
        );
    }

    private static void vykresliPodklad(
            Graphics2D g,
            int sirka,
            int vyska
    ) {
        int zdrojSirka = CERVENY_PODKLAD.getWidth();
        int zdrojVyska = CERVENY_PODKLAD.getHeight();

        int zdrojOkraj = Math.max(1, zdrojSirka / 10);
        int cielOkraj = Math.min(30, sirka / 3);

        // Ľavý ozdobný koniec.
        g.drawImage(
                CERVENY_PODKLAD,
                0, 0,
                cielOkraj, vyska,
                0, 0,
                zdrojOkraj, zdrojVyska,
                null
        );

        // Roztiahnuteľný stred.
        g.drawImage(
                CERVENY_PODKLAD,
                cielOkraj, 0,
                sirka - cielOkraj, vyska,
                zdrojOkraj, 0,
                zdrojSirka - zdrojOkraj, zdrojVyska,
                null
        );

        // Pravý ozdobný koniec.
        g.drawImage(
                CERVENY_PODKLAD,
                sirka - cielOkraj, 0,
                sirka, vyska,
                zdrojSirka - zdrojOkraj, 0,
                zdrojSirka, zdrojVyska,
                null
        );
    }
}