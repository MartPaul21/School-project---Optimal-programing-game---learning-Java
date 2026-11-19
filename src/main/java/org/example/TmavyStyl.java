package org.example;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public final class TmavyStyl {

    private static final Color POZADIE = new Color(25, 22, 20);
    private static final Color PANEL = new Color(40, 37, 35);
    private static final Color HLAVICKA = new Color(67, 47, 36);
    private static final Color RAM = new Color(115, 87, 58);

    private TmavyStyl() {
    }

    public static void upravPosuvanie(JScrollPane panel) {
        panel.setBorder(BorderFactory.createLineBorder(RAM));
        panel.setViewportBorder(null);
        panel.setBackground(PANEL);
        panel.getViewport().setBackground(PANEL);

        if (panel.getColumnHeader() != null) {
            panel.getColumnHeader().setBackground(HLAVICKA);
            panel.getColumnHeader().setOpaque(true);
        }

        upravPosuvnik(panel.getVerticalScrollBar());
        upravPosuvnik(panel.getHorizontalScrollBar());

        String[] rohy = {
                JScrollPane.UPPER_LEFT_CORNER,
                JScrollPane.UPPER_RIGHT_CORNER,
                JScrollPane.LOWER_LEFT_CORNER,
                JScrollPane.LOWER_RIGHT_CORNER
        };

        for (String roh : rohy) {
            JPanel vypln = new JPanel();
            vypln.setBackground(HLAVICKA);
            panel.setCorner(roh, vypln);
        }
    }

    private static void upravPosuvnik(JScrollBar posuvnik) {
        posuvnik.setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {
                trackColor = POZADIE;
                thumbColor = RAM;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return vytvorPrazdneTlacidlo();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return vytvorPrazdneTlacidlo();
            }

            private JButton vytvorPrazdneTlacidlo() {
                JButton tlacidlo = new JButton();
                Dimension velkost = new Dimension(0, 0);

                tlacidlo.setPreferredSize(velkost);
                tlacidlo.setMinimumSize(velkost);
                tlacidlo.setMaximumSize(velkost);
                tlacidlo.setBorder(null);
                tlacidlo.setFocusable(false);

                return tlacidlo;
            }

            @Override
            protected void paintTrack(
                    Graphics g,
                    JComponent c,
                    Rectangle plocha
            ) {
                g.setColor(POZADIE);
                g.fillRect(
                        plocha.x, plocha.y,
                        plocha.width, plocha.height
                );
            }

            @Override
            protected void paintThumb(
                    Graphics g,
                    JComponent c,
                    Rectangle plocha
            ) {
                if (plocha.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }

                Graphics2D grafika = (Graphics2D) g.create();

                try {
                    grafika.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                    );

                    grafika.setColor(
                            isDragging
                                    ? new Color(190, 150, 95)
                                    : isThumbRollover()
                                      ? new Color(155, 119, 74)
                                      : RAM
                    );

                    grafika.fillRoundRect(
                            plocha.x + 2,
                            plocha.y + 2,
                            Math.max(0, plocha.width - 4),
                            Math.max(0, plocha.height - 4),
                            6,
                            6
                    );
                } finally {
                    grafika.dispose();
                }
            }
        });

        if (posuvnik.getOrientation() == JScrollBar.VERTICAL) {
            posuvnik.setPreferredSize(new Dimension(12, 0));
        } else {
            posuvnik.setPreferredSize(new Dimension(0, 12));
        }

        posuvnik.setBackground(POZADIE);
        posuvnik.setBorder(null);
        posuvnik.setUnitIncrement(16);
    }

    public static void upravRozdelenie(JSplitPane rozdelenie) {
        rozdelenie.setUI(new BasicSplitPaneUI() {

            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {

                    @Override
                    public void paint(Graphics g) {
                        g.setColor(POZADIE);
                        g.fillRect(0, 0, getWidth(), getHeight());

                        g.setColor(RAM);

                        if (splitPane.getOrientation()
                                == JSplitPane.VERTICAL_SPLIT) {

                            int stredY = getHeight() / 2;
                            g.drawLine(
                                    0, stredY,
                                    getWidth() - 1, stredY
                            );

                            // Malý úchyt v strede deliacej lišty.
                            g.setColor(new Color(190, 150, 95));
                            g.fillRect(
                                    getWidth() / 2 - 18,
                                    stredY - 1,
                                    36,
                                    3
                            );

                        } else {
                            int stredX = getWidth() / 2;
                            g.drawLine(
                                    stredX, 0,
                                    stredX, getHeight() - 1
                            );
                        }
                    }
                };
            }
        });

        rozdelenie.setBorder(null);
        rozdelenie.setBackground(POZADIE);
        rozdelenie.setDividerSize(10);
        rozdelenie.setContinuousLayout(true);
        rozdelenie.setOneTouchExpandable(false);
    }
}