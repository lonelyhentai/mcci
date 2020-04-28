package com.evernightfireworks.mcci.services;


import com.evernightfireworks.mcci.services.managers.ShadowHungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DemandPoliceService {
    private final Logger logger = LogManager.getFormatterLogger(DemandPoliceService.class.getName());

    public DemandPoliceService() {

    }

    public double calculateHungerFactor(PlayerEntity player) {
        ShadowHungerManager shm = new ShadowHungerManager(player.getHungerManager(), player);
        return shm.run();
    }
}
