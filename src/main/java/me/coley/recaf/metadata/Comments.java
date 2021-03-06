package me.coley.recaf.metadata;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Comments metadata handler.
 *
 * @author Matt
 */
public class Comments {
	public static final String TYPE = "Lme/coley/recaf/InsnComment;";
	public static final String KEY_PREFIX = "At_";
	private final Map<Integer, String> indexToComment = new TreeMap<>();

	/**
	 * Create an empty comments handler.
	 */
	public Comments() {
	}

	/**
	 * Create a comments handler that populates existing entries from the given method.
	 *
	 * @param method
	 * 		Method with comments.
	 */
	public Comments(MethodNode method) {
		parse(method);
	}

	private void parse(MethodNode method) {
		if (method.visibleAnnotations == null) return;
		for (AnnotationNode anno : method.visibleAnnotations) {
			if (anno.desc.equals(Comments.TYPE)) {
				for (int i = 0; i < anno.values.size(); i += 2) {
					String key = ((String) anno.values.get(i)).substring(Comments.KEY_PREFIX.length());
					String comment = (String) anno.values.get(i + 1);
					if (key.matches("\\d+"))
						indexToComment.put(Integer.parseInt(key), comment);
				}
			}
		}
	}

	/**
	 * Adds a comment at the current instruction offset.
	 *
	 * @param index
	 * 		Method instruction index to insert the comment at.
	 * @param comment
	 * 		Comment string to add.
	 */
	public void addComment(int index, String comment) {
		String existing = indexToComment.get(index);
		if (existing != null) {
			comment = existing + "\n" + comment;
		}
		indexToComment.put(index, comment);
	}

	/**
	 * Write comments to the method.
	 *
	 * @param method
	 * 		Method to write to.
	 */
	public void applyTo(MethodNode method) {
		if (method.visibleAnnotations == null)
			method.visibleAnnotations = new ArrayList<>();
		indexToComment.forEach((index, comment) -> {
			AnnotationNode commentNode = new AnnotationNode(Comments.TYPE);
			commentNode.visit(Comments.KEY_PREFIX + index, comment);
			method.visibleAnnotations.add(commentNode);
		});
	}

	/**
	 * @param offset
	 * 		Instruction offset.
	 *
	 * @return Comment at offset.
	 */
	public String get(int offset) {
		return indexToComment.get(offset);
	}
}
