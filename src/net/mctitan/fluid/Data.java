package net.mctitan.fluid;

import net.mctitan.FluidFlow.BlockData;

public class Data extends BlockData {
    public long distance;
    public long tries;
    
    public Data() {
        this(0);
    }
    
    public Data(long d) {
        distance = d;
        tries = 0;
    }
}
