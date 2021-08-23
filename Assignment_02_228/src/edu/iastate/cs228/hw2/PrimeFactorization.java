package edu.iastate.cs228.hw2;


/**
 *  
 * @author Donald Calhoun
 *
 */

import java.util.ListIterator;

public class PrimeFactorization implements Iterable<PrimeFactor>
{
	private static final long OVERFLOW = -1;
	private long value; 	// the factored integer 
							// it is set to OVERFLOW when the number is greater than 2^63-1, the
						    // largest number representable by the type long. 
	
	/**
	 * Reference to dummy node at the head.
	 */
	private Node head;
	  
	/**
	 * Reference to dummy node at the tail.
	 */
	private Node tail;
	
	private int size;     	// number of distinct prime factors


	// ------------
	// Constructors 
	// ------------
	
    /**
	 *  Default constructor constructs an empty list to represent the number 1.
	 *  
	 *  Combined with the add() method, it can be used to create a prime factorization.  
	 */
	public PrimeFactorization() 
	{	 
		head = new Node();
		tail = new Node();
		
		head.next = tail;
		tail.previous = head;
		size = 0;
		
		updateValue();
	}

	
	/** 
	 * Obtains the prime factorization of n and creates a doubly linked list to store the result.   
	 * Follows the direct search factorization algorithm in Section 1.2 of the project description. 
	 * 
	 * @param n
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization(long n) throws IllegalArgumentException 
	{
		this();
		if (n < 1) {
			throw new IllegalArgumentException();
		}
		
		int m;
		int d = 2;
		int x = 0;
		boolean divisible = false;
		
		while ((d*d) <= n) {
			divisible = false;
			m = 1;
			
			while (n % ((int) Math.pow(d, m)) == 0) {
				divisible = true;
				m++;
			}
			
			if (divisible) {
				x = (int) Math.pow(d, m-1);
				n = n/x;
				add(d, m-1);
			}
			
			d++;
		}
		
		if ((d*d) >= n && n != 1) {
			add((int) n, 1);
		}
		
		updateValue();
	}
	
	
	/**
	 * Copy constructor. It is unnecessary to verify the primality of the numbers in the list.
	 * 
	 * @param pf
	 */
	public PrimeFactorization(PrimeFactorization pf)
	{
		this();
		PrimeFactorizationIterator iterate = pf.iterator();
		
		while (iterate.hasNext()) {
			PrimeFactor adding = iterate.next();
			this.add(adding.prime, adding.multiplicity);
		}
		
		this.value = pf.value();
	}
	
	/**
	 * Constructs a factorization from an array of prime factors.  Useful when the number is 
	 * too large to be represented even as a long integer. 
	 * 
	 * @param pflist
	 */
	public PrimeFactorization (PrimeFactor[] pfList)
	{
		this();
		for(PrimeFactor pf : pfList) {
			this.add(pf.prime, pf.multiplicity);
		}
		
		updateValue();
	}
	
	

	// --------------
	// Primality Test
	// --------------
	
    /**
	 * Test if a number is a prime or not.  Check iteratively from 2 to the largest 
	 * integer not exceeding the square root of n to see if it divides n. 
	 * 
	 *@param n
	 *@return true if n is a prime 
	 * 		  false otherwise 
	 */
    public static boolean isPrime(long n) 
	{
	    int i = 2;
	    
	    while (i <= ((int) Math.sqrt(n))) {
	    	if (n % i == 0) {
	    		return false;
	    	}
	    	i++;
	    }
	    
		return true; 
	}   

   
	// ---------------------------
	// Multiplication and Division 
	// ---------------------------
	
	/**
	 * Multiplies the integer v represented by this object with another number n.  Note that v may 
	 * be too large (in which case this.value == OVERFLOW). You can do this in one loop: Factor n and 
	 * traverse the doubly linked list simultaneously. For details refer to Section 3.1 in the 
	 * project description. Store the prime factorization of the product. Update value and size. 
	 * 
	 * @param n
	 * @throws IllegalArgumentException if n < 1
	 */
	public void multiply(long n) throws IllegalArgumentException 
	{
		if (n < 1) {
			throw new IllegalArgumentException();
		}
		
		PrimeFactorization factorN = new PrimeFactorization(n);
		this.multiply(factorN);
	}
	
