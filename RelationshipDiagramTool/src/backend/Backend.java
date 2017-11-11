package backend;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gui.categories.Category;
import gui.graph.Edge;
import gui.graph.Node;
import gui.graph.Timestamp;

public class Backend {
	private int idCounter;
	private List<Category> categories = new ArrayList<>();
	private List<Timestamp> timestamps = new ArrayList<>();
	
	private Category defaultCategory;
	private Category noCategory;
	
	private int currentTime;

	public Backend() {
		noCategory = new Category(idCounter++);
		noCategory.setColor(new Color(0.7f, 0.7f, 0.7f));
		
		defaultCategory = new Category(idCounter++);
		categories.add(defaultCategory);
		
		timestamps.add(new Timestamp(0, "Time step " + timestamps.size()));
		currentTime = 0;
	}


	public void reset() {
		categories.clear();
		timestamps.clear();
		idCounter = 1;
		defaultCategory = noCategory;
		timestamps.add(new Timestamp(0, "Time step " + timestamps.size()));
		currentTime = 0;
	}

	public void save(File file) {
		boolean asJson = file.getName().endsWith(".json");
		if (asJson)
			saveAsJson(file);
		else
			saveAsString(file);
	}
	
	private void saveAsString(File file) {
		try {
			FileWriter fw = new FileWriter(file);

			fw.write("Categories\n");
			for (Category category : categories) {
				fw.write(category.toString() + "\n");
			}
			fw.write("Timestamps\n");
			for (Timestamp timestamp : timestamps) {
				fw.write("#\n");
				fw.write(timestamp.toString() + "\n");
								
				fw.write("Nodes\n");
				for (Node node : timestamp.getNodes()) {
					fw.write(node.toString() + "\n");
				}
				fw.write("Edges\n");
				for (Edge edge : timestamp.getEdges()) {
					fw.write(edge.toString() + "\n");
				}
			}
			
			fw.close();
		} catch (IOException e) {
			System.out.println("Error when saving to file \"" + file + "\"");
			e.printStackTrace();
		}
	}
	
	private void saveAsJson(File file) {
		try {
			FileWriter fw = new FileWriter(file);

			fw.write("{\n"
					+ "\"categories\": [\n");
			
			for (int i = 0; i < categories.size()-1; i++) {
				fw.write(categories.get(i).toJson() + ",\n");
			}
			if (categories.size() > 0)
				fw.write(categories.get(categories.size()-1).toJson() + "\n");

			fw.write("],\n"
					+ "\"timestamps\": [\n");
			for (int i = 0; i < timestamps.size()-1; i++) {
				fw.write(timestamps.get(i).toJson() + ",\n");
			}
			
			if (timestamps.size() > 0)
				fw.write(timestamps.get(timestamps.size()-1).toJson() + "\n");
			
			fw.write("]\n"
					+ "}\n");
			
			fw.close();
		} catch (IOException e) {
			System.out.println("Error when saving to file \"" + file + "\"");
			e.printStackTrace();
		}
	}
	
	public void load(File file) {
		timestamps.clear();
		
		System.out.println("Loading file: " + file);
		boolean fromJson = file.getName().endsWith(".json");
		if (fromJson)
			loadFromJson(file);
		else
			loadFromString(file);
	}

