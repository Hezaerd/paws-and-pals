package com.hezaerd;

import com.hezaerd.utils.Wisdom;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PawsPals implements ModInitializer {
	public static final String MOD_ID = "pawspals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Wisdom.spread();
	}
}