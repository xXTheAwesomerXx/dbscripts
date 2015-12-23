package dbscripts.rs3.woodcutting;

import java.awt.Graphics;

import org.powerbot.script.Area;
import org.powerbot.script.Condition;
import org.powerbot.script.MessageEvent;
import org.powerbot.script.MessageListener;
import org.powerbot.script.PaintListener;
import org.powerbot.script.Random;
import org.powerbot.script.Script;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.Bank.Amount;
import org.powerbot.script.rt6.ChatOption;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Constants;
import org.powerbot.script.rt6.GameObject;
import org.powerbot.script.rt6.Item;
import org.powerbot.script.rt6.LocalPath;
import org.powerbot.script.rt6.Npc;
import org.powerbot.script.rt6.TilePath;

@Script.Manifest(name = "DBWoodcutter", description = "Progressive Woodcutter made by xXTheAwesomerXx", properties = "")
public class DBWoodcutter extends PollingScript<ClientContext> implements
		MessageListener, PaintListener {

	private enum State {
		RUN, CHOP, BANK, DROP
	};

	private boolean bankRegular = false, bankOak = false, bankWillow = true;

	private State getState() {
		if (ctx.backpack.select().count() != 28) {
			if (atTreeArea()) {
				return State.CHOP;
			} else {
				return State.RUN;
			}
		} else {
			if (bankLogs()) {
				if (atBankArea()) {
					return State.BANK;
				} else {
					return State.RUN;
				}
			} else {
				return State.DROP;
			}
		}
	}

	@Override
	public void repaint(Graphics graphics) {

	}

	@Override
	public void messaged(MessageEvent m) {

	}

	@Override
	public void poll() {
		switch (getState()) {

		}
	}

	private int getAssignmentTreeId() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return 1; // NOTE: Willow Id
		} else if (woodcuttingLevel >= 15) {
			return 2; // NOTE: Oak Id
		} else {
			return 3; // NOTE: Regular Id
		}
	}

	private String getAssignmentString() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return "Willow";
		} else if (woodcuttingLevel >= 15) {
			return "Oak";
		} else {
			return "Regular";
		}
	}

	private Area getAssignmentArea() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE: Willow
																// Area
		} else if (woodcuttingLevel >= 15) {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE: Oak Area
		} else {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE: Regular
																// Area
		}
	}

	private Area getAssignmentBankArea() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE: Willow
																// Bank Area
		} else if (woodcuttingLevel >= 15) {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE: Oak Bank
																// Area
		} else {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE: Regular
																// Bank Area
		}
	}

	private boolean atTreeArea() {
		return getAssignmentArea().contains(ctx.players.local());
	}

	private boolean atBankArea() {
		return getAssignmentBankArea().contains(ctx.players.local());
	}

	private boolean bankLogs() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			if (bankWillow)
				return true; // NOTE: Willow Bank Area
		} else if (woodcuttingLevel >= 15) {
			if (bankOak)
				return true; // NOTE: Oak Bank Area
		} else {
			if (bankRegular)
				return true; // NOTE: Regular Bank Area
		}
		return false;
	}

}
