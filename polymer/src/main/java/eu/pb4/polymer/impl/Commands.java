package eu.pb4.polymer.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.compat.polymc.PolyMcHelpers;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.ui.CreativeTabListUi;
import eu.pb4.polymer.impl.ui.CreativeTabUi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class Commands {
    private static final Text[] ABOUT_PLAYER;
    private static final Text[] ABOUT_COLORLESS;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var command = literal("polymer")
                .requires(PolymerImplUtils.permission("command.core", PolymerImpl.CORE_COMMAND_MINIMAL_OP))
                .executes(Commands::about)
                .then(literal("generate")
                        .requires(PolymerImplUtils.permission("command.generate", 3))
                        .executes(Commands::generate))
                .then(literal("creative")
                        .requires(PolymerImplUtils.permission("command.creative", 0))
                        .then(argument("itemGroup", IdentifierArgumentType.identifier())
                                .suggests((context, builder) -> {
                                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                                    var groups = PolymerUtils.getItemGroups(context.getSource().getPlayer());

                                    CommandSource.forEachMatching(groups, remaining, PolymerItemGroup::getId, group -> builder.suggest(group.getId().toString(), group.getDisplayName()));
                                    return builder.buildFuture();
                                })
                                .executes(Commands::creativeTab)
                        )
                        .executes(Commands::creativeTab));

        if (PolymerImpl.DEVELOPER_MODE) {
            command.then(literal("dev")
                    .then(literal("item-client")
                            .executes(Commands::itemClient))
                    .then(literal("reload-world")
                            .executes((ctx) -> {
                                PolymerUtils.reloadWorld(ctx.getSource().getPlayer());
                                return 0;
                            }))
                    .then(literal("get-mapper")
                            .executes((ctx) -> {
                                ctx.getSource().sendFeedback(new LiteralText(BlockMapper.getFrom(ctx.getSource().getPlayer()).getMapperName()), false);
                                return 0;
                            }))
                    .then(literal("reset-mapper")
                            .executes((ctx) -> {
                                BlockMapper.resetMapper(ctx.getSource().getPlayer());
                                return 0;
                            }))
                    .then(literal("run-sync")
                            .executes((ctx) -> {
                                PolymerSyncUtils.synchronizePolymerRegistries(ctx.getSource().getPlayer().networkHandler);
                                return 0;
                            }))
                    .then(literal("protocol-info")
                            .executes((ctx) -> {
                                ctx.getSource().sendFeedback(new LiteralText("Protocol supported by your client:"), false);
                                for (var entry : PolymerNetworkHandlerExtension.of(ctx.getSource().getPlayer().networkHandler).polymer_getSupportMap().object2IntEntrySet()) {
                                    ctx.getSource().sendFeedback(new LiteralText("- " + entry.getKey() + " = " + entry.getIntValue()), false);
                                }
                                return 0;
                            }))

                    .then(literal("set-pack-status")
                            .then(argument("status", BoolArgumentType.bool())
                                    .executes((ctx) -> {
                                        var status = ctx.getArgument("status", Boolean.class);
                                        PolymerRPUtils.setPlayerStatus(ctx.getSource().getPlayer(), status);
                                        ctx.getSource().sendFeedback(new LiteralText("New resource pack status: " + status), false);
                                        return 0;
                                    })))
                    .then(literal("get-pack-status")
                            .executes((ctx) -> {
                                var status = PolymerRPUtils.hasPack(ctx.getSource().getPlayer());
                                ctx.getSource().sendFeedback(new LiteralText("Resource pack status: " + status), false);
                                return 0;
                            })));
        }

        dispatcher.register(command);
    }

    private static int about(CommandContext<ServerCommandSource> context) {
        for (var text : (context.getSource().getEntity() instanceof ServerPlayerEntity ? ABOUT_PLAYER : ABOUT_COLORLESS)) {
            context.getSource().sendFeedback(text, false);
        }

        return 0;
    }

    private static int creativeTab(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (context.getSource().getPlayer().isCreative()) {
            try {
                var itemGroup = PolymerItemGroup.REGISTRY.get(context.getArgument("itemGroup", Identifier.class));
                if (itemGroup != null && itemGroup.shouldSyncWithPolymerClient(context.getSource().getPlayer())) {
                    new CreativeTabUi(context.getSource().getPlayer(), itemGroup);
                    return 2;
                }
            } catch (Exception e) {
                //
            }

            new CreativeTabListUi(context.getSource().getPlayer());
            return 1;
        } else {
            return 0;
        }
    }

    private static int itemClient(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();
        var itemStack = player.getMainHandStack();

        context.getSource().sendFeedback(new LiteralText(PolymerItemUtils.getPolymerItemStack(itemStack, player).getOrCreateNbt().toString()), false);

        return 1;
    }

    public static int generate(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText("Starting resource pack generation..."), true);

        try {
            if (CompatStatus.POLYMC) {
                Path path = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack-input");
                path.toFile().mkdirs();
                PolyMcHelpers.createResources(path);
            }
        } catch (Exception e) {
            // noop
        }

        boolean success = PolymerRPUtils.build(FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack.zip"));

        if (success) {
            context.getSource().sendFeedback(new LiteralText("Resource pack created successfully! You can find it in game folder as polymer-resourcepack.zip"), true);
        } else {
            context.getSource().sendError(new LiteralText("Found issues while creating resource pack! See logs above for more detail!"));
        }

        return 0;
    }

    static {
        var about = new ArrayList<Text>();
        var aboutBasic = new ArrayList<Text>();
        var output = new ArrayList<Text>();

        try {
            about.add(new LiteralText("Polymer").setStyle(Style.EMPTY.withColor(0xb4ff90).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PolymerImpl.GITHUB_URL))));
            about.add(new LiteralText("Version: ").setStyle(Style.EMPTY.withColor(0xf7e1a7))
                    .append(new LiteralText(PolymerImpl.VERSION).setStyle(Style.EMPTY.withColor(Formatting.WHITE))));

            aboutBasic.addAll(about);
            aboutBasic.add(LiteralText.EMPTY);
            aboutBasic.add(Text.of(PolymerImpl.DESCRIPTION));

            about.add(new LiteralText("")
                    .append(new LiteralText("Contributors")
                            .setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new LiteralText(String.join("\n", PolymerImpl.CONTRIBUTORS))
                                    ))
                            ))
                    .append("")
                    .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
            about.add(LiteralText.EMPTY);

            var desc = new ArrayList<>(List.of(PolymerImpl.DESCRIPTION.split(" ")));

            if (desc.size() > 0) {
                StringBuilder descPart = new StringBuilder();
                while (!desc.isEmpty()) {
                    (descPart.isEmpty() ? descPart : descPart.append(" ")).append(desc.remove(0));

                    if (descPart.length() > 16) {
                        about.add(new LiteralText(descPart.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                        descPart = new StringBuilder();
                    }
                }

                if (descPart.length() > 0) {
                    about.add(new LiteralText(descPart.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                }
            }

            if (PolymerImplUtils.ICON.length > about.size() + 2) {
                int a = 0;
                for (int i = 0; i < PolymerImplUtils.ICON.length; i++) {
                    if (i == (PolymerImplUtils.ICON.length - about.size() - 1) / 2 + a && a < about.size()) {
                        output.add(PolymerImplUtils.ICON[i].shallowCopy().append("  ").append(about.get(a++)));
                    } else {
                        output.add(PolymerImplUtils.ICON[i]);
                    }
                }
            } else {
                Collections.addAll(output, PolymerImplUtils.ICON);
                output.addAll(about);
            }
        } catch (Exception e) {
            e.printStackTrace();
            var invalid = new LiteralText("/!\\ [ Invalid about mod info ] /!\\").setStyle(Style.EMPTY.withColor(0xFF0000).withItalic(true));

            output.add(invalid);
            about.add(invalid);
        }

        ABOUT_PLAYER = output.toArray(new Text[0]);
        ABOUT_COLORLESS = aboutBasic.toArray(new Text[0]);
    }
}
