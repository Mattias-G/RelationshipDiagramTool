package gui.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gui.categories.Category;

public class Timestamp {
	private String name;
	
	private List<Node> nodes = new ArrayList<>();
	private List<Edge> edges = new ArrayList<>();
	
	public Timestamp(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public void removeCategory(Category toDelete, Category noCategory) {
		for (Node node : nodes) {
			if (node.getCategory() == toDelete) {
				node.setCategory(noCategory);
			}
		}
	}
	
	public void addNode(Node node) {
		nodes.add(node);
	}

	public void deleteNode(Node toDelete) {
		nodes.remove(toDelete);
		
		Iterator<Edge> i = edges.iterator();
		while (i.hasNext()) {
			Edge e = i.next();
			if (e.getStartNode() == toDelete || e.getEndNode() == toDelete)
				i.remove();
		}
	}
	
	public List<Node> getNodes() {
		return nodes;
	}

	public Node findNodeAtPoint(int x, int y) {
		for (Node node : nodes) { //TODO: invert order to give top component input first
			if (node.pointInBounds(x, y) && node.isVisible()) {
				return node;
			}	
		}
		return null;
	}

	public Node findNode(int nodeId) {
		for (Node node : nodes) {
			if (node.getId() == nodeId)
				return node;
		}
		return null;
	}

	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public void removeEdge(Edge toDelete) {
		edges.remove(toDelete);
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public Edge findEdgeAtPoint(int x, int y) {
		for (Edge edge : edges) { //TODO: invert order to give top component input first
			if (edge.pointInBounds(x, y) && edge.isVisible()) {
				return edge;
			}	
		}
		return null;
	}

	public boolean existsEdge(Node node1, Node node2) {
		for (Edge edge : edges) {
			if ((edge.getStartNode() == node1 || edge.getEndNode() == node1) &&
					(edge.getStartNode() == node2 || edge.getEndNode() == node2))
				return true;
		}
		return false;
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{\n" +
		"\"name\": \"" + name + "\"\n");
				
		sb.append("\"nodes\": [\n");

		for (int i = 0; i < nodes.size()-1; i++) {
			sb.append(nodes.get(i).toJson() + ",\n");
		}
		if (nodes.size() > 0)
			sb.append(nodes.get(nodes.size()-1).toJson() + "\n");

		sb.append("],\n"
				+ "\"edges\": [\n");

		for (int i = 0; i < edges.size()-1; i++) {
			sb.append(edges.get(i).toJson() + ",\n");
		}
		if (edges.size() > 0)
			sb.append(edges.get(edges.size()-1).toJson() + "\n");
		
		sb.append("]\n" +
				"}");
		return sb.toString();
	}
}
