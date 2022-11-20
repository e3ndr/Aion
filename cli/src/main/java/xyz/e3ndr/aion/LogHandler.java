package xyz.e3ndr.aion;

import org.jetbrains.annotations.NotNull;

import xyz.e3ndr.fastloggingframework.FastLogHandler;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogColor;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class LogHandler extends FastLogHandler {

    @Override
    protected void log(@NotNull String name, @NotNull LogLevel level, @NotNull String raw) {
        if (FastLoggingFramework.isColorEnabled()) {
            System.out.println(
                LogColor.translateToAnsi(
                    String.format("§r%s%s", level.getTextColor(), raw)
                )
            );
        } else {
            System.out.println(raw);
        }
    }

}
