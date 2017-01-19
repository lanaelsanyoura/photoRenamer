package photo_renamer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import java.awt.Component;
	
/**
 * ImageExplorer class is a GUI that allows users to edit and explore images 
 * and directories. 
 * ImageExploer uses ActionsListeners which is an implementation of 
 * an Observer design pattern. 
 */
public class PhotoRenamer {
		private final JFrame photoRenamer; 
		
		private final JLabel directoryLabel;
		private final JPanel taggingPanel;
		private final JPanel navigatingPanel;
		private final JPanel imagePanel;
		
		private final JButton openButton;
		private JButton addButton; 
		private JButton deleteButton; 
		private final JButton chooseOldNameButton;
		private final JButton searchButton;
		private final JButton mostTagsButton;
		private final JButton mostCommonTagsButton;
		private final JButton historyButton;
		private final JButton revertButton;
		
		private final JTextArea textArea;
		private final JScrollPane scrollPane;
		private JScrollPane listScroll;
		private ImageIcon mainIcon; 
		private JLabel imageLabel;
		private final JTextField tagField; 
		private JList<String> folderList;
		private Directory currentDir;
		
		private PhotoSystem pr;
		private HashMap<String, TreeNode> nodeMap;
		private TreeNode selectedNode;

		private JButton instructionButton;

		private JButton viewTagsButton;

		private String currentPath;
	
