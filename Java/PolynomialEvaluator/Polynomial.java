package PolynomialEvaluator;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class implements evaluate, add and multiply for polynomials.
 * 
 */
public class Polynomial {
	
	/**
	 * Reads a polynomial from an input stream (file or keyboard). The storage format
	 * of the polynomial is:
	 * <pre>
	 *     <coeff> <degree>
	 *     <coeff> <degree>
	 *     ...
	 *     <coeff> <degree>
	 * </pre>
	 * with the guarantee that degrees will be in descending order. For example:
	 * <pre>
	 *      4 5
	 *     -2 3
	 *      2 1
	 *      3 0
	 * </pre>
	 * which represents the polynomial:
	 * <pre>
	 *      4*x^5 - 2*x^3 + 2*x + 3 
	 * </pre>
	 * 
	 * @param sc Scanner from which a polynomial is to be read
	 * @throws IOException If there is any input error in reading the polynomial
	 * @return The polynomial linked list (front node) constructed from coefficients and
	 *         degrees read from scanner
	 */
	public static Node read(Scanner sc) 
	throws IOException {
		Node poly = null;
		while (sc.hasNextLine()) {
			Scanner scLine = new Scanner(sc.nextLine());
			poly = new Node(scLine.nextFloat(), scLine.nextInt(), poly);
			scLine.close();
		}
		return poly;
	}
	
	/**
	 * Returns the sum of two polynomials - DOES NOT change either of the input polynomials.
	 * The returned polynomial MUST have all new nodes. In other words, none of the nodes
	 * of the input polynomials can be in the result.
	 * 
	 * @param poly1 First input polynomial (front of polynomial linked list)
	 * @param poly2 Second input polynomial (front of polynomial linked list
	 * @return A new polynomial which is the sum of the input polynomials - the returned node
	 *         is the front of the result polynomial
	 */
	public static Node add(Node poly1, Node poly2) {
		
		if(poly1 == null) {
			return poly2;
		}else if(poly2 == null) {
			return poly1;
		}else if(poly1.term.degree < poly2.term.degree) {
			poly1.next = add(poly1.next,poly2);
			return poly1; 
		}else if(poly1.term.degree > poly2.term.degree) {
			poly2.next = add(poly1,poly2.next);
			return poly2; 
		}else {
			float sumCoeff = poly1.term.coeff + poly2.term.coeff;
			
			if(sumCoeff == 0) {
				return add(poly1.next,poly2.next);
			}
			
			return new Node(sumCoeff,poly1.term.degree,add(poly1.next,poly2.next));
		}
	}
	
	/**
	 * Returns the product of two polynomials - DOES NOT change either of the input polynomials.
	 * The returned polynomial MUST have all new nodes. In other words, none of the nodes
	 * of the input polynomials can be in the result.
	 * 
	 * @param poly1 First input polynomial (front of polynomial linked list)
	 * @param poly2 Second input polynomial (front of polynomial linked list)
	 * @return A new polynomial which is the product of the input polynomials - the returned node
	 *         is the front of the result polynomial
	 */
	public static Node multiply(Node poly1, Node poly2) {
		
		Node product = null, ptr = poly1;
		
		while(ptr != null) {
			Node copyOfPoly2 = copy(poly2);
			Node current = multiply(ptr.term,copyOfPoly2);
			product = add(current,product);
			
			ptr = ptr.next;
		}
		
		return product;
	}
	
	private static Node multiply(Term term, Node poly) {
		if(poly == null) {
			return null;
		}else {
			float productCoeff = term.coeff * poly.term.coeff;
			int productDegree = term.degree + poly.term.degree;
			poly.term = new Term(productCoeff, productDegree);
			poly.next = multiply(term, poly.next);
			return poly;
		}
	}
	
	private static Node copy(Node poly) {
		if(poly == null) {
			return null;
		}else {
			return new Node(poly.term.coeff, poly.term.degree, copy(poly.next));
		}
	}
		
	/**
	 * Evaluates a polynomial at a given value.
	 * 
	 * @param poly Polynomial (front of linked list) to be evaluated
	 * @param x Value at which evaluation is to be done
	 * @return Value of polynomial p at x
	 */
	public static float evaluate(Node poly, float x) {
		float total = 0;
		
		Node ptr = poly;
		
		while(ptr != null) {
			total += ptr.term.coeff * Math.pow((double)x, ptr.term.degree);
			ptr = ptr.next;
		}
		
		return total;
	}
	
	/**
	 * Returns string representation of a polynomial
	 * 
	 * @param poly Polynomial (front of linked list)
	 * @return String representation, in descending order of degrees
	 */
	public static String toString(Node poly) {
		if (poly == null) {
			return "0";
		} 
		
		String retval = poly.term.toString();
		for (Node current = poly.next ; current != null ;
		current = current.next) {
			retval = current.term.toString() + " + " + retval;
		}
		return retval;
	}	
}
