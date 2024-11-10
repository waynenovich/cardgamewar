import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class App extends JFrame {
    private final Deck deck;
    private final Player player1;
    private final Player player2;

    private JTable table;
    private JLabel scoreLabel;
    private JLabel winnerLabel;
    private JScrollPane scrollPane;

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }

    public App() {
        // Initialize and shuffle the deck
        deck = new Deck();
        deck.shuffle();

        // Initialize players
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");

        // Draw 26 cards for each player
        for (int i = 0; i < 26; i++) {
            player1.draw(deck);
            player2.draw(deck);
        }

        // Set up the GUI components
        setupGUI();

        // Start the game logic in the background using SwingWorker
        new GameWorker().execute();
    }

    private void setupGUI() {
        setTitle("WAR Card Game");
        setSize(500, 500);  // Set window size to 500x500
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set up the table with columns for each player's info and round result
        String[] columnNames = {"Player 1 Card", "Player 1 ASCII Art", "Round Winner", "Player 2 ASCII Art", "Player 2 Card"};
        String[][] data = new String[26][5];
        table = new JTable(data, columnNames);
        table.setRowHeight(60);  // Height to fit ASCII art

        // Highlight "Round Winner" cell
        table.getColumnModel().getColumn(2).setCellRenderer(new WinnerCellRenderer());

        // Initialize GUI labels for score and winner
        scoreLabel = new JLabel("Scores -> Player 1: 0, Player 2: 0", SwingConstants.CENTER);
        winnerLabel = new JLabel("", SwingConstants.CENTER);

        // Add the table inside a scroll pane
        scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        add(scoreLabel, BorderLayout.NORTH);
        add(winnerLabel, BorderLayout.SOUTH);

        // Make the frame visible
        setVisible(true);
    }

    // Inner class for handling the game logic in a background thread
    private class GameWorker extends SwingWorker<Void, String[]> {
        private int row = 0;  // Track table row for each round

        @Override
        protected Void doInBackground() throws Exception {
            for (int i = 0; i < 26; i++) {
                // Each player flips a card
                Card card1 = player1.flip();
                Card card2 = player2.flip();

                // Get card names and ASCII art for display
                String player1CardText = card1.getName();
                String player2CardText = card2.getName();
                String player1Art = getAsciiArt(card1.getValue());
                String player2Art = getAsciiArt(card2.getValue());

                // Determine round result and update scores
                String roundResult;
                if (card1.getValue() > card2.getValue()) {
                    player1.incrementScore();
                    roundResult = "Player 1";
                } else if (card1.getValue() < card2.getValue()) {
                    player2.incrementScore();
                    roundResult = "Player 2";
                } else {
                    roundResult = "Draw";
                }

                String scoreText = "Scores -> Player 1: " + player1.getScore() + ", Player 2: " + player2.getScore();

                // Publish round details to update GUI
                publish(new String[]{player1CardText, player1Art, roundResult, player2Art, player2CardText, scoreText});

                // Delay to simulate each turn
                Thread.sleep(1000);  // 1-second delay between rounds
            }
            return null;
        }

        @Override
        protected void process(java.util.List<String[]> chunks) {
            // Get the latest data chunk to update the table and labels
            String[] latestData = chunks.get(chunks.size() - 1);
            table.setValueAt(latestData[0], row, 0); // Player 1 Card
            table.setValueAt(latestData[1], row, 1); // Player 1 ASCII Art
            table.setValueAt(latestData[2], row, 2); // Round Winner
            table.setValueAt(latestData[3], row, 3); // Player 2 ASCII Art
            table.setValueAt(latestData[4], row, 4); // Player 2 Card
            scoreLabel.setText(latestData[5]);  // Update the score display

            // Scroll to the bottom row as each new round is added
            table.scrollRectToVisible(table.getCellRect(row, 0, true));

            row++;  // Move to the next table row
        }

        @Override
        protected void done() {
            // Display the final game result after all rounds are complete
            if (player1.getScore() > player2.getScore()) {
                winnerLabel.setText("Game Winner: Player 1");
            } else if (player1.getScore() < player2.getScore()) {
                winnerLabel.setText("Game Winner: Player 2");
            } else {
                winnerLabel.setText("Game Result: Draw");
            }
        }
    }

    // Custom cell renderer to highlight the "Round Winner" cell
    private class WinnerCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null && !value.equals("Draw")) {
                cell.setBackground(Color.YELLOW);  // Highlight winning round with yellow
            } else {
                cell.setBackground(Color.WHITE);  // Default color for Draw or empty cells
            }
            return cell;
        }
    }

    // Helper method to generate ASCII art for a card
    private String getAsciiArt(int value) {
        String art;
        art = switch (value) {
            case 2 -> " ***** \n*  2  *\n ***** ";
            case 3 -> " ***** \n*  3  *\n ***** ";
            case 4 -> " ***** \n*  4  *\n ***** ";
            case 5 -> " ***** \n*  5  *\n ***** ";
            case 6 -> " ***** \n*  6  *\n ***** ";
            case 7 -> " ***** \n*  7  *\n ***** ";
            case 8 -> " ***** \n*  8  *\n ***** ";
            case 9 -> " ***** \n*  9  *\n ***** ";
            case 10 -> " ***** \n* 10 *\n ***** ";
            case 11 -> " ***** \n* J  *\n ***** ";
            case 12 -> " ***** \n* Q  *\n ***** ";
            case 13 -> " ***** \n* K  *\n ***** ";
            case 14 -> " ***** \n* A  *\n ***** ";
            default -> " ***** \n* ?? *\n ***** ";
        };
        return "<html><pre>" + art + "</pre></html>";  // Format for JLabel display
    }

}
