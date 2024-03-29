package optimizationAlgorithms;

import functions.Function;
import main.CustomPair;
import main.GlobalState;

import java.util.ArrayList;
import java.util.Collections;

public class PopulationGA {
    Function                    f;
    int                         precision;
    ArrayList<CandidateGA>      candidates;
    double[]                    decimalRepresentationOfBest;

    PopulationGA(Function function, int p){
        f = function;
        precision = p;
    }

    void initializePopulation() {
        candidates = new ArrayList<>();
        for (int i=0;i<GlobalState.populationSize;++i){
            CandidateGA currentCandidate = new CandidateGA(f, precision);
            currentCandidate.generateRandomCandidate();
            candidates.add(currentCandidate);
        }
    }

    void mutatePopulation() {
        for (int i=0;i<candidates.size();++i){
            candidates.get(i).mutateCandidate();
        }
    }

    void crossOverPopulation() {
        ArrayList<CustomPair<Double,Integer>> candidatesProbabilities = new ArrayList<>();
        int count = 0;
        for (int i=0;i<candidates.size();++i) {
            double value = GlobalState.randomGen.nextDouble();
            candidatesProbabilities.add(new CustomPair<>(value, i));
            if (value < GlobalState.crossOverProbability) {
                ++count;
            }
        }
        if (count % 2 == 1){
            if (GlobalState.randomGen.nextDouble() < 0.5){
                ++count;
            } else {
                --count;
            }
        }
        Collections.sort(candidatesProbabilities, ((o1, o2) -> {
            if (o1.getKey().equals(o2.getKey())){
                return 0;
            }
            if (o1.getKey() < o2.getKey()) {
                return -1;
            }
            return 1;
        }));
        for (int i=0;i<count;i+=2){
            int[] bitwiseRepresentation1 = candidates.get(candidatesProbabilities.get(i).getValue()).getBitwiseRepresentation().clone();
            int[] bitwiseRepresentation2 = candidates.get(candidatesProbabilities.get(i+1).getValue()).getBitwiseRepresentation().clone();

            int swappedBitsCount = GlobalState.randomGen.nextInt() % (bitwiseRepresentation1.length-1), aux;
            for (int j=0;j<=swappedBitsCount;++j){
                aux = bitwiseRepresentation1[j];
                bitwiseRepresentation1[j] = bitwiseRepresentation2[j];
                bitwiseRepresentation2[j] = aux;
            }

            CandidateGA c1 = new CandidateGA(f, precision);
            c1.createFromBitwiseRepresentation(bitwiseRepresentation1);
            CandidateGA c2 = new CandidateGA(f, precision);
            c2.createFromBitwiseRepresentation(bitwiseRepresentation2);

            candidates.add(c1);
            candidates.add(c2);
        }
    }

    double selection() {
        ArrayList<CandidateGA> newCandidates = new ArrayList<>();
        double[] fitness = new double[candidates.size()];
        double[] eval = new double[candidates.size()];
        double[] wheel = new double[candidates.size()];
        double totalFitness = 0;
        double bestValue = GlobalState.getTheWorstValue();
        double worstValue = 0.0;

        for (int i=0;i<candidates.size();++i){
            double curr = candidates.get(i).evaluate();
            if (GlobalState.solutionIsBetterThanBest(bestValue, curr)){
                bestValue = curr;
                decimalRepresentationOfBest = candidates.get(i).getDecimalRepresentationOfBestCandidate().clone();
            }
            if (i == 0){
                worstValue = curr;
            } else {
                worstValue = GlobalState.getWorseValue(worstValue, curr);
            }
            eval[i] = curr;
        }

        double res = bestValue;
        worstValue = 1.1 * worstValue;
        for (int i=0;i<candidates.size();++i){
            fitness[i] = 1/(eval[i]+0.1);
            //fitness[i] = Math.abs(worstValue - eval[i]);
            totalFitness += fitness[i];
        }

        for (int i=0;i<candidates.size();++i){
            wheel[i] = (i > 0 ? wheel[i-1] : 0) + fitness[i] / totalFitness;
        }

        for (int i=0;i<GlobalState.populationSize - 6;++i){
            double curr = GlobalState.randomGen.nextDouble() * wheel[wheel.length-1];
            int chosen = wheel.length-1;
            for (int j=wheel.length-2;j>=0;--j){
                if (wheel[j] < curr){
                    chosen = j;
                    break;
                }
            }
            newCandidates.add(new CandidateGA(candidates.get(chosen)));
        }

        for (int i=0;i<6;++i){
            double best = fitness[0];
            int chosen = 0;
            for (int j=1;j<fitness.length;++j){
                if (fitness[j] > best){
                    best = fitness[j];
                    chosen = j;
                }
            }
            fitness[chosen] = 0.0;
            newCandidates.add(new CandidateGA(candidates.get(chosen)));
        }

        candidates = newCandidates;
        return res;
    }

    double[] getDecimalRepresentationOfBestCandidate(){
        return decimalRepresentationOfBest;
    }
}
