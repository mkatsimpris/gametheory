package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class NLHEHUPushFoldSNGBattleTest {

	/**
	 * TODO
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testNLHEHUPushFoldSNGBattle() throws Exception {

		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		Path path = Paths.get(System.getProperty("java.io.tmpdir"),
				"PushFoldBattleTest");
		if (!Files.exists(path))
			Files.createDirectories(path);
		NLHEHUPushFoldSNGBattle battle = new NLHEHUPushFoldSNGBattle(
				Paths.get("/home/pitt/PushFoldBattleTest"), 5, 10, 150, 5);
	}
}