	private void loadFromString(File file) {
		reset();
		
		int step = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while (reader.ready()) {
				String line = reader.readLine();
				if (line.equals("Categories"))
					step = 1;
				else if (line.equals("Timestamps"))
					step = -1;
				else if (line.equals("#"))
					step = 2;
				else if (line.equals("Nodes"))
					step = 3;
				else if (line.equals("Edges"))
					step = 4;
				else if (step == 1) {
					String[] tokens = line.split(" ");
					int i = tokens[0].length() + tokens[1].length() + tokens[2].length() + 3;
					createCategory(
							Integer.parseInt(tokens[0]),
							Boolean.parseBoolean(tokens[1]),
							Integer.parseInt(tokens[2]),
							line.substring(i));
				}
				else if (step == 2) {
					String[] tokens = line.split(" ");
					int i = tokens[0].length() + 1;
					createTimestamp(
							Integer.parseInt(tokens[0]),
							line.substring(i));
				}
				else if (step == 3) {
					String[] tokens = line.split(" ");
					int i = tokens[0].length() + tokens[1].length() + 
							tokens[2].length() + tokens[3].length() + 4;
					createNode(
							Integer.parseInt(tokens[0]),
							Integer.parseInt(tokens[1]),
							Integer.parseInt(tokens[2]),
							findCategory(Integer.parseInt(tokens[3])),
							line.substring(i));
				}
				else if (step == 4) {
					String[] tokens = line.split(" ");
					int i = tokens[0].length() + tokens[1].length() + tokens[2].length() + 3;
					createEdge(
							Integer.parseInt(tokens[0]),
							findNode(Integer.parseInt(tokens[1])),
							findNode(Integer.parseInt(tokens[2])),
							line.substring(i));
				}
				else if (step != -1) {
					reset();
					System.out.println("Error when loading file \"" + file + "\".\n"
							+ "Could not categorize line: " + line);
				}
				
			}
		} catch (IOException e) {
			System.out.println("Error when loading file \"" + file + "\"");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("Error when parsing file \"" + file + "\"");
			e.printStackTrace();
		}
	}
	
	private void loadFromJson(File file) {
		reset();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.readLine();
			
			//Read categories
			reader.readLine();
			while (reader.readLine().equals("{")) {
				String[] tokens = new String[4];
				getTokens(reader, tokens);
						
				createCategory(
						Integer.parseInt(tokens[0]),
						Boolean.parseBoolean(tokens[1]),
						Integer.parseInt(tokens[2]),
						tokens[3]);
				
				reader.readLine();
			}
			
			//Read timestamps
			reader.readLine();
			while (reader.readLine().equals("{")) {
				String[] tokens = new String[2];
				getTokens(reader, tokens);
						
				createTimestamp(
						Integer.parseInt(tokens[0]),
						tokens[1]);
							
				//Read nodes
				reader.readLine();
				while (reader.readLine().equals("{")) {
					tokens = new String[5];
					getTokens(reader, tokens);
							
					createNode(
							Integer.parseInt(tokens[0]),
							Integer.parseInt(tokens[1]),
							Integer.parseInt(tokens[2]),
							findCategory(Integer.parseInt(tokens[3])),
							tokens[4]);
					
					reader.readLine();
				}
				
				//Read edges
				reader.readLine();
				while (reader.readLine().equals("{")) {
					tokens = new String[4];
					getTokens(reader, tokens);
					
					createEdge(
							Integer.parseInt(tokens[0]),
							findNode(Integer.parseInt(tokens[1])),
							findNode(Integer.parseInt(tokens[2])),
							tokens[3]);
					
					reader.readLine();
				}

				reader.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error when loading file \"" + file + "\"");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("Error when parsing file \"" + file + "\"");
			e.printStackTrace();
		}
	}

	private void getTokens(BufferedReader reader, String[] tokens) throws IOException {
		for (int i = 0; i < tokens.length; i++) {
			String line = reader.readLine();
			if (i == tokens.length-1)
				tokens[i] = line.substring(line.indexOf(':')+3,line.length()-1);
			else
				tokens[i] = line.substring(line.indexOf(':')+2,line.length()-1);
		}
	}


	public Category createCategory() {
		Category category = new Category(idCounter++);
		categories.add(category);
		
		if (defaultCategory == noCategory)
			defaultCategory = category;
		
		return category;
	}
	
	private void createCategory(int id, boolean visible, int color, String name) {
		Category category = new Category(id);
		category.setVisible(visible);
		category.setColor(new Color(color));
		category.setName(name);
		categories.add(category);
		
		if (defaultCategory == noCategory)
			defaultCategory = category;
		
		if (id >= idCounter)
			idCounter = id+1;
	}
	
	public void deleteCategory(Category toDelete) {
		categories.remove(toDelete);
		if (toDelete == defaultCategory) {
			if (categories.isEmpty())
				defaultCategory = noCategory;
			else
				defaultCategory = categories.get(0);
		}
		
		
		for (Timestamp timestamp : timestamps) {
			timestamp.removeCategory(toDelete, noCategory);
		}
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setDefautCategory(Category category) {
		defaultCategory = category;
	}

	public Category getDefaultCategory() {
		return defaultCategory;
	}
	
	private Category findCategory(int id) {
		for (Category category : categories) {
			if (category.getId() == id)
				return category;
		}
		return noCategory;
	}
	

	public Timestamp createTimestamp(int id, String name) {
		Timestamp timestamp = new Timestamp(id, name);
		timestamps.add(timestamp);
		currentTime = timestamps.size()-1;
		return timestamp;
	}
	
	public Node createNode(int x, int y) {
		Node node = new Node(x, y, defaultCategory, idCounter++);
		timestamps.get(currentTime).addNode(node);
		return node;
	}

	private void createNode(int id, int x, int y, Category category, String name) {
		Node node = new Node(x, y, category, id);
		node.setName(name);
		timestamps.get(currentTime).addNode(node);

		if (id >= idCounter)
			idCounter = id+1;
	}
	
	public void deleteNode(Node toDelete) {
		timestamps.get(currentTime).deleteNode(toDelete);
	}
	
	public List<Node> getNodes() {
		return timestamps.get(currentTime).getNodes();
	}
	
	public Node findNodeAtPoint(int x, int y) {
		return timestamps.get(currentTime).findNodeAtPoint(x, y);
	}

	private Node findNode(int id) {
		return timestamps.get(currentTime).findNode(id);
	}
	
	
	public Edge createEdge(Node start, Node end) {
		Edge edge = new Edge(start, end, idCounter++);
		timestamps.get(currentTime).addEdge(edge);
		return edge;
	}

	private void createEdge(int id, Node start, Node end, String name) {
		Edge edge = new Edge(start, end, id);
		edge.setName(name);
		timestamps.get(currentTime).addEdge(edge);

		if (id >= idCounter)
			idCounter = id+1;
	}

	public void deleteEdge(Edge toDelete) {
		timestamps.get(currentTime).removeEdge(toDelete);
	}

	public List<Edge> getEdges() {
		return timestamps.get(currentTime).getEdges();
	}
	
	public Edge findEdgeAtPoint(int x, int y) {
		return timestamps.get(currentTime).findEdgeAtPoint(x, y);
	}

	public boolean existsEdge(Node node1, Node node2) {
		return timestamps.get(currentTime).existsEdge(node1, node2);
	}
}
