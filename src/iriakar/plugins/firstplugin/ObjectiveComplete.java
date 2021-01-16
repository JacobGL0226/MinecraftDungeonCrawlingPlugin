package iriakar.plugins.firstplugin;

public class ObjectiveComplete implements Objective {

	@Override
	public boolean isCompleted() {
		return true;
	}
	
	@Override
	public String toString() {
		return "You must complete the objective first!";
	}
	
}
