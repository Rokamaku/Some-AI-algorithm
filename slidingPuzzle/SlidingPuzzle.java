// Sliding Puzzle program

import java.io.*;
import java.util.*;

public class SlidingPuzzle
{
	public static int count = 0;
	public static char[][] board;
	public static char[][] goal;
	public static int[][] offsets = 
		{ {0,1}, {0,-1}, {1,0}, {-1,0} };

	public static void readInputFile(String fileName)
	{
		try 
		{
			Scanner scan = new Scanner(new File(fileName));
			board = new char[4][4];
			goal = new char[4][4];

			for (int r=0; r<4; r++)
			{
				String line = scan.nextLine();
				for (int c=0; c<4; c++)
				{
					board[r][c] = line.charAt(c);
				}
			}
			scan.nextLine();

			for (int r=0; r<4; r++)
			{
				String line = scan.nextLine();
				for (int c=0; c<4; c++)
				{
					goal[r][c] = line.charAt(c);
				}
			}
		}
		catch (IOException ex)
		{
			System.out.println("Could not load file");
			System.exit(0);
		}
	}

	public static void printBoard(char[][] b, String spaces)
	{
		System.out.println();
		for (int r=0; r<b.length; r++)
		{
			System.out.print(spaces);
			for (int c=0; c<b[r].length; c++)
			{
				System.out.print(b[r][c]);
			}
			System.out.println();
		}
	}

	public static boolean boardsEqual(char[][] b, char[][] g)
	{
		for (int r=0; r<b.length; r++)
		{
			for (int c=0; c<b[r].length; c++)
			{
				if (b[r][c] != g[r][c]) return false;
			}
		}
		return true;
	}

	public static int squaresOutOfPlace(char[][] b, char[][] g)
	{
		int cnt=0;
		for (int r=0; r<b.length; r++)
		{
			for (int c=0; c<b[r].length; c++)
			{
				if (b[r][c] != g[r][c] && b[r][c] != '.') cnt++;
			}
		}
		return cnt;
	}

	public static int manhattanDistances(char[][] b, char[][] g)
	{
		int cnt = 0;
		for (int r=0; r<b.length; r++)
		{
			for (int c=0; c<b[r].length; c++)
			{
				char a = b[r][c];
				if (a == '.') continue;

				int index = a-'A';
				//System.out.print(a + " ");
				int x = index%4;
				int y = index/4;
				cnt += Math.abs(x-c) + Math.abs(y-r);
			}
		}
		//System.out.println(cnt);
		return cnt;
	}

	public static boolean depthLimitedSearch(
		char[][] b, char[][] g, int depth, 
		String spaces, int xlast, int ylast)
	{
		count++;
		//printBoard(b, spaces);

		if (boardsEqual(b,g)) 
		{
			System.out.println("found result!");
			return true;
		}
		if (depth == 0) return false;

		// if it's not possible to find the solution in the
		// number of moves available, return false
		//int tilesOutOfPlace = squaresOutOfPlace(b,g);
		int tilesOutOfPlace = manhattanDistances(b,g);
		if (tilesOutOfPlace > depth) return false;

		// find the blank spot
		int x=0;
		int y=0;
		for (int r=0; r<b.length; r++)
		{
			for (int c=0; c<b[r].length; c++)
			{
				if (b[r][c] == '.') {
					x = r;
					y = c;
				}
			}
		}

		for (int i=0; i<offsets.length; i++)
		{
			// find neighbor location
			int xnew = x+offsets[i][0];
			int ynew = y+offsets[i][1];

			// check whether neighbor is in bounds
			if (xnew < 0 || xnew >= b[0].length ||
				ynew < 0 || ynew >= b.length) continue;

			// don't reverse previous move
			if (xnew == xlast && ynew == ylast) continue;

			// make move
			b[x][y] = b[xnew][ynew];
			b[xnew][ynew] = '.';

			boolean res = depthLimitedSearch(b, g, depth-1,
				spaces + "  ", x,y);

			// undo move
			b[xnew][ynew] = b[x][y];
			b[x][y] = '.';

			if (res == true) 
			{
				System.out.println(b[xnew][ynew]);
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("usage: inputFile");
			System.exit(0);
		}

		readInputFile(args[0]);
		printBoard(board, "");
		printBoard(goal, "");

		for (int d=0; d<1000; d++)
		{
			System.out.println("Depth = " + d + 
				", count = " + count);
			boolean res = depthLimitedSearch(board, goal, d, "", -1, -1);
			if (res == true) break;
		}
	}
}