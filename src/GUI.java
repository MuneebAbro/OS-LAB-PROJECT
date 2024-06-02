import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class GUI {
    private JFrame frame;
    private JPanel mainPanel;
    private CustomPanel chartPanel;
    private JScrollPane tablePane;
    private JScrollPane chartPane;
    private JTable table;
    private JButton addBtn;
    private JButton removeBtn;
    private JButton computeBtn;
    private JLabel wtLabel;
    private JLabel wtResultLabel;
    private JLabel tatLabel;
    private JLabel tatResultLabel;
    private JComboBox<String> option;
    private DefaultTableModel model;
    private int processCounter;

    public GUI() {
        processCounter = 1;
        model = new DefaultTableModel(new String[]{"Process", "AT", "BT", "Priority", "WT", "TAT"}, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        tablePane = new JScrollPane(table);
        tablePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "Process Table"));

        addBtn = createButton("Add", Color.BLUE);
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.addRow(new String[]{String.valueOf(processCounter++), "", "", "", "", ""});
            }
        });

        removeBtn = createButton("Remove", Color.RED);
        removeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row > -1) {
                    model.removeRow(row);
                }
            }
        });

        chartPanel = new CustomPanel();
        chartPanel.setPreferredSize(new Dimension(450, 100));
        chartPanel.setBackground(Color.WHITE);
        chartPane = new JScrollPane(chartPanel);
        chartPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "Gantt Chart"));
        chartPane.setPreferredSize(new Dimension(450, 100));

        wtLabel = createLabel("Average Waiting Time:");
        tatLabel = createLabel("Average Turn Around Time:");
        wtResultLabel = createLabel("");
        tatResultLabel = createLabel("");

        option = new JComboBox<>(new String[]{"FCFS", "SJF", "SRT", "Priority(NP)", "Priority(P)", "RR"});

        computeBtn = createButton("Compute", Color.BLUE);
        computeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) option.getSelectedItem();
                CPUScheduler scheduler = getScheduler(selected);

                if (scheduler != null) {
                    populateScheduler(scheduler, selected);
                    scheduler.process();

                    updateTableAndLabels(scheduler);
                    chartPanel.setTimeline(scheduler.getTimeline());
                    chartPanel.revalidate();
                }
            }
        });

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.weighty = 1;
        mainPanel.add(tablePane, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        mainPanel.add(addBtn, gbc);

        gbc.gridx = 1;
        mainPanel.add(removeBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        mainPanel.add(chartPane, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        mainPanel.add(wtLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(wtResultLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(tatLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(tatResultLabel, gbc);

        gbc.gridx = 2;
        mainPanel.add(option, gbc);

        gbc.gridx = 3;
        mainPanel.add(computeBtn, gbc);

        frame = new JFrame("CPU Scheduler Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Changed font to Arial and set it to bold
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color.darker(), 1),
                new EmptyBorder(5, 10, 5, 10)));
        return button;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14)); // Changed font to Arial and set it to bold
        return label;
    }


    private CPUScheduler getScheduler(String selected) {
        switch (selected) {
            case "FCFS":
                return new FirstComeFirstServe();
            case "SJF":
                return new ShortestJobFirst();
            case "SRT":
                return new ShortestRemainingTime();
            case "Priority(NP)":
                return new PriorityNonPreemptive();
            case "Priority(P)":
                return new PriorityPreemptive();
            case "RR":
                String tq = JOptionPane.showInputDialog("Time Quantum");
                if (tq == null) {
                    return null;
                }
                RoundRobin scheduler = new RoundRobin();
                scheduler.setTimeQuantum(Integer.parseInt(tq));
                return scheduler;
            default:
                return null;
        }
    }

    private void populateScheduler(CPUScheduler scheduler, String selected) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String process = (String) model.getValueAt(i, 0);
            int at = Integer.parseInt((String) model.getValueAt(i, 1));
            int bt = Integer.parseInt((String) model.getValueAt(i, 2));
            int pl = 1;

            if (selected.startsWith("Priority")) {
                pl = !model.getValueAt(i, 3).equals("") ? Integer.parseInt((String) model.getValueAt(i, 3)) : 1;
            }

            scheduler.add(new Row(process, at, bt, pl));
        }
    }

    private void updateTableAndLabels(CPUScheduler scheduler) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String process = (String) model.getValueAt(i, 0);
            Row row = scheduler.getRow(process);
            model.setValueAt(row.getWaitingTime(), i, 4);
            model.setValueAt(row.getTurnaroundTime(), i, 5);
        }

        wtResultLabel.setText(String.format("%.2f", scheduler.getAverageWaitingTime()));
        tatResultLabel.setText(String.format("%.2f", scheduler.getAverageTurnAroundTime()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }

    class CustomPanel extends JPanel {
        private List<Event> timeline;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (timeline != null) {
                int width = 30 * timeline.size();
                setPreferredSize(new Dimension(width, 75));

                for (int i = 0; i < timeline.size(); i++) {
                    Event event = timeline.get(i);
                    int x = 30 * i;
                    int y = 20;

                    g.drawRect(x, y, 30, 30);
                    g.setFont(new Font("Arial", Font.BOLD, 13)); // Changed font to Arial and set it to bold
                    g.drawString(event.getProcessName(), x + 5, y + 20);
                    g.setFont(new Font("Arial", Font.PLAIN, 11)); // Changed font to Arial and set it to plain
                    g.drawString(Integer.toString(event.getStartTime()), x, y + 45);

                    if (i == timeline.size() - 1) {
                        g.drawString(Integer.toString(event.getFinishTime()), x + 30, y + 45);
                    }
                }
            }
        }

        public void setTimeline(List<Event> timeline) {
            this.timeline = timeline;
            repaint();
        }
    }
}

