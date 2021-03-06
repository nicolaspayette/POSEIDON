package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.floor;


/**
 * This is a value class that stores some essential geographic properties of a NauticalMap so that
 * classes that are only concerned with those can share instances between simulations by avoiding
 * a dependency on the full NauticalMap.
 */
public final class MapExtent {
    private final int gridWidth;   // the width in cells
    private final int gridHeight;  // the height in cells
    private final double cellWidth;   // the width of a cell in degrees
    private final double cellHeight;  // the height of a cell in degrees
    private final Envelope envelope;

    public MapExtent(NauticalMap map) {
        this(map.getWidth(), map.getHeight(), map.getRasterBathymetry().getMBR());
    }

    public MapExtent(int gridWidth, int gridHeight, Envelope envelope) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.envelope = checkNotNull(envelope);
        this.cellWidth = envelope.getWidth() / (double)this.getGridWidth();
        this.cellHeight = envelope.getHeight() / (double)this.getGridHeight();
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gridWidth, gridHeight, envelope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapExtent mapExtent = (MapExtent) o;
        return gridWidth == mapExtent.gridWidth && gridHeight == mapExtent.gridHeight && envelope.equals(mapExtent.envelope);
    }

    public int toGridX(double longitude) {
        return (int) floor((longitude - envelope.getMinX()) / cellWidth);
    }

    public int toGridY(double latitude) {
        return (int) floor((envelope.getMaxY() - latitude) / cellHeight);
    }
}