	/**
	 * Multiplies the represented integer v with another number in the factorization form.  Traverse both 
	 * linked lists and store the result in this list object.  See Section 3.1 in the project description 
	 * for details of algorithm. 
	 * 
	 * @param pf 
	 */
	public void multiply(PrimeFactorization pf)
	{
		PrimeFactorizationIterator iterate = pf.iterator();
		
		while (iterate.hasNext()) {
			PrimeFactor adding = iterate.next();
			this.add(adding.prime, adding.multiplicity);
		}
		
		if (!valueOverflow()) {
			updateValue();
		}
	}
	
	
	/**
	 * Multiplies the integers represented by two PrimeFactorization objects.  
	 * 
	 * @param pf1
	 * @param pf2
	 * @return object of PrimeFactorization to represent the product 
	 */
	public static PrimeFactorization multiply(PrimeFactorization pf1, PrimeFactorization pf2)
	{
		PrimeFactorization copyPf1 = pf1;
		copyPf1.multiply(pf2);
		
		return copyPf1; 
	}

	
	/**
	 * Divides the represented integer v by n.  Make updates to the list, value, size if divisible.  
	 * No update otherwise. Refer to Section 3.2 in the project description for details. 
	 *  
	 * @param n
	 * @return  true if divisible 
	 *          false if not divisible 
	 * @throws IllegalArgumentException if n <= 0
	 */
	public boolean dividedBy(long n) throws IllegalArgumentException
	{
		if (n <= 0) {
			throw new IllegalArgumentException();
		}
		
		PrimeFactorization factorN = new PrimeFactorization(n);
		return this.dividedBy(factorN);
	}

	
	/**
	 * Division where the divisor is represented in the factorization form.  Update the linked 
	 * list of this object accordingly by removing those nodes housing prime factors that disappear  
	 * after the division.  No update if this number is not divisible by pf. Algorithm details are 
	 * given in Section 3.2. 
	 * 
	 * @param pf
	 * @return	true if divisible by pf
	 * 			false otherwise
	 */
	public boolean dividedBy(PrimeFactorization pf)
	{
		boolean isDivisible = true;
		PrimeFactorizationIterator iterate = pf.iterator();
		
		while (iterate.hasNext()) {
			PrimeFactor removing = iterate.next();
			isDivisible = this.remove(removing.prime, removing.multiplicity);
			if (!isDivisible) {
				return false;
			}
		}
		
		if (!valueOverflow()) {
			updateValue();
		}
		
		return isDivisible; 
	}

	
	/**
	 * Divide the integer represented by the object pf1 by that represented by the object pf2. 
	 * Return a new object representing the quotient if divisible. Do not make changes to pf1 and 
	 * pf2. No update if the first number is not divisible by the second one. 
	 *  
	 * @param pf1
	 * @param pf2
	 * @return quotient as a new PrimeFactorization object if divisible
	 *         null otherwise 
	 */
	public static PrimeFactorization dividedBy(PrimeFactorization pf1, PrimeFactorization pf2)
	{
		PrimeFactorization copyPf1 = pf1;
		copyPf1.dividedBy(pf2);
		
		return copyPf1; 
	}

	
	// -------------------------------------------------
	// Greatest Common Divisor and Least Common Multiple 
	// -------------------------------------------------

