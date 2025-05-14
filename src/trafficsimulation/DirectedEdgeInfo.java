package trafficsimulation;

public class DirectedEdgeInfo {
  private int totalCount;
  private int currentNum;
  private int maxNum;

  public DirectedEdgeInfo() {
    this.totalCount = 0;
    this.currentNum = 0;
    this.maxNum = 0;
  }

  public void increment() {
    totalCount++;
    currentNum++;
  }

  public void decrement() {
    currentNum--;
  }

  public void updateMaxNum() {

    assert currentNum >= 0 : "Current number of vehicles cannot be negative";
    assert totalCount >= 0 : "Total count of vehicles cannot be negative";
    assert maxNum >= 0 : "Maximum number of vehicles cannot be negative";

    if (currentNum > maxNum) {
      maxNum = currentNum;
    }
  }

  public int getTotalCount() {
    return totalCount;
  }

  public int getCurrentNum() {
    return currentNum;
  }

  public int getMaxNum() {
    return maxNum;
  }

}
