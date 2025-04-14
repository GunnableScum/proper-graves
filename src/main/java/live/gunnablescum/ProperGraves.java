package live.gunnablescum;

import live.gunnablescum.configuration.ConfigurationHandler;
import live.gunnablescum.configuration.ConfigurationScreenHandler;
import live.gunnablescum.configuration.enums.GlowingMode;
import live.gunnablescum.listener.RightClickEntityListener;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class ProperGraves implements ModInitializer {
	public static final String MOD_ID = "proper-graves";
//	public static final GlowingMode GLOWING_MODE = GlowingMode.OWNER_ONLY;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		registerCommand();
		RightClickEntityListener.registerRightClickEvent();
	}

	private void registerCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
			dispatcher.register(literal("proper-graves")
					.then(literal("status").executes(context -> {
						context.getSource().sendMessage(Text.literal("Proper-Graves Status:").fillStyle(Style.EMPTY.withFormatting(Formatting.GOLD)));
						context.getSource().sendMessage(getStatusOfGlowMode());
						return 1;
					}))
					.then(literal("reload").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
						ConfigurationHandler.reloadConfig();
						context.getSource().sendFeedback(() -> Text.literal("Config Reload successful.").fillStyle(Style.EMPTY.withFormatting(Formatting.GREEN)), true);
						return 1;
					}))
					.then(literal("config-gui").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
						if(context.getSource().isExecutedByPlayer()) {
							ServerPlayerEntity player = context.getSource().getPlayer();
							context.getSource().sendFeedback(() -> Text.literal("Editing Proper-Graves Config...").fillStyle(Style.EMPTY.withFormatting(Formatting.GRAY)), true);
							player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerEntity) -> new ConfigurationScreenHandler(syncId, playerInventory), Text.literal("Proper-Graves Config")));
						} else {
							context.getSource().sendFeedback(() -> Text.literal("This command can only be executed by a player.").fillStyle(Style.EMPTY.withFormatting(Formatting.RED)), false);
							return 0;
						}
						return 1;
					}))
			);
		});
	}

	private Text getStatusOfGlowMode() {
		GlowingMode value = ConfigurationHandler.getGlowingMode();
		return Text.literal("Glowing Mode: ").fillStyle(Style.EMPTY.withFormatting(value == GlowingMode.ENABLED ? Formatting.GREEN : Formatting.RED)).append(Text.literal(value.toString()));
	}

}

