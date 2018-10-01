package gov.nih.nlm.malaria_screener.custom;

/**
 * Created by yuh5 on 4/29/2016.
 */
public class RowItem_CellCount {

    private String title;
    private int cells;
    private int infectedCells;

    public RowItem_CellCount(String title, int cells, int infectedCells) {

        this.title = title;
        this.cells = cells;
        this.infectedCells = infectedCells;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }

    public void setInfectedCells(int infectedCells) {
        this.infectedCells = infectedCells;
    }

    public String getTitle() {
        return title;
    }

    public int getInfectedCells() {
        return infectedCells;
    }

    public int getCells() {
        return cells;
    }
}
