package me.bomb.parkourbeat;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class VoidGenerator extends ChunkGenerator {
	
	protected VoidGenerator() {
	}
   
    public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid){
        return new byte[16][];
    }
}