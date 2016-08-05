// Genetic Algorithm to produce a desired string

import java.util.*;

public class GeneticAlgorithm
{
	public static final String letters = 
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ ";
	public static final int PSIZE = 20;
	public static Random random = new Random();
	public static ArrayList< char[] > population;
	public static char[] goal;

	public static void main(String[] args)
	{
		Scanner scan = new Scanner(System.in);
		goal = args[0].toCharArray();
		population = new ArrayList< char[] >();

		// Create new population
		for (int i=0; i<PSIZE; i++)
		{
			char[] individual = makeIndividual();
			population.add(individual);
		}
		printPopulation();

		while (true)
		{
			// Do crossover events
			for (int i=0; i<PSIZE; i++)
			{
				char[] p1 = chooseRandomIndividual(PSIZE);
				char[] p2 = chooseRandomIndividual(PSIZE);
				char[] child = crossover(p1,p2);
				population.add(child);
			}
	
			// Resample population
			ArrayList< char[] > newPop = new ArrayList< char[] >();
			for (int i=0; i<PSIZE; i++)
			{
				newPop.add(resampleIndividual(population.size()));
			}
			population = newPop;
			printPopulation();

			scan.nextLine();
		}
	}

	// Print the population
	public static void printPopulation()
	{
		System.out.println();
		for (int i=0; i<population.size(); i++)
		{
			printIndividual(population.get(i));
		}
	}

	// Print an individual
	public static void printIndividual(char[] s)
	{
		for (int i=0; i<s.length; i++)
		{
			System.out.print(s[i]);
		}
		int f = fitness(s);
		System.out.println("   " + f);
	}

	// Make individual
	public static char[] makeIndividual()
	{
		char[] individual = new char[goal.length];
		for (int i=0; i<individual.length; i++) 
		{
			individual[i] = 
				letters.charAt(random.nextInt(letters.length()));
		}
		return individual;
	}

	public static int fitness(char[] s)
	{
		int f = 0;
		for (int i=0; i<s.length; i++) {
			if (s[i] == goal[i]) f++;
		}
		return f;
	}

	public static char[] crossover(char[] s, char[] t)
	{
		char[] child = new char[s.length];
		for (int i=0; i<s.length; i++)
		{
			if (random.nextDouble() < 0.5) child[i] = s[i];
			else child[i] = t[i];
		}

		int index = random.nextInt(s.length);
		int letterIndex = random.nextInt(letters.length());
		child[index] = letters.charAt(letterIndex);

		return child;
	}

	public static char[] chooseRandomIndividual(int n)
	{
		int index = random.nextInt(n);
		return population.get(index);
	}

	public static char[] resampleIndividual(int n)
	{
		int index = 0;
		int fitness = -1;
		for (int i=0; i<4; i++)
		{
			int idx = random.nextInt(n);
			int f = fitness(population.get(idx));
			if (f > fitness)
			{
				index = idx;
				fitness = f;
			}
		}
		return population.get(index);
	}
}