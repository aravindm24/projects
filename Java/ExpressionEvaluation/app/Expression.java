package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	
    	StringTokenizer s = new StringTokenizer(expr,delims);
    	
    	while(s.hasMoreTokens()) {
    		String current = s.nextToken();
    		
    		if(isNumeric(current)) {
    			continue;
    		}
    		int indexAfterToken = expr.indexOf(current) + current.length();
    		char c;
    		
    		if(indexAfterToken < expr.length()) {
    			c = expr.charAt(indexAfterToken);
    			
    			if(c == '[') {
    				addArray(current, arrays);
        		}else {
        			addVar(current,vars);
        		}
    		}else {
    			addVar(current,vars);
    		}
    	}
    }
    
    private static boolean isNumeric(String str) {
    	try {
    		Float.parseFloat(str);
    		return true;
    	}catch(NumberFormatException e) {
    		return false;
    	}
    }
    
    private static void addArray(String s, ArrayList<Array> arrays) {
    	int i;
		for(i = 0; i < arrays.size(); i++) {
			if(arrays.get(i).name.equals(s)) {
				break;
			}
		}
		if(i == arrays.size()) {
			arrays.add(new Array(s));
		}
	}
    
    private static void addVar(String s, ArrayList<Variable> vars) {
    	int i;
		for(i = 0; i < vars.size(); i++) {
			if(vars.get(i).name.equals(s)) {
				break;
			}
		}
		if(i == vars.size()) {
			vars.add(new Variable(s));
		}
	}
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	Stack<String> values = new Stack<String>();
    	Stack<Character> operations = new Stack<Character>();
    	
    	StringTokenizer str = new StringTokenizer(expr,delims);
    	String current = str.nextToken(); //starts with first token
    	
    	int i = 0;
    	
    	while(i < expr.length()) {
    		if(expr.substring(i,i+current.length()).equals(current)) {
    			//checks if the current index of the string we're at starts with a variable
    			values.push(current);
    			i += current.length();
    			//move index past variable
    			if(str.hasMoreElements()) {
    				current = str.nextToken();
    			}else {
    				current = " ";
    			}
    		}else {
    			char a = expr.charAt(i);
    			
    			if(isOperator(a)) {
    				//checks if char at current index is a +,-,*, or /
    				if(operations.isEmpty()) {
    					operations.push(a);
    				}else {
    					//if operations stack isn't empty, then we combine elements in the values stack if pushing the current operation means 
    					//that the operations stack will be unsorted by priority
    					while(!operations.isEmpty() && hasPriority(a,operations.peek())) {
    						String result = performOperation(values.pop(),values.pop(),operations.pop(),vars);
    						values.push(result);
    					}
    					//push current char after operations stack has had all of its elements removed/used or if stack will stay sorted by priority when pushed
    					operations.push(a);
    				}
    			}else if(a == '(' || a == '[') {
    				operations.push(a);
    			}else if(a == ')') {
    				//pop off operations from stack and perform operations until top element of stack is an open parentheses
    				while(operations.peek() != '(') {
    					String result = performOperation(values.pop(),values.pop(),operations.pop(),vars);
						values.push(result);
    				}
    				operations.pop();
    			}else if(a == ']') {
    				//pop off operations from stack and perform operations until top element of stack is an open bracket
    				while(operations.peek() != '[') {
    					String result = performOperation(values.pop(),values.pop(),operations.pop(),vars);
						values.push(result);
    				}
    				//top element of values stack should be the index we calculated
    				String temp = values.pop();
    				float index;
    				
    				if(isNumeric(temp)) {
    					index = Float.parseFloat(temp);
    				}else {
    					index = getVar(temp,vars).value;
    				}
    				
    				int[] arr = getArray(values.pop(),arrays).values;
    				
    				values.push(Integer.toString(arr[(int)index]));
    				
    				operations.pop();
    			}
    			
    			i++;
    		}
    	}
    	
    	//the while loop below performs any remaining operations that were not already taken care of in the above while loop
    	while(!operations.isEmpty()) {
    		String result = performOperation(values.pop(),values.pop(),operations.pop(),vars);
			values.push(result);
    	}
    	
    	String end = values.pop();
    	
    	if(isNumeric(end)) {
    		return Float.parseFloat(end);
    	}else {
    		return getVar(end,vars).value;
    	}
    }
    
    private static boolean isOperator(char c) {
    	String str = "/*-+";
    	
    	for(int i = 0; i < str.length(); i++) {
    		if(c == str.charAt(i)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private static boolean hasPriority(char a, char b) {
    	String str = "/*-+([";
    	
    	return str.indexOf(a) > str.indexOf(b);
    }
    
    private static String performOperation(String operand1, String operand2, char c, ArrayList<Variable> vars) {
    	float a,b,result;
    	
    	if(!isNumeric(operand1)) {
			a = getVar(operand1,vars).value;
		}else {
			a = Float.parseFloat(operand1);
		}
    	
    	if(!isNumeric(operand2)) {
			b = getVar(operand2,vars).value;
		}else {
			b = Float.parseFloat(operand2);
		}
    	
    	if(c == '/') {
    		result = b / a;
    	}else if(c == '*') {
    		result = a * b;
    	}else if(c == '-') {
    		result = b - a;
    	}else {
    		result = a + b;
    	}
    	
    	return Float.toString(result);
    			
    }
    
    private static Variable getVar(String var, ArrayList<Variable> vars) {
    	for(int i = 0; i < vars.size(); i++) {
    		if(vars.get(i).name.equals(var)) {
    			return vars.get(i);
    		}
    	}
    	return null;
    }
    
    private static Array getArray(String array, ArrayList<Array> arrays) {
    	for(int i = 0; i < arrays.size(); i++) {
    		if(arrays.get(i).name.equals(array)) {
    			return arrays.get(i);
    		}
    	}
    	return null;
    }
}
