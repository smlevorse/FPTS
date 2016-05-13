/**
 * @author Sean Levorse
 *
 * Creates and runs a simulation based on user specified values
 */
public class Simulation {

    // Variables
    private Portfolio simPort;          // The portfolio the simulation will be running on
    private int steps;                  // The number of steps the simulation will run
    private TimeInterval timeIntervalPerStep;    // The amount of time each step represents
    private double growthRate;           // The annual growth rate
    private double stepGrowthRate;       // The growth rate per step

    // Methods

    /**
     * Constructor
     *
     * @param portfolio        The Portfolio that will be simulated
     * @param numSteps         The number of time steps that will be simulated
     * @param timeStepInterval The amount of time each step covers
     * @param annualGrowthRate The growth per year percentage
     */
    public Simulation(Portfolio portfolio, int numSteps, TimeInterval timeStepInterval, double annualGrowthRate) {
        simPort = portfolio;
        steps = numSteps;
        timeIntervalPerStep = timeStepInterval;
        growthRate = annualGrowthRate;
        calculateStepInterval();
    }


    /**
     * Runs the simulation step by step modifying the simulations portfolio
     *
     * @return an array of portfolios of size step + 1 where the first element is the original portfolio and each subsequent element is the next step in the simulation
     */
    public Portfolio[] runStepByStep() {
        // Variables
        Portfolio[] history = new Portfolio[steps + 1];
        history[0] = simPort.clone();

        // Loop through "time" and adjust equity values in the portfolio
        for (int i = 1; i <= steps; i++) {
            // Adjust equity values
            for (int j = 0; j < simPort.getHoldings().size(); j++) {
                Holding h = simPort.getHoldings().get(j);

                //Multiply the holding price by the growth rate
                h.pricePerUnit += history[0].getHoldings().get(j).getUnitPrice() * stepGrowthRate;
                if (h.pricePerUnit < 0.0f) {
                    h.pricePerUnit = 0.0f;
                }
            }

            //copy the portfolio to the history
            history[i] = simPort.clone();
        }
        return history;
    }

    /**
     * runs the simulation and returns the modified portfolio
     *
     * @return the resulting portfolio after running the simulation
     */
    public Portfolio runFinal() {
        // Adjust equity values
        for (Holding h : simPort.getHoldings()) {
            //Multiply the holding price by the growth rate
            h.pricePerUnit *= 1 + (stepGrowthRate * steps);
        }

        //copy the portfolio to the history
        return simPort;
    }

    /**
     * Calculates the growth rate per time interval based on the annual growth rate
     */
    private void calculateStepInterval() {
        switch (timeIntervalPerStep) {
            case Day:
                stepGrowthRate = growthRate / 365;
                break;
            case Month:
                stepGrowthRate = growthRate / 12;
                break;
            case Year:
                stepGrowthRate = growthRate;
                break;
        }
    }

    /**
     * Update the simulation's internal values when the per annum percentage changes
     *
     * @param newPA
     */
    public void changePerAnnum(double newPA) {
        growthRate = newPA;
        calculateStepInterval();
    }

    /**
     * Update the simulation's internal values when the time interval changes
     *
     * @param newTI
     */
    public void changeTimeSteps(TimeInterval newTI) {
        timeIntervalPerStep = newTI;
        calculateStepInterval();
    }

    /**
     * Update the simulation's internal values when the number of steps changes
     *
     * @param newNumSteps
     */
    public void changeSteps(int newNumSteps) {
        steps = newNumSteps;
        calculateStepInterval();
    }
}
