package iriakar.plugins.firstplugin;

import java.io.Serializable;

public class Direction implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Directions {
		NORTH("NORTH"), EAST("EAST"), SOUTH("SOUTH"), WEST("WEST");
		
		String directionAsString;
		
		private Directions(String string) {
			this.directionAsString = string;
		}
		
		@Override
		public String toString() {
			return directionAsString;
		}
	}
	
	Directions direction;
	
	public Direction(Directions direction) {
		this.direction = direction;
	}
	
	public Direction() {
		this(Directions.NORTH);
	}
	
	@Override
	public String toString() {
		return direction.toString();
	}
	
	public Directions getDirection() {
		return direction;
	}
	
	public Directions setDirection(Directions direction) {
		this.direction = direction;
		return direction;
	}
	
	public Directions flipped() {
		
		Directions newDirection;
		
		switch(this.direction) {
		case NORTH:
			newDirection =  Directions.SOUTH;
			break;
		case SOUTH:
			newDirection =  Directions.NORTH;
			break;
		case EAST:
			newDirection =  Directions.WEST;
			break;
		case WEST:
			newDirection =  Directions.EAST;
			break;
		default:
			newDirection =  Directions.SOUTH;
			break;
		}
		
		return newDirection;
	}
	
	public Directions flip() {
		direction = flipped();
		return direction;
	}
	
	public Directions rotateClockwise() {
		
		switch(direction) {
		case NORTH:
			direction = Directions.EAST;
			break;
		case EAST:
			direction = Directions.SOUTH;
			break;
		case SOUTH:
			direction = Directions.WEST;
			break;
		case WEST:
			direction = Directions.NORTH;
			break;
		default:
			direction =  Directions.SOUTH;
			break;
		}
		
		return direction;
	}
	
	public Directions rotateCounterClockwise() {
		switch(direction) {
		case NORTH:
			direction =  Directions.WEST;
			break;
		case SOUTH:
			direction =  Directions.EAST;
			break;
		case EAST:
			direction =  Directions.NORTH;
			break;
		case WEST:
			direction =  Directions.SOUTH;
			break;
		default:
			direction =  Directions.SOUTH;
			break;
		}
		
		return direction;
	}
}
