package ru.mephi.vikingdemo.gui;

import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.VikingView;
import ru.mephi.vikingdemo.service.VikingQueryService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VikingQueryReportFrame extends JFrame {

    private final VikingQueryService queryService;
    private final Runnable refreshTable;
    private final JTextArea reportArea = new JTextArea();

    public VikingQueryReportFrame(VikingQueryService queryService, Runnable refreshTable) {
        this.queryService = queryService;
        this.refreshTable = refreshTable;

        setTitle("Query report");
        setSize(new Dimension(820, 440));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menu.add(action("Age", this::writeAgeReport));
        menu.add(action("Appearance", this::writeAppearanceReport));
        menu.add(action("Axes", this::writeAxesReport));
        menu.add(action("Tall", this::writeTallReport));
        menu.add(action("Legendary", this::writeLegendaryReport));
        menu.add(action("Red sorted", this::writeRedReport));
        menu.add(action("IDs", this::writeIdReport));
        menu.add(action("Create pack", this::createPack));
        add(menu, BorderLayout.NORTH);

        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);
    }

    private JButton action(String title, Runnable command) {
        JButton button = new JButton(title);
        button.addActionListener(event -> command.run());
        return button;
    }

    private void writeAgeReport() {
        Integer border = readInt("Age threshold:", 30);
        if (border == null) {
            return;
        }

        int[] range = readRange();
        if (range == null) {
            return;
        }

        reportArea.setText("Age threshold: " + border + System.lineSeparator()
                + "Greater: " + queryService.countAgeGreaterThan(border) + System.lineSeparator()
                + "Less: " + queryService.countAgeLessThan(border) + System.lineSeparator()
                + "Range: " + range[0] + ".." + range[1] + System.lineSeparator()
                + "Inside: " + queryService.countAgeInsideRange(range[0], range[1]) + System.lineSeparator()
                + "Outside: " + queryService.countAgeOutsideRange(range[0], range[1]));
    }

    private void writeAppearanceReport() {
        BeardStyle beardStyle = (BeardStyle) JOptionPane.showInputDialog(
                this,
                "Beard style:",
                "Appearance",
                JOptionPane.QUESTION_MESSAGE,
                null,
                BeardStyle.values(),
                BeardStyle.SHORT
        );
        if (beardStyle == null) {
            return;
        }

        HairColor hairColor = (HairColor) JOptionPane.showInputDialog(
                this,
                "Hair color:",
                "Appearance",
                JOptionPane.QUESTION_MESSAGE,
                null,
                HairColor.values(),
                HairColor.Red
        );
        if (hairColor == null) {
            return;
        }

        reportArea.setText("Appearance count: "
                + queryService.countByAppearance(beardStyle, hairColor)
                + System.lineSeparator()
                + "Beard: " + beardStyle
                + System.lineSeparator()
                + "Hair: " + hairColor);
    }

    private void writeAxesReport() {
        reportArea.setText("One axe: " + queryService.countWithAxes(1)
                + System.lineSeparator()
                + "Two axes: " + queryService.countWithAxes(2));
    }

    private void writeTallReport() {
        reportArea.setText(queryService.pickRandomTallViking()
                .map(this::line)
                .orElse("No tall vikings"));
    }

    private void writeLegendaryReport() {
        reportArea.setText(lines(queryService.listLegendaryOwners()));
    }

    private void writeRedReport() {
        reportArea.setText(lines(queryService.listRedVikingsByAge()));
    }

    private void writeIdReport() {
        Integer[] ids = queryService.currentIds();
        String maxId = queryService.findMaxId(ids)
                .map(String::valueOf)
                .orElse("none");

        reportArea.setText("Last ID: " + maxId + System.lineSeparator()
                + "Even IDs: " + joinIds(queryService.collectEvenIds(ids).toArray(Integer[]::new)));
    }

    private void createPack() {
        Integer amount = readInt("Pack size:", 10);
        if (amount == null) {
            return;
        }
        if (amount <= 0) {
            message("Pack size must be positive");
            return;
        }

        List<VikingView> created = queryService.createRandomPack(amount);
        refreshTable.run();
        reportArea.setText("Created: " + created.size());
    }

    private int[] readRange() {
        Integer min = readInt("Min age:", 25);
        if (min == null) {
            return null;
        }

        Integer max = readInt("Max age:", 45);
        if (max == null) {
            return null;
        }

        if (min > max) {
            message("Min age must be less than or equal to max age");
            return null;
        }

        return new int[]{min, max};
    }

    private Integer readInt(String label, int initial) {
        String value = JOptionPane.showInputDialog(this, label, initial);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            message("Value must be an integer");
            return null;
        }
    }

    private String joinIds(Integer[] ids) {
        return Arrays.stream(ids)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    private String lines(List<VikingView> vikings) {
        if (vikings.isEmpty()) {
            return "No data";
        }

        return vikings.stream()
                .map(this::line)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String line(VikingView viking) {
        return "#" + viking.id()
                + " " + viking.name()
                + ", age " + viking.age()
                + ", height " + viking.heightCm()
                + ", hair " + viking.hairColor()
                + ", beard " + viking.beardStyle()
                + ", equipment: " + equipment(viking.equipment());
    }

    private String equipment(List<EquipmentItem> items) {
        if (items == null) {
            return "";
        }

        return items.stream()
                .map(item -> item.name() + " [" + item.quality() + "]")
                .collect(Collectors.joining("; "));
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }
}
