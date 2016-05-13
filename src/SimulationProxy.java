/**
 * @author Sean Levorse
 *
 * Interfaces between simulations and the program to prevent unregulated access to the user's portfolio during simulations
 */
public class SimulationProxy {

    // There should only be one Simulation Manager
    private static SimulationProxy instance = null;
    boolean ran;
    //Variables
    private Portfolio simulationPortfolio;
    private Simulation sim;
    private Portfolio[] result;

    /**
     * Protected constructor to prevent overriding
     */
    private SimulationProxy() {
        this.reset();
    }

    /**
     * Gets the instance of Simulation Manager
     *
     * @return the instance
     */
    public static SimulationProxy getInstance() {
        // If the instance doesn't exist yet, create it
        if (instance == null) {
            instance = new SimulationProxy();
        }

        return instance;
    }

    // Methods

    //  Getters
    public Portfolio[] getResult() {
        return result;
    }

    public boolean beenRun() {
        return ran;
    }

    /**
     * Resets the simulation as well as restores equity values back to their original value
     */
    public void reset() {
        // copy the user's real portfolio for modification
        simulationPortfolio = PortfolioAdapter.currentUser.clone();
        ran = false;
    }

    /**
     * Creates a market simulation
     *
     * @param steps       The number of steps the interval should simulate
     * @param interval    The amount of time between each step
     * @param growthRatio The annual growth percentage of each equity
     * @throws IllegalArgumentException if the steps argument is less than 1
     */
    public void createSimulation(int steps, TimeInterval interval, double growthRatio) {
        // Create a new simulation object
        if (steps <= 0) {
            throw new IllegalArgumentException("Steps must be greater than 0");
        }
        sim = new Simulation(simulationPortfolio, steps, interval, growthRatio);
    }


    /**
     * Creates a market simulation
     *
     * @param interval    The amount of time between each step
     * @param growthRatio The annual growth percentage of each equity
     */
    public void createSimulation(TimeInterval interval, double growthRatio) {
        // Create a new simulation object
        sim = new Simulation(simulationPortfolio, 1, interval, growthRatio);
    }

    /**
     * Runs the simulation
     *
     * @param runInSteps true if you would like all of the steps to be run instead of just skipping to the end
     * @return true if the method successfully ran the simulation
     */
    public boolean run(boolean runInSteps) {
        if (ran) {
            /* Redacted for now since we aren't required to ask the user if he wants to rerun a simulation
            // Prompt the user if they want to run the simulation again without resetting
            Object[] options = {"Continue", "Cancel"};
            int n = JOptionPane.showOptionDialog(
                    null,
                    "This action will rerun the simulation off of the current simulation, would you like to continue?",
                    "Rerun Simulation?",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[1]);

            // if not, leave the run method
            if (n == JOptionPane.CANCEL_OPTION){
                return false;
            }
            */
            // Run the simulation based on the runInSteps variable and append
            // the result to old result object
            if (runInSteps) {
                // Get the new and old simulation
                Portfolio[] newSim = sim.runStepByStep();
                Portfolio[] oldSim = result.clone();

                // Merge the results into one array
                result = new Portfolio[oldSim.length + newSim.length - 1];
                for (int i = 0; i < result.length; i++) {
                    if (i < oldSim.length) {
                        result[i] = oldSim[i];
                    } else {
                        result[i] = newSim[i - oldSim.length + 1];
                    }
                }
            } else {
                result = new Portfolio[1];
                result[0] = sim.runFinal();
            }
            return true;
        }

        // Run the simulation based on the runInSteps variable
        if (runInSteps) {
            result = sim.runStepByStep();
        } else {
            result = new Portfolio[1];
            result[0] = sim.runFinal();
        }
        ran = true;

        return true;
    }

    /**
     * Updates a simulation when the values have been changed
     *
     * @param steps
     * @param timePerStep
     * @param perAnnum
     */
    public void changeSimulation(int steps, TimeInterval timePerStep, double perAnnum) {
        sim.changeSteps(steps);
        sim.changeTimeSteps(timePerStep);
        sim.changePerAnnum(perAnnum);
    }
}