	/**
	 * Computes the greatest common divisor (gcd) of the represented integer v and an input integer n.
	 * Returns the result as a PrimeFactor object.  Calls the method Euclidean() if 
	 * this.value != OVERFLOW.
	 *     
	 * It is more efficient to factorize the gcd than n, which can be much greater. 
	 *     
	 * @param n
	 * @return prime factorization of gcd
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization gcd(long n) throws IllegalArgumentException
	{
		if (n < 1) {
			throw new IllegalArgumentException();
		}
		
		PrimeFactorization factorN = null;
		
		if (!valueOverflow()) {
			factorN = new PrimeFactorization(Euclidean(this.value, n));
		} else {
			factorN = this.gcd(new PrimeFactorization(n));
		}
		
		return factorN;
	}
	

	/**
	  * Implements the Euclidean algorithm to compute the gcd of two natural numbers m and n. 
	  * The algorithm is described in Section 4.1 of the project description. 
	  * 
	  * @param m
	  * @param n
	  * @return gcd of m and n. 
	  * @throws IllegalArgumentException if m < 1 or n < 1
	  */
 	public static long Euclidean(long m, long n) throws IllegalArgumentException
	{
		if (n < 1 || m < 1) {
			throw new IllegalArgumentException();
		}
		
		long r0;
		long r1 = 0;;
		long r2 = 1;
		int q;
		
		while (r2 != 0) {
			if (m > n) {
				r0 = m;
				r1 = n;
			} else {
				r0 = n;
				r1 = m;
			}
			
			q = (int) (r0/r1);
			r2 = r0 - (q*r1);
			
			if (r2 != 0) {
				m = r1;
				n = r2;
			}
		}
		
 		return r1; 
	}

 	
	/**
	 * Computes the gcd of the values represented by this object and pf by traversing the two lists.  No 
	 * direct computation involving value and pf.value. Refer to Section 4.2 in the project description 
	 * on how to proceed.  
	 * 
	 * @param  pf
	 * @return prime factorization of the gcd
	 */
	public PrimeFactorization gcd(PrimeFactorization pf)
	{
		PrimeFactorization output = new PrimeFactorization();
		PrimeFactorizationIterator iterate = iterator();
		int p = 1;
		int m = 1;

		while (iterate.hasNext()) {
			PrimeFactor current = iterate.next();
			PrimeFactorizationIterator pfIterate = pf.iterator();
			while (pfIterate.hasNext()) {
				PrimeFactor pfCurrent = pfIterate.next();
				if (current.prime == pfCurrent.prime) {
					p = current.prime;
					if (current.multiplicity > pfCurrent.multiplicity) {
						m = pfCurrent.multiplicity;
					}
					else {
						m = current.multiplicity;
					}
					output.add(p, m);
				}
			}
		}
		
		return output;
	}
	
	
	/**
	 * 
	 * @param pf1
	 * @param pf2
	 * @return prime factorization of the gcd of two numbers represented by pf1 and pf2
	 */
	public static PrimeFactorization gcd(PrimeFactorization pf1, PrimeFactorization pf2)
	{
		PrimeFactorization copyPf1 = pf1;
		copyPf1.gcd(pf2);
		
		return copyPf1; 
	}

	
	/**
	 * Computes the least common multiple (lcm) of the two integers represented by this object 
	 * and pf.  The list-based algorithm is given in Section 4.3 in the project description. 
	 * 
	 * @param pf  
	 * @return factored least common multiple  
	 */
	public PrimeFactorization lcm(PrimeFactorization pf)
	{
		PrimeFactorization output = new PrimeFactorization();
		PrimeFactorizationIterator iterate = iterator();
		int p = 1;
		int m = 1;

		while (iterate.hasNext()) {
			PrimeFactor current = iterate.next();
			PrimeFactorizationIterator pfIterate = pf.iterator();
			while (pfIterate.hasNext()) {
				PrimeFactor pfCurrent = pfIterate.next();
				if (current.prime == pfCurrent.prime) {
					p = current.prime;
					if (current.multiplicity < pfCurrent.multiplicity) {
						m = pfCurrent.multiplicity;
					}
					else {
						m = current.multiplicity;
					}
					output.add(p, m);
				} else {
					if (current.prime < pfCurrent.prime) {
						p = current.prime;
						m = current.multiplicity;
					} else {
						p = pfCurrent.prime;
						m = pfCurrent.multiplicity;
					}
					output.add(p,  m);
				}
			}
			output.add(current.prime, current.multiplicity);
		}
		
		return output;
	}

	
	/**
	 * Computes the least common multiple of the represented integer v and an integer n. Construct a 
	 * PrimeFactors object using n and then call the lcm() method above.  Calls the first lcm() method. 
	 * 
	 * @param n
	 * @return factored least common multiple 
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization lcm(long n) throws IllegalArgumentException 
	{
		if (n < 1) {
			throw new IllegalArgumentException();
		}
		
		return this.lcm(new PrimeFactorization(n)); 
	}

	/**
	 * Computes the least common multiple of the integers represented by pf1 and pf2. 
	 * 
	 * @param pf1
	 * @param pf2
	 * @return prime factorization of the lcm of two numbers represented by pf1 and pf2
	 */
	public static PrimeFactorization lcm(PrimeFactorization pf1, PrimeFactorization pf2)
	{
		PrimeFactorization copyPf1 = pf1;
		copyPf1.lcm(pf2);
		
		return copyPf1; 
	}

	
	// ------------
	// List Methods
	// ------------
	
