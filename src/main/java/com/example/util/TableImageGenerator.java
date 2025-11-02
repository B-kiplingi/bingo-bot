package com.example.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TableImageGenerator {
    public static File generateTableImage(boolean[][] checked, String[][] labels, String user) {
        int rows = labels.length;
        int cols = labels[0].length;

        int cellWidth = 200;
        int cellHeight = 100;
        int borderThickness = 2;
        int labelMargin = 30; // Space for coordinate labels

        int width = cols * cellWidth + borderThickness + labelMargin;
        int height = rows * cellHeight + borderThickness + labelMargin;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Enable antialiasing for better text quality
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Draw column labels (A, B, C, D, E) at the top
        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        g.setFont(labelFont);
        g.setColor(Color.BLACK);
        FontMetrics labelFm = g.getFontMetrics();

        for (int c = 0; c < cols; c++) {
            String colLabel = String.valueOf((char)('A' + c));
            int x = labelMargin + c * cellWidth + cellWidth / 2 - labelFm.stringWidth(colLabel) / 2;
            int y = labelMargin / 2 + labelFm.getAscent() / 2;
            g.drawString(colLabel, x, y);
        }

        // Draw row labels (1, 2, 3, 4, 5) on the left
        for (int r = 0; r < rows; r++) {
            String rowLabel = String.valueOf(r + 1);
            int x = labelMargin / 2 - labelFm.stringWidth(rowLabel) / 2;
            int y = labelMargin + r * cellHeight + cellHeight / 2 + labelFm.getAscent() / 2;
            g.drawString(rowLabel, x, y);
        }

        // Draw cells
        Font font = new Font("SansSerif", Font.BOLD, 16);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = labelMargin + c * cellWidth;
                int y = labelMargin + r * cellHeight;

                // Fill cell background
                if (checked[r][c]) {
                    g.setColor(new Color(100, 200, 100)); // greenish for checked
                } else {
                    g.setColor(new Color(230, 230, 230)); // light gray
                }
                g.fillRect(x, y, cellWidth, cellHeight);

                // Draw border
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(borderThickness));
                g.drawRect(x, y, cellWidth, cellHeight);

                // Draw wrapped text (centered)
                g.setColor(Color.BLACK);
                String text = labels[r][c];

                // Wrap text to fit within the cell width (with padding)
                int padding = 10;
                java.util.List<String> lines = wrapText(text, fm, cellWidth - padding * 2);

                // Compute total height of the block
                int lineHeight = fm.getHeight();
                int blockHeight = lineHeight * lines.size();

                // Top of the text block (center vertically)
                int startY = y + (cellHeight - blockHeight) / 2 + fm.getAscent();

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    int lineWidth = fm.stringWidth(line);
                    int lineX = x + (cellWidth - lineWidth) / 2; // center horizontally
                    int lineY = startY + i * lineHeight;
                    g.drawString(line, lineX, lineY);
                }
            }
        }

        g.dispose();

        try {
            // Save to temp file
            File file = File.createTempFile(user, ".png");
            javax.imageio.ImageIO.write(image, "png", file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();

        // Allow manual line breaks
        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                if (line.isEmpty()) {
                    line.append(word);
                } else {
                    String testLine = line + " " + word;
                    if (fm.stringWidth(testLine) <= maxWidth) {
                        line.append(" ").append(word);
                    } else {
                        lines.add(line.toString());
                        line = new StringBuilder(word);
                    }
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }
}