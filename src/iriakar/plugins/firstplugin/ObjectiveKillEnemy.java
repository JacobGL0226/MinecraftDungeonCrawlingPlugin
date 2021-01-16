package iriakar.plugins.firstplugin;

import java.util.ArrayList;

import org.bukkit.entity.LivingEntity;

public class ObjectiveKillEnemy implements Objective {
	
	private ArrayList<LivingEntity> enemies;
	
	public ObjectiveKillEnemy(ArrayList<LivingEntity> enemies) {
		this.enemies = enemies;
	}

	@Override
	public boolean isCompleted() {
		return (enemies.size() < 1);
	}
	
	@Override
	public String toString() {
		return "You must complete the objective first!";
	}
}
