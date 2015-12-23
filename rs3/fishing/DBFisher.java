package dbscripts.rs3.fishing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.concurrent.Callable;

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

@Script.Manifest(name = "DBFisher", description = "Progressive Fisher made by xXTheAwesomerXx", properties = "")
public class DBFisher extends PollingScript<ClientContext> implements
		MessageListener, PaintListener {

	private enum State {
		FISH, RUN, DROP, BANK, PAUSE, TRADE, FIX_SPOT
	};

	private long START_TIME = System.currentTimeMillis(), CATCH_TIME = System
			.currentTimeMillis(), TASK_TIME = System.currentTimeMillis();
	private long totalRuntime, catchRuntime, taskRuntime;

	private final long START_EXPERIENCE = ctx.skills
			.experience(Constants.SKILLS_FISHING);
	private long expGained;
	private long expHr;
	private long expToLvl;
	private int taskCatches = 0, fishCaught = 0, currentFishingLevel;

	private String statusMessage = "Starting Script";
	private String muleName = "Arrg1638";
	private boolean pauseScript = false, bankingEnabled = true,
			doTrade = false, fixSpot = false;

	private final Tile[] pathToDocks = new Tile[] { new Tile(2851, 3142, 0),
			new Tile(2851, 3140, 0), new Tile(2855, 3143, 0),
			new Tile(2860, 3145, 0), new Tile(2862, 3146, 0),
			new Tile(2864, 3148, 0), new Tile(2868, 3147, 0),
			new Tile(2872, 3146, 0), new Tile(2877, 3145, 0),
			new Tile(2877, 3145, 0), new Tile(2879, 3143, 0),
			new Tile(2883, 3143, 0), new Tile(2888, 3143, 0),
			new Tile(2890, 3145, 0), new Tile(2893, 3147, 0),
			new Tile(2895, 3148, 0), new Tile(2899, 3151, 0),
			new Tile(2899, 3154, 0), new Tile(2901, 3156, 0),
			new Tile(2901, 3156, 0), new Tile(2905, 3156, 0),
			new Tile(2910, 3153, 0), new Tile(2910, 3153, 0),
			new Tile(2914, 3151, 0), new Tile(2917, 3152, 0),
			new Tile(2917, 3152, 0), new Tile(2917, 3154, 0),
			new Tile(2917, 3157, 0), new Tile(2917, 3157, 0),
			new Tile(2918, 3159, 0), new Tile(2922, 3163, 0),
			new Tile(2924, 3166, 0), new Tile(2924, 3169, 0),
			new Tile(2926, 3172, 0), new Tile(2925, 3176, 0) };

	private final Tile[] pathToStiles = ctx.movement.newTilePath(pathToDocks)
			.reverse().array();

	private final Tile[] pathToSarim = new Tile[] { new Tile(3233, 3221, 0),
			new Tile(3231, 3226, 0), new Tile(3228, 3230, 0),
			new Tile(3227, 3233, 0), new Tile(3224, 3237, 0),
			new Tile(3222, 3241, 0), new Tile(3221, 3245, 0),
			new Tile(3216, 3246, 0), new Tile(3212, 3246, 0),
			new Tile(3207, 3246, 0), new Tile(3201, 3246, 0),
			new Tile(3195, 3244, 0), new Tile(3190, 3244, 0),
			new Tile(3184, 3246, 0), new Tile(3177, 3244, 0),
			new Tile(3174, 3241, 0), new Tile(3170, 3239, 0),
			new Tile(3165, 3238, 0), new Tile(3160, 3238, 0),
			new Tile(3156, 3234, 0), new Tile(3149, 3234, 0),
			new Tile(3145, 3233, 0), new Tile(3141, 3230, 0),
			new Tile(3137, 3229, 0), new Tile(3130, 3226, 0),
			new Tile(3126, 3223, 0), new Tile(3120, 3221, 0),
			new Tile(3114, 3223, 0), new Tile(3110, 3224, 0),
			new Tile(3110, 3227, 0), new Tile(3109, 3231, 0),
			new Tile(3107, 3234, 0), new Tile(3106, 3235, 0),
			new Tile(3105, 3239, 0), new Tile(3106, 3241, 0),
			new Tile(3105, 3244, 0), new Tile(3105, 3247, 0),
			new Tile(3101, 3249, 0), new Tile(3098, 3248, 0),
			new Tile(3085, 3247, 0), new Tile(3079, 3250, 0),
			new Tile(3079, 3255, 0), new Tile(3079, 3262, 0),
			new Tile(3077, 3268, 0), new Tile(3075, 3274, 0),
			new Tile(3068, 3275, 0), new Tile(3064, 3275, 0),
			new Tile(3058, 3274, 0), new Tile(3054, 3274, 0),
			new Tile(3049, 3274, 0), new Tile(3045, 3274, 0),
			new Tile(3041, 3271, 0), new Tile(3037, 3269, 0),
			new Tile(3033, 3265, 0), new Tile(3033, 3258, 0),
			new Tile(3034, 3252, 0), new Tile(3034, 3247, 0),
			new Tile(3034, 3244, 0), new Tile(3031, 3241, 0),
			new Tile(3028, 3240, 0), new Tile(3028, 3235, 0),
			new Tile(3028, 3229, 0), new Tile(3027, 3225, 0),
			new Tile(3028, 3221, 0), new Tile(3028, 3219, 0),
			new Tile(3028, 3217, 0) };

	// ---- End Variables ----

	private State getState() {
		if (!doTrade) {
			if (!pauseScript) {
				if (fixSpot) {
					return State.FIX_SPOT;
				} else {
					if (hasJunkFish()) {
						return State.DROP;
					} else {
						if (hasExtraItems()) {
							if (getAssignmentBankArea().contains(
									ctx.players.local())) {
								System.out.println("Banking because of extras!"
										+ " Gear: " + hasRequiredGear()
										+ " Extra: " + hasExtraItems());
								return State.BANK;
							} else {
								System.out
										.println("Running to bank to deposit extras..."
												+ " Gear: "
												+ hasRequiredGear()
												+ " Extra: " + hasExtraItems());
								return State.RUN;
							}
						} else {
							if (hasRequiredGear()) {
								if (ctx.backpack.select().count() != 28) {
									if (getAssignmentFishingArea().contains(
											ctx.players.local())) {
										System.out
												.println("We're trying to fish!"
														+ " Gear: "
														+ hasRequiredGear()
														+ " Extra: "
														+ hasExtraItems());
										return State.FISH;
									} else {
										System.out
												.println("We're running to the fishing area?"
														+ " Gear: "
														+ hasRequiredGear()
														+ " Extra: "
														+ hasExtraItems());
										return State.RUN;
									}
								} else {
									if (bankingEnabled()) {
										if (getAssignmentBankArea().contains(
												ctx.players.local())) {
											System.out
													.println("Banking because of a full inventory!"
															+ " Gear: "
															+ hasRequiredGear()
															+ " Extra: "
															+ hasExtraItems());
											return State.BANK;
										} else {
											System.out
													.println("Running to bank to deposit inventory..."
															+ " Gear: "
															+ hasRequiredGear()
															+ " Extra: "
															+ hasExtraItems());
											return State.RUN;
										}
									} else {
										System.out.println("Dropping dumb fish"
												+ " Gear: " + hasRequiredGear()
												+ " Extra: " + hasExtraItems());
										return State.DROP;
									}
								}
							} else {
								if (getAssignmentBankArea().contains(
										ctx.players.local())) {
									System.out
											.println("We're gonna bank 'cause we have extra items or don't have gear!"
													+ " Gear: "
													+ hasRequiredGear()
													+ " Extra: "
													+ hasExtraItems());
									return State.BANK;
								} else {
									System.out
											.println("We're running to the bank because of extras or gear!"
													+ " Gear: "
													+ hasRequiredGear()
													+ " Extra: "
													+ hasExtraItems());
									return State.RUN;
								}
							}
						}
					}
				}
			} else {
				return State.PAUSE;
			}
		} else {
			return State.TRADE;
		}
	}

	@Override
	public void poll() {
		switch (getState()) {
		case FIX_SPOT:
			System.out
			.println("Not interacting with the right fishing spot!");
	if (ctx.movement.step(new Tile(ctx.players.local().tile().x()
			+ Random.nextInt(2, 5), ctx.players.local().tile().y()
			+ Random.nextInt(1, 4)))) {
		fixSpot = false;
	}
			break;
		case FISH:
			statusMessage = "Fishing!";
			if (getAssignmentString().equalsIgnoreCase("Trout/Salmon")) {
				if (ctx.camera.yaw() != 85 || ctx.camera.pitch() != 92) {
					// statusMessage = "Turning Camera";
					combineCamera(85, 92);
				}
			} else if (getAssignmentString().equalsIgnoreCase("Lobster")) {
				if (ctx.camera.yaw() != 0 || ctx.camera.pitch() != 84) {
					// statusMessage = "Turning Camera";
					combineCamera(0, 84);
				}
			} else if (getAssignmentString().equalsIgnoreCase("Crayfish")) {
				if (ctx.camera.yaw() != 274 || ctx.camera.pitch() != 85) {
					// statusMessage = "Turning Camera";
					combineCamera(274, 85);
				}
			}
			if (!ctx.npcs.select().id(getAssignmentNpcIds()).isEmpty()) {
				final Npc fishingSpot = ctx.npcs.nearest().poll();
				if (fishingSpot.tile().distanceTo(ctx.players.local()) < 15) {
					
					/*if (!getAssignmentString().equalsIgnoreCase("Lobster")) {
						Condition.wait(new Callable<Boolean>() {
							@Override
							public Boolean call() throws Exception {
								return ctx.players.local().animation() == -1
										&& getAssignmentFishingArea().contains(
												ctx.players.local());
							};
						}, 700, 5);
					} else {
						Condition.wait(new Callable<Boolean>() {
							@Override
							public Boolean call() throws Exception {
								return ctx.players.local().animation() == -1
										&& getAssignmentFishingArea().contains(
												ctx.players.local());
							};
						}, 2400, 5);
					}*/
					fishingSpot.hover();
					if (fishingSpot.valid()) {
						if (ctx.players.local().animation() == -1 && !fixSpot) {
						if (!fishingSpot.click()) {
							fishingSpot.interact(getAssignmentActionString());
						}
						} else {
							if (fixSpot) {
								System.out
								.println("Not interacting with the right fishing spot!");
						if (ctx.movement.step(new Tile(ctx.players.local().tile().x()
								+ Random.nextInt(2, 5), ctx.players.local().tile().y()
								+ Random.nextInt(1, 4)))) {
							fixSpot = false;
						}
							}
						}
					}
					fishingSpot.hover();
					/*if (!getAssignmentString().equalsIgnoreCase("Lobster")) {
						Condition.wait(new Callable<Boolean>() {
							@Override
							public Boolean call() throws Exception {
								return ctx.players.local().animation() == 1;
							}
						}, 750, 10);
					} else {
						Condition.wait(new Callable<Boolean>() {
							@Override
							public Boolean call() throws Exception {
								return ctx.players.local().animation() == 1;
							}
						}, 2500, 10);
					}*/
				} else {
					LocalPath pathToSpot = ctx.movement.findPath(fishingSpot);
					pathToSpot.traverse();
				}
			}
			break;
		case RUN:
			if (ctx.bank.opened()) {
				ctx.bank.close();
			}
			statusMessage = "Running";
			if (hasRequiredGear() && !hasExtraItems()
					&& ctx.backpack.select().count() != 28) {
				if (getAssignmentString().equalsIgnoreCase("Crayfish")
						|| getAssignmentString().equalsIgnoreCase(
								"Trout/Salmon")) {
					if (getAssignmentFishingArea().getCentralTile().distanceTo(
							ctx.players.local()) < 75) {
						Tile pathToAreaTile = ctx.movement.findPath(
								getAssignmentFishingArea().getRandomTile())
								.next();
						ctx.movement.step(pathToAreaTile);
					} else {
						if (Lodestone.LUMBRIDGE.canUse(ctx)) {
							statusMessage = "Teleporting to Lubridge!";
							Lodestone.LUMBRIDGE.teleport(ctx);
						}
					}
				} else if (getAssignmentString().equalsIgnoreCase("Lobster")) {
					if (inKaramja()) {
						System.out.println("Distance to Spot: "
								+ ctx.movement
										.distance(getAssignmentFishingArea()
												.getCentralTile()));
						if (ctx.movement.distance(getAssignmentFishingArea()
								.getCentralTile()) < 200
								&& ctx.movement
										.distance(getAssignmentFishingArea()
												.getCentralTile()) != -1) {
							LocalPath pathToArea = ctx.movement
									.findPath(getAssignmentFishingArea()
											.getCentralTile());
							pathToArea.traverse();
						} else {
							if (ctx.movement
									.distance(getAssignmentFishingArea()
											.getCentralTile()) == -1) {
								TilePath path = ctx.movement
										.newTilePath(pathToDocks);
								path.traverse();
							} else {
								LocalPath pathToArea = ctx.movement
										.findPath(getAssignmentFishingArea()
												.getCentralTile());
								pathToArea.traverse();
							}
						}
					} else {
						if (inBoat()) {
							GameObject plank = ctx.objects.select().id(2082)
									.select().nearest().poll();
							if (plank.inViewport()) {
								if (plank.tile()
										.distanceTo(ctx.players.local()) < 7) {
									if (ctx.camera.yaw() != 0
											|| ctx.camera.pitch() != 44) {
										// statusMessage = "Turning Camera";
										combineCamera(0, 44);
									}
									plank.click();
								} else {
									ctx.movement.findPath(plank).traverse();
								}
							} else {
								ctx.camera.turnTo(plank);
							}
						} else {
							if (portSarimSailorArea().contains(
									ctx.players.local())) {
								// talk to sailors
								final Npc sailorNpc = ctx.npcs.id(376).select()
										.nearest().poll();
								if (ctx.chat.chatting()) {
									if (ctx.chat.queryContinue() == true) {
										ctx.chat.clickContinue();
									} else {
										ChatOption selectYes = ctx.chat
												.select().text("Yes please.")
												.poll();
										if (selectYes.valid()) {
											selectYes.select(true);
										}
									}
								} else {
									sailorNpc.interact("Pay-fare");
								}
							} else {
								if (ctx.varpbits.varpbit(1113) != 55) {
									if (portSarimSailorArea().getCentralTile()
											.distanceTo(ctx.players.local()) > 104) {
										if (Lodestone.PORT_SARIM.canUse(ctx)) {
											Lodestone.PORT_SARIM.teleport(ctx);
										} else {
											if (new Area(
													new Tile(3009, 3300, 0),
													new Tile(3265, 3176, 0))
													.contains(ctx.players
															.local())) {
												ctx.movement.newTilePath(
														pathToSarim).traverse();
											} else {
												if (Lodestone.LUMBRIDGE
														.canUse(ctx)) {
													statusMessage = "Teleporting to Lubridge!";
													Lodestone.LUMBRIDGE
															.teleport(ctx);
												}
											}
										}
									} else {
										Tile pathToAreaTile = ctx.movement
												.findPath(
														portSarimSailorArea()
																.getRandomTile())
												.next();
										ctx.movement.step(pathToAreaTile);
									}
								}
							}
						}
					}
				}
			} else {
				// ----End running to spots
				if (!hasRequiredGear()
						|| hasExtraItems()
						|| (ctx.backpack.select().count() == 28 && bankingEnabled())) {
					System.out.print("Step 1, figure out where we are | ");
					while (!getAssignmentBankArea().contains(
							ctx.players.local())) {
						System.out.print("While we're not in the area, run | ");
						if (getAssignmentString().equalsIgnoreCase("Lobster")) {
							System.out.print("To lobster area | "
									+ "InKaramja: " + inKaramja() + " | ");
							if (inKaramja() || inBoat()) {
								System.out
										.print("Need to run to karamja bank | ");
								if (!inBoat()) {
									TilePath path = ctx.movement
											.newTilePath(pathToStiles);
									path.traverse();
								}
							} else {
								Tile pathToAreaTile = ctx.movement
										.findPath(
												getAssignmentBankArea()
														.getRandomTile())
										.next();
								ctx.movement.step(pathToAreaTile);
							}
						} else {
							if (getAssignmentBankArea().getCentralTile()
									.distanceTo(ctx.players.local()) < 104) {
								Tile pathToAreaTile = ctx.movement
										.findPath(
												getAssignmentBankArea()
														.getRandomTile())
										.next();
								ctx.movement.step(pathToAreaTile);
							} else {
								if (Lodestone.LUMBRIDGE.canUse(ctx)) {
									statusMessage = "Teleporting to Lubridge!";
									Lodestone.LUMBRIDGE.teleport(ctx);
								}
							}
						}
					}
				}
			}
			break;
		case DROP:
			if (hasJunkFish()) {
				System.out.println("Has junk");
				// while (ctx.backpack.select().id(junkItemIds()).count() > 1) {
				// System.out.println("In while statement...");
				for (Item i : ctx.backpack.select().id(junkItemIds())) {
					int oldJunkSize = ctx.backpack.select().id(junkItemIds())
							.size();
					int newJunkSize = ctx.backpack.select().id(junkItemIds())
							.size();
					if (i.id() == junkItemIds()[0]) {
						if (ctx.chat.queryContinue()) {
							ctx.chat.clickContinue(true);
						} else {
							ctx.input.send("0");
							newJunkSize = ctx.backpack.select()
									.id(junkItemIds()).size();
							if (newJunkSize == oldJunkSize) {
								ctx.input.click(false);
								System.out.println("Same size...");
							}
						}
					} else if (i.id() == junkItemIds()[1]) {
						if (ctx.chat.queryContinue()) {
							ctx.chat.clickContinue(true);
						} else {
							// ctx.input.send("9");
							ctx.input.send("9");
							newJunkSize = ctx.backpack.select()
									.id(junkItemIds()).size();
							if (newJunkSize == oldJunkSize) {
								ctx.input.click(false);
								System.out.println("Same size 2!");
							}
						}
					}
				}
				// }
			} else {
				while (ctx.backpack.select().id(getAssignmentItemIds()).count() > 1) {
					if (getAssignmentString().equalsIgnoreCase("Crayfish")) {
						for (@SuppressWarnings("unused")
						Item i : ctx.backpack.select().id(
								getAssignmentItemIds())) {
							if (ctx.chat.queryContinue()) {
								ctx.chat.clickContinue(true);
							} else {
								ctx.input.send("2");
							}
						}
					} else if (getAssignmentString().equalsIgnoreCase(
							"Trout/Salmon")) {
						for (Item i : ctx.backpack.select().id(
								getAssignmentItemIds())) {
							if (i.id() == getAssignmentItemIds()[0]) {
								if (ctx.chat.queryContinue()) {
									ctx.chat.clickContinue(true);
								} else {
									ctx.input.send("3");
								}
							} else if (i.id() == getAssignmentItemIds()[1]) {
								if (ctx.chat.queryContinue()) {
									ctx.chat.clickContinue(true);
								} else {
									ctx.input.send("4");
								}
							}
						}
					} else if (getAssignmentString()
							.equalsIgnoreCase("Lobster")) {
						// TODO: Lobster dropping method, but why drop
						// lobsters?!
					}
				}
			}
			break;
		case BANK:
			statusMessage = "Banking";
			if (!getAssignmentString().equalsIgnoreCase("Lobster")) {
				if (!ctx.bank.opened()) {
					if (ctx.bank.inViewport()) {
						ctx.bank.open();
					} else {
						ctx.camera.turnTo(ctx.bank.nearest());
					}
				} else {
					if (!hasExtraItems()) {
						if (getAssignmentString().equalsIgnoreCase(
								"Trout/Salmon")) {
							if (ctx.bank.presetGear1()) {
								System.out.println("Withdrew Preset 1");
							} else {
								System.out
										.println("Failed to withdraw preset 1");
							}
						} else if (getAssignmentString().equalsIgnoreCase(
								"Lobster")) {
							System.out.println("Banking for Lobster");
							final Npc stiles = ctx.npcs.id(11267).select()
									.nearest().poll();
							if (ctx.chat.chatting()) {
								if (ctx.chat.queryContinue() == true) {
									ctx.chat.clickContinue();
								} else {
									ChatOption selectYes = ctx.chat
											.select()
											.text("Okay, exchange all my fish for banknotes.")
											.poll();
									if (selectYes.valid()) {
										selectYes.select(true);
									}
								}
							} else {
								stiles.interact("Exchange");
							}
						}
					} else {
						for (int i = 0; i < extraItemIds().length; i++) {
							if (ctx.backpack.id(extraItemIds()[i]).select()
									.count() != 0) {
								ctx.bank.deposit(extraItemIds()[i], Amount.ALL);
							}
						}
					}
				}
			} else {
				System.out.println("Banking for Lobster");
				final Npc stiles = ctx.npcs.id(11267).select().nearest().poll();
				if (ctx.chat.chatting()) {
					if (ctx.chat.queryContinue() == true) {
						ctx.chat.clickContinue();
					} else {
						ChatOption selectYes = ctx.chat
								.select()
								.text("Okay, exchange all my fish for banknotes.")
								.poll();
						if (selectYes.valid()) {
							selectYes.select(true);
						}
					}
				} else {
					stiles.interact("Exchange");
				}
			}
			break;
		case PAUSE:
			statusMessage = "Paused";
			break;
		case TRADE:
			//ctx.properties.setProperty("trade.disable", "true");
			// TODO: Trading is currently disabled, waiting for whitelist trading
			System.out.println("Disabled trade!!!!!!!!!");
			if (ctx.widgets.widget(335).valid()) {
				if (ctx.widgets.widget(335).component(10).text().equalsIgnoreCase(muleName)) {
					System.out.println("1st trade window open, and mule name is correct");
				}
			}
			doTrade = false;
			break;
		default:
			break;
		}
	}

	@Override
	public void messaged(MessageEvent m) {
		if (m.source().isEmpty()) {
			if (m.text().contains("You catch a")) {
				fishCaught++;
				taskCatches++;
				CATCH_TIME = System.currentTimeMillis();
			} else if (m.text().equalsIgnoreCase("! You have reached level 20")
					|| m.text().equalsIgnoreCase("! You have reached level 40")) {
				taskCatches = 0;
				TASK_TIME = System.currentTimeMillis();
			} else if (m.text().contains("cast out your") || m.text().contains("You catch some")) {
				if (getAssignmentString().equalsIgnoreCase("Lobster")) {
					fixSpot = true;
				}
			}
		}
		if (m.source().equalsIgnoreCase(muleName)) {
			if (m.text().equalsIgnoreCase("What's your fishing level?")) {
				System.out.println("Mule asked what's our fishing level");
				ctx.input.sendln("");
				ctx.input.sendln(Integer.toString(ctx.skills
						.level(Constants.SKILLS_FISHING)));
			} else if (m.text().equalsIgnoreCase(
					"How many fish do you guys have?")) {
				System.out.println("Mule asked how many fish we have!");
				ctx.input.sendln("");
				ctx.input.sendln(Integer.toString(ctx.backpack.select().id(378)
						.count(true)
						+ ctx.backpack.select().id(377).count())
						+ " Lobsters");
			} else if (m.text().equalsIgnoreCase("Trade me")) {
				doTrade = true;
				System.out.println("Initiate trade sequence");
			}
		}
	}

	// ---- End Message Listener ----

	@Override
	public void repaint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		Point mouseLocation;
		totalRuntime = System.currentTimeMillis() - START_TIME;
		catchRuntime = System.currentTimeMillis() - CATCH_TIME;
		taskRuntime = System.currentTimeMillis() - TASK_TIME;
		expGained = ctx.skills.experience(Constants.SKILLS_FISHING)
				- START_EXPERIENCE;
		expHr = (expGained * 3600000) / taskRuntime;
		expToLvl = ctx.skills.experienceAt(ctx.skills
				.level(Constants.SKILLS_FISHING) + 1)
				- ctx.skills.experience(Constants.SKILLS_FISHING);
		currentFishingLevel = ctx.skills.level(Constants.SKILLS_FISHING);
		g.setColor(new Color(0, 0, 0, 70));
		g.fillRect(3, 395, 643, 182);
		g.setColor(new Color(124, 252, 0));
		g.drawString("DiscoFisher: " + statusMessage + ", inKaramja "
				+ inKaramja(), 160, 420);
		// getRuntime starts with negative milliseconds for some unknown reason
		g.drawString("Total Runtime: " + formatTime(totalRuntime)
				+ " | Last Catch: " + catchRuntime + "ms | Task Time: "
				+ formatTime(taskRuntime), 135, 445);
		// Index 14 in skills is mining index
		g.drawString(
				"Time to Lvl "
						+ (ctx.skills.level(Constants.SKILLS_FISHING) != 99 ? ctx.skills
								.level(Constants.SKILLS_FISHING) + 1 : "X")
						+ ": "
						+ formatTime((expHr <= 0 ? 0 : (expToLvl * 3600000)
								/ expHr)), 130, 465);
		g.drawString("Fishing Task: " + getAssignmentString(), 170, 485);
		g.drawString("Exp Gained: " + expGained, 14, 515);
		g.drawString("Exp/Hr: " + expHr, 177, 514);
		g.drawString(
				"Exp to Lvl "
						+ (ctx.skills.level(Constants.SKILLS_FISHING) + 1)
						+ ": " + expToLvl, 325, 514);
		g.drawString("Total Catches: " + fishCaught + ", " + taskCatches + " "
				+ getAssignmentString(), 89, 549);
		g.drawString(
				"Catches/Hr: " + (fishCaught * 3600000) / totalRuntime
						+ " | Catch/Hr(Task): " + (taskCatches * 3600000)
						/ taskRuntime, 250, 574);

		/*
		 * If - else if statements that follow are for the progress bar on the
		 * perimeter of the paint
		 */

		/*
		 * Bottom Bar Sets the width of the rect based on the percentage
		 * returned from percentageBetweenQuarters
		 */
		if (ctx.skills.experience(Constants.SKILLS_FISHING)
				- ctx.skills.experienceAt(ctx.skills
						.level(Constants.SKILLS_FISHING)) <= (ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING) + 1) - ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING))) * .25) {

			g.fillRect(3, 577, (int) (percentageBetweenQuarters(0, .25) * 646),
					3);

		}
		/*
		 * Right Bar Draws the bottom rect progress bar Sets the Y coordinate
		 */
		else if (ctx.skills.experience(Constants.SKILLS_FISHING)
				- ctx.skills.experienceAt(ctx.skills
						.level(Constants.SKILLS_FISHING)) <= (ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING) + 1) - ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING))) * .50) {

			g.fillRect(3, 577, 645, 3);
			g.fillRect(645,
					577 - (int) (percentageBetweenQuarters(.25, .50) * 189), 3,
					(int) (percentageBetweenQuarters(.25, .50) * 189));

		}

		/*
		 * Top Bar Draws the bottom and right rect progress bar Sets the X
		 * coordinate
		 */
		else if (ctx.skills.experience(Constants.SKILLS_FISHING)
				- ctx.skills.experienceAt(ctx.skills
						.level(Constants.SKILLS_FISHING)) <= (ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING) + 1) - ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING))) * .75) {
			g.fillRect(3, 577, 645, 3);
			g.fillRect(645, 392, 3, 188);
			g.fillRect(645 - (int) (percentageBetweenQuarters(.50, .75) * 648),
					392, (int) (percentageBetweenQuarters(.50, .75) * 648), 3);
		}
		/*
		 * Left Bar Draws the bottom, right, and top rect progress bar Sets the
		 * height of the rect based on the percentage returned from
		 * percentageBetweenQuarters
		 */
		else if (ctx.skills.experience(Constants.SKILLS_FISHING)
				- ctx.skills.experienceAt(ctx.skills
						.level(Constants.SKILLS_FISHING)) <= (ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING) + 1) - ctx.skills
				.experienceAt(ctx.skills.level(Constants.SKILLS_FISHING)))) {
			g.fillRect(3, 577, 645, 3);
			g.fillRect(645, 392, 3, 188);
			g.fillRect(0, 392, 648, 3);
			g.fillRect(0, 395, 3,
					(int) (percentageBetweenQuarters(.75, 1) * 186));
		}

		/*
		 * Remaining code draws two circles with outermost circle an open circle
		 * and innermost circle filled. The center-point of each circle is the
		 * mouse coordinate.
		 */
		mouseLocation = ctx.input.getLocation();

		// Need to fix
		g.setColor(Color.RED);
		g.fillOval(mouseLocation.x - 2, mouseLocation.y - 2, 4, 4);
		g.setColor(Color.BLACK);
		g.drawOval(mouseLocation.x - 5, mouseLocation.y - 5, 10, 10);
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

	private double percentageBetweenQuarters(double previousPercentage,
			double nextPercentage) {
		return (ctx.skills.experience(14) - ((ctx.skills
				.experienceAt(ctx.skills.level(14) + 1) - ctx.skills
				.experienceAt(ctx.skills.level(14)))
				* previousPercentage + ctx.skills.experienceAt(ctx.skills
				.level(14))))
				/ ((ctx.skills.experienceAt(ctx.skills.level(14) + 1) - ctx.skills
						.experienceAt(ctx.skills.level(14))) * nextPercentage - (ctx.skills
						.experienceAt(ctx.skills.level(14) + 1) - ctx.skills
						.experienceAt(ctx.skills.level(14)))
						* previousPercentage);

	}

	// ---- End Paint Methods ----

	private int[] getAssignmentItemIds() {
		if (currentFishingLevel < 20) {
			return new int[] { 13435 };
		} else if (currentFishingLevel < 40) {
			return new int[] { 335, 331 };
		} else {
			return new int[] { 377 };
		}
	}

	private int[] extraItemIds() {
		if (getAssignmentString().equalsIgnoreCase("Lobster")) {
			return new int[] { 314, 331, 335, 13435 };
		} else if (getAssignmentString().equalsIgnoreCase("Trout/Salmon")) {
			return new int[] { 13435 };
		} else {
			return new int[] { 0 };
		}
	}

	private int[] junkItemIds() {
		if (getAssignmentString().equalsIgnoreCase("Lobster")) {
			return new int[] { 317, 321 };
		} else if (getAssignmentString().equalsIgnoreCase("Trout/Salmon")) {
			return new int[] { 0 };
		} else {
			return new int[] { 0 };
		}
	}

	private int getAssignmentNpcIds() {
		if (currentFishingLevel < 20) {
			return 6267;
		} else if (currentFishingLevel < 40) {
			// return 328; // Barb village npc id
			return 329; // River lum npc id
		} else {
			return 324;
		}
	}

	private String getAssignmentActionString() {
		if (currentFishingLevel < 20) {
			return "Cage";
		} else if (currentFishingLevel < 40) {
			return "Lure";
		} else {
			return "Cage";
		}
	}

	private String getAssignmentString() {
		if (currentFishingLevel < 20) {
			return "Crayfish";
		} else if (currentFishingLevel < 40) {
			return "Trout/Salmon";
		} else {
			return "Lobster";
		}
	}

	private Area getAssignmentFishingArea() {
		if (currentFishingLevel < 20) {
			return new Area(new Tile(3256, 3208), new Tile(3259, 3203));
		} else if (currentFishingLevel < 40) {
			return new Area(new Tile(3239, 3255), new Tile(3242, 3241));
		} else {
			return new Area(new Tile(2919, 3184), new Tile(2928, 3172));
		}
	}

	private Area getAssignmentBankArea() {
		if (getAssignmentString().equalsIgnoreCase("Lobster")) {
			if (inKaramja() || inBoat()) {
				return stilesArea();
			} else {
				return new Area(new Tile(3213, 3259), new Tile(3216, 3254));
			}
		} else {
			return new Area(new Tile(3213, 3259), new Tile(3216, 3254));
		}
	}

	private Area portSarimSailorArea() {
		return new Area(new Tile(3022, 3224), new Tile(3029, 3212));
	}

	/*
	 * private Area inBoatArea() { return new Area(new Tile(2952, 3144), new
	 * Tile(2962, 3140)); }
	 */

	private Area stilesArea() {
		return new Area(new Tile(2848, 3146), new Tile(2853, 3141));
	}

	/*
	 * private Area generalStoreArea() {// npcid 520 return new Area(new
	 * Tile(3211, 3243), new Tile(3218, 3239)); }
	 */

	private boolean inKaramja() {
		return new Area(new Tile(2816, 3206), new Tile(3959, 3135))
				.contains(ctx.players.local().tile());
	}

	private boolean inBoat() {
		return !ctx.objects.id(2082).select().isEmpty()
				&& ctx.players.local().tile().floor() == 1;
	}

	private boolean bankingEnabled() {
		if (getAssignmentString().equals("Crayfish")) {
			return false;// TODO: Make banking available so that we can buy at
							// least
			// 50 feathers and a fly fishing rod if it is not in the toolbelt
		} else {
			if (bankingEnabled) {
				return true;
			} else {
				return false;
			}
		}
	}

	/*
	 * private boolean sellExtra() { return sellExtra; }
	 */

	@SuppressWarnings("unused")
	private boolean hasExtraItems() {
		for (int i = 0; i < extraItemIds().length; i++) {
			if (ctx.backpack.select().id(extraItemIds()[i]).count() >= 1) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/*
	 * private boolean atGeneralStore() { return
	 * generalStoreArea().contains(ctx.players.local()); }
	 */

	private boolean hasRequiredGear() {
		if (getAssignmentString().equalsIgnoreCase("Crayfish")) {
			return true;
		} else {
			if (getAssignmentString().equalsIgnoreCase("Trout/Salmon")) {
				if (ctx.backpack.select().id(314).count(true) < 1) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean hasJunkFish() {
		for (int i = 0; i < junkItemIds().length; i++) {
			if (ctx.backpack.select().id(junkItemIds()[i]).count() >= 1) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	// ---- End Assignment Methods ----

	private boolean combineCamera(final int angle, final int pitch) {
		final Runnable setAngle = new Runnable() {
			@Override
			public void run() {
				ctx.camera.angle(angle);
			}
		};
		final Runnable setPitch = new Runnable() {
			@Override
			public void run() {
				ctx.camera.pitch(pitch);
			}
		};

		if (Random.nextInt(0, 100) < 50) {
			new Thread(setAngle).start();
			new Thread(setPitch).start();
		} else {
			new Thread(setPitch).start();
			new Thread(setAngle).start();
		}

		return Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return ctx.camera.pitch() == pitch && ctx.camera.yaw() == angle;
			}
		}, Random.nextInt(50, 200), Random.nextInt(4, 8));
	}

}
