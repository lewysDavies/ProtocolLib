package com.comphenix.protocol;

import java.util.Collections;
import java.util.List;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.utility.Constants;

import net.minecraft.server.v1_16_R2.DispenserRegistry;
import net.minecraft.server.v1_16_R2.WorldServer;

import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_16_R2.util.Versioning;
import org.spigotmc.SpigotWorldConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Used to ensure that ProtocolLib and Bukkit is prepared to be tested.
 *
 * @author Kristian
 */
public class BukkitInitialization {
	private static final BukkitInitialization instance = new BukkitInitialization();

	private BukkitInitialization() {
		System.out.println("Created new BukkitInitialization on " + Thread.currentThread().getName());
	}

	private boolean initialized;
	private boolean packaged;

	public static synchronized void initializePackage() {
		instance.setPackage();
	}

	public static synchronized void initializeItemMeta() {
		instance.initialize();
	}

	/**
	 * Initialize Bukkit and ProtocolLib such that we can perfrom unit testing
	 */
	private void initialize() {
		if (!initialized) {
			// Denote that we're done
			initialized = true;

			initializePackage();

			try {
				LogManager.getLogger();
			} catch (Throwable ex) {
				// Happens only on my Jenkins, but if it errors here it works when it matters
				ex.printStackTrace();
			}

			DispenserRegistry.init();

			// Mock the server object
			Server mockedServer = mock(Server.class);

			when(mockedServer.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Minecraft"));
			when(mockedServer.getName()).thenReturn("Mock Server");
			when(mockedServer.getVersion()).thenReturn(CraftServer.class.getPackage().getImplementationVersion());
			when(mockedServer.getBukkitVersion()).thenReturn(Versioning.getBukkitVersion());

			when(mockedServer.getItemFactory()).thenReturn(CraftItemFactory.instance());
			when(mockedServer.isPrimaryThread()).thenReturn(true);

			WorldServer nmsWorld = mock(WorldServer.class);

			SpigotWorldConfig mockWorldConfig = mock(SpigotWorldConfig.class);

			try {
				FieldUtils.writeField(nmsWorld.getClass().getField("spigotConfig"), nmsWorld, mockWorldConfig, true);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException(ex);
			}

			CraftWorld world = mock(CraftWorld.class);
			when(world.getHandle()).thenReturn(nmsWorld);

			List<World> worlds = Collections.singletonList(world);
			when(mockedServer.getWorlds()).thenReturn(worlds);

			// Inject this fake server
			Bukkit.setServer(mockedServer);
		}
	}

	/**
	 * Ensure that package names are correctly set up.
	 */
	private void setPackage() {
		if (!packaged) {
			packaged = true;

			try {
				LogManager.getLogger();
			} catch (Throwable ex) {
				// Happens only on my Jenkins, but if it errors here it works when it matters
				ex.printStackTrace();
			}

			Constants.init();
		}
	}
}