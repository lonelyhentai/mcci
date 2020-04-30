package com.evernightfireworks.mcci.services;


import com.evernightfireworks.mcci.services.managers.ShadowDevelopmentManager;
import com.evernightfireworks.mcci.services.managers.ShadowHungerManager;
import com.evernightfireworks.mcci.services.managers.ShadowProductionManager;
import com.evernightfireworks.mcci.services.managers.ShadowSafetyManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class DemandPoliceService {

    public static int calculateHungerFactor(PlayerEntity player) {
        ShadowHungerManager shm = new ShadowHungerManager(player.getHungerManager(), player);
        return shm.run();
    }

    public static int calculateSafetyFactor(PlayerEntity player) {
        ShadowSafetyManager ssm = new ShadowSafetyManager(player);
        return ssm.run();
    }

    public static int calculateProductionFactor(PlayerEntity player) {
        ShadowProductionManager spm = new ShadowProductionManager(player);
        return spm.run();
    }

    public static int calculateDevelopmentFactor(ServerPlayerEntity player) {
        ShadowDevelopmentManager sdm = new ShadowDevelopmentManager(player);
        return sdm.run();
    }
}
