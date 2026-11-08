package daa;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class PlotResults {
    private record Row(String algorithm, String input, int n, double time, long operations) { }
    private record Point(int n, double value) { }
    private record Series(String name, Color color, boolean dashed, List<Point> points) { }

    private PlotResults() { }

    public static void main(String[] args) throws IOException {
        List<Row> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of("results/results.csv"));
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split(",");
            rows.add(new Row(fields[0], fields[1], Integer.parseInt(fields[2]),
                    Double.parseDouble(fields[5]), Long.parseLong(fields[6])));
        }
        Files.createDirectories(Path.of("results/plots"));
        String[][] edit = {{"recursive"}, {"table", "two_rows"}};
        String[][] search = {{"naive", "kmp"}};
        chart(rows, edit, false, "Edit Distance: measured work", "Calls / cell updates (count, log10)", "edit_work.png");
        chart(rows, edit, true, "Edit Distance: median of five runs", "Median time (ms, log10)", "edit_time.png");
        chart(rows, search, false, "String search: comparisons including LPS", "Character comparisons (count, log10)", "search_work.png");
        chart(rows, search, true, "String search: median of five runs", "Median time (ms, log10)", "search_time.png");
        System.out.println("Saved four PNG charts in results/plots/");
    }

    private static void chart(List<Row> rows, String[][] panels, boolean time,
                              String title, String yLabel, String filename) throws IOException {
        int width = 1400;
        int height = 720;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        centered(g, title, width / 2, 32);
        for (int panel = 0; panel < panels.length; panel++) {
            List<Series> series = new ArrayList<>();
            for (String algorithm : panels[panel]) {
                for (String input : new String[]{"random", "disjoint", "late_mismatch"}) {
                    Map<Integer, List<Row>> groups = new TreeMap<>();
                    for (Row row : rows) {
                        if (row.algorithm.equals(algorithm) && row.input.equals(input)) {
                            groups.computeIfAbsent(row.n, ignored -> new ArrayList<>()).add(row);
                        }
                    }
                    if (groups.isEmpty()) continue;
                    List<Point> points = new ArrayList<>();
                    for (var group : groups.entrySet()) {
                        double[] values = group.getValue().stream()
                                .mapToDouble(row -> time ? row.time : row.operations).sorted().toArray();
                        if (values.length != 5 || values[0] <= 0) {
                            throw new IllegalArgumentException("Expected five positive measurements per case");
                        }
                        points.add(new Point(group.getKey(), values[values.length / 2]));
                    }
                    series.add(new Series(algorithm + " / " + input, color(algorithm),
                            !input.equals("random"), points));
                }
            }
            drawPanel(g, series, panel * width / panels.length, width / panels.length,
                    String.join(" + ", panels[panel]), yLabel);
        }
        g.dispose();
        ImageIO.write(image, "png", Path.of("results/plots", filename).toFile());
    }

    private static void drawPanel(Graphics2D g, List<Series> series, int offset, int width,
                                  String subtitle, String yLabel) {
        int left = offset + 100;
        int right = offset + width - 35;
        int top = 205;
        int bottom = 635;
        List<Point> all = series.stream().flatMap(s -> s.points.stream()).toList();
        int minN = all.stream().mapToInt(Point::n).min().orElseThrow();
        int maxN = all.stream().mapToInt(Point::n).max().orElseThrow();
        double minY = Math.floor(Math.log10(all.stream().mapToDouble(Point::value).min().orElseThrow()));
        double maxY = Math.ceil(Math.log10(all.stream().mapToDouble(Point::value).max().orElseThrow()));
        if (maxY == minY) maxY++;
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        centered(g, subtitle, offset + width / 2, 65);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        for (int i = 0; i < series.size(); i++) {
            Series s = series.get(i);
            int y = 95 + i * 24;
            g.setColor(s.color);
            g.setStroke(stroke(s.dashed));
            g.drawLine(left, y, left + 40, y);
            g.setColor(Color.BLACK);
            g.drawString(s.name, left + 50, y + 5);
        }
        g.setStroke(new BasicStroke(1));
        for (int exponent = (int) minY; exponent <= maxY; exponent++) {
            int y = (int) (bottom - (exponent - minY) / (maxY - minY) * (bottom - top));
            g.setColor(new Color(225, 228, 232));
            g.drawLine(left, y, right, y);
            g.setColor(Color.DARK_GRAY);
            String label = "1e" + exponent;
            g.drawString(label, left - 12 - g.getFontMetrics().stringWidth(label), y + 5);
        }
        for (int n : all.stream().mapToInt(Point::n).distinct().sorted().toArray()) {
            int x = x(n, minN, maxN, left, right);
            g.setColor(new Color(235, 237, 240));
            g.drawLine(x, top, x, bottom);
            g.setColor(Color.DARK_GRAY);
            centered(g, Integer.toString(n), x, bottom + 25);
        }
        g.setColor(Color.BLACK);
        g.drawRect(left, top, right - left, bottom - top);
        centered(g, "n (characters, log10)", (left + right) / 2, 693);
        g.rotate(-Math.PI / 2);
        centered(g, yLabel, -(top + bottom) / 2, offset + 25);
        g.rotate(Math.PI / 2);
        for (Series s : series) {
            g.setColor(s.color);
            g.setStroke(stroke(s.dashed));
            int lastX = -1;
            int lastY = -1;
            for (Point p : s.points) {
                int px = x(p.n, minN, maxN, left, right);
                int py = (int) (bottom - (Math.log10(p.value) - minY) / (maxY - minY) * (bottom - top));
                if (lastX >= 0) g.drawLine(lastX, lastY, px, py);
                if (s.name.startsWith("two_rows")) g.fillRect(px - 3, py - 3, 6, 6);
                else g.drawOval(px - 6, py - 6, 12, 12);
                lastX = px;
                lastY = py;
            }
        }
    }

    private static int x(int n, int min, int max, int left, int right) {
        return left + (int) (Math.log10((double) n / min) / Math.log10((double) max / min) * (right - left));
    }

    private static BasicStroke stroke(boolean dashed) {
        return dashed ? new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                0, new float[]{8, 6}, 0) : new BasicStroke(2.5f);
    }

    private static Color color(String algorithm) {
        return switch (algorithm) {
            case "recursive" -> new Color(38, 99, 180);
            case "table", "naive" -> new Color(203, 84, 35);
            default -> new Color(0, 125, 105);
        };
    }

    private static void centered(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y);
    }
}
