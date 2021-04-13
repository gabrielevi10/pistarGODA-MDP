package br.unb.cic.goda.model;

public class FormulaTreeModel {
    private String id;
    private FormulaTreeNode tree;

    public String getId() {
        return id;
    }

    public FormulaTreeNode getTree() {
        return tree;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTree(FormulaTreeNode tree) {
        this.tree = tree;
    }
}
