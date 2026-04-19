import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class RecursiveLister extends JFrame {

    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JButton startButton;
    private JButton quitButton;
    private JLabel titleLabel;
    private JLabel statusLabel;
    private int fileCount;
    private int dirCount;

    public RecursiveLister() {
        initComponents();
        setupLayout();
        setupListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Recursive File Lister");
        setSize(750, 600);
        setMinimumSize(new Dimension(550, 400));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        titleLabel = new JLabel("Recursive File Lister", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 80, 160));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setBackground(new Color(245, 247, 250));
        textArea.setForeground(new Color(30, 30, 30));
        textArea.setMargin(new Insets(8, 10, 8, 10));
        textArea.setText("Select a directory to begin listing files...\n");

        scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(new Color(180, 200, 230), 1)
        ));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        startButton = createStyledButton("Choose Directory", new Color(30, 100, 200), Color.WHITE);
        quitButton  = createStyledButton("Quit",             new Color(200, 50,  50),  Color.WHITE);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(80, 80, 80));
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 40));

        btn.addMouseListener(new MouseAdapter() {
            Color original = bg;
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(original.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 238, 255));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(180, 200, 230)));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 244, 255));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(180, 200, 230)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        btnPanel.setOpaque(false);
        btnPanel.add(startButton);
        btnPanel.add(quitButton);
        bottomPanel.add(btnPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        startButton.addActionListener(e -> chooseDirectory());
        quitButton.addActionListener(e -> System.exit(0));
    }

    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a Directory to List");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            textArea.setText("");
            fileCount = 0;
            dirCount  = 0;

            textArea.append("Directory listing for: " + selectedDir.getAbsolutePath() + "\n");
            textArea.append("=".repeat(60) + "\n\n");

            long startTime = System.currentTimeMillis();
            listFilesRecursively(selectedDir, 0);
            long elapsed = System.currentTimeMillis() - startTime;

            textArea.append("\n" + "=".repeat(60) + "\n");
            textArea.append(String.format("Done!  %d file(s),  %d folder(s)  [%d ms]\n",
                    fileCount, dirCount, elapsed));

            statusLabel.setText(String.format("Found %d file(s) and %d folder(s) in %d ms",
                    fileCount, dirCount, elapsed));

            textArea.setCaretPosition(0);
        }
    }

    private void listFilesRecursively(File directory, int depth) {
        String indent = "  ".repeat(depth);

        File[] entries = directory.listFiles();
        if (entries == null) {
            textArea.append(indent + "[Cannot access: " + directory.getName() + "]\n");
            return;
        }

        java.util.Arrays.sort(entries, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File entry : entries) {
            if (entry.isDirectory()) {
                dirCount++;
                textArea.append(indent + "[DIR]  " + entry.getName() + "/\n");
                listFilesRecursively(entry, depth + 1);   // <-- recursive call
            } else {
                fileCount++;
                long sizeKb = entry.length() / 1024;
                textArea.append(indent + "       " + entry.getName()
                        + "  (" + sizeKb + " KB)\n");
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(RecursiveLister::new);
    }
}
