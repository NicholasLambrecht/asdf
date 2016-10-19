package Entity.Mob;

import Entity.Projectile.EFireball;
import Entity.Spawner.BloodParticleSpawner;
import Game.Screen;
import Graphics.Sprite;
import Sounds.SoundEffect;
import UI.DamageBattleText;
import UI.HealthBar;

public class Mage extends Mob {

	private int ay, ax;
	private double aAx, aAy;
	HealthBar hpb;
	private int anim = 0;
	private int animSPD = 3;
	int invulnFrame = 0;
	int invulnFrames = 4;
	private boolean casting = true;
	private int loading = 0;
	private int shootingWU = 0;
	private int spd = 4;

	public Mage(int x, int y) {
		sprite = Sprite.archer;
		this.x = x;
		this.y = y;
		maxHp = 120;
		setHp(120);
		damage = 15;
		expValue = 14;
		hpb = new HealthBar(x, y - 6, getHp());
		expLevel = 12;
	}

	public boolean playerIsNear() {
		boolean result = false;
		int px = level.player.x;
		int py = level.player.y;

		int dx = Math.abs(px - x);
		int dy = Math.abs(py - y);

		if (dx < 1350 && dy < 532) {
			result = true;
		}
		return result;
	}

	public void initDif(int difficulty) {
		if (difficulty == 1) {
			setHp(getHp() * 2);
			expValue = expValue / 3;
			damage = damage * 2;
			hpb.updateMaX(getHp());
		}
	}

	public void update() {
		burnDamage();
		invulnFrame++;
		if (invulnFrame >= invulnFrames) {
			invulnFrame = invulnFrames;
		}

		if (AI && playerIsNear()) runAI();

		if (!playerIsNear()) {
			aAy = 0;
			aAx = 0;
			ax = 0;
			ay = 0;
			// x = 86 * 32;
			// y = 61 * 32;
		}

		if (!collision(x, y + 4 * 32)) {
			//	aAy += 1;
		}
		if (!collision(x, y + 4)) {
			//aAy += .01;
			//moving = true;
		}

		if (!moving) {
			aAy *= .5;
			ay *= .5;
			aAx *= .5;
			ax *= .5;
		}

		if (aAy > 2) {
			aAy = 2;
		}
		if (aAx > 2) {
			aAx = 2;
		}
		if (aAx < -2) {
			aAx = -2;
		}
		if (aAy < -2) {
			aAy = -2;
		}

		ax += aAx;
		ay += aAy;

		if (ax > 5) {
			ax = 5;
		}
		if (ay > 5) {
			ay = 5;
		}

		int nx = x + ax;

		if (ay > 0) {
			int nLy = (int) ay;
			for (int i = 0; i < nLy; i++) {
				if (!collision(x, y + 1)) {
					y++;
				} else break;
			}
		} else if (ay < 0) {
			int nLy = (int) -ay;
			for (int i = 0; i < nLy; i++) {
				if (!collision(x, y - 1)) {
					y--;
				} else break;
			}
		}
		ay = 0;
		if (!collision(nx, y - 1)) {
			if (ax > 0) {
				for (int i = 0; i < ax; i++) {
					x = x + 1;
					if (collision(x, y - 1)) {
						x--;
						i = (int) ax;
					}
				}
			}
			if (ax < 0) {
				int aLx = (int) -ax;
				for (int i = 0; i < aLx; i++) {
					x = x - 1;
					if (collision(x, y - 1)) {
						x++;
						i = (int) aLx;
					}
				}
			}
		}
		ax = 0;
		while (collision(x, y)) {
			y--;
		}

		anim++;
		if (anim > animSPD * 11) anim = 0;
		findSprite();

		hpb.update(x, y - 6, getHp());
		// x = 200; y = 800;
	}

	private void shoot() {
		int xClicked = level.player.x + 32;
		int yClicked = level.player.y + 20;
		double dx = xClicked - (x + 20);
		double dy = yClicked - (y + 34);
		double direction = Math.atan2(dy, dx);
		level.add(new EFireball(x + 20, y + 34, direction, level, damage));
		SoundEffect.FIREBALL.play();
	}

