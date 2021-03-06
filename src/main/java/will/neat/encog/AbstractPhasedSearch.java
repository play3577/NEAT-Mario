package will.neat.encog;

import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.opp.OperationList;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Strategy;
import org.encog.util.obj.ObjectHolder;

/**
 * Created by hardwiwill on 25/01/17.
 */
public abstract class AbstractPhasedSearch implements Strategy {

    protected TrainEA neat;

    public enum Phase { COMPLEXIFICATION, SIMPLIFICATION }
    protected Phase phase = Phase.SIMPLIFICATION;

    // last generation switch
    protected int lastTransitionGeneration = 0;

    // operations that are phase specific (additive/substractive mutations)
    protected OperationList[] phaseOps = new OperationList[2];

    protected AbstractPhasedSearch() {
        phaseOps[0] = new OperationList();
        phaseOps[1] = new OperationList();
    }

    public void addPhaseOp(int phase, double prob, EvolutionaryOperator op) {
        phaseOps[phase].add(prob, op);
        op.init(neat);
    }

    @Override
    public void init(MLTrain train) {
        this.neat = (TrainEA) train;

        for (OperationList list : phaseOps) {
            for (ObjectHolder<EvolutionaryOperator> op : list.getList()) {
                op.getObj().init(this.neat);
            }
        }

        addOps();
    }

    private void addOps() {
        // add ops of the current phase
        this.neat.getOperators().getList().addAll(phaseOps[phase.ordinal()].getList());

        // finalize (make probabilities add to 1)
        this.neat.getOperators().finalizeStructure();
    }

    @Override
    public void postIteration() { }

    protected void switchPhase() {
        Phase last = phase;
        if (phase == Phase.COMPLEXIFICATION) {
            phase = Phase.SIMPLIFICATION;
        } else phase = Phase.COMPLEXIFICATION;

        // remove operations associated with the last phase
        neat.getOperators().getList().removeIf(objectHolder ->
                phaseOps[last.ordinal()].getList().stream().anyMatch(phaseOp ->
                        phaseOp.getObj().getClass() == objectHolder.getObj().getClass()
                )
        );

        addOps();

        lastTransitionGeneration = neat.getIteration();

        System.out.println("Phase changed to : " + phase);
//        System.out.println("operators: " + train.getOperators().getList());
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }
}
