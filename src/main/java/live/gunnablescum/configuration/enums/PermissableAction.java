package live.gunnablescum.configuration.enums;

import net.minecraft.util.Formatting;

public enum PermissableAction {
    ALLOW("Allow", Formatting.GREEN),
    SERVER_OPERATOR_ONLY("Server Operator Only", Formatting.YELLOW),
    DENY("Denied", Formatting.RED);

    private final String name;
    private final Formatting color;

    PermissableAction(String name, Formatting color) {
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
