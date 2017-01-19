package photo_renamer;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The TreeNode class represents either an Image or Directory file and implements Serializable to keep track of previously managed nodes.
 */
public class TreeNode implements Serializable{

	private static final long serialVersionUID = 1137056245318029932L;
	/**
	 * The current name of this node
	 */
	private String name; 
	/**
	 * Either an image or a directory
	 */
	private FileType type;
	/**
	 * The current Filepath 
	 */
	private String filePath;
	/**
	 * The parent Node (= null for the root directory)
	 */
	protected TreeNode parent; 
	/**
	 * Children of the TreeNode with paths representing the keys
	 */
	private Map<String, TreeNode> children;
	/**
	 * The file this TreeNode is tied to
	 */
	private File file;	
	/**
	 * The history of changes made to this TreeNode and all its children
	 */
	protected ArrayList<String[]> nameLog;
	
	/**
	 * Initialize the TreeNode object
	 * @param file
	 * @param name
	 * @param type
	 * @param parent
	 * @param children
	 */
	public TreeNode(File file, String name, FileType type, TreeNode parent, HashMap<String,TreeNode> children) {
		this.name = name;
		this.type = type;
		this.parent = parent;
		this.file = file;
		this.children = children;
		this.nameLog = new ArrayList<String[]> ();
		this.filePath = file.getPath();
	}

	/**
	 * Get the name of this TreeNode
	 * @return the name of this TreeNode
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the name log of this TreeNode
	 * @return this TreeNode's log
	 */
	
	public ArrayList<String[]> getNameLog() {
		return nameLog;
	}
	
	/**
	 * Set the name log of this TreeNode
	 * @param new  nameLog
	 */
	public void setNameLog(ArrayList<String[]> nameLog) {
		this.nameLog = nameLog;
	}
	
	/**
	 * Get the File of this TreeNode
	 * @return this TreeNode's File
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Set the File this TreeNode represents 
	 * @param the new File
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Get the filePath of this TreeNode
	 * @return this TreeNode's filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Set the filePath of this TreeNode
	 * @param the new filePath
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Set the name of this TreeNode
	 * @param name
	 * 			 the name of this TreeNode
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the parent of this TreeNode
	 * @param the parent of this TreeNode
	 */
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * Set the parent of this TreeNode
	 * @return the parent of this TreeNode
	 */
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	/**
	 * Get the children of this TreeNode, Path to image
	 * @return the parent of this TreeNode
	 */
	public Collection<TreeNode> getChildren() {
		return this.children.values();		
	}
	
	/**
	 * Get the children of this TreeNode, Path to image
	 * @return the parent of this TreeNode
	 */
	public Set<String> getPathChildren() {
		return this.children.keySet();
		
		
	}
	
	/**
	 * Add the given child to TreeNode
	 * @param path
	 * @param childNode
	 */
	public void addChild(String path, TreeNode childNode) {
		this.children.put(path, childNode);
		childNode.setParent(this);
	}
	
	/**
	 * Checks whether this TreeNode is an image
	 * @return Returns true if TreeNode is an Image else, returns false 
	 */
	public boolean isImage() {
		return this.type == FileType.IMAGE;
	}
	
	/**
	 * Returns a string representation of this TreeNode
	 * @return The TreeNode's name
	 */
	@Override
	public String toString(){
		return name;
	}
		
	/**
	 * Add the time stamp to this TreeNode and all its parents 
	 * @param name
	 * @param changeDescription
	 */
	public void putTimestamp(String name, String changeDescription) {
		String[] thisChange;
		
		if (this.parent != null){
			thisChange = new String[] {LocalDateTime.now().toString(), name, changeDescription, this.parent.file.getPath()};
			this.nameLog.add(thisChange);
			this.parent.putTimestamp(name, changeDescription);
		} else {
			thisChange = new String[] {LocalDateTime.now().toString(), name, changeDescription, "" };
			this.nameLog.add(thisChange);
		}
			
		
	}
	
	/**
	 * Get the formatted String Buffer representation of this TreeNode's nameLog
	 * @return the history of the TreeNode as a StringBuffer
	 */
	public StringBuffer getHistory(){
		Integer index = 0;
		StringBuffer contents = new StringBuffer();
		for (String[] nestedList: this.nameLog){
			contents.append(index.toString() +": "+ nestedList[0] + ", " + nestedList[1] + ", " + nestedList[2]);
			contents.append("\n");
			index = index +1;
		} 
		return contents;
	}
		}

	
	


