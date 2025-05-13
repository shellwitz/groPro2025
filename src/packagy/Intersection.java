package packagy;

import java.util.Random;

public class Intersection {
  public Coord coord;

      NamesAndProbabilities namesAndProbabilities;


      public NamesAndProbabilities calcProbArr(String nameToIgnore){

            double[] newProbArr = new double[namesAndProbabilities.names.length - 1];
            String[] newNamesArr = new String[namesAndProbabilities.names.length - 1];
            double total = 0;
            int j = 0;


            for (int i = 0; i < namesAndProbabilities.names.length; i++){
                  if (!namesAndProbabilities.names[i].equals(nameToIgnore)){
                        total += namesAndProbabilities.probabilities[i];
                        newNamesArr[j] = namesAndProbabilities.names[i];
                        j++;
                  }

            }

            j = 0;

            for (int i = 0; i < namesAndProbabilities.names.length; i++){
                  if (!namesAndProbabilities.names[i].equals(nameToIgnore)){
                        newProbArr[j] = namesAndProbabilities.probabilities[i] / total;
                        j++;
                  }
            }

            return new NamesAndProbabilities(newNamesArr, newProbArr);

      }

      public String getNewDestinationByProbability(String nameToIgnore, Random random){

            NamesAndProbabilities newNamesAndProbabilities = calcProbArr(nameToIgnore);

            double randomValue = random.nextDouble(); // Random value between 0 and 1

            double cumulativeProbability = 0.0;
            for (int i = 0; i < newNamesAndProbabilities.probabilities.length; i++) {
                  cumulativeProbability += newNamesAndProbabilities.probabilities[i];
                  if (randomValue <= cumulativeProbability) {
                        return newNamesAndProbabilities.names[i];
                  }
            }
            return null; // Return null if no index is found (shouldn't happen if probabilities sum to 1)
      }




}
