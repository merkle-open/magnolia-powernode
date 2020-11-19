package com.namics.oss.magnolia.helper;

import com.namics.oss.magnolia.powernode.PowerNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class JcrNodeTreePrinter {

	public static void print(PowerNode root, Consumer<String> printer) {
		String tree = getTreeString(root);
		printer.accept(tree);
	}

	public static String getTree(PowerNode root) {
		return getTreeString(root);
	}

	private static String getTreeString(PowerNode root) {
		List<PowerNode> childNodes = root.getChildNodes();
		List<JcrTreeNode> children = buildTree(childNodes);
		JcrTreeNode treeRoot = new JcrTreeNode(root.getName(), children);
		return treeRoot.toString();
	}

	private static List<JcrTreeNode> buildTree(List<PowerNode> childNodes) {
		List<JcrTreeNode> currentLevel = new ArrayList<>();
		for (PowerNode child : childNodes) {
			List<JcrTreeNode> children = Collections.emptyList();
			if (child.hasNodes()) {
				children = buildTree(child.getChildNodes());
			}
			JcrTreeNode node = new JcrTreeNode(child.getName(), children);
			currentLevel.add(node);
		}
		return currentLevel;
	}

	/**
	 * See: https://stackoverflow.com/a/8948691/965852
	 */
	private static class JcrTreeNode {

		final String name;
		final List<JcrTreeNode> children;

		public JcrTreeNode(String name, List<JcrTreeNode> children) {
			this.name = name;
			this.children = children;
		}

		public String toString() {
			StringBuilder buffer = new StringBuilder(50);
			print(buffer, StringUtils.EMPTY, StringUtils.EMPTY);
			return buffer.toString();
		}

		private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
			buffer.append(prefix);
			buffer.append(name);
			buffer.append('\n');
			for (Iterator<JcrTreeNode> it = children.iterator(); it.hasNext(); ) {
				JcrTreeNode next = it.next();
				if (it.hasNext()) {
					next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
				} else {
					next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
				}
			}
		}
	}
}
