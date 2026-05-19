package ru.mephi.vikingdemo.gui;

import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.model.VikingView;
import ru.mephi.vikingdemo.service.VikingQueryService;
import ru.mephi.vikingdemo.service.VikingService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class VikingDesktopFrame extends JFrame {

    private final VikingService vikingService;
    private final VikingQueryService queryService;
    private final VikingTableModel tableModel = new VikingTableModel();
    private JTable vikingTable;

    public VikingDesktopFrame(VikingService vikingService, VikingQueryService queryService) {
        this.vikingService = vikingService;
        this.queryService = queryService;

        setTitle("Viking Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1000, 420));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("Viking Demo", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        add(header, BorderLayout.NORTH);

        vikingTable = new JTable(tableModel);
        vikingTable.setRowHeight(28);
        add(new JScrollPane(vikingTable), BorderLayout.CENTER);

        JButton createButton = new JButton("Create random viking");
        createButton.addActionListener(event -> onCreateViking());
        JButton addButton = new JButton("Add viking");
        addButton.addActionListener(event -> onAddCustomViking());
        JButton deleteButton = new JButton("Delete viking");
        deleteButton.addActionListener(event -> onDeleteSelectedViking());
        JButton updateButton = new JButton("Update viking");
        updateButton.addActionListener(event -> onUpdateSelectedViking());
        JButton reportButton = new JButton("Query report");
        reportButton.addActionListener(event -> onOpenQueryReport());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(createButton);
        bottomPanel.add(addButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(reportButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        onInit();
    }

    private void onCreateViking() {
        VikingView viking = vikingService.createRandomViking();
        tableModel.addViking(viking);
    }
    
    public void addNewViking(VikingView viking){
        tableModel.addViking(viking);
    }

    private void onAddCustomViking() {
        Viking viking = promptViking(null);
        if (viking == null) {
            return;
        }

        try {
            VikingView created = vikingService.addViking(viking);
            tableModel.addViking(created);
            showMessage("Added");
        } catch (RuntimeException exception) {
            showMessage("Error: " + exception.getMessage());
        }
    }

    private void onDeleteSelectedViking() {
        Integer row = selectedModelRow();
        if (row == null) {
            return;
        }

        VikingView selected = tableModel.getVikingAt(row);
        int result = JOptionPane.showConfirmDialog(
                this,
                "Delete " + selected.name() + "?",
                "Delete viking",
                JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        if (vikingService.deleteViking(selected.id())) {
            tableModel.removeViking(row);
        } else {
            showMessage("Viking not found");
            refreshTable();
        }
    }

    private void onUpdateSelectedViking() {
        Integer row = selectedModelRow();
        if (row == null) {
            return;
        }

        VikingView old = tableModel.getVikingAt(row);
        Viking update = promptViking(old);
        if (update == null) {
            return;
        }

        Optional<VikingView> updated = vikingService.updateViking(old.id(), update);
        if (updated.isPresent()) {
            tableModel.updateViking(row, updated.get());
            showMessage("Updated");
        } else {
            showMessage("Viking not found");
            refreshTable();
        }
    }

    private void onOpenQueryReport() {
        new VikingQueryReportFrame(queryService, this::refreshTable).setVisible(true);
    }

    private void onInit() {
        refreshTable();
    }

    private void refreshTable() {
        tableModel.reload(vikingService.findAll());
    }

    private Integer selectedModelRow() {
        int selectedRow = vikingTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Select a viking first");
            return null;
        }

        return vikingTable.convertRowIndexToModel(selectedRow);
    }

    private Viking promptViking(VikingView initial) {
        String name = promptText("Name:", initial == null ? "" : initial.name());
        if (name == null || name.isBlank()) {
            return null;
        }

        Integer age = promptInteger("Age:", initial == null ? 30 : initial.age());
        if (age == null) {
            return null;
        }

        Integer height = promptInteger("Height (cm):", initial == null ? 180 : initial.heightCm());
        if (height == null) {
            return null;
        }

        HairColor hairColor = (HairColor) JOptionPane.showInputDialog(
                this,
                "Hair color:",
                "Select hair color",
                JOptionPane.QUESTION_MESSAGE,
                null,
                HairColor.values(),
                initial == null ? HairColor.Blond : initial.hairColor()
        );
        if (hairColor == null) {
            return null;
        }

        BeardStyle beardStyle = (BeardStyle) JOptionPane.showInputDialog(
                this,
                "Beard style:",
                "Select beard style",
                JOptionPane.QUESTION_MESSAGE,
                null,
                BeardStyle.values(),
                initial == null ? BeardStyle.SHORT : initial.beardStyle()
        );
        if (beardStyle == null) {
            return null;
        }

        List<EquipmentItem> equipment = promptEquipment(initial == null ? List.of() : initial.equipment());
        if (equipment == null) {
            return null;
        }

        return new Viking(
                name.trim(),
                age,
                height,
                hairColor,
                beardStyle,
                equipment
        );
    }

    private String promptText(String message, String initial) {
        return JOptionPane.showInputDialog(this, message, initial);
    }

    private Integer promptInteger(String message, int initial) {
        String value = JOptionPane.showInputDialog(this, message, initial);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            int number = Integer.parseInt(value.trim());
            if (number <= 0) {
                showMessage("Value must be positive");
                return null;
            }
            return number;
        } catch (NumberFormatException exception) {
            showMessage("Invalid number");
            return null;
        }
    }

    private List<EquipmentItem> promptEquipment(List<EquipmentItem> initial) {
        JTextArea area = new JTextArea(equipmentToText(initial), 6, 28);
        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(area),
                "Equipment: one item per line as name,quality",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            return parseEquipment(area.getText());
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
            return null;
        }
    }

    private String equipmentToText(List<EquipmentItem> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return "Axe,Rare\nShield,Common";
        }

        StringBuilder text = new StringBuilder();
        for (EquipmentItem item : equipment) {
            if (!text.isEmpty()) {
                text.append(System.lineSeparator());
            }
            text.append(item.name()).append(",").append(item.quality());
        }

        return text.toString();
    }

    private List<EquipmentItem> parseEquipment(String text) {
        List<EquipmentItem> equipment = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return equipment;
        }

        String[] lines = text.split("\\R");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            String[] parts = trimmedLine.split(",", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException("Equipment format: name,quality");
            }

            equipment.add(new EquipmentItem(parts[0].trim(), parts[1].trim()));
        }

        return equipment;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
