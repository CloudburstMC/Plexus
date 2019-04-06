package com.nukkitx.plexus.console;

import com.nukkitx.plexus.PlexusProxy;
import lombok.RequiredArgsConstructor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

@RequiredArgsConstructor
public class PlexusTerminalConsole extends SimpleTerminalConsole implements Runnable {
    private final PlexusProxy proxy;

    @Override
    protected boolean isRunning() {
        return proxy.isRunning();
    }

    @Override
    protected void runCommand(String s) {

    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        builder.appName("Plexus");
        builder.option(LineReader.Option.HISTORY_BEEP, false);
        builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true);
        builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true);
        return super.buildReader(builder);
    }

    @Override
    protected void shutdown() {
        proxy.shutdown();
    }

    @Override
    public void run() {
        start();
    }
}
