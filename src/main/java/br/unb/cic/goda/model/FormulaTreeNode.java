package br.unb.cic.goda.model;

import java.util.LinkedList;
import java.util.List;

public class FormulaTreeNode {
    public String id;
    public String formula;
    public String annotation;
    public List<FormulaTreeNode> subNodes = new LinkedList<>();
}