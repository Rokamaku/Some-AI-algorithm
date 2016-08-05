// Program to print all the permutations of a string

public class Permutations
{
	public static void main(String[] args)
	{
		String s = args[0];
		char[] ss = s.toCharArray();
		printPermutations(ss,0);
	}

	public static void printArray(char[] s)
	{
		for (int i=0; i<s.length; i++)
		{
			System.out.print(s[i]);
		}
		System.out.println();
	}

	public static void printPermutations(
		char[] s, int index)
	{
		if (index >= s.length) 
		{
			printArray(s);
		}

		for (int i=index; i<s.length; i++)
		{
			// swap element index and element i
			char temp = s[index];
			s[index] = s[i];
			s[i] = temp;

			printPermutations(s, index+1);

			// swap back
			temp = s[index];
			s[index] = s[i];
			s[i] = temp;
		}
	}
}