	/**
	 * Traverses the list to determine if p is a prime factor. 
	 * 
	 * Precondition: p is a prime. 
	 * 
	 * @param p  
	 * @return true  if p is a prime factor of the number v represented by this linked list
	 *         false otherwise 
	 * @throws IllegalArgumentException if p is not a prime
	 */
	public boolean containsPrimeFactor(int p) throws IllegalArgumentException
	{
		if (!isPrime(p)) {
			throw new IllegalArgumentException();
		}
		
		PrimeFactorizationIterator iterate = iterator();
		
		while (iterate.hasNext()) {
			PrimeFactor current = iterate.next();
			if (p == current.prime) {
				return true;
			}
		}
		
		return false; 
	}
	
	// The next two methods ought to be private but are made public for testing purpose. Keep
	// them public 
	
	/**
	 * Adds a prime factor p of multiplicity m.  Search for p in the linked list.  If p is found at 
	 * a node N, add m to N.multiplicity.  Otherwise, create a new node to store p and m. 
	 *  
	 * Precondition: p is a prime. 
	 * 
	 * @param p  prime 
	 * @param m  multiplicity
	 * @return   true  if m >= 1
	 *           false if m < 1   
	 */
    public boolean add(int p, int m) 
    {
    	if (m < 1) {
    		updateValue();
    		return false;
    	}

		PrimeFactorizationIterator iterate = iterator();
		
		if (iterate.hasNext()) {
			if (iterate.next().prime > p) {
				iterate.previous();
				iterate.add(new PrimeFactor(p, m));
			} else {
				iterate.previous();
			}
		} else {
			iterate.add(new PrimeFactor(p, m));
		}
		
		while (iterate.hasNext()) {
			PrimeFactor n = iterate.next();
			if (n.prime > p) {
				break;
			} else if (n.prime == p) {
				iterate.previous();
				iterate.add(new PrimeFactor(p, m));
				iterate.next();
			} else if (iterate.hasNext()){
				PrimeFactor nm = iterate.next();
				iterate.previous();
				
				if (p <= nm.prime) {
					iterate.add(new PrimeFactor(p, m));
					break;
				}
			} else {
				iterate.add(new PrimeFactor(p, m));
			}
		}
		
		updateValue();
		return true;
    }

	    
    /**
     * Removes m from the multiplicity of a prime p on the linked list.  It starts by searching 
     * for p.  Returns false if p is not found, and true if p is found. In the latter case, let 
     * N be the node that stores p. If N.multiplicity > m, subtracts m from N.multiplicity.  
     * If N.multiplicity <= m, removes the node N.  
     * 
     * Precondition: p is a prime. 
     * 
     * @param p
     * @param m
     * @return true  when p is found. 
     *         false when p is not found. 
     * @throws IllegalArgumentException if m < 1
     */
    public boolean remove(int p, int m) throws IllegalArgumentException
    {
    	if (m < 1) {
    		throw new IllegalArgumentException();
    	}
    	if (containsPrimeFactor(p)) {
    		PrimeFactorizationIterator iterate = iterator();
    		
    		while (iterate.hasNext()) {
    			PrimeFactor n = iterate.next();
    			if (n.prime == p) {
					if (n.multiplicity > m) {
						n.multiplicity -= m;
					}
					else iterate.remove();
					break;
    			}
    		}
    		updateValue();
    		return true;
    	}
    	
    	return false;
    }


