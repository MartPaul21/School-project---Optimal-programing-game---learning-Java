package org.example;

import javax.swing.border.AbstractBorder;
import java.awt.*;

public class OzdobnyRam extends AbstractBorder {

    private static final Color TMAVA = new Color(48, 35, 25);
    private static final Color BRONZ = new Color(119, 87, 51);
    private static final Color SVETLA = new Color(190, 151, 96);

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(10, 10, 10, 10);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(10, 10, 10, 10);
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(
            Component c,
            Graphics g,
            int x,
            int y,
            int width,
            int height
    ) {
        if (width < 40 || height < 40) {
            return;
        }

        Graphics2D grafika = (Graphics2D) g.create();

        try {
            grafika.translate(x, y);

            grafika.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Široký tmavý základ rámu.
            grafika.setColor(TMAVA);
            grafika.setStroke(new BasicStroke(6f));
            grafika.drawRect(4, 4, width - 9, height - 9);

            // Vonkajšia bronzová linka.
            grafika.setStroke(new BasicStroke(1f));
            grafika.setColor(BRONZ);
            grafika.drawRect(1, 1, width - 3, height - 3);

            // Vnútorná linka.
            grafika.drawRect(8, 8, width - 17, height - 17);

            // Svetlo na hornej a ľavej hrane.
            grafika.setColor(SVETLA);
            grafika.drawLine(3, 3, width - 4, 3);
            grafika.drawLine(3, 3, 3, height - 4);

            // Tieň na spodnej a pravej hrane.
            grafika.setColor(new Color(19, 15, 12));
            grafika.drawLine(
                    5, height - 5,
                    width - 5, height - 5
            );
            grafika.drawLine(
                    width - 5, 5,
                    width - 5, height - 5
            );

            // Rovnaká ozdoba otočená do všetkých štyroch rohov.
            vykresliRoh(grafika, 0, 0, 1, 1);
            vykresliRoh(grafika, width - 1, 0, -1, 1);
            vykresliRoh(grafika, 0, height - 1, 1, -1);
            vykresliRoh(
                    grafika,
                    width - 1,
                    height - 1,
                    -1,
                    -1
            );

        } finally {
            grafika.dispose();
        }
    }

    private void vykresliRoh(
            Graphics2D povodna,
            int x,
            int y,
            int smerX,
            int smerY
    ) {
        Graphics2D g = (Graphics2D) povodna.create();

        try {
            g.translate(x, y);
            g.scale(smerX, smerY);

            // Kovové kovanie v tvare písmena L.
            Polygon kovanie = new Polygon(
                    new int[]{1, 31, 26, 9, 9, 1},
                    new int[]{1, 1, 8, 8, 26, 31},
                    6
            );

            g.setPaint(
                    new GradientPaint(
                            0, 0, SVETLA,
                            24, 24, TMAVA
                    )
            );
            g.fillPolygon(kovanie);

            g.setColor(BRONZ);
            g.setStroke(new BasicStroke(1f));
            g.drawPolygon(kovanie);

            // Dva drobné nity.
            vykresliNit(g, 17, 4);
            vykresliNit(g, 4, 17);

        } finally {
            g.dispose();
        }
    }

    private void vykresliNit(Graphics2D g, int x, int y) {
        g.setColor(new Color(20, 15, 11));
        g.fillOval(x - 2, y - 2, 6, 6);

        g.setColor(BRONZ);
        g.fillOval(x - 2, y - 2, 4, 4);

        g.setColor(SVETLA);
        g.fillOval(x - 1, y - 1, 2, 2);
    }
}