package net.runelite.client.plugins.nmzhelper.Tasks;

import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.nmzhelper.MiscUtils;
import net.runelite.client.plugins.nmzhelper.NMZHelperPlugin;
import net.runelite.client.plugins.nmzhelper.Task;

import java.util.List;
import java.util.stream.Collectors;

public class RockCakeTask extends Task
{
	@Override
	public boolean validate()
	{
		//fail if:

		//not in the nightmare zone
		if (!MiscUtils.isInNightmareZone(client))
			return false;

		//not overloaded
		if (client.getVar(Varbits.NMZ_OVERLOAD) == 0)
			return false;

		//don't have rock cake
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null)
		{
			return false;
		}

		if (inventoryWidget.getWidgetItems()
			.stream()
			.filter(item -> item.getId() == ItemID.DWARVEN_ROCK_CAKE_7510)
			.collect(Collectors.toList()).isEmpty())
			return false;

		//already 1 hp
		if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= 1)
			return false;

		//out of absorption points
		return client.getVar(Varbits.NMZ_ABSORPTION) > 0;
	}

	@Override
	public String getTaskDescription()
	{
		return "Rock caking";
	}

	@Override
	public void onGameTick(GameTick event)
	{
		if (NMZHelperPlugin.rockCakeDelay > 0)
		{
			NMZHelperPlugin.rockCakeDelay--;
			return;
		}

		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null)
		{
			return;
		}

		List<WidgetItem> items = inventoryWidget.getWidgetItems()
			.stream()
			.filter(item -> item.getId() == ItemID.DWARVEN_ROCK_CAKE_7510)
			.collect(Collectors.toList());

		if (items == null || items.isEmpty())
		{
			return;
		}

		WidgetItem item = items.get(0);

		entry = new MenuEntry("Guzzle", "<col=ff9040>Dwarven rock cake", item.getId(), MenuAction.ITEM_THIRD_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false);
		click();
	}
}
