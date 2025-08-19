package cn.sparrowmini.common;

import java.util.ArrayList;
import java.util.List;

public class SparrowTree<T, ID> {

	private ID id;

	private String name;
	private T me;
	private int level;
	private int childCount;
	private ID previousNodeId;
	private ID nextNodeId;
	private List<SparrowTree<T, ID>> children = new ArrayList<SparrowTree<T, ID>>();

	public SparrowTree(T me) {
		this.me = me;
	}

	public SparrowTree(String name, int level) {
		this.name = name;
		this.level = level;
	}

	public SparrowTree(T me, String name, int level) {
		this.me = me;
		this.name = name;
		this.level = level;
	}

	public SparrowTree(ID id, String name) {
		this.id = id;
		this.name = name;
	}

	public SparrowTree(T object, ID id) {
		this.me = object;
		this.id = id;
	}

	public SparrowTree(ID id, String name, ID previousNodeId, ID nextNodeId) {
		this.id = id;
		this.nextNodeId = nextNodeId;
		this.previousNodeId = previousNodeId;
	}

	public SparrowTree(ID id, ID previousNodeId, ID nextNodeId) {
		this.id = id;
		this.nextNodeId = nextNodeId;
		this.previousNodeId = previousNodeId;
	}

	public SparrowTree(T object, ID id, ID previousNodeId, ID nextNodeId) {
		this.id = id;
		this.me = object;
		this.nextNodeId = nextNodeId;
		this.previousNodeId = previousNodeId;
	}
	
	

	public SparrowTree() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ID getId() {
		return id;
	}

	public void setId(ID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T getMe() {
		return me;
	}

	public void setMe(T me) {
		this.me = me;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getChildCount() {
		return childCount;
	}

	public void setChildCount(int childCount) {
		this.childCount = childCount;
	}

	public ID getPreviousNodeId() {
		return previousNodeId;
	}

	public void setPreviousNodeId(ID previousNodeId) {
		this.previousNodeId = previousNodeId;
	}

	public ID getNextNodeId() {
		return nextNodeId;
	}

	public void setNextNodeId(ID nextNodeId) {
		this.nextNodeId = nextNodeId;
	}

	public List<SparrowTree<T, ID>> getChildren() {
		return children;
	}

	public void setChildren(List<SparrowTree<T, ID>> children) {
		this.children = children;
	}

}
