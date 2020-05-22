package com.evernightfireworks.mcci.commands;

import com.evernightfireworks.mcci.services.CausalService;
import com.evernightfireworks.mcci.services.WebViewService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.text.LiteralText;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class CausalCommands implements ClientCommandPlugin {
    private String currentSessionName = CausalService.SESSION_DEFAULT;
    private String currentDataName = CausalService.DATA_DEFAULT;

    private static void sendError(CommandContext<CottonClientCommandSource> ctx, String s) {
        ctx.getSource().sendError(new LiteralText(s));
    }

    private static void sendError(CommandContext<CottonClientCommandSource> ctx, String sessionName, String failureName, Exception e) {
        ctx.getSource().sendError(new LiteralText("session of '" + sessionName + "' " + failureName + " failed : " + e.getClass().getName() + " - " + e.getLocalizedMessage()));
    }

    private static void feedBack(CommandContext<CottonClientCommandSource> ctx, String s) {
        ctx.getSource().sendFeedback(new LiteralText(s));
    }

    private static void feedBack(CommandContext<CottonClientCommandSource> ctx, String sessionName, String succeedName) {
        ctx.getSource().sendFeedback(new LiteralText("Succeed to " + succeedName + " of '" + sessionName + "'"));
    }

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
                                                                CausalService.sessionCreate(sessionName);
                                                                this.currentSessionName = sessionName;
                                                                feedBack(ctx, sessionName, "create");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, sessionName, "create", e);
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
                                                                CausalService.sessionChange(sessionName);
                                                                this.currentSessionName = sessionName;
                                                                feedBack(ctx, sessionName, "activate");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, sessionName, "activate", e);
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
                                                            try {
                                                                CausalService.sessionDelete(sessionName);
                                                                if (sessionName.equals(this.currentSessionName)) {
                                                                    this.currentSessionName = CausalService.SESSION_DEFAULT;
                                                                }
                                                                feedBack(ctx, sessionName, "delete");
                                                                return 1;
                                                            } catch (UnsupportedOperationException e) {
                                                                sendError(ctx, "can not delete default session");
                                                                return -1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, sessionName, "delete", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("sessions")
                                        .executes(ctx -> {
                                            try {
                                                String joinedSessions = CausalService.sessionList();
                                                feedBack(ctx, "list all sessions:\n" + joinedSessions);
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, "failed to list sessions: " + e.getClass().getName() + " - " + e.getLocalizedMessage());
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("define")
                                        .then(
                                                ArgumentBuilders.argument("exposure", string())
                                                        .then(
                                                                ArgumentBuilders.argument("outcome", string())
                                                                        .executes(ctx -> {
                                                                            String sessionName = this.currentSessionName;
                                                                            String exposure = StringArgumentType.getString(ctx, "exposure");
                                                                            String outcome = StringArgumentType.getString(ctx, "outcome");
                                                                            try {
                                                                                CausalService.sessionDefine(sessionName, exposure, outcome);
                                                                                feedBack(ctx, sessionName, "define problem");
                                                                                return 1;
                                                                            } catch (Exception e) {
                                                                                sendError(ctx, sessionName, "define problem", e);
                                                                                return -1;
                                                                            }
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("struct")
                                        .then(
                                                ArgumentBuilders.argument("content", string())
                                                        .executes(ctx -> {
                                                            String sessionName = this.currentSessionName;
                                                            String content = StringArgumentType.getString(ctx, "content");
                                                            try {
                                                                CausalService.graphContent(sessionName, content);
                                                                feedBack(ctx, this.currentSessionName, "struct graph");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "struct graph", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("latents")
                                        .then(ArgumentBuilders.argument("variables", string())
                                                .executes(ctx -> {
                                                    String sessionName = this.currentSessionName;
                                                    String variables = StringArgumentType.getString(ctx, "variables");
                                                    try {
                                                        CausalService.graphSetLatents(sessionName, variables);
                                                        feedBack(ctx, sessionName, "set latent variables");
                                                        return 1;
                                                    } catch (Exception e) {
                                                        sendError(ctx, sessionName, "set latent variables", e);
                                                        return -1;
                                                    }
                                                })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("graph")
                                        .executes(ctx -> {
                                            try {
                                                CausalService.viewPage(this.currentSessionName, CausalService.CausalPageType.DOT);
                                                feedBack(ctx, this.currentSessionName, "edit graph");
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "edit graph", e);
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("plot")
                                        .executes(ctx -> {
                                            try {
                                                CausalService.graphPlot(this.currentSessionName);
                                                WebViewService.view(
                                                        CausalService.getJupyterURL(this.currentSessionName, CausalService.CausalPageType.IMAGE),
                                                        "graph of '" + this.currentSessionName + "'", true
                                                );
                                                feedBack(ctx, this.currentSessionName, "plot graph");
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "plot graph", e);
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("describe")
                                        .executes(ctx -> {
                                            try {
                                                List<String> pair = CausalService.graphDescribe(this.currentSessionName);
                                                feedBack(
                                                        ctx,
                                                        "exposure, outcome: " + pair.get(0)
                                                                + "\nobserved variables: " + pair.get(1)
                                                                + "\nlatent variables: " + pair.get(2)
                                                );
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "describe data", e);
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("insert")
                                        .then(
                                                ArgumentBuilders.argument("record", string())
                                                        .executes(ctx -> {
                                                            String record = StringArgumentType.getString(ctx, "record");
                                                            try {
                                                                CausalService.dataInsert(this.currentSessionName, this.currentDataName, record);
                                                                feedBack(ctx, this.currentSessionName, "insert new record to table '" + this.currentDataName + "'");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "insert new record to table '" + this.currentDataName + "'", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("data")
                                        .executes(ctx -> {
                                            try {
                                                CausalService.viewPage(this.currentSessionName, CausalService.CausalPageType.DATA, this.currentDataName);
                                                feedBack(ctx, this.currentSessionName, "edit data");
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "edit data", e);
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("checkout")
                                        .then(
                                                ArgumentBuilders.argument("table", string())
                                                        .executes(ctx -> {
                                                            String table = StringArgumentType.getString(ctx, "table");
                                                            try {
                                                                CausalService.dataChange(this.currentSessionName, table);
                                                                feedBack(ctx, this.currentSessionName, "checkout data table '" + table + "'");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "checkout data table '" + table + "'", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("setup")
                                        .then(
                                                ArgumentBuilders.argument("table", string())
                                                        .executes(ctx -> {
                                                            String table = StringArgumentType.getString(ctx, "table");
                                                            try {
                                                                CausalService.dataCreate(this.currentSessionName, table);
                                                                this.currentDataName = table;
                                                                feedBack(ctx, this.currentSessionName, "setup new table '" + table + "'");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "setup new table '" + table + "'", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("tables")
                                        .executes(ctx -> {
                                            try {
                                                String joinedTables = CausalService.dataList(this.currentSessionName);
                                                feedBack(ctx, "list all tables:\n" + joinedTables);
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, "Failed to list tables: " + e.getClass().getName() + " - " + e.getLocalizedMessage());
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("drop")
                                        .then(
                                                ArgumentBuilders.argument("table", string())
                                                        .executes(ctx -> {
                                                            String tableName = StringArgumentType.getString(ctx, "table");
                                                            try {
                                                                CausalService.dataDelete(this.currentSessionName, tableName);
                                                                if (tableName.equals(this.currentDataName)) {
                                                                    this.currentDataName = CausalService.DATA_DEFAULT;
                                                                }
                                                                feedBack(ctx, this.currentSessionName, "delete table '" + tableName + "'");
                                                                return 1;
                                                            } catch (UnsupportedOperationException e) {
                                                                sendError(ctx, "can not delete default table");
                                                                return -1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "delete table '" + tableName + "'", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("identify")
                                        .executes(ctx -> {
                                            try {
                                                String result = CausalService.identify(this.currentSessionName, null);
                                                feedBack(ctx, "result of identify:\n" + result);
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "identify problem", e);
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("custom-identify")
                                        .then(
                                                ArgumentBuilders.argument("path", string())
                                                        .executes(ctx -> {
                                                            String path = StringArgumentType.getString(ctx, "path");
                                                            try {
                                                                String result = CausalService.identify(this.currentSessionName, path);
                                                                feedBack(ctx, "result of identify:\n" + result);
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "identify problem", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("instrument")
                                        .executes(ctx -> {
                                            try {
                                                String result = CausalService.instrument(this.currentSessionName);
                                                feedBack(ctx, "result of instrument:\n" + result);
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "find instrument", e);
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("analyse")
                                        .executes(ctx -> {
                                            try {
                                                CausalService.viewPage(this.currentSessionName, CausalService.CausalPageType.ANALYSIS);
                                                feedBack(ctx, this.currentSessionName, "open analysis lab");
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "open analysis lab", e);
                                                return -1;
                                            }
                                        })
                        )

        );
    }
}
