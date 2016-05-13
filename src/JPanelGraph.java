/**
 * Modified from public github repository gist.github.com/roooodcastro/6325153
 */

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven Teplica
 * @contributors Sean Levorse
 * <p>
 * A custom graph class that renders the simulation dynamically
 */

public class JPanelGraph extends JPanel {

    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private static String[] numSuffixes = {"M", "B", "T", "Qa", "Qu", "Sx", "Sp", "Oct", "Non", "Dec", "Und", "Duo"};
    private int padding = 55;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    private List<GraphScore> scores;
    private double graphBuffer = .003;

    public JPanelGraph(List<GraphScore> scores) {
        this.scores = scores;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.size() - 1);
        double yScale = ((double) getModifiedHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            int x1 = (int) (i * xScale + padding + labelPadding);
            int y1 = (int) ((getMaxScore() - scores.get(i).val) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        // Draw graph background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getModifiedHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {

            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getModifiedHeight() - ((i * (getModifiedHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (scores.size() >= 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                Double val = (((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0;
                DecimalFormat f = new DecimalFormat("#,###.00");
                FontMetrics metrics = g2.getFontMetrics();
                String yLabel = "";

                int pow;
                double temp = val;
                for (pow = 0; ; ) {
                    if (temp / 10 < 1) {
                        break;
                    }
                    pow++;
                    temp /= 10;
                }

                pow /= 3;
                if (pow >= 2) {
                    if (pow - 2 < numSuffixes.length) {
                        yLabel = f.format(val / Math.pow(10, pow * 3)) + numSuffixes[pow - 2];
                    } else {
                        yLabel = f.format(val / Math.pow(10, (numSuffixes.length - 1) * 3)) + numSuffixes[numSuffixes.length - 1];
                    }
                } else {
                    yLabel = f.format(val);
                }

                int labelWidth = metrics.stringWidth(yLabel);

                if (i % 2 == 0) {
                    if (scores.size() <= 1) {
                        if (i == 0) {
                            g2.drawString("0", x0 - metrics.stringWidth("0") - 5, y0 + (metrics.getHeight() / 2) - 3);
                        }
                    } else {
                        g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
                    }
                }
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        if (scores.size() > 1) {
            for (int i = 0; i < scores.size(); i++) {
                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scores.size() - 1) + padding + labelPadding;
                int x1 = x0;
                int y0 = getModifiedHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((scores.size() / 20.0)) + 1)) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getModifiedHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                }
                String xLabel = "";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(xLabel) / 2;
                if (i == 0) {
                    xLabel = JPanelSimulate.startDate;
                    labelWidth += 20;
                    g2.drawString("Time Elapsed", getWidth() / 2 - 20, y0 + metrics.getHeight() + 6);
                } else if (i == scores.size() - 1) {
                    xLabel = JPanelSimulate.currDate;
                    labelWidth += 50;
                }

                g2.drawString(xLabel, x0 - labelWidth, y0 + metrics.getHeight() + 3);
                g2.drawLine(x0, y0, x1, y1);
            }
        }

        // create x and y axes
        g2.drawLine(padding + labelPadding, getModifiedHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getModifiedHeight() - padding - labelPadding, getWidth() - padding, getModifiedHeight() - padding - labelPadding);

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(GRAPH_STROKE);
        if (scores.size() > 1) {
            for (int i = 0; i < graphPoints.size() - 1; i++) {
                int x1 = graphPoints.get(i).x;
                int y1 = graphPoints.get(i).y;
                int x2 = graphPoints.get(i + 1).x;
                int y2 = graphPoints.get(i + 1).y;
                if (scores.get(i + 1).days == 365) {
                    g2.setColor(new Color(255, 133, 51));
                } else if (scores.get(i + 1).days > 1) {
                    g2.setColor(new Color(71, 209, 71));
                } else {
                    g2.setColor(lineColor);
                }
                g2.drawLine(x1, y1, x2, y2);
            }

            g2.setStroke(oldStroke);
            g2.setColor(pointColor);
            for (int i = 0; i < graphPoints.size(); i++) {
                int x = graphPoints.get(i).x - pointWidth / 2;
                int y = graphPoints.get(i).y - pointWidth / 2;
                int ovalW = pointWidth;
                int ovalH = pointWidth;
                if (scores.get(i).days == 365) {
                    g2.setColor(new Color(255, 102, 0));
                } else if (scores.get(i).days > 1) {
                    g2.setColor(new Color(46, 184, 46));
                } else {
                    g2.setColor(pointColor);
                }
                g2.fillOval(x, y, ovalW, ovalH);
            }
        }
    }

    private double getMinScore() {
        double minScore = Double.MAX_VALUE;
        for (GraphScore score : scores) {
            minScore = Math.min(minScore, score.val);
        }
        minScore -= minScore * graphBuffer;

        return minScore > 0 ? minScore : 0;
    }

    private double getMaxScore() {
        double maxScore = Double.MIN_VALUE;
        for (GraphScore score : scores) {
            maxScore = Math.max(maxScore, score.val);
        }
        maxScore += maxScore * graphBuffer;
        return maxScore;
    }

    public void setScores(List<GraphScore> scores) {
        this.scores = scores;
        invalidate();
        this.repaint();
    }

    public int getModifiedHeight() {
        return getHeight() + 50;
    }
}