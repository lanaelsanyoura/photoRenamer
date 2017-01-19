package photo_renamer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * PhotoSystem class uses Singleton design patter, so that only one instance of class can be created. 
 * This class delegates add, removing, and edition of tags between images 
 * and directories. 
 */
public class PhotoSystem {

	private static final PhotoSystem instance = new PhotoSystem();
	/**
	 * The master list of tags
	 */
	protected ArrayList<String> tagList; 
	/**
	 * Serialized tags
	 */
	protected static File tagFile;
	/**
	 * List of serialized log of changes
	 */
	protected static File logFile;
	/**
	 * The path of the file to the node
	 */
	private HashMap<String, TreeNode> pathToNode;
	

	private PhotoSystem(){
		
		// Serialize/
		tagFile = new File("tagList.ser");
		logFile = new File("Log.ser");
		this.tagList = new ArrayList<String>();
		this.pathToNode = new HashMap<String, TreeNode>();
		
        if (tagFile.exists()) {
        	try {
				this.readFromTagsFile(tagFile.getPath());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        } else if (!tagFile.exists()){
            try {
				tagFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        if (logFile.exists()) {
        	 try {
				this.readLogsFromFile(logFile.getPath());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
             try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public static PhotoSystem getInstance() {
		return instance;
	}
	
	/**
	 * Return an array list of tags
	 * @return List of tags
	 */
	public  ArrayList<String> getTagList() {
		return this.tagList;
	}

	/**
	 * Set the this.tagList
	 * @param this.tagList
	 * 				The list of tags
	 */
	public  void setTagList(ArrayList<String> tagList) {
		this.tagList = tagList;
	}

	public Directory buildTree (File file){
			String path = file.getPath();
			try {
				this.readLogsFromFile(logFile.getPath());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Directory rootNode = null;
			if (this.pathToNode.containsKey(path)) {
				rootNode = (Directory) this.pathToNode.get(path);
			}else {
				rootNode = new Directory(file, file.getName(), FileType.DIRECTORY, null);
				this.pathToNode.put(path, rootNode);
			}
			buildTreeHelper(file, rootNode);
			
			return rootNode;
	}
	
	/**
	 * Build the tree of the given file
	 * @param file
	 * 			The file of the node
	 * @param curr
	 * 			The node we're building off of
	 */
	public void buildTreeHelper(File file, TreeNode curr) {
		
		// use the file.listFiles() method to get all the files/directories inside file
		File [] list = file.listFiles();
		
		// iterate though the files/directories inside file
		// if it is an Image, add it to the tree; if it is a directory recurse though it
		for(File child: list){
			String path = child.getPath();
			if (child.isDirectory()){
				// Deserialize path to node hashmap
				try {
					this.readLogsFromFile(logFile.getPath());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				Directory childNode = null;
				if (this.pathToNode.containsKey(path)) { // Child has been edited before
					
					childNode = (Directory) this.pathToNode.get(child.getPath());
					Set<String> childrenPaths = curr.getPathChildren();
					if (!(childrenPaths.contains(childNode.getFilePath()))) {	// Child was not a child of the root
						
						curr.addChild(childNode.getFilePath(), childNode);
						this.pathToNode.put(path, childNode);
						
						Directory parent = (Directory) curr;
						Directory newChildNode = (Directory) childNode;
						
						mergeTags((Directory) parent, newChildNode);
					  }
				}else {
					childNode = new Directory(child, child.getName(), FileType.DIRECTORY , curr);
					this.pathToNode.put(path, childNode);
					curr.addChild(childNode.getFilePath(), childNode);
				}
				
				buildTreeHelper(child, childNode);
				this.pathToNode.put(curr.getFile().getPath(), curr);
				// Serialize this log now
				this.saveMapToFile(logFile.getPath());
			
			// If we encounter an image 
			}else{		
				String extension = fileIsImage(child.getName());
				if (extension != null){
					Image childNode = null;
					if (this.pathToNode.containsKey(path)) {
						childNode = (Image) this.pathToNode.get(child.getPath());
						
					}else {
						childNode = new Image(child, child.getName(), FileType.IMAGE , curr, extension);
						this.pathToNode.put(path, childNode);
						curr.addChild(childNode.getName(), childNode);
						
					}
					// Serialize this log now
					this.pathToNode.put(curr.getFile().getPath(), curr);
					this.saveMapToFile(logFile.getPath());
					
				}
				
					}
			}
		}
	
	private void mergeTags(Directory parent, Directory child) {
		if (parent != null) {
			ArrayList<String[]>parentLog = parent.getNameLog();
			ArrayList<String[]> childLog = child.getNameLog();
			HashMap<String, ArrayList<Image>> childTags = child.getListofTags();
			Set<String> tagList = childTags.keySet();
			HashMap<String, ArrayList<Image>> parentTags = parent.getListofTags();
			
			for (String[] change : childLog) {
				if (!parentLog.contains(change)) {
					parentLog.add(change);
					parent.setNameLog(parentLog);
				}
			}
			for (String tag : tagList) {
				if (parentTags.containsKey(tag)) {
					parent.getListofTags().get(tag).addAll(child.getListofTags().get(tag));
				} else {
					parent.getListofTags().put(tag,child.getListofTags().get(tag));
				}
			}
			mergeTags((Directory)parent.getParent(), (Directory)child.getParent());
		} 
		
	}
	
	/**
	 * Add the given Tag to the image and directory logs
	 * @param img
	 * 			Image to be tagged
	 * @param tag
	 * 			The tag to be added
	 * @param dir
	 * 			The directory to which this image belongs to
	 */
	
	public  void addImageTag(Image img, String tag, Directory dir) throws TagExistsException{

			// Replace the paths and add a new tag
			
			((Directory) img.parent).addImageTag(img, tag);
			if (!this.tagList.contains(tag) ) {
				this.tagList.add(tag);
				this.saveTagsToFile(tagFile.getPath());
			}
			
			String path = img.getFilePath();
						
			img.addTag(tag);
			updatePathToNode(img, path);

			
	}
	
	private void updatePathToNode(Image img, String oldPath) {
		String newPath = img.getFilePath();
		
		this.pathToNode.remove(oldPath);
		this.pathToNode.put(newPath, img);
		this.pathToNode.put(img.parent.getFilePath(), img.getParent());
		
		this.saveMapToFile(logFile.getPath());
	}
	
	/**
	 * Delete the given Tag from all images in the directory dir, keep track using the image and directory logs
	 * 
	 * @param tag
	 * 			The tag to be deleted
	 * @param dir
	 * 			The directory to which this image belongs to
	 */
	public  void deleteFromAllImages(String tag, Directory dir) {
		ArrayList<Image> Alist = dir.getListofTags().get(tag);
		if (Alist != null) {
			Image[] images = Alist.toArray(new Image[0]);
			for (Image img: images) {
				
				((Directory) img.parent).deleteImageTag(img, tag);
				
				String path = img.getFilePath();
				
				img.deleteTag(tag);
				
				 updatePathToNode(img, path);
				
			}
		}
	}
	/**
	 * Add the given Tag to the image and directory logs
	 * @param img
	 * 			Image to be edited
	 * @param tag
	 * 			The tag to be deleted
	 * @param dir
	 * 			The directory to which this image belongs to
	 */
	public  void deleteImageTag(Image img, String tag, Directory dir) {
		
		// Replace the paths and delete a new tag
		
		((Directory) img.parent).deleteImageTag(img,tag);
		
		String path = img.getFilePath();
		
		img.deleteTag(tag);
		
		 updatePathToNode(img, path);
	}
	
	/**
	 * Search for the given image by Tag
	 * @param tag
	 * 			Tag to be searched by
	 * @param dir
	 * 			The directory to which this image belongs to
	 * @return The images that contain this tag
	 * @throws IOException 
	 */
	public  ArrayList<Image> searchImagesbyTag(String tag, Directory dir) throws IOException {
		return dir.searchImagesbyTag(tag);
	}
	
	/**
	 * Find the image with the most tags
	 * @param current
	 * @return The image with the most tags
	 */
	public  Image mostTags(TreeNode current) {
		TreeNode newCurr = null;
		if (current.isImage()) {
			newCurr = (Image) mostTagsHelper(current.getParent(), (Image)current);
		}
		else {
			for (TreeNode child: current.getChildren()) {
				newCurr = mostTags(child);
			}
		}
		return (Image) newCurr;
		
	
	}
	
	/**
	 * Helper function for finding the image with the most tags
	 * @param curr 
	 * 			Current node
	 * @param max 
	 * 			Maximum number of tags an image has
	 * @return The image with the most tags
	 */
	private static Image mostTagsHelper(TreeNode curr, Image max) {
		Image newMax = max;
		if (curr.isImage()) {
			if (((Image) curr).getTags().size() >= max.getTags().size()) {
				newMax = (Image) curr;
			}
		}
		else {
			Collection<TreeNode> children = curr.getChildren();
			for (TreeNode child: children) {
				max = mostTagsHelper(child, max);
			}
			
			newMax = max;	
		}
		return (Image) newMax;	
	}
	
	/**
	 * Revert all the changes up until a certain point
	 * @param dir
	 * 			Directory to revert changes from
	 * @param begin
	 * 			The time to which we will revert the changes
	 * @throws TagExistsException 
	 */
	
	public  void revertChanges(Directory dir, Integer begin) throws TagExistsException{
		
		String[][] log = dir.getNameLog().toArray(new String[0] [0]);
		int end = log.length - 1;

		if ( begin <= end) {
			while (end >= begin) {
				
				String[] nestedList = log[end];
				String change = nestedList[2];
				String ext = PhotoSystem.fileIsImage(nestedList[1]);
				String original = new String();
				if (!nestedList[1].contains(" @")){
					original = nestedList[1];
				}
				else{
					original = nestedList[1].split(" @")[0] + ext;
				}
				Image imgNode = (Image) dir.findChildReverted(original, nestedList[3]);
				// Added
				
				if (change.startsWith("A")) {
					String newTag = change.split(" ")[1];
					deleteImageTag(imgNode, newTag, dir);
					
				}
				
				if (change.startsWith("D")) {
					String newTag = change.split(" ")[1];
					addImageTag(imgNode, newTag, dir);
				}
				
				if (change.startsWith("N")) {
					String[] oldTonew = (change.split(" : ")[1]).split(" -> ");
					String old = oldTonew[0];
					try {
						this.chooseOldName(imgNode, old);
					} catch (NonExistentNameException e) {
						
					}

				}
				end = end -1;
			}
		}
	}

	/**
	 * Get the most common tag
	 * @param dir
	 * @return the most common tag
	 */
	public  HashMap<Integer, ArrayList<String>> mostCommonTag(Directory dir){
		return dir.mostCommonTag();
	} 
	
	/**
	 * Get the time log of this directory 
	 * @param dir
	 * @return the most common tag
	 */
	public  StringBuffer getHistory(TreeNode dir) {
		return dir.getHistory();
		}
	
	/**
	 * Allow the user to choose from old names
	 * @param img
	 * 			The image we are editing
	 * @param dir
	 * 			The directory the image belongs to
	 */
	public  void chooseOldName(Image img, String name) throws NonExistentNameException {
			String path = img.getFilePath();
			
			img.chooseFromOldName(name);
			
			 updatePathToNode(img, path);
		
		
	}
	
	/**
	 * Return the extension of the file/node it it's an image
	 * @param name
	 * 			The name of the image
	 * @return The image's extension or null if this file is not an image
	 */
	private static  String fileIsImage(String name) {
		String[] allowedExtensions = {".jpg", ".gif", ".svg", ".jpeg", ".png", ".tiff", ".bmp", ".JPG"};
		for (String suff: allowedExtensions) {
			if (name.endsWith(suff)) {
				return suff;
			}
		}
		return null;
	}

	
	/**
	 * Return all the nodes in this image Tree 
	 * @param fileNode
	 * 				The root of the tree
	 * @param contents
	 * 				The String Buffer we are concatenating to
	 * @param prefix
	 * 				The prefix to append to
	 */
	public  void buildDirectoryMap(TreeNode fileNode, StringBuffer contents, String prefix, HashMap<String, TreeNode> nodeMap) {
			
			// add the initial prefix and root FileNode
			nodeMap.put(prefix + fileNode.getName(), fileNode);
			contents.append(prefix);
			contents.append(fileNode.getName());
			// increase the prefix using the static variable DirctoryExplorer.PREFIX
			prefix = prefix + ">";
			
			// iterate though the children of the root node
			// if it is a file, add it; if it is a directory, recurse though it
			for (TreeNode child: fileNode.getChildren()){
				if (child.isImage()){
					contents.append('\n');  // '\n' character adds new line
					nodeMap.put(prefix + child.getName(), child);
					contents.append(prefix);
					contents.append(child.getName());
				} else{
					contents.append('\n');
					buildDirectoryMap(child, contents, prefix, nodeMap);
				}
}
	}
	
	/**
	 * Add more than one tag to the given image in the chosen directory
	 * @param tags
	 * 			The list of tags
	 * @param img
	 * 			The image the tags are to be added to
	 * @param dir
	 * 			The directory we are adding tags to 
	 * @throws TagExistsException 
	 */
	public  void addManyTags(String[] tags, Image img, Directory dir) throws TagExistsException {
		for (String spacedTag: tags) {
			String tag = spacedTag.trim();
			addImageTag(img, tag, dir);
		}
	}
	
	/**
	 * Delete more than one tag to the given image in the chosen directory
	 * @param tags
	 * 			The list of tags
	 * @param img
	 * 			The image the tags are to be added to
	 * @param dir
	 * 			The directory we are adding tags to 
	 * @throws TagExistsException 
	 */
	public  void deleteManyTags(String[] tags, Image img, Directory dir, StringBuffer message) {
		
		for (String spacedTag: tags) {
			String tag = spacedTag.trim();
			if(img.getTags().contains(tag)){
				deleteImageTag(img, tag, dir);
		}else{
			message.append(tag + "\n");
		}
			
		}
	}
    
	/**
	 * Save the structure to the given file
	 * @param filePath
	 * 				The filePath of the file we are writing to
	 * @param toStore
	 * 				The generic object toStore
	 */
	public  void saveMapToFile(String filePath){
		try {
	        OutputStream file = new FileOutputStream(filePath);
	        OutputStream buffer = new BufferedOutputStream(file);
	        ObjectOutput output = new ObjectOutputStream(buffer);
	
	        // serialize the Map
	        output.writeObject(this.pathToNode);
	        output.close();
		 } catch (IOException ex) {
	          System.out.println("Can't Save");
	        } 
    }
	
	/**
	 * Save the structure to the given file
	 * @param filePath
	 * 				The filePath of the file we are writing to
	 * @param toStore
	 * 				The generic object toStore
	 */
	public  void saveTagsToFile(String filePath){
		try {
	        OutputStream file = new FileOutputStream(filePath);
	        OutputStream buffer = new BufferedOutputStream(file);
	        ObjectOutput output = new ObjectOutputStream(buffer);
	
	        // serialize the Map
	        output.writeObject(this.tagList);
	        output.close();
		 } catch (IOException ex) {
	          System.out.println("Can't Save");
	        } 
    }
	

	@SuppressWarnings({ "unchecked" })
	public void readLogsFromFile(String path) throws ClassNotFoundException {
        try {
            InputStream file = new FileInputStream(path);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            
            this.pathToNode = (HashMap<String, TreeNode>) input.readObject();
            
            input.close();
        } catch (IOException ex) {
          
        }  
        
	}
	
	@SuppressWarnings({ "unchecked" })
	public void readFromTagsFile(String path) throws ClassNotFoundException {
        try {
            InputStream file = new FileInputStream(path);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            
            this.tagList = (ArrayList<String>) input.readObject();
            
            input.close();
        } catch (IOException ex) {
          
        }  
        
	}
	public HashMap<String, TreeNode> getPathToNode() {
		return this.pathToNode;
	}
}

