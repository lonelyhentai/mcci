package com.evernightfireworks.mcci.services;


import com.evernightfireworks.mcci.services.managers.ShadowDevelopmentManager;
import com.evernightfireworks.mcci.services.managers.ShadowHungerManager;
import com.evernightfireworks.mcci.services.managers.ShadowProductionManager;
import com.evernightfireworks.mcci.services.managers.ShadowSafetyManager;
import net.minecraft.entity.player.PlayerEntity;

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

    public static int calculateDevelopmentFactor(PlayerEntity player) {
        ShadowDevelopmentManager sdm = new ShadowDevelopmentManager(player);
        return sdm.run();
    }

    public static int calculateRiskFactor(int hunger, int safety, int production, int development) {
        return 100 - (int)Math.ceil(Math.pow(((double) (100 - production) * (100 - development) * (safety) * (hunger)), 0.25));
    }
}
