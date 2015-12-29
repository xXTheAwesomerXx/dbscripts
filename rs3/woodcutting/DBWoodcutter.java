package dbscripts.rs3.woodcutting;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import org.powerbot.script.Area;
import org.powerbot.script.MessageEvent;
import org.powerbot.script.MessageListener;
import org.powerbot.script.PaintListener;
import org.powerbot.script.Random;
import org.powerbot.script.Script;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.Action;
import org.powerbot.script.rt6.Bank.Amount;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Constants;
import org.powerbot.script.rt6.GameObject;
import org.powerbot.script.rt6.Item;

@Script.Manifest(name = "DBWoodcutter", description = "Progressive Woodcutter made by xXTheAwesomerXx", properties = "")
public class DBWoodcutter extends PollingScript<ClientContext> implements
		MessageListener, PaintListener {

	private enum State {
		RUN, CHOP, BANK, DROP
	};

	private boolean bankRegular = false, bankOak = false, bankWillow = true,
			draynorWillows = true;
	private int randomInventInt = 25;

	private String statusMessage = "Starting...";
	private long START_TIME = System.currentTimeMillis(), CHOP_TIME = System
			.currentTimeMillis(), TASK_TIME = System.currentTimeMillis();
	private long totalRuntime, chopRuntime, taskRuntime;

	private final long START_EXPERIENCE = ctx.skills
			.experience(Constants.SKILLS_WOODCUTTING);
	private long TASK_START_EXPERIENCE = ctx.skills
			.experience(Constants.SKILLS_WOODCUTTING);
	private long expGained, taskExpGained;
	private long expHr, taskExpHr;
	private long expToLvl;
	private int taskChopped = 0, logsChopped = 0,
			currentWoodcuttingLevel = ctx.skills
					.level(Constants.SKILLS_WOODCUTTING);

	private final Tile[] sarimToDraynorPath = new Tile[] {
			new Tile(2987, 3202, 0), new Tile(2989, 3206, 0),
			new Tile(2992, 3210, 0), new Tile(2995, 3213, 0),
			new Tile(2997, 3216, 0), new Tile(2998, 3218, 0),
			new Tile(2999, 3221, 0), new Tile(3001, 3224, 0),
			new Tile(3003, 3227, 0), new Tile(3005, 3231, 0),
			new Tile(3006, 3235, 0), new Tile(3007, 3239, 0),
			new Tile(3008, 3242, 0), new Tile(3008, 3246, 0),
			new Tile(3009, 3249, 0), new Tile(3012, 3252, 0),
			new Tile(3017, 3253, 0), new Tile(3019, 3254, 0),
			new Tile(3019, 3258, 0), new Tile(3020, 3261, 0),
			new Tile(3021, 3264, 0), new Tile(3024, 3267, 0),
			new Tile(3029, 3270, 0), new Tile(3033, 3273, 0),
			new Tile(3037, 3273, 0), new Tile(3043, 3273, 0),
			new Tile(3046, 3274, 0), new Tile(3051, 3275, 0),
			new Tile(3055, 3276, 0), new Tile(3058, 3276, 0),
			new Tile(3062, 3275, 0), new Tile(3065, 3275, 0),
			new Tile(3070, 3275, 0), new Tile(3072, 3276, 0),
			new Tile(3076, 3272, 0), new Tile(3076, 3271, 0),
			new Tile(3075, 3268, 0), new Tile(3075, 3268, 0),
			new Tile(3076, 3266, 0), new Tile(3076, 3266, 0),
			new Tile(3077, 3263, 0), new Tile(3079, 3259, 0),
			new Tile(3079, 3254, 0), new Tile(3081, 3254, 0),
			new Tile(3081, 3250, 0), new Tile(3082, 3246, 0),
			new Tile(3085, 3245, 0), new Tile(3084, 3241, 0),
			new Tile(3086, 3238, 0), new Tile(3086, 3238, 0),
			new Tile(3084, 3237, 0) };

	private State getState() {
		if (!hasExtraItems()) {
			if (ctx.backpack.select().count() < getRandomInventInt()) {
				if (atTreeArea()) {
					statusMessage = "Chopping...";
					return State.CHOP;
				} else {
					statusMessage = "Running to Resources";
					return State.RUN;
				}
			} else {
				if (bankLogs()) {
					if (atBankArea()) {
						statusMessage = "Banking...";
						return State.BANK;
					} else {
						statusMessage = "Running to Bank";
						return State.RUN;
					}
				} else {
					statusMessage = "Dropping";
					return State.DROP;
				}
			}
		} else {
			statusMessage = "Dropping Extras";
			return State.DROP;
		}
	}

	@Override
	public void repaint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		totalRuntime = System.currentTimeMillis() - START_TIME;
		chopRuntime = System.currentTimeMillis() - CHOP_TIME;
		taskRuntime = System.currentTimeMillis() - TASK_TIME;
		expGained = ctx.skills.experience(Constants.SKILLS_WOODCUTTING)
				- START_EXPERIENCE;
		taskExpGained = ctx.skills.experience(Constants.SKILLS_WOODCUTTING)
				- TASK_START_EXPERIENCE;
		expHr = (expGained * 3600000) / totalRuntime;
		taskExpHr = (taskExpGained * 3600000) / taskRuntime;
		expToLvl = ctx.skills.experienceAt(ctx.skills
				.level(Constants.SKILLS_WOODCUTTING) + 1)
				- ctx.skills.experience(Constants.SKILLS_WOODCUTTING);
		currentWoodcuttingLevel = ctx.skills
				.level(Constants.SKILLS_WOODCUTTING);
		g.setColor(new Color(0, 0, 0, 70));
		g.fillRect(5, 35, 523, 154);
		g.setColor(new Color(204, 0, 0));
		g.drawString("DiscoFisher: " + statusMessage, 10, 50);
		g.drawString("Total Runtime: " + formatTime(totalRuntime)
				+ " | Task Time: " + formatTime(taskRuntime), 10, 65);
		g.drawString("Last Chop: " + chopRuntime + "ms", 10, 80);
		g.drawString("Time to Lvl "
				+ (currentWoodcuttingLevel != 99 ? currentWoodcuttingLevel + 1
						: "X") + ": "
				+ formatTime((expHr <= 0 ? 0 : (expToLvl * 3600000) / expHr)),
				10, 95);
		g.drawString("Chopping Task: " + getAssignmentString()
				+ ", Banking Enabled: " + bankLogs(), 10, 110);
		g.drawString("Exp Gained: " + expGained + " | Exp Gained(Task): "
				+ taskExpGained, 10, 125);
		g.drawString("Exp/Hr: " + expHr + " | Exp/Hr(Task): " + taskExpHr, 10,
				140);
		g.drawString("Exp to Lvl " + (currentWoodcuttingLevel + 1) + ": "
				+ expToLvl, 10, 155);
		g.drawString("Total Logs: " + logsChopped + ", " + taskChopped + " "
				+ getAssignmentString(), 10, 170);
		g.drawString("Logs/Hr(Total): " + (logsChopped * 3600000)
				/ totalRuntime + " | Logs/Hr(" + getAssignmentString() + "): "
				+ (taskChopped * 3600000) / taskRuntime, 10, 185);
	}

	// ---- End Paint Listener ----

	private String formatTime(long millis) {
		int hours = (int) (millis / 3600000);
		millis %= 3600000;

		int minutes = (int) (millis / 60000);
		millis %= 60000;

		int seconds = (int) (millis / 1000);

		return (hours < 10 ? "0" + hours : hours) + " : "
				+ (minutes < 10 ? "0" + minutes : minutes) + " : "
				+ (seconds < 10 ? "0" + seconds : seconds);
	}

	@Override
	public void messaged(MessageEvent m) {
		if (m.source().isEmpty()) {
			if (m.text().contains("You get some")) {
				logsChopped++;
				taskChopped++;
				CHOP_TIME = System.currentTimeMillis();
			} else if ((m.text().contains("15") || m.text().contains("30"))
					&& m.text().contains("advanced")) {
				this.taskChopped = 0;
				TASK_TIME = System.currentTimeMillis();
				TASK_START_EXPERIENCE = ctx.skills
						.experience(Constants.SKILLS_WOODCUTTING);
			}
		}
	}

	@Override
	public void poll() {
		switch (getState()) {
		case CHOP:
			if (ctx.bank.opened()) {
				ctx.bank.close();
			}
			if (getAssignmentString().equalsIgnoreCase("Willow")
					&& draynorWillows) {
				if (ctx.widgets.widget(1184).component(11).text()
						.equalsIgnoreCase("Guard in tree")) {
					ctx.movement.step(ctx.players.local().tile().derive(2, 2));
					ctx.camera.angle('n');
				}
			}
			if (ctx.camera.pitch() > 55 || ctx.camera.pitch() < 15) {
				ctx.camera.pitch(Random.nextInt(15, 55));
			}
			equipBetterAxe();
			final GameObject obj = ctx.objects.select()
					.id(getAssignmentTreeIds()).nearest().poll();
			if (obj.valid()) {
				if (obj.inViewport()) {
					if (ctx.players.local().animation() != -1) {
						if (bankLogs() == false) {
							final int randomInt = Random.nextInt(5, 15);
							if (ctx.backpack.select().count() > (getRandomInventInt() - randomInt)) {
								while (ctx.players.local().animation() == 21191) {
									Point pointOfRandomWillow = ctx.backpack
											.id(getAssignmentItemId())
											.shuffle().poll().nextPoint();
									if (ctx.chat.queryContinue()) {
										ctx.chat.clickContinue(true);
									} else {
										if (!ctx.combatBar.select()
												.id(getAssignmentItemId())
												.isEmpty()) {
											Action dropLogs = ctx.combatBar
													.first().poll();
											ctx.combatBar
													.actionAt(dropLogs.slot())
													.component()
													.interact("Drop");
										} else {
											ctx.input.move(pointOfRandomWillow);
											ctx.input.drag(ctx.combatBar
													.first().poll().component()
													.nextPoint(), true);
										}
									}
								}
								setRandomInventInt(Random.nextInt(20, 25));
							} else {
								final GameObject obj2 = ctx.objects.select()
										.name(getAssignmentTreeString()).shuffle().poll();
								if (obj2.valid()) {
									if (ctx.players.local().animation() == 21191) {
										if (obj2.inViewport()) {
										obj2.hover();
										} else {
											ctx.camera.turnTo(obj2);
										}
									}
								}
							}
						}
					} else {
							obj.interact(true, "Chop down",
									getAssignmentTreeString());
					}
				} else {
					ctx.camera.turnTo(obj);
					obj.hover();
				}
			} else {
				final GameObject obj2 = ctx.objects.select()
						.name(getAssignmentTreeString()).nearest().poll();
				if (obj2.valid()) {
					if (ctx.players.local().animation() == -1) {
						obj2.interact(true, "Chop down",
								getAssignmentTreeString());
					}
				}
			}
			break;
		case DROP:
			if (!hasExtraItems()) {
				setRandomInventInt(Random.nextInt(20, 25));
				while (ctx.backpack.select().id(getAssignmentItemId()).count() > 0) {
					Point pointOfRandomWillow = ctx.backpack
							.id(getAssignmentItemId()).shuffle().poll()
							.nextPoint();
					if (ctx.chat.queryContinue()) {
						ctx.chat.clickContinue(true);
					} else {
						if (!ctx.combatBar.select().id(getAssignmentItemId())
								.isEmpty()) {
							Action dropLogs = ctx.combatBar.first().poll();
							ctx.combatBar.actionAt(dropLogs.slot()).component()
									.interact("Drop");
						} else {
							ctx.input.move(pointOfRandomWillow);
							ctx.input.drag(ctx.combatBar.first().poll()
									.component().centerPoint(), true);
						}
					}
				}
			} else {
				statusMessage = "Has extra logs, dropping...";
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
			if (!ctx.bank.opened()) {
				if (ctx.bank.inViewport()) {
					ctx.bank.open();
				} else {
					ctx.camera.turnTo(ctx.bank.nearest());
				}
			} else {
				while (ctx.backpack.select().id(getAssignmentItemId()).count() > 0) {

					ctx.bank.deposit(getAssignmentItemId(), Amount.ALL);
				}

				withdrawBestUsableAxe();
				while (hasUnusableAxe()) {
					depositUnusableAxe();
				}
				while (hasExtraAxeInInventory()) {
					depositExtraAxeInInventory();
				}
			}
			break;
		case RUN:
			if (ctx.bank.opened()) {
				ctx.bank.close();
			}
			equipBetterAxe();
			ctx.camera.angle('n');
			if (ctx.backpack.select().count() <= getRandomInventInt()) {
				if (!atTreeArea()) {
					final Tile pathToTreeArea;
					if (!getAssignmentString().equalsIgnoreCase("Willow")) {
						pathToTreeArea = ctx.movement.findPath(
								getAssignmentArea().getCentralTile()).next();
					} else {
						if (!draynorWillows) {
							pathToTreeArea = ctx.movement.findPath(
									new Tile(2985, 3190)).next();
						} else {
							if (!inOverallPortSarimArea()) {
								pathToTreeArea = ctx.movement.findPath(
										new Tile(3083, 3236)).next();
							} else {
								pathToTreeArea = ctx.movement.newTilePath(
										sarimToDraynorPath).next();
							}
						}
					}
					if (pathToTreeArea != null && pathToTreeArea.x() != -1) {
						ctx.movement.step(pathToTreeArea);
					} else {
						teleportToKnownLocation();
					}
				}
			} else {
				Tile pathToBank = ctx.movement.findPath(
						getAssignmentBankArea().getCentralTile()).next();
				if (pathToBank != null && pathToBank.x() != -1) {
					ctx.movement.step(pathToBank);
				} else {
					teleportToKnownLocation();
				}
			}
			break;
		default:
			break;
		}
	}

	private void teleportToKnownLocation() {
		if (getAssignmentString().equalsIgnoreCase("Willow") && draynorWillows) {
			if (!inOverallDraynorArea()) {
				if (Lodestone.DRAYNOR.canUse(ctx)) {
					Lodestone.DRAYNOR.teleport(ctx);
				} else {
					if (Lodestone.PORT_SARIM.canUse(ctx)) {
						Lodestone.PORT_SARIM.teleport(ctx);
					} else {
						stop();
					}
				}
			}
		} else {
			if (Lodestone.PORT_SARIM.canUse(ctx)) {
				Lodestone.PORT_SARIM.teleport(ctx);
			} else {
				stop();
			}
		}
	}

	private boolean hasUnusableAxe() {
		if (getAssignmentString().equalsIgnoreCase("Willow")) {
			if (ctx.backpack.select().id(1355).count() > 0) {
				if (currentWoodcuttingLevel < 21) {
					return true;
				}
			}
			if (ctx.backpack.select().id(1357).count() > 0) {
				if (currentWoodcuttingLevel < 31) {
					return true;
				}
			}
			if (ctx.backpack.select().id(1359).count() > 0) {
				if (currentWoodcuttingLevel < 41) {
					return true;
				}
			}
		}
		return false;
	}

	private void depositUnusableAxe() {
		if (getAssignmentString().equalsIgnoreCase("Willow")) {
			if (ctx.bank.opened()) {
				if (ctx.backpack.select().id(1355).count() > 0) {
					if (currentWoodcuttingLevel < 21) {
						ctx.bank.deposit(1355, Amount.ALL);
					}
				}
				if (ctx.backpack.select().id(1357).count() > 0) {
					if (currentWoodcuttingLevel < 31) {
						ctx.bank.deposit(1357, Amount.ALL);
					}
				}
				if (ctx.backpack.select().id(1359).count() > 0) {
					if (currentWoodcuttingLevel < 41) {
						ctx.bank.deposit(1359, Amount.ALL);
					}
				}
			}
		}
	}

	private void withdrawBestUsableAxe() {
		if (getAssignmentString().equalsIgnoreCase("Willow")) {
			if (ctx.bank.opened()) {
				if (currentWoodcuttingLevel >= 41) {
					if (ctx.bank.select().id(1359).count() > 0) {
						if (ctx.equipment.select().id(1359).count() == 0) {
							if (ctx.backpack.select().id(1359).count() == 0) {
								ctx.bank.withdraw(1359, Amount.ONE);
							}
						}
					}
				} else if (currentWoodcuttingLevel >= 31) {
					if (ctx.bank.select().id(1357).count() > 0) {
						if (ctx.equipment.select().id(1357).count() == 0) {
							if (ctx.backpack.select().id(1357).count() == 0) {
								ctx.bank.withdraw(1357, Amount.ONE);
							}
						}
					}
				} else if (currentWoodcuttingLevel >= 21) {
					if (ctx.bank.select().id(1355).count() > 0) {
						if (ctx.equipment.select().id(1355).count() == 0) {
							if (ctx.backpack.select().id(1355).count() == 0) {
								ctx.bank.withdraw(1355, Amount.ONE);
							}
						}
					}
				}
			}
		}
	}

	private void depositExtraAxeInInventory() {
		if (getAssignmentString().equalsIgnoreCase("Willow")) {
			if (ctx.bank.opened()) {
				int bestAxeId = 0;
				if (currentWoodcuttingLevel >= 41) {
					if (ctx.backpack.select().id(1355).count() > 0) {
						bestAxeId = 1355;
					}
					if (ctx.backpack.select().id(1357).count() > 0) {
						bestAxeId = 1357;
					}
					if (ctx.backpack.select().id(1359).count() > 0) {
						bestAxeId = 1359;
					}
				} else if (currentWoodcuttingLevel >= 31) {
					if (ctx.backpack.select().id(1355).count() > 0) {
						bestAxeId = 1355;
					}
					if (ctx.backpack.select().id(1357).count() > 0) {
						bestAxeId = 1357;
					}
				} else if (currentWoodcuttingLevel >= 21) {
					if (ctx.backpack.select().id(1355).count() > 0) {
						bestAxeId = 1355;
					}
				}
				if (bestAxeId != 0) {
					for (Item item : ctx.backpack.select().id(1355, 1357, 1359)) {
						if (item.id() != bestAxeId) {
							ctx.bank.deposit(item.id(), Amount.ALL);
						} else {
							if (ctx.backpack.select().id(item.id()).count() > 1) {
								ctx.bank.deposit(bestAxeId, (ctx.backpack
										.select().id(bestAxeId).count() - 1));
							}
						}
					}
				}
			}
		}
	}

	private boolean hasExtraAxeInInventory() {
		int bestAxeId = 0;
		boolean hasExtraAxe = false;
		if (getAssignmentString().equalsIgnoreCase("Willow")) {
			if (currentWoodcuttingLevel >= 41) {
				if (ctx.backpack.select().id(1355).count() > 0) {
					bestAxeId = 1355;
				}
				if (ctx.backpack.select().id(1357).count() > 0) {
					if (bestAxeId == 1355) {
						hasExtraAxe = true;
					}
					bestAxeId = 1357;
				}
				if (ctx.backpack.select().id(1359).count() > 0) {
					if (bestAxeId == 1355 || bestAxeId == 1357) {
						hasExtraAxe = true;
					}
					bestAxeId = 1359;
				}
			} else if (currentWoodcuttingLevel >= 31) {
				if (ctx.backpack.select().id(1355).count() > 0) {
					bestAxeId = 1355;
				}
				if (ctx.backpack.select().id(1357).count() > 0) {
					if (bestAxeId == 1355) {
						hasExtraAxe = true;
					}
					bestAxeId = 1357;
				}
			} else if (currentWoodcuttingLevel >= 21) {
				if (ctx.backpack.select().id(1355).count() > 0) {
					bestAxeId = 1355;
				}
			}
		}
		if (bestAxeId != 0) {
			if (ctx.backpack.select().id(bestAxeId).count() > 1) {
				hasExtraAxe = true;
			}
		}
		return hasExtraAxe;
	}

	private void equipBetterAxe() {
		if (!ctx.backpack.select().id(getBetterAxeId()).isEmpty()) {
			if (ctx.equipment.select().id(getBetterAxeId()).isEmpty()) {
				if (hasRequiredAttackLevel()) {
					for (Item i : ctx.backpack.id(getBetterAxeId()).shuffle()) {
						i.interact("Wield");
					}
				}
			}
		}
	}

	private void setRandomInventInt(int randomInt) {
		randomInventInt = randomInt;
	}

	private int getRandomInventInt() {
		return randomInventInt;
	}

	private int getAssignmentItemId() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		if (woodcuttingLevel >= 30) {
			return 1519; // NOTE: Willow Id
		} else if (woodcuttingLevel >= 15) {
			return 1521; // NOTE: Oak Id
		} else {
			return 1511; // NOTE: Regular Id
		}
	}

	private int getBetterAxeId() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		if (woodcuttingLevel >= 41) {
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

	private int[] extraItemIds() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		if (woodcuttingLevel >= 30) {
			return new int[] { 1511, 1521 }; // NOTE:Willow Extra
		} else if (woodcuttingLevel >= 15) {
			return new int[] { 1511 }; // NOTE:Oak Extra
		} else {
			return new int[] { 1519, 1521 }; // NOTE:Regular Extra
		}
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

	private String getAssignmentString() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		if (woodcuttingLevel >= 30) {
			return "Willow";
		} else if (woodcuttingLevel >= 15) {
			return "Oak";
		} else {
			return "Regular";
		}
	}

	private String getAssignmentTreeString() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		if (woodcuttingLevel >= 30) {
			return "Willow";
		} else if (woodcuttingLevel >= 15) {
			return "Oak";
		} else {
			return "Tree";
		}
	}

	private Area getAssignmentArea() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		if (woodcuttingLevel >= 30) {
			if (!draynorWillows) {
				return new Area(new Tile(2984, 3226), new Tile(2991, 3208)); // NOTE:Port
																				// Sarim
																				// Willow
																				// Area
			} else {
				return new Area(new Tile(3080, 3239), new Tile(3092, 3226)); // NOTE:Draynor
																				// Willow
																				// Area
			}
		} else if (woodcuttingLevel >= 15) {
			return new Area(new Tile(2975, 3220), new Tile(2995, 3200)); // NOTE:Oak
																			// Area
		} else {
			return new Area(new Tile(2975, 3233), new Tile(3009, 3191)); // NOTE:Regular
																			// Area
		}
	}

	private Area getAssignmentBankArea() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		if (woodcuttingLevel >= 30) {
			if (draynorWillows) {
				return new Area(new Tile(3088, 3246), new Tile(3097, 3240)); // NOTE:Willow
																				// Bank
				// Area
			} else {
				return new Area(new Tile(1, 2), new Tile(3, 4));
			}
		} else if (woodcuttingLevel >= 15) {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE:Oak Bank
																// Area
		} else {
			return new Area(new Tile(1, 2), new Tile(3, 4)); // NOTE:Regular
																// Bank Area
		}
	}

	private boolean hasRequiredAttackLevel() {
		int woodcuttingLevel = currentWoodcuttingLevel;
		int strLevel = ctx.skills.level(Constants.SKILLS_STRENGTH);
		if (woodcuttingLevel >= 61) {
			if (strLevel >= 60)
				return true;
		} else if (woodcuttingLevel >= 41) {
			if (strLevel >= 50)
				return true;
		} else if (woodcuttingLevel >= 31) {
			if (strLevel >= 40)
				return true;
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
		int woodcuttingLevel = currentWoodcuttingLevel;
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

	private boolean hasExtraItems() {
		for (int i = 0; i < extraItemIds().length; i++) {
			if (ctx.backpack.select().id(extraItemIds()[i]).count() > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean inOverallPortSarimArea() {
		return new Area(new Tile(2908, 3313), new Tile(3069, 3158))
				.contains(ctx.players.local());
	}

	private boolean inOverallDraynorArea() {
		return new Area(new Tile(3070, 3321), new Tile(3102, 3224))
				.contains(ctx.players.local());
	}

}
