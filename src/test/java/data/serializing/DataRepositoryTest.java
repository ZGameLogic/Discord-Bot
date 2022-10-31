package data.serializing;

import bot.role.data.structures.KingData;
import bot.role.data.jsonConfig.GameConfigValues;
import org.junit.jupiter.api.*;

import java.io.File;

class DataRepositoryTest {

    private DataRepository<SavableData> data;
    private static final String TESTING_DIR = "testing\\arena";
    private static DataRepository<GameConfigValues> gcvData;

    @BeforeAll
    static void generateGCV(){
        gcvData = new DataRepository<>("game_config");
        if(!gcvData.exists("game config values")) {
            GameConfigValues gcv = new GameConfigValues();
            gcv.setId("game config values");
            gcvData.saveSerialized(gcv);
        }
    }

    @AfterAll
    static void deleteTestDir(){
        File testingDir = new File(TESTING_DIR);
        testingDir.delete();
        testingDir.getParentFile().delete();
    }

    @BeforeEach
    void clearDataCacher(){
        data = null;
    }

    @AfterEach
    void removeTestData(){
        data.deleteDir();
    }

    @Test
    @Timeout(value = 2)
    void kingDataSaveLoadTest(){
        KingData kd = new KingData();
        kd.addPlayerKingFought(123456);
        data = new DataRepository<>(TESTING_DIR + "king data");
        data.saveSerialized(kd);
        assert (data.exists("king"));
        KingData newKd = (KingData) data.loadSerialized();
        assert (kd.equals(newKd));
    }


}