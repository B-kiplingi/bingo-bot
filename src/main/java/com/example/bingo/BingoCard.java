package com.example.bingo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.util.TableImageGenerator.generateTableImage;

public class BingoCard {
    private final long userId;
    private final String[][] layout = new String[5][5];
    private final boolean[][] checked = new boolean[5][5];
    private String messageId;
    private String channelId;

    public BingoCard(long userId, List<String> roundItems) {
        this.userId = userId;
        List<String> shuffled = new ArrayList<>(roundItems);
        Collections.shuffle(shuffled);

        for (int i = 0; i < 25; i++) {
            layout[i / 5][i % 5] = shuffled.get(i);
        }
    }

    public boolean check(String item) {
        int[] coords = parseCoords(item);
        int x = coords[0];
        int y = coords[1];

        if( x >= 0 && x < 5 && y >= 0 && y < 5 ) {
            checked[x][y] = true;
            return true;
        }
        return false;
    }

    public File getCardMessage() {
        return generateTableImage(checked, layout, Long.toString(userId));
    }

    public void setMessageInfo(String messageId, String channelId) {
        this.messageId = messageId;
        this.channelId = channelId;

        System.out.println("setMessageInfo: " + messageId + " " + channelId);
    }

    public String getMessageId() {
        return messageId;
    }

    public String getChannelId() {
        return channelId;
    }

    /*    public MessageCreateData cardMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Your Bingo Card");
        embed.setColor(Color.CYAN);

        // Add legend to embed
        StringBuilder legend = new StringBuilder();
        int num = 1;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                String status = checked[i][j] ? "✅" : " ";
                legend.append(status).append(" **").append(num).append(".** ").append(layout[i][j]).append("\n");
                num++;
            }
        }
        embed.setDescription(legend.toString());

        List<ActionRow> rows = new ArrayList<>();
        int cellNum = 1;

        for (int i = 0; i < 5; i++) {
            List<Button> buttons = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                String emoji = checked[i][j] ? "✅" : "⬜";
                String customId = "bingo:" + ((i>9) ? i : "0" + i) + ":" + j;

                ButtonStyle style = checked[i][j] ? ButtonStyle.SUCCESS : ButtonStyle.SECONDARY;
                buttons.add(Button.of(style, customId, String.valueOf(cellNum++), Emoji.fromUnicode(emoji)));
            }
            rows.add(ActionRow.of(buttons));
        }

        return new MessageCreateBuilder()
                .setEmbeds(embed.build())
                .setComponents(rows)
                .build();
    }*/

    public boolean hasWon() {
        for (int i = 0; i < 5; i++) {
            boolean hasWon = true;
            for (int j = 0; j < 5; j++) {
                if (!checked[i][j]) {
                    hasWon = false;
                    break;
                }
            }
            if (hasWon) {return true;}
        }

        for (int i = 0; i < 5; i++) {
            boolean hasWon = true;
            for (int j = 0; j < 5; j++) {
                if (!checked[j][i]) {
                    hasWon = false;
                    break;
                }
            }
            if (hasWon) {return true;}
        }

        boolean hasWon = true;
        for (int i = 0; i < 5; i++) {
            if(!checked[i][i]) {
                hasWon = false;
                break;
            }
        }
        if (hasWon) {return true;}

        hasWon = true;
        for (int i = 0; i < 5; i++) {
            if(!checked[i][4-i]) {
                hasWon = false;
                break;
            }
        }
        if (hasWon) {return true;}
        else {return false;}
    }

    public static int[] parseCoords(String coord) {
        if (coord == null || coord.length() != 2) {
            return null; // Invalid format
        }

        char letter = coord.toUpperCase().charAt(0);
        char number = coord.charAt(1);

        // Convert A-E to 0-4
        int col = letter - 'A';
        // Convert 1-5 to 0-4
        int row = number - '1';

        // Validate range
        if (col < 0 || col > 4 || row < 0 || row > 4) {
            return null; // Out of bounds
        }

        return new int[]{row, col};
    }
}