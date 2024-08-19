package globalquake.client;

import java.awt.*;

public enum ClientSocketStatus {

    DISCONNECTED("已断开连接", Color.red),
    CONNECTING("连接中...", Color.yellow),
    CONNECTED("已连接", Color.green);

    private final String name;
    private final Color color;

    ClientSocketStatus(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
