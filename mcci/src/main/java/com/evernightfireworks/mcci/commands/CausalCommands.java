package com.evernightfireworks.mcci.commands;

import com.evernightfireworks.mcci.services.CausalService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;

import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class CausalCommands implements ClientCommandPlugin {
    private final Logger logger = LogManager.getFormatterLogger(CausalCommands.class.getName());
    static private final String defaultSessionName = "default";
    private String currentSessionName = defaultSessionName;

    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
        dispatcher.register(
                ArgumentBuilders.literal("mcci")
                        .then(
                                ArgumentBuilders.literal("create")
                                        .then(
                                                ArgumentBuilders.argument("session", string())
                                                        .executes(ctx -> {
                                                            String sessionName = StringArgumentType.getString(ctx, "session");
                                                            try {
                                                                CausalService.createSession(sessionName);
                                                                CausalService.viewPage(sessionName, CausalService.CausalPageType.GRAPH);
                                                                this.currentSessionName = sessionName;
                                                                ctx.getSource().sendFeedback(new LiteralText(
                                                                        "Succeed to create '" + sessionName + "'"
                                                                ));
                                                                return 1;
                                                            } catch (FileAlreadyExistsException e) {
                                                                ctx.getSource().sendError(new LiteralText("session '"
                                                                        + sessionName + "' existed"));
                                                                return -1;
                                                            } catch (IOException e) {
                                                                ctx.getSource().sendError(new LiteralText("session '"
                                                                        + sessionName + "' created failed: " + e.getClass().getName()
                                                                        + "," + e.getLocalizedMessage()
                                                                ));
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("activate")
                                        .then(
                                                ArgumentBuilders.argument("session", string())
                                                        .executes(ctx -> {
                                                            String sessionName = StringArgumentType.getString(ctx, "session");
                                                            try {
                                                                CausalService.changeSession(sessionName);
                                                                this.currentSessionName = sessionName;
                                                                ctx.getSource().sendFeedback(new LiteralText(
                                                                        "Succeed to activate '" + sessionName + "'"
                                                                ));
                                                                return 1;
                                                            } catch (FileNotFoundException e) {
                                                                ctx.getSource().sendError(new LiteralText("session '"
                                                                        + sessionName + "' not existed"));
                                                                return -1;
                                                            } catch (NotDirectoryException e) {
                                                                ctx.getSource().sendError(new LiteralText("session '"
                                                                        + sessionName + "' is in invalid state"));
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("delete")
                                        .then(
                                                ArgumentBuilders.argument("session", string())
                                                        .executes(ctx -> {
                                                            String sessionName = StringArgumentType.getString(ctx, "session");
                                                            if (sessionName.equals(defaultSessionName)) {
                                                                ctx.getSource().sendError(new LiteralText("can not delete default session"));
                                                                return -1;
                                                            }
                                                            try {
                                                                CausalService.deleteSession(sessionName);
                                                                if (sessionName.equals(this.currentSessionName)) {
                                                                    this.currentSessionName = defaultSessionName;
                                                                }
                                                                ctx.getSource().sendFeedback(new LiteralText(
                                                                        "Succeed to delete '" + sessionName + "'"
                                                                ));
                                                                return 1;
                                                            } catch (IOException e) {
                                                                ctx.getSource().sendError(new LiteralText("session '"
                                                                        + sessionName + "' delete failed: " + e.getClass().getName()
                                                                        + "," + e.getLocalizedMessage()
                                                                ));
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("format")
                                        .executes(ctx -> {
                                            try {
                                                Pair<String,String> pair = CausalService.getRecordHeader(this.currentSessionName);
                                                ctx.getSource().sendFeedback(new LiteralText(
                                                        "common variables: " + pair.getLeft() + "\nunobserved cofounders: " + pair.getRight() + ""
                                                ));
                                                return 1;
                                            } catch (Exception e) {
                                                ctx.getSource().sendError(new LiteralText(
                                                        "failed to show record format: "
                                                                + e.getClass().getName() + ","
                                                                + e.getLocalizedMessage()
                                                ));
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("graph")
                                        .executes(ctx -> {
                                            try {
                                                CausalService.viewPage(this.currentSessionName, CausalService.CausalPageType.GRAPH);
                                                ctx.getSource().sendFeedback(new LiteralText(
                                                        "Succeed to edit graph of '" + this.currentSessionName + "'"
                                                ));
                                                return 1;
                                            } catch (IOException e) {
                                                ctx.getSource().sendError(new LiteralText(
                                                        e.getLocalizedMessage()
                                                ));
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("log")
                                        .then(
                                                ArgumentBuilders.argument("record", string())
                                                        .executes(ctx -> {
                                                            String record = StringArgumentType.getString(ctx, "record");
                                                            try {
                                                                CausalService.appendRecord(this.currentSessionName, record);
                                                                ctx.getSource().sendFeedback(new LiteralText(
                                                                        "Succeed to log new record of '" + this.currentSessionName + "'"
                                                                ));
                                                                return 1;
                                                            } catch (IOException e) {
                                                                ctx.getSource().sendError(new LiteralText(
                                                                        "failed to append new record: " + e.getClass().getName()
                                                                                + ", " + e.getLocalizedMessage()
                                                                ));
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("data")
                                        .executes(ctx -> {
                                            try {
                                                CausalService.viewPage(this.currentSessionName, CausalService.CausalPageType.DATA);
                                                ctx.getSource().sendFeedback(new LiteralText(
                                                        "Succeed to edit data of '" + this.currentSessionName + "'"
                                                ));
                                                return 1;
                                            } catch (IOException e) {
                                                ctx.getSource().sendError(new LiteralText(
                                                        e.getLocalizedMessage()
                                                ));
                                                return -1;
                                            }
                                        })
                        )

        );
    }
}
