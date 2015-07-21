import java.util.ArrayList;

public class DiscreteLogarithm 
{	
	private int generator;
	private int residue;
	private int prime;
	private int primePower;
	private int modulus;
	
	private int primeTotientOddComponent;
	private int primeTotientEvenComponent;
	private int twoPower;
	private int totient;
	
	private ArrayList<Integer> exponentModifiers;
	private int[] generatorTwoPowers;
	
	private int rootExponent;
	private int exponentAnswer;
	
	public DiscreteLogarithm(int generator, int residue, int prime, int primePower)
	{
		if ((generator > 1) && (residue >= 1) && (prime > 1) && (primePower >= 1))
		{
			System.out.println("Generator: " + generator);
			System.out.println("Residue: " + residue);
			System.out.println("Prime: " + prime);
			System.out.println("Prime Power: " + primePower);
			
			modulus = (int) Math.pow(prime, primePower);
			
			if (!(coprimeToModulus(generator) || coprimeToModulus(residue)))
			{
				System.out.println("Generator and residue must be coprime to the modulus");
				System.exit(0);
			}
			else
			{
				this.generator = makePositive(generator, modulus);
				this.residue = makePositive(residue, modulus);
				this.prime = prime;
				this.primePower = primePower;
				
				primeTotientOddComponent = prime - 1;
				primeTotientEvenComponent = 1;
				twoPower = 0;
				
				totient = (modulus / prime) * primeTotientOddComponent;
				
				while ((primeTotientOddComponent % 2) == 0)
				{
					primeTotientOddComponent /= 2;
					twoPower++;
					primeTotientEvenComponent *= 2;
				}
				
				System.out.println("\nModulus: " + modulus);
				System.out.println("Totient: " + totient);
				System.out.println("Prime Totient Odd Component: " + primeTotientOddComponent);
				System.out.println("Prime Totient Even Component: " + primeTotientEvenComponent);				
				
				findTwoPowerRootExponent();
				precalculateGeneratorTwoPowers();
				calculateExponentModifiers();
				extractExponent();
				
				System.out.println(generator + "^" + exponentAnswer + " = " +
								   residue + " (mod " + prime + "^" + primePower + ")");
				
			}
		}
		else
		{
			System.out.println("All values must be greater than (or equal to, in some cases, one");
			System.exit(0);
		}
	}			

	private void findTwoPowerRootExponent() 
	{
		int l = subtract(0, findInverse((totient / primeTotientEvenComponent), 
									    primeTotientEvenComponent), 
						 primeTotientEvenComponent);
		rootExponent = ((l * (totient / primeTotientEvenComponent)) + 1) / 
						primeTotientEvenComponent;
		
		System.out.println("Root Exponent: " + rootExponent);
		
	}

	private void calculateExponentModifiers() 
	{
		exponentModifiers = new ArrayList<Integer>();
		
		boolean done = false;
		int tempResidue = residue;
		int addModifier;
				
		while (!done)
		{
			System.out.println();
			
			addModifier = 0;
			
			if (!isTwoPowerResidue(tempResidue, twoPower))
			{
				int tempTwoPower = twoPower - 1;
				
				while (!isTwoPowerResidue(tempResidue, tempTwoPower) && (tempTwoPower >= 1))
				{
					tempTwoPower--;
				}
							
				tempResidue = multiply(tempResidue, generatorTwoPowers[tempTwoPower], modulus);
				addModifier += Math.pow(2, tempTwoPower);
				
				for (int i = tempTwoPower + 1; i < twoPower; i++)
				{
					if (!isTwoPowerResidue(tempResidue, i))
					{
						tempResidue = multiply(tempResidue, generatorTwoPowers[i], modulus);
						addModifier += Math.pow(2, i);
					}
				}
			}

			System.out.println(tempResidue + " is a 2^" + twoPower + 
							   "th residue modulo " + modulus);
			
			tempResidue = exponent(tempResidue, rootExponent, modulus);
			exponentModifiers.add(addModifier);
			
			System.out.println("2^" + twoPower + " residue root: " + tempResidue);
			System.out.println("2^" + twoPower + " residue generator adder: " + addModifier);
			
			if (tempResidue == 1)
			{
				exponentAnswer = 0;
				done = true;
			}
			else if (tempResidue == generator)
			{
				exponentAnswer = 1;
				done = true;
			}
		}
	}

