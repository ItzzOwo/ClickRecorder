import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main extends JFrame {
    private JButton clickButton;
    private JLabel cpsLabel;
    private JLabel delayLabel;

    private int clickCount;
    private long startTime;
    private PrintWriter logWriter;

    public Main() {
        setTitle("Click Per Second App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 150));

        // Set dark mode look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel logPanel = new JPanel(new GridLayout(2, 1));
        JLabel questionLabel = new JLabel("Would you like to log the click delays?");
        questionLabel.setForeground(Color.DARK_GRAY);
        questionLabel.setFont(questionLabel.getFont().deriveFont(Font.BOLD, 14f));
        logPanel.add(questionLabel);

        JPanel buttonPanel = new JPanel();
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");

        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logDelays();
                showClickGUI();
            }
        });

        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showClickGUI();
            }
        });

        yesButton.setBackground(Color.DARK_GRAY);
        yesButton.setForeground(Color.DARK_GRAY);
        yesButton.setFocusPainted(false);
        yesButton.setFont(yesButton.getFont().deriveFont(Font.BOLD));
        yesButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        noButton.setBackground(Color.DARK_GRAY);
        noButton.setForeground(Color.DARK_GRAY);
        noButton.setFocusPainted(false);
        noButton.setFont(noButton.getFont().deriveFont(Font.BOLD));
        noButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        logPanel.add(buttonPanel);

        mainPanel.add(logPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        setVisible(true);
    }

    private void showClickGUI() {
        getContentPane().removeAll();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(350, 120));
        getContentPane().setBackground(Color.DARK_GRAY);

        clickButton = new JButton("Click Me");
        clickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleClick();
            }
        });
        clickButton.setBackground(Color.DARK_GRAY);
        clickButton.setForeground(Color.DARK_GRAY);
        clickButton.setFocusPainted(false);
        clickButton.setFont(clickButton.getFont().deriveFont(Font.BOLD));
        clickButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        add(clickButton, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        cpsLabel = new JLabel("Clicks per second: ");
        cpsLabel.setForeground(Color.WHITE);
        cpsLabel.setFont(cpsLabel.getFont().deriveFont(Font.BOLD, 14f));
        delayLabel = new JLabel("Delay between clicks: ");
        delayLabel.setForeground(Color.WHITE);
        delayLabel.setFont(delayLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(cpsLabel);
        panel.add(delayLabel);
        panel.setBackground(Color.DARK_GRAY);
        add(panel, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    private void handleClick() {
        if (startTime == 0) {
            startTime = System.nanoTime();
            clickCount = 1;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (clickCount > 0) {
                        double cps = (double) clickCount / 0.5;
                        updateCPSLabel(cps);
                    } else {
                        updateCPSLabel(0.0);
                        updateDelayLabel(0.0);
                    }
                    clickCount = 0;
                    startTime = 0;
                }
            }).start();
        } else {
            long currentTime = System.nanoTime();
            double delay = (double) (currentTime - startTime) / 1000000.0; // Convert to milliseconds
            startTime = currentTime;
            updateDelayLabel(delay);
            logDelay(delay);
            clickCount++;
        }
    }

    private void updateCPSLabel(double cps) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                cpsLabel.setText("Clicks per second: " + String.format("%.4f", cps));
            }
        });
    }

    private void updateDelayLabel(double delay) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                delayLabel.setText("Delay between clicks: " + formatDelay(delay) + " milliseconds");
            }
        });
    }

    private void logDelay(double delay) {
        if (logWriter != null) {
            logWriter.println(formatDelay(delay));
            logWriter.flush();
        }
    }

    private String formatDelay(double delay) {
        // Format milliseconds with nanoseconds padded with leading zeros
        long milliseconds = (long) delay;
        int nanoseconds = (int) ((delay - milliseconds) * 1000000);
        return String.format("%d.%06d", milliseconds, nanoseconds);
    }

    private void logDelays() {
        // Show file explorer to choose file location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose a File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                logWriter = new PrintWriter(new FileWriter(selectedFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // User canceled the file selection, handle accordingly
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Main();
            }
        });
    }
}