    /**
     * 
     * @return size of the list
     */
	public int size() 
	{
		return size; 
	}

	
	/**
	 * Writes out the list as a factorization in the form of a product. Represents exponentiation 
	 * by a caret.  For example, if the number is 5814, the returned string would be printed out 
	 * as "2 * 3^2 * 17 * 19". 
	 */
	@Override 
	public String toString()
	{
		String output = "";
		PrimeFactorizationIterator iterate = iterator();
		
		if (iterate.hasNext()) {
			output = iterate.next().toString();
		}
		
		while (iterate.hasNext()) {
			output = output + " * " + iterate.next().toString();
		}
		
		if (value == 1) {
			return "1";
		}
		return output;
	}

	
	// The next three methods are for testing, but you may use them as you like.  

	/**
	 * @return true if this PrimeFactorization is representing a value that is too large to be within 
	 *              long's range. e.g. 999^999. false otherwise.
	 */
	public boolean valueOverflow() {
		return value == OVERFLOW;
	}

	/**
	 * @return value represented by this PrimeFactorization, or -1 if valueOverflow()
	 */
	public long value() {
		return value;
	}

	
	public PrimeFactor[] toArray() {
		PrimeFactor[] arr = new PrimeFactor[size];
		int i = 0;
		for (PrimeFactor pf : this)
			arr[i++] = pf;
		return arr;
	}


	
	@Override
	public PrimeFactorizationIterator iterator()
	{
	    return new PrimeFactorizationIterator();
	}
	
	/**
	 * Doubly-linked node type for this class.
	 */
    private class Node 
    {
		public PrimeFactor pFactor;			// prime factor 
		public Node next;
		public Node previous;
		
		/**
		 * Default constructor for creating a dummy node.
		 */
		public Node()
		{
			pFactor = null;
		}
	    
		/**
		 * Precondition: p is a prime
		 * 
		 * @param p	 prime number 
		 * @param m  multiplicity 
		 * @throws IllegalArgumentException if m < 1 
		 */
		public Node(int p, int m) throws IllegalArgumentException 
		{	
			if (m < 1) {
				throw new IllegalArgumentException();
			}
			
			pFactor = new PrimeFactor(p, m);
		}   

		
		/**
		 * Constructs a node over a provided PrimeFactor object. 
		 * 
		 * @param pf
		 * @throws IllegalArgumentException
		 */
		public Node(PrimeFactor pf)  
		{
			if (pf.multiplicity < 1) {
				throw new IllegalArgumentException();
			}
			
			pFactor = pf.clone();
		}


		/**
		 * Printed out in the form: prime + "^" + multiplicity.  For instance "2^3". 
		 * Also, deal with the case pFactor == null in which a string "dummy" is 
		 * returned instead.  
		 */
		@Override
		public String toString() 
		{
			if (pFactor == null) {
				return "dummy";
			}
			
			return pFactor.toString(); 
		}
    }

    
    private class PrimeFactorizationIterator implements ListIterator<PrimeFactor>
    {  	
        // Class invariants: 
        // 1) logical cursor position is always between cursor.previous and cursor
        // 2) after a call to next(), cursor.previous refers to the node just returned 
        // 3) after a call to previous() cursor refers to the node just returned 
        // 4) index is always the logical index of node pointed to by cursor

        private Node cursor = head.next;
        private Node pending = null;    // node pending for removal
        private int index = 0;      
  	  
    	// other instance variables ... 
    	  
      
        /**
    	 * Default constructor positions the cursor before the smallest prime factor.
    	 */
    	public PrimeFactorizationIterator()
    	{
    		cursor = head.next;
    	}

