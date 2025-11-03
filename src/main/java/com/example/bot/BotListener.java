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

                if (!manager.isActive()) {
                    event.reply("This round already ended!").setEphemeral(true).queue();
                    return;
                }

                if (manager.getCurrentRound().isPlaying(event.getUser().getIdLong())) {
                    event.reply("You already joined this round!").setEphemeral(true).queue();
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

                //deffer reply to make sure the command gets acknowledged
                event.reply("generating card").setEphemeral(true).queue();

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

                if (!card.check(item)) {
                    event.reply("Couldn't find `" + item + "` on your card.").setEphemeral(true).queue();
                    return;
                }

                if (card.getMessageId() == null || card.getChannelId() == null) {
                    System.out.println("no message found");
                    return;
                }

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

                if (card.hasWon()) {
                    if(manager.isActive()) {
                        manager.endRound(event.getUser().getIdLong());
                        // announce to the channel
                        try (InputStream stream = getClass().getResourceAsStream("/res/winner.jpg")) {
                            if (stream == null) {
                                event.reply("Congrats! We send a funny image your way but there's a výluka on the way.").setEphemeral(true).queue();
                                return;
                            }
                            byte[] bytes = stream.readAllBytes();
                            event.getChannel().sendMessage(event.getUser().getAsMention() + " congratulation, you have been české-dráhyed the most!").addFiles(FileUpload.fromData(bytes, "win.jpg")).queue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            String user = event.getJDA().getUserById(manager.getCurrentRound().getWinnerId()).getAsMention();
                            event.reply(user + " got české-dráhyed harder than you and already won.").setEphemeral(true).queue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        event.reply("Somebody already beat you to it, but české dráhy is not sure who... we're still looking for him");
                    }
                } else {
                    event.reply("Marked **" + item + "**!").setEphemeral(true).queue();
                }
                manager.onPlayerAction();
            }
            case "bingo-uncheck" -> {
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

                boolean winner = card.hasWon();

                if (!card.uncheck(item)) {
                    event.reply("`" + item + "` isn't on your card, or is already unchecked.").setEphemeral(true).queue();
                    return;
                }

                if(winner && !card.hasWon()) {
                    event.getChannel().sendMessage("Newer mind, looks like " + event.getUser().getAsMention() + " is about as reliable as České Dráhy's timetable").queue();
                }

                if (card.getMessageId() == null || card.getChannelId() == null) {
                    System.out.println("no message found");
                    return;
                }

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

                event.reply("Looks like your check arrived at the wrong platform, we've corrected the issue.").setEphemeral(true).queue();

                manager.onPlayerAction();
            }
        }
    }
}

