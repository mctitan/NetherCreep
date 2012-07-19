package net.mctitan.fluid;

import net.mctitan.FluidFlow.BlockData;

public class Data extends BlockData {
    public long time;
    public long distance;
    public long tries;
    
    public Data() {
        this(0);
    }
    
    public Data(long delta) {
        this(delta,1);
    }
    
    public Data(long delta, long d) {
        time = System.nanoTime()+delta;
        distance = d;
        tries = 0;
    }
}
