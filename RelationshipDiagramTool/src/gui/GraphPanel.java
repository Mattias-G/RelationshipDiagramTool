package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import backend.Backend;
import gui.categories.Category;
import gui.graph.Edge;
import gui.graph.Node;
import gui.misc.DescriptionDialogHandler;
import gui.misc.SiblingActions;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements GuiPanel {
	public static final double DOUBLE_CLICK_TIME_MILLIS = 500;

	private Backend backend;
	private GuiPanelGroup siblings;
	private DescriptionDialogHandler descriptionDialogHandler;
	
	private Node selectedNode;
	private Edge selectedEdge;
	private List<Node> areaSelectedNodes = new ArrayList<>();
	
	private boolean dragging;
	private boolean editing;
	private boolean creatingEdge;
	private boolean panning;
	private boolean areaSelecting;

	private long lastLeftClickTime;
	private long lastRightClickTime;
	
	private int zoom = 1;
	
	private int viewX;
	private int viewY;

	private int panViewOriginPointX;
	private int panViewOriginPointY;
	private int panMouseOriginPointX;
	private int panMouseOriginPointY;
	
	private int selectionOriginPointX;
	private int selectionOriginPointY;
	
	private int mouseX;
	private int mouseY;
	
	public GraphPanel(Backend backend, DescriptionDialogHandler descriptionDialogHandler) {
		lastLeftClickTime = 0;
		lastRightClickTime = 0;
		
		this.backend = backend;
		this.descriptionDialogHandler = descriptionDialogHandler;

		MouseListener ml = new MouseListener();
		addMouseListener(ml);
		addMouseMotionListener(ml);
		addMouseWheelListener(ml);
		//Key listener is added later
	}
	
	public void registerKeyListener() {
		getTopLevelAncestor().addKeyListener(new KeyListener());
	}

	public void setSiblingComponent(GuiPanelGroup siblings) {
		this.siblings = siblings;
	}
	

	private void unselectNode() {
		if (selectedNode != null) {
			selectedNode.setSelected(false);
			selectedNode.setEditing(false);
			selectedNode = null;
		}
	}
	
	private void unselectEdge() {
		if (selectedEdge != null) {
			selectedEdge.setSelected(false);
			selectedEdge.setEditing(false);
			selectedEdge = null;
		}
	}

  private void selectNode(Node node)
  {
    selectedNode = node;
    if (!areaSelectedNodes.contains(node))
      unselectArea();
    node.setSelected(true);
  }

	private void selectNodesInArea(int x1, int y1, int x2, int y2) {
		for (Node node : backend.getNodes()) {
			if (node.isVisible() && node.coveredByArea(x1,y1,x2,y2)) {
				areaSelectedNodes.add(node);
				node.setSelected(true);
			}
		}
	}

  private void addNodesInAreaToSelection(int x1, int y1, int x2, int y2) {
    for (Node node : backend.getNodes()) {
      if (node.isVisible() && node.coveredByArea(x1,y1,x2,y2)) {
        addNodeToSelection(node);
      }
    }
  }
  
  private void addNodeToSelection(Node node) {
    if (areaSelectedNodes.isEmpty() && selectedNode != null) {
      areaSelectedNodes.add(selectedNode);
      selectedNode = null;
    }
      
    if (!node.isSelected) {
      areaSelectedNodes.add(node);
      node.setSelected(true);
    }
  }

  private void unselectArea() {
    if (!areaSelectedNodes.isEmpty()) {
      for (Node node : areaSelectedNodes) {
        node.setSelected(false);
      }
      areaSelectedNodes.clear();
    }
  }
	
	private int transformedX(int x) {
		int vx = (int)(getWidth()/2 * zoom - (getWidth()/2 - viewX));
		return x * zoom - vx;
	}
	
	private int transformedY(int y) {
		int vy = (int)(getHeight()/2 * zoom - (getHeight()/2 - viewY));
		return y * zoom - vy;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D)g;
				
		g2d.translate(getWidth()/2, getHeight()/2);
		g2d.scale(1.0/zoom, 1.0/zoom);
		g2d.translate(-getWidth()/2, -getHeight()/2);
		g2d.translate(viewX, viewY);

		if (creatingEdge) {
			g2d.setColor(Color.BLACK);
			Node n = backend.findNodeAtPoint(transformedX(mouseX), transformedY(mouseY));
			if (n != null)
				g2d.drawLine(selectedNode.getX(), selectedNode.getY(), n.getX(), n.getY());
			else
				g2d.drawLine(selectedNode.getX(), selectedNode.getY(), transformedX(mouseX), transformedY(mouseY));
		}
		
		for (Edge edge : backend.getEdges()) {
			edge.draw(g2d);
		}
		for (Node node : backend.getNodes()) {
			node.draw(g2d);
		}
		
		if (areaSelecting) {
			int dx = transformedX(mouseX) - selectionOriginPointX;
			int dy = transformedY(mouseY) - selectionOriginPointY;
			int x = dx >= 0 ? selectionOriginPointX : selectionOriginPointX + dx;
			int y = dy >= 0 ? selectionOriginPointY : selectionOriginPointY + dy;

			dx = Math.abs(dx);
			dy = Math.abs(dy);
			
			g2d.setColor(new Color(0, 0, 1, 0.2f));
			g2d.fillRect(x, y, dx, dy);
			g2d.setColor(Color.BLUE);
			g2d.drawRect(x, y, dx, dy);
		}
	}

	@Override
	public void unfocus() {
		if (selectedEdge != null || selectedNode != null || !areaSelectedNodes.isEmpty()) {
			unselectEdge();
			unselectNode();
			unselectArea();
			editing = false;
			repaint();
		}
	}
	
	@Override
	public boolean isEditing() {
		return editing;
	}
	
	private void leftClickOnNode(Node node, MouseEvent e) {
		// select
		if (e.isShiftDown()) {
			if (selectedNode != null)
				selectedNode.isEditing = false;
			addNodeToSelection(node);
		}
		else {
			selectNode(node);
			dragging = true;
		}
	}

	private void rightClickOnNode(Node node, MouseEvent e) {
		// select + draw edge or edit description
		if (e.isShiftDown()) {
			if (selectedNode != null)
				selectedNode.isEditing = false;
			addNodeToSelection(node);
		}
		else if (e.isControlDown()) {
			descriptionDialogHandler.showEditingDialogue(node);
			selectNode(node);
		}
		else {
			selectNode(node);
			creatingEdge = true;
		}
	}

	private void leftClickOnEdge(Edge edge, MouseEvent e) {
		// select
		if (!e.isShiftDown()) {
			unselectArea();
			edge.setSelected(true);
			selectedEdge = edge;
		}
	}

	private void rightClickOnEdge(Edge edge, MouseEvent e) {
		// select or edit description
		if (!e.isShiftDown()) {
			if (e.isControlDown()) {
				descriptionDialogHandler.showEditingDialogue(edge);
			}
			unselectArea();
			edge.setSelected(true);
			selectedEdge = edge;
		}
	}

	private void leftClickOnEmpty(int x, int y, MouseEvent e) {
		// select area
		if (selectedNode != null)
			selectedNode.isEditing = false;
		if (!e.isShiftDown())
			unselectArea();
		areaSelecting = true;
		selectionOriginPointX = x;
		selectionOriginPointY = y;
	}

	private void rightClickOnEmpty(int x, int y, MouseEvent e) {
		if (!e.isShiftDown())
			unselectArea();
	}
	
	private void leftDoubleClickOnNode(Node node, MouseEvent e) {
		// rename
		if (!e.isShiftDown()) {
			unselectArea();
			node.setSelected(true);
			node.setEditing(true);
			editing = true;
		}
	}

	private void rightDoubleClickOnNode(Node node, MouseEvent e) {
		// do nothing
	}

	private void leftDoubleClickOnEdge(Edge edge, MouseEvent e) {
		// rename
		if (!e.isShiftDown()) {
			edge.setEditing(true);
			editing = true;
		}
	}

	private void rightDoubleClickOnEdge(Edge edge, MouseEvent e) {
		// do nothing
		
	}

	private void leftDoubleClickOnEmpty(int x, int y, MouseEvent e) {
		// do nothing
	}

	private void rightDoubleClickOnEmpty(int x, int y, MouseEvent e) {
		// create new
		if (!e.isShiftDown()) {
			selectedNode = backend.createNode(x, y);
			selectedNode.setSelected(true);
			selectedNode.setEditing(true);
			editing = true;
		}
	}
	
	private class MouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			siblings.unfocus();
			
			if (e.getButton() != MouseEvent.BUTTON2) {
				if (!e.isShiftDown() && (selectedNode == null || !areaSelectedNodes.contains(selectedNode)))
					unselectNode();
				unselectEdge();
				creatingEdge = false;
				editing = false;
			}

			int x = transformedX(e.getX());
			int y = transformedY(e.getY());

			boolean isLeft = e.getButton() == MouseEvent.BUTTON1;
			boolean isRight = e.getButton() == MouseEvent.BUTTON3;
			boolean doubleLeft = false;
			boolean doubleRight = false;

			Node node = backend.findNodeAtPoint(x, y);
			Edge edge = backend.findEdgeAtPoint(x, y);

			// Select node
			if (node != null) {
				if (isLeft)
					leftClickOnNode(node, e);
				else if (isRight)
					rightClickOnNode(node, e);
			}
			// Select edge
			else if (edge != null) {
				if (isLeft)
					leftClickOnEdge(edge, e);
				else if (isRight)
					rightClickOnEdge(edge, e);
			}
			// Select empty
			else {
				if (isLeft)
					leftClickOnEmpty(x, y, e);
				else if (isRight)
					rightClickOnEmpty(x, y, e);
			}
			
			// Update double click
			if (isLeft) {
				if (System.currentTimeMillis() - lastLeftClickTime < DOUBLE_CLICK_TIME_MILLIS)
					doubleLeft = true;
				lastLeftClickTime = System.currentTimeMillis();
			} 
			else if (isRight) {
				if (System.currentTimeMillis() - lastRightClickTime < DOUBLE_CLICK_TIME_MILLIS)
					doubleRight = true;
				lastRightClickTime = System.currentTimeMillis();
			}
			
			// Double click on node
			if (node != null) {
				if (doubleLeft)
					leftDoubleClickOnNode(node, e);
				else if (doubleRight)
					rightDoubleClickOnNode(node, e);
			}
			// Double click on edge
			else if (edge != null) {
				if (doubleLeft)
					leftDoubleClickOnEdge(edge, e);
				else if (doubleRight)
					rightDoubleClickOnEdge(edge, e);
			}
			// Double click on empty
			else {
				if (doubleLeft)
					leftDoubleClickOnEmpty(x, y, e);
				else if (doubleRight)
					rightDoubleClickOnEmpty(x, y, e);
			}
			

			//pan if middle mouse button
			if (e.getButton() == MouseEvent.BUTTON2) {
				panning = true;
			}
			
			mouseX = e.getX();
			mouseY = e.getY();
			panMouseOriginPointX = mouseX;
			panMouseOriginPointY = mouseY;
			panViewOriginPointX = viewX;
			panViewOriginPointY = viewY;
			repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			int x = transformedX(e.getX());
			int y = transformedY(e.getY());
			
			dragging = false;
			panning = false;
			
			if (creatingEdge) {
				Node endNode = backend.findNodeAtPoint(x, y);
				if (endNode != null && endNode != selectedNode && 
						!backend.existsEdge(selectedNode, endNode)) {
					selectedEdge = backend.createEdge(selectedNode, endNode);
					selectedEdge.setSelected(true);
					selectedEdge.setEditing(true);
					editing = true;

					unselectNode();
				}
				creatingEdge = false;
				repaint();
			}
			
			if (areaSelecting) {
				int dx = transformedX(mouseX) - selectionOriginPointX;
				int dy = transformedY(mouseY) - selectionOriginPointY;
				int x0 = dx >= 0 ? selectionOriginPointX : selectionOriginPointX + dx;
				int y0 = dy >= 0 ? selectionOriginPointY : selectionOriginPointY + dy;
				dx = Math.abs(dx);
				dy = Math.abs(dy);
				
				if (e.isShiftDown())
					addNodesInAreaToSelection(x0, y0, x0+dx, y0+dy);
				else
					selectNodesInArea(x0, y0, x0+dx, y0+dy);

				areaSelecting = false;
				repaint();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int x = transformedX(e.getX());
			int y = transformedY(e.getY());
			
			if (dragging) {
				if (!areaSelectedNodes.isEmpty()) {
					int dx = x - selectedNode.getX();
					int dy = y - selectedNode.getY();
					for (Node node : areaSelectedNodes) {
						node.setPosition(node.getX() + dx, node.getY() + dy);
					}
				}
				else
					selectedNode.setPosition(x, y);
			}
			
			if (panning) {
				viewX = panViewOriginPointX + (e.getX() - panMouseOriginPointX) * zoom;
				viewY = panViewOriginPointY + (e.getY() - panMouseOriginPointY) * zoom;
			}
			
			mouseX = e.getX();
			mouseY = e.getY();
			
			if (dragging || panning || creatingEdge || areaSelecting)
				repaint();
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			zoom += e.getWheelRotation();
			zoom = Math.max(1, Math.min(zoom, 10));
			repaint();
		}
	}
	
	
	private class KeyListener extends KeyAdapter {
		@Override
		public void keyTyped(KeyEvent e) {
			if (editing) {
				if (selectedNode != null)
					selectedNode.keyTyped(e.getKeyChar());
				else
					selectedEdge.keyTyped(e.getKeyChar());
				repaint();
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			boolean repaint = false;
			
			if (!editing && e.getKeyCode() == KeyEvent.VK_DELETE) {
				if (!areaSelectedNodes.isEmpty()) {
					for (Node node : areaSelectedNodes) {
						backend.deleteNode(node);
					}
					areaSelectedNodes.clear();
					selectedNode = null;
					repaint = true;
				}
				else if (selectedNode != null) {
					backend.deleteNode(selectedNode);
					selectedNode = null;
					repaint = true;
				}
				else if (selectedEdge != null) {
					backend.deleteEdge(selectedEdge);
					selectedEdge = null;
					repaint = true;
				}
			}
			
			if (!editing && e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
				int i = e.getKeyCode() - KeyEvent.VK_1;
				if (backend.getCategories().size() > i) {
					if (!areaSelectedNodes.isEmpty()) {
						for (Node node : areaSelectedNodes) {
							node.setCategory(backend.getCategories().get(i));
						}
						repaint = true;
					}
					else if (selectedNode != null) {
						selectedNode.setCategory(backend.getCategories().get(i));
						repaint = true;
					}
				}
			}
			
			if (!editing && !siblings.anyoneEditing()) {
				if (!editing && !siblings.anyoneEditing() && (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD)) {
					backend.nextTimestamp();
					repaint = true;
					siblings.repaint();
				}
				if (!editing && !siblings.anyoneEditing() && (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_SUBTRACT)) {
					backend.prevTimestamp();
					repaint = true;
					siblings.repaint();
				}


				if (e.getKeyCode() == KeyEvent.VK_A && e.isControlDown()) {
					unselectNode();
					unselectEdge();
					unselectArea();
					areaSelectedNodes.addAll(backend.getNodes());
					for (Node node : areaSelectedNodes) {
						node.isSelected = true;
					}
					repaint = true;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
					if (!areaSelectedNodes.isEmpty()) {
						backend.copyNodes(areaSelectedNodes);
					}
					else if (selectedNode != null) {
						List<Node> nodes = new ArrayList<>();
						nodes.add(selectedNode);
						backend.copyNodes(nodes);
					}
				}

				if (e.getKeyCode() == KeyEvent.VK_V && e.isControlDown()) {
					unselectNode();
					unselectEdge();
					unselectArea();
					areaSelectedNodes = backend.pasteNodes(); 
					for (Node node : areaSelectedNodes) {
						node.isSelected = true;
					}
					repaint = true;
				}
			}
			
			if (editing && (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
					e.getKeyCode() == KeyEvent.VK_ENTER)) {
				editing = false;
				if (selectedNode != null)
					selectedNode.setEditing(false);
				else
					selectedEdge.setEditing(false);
				repaint = true;
			}
				
			if ((selectedNode != null || selectedEdge != null || !areaSelectedNodes.isEmpty()) && 
					e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				unselectNode();
				unselectEdge();
				unselectArea();
				repaint = true;
			}
			
			if (repaint)
				repaint();
		}
	}


	@Override
	public void performAction(SiblingActions actionCode, Object... params) {
		if (actionCode == SiblingActions.setCategory) {
			if (!areaSelectedNodes.isEmpty()) {
				for (Node node : areaSelectedNodes) {
					node.setCategory((Category)params[0]);
				}
			}
			else if (selectedNode != null) 
				selectedNode.setCategory((Category)params[0]);
		}
	}
	
}