    	@Override
    	public boolean hasNext()
    	{
    		if (nextIndex() < size) {
    			return true;
    		}
    		
    		return false; 
    	}

    	
    	@Override
    	public boolean hasPrevious()
    	{
    		if (previousIndex() >= 0) {
    			return true;
    		}
    		
    		return false; 
    	}

 
    	@Override 
    	public PrimeFactor next() 
    	{
    		index++;
    		pending = cursor;
    		cursor = cursor.next;
    		
    		return pending.pFactor; 
    	}

 
    	@Override 
    	public PrimeFactor previous() 
    	{
    		index--;
    		pending = cursor;
    		cursor = cursor.previous;
    		
    		return pending.pFactor; 
    	}

   
    	/**
    	 *  Removes the prime factor returned by next() or previous()
    	 *  
    	 *  @throws IllegalStateException if pending == null 
    	 */
    	@Override
    	public void remove() throws IllegalStateException
    	{
    		if (pending == null) {
    			throw new IllegalStateException();
    		}
    		
    		unlink(pending);
    		size--;
    		pending = null;
    	}
 
 
    	/**
    	 * Adds a prime factor at the cursor position.  The cursor is at a wrong position 
    	 * in either of the two situations below: 
    	 * 
    	 *    a) pf.prime < cursor.previous.pFactor.prime if cursor.previous != head. 
    	 *    b) pf.prime > cursor.pFactor.prime if cursor != tail. 
    	 * 
    	 * Take into account the possibility that pf.prime == cursor.pFactor.prime. 
    	 * 
    	 * Precondition: pf.prime is a prime. 
    	 * 
    	 * @param pf  
    	 * @throws IllegalArgumentException if the cursor is at a wrong position. 
    	 */
    	@Override
        public void add(PrimeFactor pf) throws IllegalArgumentException 
        {
        	if ((cursor.previous != head && pf.prime < cursor.previous.pFactor.prime) || (cursor != tail && pf.prime > cursor.pFactor.prime)) {
        		throw new IllegalArgumentException();
        	}
        	
        	if (cursor.pFactor != null && pf.prime == cursor.pFactor.prime) {
        		cursor.pFactor.multiplicity += pf.multiplicity;
        	} else {
				link(cursor.previous, new Node(pf));
				index++;
				size++;
        	}
        }


    	@Override
		public int nextIndex() 
		{
			return index;
		}


    	@Override
		public int previousIndex() 
		{
			return index - 1;
		}

		@Deprecated
		@Override
		public void set(PrimeFactor pf) 
		{
			throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support set method");
		}
        
    	// Other methods you may want to add or override that could possibly facilitate 
    	// other operations, for instance, addition, access to the previous element, etc.
    	// 
    	// ...
    	// 
    }

    
    // --------------
    // Helper methods 
    // -------------- 
    
    /**
     * Inserts toAdd into the list after current without updating size.
     * 
     * Precondition: current != null, toAdd != null
     */
    private void link(Node current, Node toAdd)
    {
    	current.next.previous = toAdd;
    	toAdd.next = current.next;
    	toAdd.previous = current;
    	current.next = toAdd;
    }

	 
    /**
     * Removes toRemove from the list without updating size.
     */
    private void unlink(Node toRemove)
    {
    	toRemove.previous.next = toRemove.next;
    	toRemove.next.previous = toRemove.previous;
    }


    /**
	  * Remove all the nodes in the linked list except the two dummy nodes. 
	  * 
	  * Made public for testing purpose.  Ought to be private otherwise. 
	  */
	public void clearList()
	{
		link(head, tail);
		size = 0; 
	}	
	
	/**
	 * Multiply the prime factors (with multiplicities) out to obtain the represented integer.  
	 * Use Math.multiply(). If an exception is throw, assign OVERFLOW to the instance variable value.  
	 * Otherwise, assign the multiplication result to the variable. 
	 * 
	 */
	private void updateValue()
	{
		try {	
			value = 1;
			long v;
			PrimeFactorizationIterator iterate = iterator();
			
			while (iterate.hasNext()) {
				PrimeFactor pf = iterate.next();
				v = (long) Math.pow(pf.prime,  pf.multiplicity);
				value = Math.multiplyExact(value, v);
			}
		} 
			
		catch (ArithmeticException e) 
		{
			value = OVERFLOW;
		}
		
	}
}
