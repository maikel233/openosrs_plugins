package net.runelite.client.plugins.autothiever;

import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

import java.util.Arrays;

public enum FoodType
{

    Food(ItemID.MANTA_RAY, ItemID.ANGLERFISH,ItemID.SHARK, ItemID.MONKFISH,ItemID.SWORDFISH, ItemID.BASS, ItemID.LOBSTER, ItemID.TUNA, ItemID.SALMON, ItemID.TROUT, ItemID.PIKE, ItemID.SHRIMPS);

    public int[] ItemIDs;

    FoodType(int... ids)
    {
        this.ItemIDs = ids;
    }

    public boolean containsId(int id)
    {
        return Arrays.stream(this.ItemIDs).anyMatch(x -> x == id);
    }

    public WidgetItem getItemFromInventory(Client client)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return null;
        }

        for (WidgetItem item : inventoryWidget.getWidgetItems())
        {
            if (Arrays.stream(ItemIDs).anyMatch(i -> i == item.getId()))
            {
                return item;
            }
        }

        return null;
    }
}
