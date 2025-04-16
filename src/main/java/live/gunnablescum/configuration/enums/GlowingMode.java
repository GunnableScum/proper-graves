package live.gunnablescum.configuration.enums;

import net.minecraft.util.Formatting;

public enum GlowingMode {
	DISABLED("Disabled", Formatting.RED),
	ENABLED("Enabled", Formatting.GREEN),
	OWNER_ONLY("Owner Only", Formatting.YELLOW);

	private final String name;
	private final Formatting color;

	GlowingMode(String name, Formatting color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public Formatting getColor() {
		return color;
	}
}