		public PhotoRenamer (){ 
			
			this.nodeMap = new HashMap<>();
			this.selectedNode = null;
			pr = PhotoSystem.getInstance();
			this.photoRenamer = new JFrame("PhotoRenamer");
			directoryLabel = new JLabel ("Select a directory");
			photoRenamer.setPreferredSize(new Dimension(1200, 1000));
			taggingPanel = new JPanel();
			navigatingPanel = new JPanel();
			
			BorderLayout imageLayout = new BorderLayout();
			imagePanel = new JPanel();
			imagePanel.setLayout(imageLayout);
			imagePanel.setPreferredSize(new Dimension(700, 300));
			
			//The open new directory button
			openButton = new JButton("Choose Directory");
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chooseFile();
				}
			});
			
			// The add new tag to image button
			addButton = new JButton("Add Tag");
			addButton.setEnabled(false);
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addTagtoImage();
				}
			});
			
			//The delete tag from image button 
			deleteButton = new JButton("Delete Tag");	
			deleteButton.setEnabled(false);
			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteTagFromImage();
				}
			});
			
			chooseOldNameButton = new JButton("Choose Old Name for Image");
			chooseOldNameButton.setEnabled(false);
			chooseOldNameButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					chooseOldName();
				}
			});
			
			// Gives the user instructions on how to navigate through the program
			instructionButton = new JButton("Instructions");
			instructionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					displayWelcomeMessage();
							
				}
			});
			
			// The search for image button
			searchButton = new JButton("Search Images");
			searchButton.setEnabled(false);
			searchButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					searchForImage();
				}
			});
			
			// Button to find image with most tags
			mostTagsButton = new JButton("Find image with most tags");
			mostTagsButton.setEnabled(false);
			mostTagsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getMostTagged();
				}
			});
			
			// Button to find most commonly used tag
			mostCommonTagsButton = new JButton("Find most commonly used tag");
			mostCommonTagsButton.setEnabled(false);
			mostCommonTagsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getMostCommonTag();
				}
			});
			
			// Button to get the history of changes 
			historyButton = new JButton("Get Selected File's Changes");
			historyButton.setEnabled(false);
			historyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getHistoryofImages();
				}
			});
			
			// Button to revert changes 
			revertButton = new JButton("Revert changes");
			revertButton.setEnabled(false);
			revertButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					revertChanges();
				}
			});
			
			// Button to revert changes 
			viewTagsButton = new JButton("View Master Tag List");
			viewTagsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textArea.setText(pr.getTagList().toString());
				}
			});
			
			textArea = new JTextArea(30, 5);
			displayWelcomeMessage();
			scrollPane = new JScrollPane(this.textArea);
			
			tagField = new JTextField();
			// Add a KeyListener so that when the user inputs two tags separated by a comma,
			// it means that they add adding/delete many tags at a time
			tagField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_COMMA){
						deleteButton.setText("Delete Many Tags");
						addButton.setText("Add Many Tags");
					}else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
						if (!tagField.getText().contains(",")){
							deleteButton.setText("Delete Tag");
							addButton.setText("Add Tag");
						}
					}
				}
				
			});
			
		
			BufferedImage img = null;
			this.currentPath = new File("").getAbsolutePath();
			if (!currentPath.endsWith("/src")) {
				currentPath += "/src";
			}
			
			try {
			 img =  ImageIO.read(new File(currentPath +  "/Images/image.png" ) );//new File(new File("").getAbsolutePath() + "/src/Images/image.png"));
			 
			} catch  (IOException e){
				
			}
			
			mainIcon = new ImageIcon(img);
			imageLabel = new JLabel("Your Image", mainIcon, JLabel.CENTER);
			
			folderList = new JList<String>(new DefaultListModel<String>());
			folderList.setPreferredSize(new Dimension(200, 400));
			folderList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					chooseFromList(folderList.getSelectedValue());
										
				}
				
			});
			listScroll = new JScrollPane(folderList);
			
			navigatingPanel.add(openButton);
			navigatingPanel.add(searchButton);
			navigatingPanel.add(mostTagsButton);
			navigatingPanel.add(mostCommonTagsButton);
			navigatingPanel.add(historyButton);
			navigatingPanel.add(revertButton);
			
			
			
			// Format text box
			tagField.setPreferredSize(new Dimension(200, 30));
			taggingPanel.add(tagField);
			taggingPanel.add(deleteButton);
			taggingPanel.add(addButton);
			taggingPanel.add(chooseOldNameButton);
			taggingPanel.add(instructionButton);
			taggingPanel.add(viewTagsButton);

			
			//Set the position of the text, relative to the icon:
			imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
			imageLabel.setHorizontalTextPosition(JLabel.CENTER);
			
		
			imagePanel.add(imageLabel, BorderLayout.CENTER);
			imagePanel.add(folderList, BorderLayout.WEST);
			
			// The directory choosing button.
			
			openButton.setVerticalTextPosition(AbstractButton.CENTER);
			openButton.setHorizontalTextPosition(AbstractButton.LEADING); 
			openButton.setMnemonic(KeyEvent.VK_D);
			openButton.setActionCommand("disable");

			
			
			photoRenamer.add(imagePanel, BorderLayout.EAST);
			photoRenamer.add(navigatingPanel, BorderLayout.SOUTH);
			photoRenamer.add(taggingPanel, BorderLayout.NORTH);
			
			
			
			// Set initial current directory to null, the directory that user chooses becomes new currentDir
			this.currentDir = null;		
			
			Container c = photoRenamer.getContentPane();
			c.add(directoryLabel, BorderLayout.CENTER);
			c.add(scrollPane, BorderLayout.CENTER);
			c.add(listScroll, BorderLayout.WEST);


		}
	
		
		/**
		 * Get the current directory that the program is working with. 
		 * @return current Directory
		 */
		public Directory getCurrentDir() {
			return currentDir;
		}


		/**
		 * Set the current directory that the program is working with.
		 * @param currentDir
		 */
		public void setCurrentDir(Directory currentDir) {
			this.currentDir = currentDir;
		}


		/**
		 * Update the thumbnail of the picture to display the image
		 * every time the user chooses an image. If it is a directory, display 
		 * a generic image of a directory. 
		 */
		private void updateIcon() {
			
			// Default Image
			BufferedImage img = null;
			String fileImgName =  this.currentPath + "/Images/image.png";
			String fileName = "image.png";
			
			// Selected Image or Directory
			if (this.selectedNode != null) {
				fileName = this.selectedNode.getName();
				
				//Directory's image
				if (this.selectedNode.getFile().isDirectory()) {
					if (this.selectedNode.getChildren().isEmpty()) {
						fileImgName =  this.currentPath +  "/Images/folder.png";
					}
					else {
						fileImgName =  this.currentPath +  "/Images/fullFolder.png";
				}
				
				// Selected Image
				} else {
					fileImgName = this.selectedNode.getFilePath();
				}
			try {
				img = ImageIO.read(new File(fileImgName));
			} catch  (IOException e){
			}
			
			this.mainIcon.setImage(img.getScaledInstance(320, 320, BufferedImage.SCALE_DEFAULT));
			this.imageLabel.setText(fileName); 
			
			}
		}
		
		
		/**
		 * Set the file that the user has chosen to work with as the currently
		 * selected file. If it is an image, enable image commands. 
		 * @param selected
		 * 		The currently selected TreeNode
		 */
		private void chooseFromList(String selected) {
			Component[] imageCommands = taggingPanel.getComponents();
			for (Component comp : imageCommands) {
				comp.setEnabled(true);
			}
			this.selectedNode = this.nodeMap.get(selected);
			if ((this.selectedNode == null) || (!this.selectedNode.isImage())){
				this.modifyImageButtons();
			}
			else{
				if (tagField.getText().contains(",")){
					deleteButton.setText("Delete Many Tags");
				}
				else{
				deleteButton.setText("Delete Tag");
				}
				addButton.setEnabled(true); // Only add tags to images
				revertButton.setEnabled(false); // Only revert changes of a directory
			}
			updateIcon();
		}
		
		/**
		 * Modify the buttons when dealing with a directory
		 */
		private void modifyImageButtons() {
			this.textArea.setText("Please select a file");
			deleteButton.setText("Delete Tag from All Directory Images");
			addButton.setEnabled(false);
			chooseOldNameButton.setEnabled(false);
			revertButton.setEnabled(true);
		}
		
		/**
		 * Use the currently selected directory, and build a tree to include 
		 * all the directories children. 
		 */
		private void chooseFile(){ 
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fileChooser.showOpenDialog(photoRenamer.getContentPane());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if (file.exists()  && file.isDirectory()) {
					directoryLabel.setText("Selected File" + file.getAbsolutePath());
					// Display a temporary message while the directory is
					// traversed.
					this.textArea.setText("Building file tree...");

					// Make the root.
					TreeNode fileTree = pr.buildTree(file);
										
					// Build the string representation and put it into the text
					// area.
					this.currentDir = (Directory) fileTree;

					updateString();
					textArea.setText("Now you can edit and navigate the images in " + fileTree.getName());
					
					Component[] dirComponents =  navigatingPanel.getComponents();
					for (Component comp : dirComponents) {
						comp.setEnabled(true);
					}
				}
				
			} else {
				directoryLabel.setText("No Path Selected");
			}
		}
		
		
		/**
		 * Build a string buffer representation of the contents of the tree rooted
		 * at n, prepending each file name with prefix, and adding and an additional
		 * prefix for subdirectory contents. Update this string whenever the 
		 * Directory or its children get updated. 
		 */
		private void updateString(){
			StringBuffer contents = new StringBuffer();
			this.nodeMap = new HashMap<>();
			((PhotoSystem) pr).buildDirectoryMap(this.currentDir, contents, ">", this.nodeMap);
			
			String[] listcontents = contents.toString().split("\n");
			DefaultListModel<String> newList = new DefaultListModel<String>();
			for (String item: listcontents) {
				newList.addElement(item);
			}
			this.folderList.setModel(newList);
		}
		
		/**
		 * When the user presses this button, return the most commonly used 
		 * tag in the directory. 
		 */
		private void getMostCommonTag(){ 
			HashMap<Integer, ArrayList<String>> tags = pr.mostCommonTag(this.currentDir);
			this.textArea.setText("The most commonly used tag is " + tags.toString());
		}
	
		/**
		 * When the user presses this button, return the most tagged 
		 * image in the directory. 
		 */
		private void getMostTagged(){
			Image mostTaggedImage = pr.mostTags(this.currentDir);
			this.textArea.setText("The image with the most tags is " + mostTaggedImage.getName());
		}
		
		/**
		 * Search for the images containing the tag that is inputed by the 
		 * user in the text field. If the user does not input anything, 
		 * Prompt them to input a tag to search. 
		 */
		private void searchForImage(){
			if (!this.tagField.getText().isEmpty()){
				String tag = this.tagField.getText();
				ArrayList<Image> imgList;
				try {
					imgList = pr.searchImagesbyTag(tag, this.currentDir);
					this.textArea.setText("The images with this tag are: " + imgList.toString());
				} catch (IOException e) {
					this.textArea.setText(e.getMessage());
				}
				
			} else{
				this.textArea.setText("Please input a tag to search");
			}
			
		}
		
		/**
		 * Display the history of changes in the system when button is pressed. 
		 */
		private void getHistoryofImages(){ 
			TreeNode selected = this.currentDir;
			if (this.selectedNode != null) {
				selected = this.selectedNode;
			}
			StringBuffer history = pr.getHistory(selected);
			this.textArea.setText("The history of " + selected.toString() + " is : \n" + history.toString());
		}
		
		/**
		 * Revert the changes up to a specific date, when button is pressed. 
		 * Make sure the the user inputs a valid index to revert up to. 
		 */
		private void revertChanges(){
			String begin = tagField.getText();
			if (begin.matches("\\d+")){
				int start = Integer.parseInt(begin);
				try {
					pr.revertChanges((Directory)this.currentDir, start);
				} catch (TagExistsException e) {
				}
				this.getHistoryofImages();
				this.updateString();
			}else{
				this.textArea.setText("Please put in a vaid index that you wish to revert to.");
			}
	
		}
		
		/**
		 * Add a tag to an image. Make sure that the tag is specified by the user
		 * using the text field, and that it does not already exist.  
		 * If more than one tag is inputed (using a comma), add all tags to the image. 
		 */
		private void addTagtoImage(){
		
			String tag = tagField.getText();
			if (tag.trim().isEmpty()){
				this.textArea.setText("Please input a tag to be added.");
			} else {
			if(tag.contains(",")){
				this.addManyTags(tag);
			}else{
				this.addSingleTag(tag);
			}
		}
		}
		
		/**
		 * Add one tag to the selected image
		 * @param tag
		 */
		private void addSingleTag(String tag) {
			try{
				pr.addImageTag((Image)this.selectedNode, tag.trim(), this.currentDir);
				this.imageLabel.setText(this.selectedNode.getName()); 
				this.textArea.setText("Tag " + tag + "added." );
				updateString();
			} catch (TagExistsException e){
				this.textArea.setText(e.getMessage());
			}
			
		}


		/**
		 * Add many tags to the selected image
		 * @param tag
		 */
		private void addManyTags(String tag) {
			String[] list = tag.split(",");
			try {
				pr.addManyTags(list, (Image)this.selectedNode, this.currentDir);
				this.imageLabel.setText(this.selectedNode.getName()); 
				this.textArea.setText("Tags " + list.toString() + "added." );
				updateString();
			} catch (TagExistsException e) {
				this.textArea.setText(e.getMessage());
			}
		}
		/**
		 * Delete tag from an image. Make sure that the tag is specified by the user
		 * using the text field, and that it exists.
		 * If more than one tag is inputed (using a comma), delete all tags from the image. 
		 * If the user chooses to delete a tag from a directory, delete all instances 
		 * of the tag found in the directory's images.
		 */
		private void deleteTagFromImage(){
			String tag = tagField.getText();
			if (tag.isEmpty()) {
				this.textArea.setText("Please input a tag to be deleted" );
			}
			else if (this.selectedNode == null) {
				pr.deleteFromAllImages(tag.trim(), (Directory) this.currentDir);
				this.imageLabel.setText(this.currentDir.getName()); 
				updateString();	
				
			} else {
			if (this.selectedNode.isImage()){
				if(tag.contains(",")){
					this.deleteManyTags(tag);
				}else{
					this.deleteOneTag(tag);
				}
				
			}else{
					pr.deleteFromAllImages(tag.trim(), (Directory) this.selectedNode);
					this.imageLabel.setText(this.selectedNode.getName()); 
					this.textArea.setText("Tag " + tag + " deleted from all images." );
			
			}
			updateString();	
			}
		}
		
		/**
		 * Delete a single tag
		 * @param tag
		 */
		private void deleteOneTag(String tag) {
			if (((Image) this.selectedNode).getTags().contains(tag)){
				pr.deleteImageTag((Image)this.selectedNode, tag.trim(), this.currentDir);
				this.imageLabel.setText(this.selectedNode.getName()); 
				this.textArea.setText("Tag " + tag + " deleted." );
				updateString();
			} else{
				this.textArea.setText("Cannot delete a tag that does not exist.");
			}
			
		}

		
		/**
		 * Delete many tags from the selected node
		 * @param tag
		 */
		private void deleteManyTags(String tag) {
			String[] list = tag.split(",");
			StringBuffer message = new StringBuffer();
			pr.deleteManyTags(list, (Image)this.selectedNode, this.currentDir, message);
			this.imageLabel.setText(this.selectedNode.getName()); 
			this.textArea.setText("Tags " + list.toString() + " deleted." );
			if (!message.toString().isEmpty()) {
				this.textArea.setText("The following tags could not be deleted: \n " + message.toString());
			
		}

		}
		
		private void chooseOldName(){
			String name = this.tagField.getText();
			try {
				pr.chooseOldName((Image) this.selectedNode, name);
				this.imageLabel.setText(this.selectedNode.getName());
				this.textArea.setText("Name changed to: " +name );
				updateString();
			} catch (NonExistentNameException e) {		
				this.textArea.setText(e.getMessage() + "\n" + "Previous image names are: " + ((Image) this.selectedNode).getPreviousNames());
			}
		}
		
		private void displayWelcomeMessage() {
		String message  = "Welcome to PhotoRenamer! Here are some tips \n \n"
				+ "Choose a Directory \n \n"
				+ "Add One Tag: Select an image and add your tag. \n \t e.g. Tag <image.jpg>  with 'Vacation' --> 'image @Vacation.jpg' \n \n"
				+ "Add Many Tags: Seperate your tags by COMMAS (,) \n \t e.g. Tag <image.jpg> with 'Vacation, July, Sunset'--> 'image @Vacation @July @Sunset.jpg' \n \n"
				+ "Delete One Tag: Delete a specified tag that already exists from \n \t (a) An image \n \t (b) All the images under a selected Directory \n \n"
				+ "Delete Many Tags: Seperate the tags with COMMAS (,)  \n \n"
				+ "Get History: See all the changes that you have made \n \n"
				+ "Revert Changes: After looking at the history, select a point to which you want to revert to \n \n"
				+ "Search Image: Search Images by the specified tag in the current Directory \n \n " 
				+ "Get Most Common Tag: Search through the selected Directory for the most commonly used tag \n \n"
				+ "Get Image with Most Tags: \n \tFind out which of your images has the most tags! \n \n"
				+ "Choose From Old Name: Input an old name you would want to choose from and rename the selected image to a previous name";
				this.textArea.setText(message);
		}
		
		/**
		 * Create the GUI, combining all it's elements.
		 * Influenced from Week 11 Lab. 
		 */
		private void createAndShowGui() {
			photoRenamer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			photoRenamer.pack();
			photoRenamer.setVisible(true);
		}

		/**
		 * Create and show an image explorer, which the user can then  
		 * work with.
		 *
		 * @param argsv
		 *            the command-line arguments.
		 */
		public static void main(String[] args) {
			PhotoRenamer v = new PhotoRenamer();
			v.createAndShowGui();
			
		}	

	}


