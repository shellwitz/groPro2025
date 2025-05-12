package packagy;

import java.io.IOException;

import static packagy.TextFileReader.*;

public class Main {
  public static void main(String[] args) throws IOException {

    System.out.println("Hello World");

    City city = readFromFile("C:\\Users\\debel\\Workspace\\groPro2025\\groPro2025\\testy.txt");
    city.simulate ();
    System.out.println("City created");
  }

}
