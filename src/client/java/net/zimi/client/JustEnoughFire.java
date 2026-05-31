package net.zimi.client;

import net.fabricmc.api.ClientModInitializer;

public class JustEnoughFire implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        IO.println("Just Enough Fire!");
	}
}