package net.runelite.client.plugins.nmzhelper;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
public class NMZHelperOverlay extends Overlay
{
	private final Client client;
	private final NMZHelperPlugin plugin;
	private final NMZHelperConfig config;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	private NMZHelperOverlay(Client client, NMZHelperPlugin plugin, NMZHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		this.setPriority(OverlayPriority.HIGHEST);
		this.setPosition(OverlayPosition.BOTTOM_LEFT);
		this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "NMZ Helper Overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin == null)
			return null;

		if (!plugin.pluginStarted)
			return null;

		panelComponent.getChildren().clear();

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT);
		tableComponent.setDefaultColor(Color.ORANGE);

		tableComponent.addRow("NMZ Helper");
		tableComponent.addRow(plugin.status);



		if (!tableComponent.isEmpty())
		{
			panelComponent.getChildren().add(tableComponent);
		}

		panelComponent.setPreferredSize(new Dimension(175, 100));
		panelComponent.setBackgroundColor(Color.BLACK);

		return panelComponent.render(graphics);
	}
}
