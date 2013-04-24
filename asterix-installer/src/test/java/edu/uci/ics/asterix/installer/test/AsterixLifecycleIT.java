package edu.uci.ics.asterix.installer.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import edu.uci.ics.asterix.installer.command.CommandHandler;
import edu.uci.ics.asterix.installer.error.VerificationUtil;
import edu.uci.ics.asterix.installer.model.AsterixInstance;
import edu.uci.ics.asterix.installer.model.AsterixInstance.State;
import edu.uci.ics.asterix.installer.model.AsterixRuntimeState;
import edu.uci.ics.asterix.installer.service.ServiceProvider;
import edu.uci.ics.asterix.test.aql.TestsUtils;
import edu.uci.ics.asterix.testframework.context.TestCaseContext;


public class AsterixLifecycleIT {

    private static final int NUM_NC = 1;
    private static final CommandHandler cmdHandler = new CommandHandler();
    private static final String PATH_BASE = "src/test/resources/integrationts/lifecycle";
    private static final String PATH_ACTUAL = "ittest/";
    private static final Logger LOGGER = Logger.getLogger(AsterixLifecycleIT.class.getName());
    private static List<TestCaseContext> testCaseCollection;

    @BeforeClass
    public static void setUp() throws Exception {
        AsterixInstallerIntegrationUtil.init();
        TestCaseContext.Builder b = new TestCaseContext.Builder();
        testCaseCollection = b.build(new File(PATH_BASE));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        AsterixInstallerIntegrationUtil.deinit();
    }

    @Parameters
    public static Collection<Object[]> tests() throws Exception {
        Collection<Object[]> testArgs = new ArrayList<Object[]>();
        return testArgs;
    }

    @Test
    public void testStopActiveInstance() throws Exception {
        try {
            AsterixInstallerIntegrationUtil.transformIntoRequiredState(State.ACTIVE);
            String command = "stop -n " + AsterixInstallerIntegrationUtil.ASTERIX_INSTANCE_NAME;
            cmdHandler.processCommand(command.split(" "));
            AsterixInstance instance = ServiceProvider.INSTANCE.getLookupService().getAsterixInstance(
                    AsterixInstallerIntegrationUtil.ASTERIX_INSTANCE_NAME);
            AsterixRuntimeState state = VerificationUtil.getAsterixRuntimeState(instance);
            assert (state.getFailedNCs().size() == NUM_NC && !state.isCcRunning());
            System.out.println("Test stop active instance PASSED");
        } catch (Exception e) {
            throw new Exception("Test configure installer " + "\" FAILED!", e);
        }
    }

    @Test
    public void testStartActiveInstance() throws Exception {
        try {
            AsterixInstallerIntegrationUtil.transformIntoRequiredState(State.INACTIVE);
            String command = "start -n " + AsterixInstallerIntegrationUtil.ASTERIX_INSTANCE_NAME;
            cmdHandler.processCommand(command.split(" "));
            AsterixInstance instance = ServiceProvider.INSTANCE.getLookupService().getAsterixInstance(
                    AsterixInstallerIntegrationUtil.ASTERIX_INSTANCE_NAME);
            AsterixRuntimeState state = VerificationUtil.getAsterixRuntimeState(instance);
            assert (state.getFailedNCs().size() == 0 && state.isCcRunning());
            System.out.println("Test start active instance PASSED");
        } catch (Exception e) {
            throw new Exception("Test configure installer " + "\" FAILED!", e);
        }
    }

    @Test
    public void testDeleteActiveInstance() throws Exception {
        try {
            AsterixInstallerIntegrationUtil.transformIntoRequiredState(State.INACTIVE);
            String command = "delete -n " + AsterixInstallerIntegrationUtil.ASTERIX_INSTANCE_NAME;
            cmdHandler.processCommand(command.split(" "));
            AsterixInstance instance = ServiceProvider.INSTANCE.getLookupService().getAsterixInstance(
                    AsterixInstallerIntegrationUtil.ASTERIX_INSTANCE_NAME);
            assert (instance == null);
            System.out.println("Test delete active instance PASSED");
        } catch (Exception e) {
            throw new Exception("Test delete active instance " + "\" FAILED!", e);
        } finally {
            // recreate instance
            AsterixInstallerIntegrationUtil.createInstance();
        }
    }

    @Test
    public void test() throws Exception {
        for (TestCaseContext testCaseCtx : testCaseCollection) {
            TestsUtils.executeTest(PATH_ACTUAL, testCaseCtx);
        }
    }
    
    public static void main(String[] args) throws Exception {
        try {
            setUp();
            new AsterixLifecycleIT().test();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("TEST CASES FAILED");
        } finally {
            tearDown();
        }
    }


}