	private void precalculateGeneratorTwoPowers() 
	{
		generatorTwoPowers = new int[twoPower + 1];
		
		generatorTwoPowers[0] = generator;
		
		for (int i = 1; i <= twoPower; i++)
		{
			generatorTwoPowers[i] = square(generatorTwoPowers[i - 1], modulus);
		}
	}

	private void extractExponent() 
	{
		for (int i = (exponentModifiers.size() - 1); i >= 0; i--)
		{
			exponentAnswer = subtract(multiply(primeTotientEvenComponent, exponentAnswer, totient),
							 		  exponentModifiers.get(i), totient);
		}
	}
	
	private boolean isTwoPowerResidue(int num, int power)
	{
		return (exponent(num, (totient / primeTotientEvenComponent), modulus) == 1);
	}
	
	private static int exponent(int num, int power, int someModulus) 
	{
		int answer = 1;
		
		while (power > 0)
		{
			if ((power & 1) == 1)
			{
				answer = multiply(answer, num, someModulus);
			}
			
			power = power >> 1;
			num = square(num, someModulus);
		}
		
		return answer;
	}

	private boolean coprimeToModulus(int num) 
	{
		return areCoprime(num, modulus);
	}
	
	private static int[] findBezoutPair(int numOne, int numTwo, int someModulus)
	{
		if (areCoprime(numOne, numTwo))
		{
			int prevX = 1;
			int prevY = 0;
			int prevRemainder = numOne;
			
			int curX = 0;
			int curY = 1;
			int curRemainder = numTwo;
			
			int quotient = 0; 
			
			while (curRemainder > 0)
			{
				int tempX = curX;
				int tempY = curY;
				int tempRemainder = curRemainder;
				
				quotient = prevRemainder / curRemainder;
				curRemainder = prevRemainder - (quotient * curRemainder);
				
				curX = prevX - (quotient * curX);
				curY = prevY - (quotient * curY);
				
				prevX = tempX;
				prevY = tempY;
				prevRemainder = tempRemainder;
			}
			
			return (new int[]{makePositive(prevX, someModulus), 
							  makePositive(prevY, someModulus)});
		}
		else
		{
			System.out.println("Cannot find Bezout pair. " + numOne + " and " +
							   numTwo + " are not coprime");
			
			return null;
		}
	}
	
	private static int findInverse(int num, int someModulus)
	{
		if (areCoprime(num, someModulus))
		{
			return findBezoutPair(num, someModulus, someModulus)[0];
		}
		else
		{
			System.out.println("Cannot invert " + num + " (mod " + someModulus +  ")");
			return 0;
		}
	}
	
	private static boolean areCoprime(int numOne, int numTwo) 
	{
		int remainderOne = Math.min(numOne, numTwo);
		int remainderTwo = Math.max(numOne, numTwo);
		
		while (remainderOne != 0)
		{
			int temp = remainderOne;
			
			remainderOne = remainderTwo % remainderOne;
			remainderTwo = temp;
		}
		
		return (remainderTwo == 1);
	}

	private static int makePositive(int num, int someModulus)
	{
		return (((num % someModulus) + someModulus) % someModulus);
	}
	
	private static int add(int numOne, int numTwo, int someModulus)
	{
		return makePositive((numOne + numTwo), someModulus);
	}
	
	private static int subtract(int numOne, int numTwo, int someModulus)
	{
		return makePositive((numOne - numTwo), someModulus);
	}
	
	private static int multiply(int numOne, int numTwo, int someModulus)
	{
		return makePositive((numOne * numTwo), someModulus);
	}
	
	private static int divide(int numOne, int numTwo, int someModulus)
	{
		return multiply(numOne, findInverse(makePositive(numTwo, someModulus), someModulus), someModulus);
	}
	
	private static int square(int num, int someModulus)
	{
		return multiply(num, num, someModulus);
	}
	
	public static void main(String[] args)
	{
		DiscreteLogarithm example = new DiscreteLogarithm(6, 198, 229, 1);
	}
}
