package dbscripts.rs3.woodcutting;

import java.awt.Graphics;

import org.powerbot.script.Area;
import org.powerbot.script.MessageEvent;
import org.powerbot.script.MessageListener;
import org.powerbot.script.PaintListener;
import org.powerbot.script.Random;
import org.powerbot.script.Script;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.Action;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Constants;
import org.powerbot.script.rt6.GameObject;
import org.powerbot.script.rt6.Item;
import org.powerbot.script.rt6.LocalPath;

@Script.Manifest(name = "DBWoodcutter", description = "Progressive Woodcutter made by xXTheAwesomerXx", properties = "")
public class DBWoodcutter extends PollingScript<ClientContext> implements
		MessageListener, PaintListener {

	private enum State {
		RUN, CHOP, BANK, DROP
	};

	private boolean bankRegular = false, bankOak = false, bankWillow = false;
	private int randomInventInt = 25;

	private State getState() {
		if (!hasExtraItems()) {
			if (ctx.backpack.select().count() < getRandomInventInt()) {
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
		} else {
			return State.DROP;
		}
	}

	@Override
	public void repaint(Graphics graphics) {
		// TODO: Add Paint
	}

	@Override
	public void messaged(MessageEvent m) {
		if (m.source().isEmpty()) {
			if (m.text().contains("You get some")) {
				// TODO: Log chopped amount
			}
		}
	}

	@Override
	public void poll() {
		switch (getState()) {
		case CHOP:
			if (!ctx.objects.select().id(getAssignmentTreeIds()).isEmpty()) {
				equipBetterAxe();
				final GameObject obj = ctx.objects.nearest().poll();
				if (getAssignmentArea().contains(obj)) {
					if (obj.inViewport()) {
						if (ctx.players.local().animation() == 21191) {
							if (ctx.backpack.select().count() > (getRandomInventInt() - 5)) {
								while (ctx.backpack.select().count() > (getRandomInventInt() - 5) && ctx.players.local().animation() == 21191) {
									System.out.println("In while statement for REAL chop n drop!");
									for (Item i : ctx.backpack.select().id(
											getAssignmentItemId()).limit(getRandomInventInt() - 10)) {
										if (ctx.chat.queryContinue()) {
											ctx.chat.clickContinue(true);
										} else {
											if (!ctx.combatBar.select()
													.id(getAssignmentItemId()).isEmpty()) {
												System.out.println("Bar Slot: "
														+ ctx.combatBar.select()
																.id(getAssignmentItemId())
																.poll().slot());
												Action dropLogs = ctx.combatBar.select()
														.id(getAssignmentItemId()).poll();
												ctx.combatBar.actionAt(dropLogs.slot())
														.component().interact("Drop");
											} else {
												ctx.input.click(i.nextPoint(), true);
												ctx.input
														.drag(ctx.combatBar.select().id(-1)
																.poll().component()
																.centerPoint(), true);
											}
										}
									}
								}
							}
						} else {
							obj.interact("Chop");
						}
					} else {
						ctx.camera.turnTo(obj);
					}
				} else {
					LocalPath pathToCentralArea = ctx.movement
							.findPath(getAssignmentArea().getCentralTile());
					pathToCentralArea.traverse();
				}
			}
			break;
		case DROP:
			if (!hasExtraItems()) {
				setRandomInventInt(Random.nextInt(20, 25));
				while (ctx.backpack.select().id(getAssignmentItemId()).count() > 0) {
					for (Item i : ctx.backpack.select().id(
							getAssignmentItemId())) {
						if (ctx.chat.queryContinue()) {
							ctx.chat.clickContinue(true);
						} else {
							if (!ctx.combatBar.select()
									.id(getAssignmentItemId()).isEmpty()) {
								System.out.println("Bar Slot: "
										+ ctx.combatBar.select()
												.id(getAssignmentItemId())
												.poll().slot());
								Action dropLogs = ctx.combatBar.select()
										.id(getAssignmentItemId()).poll();
								ctx.combatBar.actionAt(dropLogs.slot())
										.component().interact("Drop");
							} else {
								ctx.input.click(i.nextPoint(), true);
								ctx.input
										.drag(ctx.combatBar.select().id(-1)
												.poll().component()
												.centerPoint(), true);
							}
						}
					}
				}
			} else {
				System.out.println("Has extra logs, dropping...");
				while (hasExtraItems()) {
					for (Item i : ctx.backpack.select().id(extraItemIds())) {
						if (ctx.chat.queryContinue()) {
							ctx.chat.clickContinue(true);
						} else {
							i.interact("Drop");
						}
					}
				}
			}
			break;
		case BANK:
			break;
		case RUN:
			if (ctx.backpack.select().count() < getRandomInventInt()) {
				if (!atTreeArea()) {
					LocalPath pathToTreeArea;
					if (!getAssignmentString().equalsIgnoreCase("Willow")) {
						pathToTreeArea = ctx.movement
								.findPath(getAssignmentArea().getCentralTile());
					} else {
						pathToTreeArea = ctx.movement.findPath(new Tile(2985,
								3190));
					}
					if (pathToTreeArea.valid()) {
						pathToTreeArea.traverse();
					} else {
						System.out.println("Path not valid?");
						if (Lodestone.PORT_SARIM.canUse(ctx)) {
							Lodestone.PORT_SARIM.teleport(ctx);
						} else {
							System.out
									.println("Not near port sarim area, and lodestone not active!");
							stop();
						}
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private void setRandomInventInt(int randomInt) {
		randomInventInt = randomInt;
	}

	private int getRandomInventInt() {
		return randomInventInt;
	}

	private int getAssignmentItemId() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return 1519; // NOTE: Willow Id
		} else if (woodcuttingLevel >= 15) {
			return 1521; // NOTE: Oak Id
		} else {
			return 1511; // NOTE: Regular Id
		}
	}

	private int getBetterAxeId() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 61) {
			return 6739;
		} else if (woodcuttingLevel >= 41) {
			return 1359;
		} else if (woodcuttingLevel >= 31) {
			return 1357;
		} else if (woodcuttingLevel >= 21) {
			return 1355;
		} else if (woodcuttingLevel >= 6) {
			return 1353;
		}
		return 0;
	}

	private int[] getAssignmentTreeIds() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return new int[] { 38616, 38627, }; // NOTE:Willow Id
		} else if (woodcuttingLevel >= 15) {
			return new int[] { 38731, 38732 }; // NOTE:Oak Id
		} else {
			return new int[] { 38760, 38762, 38782, 38783, 38784, 38786, 38788 }; // NOTE:Regular
																					// Id
		}
	}

	private int[] extraItemIds() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return new int[] { 1511, 1521 }; // NOTE:Willow Extra
		} else if (woodcuttingLevel >= 15) {
			return new int[] { 1511 }; // NOTE:Oak Extra
		} else {
			return new int[] { 1519, 1521 }; // NOTE:Regular Extra
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
			return new Area(new Tile(2970, 3200), new Tile(2995, 3182)); // NOTE:Willow
																			// Area
		} else if (woodcuttingLevel >= 15) {
			return new Area(new Tile(2975, 3233), new Tile(2995, 3200)); // NOTE:Oak
																			// Area
		} else {
			return new Area(new Tile(2975, 3233), new Tile(3009, 3191)); // NOTE:Regular
																			// Area
		}
	}

	private Area getAssignmentBankArea() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		if (woodcuttingLevel >= 30) {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE:Willow Bank
																// Area
		} else if (woodcuttingLevel >= 15) {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE:Oak Bank
																// Area
		} else {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE:Regular
																// Bank Area
		}
	}

	private void equipBetterAxe() {
		if (!ctx.backpack.select().id(getBetterAxeId()).isEmpty()) {
			if (ctx.equipment.select().id(getBetterAxeId()).isEmpty()) {
				if (hasRequiredAttackLevel()) {
					System.out.println("Equip better pick");
				} else {
					System.out.println("Don't have required attack level...");
				}
			} else {
				System.out.println("Don't have better axe equiped");
			}
		}
	}

	private boolean hasRequiredAttackLevel() {
		int woodcuttingLevel = ctx.skills.level(Constants.SKILLS_WOODCUTTING);
		int strLevel = ctx.skills.level(Constants.SKILLS_STRENGTH);
		/*
		 * if (woodcuttingLevel >= 71) { if (strLevel >= 70) return true; } else
		 */if (woodcuttingLevel >= 61) {
			if (strLevel >= 60)
				return true;
			/*
			 * } else if (woodcuttingLevel >= 50) { if (strLevel >= 1) return
			 * true;
			 */
		} else if (woodcuttingLevel >= 41) {
			if (strLevel >= 50)
				return true;
		} else if (woodcuttingLevel >= 31) {
			if (strLevel >= 40)
				return true;
			/* } else if (woodcuttingLevel >= 25 { */
		} else if (woodcuttingLevel >= 21) {
			if (strLevel >= 30)
				return true;
		} else if (woodcuttingLevel >= 6) {
			if (strLevel >= 20)
				return true;
		}
		return false;
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

	@SuppressWarnings("unused")
	private boolean hasExtraItems() {
		for (int i = 0; i < extraItemIds().length; i++) {
			if (ctx.backpack.select().id(extraItemIds()[i]).count() > 0) {
				System.out.println("Have extra");
				return true;
			}
		}
		return false;
	}

}
