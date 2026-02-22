package brig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            log.info("Starting BRIG...");
            Thread BrigMain = new Thread(new BRIG(), "BRIG-Main");
            BrigMain.start();
        } catch (Exception e) {
            log.error("Failed to start BRIG application", e);
        }
    }
}
