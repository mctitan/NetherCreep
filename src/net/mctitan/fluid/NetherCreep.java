package net.mctitan.fluid;

import java.util.HashSet;
import java.util.Random;
import net.mctitan.FluidFlow.Fluid;
import net.mctitan.FluidFlow.FluidBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class NetherCreep extends Fluid {
    private BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN,
                                 BlockFace.NORTH, BlockFace.SOUTH,
                                 BlockFace.EAST, BlockFace.WEST};
    private BlockFace[] up = {BlockFace.UP};
    private long flow = 10L*1000L*1000L*1000L;
    private long maxDistance = 100;
    private long maxTries = 6;
    private long spongeDistance = 5;
    private long recursionDepth = 1000;
    private HashSet<Material> overwrite;
    private HashSet<Material> flowable;
    private Random random;
    
    @Override
    public org.bukkit.Material getMaterial() {
        return Material.NETHERRACK;
    }

    @Override
    public void flow(FluidBlock fb) {
        Material temp;
        int mask = random.nextInt(64)<<1;
        int bit = 1;
        long tries;
        
        while(fb.data != null && ((Data)fb.data).time > System.nanoTime())
            try{Thread.sleep(0, 1000);}catch(Exception e) {} //sleeps 1 micro-second
        
        //if the material of the block is not correct, do nothing
        temp = getType(fb);
        if(!flowable.contains(temp))
            return;
        
        //check to see if a sponge is nearby, if so deactivate this block
        if(spongeNearby(fb))
            return;
        
        if(fb.data == null)
            fb.data = new Data();
        
        tries = ((Data)fb.data).tries;
        
        for(FluidBlock f : fb.getBlockFaces(faces)) {
            bit <<= 1;
            temp = getType(f);
            
            if((bit & mask) == 0)
                continue;
            
            ++tries;
            if(tries > maxTries)
                break;
            
            if(!canOverwrite(temp))
                continue;
            
            overwriteBlock(temp, f);
            
            addFlow(f, fb);
        }
        
        if(tries < maxTries)
            addFlow(fb, fb);
        ((Data)fb.data).tries = tries;
    }
    
    public boolean spongeNearby(FluidBlock fb) {
        FluidBlock sponge = new FluidBlock(fb.loc);
        for(long i  = -spongeDistance; i <= spongeDistance; ++i) {
            for(long j = -spongeDistance; j <= spongeDistance; ++j) {
                for(long k = -spongeDistance; k < spongeDistance; ++k) {
                    sponge.loc = new Location(fb.loc.getWorld(), fb.loc.getX()+i, fb.loc.getY()+j, fb.loc.getZ()+k);
                    if(getType(sponge) == Material.SPONGE) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean canOverwrite(Material mat) {
        return overwrite.contains(mat);
    }
    
    public void overwriteBlock(Material temp, FluidBlock f) {
        FluidBlock above = f.getBlockFaces(up).getFirst();
        changeAbove(above);
        
        if(temp == Material.DIRT || temp == Material.GRASS)
            setType(f, Material.NETHERRACK);
        else if(temp == Material.SAND)
            setType(f, Material.SOUL_SAND);
    }
    
    public void changeAbove(FluidBlock above) {
        changeAbove(above, 0);
    }
    
    public void changeAbove(FluidBlock above, long distance) {
        Material temp = getType(above);
        
        if(distance >= recursionDepth)
            return;
        
        if(temp == Material.SUGAR_CANE_BLOCK || temp == Material.CACTUS) {
            changeAbove(above.getBlockFaces(up).getFirst(), distance+1);
            setType(above, Material.GLOWSTONE);
        } else if(temp == Material.LOG || temp == Material.LEAVES) {
            if(temp == Material.LOG)
                setType(above, Material.MOSSY_COBBLESTONE);
            else if(temp == Material.LEAVES)
                setType(above, Material.AIR);
            
            for(FluidBlock fb : above.getBlockFaces(faces))
                changeAbove(fb, distance+1);
        } else if(temp == Material.LONG_GRASS)
            setType(above, Material.FIRE);
        else if(temp == Material.SNOW)
            setType(above, Material.AIR);
    }
    
    @Override
    public void addFlow(FluidBlock to) {
        addFlow(to, null);
    }
    
    public void addFlow(FluidBlock to, FluidBlock from) {
        long distance = 0;
        if(from != null && from.data != null && from.data instanceof Data)
            distance = ((Data)from.data).distance+1;
        
        //if distance is too long, do nothing
        if(distance >= maxDistance)
            return;
        
        to.data = new Data(flow, distance);
        super.addFlow(to);
    }

    @Override
    public void init() {
        //create objects
        overwrite = new HashSet<>();
        flowable = new HashSet<>();
        random = new Random(System.nanoTime());
        
        //read in configuration file
        flow = getConfig().getLong("NetherCreep.flowdelay");
        maxDistance = getConfig().getLong("NetherCreep.maxDistance");
        maxTries = getConfig().getLong("NetherCreep.maxTries");
        spongeDistance = getConfig().getLong("NetherCreep.spongeDistance");
        recursionDepth = getConfig().getLong("NetherCreep.recursionDepth");
        
        //other shit that should be in the config file
        flowable.add(Material.NETHERRACK);
        flowable.add(Material.SOUL_SAND);
        overwrite.add(Material.DIRT);
        overwrite.add(Material.GRASS);
        overwrite.add(Material.SAND);
        overwrite.add(Material.LOG);
        
    }
}
