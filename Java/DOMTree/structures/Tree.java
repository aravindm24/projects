package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	/**
	 * Builds the DOM tree from input HTML file, through scanner passed
	 * in to the constructor and stored in the sc field of this object. 
	 * 
	 * The root of the tree that is built is referenced by the root field of this object.
	 */
	public void build() {
		String s = sc.nextLine();
		root = new TagNode(s.substring(1,s.length()-1),null,null);
		build(sc.nextLine(),s,root);
	}
	
	private void build(String line, String prev, TagNode ptr) {
		if(line.length() > 1) {
			if(line.charAt(1) == '/') {
				return;
			}
		}
		
		//System.out.println(root);
		String s = extractString(line);
		
		if(prev.charAt(0) == '<') {
			if (ptr.firstChild == null) {
				ptr.firstChild = new TagNode(s, null, null);
				build(sc.nextLine(), line, ptr.firstChild);
			}else {
				while(ptr.sibling != null) {
					ptr = ptr.sibling;
				}
				
				ptr.sibling = new TagNode(s,null,null);
				build(sc.nextLine(),line,ptr.sibling);
				return;
			}
		}else {
			ptr.sibling = new TagNode(s,null,null);
			build(sc.nextLine(),line,ptr.sibling);
			return;
		}
		
		if (sc.hasNextLine()) {
			build(sc.nextLine(), line, ptr);
		}
	}
	
	private String extractString(String s) {
		if(s.charAt(0) == '<') {
			return s.substring(1,s.length() - 1);
		}else {
			return s;
		}
	}
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		replaceTag(oldTag,newTag,root);
	}
	
	private void replaceTag(String oldTag, String newTag, TagNode ptr) {
		if(ptr == null) {
			return;
		}else {
			if(ptr.tag.equals(oldTag)) {
				ptr.tag = newTag;
			}
			replaceTag(oldTag, newTag, ptr.sibling);
			replaceTag(oldTag, newTag, ptr.firstChild);
		}
	}
	
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		TagNode ptr = root;
		
		while(!ptr.tag.equals("body")) {
			ptr = ptr.firstChild;
		}
		
		ptr = ptr.firstChild;
		
		while(!ptr.tag.equals("table")) {
			ptr = ptr.sibling;
		}
		
		ptr = ptr.firstChild;
		
		int count = 1;
		
		while(count != row) {
			ptr = ptr.sibling;
			count++;
		}
		
		ptr = ptr.firstChild;
		
		while(ptr != null) {
			ptr.firstChild = new TagNode("b",ptr.firstChild,null);
			ptr = ptr.sibling;
		}
		return;
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		if(tag.equals("ol") || tag.equals("ul")) {
			removeTag2(tag,root,null);
		}else {
			removeTag1(tag,root,null);
		}
	}
	
	private void removeTag1(String tag, TagNode ptr, TagNode prev) {
		if(ptr == null) {
			return;
		}else {
			if(ptr.tag.equals(tag)) {
				if(prev.firstChild != null && prev.firstChild.tag.equals(ptr.tag)) {
					TagNode ptr2 = ptr.firstChild;
					
					while(ptr2 != null && ptr2.sibling != null) {
						ptr2 = ptr2.sibling;
					}
					ptr2.sibling = ptr.sibling;
					prev.firstChild = ptr.firstChild;
					//print();
					removeTag1(tag,ptr.firstChild,prev);
					return;
				}else {
					TagNode ptr2 = ptr.firstChild;
					
					while(ptr2 != null && ptr2.sibling != null) {
						ptr2 = ptr2.sibling;
					}
					
					ptr2.sibling = ptr.sibling;
					prev.sibling = ptr.firstChild;
					//print();
					removeTag1(tag,ptr.sibling,prev.sibling);
					return;
				}
			}
			removeTag1(tag,ptr.firstChild, ptr);
			removeTag1(tag,ptr.sibling, ptr);
		}
	}
	
	private void removeTag2(String tag, TagNode ptr, TagNode prev) {
		if(ptr == null) {
			return;
		}else {
			if(ptr.tag.equals(tag)) {
				if (prev.firstChild != null && prev.firstChild.tag.equals(ptr.tag)) {
					prev.firstChild = ptr.firstChild;
					TagNode ptr2 = ptr.firstChild, ptr3 = ptr.sibling, prev2 = null;
					while (ptr2 != null) {
						ptr2.tag = "p";
						removeTag2(tag, ptr2.firstChild, ptr);
						prev2 = ptr2;
						ptr2 = ptr2.sibling;
					}
					prev2.sibling = ptr3;
				}else {
					prev.sibling = ptr.firstChild;
					TagNode ptr2 = ptr.firstChild, ptr3 = ptr.sibling, prev2 = null;
					while (ptr2 != null) {
						ptr2.tag = "p";
						removeTag2(tag, ptr2.firstChild, ptr);
						prev2 = ptr2;
						ptr2 = ptr2.sibling;
					}
					prev2.sibling = ptr3;
				}
				removeTag2(tag,ptr.sibling,ptr);
			}else {
				removeTag2(tag,ptr.firstChild,ptr);
				removeTag2(tag,ptr.sibling,ptr);
			}
		}
	}
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	public void addTag(String word, String tag) {
		addTag(word,tag,root);
	}
	
	private void addTag(String word, String tag, TagNode ptr) {
		if(ptr == null) {
			return;
		}else {
			if(ptr.tag.contains(word)) {
				StringTokenizer tokens = new StringTokenizer(ptr.tag," ");
				String currentTag = ptr.tag;
				while(tokens.hasMoreTokens()) {
					String str = tokens.nextToken();
					if(getTagged(word, str)) {
						int index = ptr.tag.indexOf(str);
						String toTag;
						if(str.length() == word.length() + 1) {
							toTag = ptr.tag.substring(index, index + word.length() + 1);
						}else {
							toTag = ptr.tag.substring(index, index + word.length());
						}
						TagNode tagged = new TagNode(toTag,null,null);
						TagNode currentSibling = ptr.sibling;
						if(index == 0) {
							ptr.tag = tag;
							ptr.firstChild = tagged;
							if(currentTag.length() != word.length()) {
								ptr.sibling = new TagNode(currentTag.substring(index+word.length()),null,currentSibling);
							}
							addTag(word,tag,ptr.sibling);
							break;
						}else if(index + toTag.length() == currentTag.length()) {
							ptr.tag = currentTag.substring(0,index);
							ptr.sibling = new TagNode(tag,tagged,currentSibling);
							addTag(word,tag,ptr.sibling.sibling);
							break;
						}else {
							String after = currentTag.substring(index+word.length());
							ptr.tag = currentTag.substring(0,index);
							ptr.sibling = new TagNode(tag,tagged,new TagNode(after,null,currentSibling));
							addTag(word,tag,ptr.sibling.sibling);
							break;
						}
					}
				}
			}else {
				addTag(word, tag, ptr.firstChild);
				addTag(word, tag, ptr.sibling);
			}
		}
	}
	
	private boolean getTagged(String word, String s) {
		String validChars = "!?.;:";
		s = s.toLowerCase();
		if(s.length() > word.length()+1) {
			return false;
		}else if(s.length() == word.length()+1){
			char c = s.charAt(s.length() - 1);
			
			if(s.substring(0,s.length() - 1).equals(word) && validChars.contains(c+"")) {
				return true;
			}
		}else if(s.length() == word.length()){
			if(s.equals(word)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
	/**
	 * Prints the DOM tree. 
	 *
	 */
	public void print() {
		print(root, 1);
	}
	
	private void print(TagNode root, int level) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			for (int i=0; i < level-1; i++) {
				System.out.print("      ");
			};
			if (root != this.root) {
				System.out.print("|----");
			} else {
				System.out.print("     ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level+1);
			}
		}
	}
}
