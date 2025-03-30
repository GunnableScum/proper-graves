package live.gunnablescum;

import live.gunnablescum.listener.RightClickEntityListener;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProperGraves implements ModInitializer {
	public static final String MOD_ID = "proper-graves";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RightClickEntityListener.registerRightClickEvent();
	}
}