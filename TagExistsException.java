package photo_renamer;
public class TagExistsException extends Exception{
	private static final long serialVersionUID = 1L;

	public TagExistsException(String message) {
		super(message); 
	}

}