	public void takeDamage(int damage) {
		if (invulnFrame >= invulnFrames && damage > 0) {
			setHp(getHp() - damage);
			damaged = true;
			if (getHp() <= 0) {
				die();
			}
			if (damage > 5) level.add(new BloodParticleSpawner((int) x + 64, (int) y + 64, 800, 100, level));
			if (damage > 0) level.addText(new DamageBattleText(x, y, damage + ""));
			invulnFrame = 0;
		}
	}

	private void findSprite() {
		if (dir == 0) {
			sprite = Sprite.mage;
			if (casting) {
				if (loading < 150/spd) {
					sprite = Sprite.mageAttack1;
				} else if (loading < 160/spd) {
					sprite = Sprite.mageAttack2;
				}
			}
		} else {
			sprite = Sprite.mageR;
			if (casting) {
				if (loading < 150/spd) {
					sprite = Sprite.mageAttack1R;
				} else if (loading < 160/spd) {
					sprite = Sprite.mageAttack2R;
				}
			}
		}
	}

	private void runAI() {
		int cd = 150 / spd;
		if(getHp() < 50) spd = 4;
		else spd = 3;
		shootingWU++;
		if (shootingWU > cd) {
			casting = true;
			shootingWU = 0;
		}

		int dx = level.player.x - x;
		int dy = level.player.y - y;
		moving = false;

		if (casting) {
			loading++;
			if (loading > 160 / spd) {
				casting = false;
				shoot();
				loading = 0;
			}
		}

		if (Math.abs(dy) + Math.abs(dx) > 400) {
			if (level.player.x > x) {
				aAx += .2;
				dir = 1;
				moving = true;
			}
			if (level.player.x < x) {
				aAx -= .2;
				dir = 0;
				moving = true;
			}
			if (level.player.y > y) {
				aAy += .2;
				moving = true;
			}
			if (level.player.y < y) {
				aAy -= .2;
				moving = true;
			}
		}
		if (Math.abs(dy) + Math.abs(dx) < 400) {
			if (level.player.x < x) {
				aAx += .2;
				dir = 0;
				moving = true;
			}
			if (level.player.x > x) {
				aAx -= .2;
				dir = 1;
				moving = true;
			}
			if (level.player.y < y) {
				aAy += .2;
				moving = true;
			}
			if (level.player.y > y) {
				aAy -= .2;
				moving = true;
			}
		}

	}

	public boolean collision(int nx, int ny) {
		boolean solid = false;
		for (int c = 0; c < 4; c++) {
			double xt = ((nx + 16) + (c % 2 * 32));
			double yt = ((ny) + (c / 2 * 64));
			int ix = (int) Math.ceil(xt);
			int iy = (int) Math.ceil(yt);
			if (c % 2 == 0) ix = (int) Math.floor(xt);
			if (c / 2 == 0) iy = (int) Math.floor(yt);
			// Insert checks here (this checks all 4 corners of the sprite,
			// refine to change hit box)
			if (level.getTile(ix / 32, iy / 32).solid()) solid = true;
			if (c % 2 == 0) {
				if (level.getTile(ix / 32 + 1, iy / 32).solid()) solid = true;
			}
			if (c / 2 == 1) {
				if (level.getTile(ix / 32, iy / 32 - 1).solid()) solid = true;
			}
		}
		return solid;
	}

	public boolean playerCollision(int nx, int ny) {
		return false;
	}

	public boolean unitCollision(int nx, int ny) {
		boolean hit = false;
		// if (mobRight >= playerStartX && mobStartX <= playerStartX +
		// playerSizeX && playerStartY + playerSizeY >= mobStartY &&
		// playerStartY <= mobBottom && invulnFrame == invulnFrames) {
		if (nx >= x && nx <= x + 64 && ny >= y && ny <= y + 64) {
			hit = true;
		}
		return hit;
	}

	protected void knockback(int knockback, int dir) {
		if (dir == 1) {
			aAx += knockback / 5;
		} else {
			aAx -= knockback / 5;
		}
	}

	public void render(Screen screen) {
		hpb.render(screen);
		screen.renderSprite(x, y, sprite, true);
	}
}
