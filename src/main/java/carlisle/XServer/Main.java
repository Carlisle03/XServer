package carlisle.XServer;

import carlisle.XServer.commands.GamemodeCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class Main {

    public static void start() {
        // Initialize the server
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        // Set the ChunkGenerator
        setupInstanceContainer(instanceContainer);

        // Add an event callback for player configuration
        setupPlayerConfiguration(instanceContainer);

        // Register commands
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GamemodeCommand());

        // Start the server
        minecraftServer.start("0.0.0.0", 25565);
    }

    private static void setupInstanceContainer(InstanceContainer instanceContainer) {
        instanceContainer.setChunkSupplier(LightingChunk::new);
        instanceContainer.setChunkLoader(new AnvilLoader("worlds/world"));
        instanceContainer.saveChunksToStorage();
        instanceContainer.setGenerator(unit -> {
            final Point start = unit.absoluteStart();
            final Point size = unit.size();

            for (int x = 0; x < size.blockX(); x++) {
                for (int z = 0; z < size.blockZ(); z++) {
                    for (int y = 0; y < Math.min(40 - start.blockY(), size.blockY()); y++) {
                        unit.modifier().setBlock(start.add(x, y, z), Block.OAK_WOOD);
                    }
                }
            }
        });
    }

    private static void setupPlayerConfiguration(InstanceContainer instanceContainer) {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });
    }

    public static void main(String[] args) {
        System.out.println("XSERVER RUNNING");
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("- XServer -");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 680);
        frame.setLayout(null);

        JButton stopButton = createButton("Stop", 150, 200, e -> System.exit(0));
        JButton startButton = createButton("Start", 150, 250, e -> {
            System.out.println("Starting");
            start();
        });

        frame.add(stopButton);
        frame.add(startButton);
        frame.setVisible(true);
    }

    private static @NotNull JButton createButton(String text, int x, int y, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 220, 50);
        button.addActionListener(action);
        return button;
    }
}