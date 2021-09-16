package net.runelite.client.plugins.autothiever;

import com.google.inject.Provides;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;

@Extension
@PluginDescriptor(
	name = "Auto Thiever",
	description = "Automatically thieves npcs",
	tags = {"auto", "thiever", "thieving", "skill", "skilling"},
	enabledByDefault = false
)
public class AutoThieverPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private AutoThieverConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AutoThieverOverlay overlay;

	@Inject
	private ItemManager itemManager;

	private MenuEntry entry;

	private Random r = new Random();

	private int nextOpenPouchCount;
	private boolean emptyPouches = false;
	boolean pluginStarted = false;
	private int tickDelay = 0;

	@Provides
	AutoThieverConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(AutoThieverConfig.class);
	}

	public int Tries; //Safe fail

	//Overlay stuff
	public String status = "initializing...";
	public long start; // Time when we start the script
	public int startXP;
	public int CurrentXP;


	@Override
	protected void startUp() throws Exception
	{
		Tries = 0;
		nextOpenPouchCount = getRandom(1, 28);
		pluginStarted = false;
		overlayManager.add(overlay);
		status = "initializing...";

		start = System.currentTimeMillis();
		startXP = client.getSkillExperience(Skill.THIEVING);
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginStarted = false;
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked event)
	{
		if (!event.getGroup().equals("autothiever"))
		{
			return;
		}

		if (event.getKey().equals("startButton"))
		{
			status = ("AutoThiever is running!");
			pluginStarted = true;
			nextOpenPouchCount = getRandom(1, 28);
		}
		else if (event.getKey().equals("stopButton"))
		{
			status = ("AutoThiever stopped!");
			pluginStarted = false;
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		final String message = event.getMessage();

		if (event.getType() == ChatMessageType.SPAM)
		{
			if (message.startsWith("You pickpocket") || message.startsWith("You pick-pocket") || message.startsWith("You steal") || message.startsWith("You successfully pick-pocket") || message.startsWith("You successfully pick") || message.startsWith("You successfully steal") || message.startsWith("You pick the knight") || message.startsWith("You pick the Elf"))
			{
				tickDelay = 0;
			}
			else if (message.startsWith("You fail to pick") || message.startsWith("You fail to steal"))
			{
				tickDelay = getRandom(config.clickDelayMin(), config.clickDelayMax());
			}
			else if (message.startsWith("You open all of the pouches"))
			{
				emptyPouches = false;
				nextOpenPouchCount = getRandom(1, 28);
			}

		}
		else if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (message.startsWith("You need to empty your"))
			{
				emptyPouches = true;
			}

		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (tickDelay > 0) {
			tickDelay--;
			return;
		}

		if (!pluginStarted)
			return;

		if (shouldEat())
		{
			eat();
			return;
		}

		handleRandomPouchOpening();

		if (emptyPouches)
		{
			openPouches();
			return;
		}

		NPC npc = new NPCQuery()
			.idEquals(config.npcId())
			.result(client)
			.nearestTo(client.getLocalPlayer());

		if (npc == null)
		{
			return;
		}


		status = ("Pickpocketing: " + npc.getName());
		//String option, String target, int identifier, int opcode, int param0, int param1, boolean forceLeftClick
		entry = new MenuEntry("Pickpocket", "<col=ffff00>" + npc.getName() + "<col=ff00>  (level-" + npc.getCombatLevel() + ")", npc.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0, false);
		click();
		tickDelay = 1;

		CurrentXP = (startXP - (client.getSkillExperience(Skill.THIEVING)));
	}

	public void handleRandomPouchOpening()
	{
		WidgetItem item = getInventoryItem(ItemID.COIN_POUCH, ItemID.COIN_POUCH_22522, ItemID.COIN_POUCH_22523, ItemID.COIN_POUCH_22524,
			ItemID.COIN_POUCH_22525, ItemID.COIN_POUCH_22526, ItemID.COIN_POUCH_22527, ItemID.COIN_POUCH_22528,
			ItemID.COIN_POUCH_22529, ItemID.COIN_POUCH_22530, ItemID.COIN_POUCH_22531, ItemID.COIN_POUCH_22532,
			ItemID.COIN_POUCH_22533, ItemID.COIN_POUCH_22534, ItemID.COIN_POUCH_22535, ItemID.COIN_POUCH_22536,
			ItemID.COIN_POUCH_22537, ItemID.COIN_POUCH_22538);

		if (item == null)
		{
			return;
		}

		if (item.getQuantity() >= nextOpenPouchCount)
		{
			emptyPouches = true;
		}
	}

	public void openPouches()
	{
		WidgetItem item = getInventoryItem(ItemID.COIN_POUCH, ItemID.COIN_POUCH_22522, ItemID.COIN_POUCH_22523, ItemID.COIN_POUCH_22524,
			ItemID.COIN_POUCH_22525, ItemID.COIN_POUCH_22526, ItemID.COIN_POUCH_22527, ItemID.COIN_POUCH_22528,
			ItemID.COIN_POUCH_22529, ItemID.COIN_POUCH_22530, ItemID.COIN_POUCH_22531, ItemID.COIN_POUCH_22532,
			ItemID.COIN_POUCH_22533, ItemID.COIN_POUCH_22534, ItemID.COIN_POUCH_22535, ItemID.COIN_POUCH_22536,
			ItemID.COIN_POUCH_22537, ItemID.COIN_POUCH_22538);

		if (item == null)
		{
			return;
		}

		//String option, String target, int identifier, int opcode, int param0, int param1, boolean forceLeftClick
		//entry = new MenuEntry("Open-all", "<col=ff9040>" + itemManager.getItemDefinition(item.getId()).getName(), item.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false);
		status = ("Opening pouches...");
		entry =  new MenuEntry("Open-all", "Open-all", item.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item.getIndex(), 9764864, false);
		click();
		tickDelay = 1;
	}

	public WidgetItem getFoodItem()
	{
		WidgetItem item;

		item = FoodType.Food.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}

	public void eat()
	{

		WidgetItem FoodItem = getFoodItem();

		if (FoodItem == null)
		{
			return;
		}

		status = ("Eating food!");

			clientThread.invoke(() ->
					client.invokeMenuAction(
							"Eat",
							"<col=ff9040>Food",
							FoodItem.getId(),
							MenuAction.ITEM_FIRST_OPTION.getId(),
							FoodItem.getIndex(),
							WidgetInfo.INVENTORY.getPackedId()
					)
			);

		tickDelay = 4;
	}

	public boolean shouldEat()
	{
		switch (config.hpCheckStyle())
		{
			case EXACT_HEALTH:
				return client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.hpToEat();

			case PERCENTAGE:
				return (((float)client.getBoostedSkillLevel(Skill.HITPOINTS) / (float)client.getRealSkillLevel(Skill.HITPOINTS)) * 100.f) <= (float)config.hpToEat();
		}

		return false;
	}

	public int getRandom(int min, int max)
	{
		return r.nextInt((max - min) + 1) + min;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (entry != null)
		{
			event.setMenuEntry(entry);
		}

		entry = null;
	}

	public void click()
	{
		Point pos = client.getMouseCanvasPosition();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (pos.getX() * width), (int) (pos.getY() * height));
			client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
			client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
			client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
			return;
		}

		client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
		client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
		client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
	}

	public WidgetItem getInventoryItem(int... ids)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null)
		{
			return null;
		}

		for (WidgetItem item : inventoryWidget.getWidgetItems())
		{
			if (Arrays.stream(ids).anyMatch(i -> i == item.getId()))
			{
				return item;
			}
		}

		return null;
	}
}