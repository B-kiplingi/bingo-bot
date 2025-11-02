package com.example.bot;

import com.example.bingo.BingoCard;
import com.example.bingo.BingoManager;
import com.example.bingo.BingoRound;
import com.example.bingo.BingoSource;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class BotListener extends ListenerAdapter {
    private final BingoManager manager = new BingoManager();

    public BotListener() {
        manager.load();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "bingo-start" -> {
                var channels = event.getGuild().getTextChannelsByName("pool", true);
                if (channels.isEmpty()) {
                    event.reply("❌ No channel named 'pool' found!").setEphemeral(true).queue();
                    return;
                }

                List<String> pool = BingoSource.loadItemsFromChannel(channels.getFirst());

                // Check if enough items loaded
                if (pool.size() < 25) {
                    event.reply("❌ Not enough items in pool (need at least 25, found " + pool.size() + ")").setEphemeral(true).queue();
                    return;
                }

                manager.startNewRound(pool);
                manager.onPlayerAction();

                event.reply("✅ New bingo round started with " + pool.size() + " items! Use `/bingo-join` to get your card!").queue();
            }
            case "bingo-join" -> {
                BingoRound round = manager.getCurrentRound();
                if (round == null) {
                    event.reply("No active round.").setEphemeral(true).queue();
                    return;
                }
                BingoCard card = round.addPlayer(event.getUser().getIdLong());

                manager.onPlayerAction();

                File image = card.getCardMessage();
                event.replyFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(image, event.getUser().getIdLong() + ".png"))
                        .setContent("Here’s your Bingo card!")
                        .queue(response -> {
                            // Store the message ID after sending
                            response.retrieveOriginal().queue(message -> card.setMessageInfo(
                                    message.getId(),
                                    event.getChannel().getId()
                            ));
                        });
            }
            case "bingo-card" -> {
                BingoRound round = manager.getCurrentRound();
                if (round == null) {
                    event.reply("❌ No active round right now!").setEphemeral(true).queue();
                    return;
                }

                BingoCard card = round.getPlayer(event.getUser().getIdLong());
                if (card == null) {
                    event.reply("❌ You haven’t joined this round yet.").setEphemeral(true).queue();
                    return;
                }

                File image = card.getCardMessage();
                event.getChannel().sendFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(image, event.getUser().getIdLong() + ".png"))
                        .setContent("Here’s your Bingo card!")
                        .queue(message -> {
                            // Store the message ID after sending
                            card.setMessageInfo(
                                    message.getId(),
                                    event.getChannel().getId()
                            );
                        });
            }
            case "bingo-check" -> {
                String item = event.getOption("item").getAsString();

                BingoRound round = manager.getCurrentRound();
                if (round == null) {
                    event.reply("❌ No active round right now!").setEphemeral(true).queue();
                    return;
                }

                BingoCard card = round.getPlayer(event.getUser().getIdLong());
                if (card == null) {
                    event.reply("❌ You haven’t joined this round yet.").setEphemeral(true).queue();
                    return;
                }

                if (card.check(item)) {
                    if (card.getMessageId() != null && card.getChannelId() != null) {
                        System.out.println("editing message");
                        File image = card.getCardMessage();

                        event.getChannel().retrieveMessageById(card.getMessageId())
                                .queue(
                                        msg -> msg.editMessage("Here's your Bingo card!")
                                                .setFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(image, event.getUser().getIdLong() + ".png"))
                                                .queue(
                                                        success -> System.out.println("Message edited!"),
                                                        error -> System.out.println("Edit failed: " + error.getMessage())
                                                ),
                                        error -> System.out.println("Retrieve failed") // Ignore if message was deleted
                                );
                    } else {
                        System.out.println("no message found");
                    }
                    if (card.hasWon()) {
                        if(manager.isActive()) {
                            manager.endRound(event.getUser().getIdLong());
                            event.reply("🎉 **BINGO!** You won! Congratulations!").setEphemeral(true).queue();
                            // announce to the channel
                            try (InputStream stream = getClass().getResourceAsStream("/res/winner.jpg")) {
                                if (stream == null) {
                                    event.reply("❌ Could not find the image inside the jar!").setEphemeral(true).queue();
                                    return;
                                }

                                event.getChannel().sendMessage(event.getUser().getAsMention() + " congratulation, you have been české-dráhyed the most!").addFiles(FileUpload.fromData(stream, "win.jpg")).queue();
                            } catch (Exception e) {

                            }
                        } else {
                            event.reply("You weren't the first, unfortunately.").setEphemeral(true).queue();
                        }
                    } else {
                        event.reply("Marked **" + item + "**!").setEphemeral(true).queue();
                    }
                } else {
                    event.reply("Couldn't find `" + item + "` on your card.").setEphemeral(true).queue();
                }

                manager.onPlayerAction();
            }
        }
    }
}

