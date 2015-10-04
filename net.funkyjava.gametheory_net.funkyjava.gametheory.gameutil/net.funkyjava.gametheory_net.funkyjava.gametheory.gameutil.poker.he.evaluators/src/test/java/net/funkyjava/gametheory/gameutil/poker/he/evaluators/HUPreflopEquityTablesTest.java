package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

public class HUPreflopEquityTablesTest {

	@Test
	public void test() throws IOException {
		HUPreflopEquityTables.compute();
		HUPreflopEquityTables
				.writeTo(Paths.get("/Users/pitt/HE_HU_EQUITY.zip"));
	}
}
