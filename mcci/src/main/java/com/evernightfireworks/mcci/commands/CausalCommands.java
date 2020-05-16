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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public class CausalCommands implements ClientCommandPlugin {
    private final Logger logger = LogManager.getFormatterLogger(CausalCommands.class.getName());
    static private final String defaultSessionName = "default";
    private String currentSessionName = defaultSessionName;

    public static void sendError(CommandContext<CottonClientCommandSource> ctx, String s) {
        ctx.getSource().sendError(new LiteralText(s));
    }

    public static void sendError(CommandContext<CottonClientCommandSource> ctx, String sessionName, String failureName, Exception e) {
        ctx.getSource().sendError(new LiteralText("session of '" + sessionName + "' " + failureName + " failed : " + e.getClass().getName() + " - " + e.getLocalizedMessage()));
    }

    public static void feedBack(CommandContext<CottonClientCommandSource> ctx, String s) {
        ctx.getSource().sendFeedback(new LiteralText(s));
    }


    public static void feedBack(CommandContext<CottonClientCommandSource> ctx, String sessionName, String succeedName) {
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
                                                                CausalService.createSession(sessionName);
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
                                                                CausalService.changeSession(sessionName);
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
                                                            if (sessionName.equals(defaultSessionName)) {
                                                                sendError(ctx, "can not delete default session");
                                                                return -1;
                                                            }
                                                            try {
                                                                CausalService.deleteSession(sessionName);
                                                                if (sessionName.equals(this.currentSessionName)) {
                                                                    this.currentSessionName = defaultSessionName;
                                                                }
                                                                feedBack(ctx, sessionName, "delete");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, sessionName, "delete", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("list")
                                        .executes(ctx -> {
                                            try {
                                                String joinedSessions = CausalService.listSessions();
                                                feedBack(ctx, "list all sessions:\n" + joinedSessions);
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, "Failed to list sessions: " + e.getClass().getName() + " - " + e.getLocalizedMessage());
                                                return -1;
                                            }
                                        })
                        )
                        .then(
                                ArgumentBuilders.literal("define")
                                        .then(
                                                ArgumentBuilders.argument("source", string())
                                                .then(
                                                        ArgumentBuilders.argument("target", string())
                                                                .executes(ctx -> {
                                                                    String sessionName = this.currentSessionName;
                                                                    String source = StringArgumentType.getString(ctx, "source");
                                                                    String target = StringArgumentType.getString(ctx, "target");
                                                                    try {
                                                                        CausalService.defineSession(sessionName, source, target);
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
                                ArgumentBuilders.literal("graph")
                                        .then(
                                                ArgumentBuilders.literal("struct")
                                                        .then(
                                                                ArgumentBuilders.argument("content", string())
                                                                        .executes(ctx -> {
                                                                            String sessionName = this.currentSessionName;
                                                                            String content = StringArgumentType.getString(ctx, "content");
                                                                            try {
                                                                                CausalService.setGraph(sessionName, content);
                                                                                feedBack(ctx, this.currentSessionName, "set graph content");
                                                                                return 1;
                                                                            } catch (Exception e) {
                                                                                sendError(ctx, this.currentSessionName, "set graph content", e);
                                                                                return -1;
                                                                            }
                                                                        })
                                                        )
                                        )
                                        .then(
                                                ArgumentBuilders.literal("unobserved")
                                                        .then(ArgumentBuilders.argument("variables", string())
                                                                .executes(ctx -> {
                                                                    String sessionName = this.currentSessionName;
                                                                    String variables = StringArgumentType.getString(ctx, "variables");
                                                                    try {
                                                                        CausalService.setUnobserveds(sessionName, variables);
                                                                        feedBack(ctx, sessionName, "set unobserved variables");
                                                                        return 1;
                                                                    } catch (Exception e) {
                                                                        sendError(ctx, sessionName, "set unobserved variables", e);
                                                                        return -1;
                                                                    }
                                                                })
                                                        )
                                        )
                                        .then(
                                                ArgumentBuilders.literal("edit")
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
                                                                CausalService.generateGraph(this.currentSessionName);
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

                        )
                        .then(
                                ArgumentBuilders.literal("data")
                                        .then(
                                                ArgumentBuilders.literal("edit")
                                                        .executes(ctx -> {
                                                            try {
                                                                CausalService.viewPage(this.currentSessionName, CausalService.CausalPageType.DATA);
                                                                feedBack(ctx, this.currentSessionName, "edit data");
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "edit data", e);
                                                                return -1;
                                                            }
                                                        })
                                        )
                                        .then(
                                                ArgumentBuilders.literal("format")
                                                        .executes(ctx -> {
                                                            try {
                                                                List<String> pair = CausalService.getRecordFormat(this.currentSessionName);
                                                                feedBack(
                                                                        ctx,
                                                                        "source, target: " + pair.get(0)
                                                                                + "\nobserved variables: " + pair.get(1)
                                                                                + "\nunobserved variables: " + pair.get(2)
                                                                );
                                                                return 1;
                                                            } catch (Exception e) {
                                                                sendError(ctx, this.currentSessionName, "format data", e);
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
                                                                                feedBack(ctx, this.currentSessionName, "log new record");
                                                                                return 1;
                                                                            } catch (Exception e) {
                                                                                sendError(ctx, this.currentSessionName, "log new record", e);
                                                                                return -1;
                                                                            }
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                ArgumentBuilders.literal("identify")
                                        .executes(ctx -> {
                                            try {
                                                String result = CausalService.identify(this.currentSessionName);
                                                feedBack(ctx, "result of identify:\n" + result);
                                                return 1;
                                            } catch (Exception e) {
                                                sendError(ctx, this.currentSessionName, "identify problem", e);
                                                return -1;
                                            }
                                        })
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
