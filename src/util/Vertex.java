package util;

import java.io.Serializable;

import map.Cell;

/**
 * Vertice for a graph structure
 * @author santiagobernal
 *
 */
public class Vertex implements Serializable{ 
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	final private String id;
    final private Cell cell;


    public Vertex(String id, Cell cell) {
        this.id = id;
        this.cell = cell;
    }
    public String getId() {
        return id;
    }

    public Cell getCell() {
        return cell;
    }

    @Override
    public int hashCode() {
        return this.cell.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex) obj;
        if (cell == null) {
            if (other.cell != null)
                return false;
        } else if (!cell.equals(other.cell))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return cell.toString();
    }

}